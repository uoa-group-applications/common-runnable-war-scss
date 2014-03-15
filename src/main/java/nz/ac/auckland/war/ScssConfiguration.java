package nz.ac.auckland.war;

import com.bluetrainsoftware.classpathscanner.ClasspathScanner;
import com.bluetrainsoftware.classpathscanner.ResourceScanListener;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This just lets us look for SCSS directories. They are in src/main/webapp/scss, src/test/webapp/scss or
 * src/main/resources/META-INF/resources/scss. We need to check if they have been compiled. All artifacts that we detect
 * are open IDE artifacts and not jars or other directories, we check if they have an scss directory that hasn't been compiled.
 *
 * @author: Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class ScssConfiguration extends AbstractConfiguration {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ScssConfiguration.class);

	// hopefully it will work on Windows now
	private static final String TARGET_CLASSES = "/target/classes".replace('/', File.separatorChar);
	private static final String SRC_MAIN_RESOURCES_META_INF_RESOURCES_SCSS = "src/main/resources/META-INF/resources/scss".replace('/', File.separatorChar);
	private static final String TARGET_CLASSES_META_INF_RESOURCES_CSS = "target/classes/META-INF/resources/css".replace('/', File.separatorChar);

	private List<ScssCheck> scssFilesChecks = new ArrayList<>();

	class ScssCheck {
		final File scssDir;
		final File pomDir;

		public ScssCheck(File resourceDir, File pomDir) {
			this.scssDir = resourceDir;
			this.pomDir = pomDir;
		}
	}

	@Override
	public void preConfigure(final WebAppContext context) throws Exception {
		if (System.getProperty(WebAppRunner.WEBAPP_WAR_FILENAME) == null) {
			log.info("Registering scanner for uncompiled SCSS directories");

			ClasspathScanner.getInstance().registerResourceScanner(new ResourceScanListener() {
				@Override
				public List<ScanResource> resource(List<ScanResource> scanResources) throws Exception {
					return null;  // don't need contents of any files
				}

				@Override
				public void deliver(ScanResource desire, InputStream inputStream) {
				}

				@Override
				public InterestAction isInteresting(InterestingResource interestingResource) {
					if (interestingResource.directory != null )  {
						if (interestingResource.directory.getPath().endsWith(TARGET_CLASSES)) {
							File pomDir = interestingResource.directory.getParentFile().getParentFile();
							File scssDir = new File(pomDir, SRC_MAIN_RESOURCES_META_INF_RESOURCES_SCSS);
							if (scssDir.exists() && scssDir.isDirectory()) {
								scssFilesChecks.add(new ScssCheck(scssDir, interestingResource.directory.getParentFile().getParentFile()));
							}
						}
					}

					return InterestAction.ONCE;
				}

				@Override
				public void scanAction(ScanAction action) {
					if (action == ScanAction.COMPLETE) {
						processScssChecks(context);
					}
				}
			});
		}
	}

	/**
	 * determines if this source path (which all directories are) is a Web resources directory and this should have CSS
	 *
	 * @param context - the web application context
	 * @throws IOException
	 */
	private void processScssChecks(final WebAppContext context) {
		List<Resource> theResources = (List<Resource>) context.getAttribute(ScanConfiguration.RESOURCE_URLS);

		for (ScssCheck check : scssFilesChecks) {
			File cssDir = new File(check.pomDir, TARGET_CLASSES_META_INF_RESOURCES_CSS);

			if (!cssDir.exists()) {
				throw new RuntimeException("SCSS/SASS directory found at " + check.scssDir.getAbsolutePath() + " but no matching CSS directory found at " +
					cssDir.getAbsolutePath() + " - please ensure you are running \"mvn sass:watch\" or \"mvn process-resources\" for each open web fragment project.");
			} else {
				log.debug("webapp.scan: added scss directory resource {}", cssDir.getParentFile().getAbsolutePath());
				theResources.add(new ScssResource(cssDir.getParentFile().toURI()));
			}
		}
	}
}
