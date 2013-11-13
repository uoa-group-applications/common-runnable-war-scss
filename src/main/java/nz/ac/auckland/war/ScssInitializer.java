package nz.ac.auckland.war;

import bathe.BatheInitializer;

/**
 * Ensures that the Scss class is on the list of extras.
 *
 * @author: Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class ScssInitializer implements BatheInitializer {
	@Override
	public int getOrder() {
		return 20;
	}

	@Override
	public String getName() {
		return "scss-devmode";
	}

	@Override
	public String[] initialize(String[] args, String jumpClass) {
		String configClasses = System.getProperty(WebAppRunner.WEBAPP_EXTRA_CONFIGURATION_CLASSES, "");

		if (configClasses.length() > 0) {
			configClasses += ";";
		}

		System.setProperty(WebAppRunner.WEBAPP_EXTRA_CONFIGURATION_CLASSES, configClasses + ScssConfiguration.class.getName());

		return args;
	}
}
