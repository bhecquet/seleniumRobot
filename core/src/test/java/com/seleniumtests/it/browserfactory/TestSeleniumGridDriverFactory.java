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
package com.seleniumtests.it.browserfactory;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.mockito.Mock;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.mashape.unirest.http.Unirest;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.WebUIDriverFactory;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.ut.connectors.ConnectorsTest;

@PrepareForTest({Unirest.class, WebUIDriverFactory.class, SeleniumGridDriverFactory.class})
@PowerMockIgnore({"javax.net.ssl.*", "com.google.inject.*"})
public class TestSeleniumGridDriverFactory extends ConnectorsTest {


	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private Options options;
	
	@Mock
	private Timeouts timeouts;
	
	@BeforeMethod(groups={"it"})
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(Unirest.class);
	}
	
	/**
	 * issue #287: check the real behavior of the bug (for now, this cannot be corrected because of https://github.com/cbeust/testng/issues/2148)
	 * When the AfterMethod fails, test is not retried and this is a bug in testNG
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=false)
	public void testSessionNotGet(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");

			WebUIDriver uiDriver = spy(new WebUIDriver("main"));
			
			PowerMockito.whenNew(WebUIDriver.class).withArguments(any()).thenReturn(uiDriver);
			PowerMockito.whenNew(RemoteWebDriver.class).withAnyArguments().thenReturn(driver);
			when(driver.manage()).thenReturn(options);
			when(options.timeouts()).thenReturn(timeouts);
			when(driver.getSessionId()).thenReturn(new SessionId("abcdef"));
			
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
			createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, "{\"http://localhost:5555\": {" + 
					"    \"busy\": false," + 
					"    \"lastSessionStart\": \"never\"," + 
					"    \"version\": \"4.6.0\"," + 
					"    \"usedTestSlots\": 0,\n" + 
					"    \"testSlots\": 1," + 
					"    \"status\": \"ACTIVE\"" + 
					"  }," + 
					"  \"hub\": {" + 
					"    \"version\": \"4.6.1\"," + 
					"    \"status\": \"ACTIVE\"" + 
					"  }," + 
					"  \"success\": true" + 
					"}");
			
//			// session is found (kept here for information)
//			createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, "{" + 
//					"  \"inactivityTime\": 409," + 
//					"  \"internalKey\": \"fef800fc-941d-4f76-9590-711da6443e00\"," + 
//					"  \"msg\": \"slot found !\"," + 
//					"  \"proxyId\": \"http://localhost:5555\"," + 
//					"  \"session\": \"7ef50edc-ce51-40dd-98b6-0a369bff38b1\"," + 
//					"  \"success\": true" + 
//					"}");
			
			// session not found
			createServerMock("GET", SeleniumRobotGridConnector.API_TEST_SESSSION, 200, "{" + 
					"  \"msg\": \"Cannot find test slot running session 7ef50edc-ce51-40dd-98b6-0a369bff38b in the registry.\"," + 
					"  \"success\": false" + 
					"}");
		
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});

			// check that we tried to create the driver twice (the bug is based on the fact that we create it only once)
			verify(uiDriver, times(2)).createWebDriver();
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
		
	}
	
	/**
	 * issue #287: check driver is recreated if session cannot be get
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=false)
	public void testSessionNotGetOnFirstTime(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, SERVER_URL + "/wd/hub");
			
			WebUIDriver uiDriver = spy(new WebUIDriver("main"));
			
			PowerMockito.whenNew(WebUIDriver.class).withArguments(any()).thenReturn(uiDriver);
			PowerMockito.whenNew(RemoteWebDriver.class).withAnyArguments().thenReturn(driver);
			when(driver.manage()).thenReturn(options);
			when(options.timeouts()).thenReturn(timeouts);
			when(driver.getSessionId()).thenReturn(new SessionId("abcdef"));
			
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
			createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, "{\"http://localhost:5555\": {" + 
					"    \"busy\": false," + 
					"    \"lastSessionStart\": \"never\"," + 
					"    \"version\": \"4.6.0\"," + 
					"    \"usedTestSlots\": 0,\n" + 
					"    \"testSlots\": 1," + 
					"    \"status\": \"ACTIVE\"" + 
					"  }," + 
					"  \"hub\": {" + 
					"    \"version\": \"4.6.1\"," + 
					"    \"status\": \"ACTIVE\"" + 
					"  }," + 
					"  \"success\": true" + 
					"}");
			
//		// session is found (kept here for information)
//		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, "{" + 
//				"  \"inactivityTime\": 409," + 
//				"  \"internalKey\": \"fef800fc-941d-4f76-9590-711da6443e00\"," + 
//				"  \"msg\": \"slot found !\"," + 
//				"  \"proxyId\": \"http://localhost:5555\"," + 
//				"  \"session\": \"7ef50edc-ce51-40dd-98b6-0a369bff38b1\"," + 
//				"  \"success\": true" + 
//				"}");
			
			// session not found
			createServerMock("GET", SeleniumRobotGridConnector.API_TEST_SESSSION, 200, "{" + 
					"  \"msg\": \"Cannot find test slot running session 7ef50edc-ce51-40dd-98b6-0a369bff38b in the registry.\"," + 
					"  \"success\": false" + 
					"}");
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			// check that we tried to create the driver twice (the bug is based on the fact that we create it only once)
			verify(uiDriver, times(2)).createWebDriver();
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

}
