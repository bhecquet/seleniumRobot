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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.Mock;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.neotys.selenium.proxies.NLWebDriver;
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
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;

import net.lightbody.bmp.BrowserMobProxy;

@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class, OSUtilityFactory.class, SeleniumGridConnectorFactory.class, WebUIDriver.class})
public class TestTestTasks extends MockitoTest {

	@Mock
	private SeleniumRobotVariableServerConnector variableServer;
	
	@Mock
	private OSUtilityWindows osUtility;
	
	@Mock
	private BrowserMobProxy mobProxy;
	
	@Mock
	private NLWebDriver neoloadDriver;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {

		PowerMockito.mockStatic(OSUtilityFactory.class);
		PowerMockito.when(OSUtilityFactory.getInstance()).thenReturn(osUtility);
		
		when(osUtility.getProgramExtension()).thenReturn(".exe");
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testUpdateVariableWithoutServer(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			initThreadContext(testNGCtx);
			TestTasks.createOrUpdateParam("key", "value");
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}
	
	/**
	 * Check upsert of a new variable
	 * Verify server is called and configuration is updated according to the new value
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testUpdateNewVariable(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value");
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable("key", "value")), eq(true));
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check that if variable exists but we request a reservable variable, it's recreated so that we can have multiple variables
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testUpdateExistingVariable(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key", 2, null));
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 3, true);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable(null, "key", "value", true, "key", 3, null)), eq(false));
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check that if variable exists, it's updated, if we do not request it to be reservable
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testRecreateExistingVariableIfReservable(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key", 2, null));
			
			TestTasks.createOrUpdateParam("key", "value", false, 3, false);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable(10, "key", "value", false, "key", 3, null)), eq(false));
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check upsert of a new variable which will not be version specific
	 * Verify server is called and configuration is updated according to the new value
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testUpdateNewVariableNotSpecificToVersion(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable("key", "value")), eq(false));
			
			// check configuration is updated
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("key"), varToReturn);
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check error is raised when TTL is not specified ( <= 0) for a reservable variable
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testUpdateReservableVariableWithoutTTL(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 0, true);
		
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check that if TTL and reservable is set, all information are passer to server
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testUpdateReservableVariableWithTTL(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 1, true);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable(null, "key", "value", true, "key", 1, null)), eq(false));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
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
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd"))).thenReturn(Arrays.asList(gridConnector));
		
		try {
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
	 * @param testNGCtx
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testKillProcessGridNotUsed(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doNothing().when(gridConnector).killProcess("some_process");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd"))).thenReturn(Arrays.asList(gridConnector));
		
		try {
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
	 * @param testNGCtx
	 */
	@Test(groups= {"ut"})
	public void testKillProcessOtherRunMode(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://saucelabs:4444/hub/wd"));
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://saucelabs:4444/hub/wd"))).thenReturn(Arrays.asList(gridConnector));
		
		try {
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
		doReturn(Arrays.asList(10, 20)).when(gridConnector).getProcessList("some_process");
		
		// grid connector is in use only if session Id exists
		doReturn(new SessionId("1234")).when(gridConnector).getSessionId();
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd"))).thenReturn(Arrays.asList(gridConnector));
		
		try {
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
	 * @param testNGCtx
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListGridNotUsed(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://localhost:4444/hub/wd"));
		doReturn(Arrays.asList(10, 20)).when(gridConnector).getProcessList("some_process");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/hub/wd"))).thenReturn(Arrays.asList(gridConnector));
		
		try {
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
	 * @param testNGCtx
	 */
	@Test(groups= {"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListOtherRunMode(final ITestContext testNGCtx) {
		SeleniumGridConnector gridConnector = spy(new SeleniumRobotGridConnector("http://saucelabs:4444/hub/wd"));
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://saucelabs:4444/hub/wd"))).thenReturn(Arrays.asList(gridConnector));
		
		try {
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
	
	/**
	 * It should not be possible to create a manual step when automatic steps are enabled
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAddManualStepInAutomaticMode(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		TestTasks.addStep("foo");

	}
	
	/**
	 * Creation of a manual step, check it's written
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testAddManualStepInManualMode(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
			TestTasks.addStep("foo");
			TestTasks.addStep(null); // add a final step so that previous step is written
			Assert.assertEquals(TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).size(), 1);
			Assert.assertEquals(TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).get(0).getName(), "foo");
		} finally {
			TestLogging.reset();
		}
	}
	
	/**
	 * Creation of a manual step, check it's written
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testAddManualStepWithBrowserMobProxy(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getBrowserMobProxy()).thenReturn(mobProxy);
		
		try {
			SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
			TestTasks.addStep("foo");
			TestTasks.addStep(null); 
			
			// check we crate a new page
			verify(mobProxy).newPage("foo");
			
		} finally {
			TestLogging.reset();
		}
	}
	
	/**
	 * Creation of a manual step, check it's written
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups= {"ut"})
	public void testAddManualStepWithNeoload(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getNeoloadDriver()).thenReturn(neoloadDriver);
		
		try {
			SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
			TestTasks.addStep("foo");
			TestTasks.addStep(null); 
			
			// check we crate a new page
			verify(neoloadDriver).startTransaction("foo");
			verify(neoloadDriver).stopTransaction();
			
		} finally {
			TestLogging.reset();
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
}
