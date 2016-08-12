package com.seleniumtests.core.runner;

public class SeleniumRobotRunner {
	
	private static boolean cucumberTest = false;
	
	private SeleniumRobotRunner() {
		
	}

	public static boolean isCucumberTest() {
		return cucumberTest;
	}
	
	public static void setCucumberTest(boolean cucumberTest) {
		SeleniumRobotRunner.cucumberTest = cucumberTest;
	}
}
