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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.mockito.Mock;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.mashape.unirest.http.JsonNode;
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
	
	@Mock
	private Navigation navigation;
	
	@Mock
	private TargetLocator targetLocator;
	
	@Mock
	private RemoteWebElement element;
	
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
	@Test(groups={"it"})
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
//			createServerMock("GET", SeleniumRobotGridConnector.API_TEST_SESSSION, 200, "{" + 
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
			
			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertEquals(StringUtils.countMatches(logs, "Start creating *chrome driver"), 2);
			
			
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
	@Test(groups={"it"})
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
			when(driver.navigate()).thenReturn(navigation);
			when(driver.switchTo()).thenReturn(targetLocator);
			when(driver.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "75.0", Platform.WINDOWS));
			when(driver.findElement(By.id("text2"))).thenReturn(element);
			when(element.getAttribute(anyString())).thenReturn("attribute");
			when(element.getSize()).thenReturn(new Dimension(10, 10));
			when(element.getLocation()).thenReturn(new Point(5, 5));
			when(element.getTagName()).thenReturn("h1");
			when(element.getText()).thenReturn("text");
			when(element.isDisplayed()).thenReturn(true);
			when(element.isEnabled()).thenReturn(true);
			
			createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
			createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
			createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
			createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, "{\"http://localhost:4321\": {" + 
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

			
			
			createJsonServerMock("GET", SeleniumRobotGridConnector.API_TEST_SESSSION, 200, 
					// session not found
					"{" + 
					"  \"msg\": \"Cannot find test slot running session 7ef50edc-ce51-40dd-98b6-0a369bff38b in the registry.\"," + 
					"  \"success\": false" + 
					"}", 
					// session found
					"{" + 
					"  \"inactivityTime\": 409," + 
					"  \"internalKey\": \"fef800fc-941d-4f76-9590-711da6443e00\"," + 
					"  \"msg\": \"slot found !\"," + 
					"  \"proxyId\": \"http://localhost:4321\"," + 
					"  \"session\": \"7ef50edc-ce51-40dd-98b6-0a369bff38b1\"," + 
					"  \"success\": true" + 
					"}");
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			// check that we tried to create the driver once (we should have retried driver creation on grid inside 'createWebDriver' call)
			verify(uiDriver, times(1)).createWebDriver();

			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertEquals(StringUtils.countMatches(logs, "Start creating *chrome driver"), 1); // driver is really created once, but, from WebUIDriver point of view, there was only one call
			Assert.assertTrue(logs.contains("Cannot find test slot running session 7ef50edc-ce51-40dd-98b6-0a369bff38b")); // check error on first creation is displayed
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

}
