/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
