/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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

import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


public class TestSeleniumRobotLogger extends MockitoTest {

	
	/**
	 * Check that in DEV mode, debug logs are displayed
	 * @throws IOException 
	 */ 
	@Test(groups= {"ut"})
	public void testLogInDevMode() throws IOException {
		try {
			SeleniumRobotLogger.reset();
			System.setProperty(SeleniumRobotLogger.INTERNAL_DEBUG, "core");
			SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory());
			
			Logger logger = spy(SeleniumRobotLogger.getLogger(TestSeleniumRobotLogger.class));
			
			logger.info(SeleniumRobotLogger.START_TEST_PATTERN + "testLogInDevMode");
			logger.info("some info");
			logger.debug("some debug");
			logger.info(SeleniumRobotLogger.END_TEST_PATTERN + "testLogInDevMode");

			// check log file content
			SeleniumRobotLogger.parseLogFile();
			String logs = SeleniumRobotLogger.getTestLogs().get("testLogInDevMode");
			Assert.assertTrue(logs.contains("some info"));
			Assert.assertTrue(logs.contains("some debug"));
			
		} finally {
			System.clearProperty(SeleniumRobotLogger.INTERNAL_DEBUG);
			SeleniumRobotLogger.reset();
		}
	}
	
	/**
	 * Check that in RUN mode, debug logs are not displayed
	 * @throws IOException 
	 */
	@Test(groups= {"ut"})
	public void testLogInRunMode() throws IOException {
		try {
			SeleniumRobotLogger.reset();
			System.setProperty(SeleniumRobotLogger.INTERNAL_DEBUG, "none");
			SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory());
			
			Logger logger = spy(SeleniumRobotLogger.getLogger(TestSeleniumRobotLogger.class));
			
			logger.info(SeleniumRobotLogger.START_TEST_PATTERN + "testLogInRunMode");
			logger.info("some info");
			logger.debug("some debug");
			logger.info(SeleniumRobotLogger.END_TEST_PATTERN + "testLogInRunMode");

			// check log file content
			SeleniumRobotLogger.parseLogFile();
			String logs = SeleniumRobotLogger.getTestLogs().get("testLogInRunMode");
			Assert.assertTrue(logs.contains("some info"));
			Assert.assertFalse(logs.contains("some debug"));
			
		} finally {
			System.clearProperty(SeleniumRobotLogger.INTERNAL_DEBUG);
			SeleniumRobotLogger.reset();
		}
	}
	
	@Test(groups= {"ut"})
	public void testSystemOutRedirectedToLogger() throws IOException {
		try {
			SeleniumRobotLogger.reset();
			System.setProperty(SeleniumRobotLogger.INTERNAL_DEBUG, "none");
			SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory());


			logger.info(SeleniumRobotLogger.START_TEST_PATTERN + "testSystemOutRedirectedToLogger");
			System.out.println("information");
			System.err.println("error");
			logger.info(SeleniumRobotLogger.END_TEST_PATTERN + "testSystemOutRedirectedToLogger");
			
			// check log file content
			SeleniumRobotLogger.parseLogFile();
			String logs = SeleniumRobotLogger.getTestLogs().get("testSystemOutRedirectedToLogger");
			Assert.assertTrue(logs.contains("Sys$Out: information"));
			Assert.assertTrue(logs.contains("Sys$Error: error"));
			
		} finally {
			System.clearProperty(SeleniumRobotLogger.INTERNAL_DEBUG);
			SeleniumRobotLogger.reset();
		}
	}
	
	/**
	 * #issue #192: check not NPE is raised when cleaning default output directory which does not exists
	 * This occurs when default output directory is not the same as the user defined output directory
	 * @throws IOException 
	 */
	@Test(groups= {"ut"})
	public void testCleanOutputDir() throws IOException {

		try {
			// delete default output directory so that it does not exist when trying to clean it
			try {
				FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory()));
				WaitHelper.waitForSeconds(1);
			} catch (IOException e) {
				// do nothing
			}
			
			SeleniumRobotLogger.reset();
			SeleniumRobotLogger.updateLogger(Paths.get(System.getProperty("java.io.tmpdir"), "SR").toString(), SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory());

			
		} finally {
			SeleniumRobotLogger.reset();
		}
	}
}
