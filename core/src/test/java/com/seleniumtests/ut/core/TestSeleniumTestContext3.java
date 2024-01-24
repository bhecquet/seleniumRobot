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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
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
@PrepareForTest({ SeleniumRobotVariableServerConnector.class, SeleniumRobotServerContext.class, SeleniumGridConnectorFactory.class,
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
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();

			// do a second parameter retrieving
			seleniumTestsCtx.setTestConfiguration();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key").getValue(), "val1");

			verify(variableServer).getVariables(0, -1);

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
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
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();

			// do a parameter retrieving for copied context
			SeleniumTestsContext seleniumTestsCtx2 = new SeleniumTestsContext(seleniumTestsCtx, false);
			seleniumTestsCtx2.configureContext(testResult);

			// check that variables are kept even if variable server has not been re-called
			Assert.assertEquals(seleniumTestsCtx2.getConfiguration().get("key").getValue(), "val1");

			// only one call, done by first context init, further retrieving is forbidden
			verify(variableServer).getVariables(0, -1);

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
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
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);

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
			verify(variableServer, times(2)).getVariables(0, -1);

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
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
				+ "com.seleniumtests.ut.core.TestSeleniumTestContext3" + "-" + "-test__with_@_chars-"
				+ "-" + Arrays.hashCode(params)
				+ "-" + "org.testng.internal.TestNGMethod";

		Optional<String> keys = SeleniumTestsContext.getOutputFolderNames().keySet().stream().filter(o -> o.contains("test__with_@_chars")).findFirst();
		Assert.assertTrue(keys.isPresent() && keys.get().contains(key));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir(),
				"-test__with_@_chars-");

	}

	// used for generating TestResult
	public void myTest() {
	}
}
