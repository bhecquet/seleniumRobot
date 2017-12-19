package com.seleniumtests.core.runner;

public class SeleniumRobotCucumberTestListener extends SeleniumRobotTestListener {

	public void configureCucumberTest() {
		SeleniumRobotTestPlan.setCucumberTest(true);
	}
}
