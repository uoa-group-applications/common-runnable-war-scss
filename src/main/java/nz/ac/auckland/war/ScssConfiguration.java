package nz.ac.auckland.war;

import com.bluetrainsoftware.classpathscanner.ClasspathScanner;
import com.bluetrainsoftware.classpathscanner.ResourceScanListener;
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
		if ("true".equals(System.getProperty(WebAppRunner.DEVMODE))) {
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
				public boolean isInteresting(URL url) {
					String resource = url.toString();

					if (resource.startsWith("file:")) {
						File file = new File(resource.substring("file:".length()));
						if (file.isDirectory() && resource.endsWith("/target/classes")) {
							File pomDir = file.getParentFile().getParentFile();
							File scssDir = new File(pomDir, "src/main/resources/META-INF/resources/scss");
							if (scssDir.exists() && scssDir.isDirectory()) {
								scssFilesChecks.add(new ScssCheck(scssDir, file.getParentFile().getParentFile()));
							}
						}
					}

					return false;
				}

				@Override
				public boolean removeListenerOnScanCompletion() {
					processScssChecks(context);

					return true;
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
		for (ScssCheck check : scssFilesChecks) {
			if (new File(check.scssDir, "scss").exists()) {
				File cssDir = new File(check.pomDir, "target/classes/META-INF/resources/css");

				if (!cssDir.exists()) {
					throw new RuntimeException("SCSS/SASS directory found at " + check.scssDir.getAbsolutePath() + " but no matching CSS directory found at " +
						cssDir.getAbsolutePath() + " - please ensure you are running \"mvn sass:watch\" or \"mvn process-resources\" for each open web fragment project.");
				}
			}
		}
	}
}
