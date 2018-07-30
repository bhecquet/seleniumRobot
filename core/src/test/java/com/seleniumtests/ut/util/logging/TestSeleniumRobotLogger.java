/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.ut.util.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


public class TestSeleniumRobotLogger extends MockitoTest {

	public void doBeforeMethod(final Method method) {
		if ("testLogInDevMode".equals(method.getName())) {
			System.setProperty("devMode", "true");
		} else {
			System.setProperty("devMode", "false");
		}
	}
	
	/**
	 * Check that in DEV mode, debug logs are displayed
	 */
	@Test(groups= {"ut"})
	public void testLogInDevMode() {
		try {
			SeleniumRobotLogger.reset();
			SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory());
			
			Logger logger = spy(SeleniumRobotLogger.getLogger(TestSeleniumRobotLogger.class));
			
			logger.info(SeleniumRobotLogger.START_TEST_PATTERN + "testLogInDevMode");
			logger.info("some info");
			logger.debug("some debug");
			logger.info(SeleniumRobotLogger.END_TEST_PATTERN + "testLogInDevMode");
			
			verify(logger, times(4)).callAppenders(any(LoggingEvent.class));
			
			// check log file content
			SeleniumRobotLogger.parseLogFile();
			String logs = SeleniumRobotLogger.getTestLogs().get("testLogInDevMode");
			Assert.assertTrue(logs.contains("some info"));
			Assert.assertTrue(logs.contains("some debug"));
			
		} finally {
			System.clearProperty("devMode");
			SeleniumRobotLogger.reset();
		}
	}
	
	/**
	 * Check that in RUN mode, debug logs are not displayed
	 */
	@Test(groups= {"ut"})
	public void testLogInRunMode() {
		try {
			SeleniumRobotLogger.reset();
			SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory());
			
			Logger logger = spy(SeleniumRobotLogger.getLogger(TestSeleniumRobotLogger.class));
			
			logger.info(SeleniumRobotLogger.START_TEST_PATTERN + "testLogInRunMode");
			logger.info("some info");
			logger.debug("some debug");
			logger.info(SeleniumRobotLogger.END_TEST_PATTERN + "testLogInRunMode");
			
			verify(logger, times(3)).callAppenders(any(LoggingEvent.class));
			
			// check log file content
			SeleniumRobotLogger.parseLogFile();
			String logs = SeleniumRobotLogger.getTestLogs().get("testLogInRunMode");
			Assert.assertTrue(logs.contains("some info"));
			Assert.assertFalse(logs.contains("some debug"));
			
		} finally {
			System.clearProperty("devMode");
			SeleniumRobotLogger.reset();
		}
	}
}
