package nz.ac.auckland.war;

import org.eclipse.jetty.util.resource.FileResource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * This enables us to serve only css resources from the target/classes/META-INF/resources directory
 *
 * @author: Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class ScssResource extends FileResource {
	public ScssResource(URL url) throws IOException, URISyntaxException {
		super(url);
	}

	public ScssResource(URI uri) {
		super(uri);
	}

	/**
	 * only return .css files
	 *
	 * @return - array of css files
	 */
	@Override
	public String[] list() {
		File _file = getFile();

		String[] list =_file.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".css") || dir.isDirectory();
			}
		});

		if (list==null)
			return null;

		for (int i=list.length;i-->0;)
		{
			if (new File(_file,list[i]).isDirectory() &&
				!list[i].endsWith("/"))
				list[i]+="/";
		}

		return list;
	}
}
