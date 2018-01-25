package com.seleniumtests.it.stubclasses;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;

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
	
	@When("write_error (\\w+)")
	public void writeTextWithError(String text) {
		throw new WebDriverException("no element found");
	}
}
