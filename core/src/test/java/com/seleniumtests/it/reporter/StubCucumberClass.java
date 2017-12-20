package com.seleniumtests.it.reporter;

import org.apache.log4j.Logger;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import cucumber.api.java.en.When;

public class StubCucumberClass {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(StubCucumberClass.class);

	@When("write (\\w+)")
	public void writeText(String text) {
		logger.info("write " + text);
		WaitHelper.waitForSeconds(1);
	}
}
