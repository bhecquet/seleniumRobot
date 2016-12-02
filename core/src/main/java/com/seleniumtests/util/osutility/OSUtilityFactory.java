package com.seleniumtests.util.osutility;

public class OSUtilityFactory {
	
	private OSUtilityFactory() {
		// nothing to do
	}

	public static OSUtility getInstance() {
		if (OSUtility.isWindows()){
			return new OSUtilityWindows();
		} else {
			return new OSUtilityUnix();
		}
	}
}
