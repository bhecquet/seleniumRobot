package com.seleniumtests.it.reporter;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotRunner;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class StubParentClass {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotRunner.class);

	/**
	 * Generate context to have logger correctly initialized
	 * @param testContext
	 */
	@BeforeSuite(groups="stub")
	public void initSuite(final ITestContext testContext) {

        SeleniumTestsContextManager.initGlobalContext(testContext);
        SeleniumTestsContextManager.initThreadContext(testContext);

		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
        SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
	}
	
	/**
	 * Simulate SeleniumRobotRunner logs for log parsing feature
	 * @param method
	 */
	@BeforeMethod(groups="stub")
	public void beforeTestMethod(final Method method) {
		logger.info(SeleniumRobotLogger.START_TEST_PATTERN + method.getName());
	}
	
	/**
	 * Simulate SeleniumRobotRunner logs for log parsing feature
	 * @param method
	 */
	@AfterMethod(groups="stub")
	public void afterTestMethod(final Method method) {
		logger.info(SeleniumRobotLogger.END_TEST_PATTERN + method.getName());
	}
	
	@AfterSuite(groups="stub")
	public void finishSuite() {
		
		try {
			SeleniumRobotLogger.parseLogFile();
		} catch (IOException e) {
			logger.error("cannot read log file");
		}
	}
}
