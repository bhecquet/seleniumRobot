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
package com.seleniumtests.ut.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;

public class TestTestTasks extends MockitoTest {
	
	@Mock
	private OSUtilityWindows osUtility;

	private MockedStatic<OSUtilityFactory> mockedOsUtilityFactory;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {

		mockedOsUtilityFactory = mockStatic(OSUtilityFactory.class);
		mockedOsUtilityFactory.when(OSUtilityFactory::getInstance).thenReturn(osUtility);

		when(osUtility.getProgramExtension()).thenReturn(".exe");
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedOsUtilityFactory.close();
	}
	
	/*
	 * Issue #374: Now, it's allowed to updateParam without server, check param is then available
	 */
	@Test(groups= {"ut"})
	public void testUpdateVariableWithoutServer(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			initThreadContext(testNGCtx);
			TestTasks.createOrUpdateParam("key", "value");
			Assert.assertEquals(TestTasks.param("key"), "value");
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}
	
	/**
	 * Check upsert of a new variable locally
	 * Verify server is not called and configuration is updated according to the new value
	 */
	@Test(groups= {"ut"})
	public void testUpdateNewVariableLocally(final ITestContext testNGCtx) throws Exception {
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateLocalParam("key", "value");
			
			// check upsert has been NOT called
			verify(mockedVariableServer.constructed().get(0), never()).upsertVariable(new TestVariable("key", "value"), true);
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key").getValue(), "value");
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check upsert of a new variable
	 * Verify server is called and configuration is updated according to the new value
	 */
	@Test(groups= {"ut"})
	public void testUpdateNewVariable(final ITestContext testNGCtx) throws Exception {

		TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value");
			
			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0)).upsertVariable(new TestVariable("key", "value"), true);
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check that if variable exists but we request a reservable variable, it's recreated so that we can have multiple variables
	 */
	@Test(groups= {"ut"})
	public void testUpdateExistingVariable(final ITestContext testNGCtx) throws Exception {
		TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key", 2, null));
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 3, true);
			
			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0)).upsertVariable(new TestVariable(null, "key", "value", true, "key", 3, null), false);
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check that if variable exists, it's updated, if we do not request it to be reservable
	 */
	@Test(groups= {"ut"})
	public void testRecreateExistingVariableIfReservable(final ITestContext testNGCtx) throws Exception {
		TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key", 2, null));
			
			TestTasks.createOrUpdateParam("key", "value", false, 3, false);
			
			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0)).upsertVariable(new TestVariable(10, "key", "value", false, "key", 3, null), false);
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check upsert of a new variable which will not be version specific
	 * Verify server is called and configuration is updated according to the new value
	 */
	@Test(groups= {"ut"})
	public void testUpdateNewVariableNotSpecificToVersion(final ITestContext testNGCtx) throws Exception {
		TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false);
			
			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0)).upsertVariable(new TestVariable("key", "value"), false);
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check error is raised when TTL is not specified ( <= 0) for a reservable variable
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testUpdateReservableVariableWithoutTTL(final ITestContext testNGCtx) throws Exception {
		TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 0, true);
		
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check that if TTL and reservable is set, all information are passer to server
	 */
	@Test(groups= {"ut"})
	public void testUpdateReservableVariableWithTTL(final ITestContext testNGCtx) throws Exception {
		TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 1, true);
			
			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0)).upsertVariable(new TestVariable(null, "key", "value", true, "key", 1, null), false);
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	@Test(groups= {"ut"})
	public void testDeleteVariableWithoutServer(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			initThreadContext(testNGCtx);
			TestTasks.deleteParam("key");
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}

	@Test(groups= {"ut"})
	public void testDeleteVariable(final ITestContext testNGCtx) throws Exception {
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(1, "key", "value", true, "key", 1, null));
			TestTasks.deleteParam("key");

			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0)).deleteVariable(new TestVariable(1, "key", "value", true, "key", 1, null));
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * If variable has no id, do not try to delete it from server
	 */
	@Test(groups= {"ut"})
	public void testDeleteVariableNoId(final ITestContext testNGCtx) throws Exception {
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(null, "key", "value", true, "key", 1, null));
			TestTasks.deleteParam("key");

			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0), never()).deleteVariable(new TestVariable(1, "key", "value", true, "key", 1, null));
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	@Test(groups= {"ut"})
	public void testDeleteVariableNotPresent(final ITestContext testNGCtx) throws Exception {
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.deleteParam("key");

			// check upsert has been called
			verify(mockedVariableServer.constructed().get(0), never()).deleteVariable(new TestVariable(1, "key", "value", true, "key", 1, null));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	@Test(groups= {"ut"})
	public void testKillProcessLocal(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			initThreadContext(testNGCtx);
			TestTasks.killProcess("some_process");
			
			verify(osUtility).killProcessByName("some_process", true);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	@Test(groups= {"ut"})
	public void testKillProcessGrid(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doNothing().when(gridConnector).killProcess("some_process");
		
		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			TestTasks.killProcess("some_process");
			
			verify(gridConnector).killProcess("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Test when the grid connector is not initialized
	 * When grid connector does not return any sessionId, it's not active
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testKillProcessGridNotUsed(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doNothing().when(gridConnector).killProcess("some_process");

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			TestTasks.killProcess("some_process");
			
			verify(gridConnector).killProcess("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Test kill process is not called when not using local or grid mode
	 */
	@Test(groups= {"ut"})
	public void testKillProcessOtherRunMode(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://saucelabs:4444/hub/wd"));

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://saucelabs:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "saucelabs");
			System.setProperty(SeleniumTestsContext.PLATFORM, "windows");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://saucelabs:4444/hub/wd");
			initThreadContext(testNGCtx);
			TestTasks.killProcess("some_process");
			
			verify(gridConnector, never()).killProcess("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.PLATFORM);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	

	@Test(groups= {"ut"})
	public void testGetProcessListLocal(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			initThreadContext(testNGCtx);
			TestTasks.getProcessList("some_process");
			
			verify(osUtility).getRunningProcesses("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	@Test(groups= {"ut"})
	public void testGetProcessListGrid(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doReturn(List.of(10, 20)).when(gridConnector).getProcessList("some_process");
		
		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			TestTasks.getProcessList("some_process");
			
			verify(gridConnector).getProcessList("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Test when the grid connector is not initialized
	 * When grid connector does not return any sessionId, it's not active
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListGridNotUsed(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doReturn(List.of(10, 20)).when(gridConnector).getProcessList("some_process");

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			TestTasks.getProcessList("some_process");
			
			verify(gridConnector).getProcessList("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Test kill process is not called when not using local or grid mode
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListOtherRunMode(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://saucelabs:4444/hub/wd"));

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://saucelabs:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "saucelabs");
			System.setProperty(SeleniumTestsContext.PLATFORM, "windows");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://saucelabs:4444/hub/wd");
			initThreadContext(testNGCtx);
			TestTasks.getProcessList("some_process");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.PLATFORM);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	
	@Test(groups= {"ut"})
	public void testExecuteCommandLocal(final ITestContext testNGCtx) {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(ArgumentMatchers.any(String[].class), eq(-1), isNull())).thenReturn("hello guys");
			initThreadContext(testNGCtx);
			String response = TestTasks.executeCommand("echo", "hello");
			Assert.assertEquals(response, "hello guys");
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	@Test(groups= {"ut"})
	public void testExecuteCommandLocalWithTimeout(final ITestContext testNGCtx) {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(ArgumentMatchers.any(String[].class), eq(35), isNull())).thenReturn("hello guys");
			initThreadContext(testNGCtx);
			String response = TestTasks.executeCommand("echo", 35, null, "hello");
			Assert.assertEquals(response, "hello guys");
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	@Test(groups= {"ut"})
	public void testExecuteCommandGrid(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		gridConnector.setNodeUrl("http://localhost:5555/hub/wd");
		doReturn("hello guys").when(gridConnector).executeCommand("echo", -1, "hello");
		
		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			String response = TestTasks.executeCommand("echo", "hello");
			Assert.assertEquals(response, "hello guys");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	@Test(groups= {"ut"})
	public void testExecuteCommandGridWithTimeout(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		gridConnector.setNodeUrl("http://localhost:5555/hub/wd");
		doReturn("hello guys").when(gridConnector).executeCommand("echo", 25, "hello");
		
		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			String response = TestTasks.executeCommand("echo", 25, null, "hello");
			Assert.assertEquals(response, "hello guys");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Test when the grid connector is not initialized
	 * When grid connector does not return any sessionId, it's not active
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testExecuteCommandNotUsed(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doReturn("hello guys").when(gridConnector).executeCommand("echo", "hello");

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
			
			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			TestTasks.executeCommand("echo", "hello");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Test kill process is not called when not using local or grid mode
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testExecuteCommandOtherRunMode(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://saucelabs:4444/hub/wd"));

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://saucelabs:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "saucelabs");
			System.setProperty(SeleniumTestsContext.PLATFORM, "windows");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://saucelabs:4444/hub/wd");
			initThreadContext(testNGCtx);
			TestTasks.executeCommand("echo", "hello");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.PLATFORM);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * It should not be possible to create a manual step when automatic steps are enabled
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAddManualStepInAutomaticMode() {
		TestTasks.addStep("foo");

	}
	
	/**
	 * Creation of a manual step, check it's written
	 */
	@Test(groups= {"ut"})
	public void testAddManualStepInManualMode() {
		try {
			SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
			TestTasks.addStep("foo");
			TestTasks.addStep(null); // add a final step so that previous step is written
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().size(), 1);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(0).getName(), "foo");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(0).getAction(), "foo");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(0).getOrigin(), TestTestTasks.class); // check origin is the class that made the call
		} finally {
			GenericTest.resetTestNGResultAndLogger();
		}
	}
	
	/**
	 * Check we get parameters from the thread context
	 */
	@Test(groups= {"ut"})
	public void testParam() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foo", new TestVariable("foo", "bar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foo", new TestVariable("foo", "bar2"));
		Assert.assertEquals(TestTasks.param("foo"), "bar");
	}
	
	/**
	 * Check empty string is returned if parameter is not known
	 */
	@Test(groups= {"ut"})
	public void testParamDoesNotExist() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foo", new TestVariable("foo", "bar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foo", new TestVariable("foo", "bar2"));
		Assert.assertEquals(TestTasks.param("foo2"), "");
	}
	
	/**
	 * Check we look at global context if thread context is not initialized
	 */
	@Test(groups= {"ut"})
	public void testParamThreadContextNotInitialized() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foo", new TestVariable("foo", "bar2"));
		Assert.assertEquals(TestTasks.param("foo"), "bar2");
	}
	
	/**
	 * Check error is raised if thread and global context are not initialized
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testParamGlobalContextNotInitialized() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.setGlobalContext(null);
		TestTasks.param("foo");
	}
	
	/**
	 * Check we get parameters with pattern on key from the thread context
	 */
	@Test(groups= {"ut"})
	public void testParamKeyPattern() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "bar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "bar_global"));
		Assert.assertEquals(TestTasks.param(Pattern.compile("ofo")), "bar");
	}
	
	/**
	 * Check empty string is returned if parameter is not known
	 */
	@Test(groups= {"ut"})
	public void testParamKeyPatternDoesNotExist() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "bar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "bar_global"));
		Assert.assertEquals(TestTasks.param(Pattern.compile("offo")), "");
	}
	
	/**
	 * Check we look at global context if thread context is not initialized
	 */
	@Test(groups= {"ut"})
	public void testParamKeyPatternThreadContextNotInitialized() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "bar2"));
		Assert.assertEquals(TestTasks.param(Pattern.compile("ofo")), "bar2");
	}
	
	/**
	 * Check error is raised if thread and global context are not initialized
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testParamKeyPatternGlobalContextNotInitialized() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.setGlobalContext(null);
		TestTasks.param(Pattern.compile("ofo"));
	}
	
	/**
	 * Check we get parameters with pattern on value from the thread context
	 */
	@Test(groups= {"ut"})
	public void testParamValuePattern() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "barbar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foobar", new TestVariable("foobar", "barbar2"));
		Assert.assertEquals(TestTasks.param(null, Pattern.compile("rba")), "barbar");
	}
	
	/**
	 * Check empty string is returned if parameter is not known
	 */
	@Test(groups= {"ut"})
	public void testParamValuePatternDoesNotExist() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "barbar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foobar", new TestVariable("foobar", "barbar2"));
		Assert.assertEquals(TestTasks.param(null, Pattern.compile("rbba")), "");
	}
	
	/**
	 * Check we look at global context if thread context is not initialized
	 */
	@Test(groups= {"ut"})
	public void testParamValuePatternThreadContextNotInitialized() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foobar", new TestVariable("foobar", "barbar2"));
		Assert.assertEquals(TestTasks.param(null, Pattern.compile("rba")), "barbar2");
	}
	
	/**
	 * Check error is raised if thread and global context are not initialized
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testParamValuePatternGlobalContextNotInitialized() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.setGlobalContext(null);
		TestTasks.param(null, Pattern.compile("rba"));
	}

	/**
	 * Check we get parameters with pattern on value and key from the thread context
	 */
	@Test(groups= {"ut"})
	public void testParamKeyAndValuePattern() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "barbar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foobar", new TestVariable("foobar", "barbar2"));
		Assert.assertEquals(TestTasks.param(Pattern.compile("foo"), Pattern.compile("rba")), "barbar");
	}
	
	/**
	 * Check no variable matches because of non matching key
	 */
	@Test(groups= {"ut"})
	public void testParamKeyAndValuePatternNoKeyMatching() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "barbar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foobar", new TestVariable("foobar", "barbar2"));
		Assert.assertEquals(TestTasks.param(Pattern.compile("ffo"), Pattern.compile("rba")), "");
	}
	
	/**
	 * Check no variable matches because of non matching key
	 */
	@Test(groups= {"ut"})
	public void testParamKeyAndValuePatternNoValueMatching() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foofoo", new TestVariable("foofoo", "barbar"));
		SeleniumTestsContextManager.getGlobalContext().getConfiguration().put("foobar", new TestVariable("foobar", "barbar2"));
		Assert.assertEquals(TestTasks.param(Pattern.compile("foo"), Pattern.compile("rbba")), "");
	}

	@Test(groups= {"ut"})
	public void testDownloadFileLocal(final ITestContext testNGCtx) {
		try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			mockedFiles.when(() -> Files.list(any(Path.class))).thenReturn(Stream.of(new File("file1.pdf").toPath(), new File("file2.pdf").toPath()));
			initThreadContext(testNGCtx);
			File file = TestTasks.getDownloadedFile("file1.pdf");
			Assert.assertEquals(file.getName(), "file1.pdf");
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	@Test(groups= {"ut"})
	public void testDownloadFileLocalNoFile(final ITestContext testNGCtx) {
		try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			mockedFiles.when(() -> Files.list(any(Path.class)))
					.thenAnswer((Answer<Stream<Path>>) invocation -> new ArrayList<Path>().stream()
            );
			initThreadContext(testNGCtx);
			Instant start = Instant.now();
			File file = TestTasks.getDownloadedFile("file1.pdf");
			Assert.assertNull(file);
			Assert.assertTrue(Instant.now().minusSeconds(9).isAfter(start));
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}

	/**
	 * Exception thrown when accessing file system
	 */
	@Test(groups= {"ut"})
	public void testDownloadFileLocalNoFile2(final ITestContext testNGCtx) {
		try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			mockedFiles.when(() -> Files.list(any(Path.class))).thenThrow(new IOException());
			initThreadContext(testNGCtx);
			Instant start = Instant.now();
			File file = TestTasks.getDownloadedFile("file1.pdf");
			Assert.assertNull(file);
			Assert.assertTrue(Instant.now().minusSeconds(9).isAfter(start));
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}

	/**
	 * If file in not present immediately, wait for it
	 */
	@Test(groups= {"ut"})
	public void testDownloadFileLocalWithTimeout(final ITestContext testNGCtx) {
		try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			mockedFiles.when(() -> Files.list(any(Path.class)))
					.thenAnswer((Answer<Stream<Path>>) invocation -> new ArrayList<Path>().stream())
					.thenAnswer((Answer<Stream<Path>>) invocation -> Stream.of(new File("file1.pdf").toPath()));
			initThreadContext(testNGCtx);
			File file = TestTasks.getDownloadedFile("file1.pdf");
			Assert.assertEquals(file.getName(), "file1.pdf");
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}

	@Test(groups= {"ut"})
	public void testDownloadFileGrid(final ITestContext testNGCtx) {
		File file = downloadFileWithGrid(testNGCtx, List.of("file1.pdf", "file2.pdf"), "file1.pdf");
		Assert.assertEquals(file.getName(), "file1.pdf");
	}
	@Test(groups= {"ut"})
	public void testDownloadFileGridNoFile(final ITestContext testNGCtx) {
		File file = downloadFileWithGrid(testNGCtx, new ArrayList<>(), "file1.pdf");
		Assert.assertNull(file);
	}
	@Test(groups= {"ut"})
	public void testDownloadFileGridPartialName(final ITestContext testNGCtx) {
		File file = downloadFileWithGrid(testNGCtx, List.of("file1.pdf", "file2.pdf"), "file1");
		Assert.assertEquals(file.getName(), "file1.pdf");
	}
	@Test(groups= {"ut"} ,expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "No grid connector active")
	public void testDownloadFileGridNoGrid(final ITestContext testNGCtx) {

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(new ArrayList<>());

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);

			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			TestTasks.getDownloadedFile("file1.pdf");
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}

	}

	/**
	 * In case download takes time, wait until 10 secs
	 */
	@Test(groups= {"ut"})
	public void testDownloadBigFile(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		gridConnector.setNodeUrl("http://localhost:5555/hub/wd");
		doReturn(new ArrayList<>(), new ArrayList<>(), List.of("file1.pdf")).when(gridConnector).listFilesToDownload();
		when(gridConnector.downloadFileFromName(eq("file1.pdf"), any(File.class))).thenReturn(new File("file1.pdf"));

		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);

			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			File file =  TestTasks.getDownloadedFile("file1.pdf");
			Assert.assertEquals(file.getName(), "file1.pdf");
			verify(gridConnector, times(3)).listFilesToDownload();

		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	private File downloadFileWithGrid(ITestContext testNGCtx, List<String> filesPresentOnGrid, String fileToDownload) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		gridConnector.setNodeUrl("http://localhost:5555/hub/wd");
		doReturn(filesPresentOnGrid).when(gridConnector).listFilesToDownload();
		when(gridConnector.downloadFileFromName(eq("file1.pdf"), any(File.class))).thenReturn(new File("file1.pdf"));

		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();

		try (MockedStatic<SeleniumGridConnectorFactory> mockedGridConnectorFactory = mockStatic(SeleniumGridConnectorFactory.class)) {
			mockedGridConnectorFactory.when(() -> SeleniumGridConnectorFactory.getInstances(List.of("http://localhost:4444/hub/wd"))).thenReturn(List.of(gridConnector));

			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);

			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors();
			return TestTasks.getDownloadedFile(fileToDownload);

		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	@Test(groups= {"ut"})
	public void testGeolocation() {
		try (MockedStatic<WebUIDriver> mockedWebUIDriver = mockStatic(WebUIDriver.class)) {
			CustomEventFiringWebDriver mockedDriver = mock(CustomEventFiringWebDriver.class);
			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(true)).thenReturn(mockedDriver);
			TestTasks.setGeolocation(10, 12);
			verify(mockedDriver).setGeolocation(10, 12);
		}
	}

}
