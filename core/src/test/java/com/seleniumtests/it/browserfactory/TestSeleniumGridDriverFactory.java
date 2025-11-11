/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.browserfactory;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.reporter.ReporterTest;


public class TestSeleniumGridDriverFactory extends ConnectorsTest {
	
	/**
	 * Test standard case, a driver session is get
	 */
	@Test(groups={"it"})
	public void testSessionGet() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");
			
			WebUIDriver uiDriver = createMockedWebDriver();
			
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");

			createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200,

					// session found
					String.format(GRID_STATUS_WITH_SESSION, "abcdef"));
		
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});

			// check that we tried to create the driver twice (the bug is based on the fact that we create it only once)
			verify(uiDriver).createWebDriver();
			
			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertEquals(StringUtils.countMatches(logs, "Start creating *chrome driver"), 1);
			Assert.assertTrue(logs.contains("Test is OK"));
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
		
	}
	
	/**
	 * issue #287: check driver is recreated if session cannot be get
	 */
	@Test(groups={"it"})
	public void testSessionNotGetOnFirstTime() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");
			
			WebUIDriver uiDriver = createGridHubMockWithNodeOK();

			// make the session information fail on first time
			createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200,
					// session not found (null)
					GRID_STATUS_NO_SESSION, // called when we get grid status
					GRID_STATUS_NO_SESSION, // first error
					GRID_STATUS_NO_SESSION, // second error
					// session found (id provided)
					String.format(GRID_STATUS_WITH_SESSION, "abcdef"));
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			// check that we tried to create the driver once (we should have retried driver creation on grid inside 'createWebDriver' call)
			verify(uiDriver, times(1)).createWebDriver();

			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertEquals(StringUtils.countMatches(logs, "Start creating *chrome driver"), 1); // driver is really created once, but, from WebUIDriver point of view, there was only one call
			Assert.assertTrue(logs.contains("Could not start a new session. Could not get session information from grid")); // check error on first creation is displayed
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * issue #311: Check we do not restart a test when no node is available to handle the request
	 */
	@Test(groups={"it"})
	public void testSessionNeverGet() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID_TIMEOUT, "1");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");
			createMockedWebDriver();
			
			// grid is there but we cannot get any matching node
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 500, "{}");
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			// check the test is not retried when 
			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertFalse(logs.contains("Retrying 1 time"));
	
		} finally {

			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * issue #311: Check that after 3 tests that cannot get there node, we skip all the remaining tests
	 */
	@Test(groups={"it"})
	public void testTestsAreSkippedIfNodeIsNeverAvailable() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID_TIMEOUT, "1");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");
			createMockedWebDriver();
			
			// grid is there but we cannot get any matching node
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 500, "{}");
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.NONE, new String[] {"testDriverShort", "testDriverShort2", "testDriverShort3", "testDriverShort4"});
			
			// check the test is not retried when 
			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertFalse(logs.contains("Retrying 1 time"));
			
			// check the 4th test is skipped but other are not
			Assert.assertTrue(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [0 times])"));
			Assert.assertTrue(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [1 times])"));
			Assert.assertTrue(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [2 times])"));
			Assert.assertFalse(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [3 times])"));
			Assert.assertTrue(logs.contains("Error creating driver, skip test (cause: Skipping as the 3 previous tests could not get any matching node. Check your test configuration and grid setup)"));
			
		} finally {

			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * issue #311: Check that after 3 tests that cannot get there node, we skip all the remaining tests
	 */
	@Test(groups={"it"})
	public void testTestsAreNotSkippedIfOneNodeIsGet() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID_TIMEOUT, "1");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");
			createMockedWebDriver();
			
			// grid is there
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
			createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, 
					// node is not there 2 times
					"{"
					+ "  \"value\": {"
					+ "    \"ready\": false,"
					+ "    \"message\": \"Selenium Grid ready.\","
					+ "    \"nodes\": ["
					+ "    ]"
					+ "  }"
					+ "}",
					"{"
					+ "  \"value\": {"
					+ "    \"ready\": false,"
					+ "    \"message\": \"Selenium Grid ready.\","
					+ "    \"nodes\": ["
					+ "    ]"
					+ "  }"
					+ "}"
					,
					// session found
					String.format(GRID_STATUS_WITH_SESSION, "abcdef"));

			
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.NONE, new String[] {"testDriverShort", "testDriverShort2", "testDriverShort3", "testDriverShort4"});
			
			String logs = ReporterTest.readSeleniumRobotLogFile();

			Assert.assertTrue(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [0 times])"));
			Assert.assertTrue(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [1 times])"));
			Assert.assertFalse(logs.contains("Error creating driver, skip test (cause: Cannot create driver on grid, it may be fully used [2 times])"));
			// check that testDriverShort3 and testDriverShort4 are executed because now, we find nodes
			Assert.assertEquals(StringUtils.countMatches(logs, "Error creating driver, skip test"), 2);
			Assert.assertEquals(StringUtils.countMatches(logs, "Test has not started or has been skipped"), 2); // 2 tests KO (the first ones)
			Assert.assertEquals(StringUtils.countMatches(logs, "Error in @AfterMethod void com.seleniumtests.it.stubclasses.StubTestClassForDriverTest.reset(Method): No grid connector active"), 2); // 2 tests KO (the first ones)
			Assert.assertEquals(StringUtils.countMatches(logs, "Test is OK"), 2); // 2 tests OK because we got nodes
			Assert.assertFalse(logs.contains("Skipping as the 3 previous tests could not get any matching node"));
			
			Assert.assertEquals(SeleniumGridDriverFactory.getCounter(), 0);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

}
