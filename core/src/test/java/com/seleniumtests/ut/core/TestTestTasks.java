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
package com.seleniumtests.ut.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

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
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;

@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class, OSUtilityFactory.class, SeleniumGridConnectorFactory.class})
public class TestTestTasks extends MockitoTest {

	@Mock
	private SeleniumRobotVariableServerConnector variableServer;
	
	@Mock
	private OSUtilityWindows osUtility;
	
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
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
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
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key", 2));
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 3, true);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable(null, "key", "value", true, "key", 3)), eq(false));
			
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
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			SeleniumTestsContextManager.getThreadContext().getConfiguration().put("key", new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key", 2));
			
			TestTasks.createOrUpdateParam("key", "value", false, 3, false);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable(10, "key", "value", false, "key", 3)), eq(false));
			
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
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
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
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
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
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			TestVariable varToReturn = new TestVariable(10, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
			when(variableServer.upsertVariable(any(TestVariable.class), anyBoolean())).thenReturn(varToReturn);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			TestTasks.createOrUpdateParam("key", "value", false, 1, true);
			
			// check upsert has been called
			verify(variableServer).upsertVariable(eq(new TestVariable(null, "key", "value", true, "key", 1)), eq(false));
			
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
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			initThreadContext(testNGCtx);
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
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://saucelabs:4444/hub/wd")).thenReturn(gridConnector);
		
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
}
