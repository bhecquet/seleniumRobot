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
package com.seleniumtests.ut.core;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.WebUIDriver;

/**
 * - Test creation of seleniumrobot server connection inside
 * SeleniumTestsContext - test handling of output directory (issue #112)
 * 
 * @author behe
 *
 */
@PrepareForTest({ SeleniumRobotVariableServerConnector.class, SeleniumGridConnectorFactory.class,
		SeleniumTestsContext.class, TestManager.class })
public class TestSeleniumTestContext3 extends ConnectorsTest {

	@Mock
	private SeleniumRobotVariableServerConnector variableServer;

	@Mock
	private TestManager testManager;

//	@Mock
//	private ITestResult testResult;
//	@Mock
//	private ITestResult testResult2;
//	
//	@Mock
//	private ITestNGMethod testMethod;

	/**
	 * Check we create a variable server if all connection params are present
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = "ut")
	public void testVariableServerConnection(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			// check upsert has been called
			verify(variableServer).isAlive();

			Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check we create a no variable server if all it's not active
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = "ut")
	public void testNoVariableServerIfNotRequested(final ITestContext testNGCtx, final XmlTest xmlTest)
			throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check we create a no variable server if no URL
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = "ut", expectedExceptions = ConfigurationException.class)
	public void testNoVariableServerIfNoURL(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}

	/**
	 * Check we create a no variable server if not active
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = "ut", expectedExceptions = ConfigurationException.class)
	public void testNoVariableServerIfNotAlive(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(false);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			// check upsert has been called
			verify(variableServer).isAlive();
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Test that a grid connection is created when all parameters are correct
	 * 
	 * @param testNGCtx
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	@Test(groups = "ut")
	public void testGridConnection(final ITestContext testNGCtx) throws NoSuchMethodException, SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		SeleniumGridConnector gridConnector = spy(new SeleniumGridConnector("http://localhost:4444/hub/wd"));

		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd")))
				.thenReturn(Arrays.asList(gridConnector));

		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
					gridConnector);

		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	/**
	 * Test when grid mode is set, but no grid URL is configured. A configuration
	 * exception should be raised
	 * 
	 * @param testNGCtx
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test(groups = "ut", expectedExceptions = ConfigurationException.class)
	public void testGridConnectionEmpty(final ITestContext testNGCtx) throws NoSuchMethodException, SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();

		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}

	/**
	 * Local test, no grid connector
	 * 
	 * @param testNGCtx
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	@Test(groups = "ut")
	public void testNoGridConnection(final ITestContext testNGCtx) throws NoSuchMethodException, SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		SeleniumGridConnector gridConnector = spy(new SeleniumGridConnector("http://localhost:4444/hub/wd"));

		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd")))
				.thenReturn(Arrays.asList(gridConnector));

		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());

		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	/**
	 * grid test but missing url. An error should be raised
	 * 
	 * @param testNGCtx
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	@Test(groups = "ut", expectedExceptions = ConfigurationException.class)
	public void testGridConnectionWithoutUrl(final ITestContext testNGCtx) throws NoSuchMethodException,
			SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		SeleniumGridConnector gridConnector = spy(new SeleniumGridConnector("http://localhost:4444/hub/wd"));

		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd")))
				.thenReturn(Arrays.asList(gridConnector));

		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	/**
	 * If parameter is defined in variable server and as JVM parameter (user
	 * defined), the user defined parameter must be used
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testUserDefinedParamOverridesVariableServer(final ITestContext testNGCtx, final XmlTest xmlTest)
			throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0)).thenReturn(variables);
			System.setProperty("key1", "userValue");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "userValue");

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty("key1");
		}
	}

	/**
	 * Check variables can be get from variable server
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testVariablesFromVariableServer(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0)).thenReturn(variables);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			seleniumTestsCtx.configureContext(testResult);
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "val1");

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check that if we request variable configuration several times, seleniumRobot
	 * server is called only once to avoid problems with variable reservation
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testVariablesAreGetOnlyOnce(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key", new TestVariable("key", "val1"));

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0)).thenReturn(variables);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();

			// do a second parameter retrieving
			seleniumTestsCtx.setTestConfiguration();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key").getValue(), "val1");

			verify(variableServer).getVariables(0);

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check that when copying context, it's possible to prevent it to retrieve
	 * variables from server
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testVariablesAreGetOnlyOnceWithContextCopy(final ITestContext testNGCtx, final XmlTest xmlTest)
			throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key", new TestVariable("key", "val1"));

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0)).thenReturn(variables);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();

			// do a parameter retrieving for copied context
			SeleniumTestsContext seleniumTestsCtx2 = new SeleniumTestsContext(seleniumTestsCtx, false);
			seleniumTestsCtx2.configureContext(testResult);

			// check that variables are kept even if variable server has not been re-called
			Assert.assertEquals(seleniumTestsCtx2.getConfiguration().get("key").getValue(), "val1");

			// only one call, done by first context init, further retrieving is forbidden
			verify(variableServer).getVariables(0);

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * issue #291: Check that when copying context, grid connector is also copied so
	 * that it can be possible to re-use a driver created in \@BeforeMethod
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testGridConnectorIsCopiedWithContextCopy(final ITestContext testNGCtx, final XmlTest xmlTest)
			throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key", new TestVariable("key", "val1"));

		try {
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4321/wd/hub");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");

			createGridHubMockWithNodeOK();

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			seleniumTestsCtx.configureContext(testResult);
			WebUIDriver.getWebDriver(true);

			// do a parameter retrieving for copied context
			SeleniumTestsContext seleniumTestsCtx2 = new SeleniumTestsContext(seleniumTestsCtx, false);
			Assert.assertEquals(seleniumTestsCtx2.getSeleniumGridConnector(),
					seleniumTestsCtx.getSeleniumGridConnector());

		} finally {
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
	}

	/**
	 * Check that when copying context, it's possible to allow it to retrieve
	 * variables from server
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testVariablesAreGetMultipleTimesWithContextCopy(final ITestContext testNGCtx, final XmlTest xmlTest)
			throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key", new TestVariable("key", "val1"));

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0)).thenReturn(variables);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();

			// do a parameter retrieving for copied context. Context is copied with
			// permitting calls to variable server
			SeleniumTestsContext seleniumTestsCtx2 = new SeleniumTestsContext(seleniumTestsCtx);
			seleniumTestsCtx2.configureContext(testResult);

			// check that variables are kept even if variable server has not been re-called
			Assert.assertEquals(seleniumTestsCtx2.getConfiguration().get("key").getValue(), "val1");

			// 2 calls, one for each context because we allow variable retrieving when
			// copying
			verify(variableServer, times(2)).getVariables(0);

		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check that with a test name containing special characters, we create an
	 * output folder for this test whose name is the name of the test
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	@Test(groups = "ut")
	public void testNewOutputFolderWithOddTestName(final ITestContext testNGCtx) throws NoSuchMethodException,
			SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		initThreadContext(testNGCtx);

		ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());

		CucumberScenarioWrapper scenarioWrapper = mock(CucumberScenarioWrapper.class);
		when(scenarioWrapper.toString()).thenReturn("<test | with @ chars>");
		CucumberScenarioWrapper[] params = new CucumberScenarioWrapper[] { scenarioWrapper };
		testResult.setParameters(params);

		SeleniumTestsContextManager.updateThreadContext(testResult);

		String key = testNGCtx.getSuite().getName() + "-" + testNGCtx.getName() + "-"
				+ "com.seleniumtests.ut.core.TestSeleniumTestContext3" + "-" + "-test__with_@_chars-" + "-"
				+ Arrays.hashCode(params);
		Assert.assertTrue(SeleniumTestsContext.getOutputFolderNames().containsKey(key));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir(),
				"-test__with_@_chars-");

	}

	@Test(groups = "ut")
	public void testInitTestManager(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.TMS_TYPE, "squash");
			System.setProperty(SeleniumTestsContext.TMS_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.TMS_USER, "user");
			System.setProperty(SeleniumTestsContext.TMS_PASSWORD, "password");
			System.setProperty(SeleniumTestsContext.TMS_PROJECT, "project");
			System.setProperty("tmsDomain", "domain"); // check that any parameter starting with "tms" will be used for configuration

			PowerMockito.mockStatic(TestManager.class);


			PowerMockito.when(TestManager.getInstance(argThat(config -> config.getString("tmsType").equals("squash") && config.getString("tmsDomain").equals("domain")))).thenReturn(testManager);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			// check test manager has been created
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestManagerInstance(), testManager);

		} finally {
			System.clearProperty(SeleniumTestsContext.TMS_TYPE);
			System.clearProperty(SeleniumTestsContext.TMS_URL);
			System.clearProperty(SeleniumTestsContext.TMS_USER);
			System.clearProperty(SeleniumTestsContext.TMS_PASSWORD);
			System.clearProperty(SeleniumTestsContext.TMS_PROJECT);
			System.clearProperty("tmsDomain");
		}
	}

	// used for generating TestResult
	public void myTest() {
	}
}
