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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

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
			
			initThreadContext(testNGCtx, "myTest");
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
			
			initThreadContext(testNGCtx, "myTest");
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
