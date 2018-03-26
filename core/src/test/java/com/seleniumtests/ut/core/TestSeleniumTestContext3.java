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
package com.seleniumtests.ut.core;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;

/**
 * - Test creation of seleniumrobot server connection inside SeleniumTestsContext
 * - test handling of output directory (issue #112)
 * @author behe
 *
 */
@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumGridConnectorFactory.class, SeleniumTestsContext.class})
public class TestSeleniumTestContext3 extends MockitoTest {
	
	@Mock
	private SeleniumRobotVariableServerConnector variableServer;

	/**
	 * Check we create a variable server if all connexion params are present
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut")
	public void testVariableServerConnection(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			
			initThreadContext(testNGCtx, "myTest");
			
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
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut")
	public void testNoVariableServerIfNotRequested(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			initThreadContext(testNGCtx, "myTest");
		
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check we create a no variable server if no URL
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut", expectedExceptions=ConfigurationException.class)
	public void testNoVariableServerIfNoURL(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			
			initThreadContext(testNGCtx, "myTest");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}
	
	/**
	 * Check we create a no variable server if not active
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut", expectedExceptions=ConfigurationException.class)
	public void testNoVariableServerIfNotAlive(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(false);
			
			initThreadContext(testNGCtx, "myTest");
			
			// check upsert has been called
			verify(variableServer).isAlive();			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Test that a grid connection is created when all parameters are correct
	 * @param testNGCtx
	 */
	@Test(groups="ut")
	public void testGridConnection(final ITestContext testNGCtx) {
		
		SeleniumGridConnector gridConnector = new SeleniumGridConnector("http://localhost:4444/hub/wd");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			
			initThreadContext(testNGCtx, "myTest");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(), gridConnector);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Local test, no grid connector
	 * @param testNGCtx
	 */
	@Test(groups="ut")
	public void testNoGridConnection(final ITestContext testNGCtx) {
		
		SeleniumGridConnector gridConnector = new SeleniumGridConnector("http://localhost:4444/hub/wd");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			
			initThreadContext(testNGCtx, "myTest");
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * grid test but missing url
	 * @param testNGCtx
	 */
	@Test(groups="ut", expectedExceptions=ConfigurationException.class)
	public void testGridConnectionWithoutUrl(final ITestContext testNGCtx) {
		
		SeleniumGridConnector gridConnector = new SeleniumGridConnector("http://localhost:4444/hub/wd");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			
			initThreadContext(testNGCtx, "myTest");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	
	/**
	 * If parameter is defined in variable server and as JVM parameter (user defined), the user defined parameter must be used
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testUserDefinedParamOverridesVariableServer(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		
		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables()).thenReturn(variables);
			System.setProperty("key1", "userValue");
			
			initThreadContext(testNGCtx, "myTest");
			
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "userValue");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty("key1");
		}
	}	
}
