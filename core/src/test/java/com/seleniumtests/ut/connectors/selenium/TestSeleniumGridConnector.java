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
package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.mockito.Mock;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;


//@PrepareForTest({HttpClients.class, Unirest.class})
public class TestSeleniumGridConnector extends ConnectorsTest {

	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private GetRequest getRequest;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private StatusLine statusLine;
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private RemoteWebDriver driver2;
	
	private Capabilities capabilities = new DesiredCapabilities();
	
	@BeforeMethod(groups={"ut"})
	private void init() throws ClientProtocolException, IOException {
//		PowerMockito.mockStatic(HttpClients.class);
		
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)any(HttpHost.class), any(HttpRequest.class))).thenReturn(response);
		when(driver.getCapabilities()).thenReturn(capabilities); 
		when(driver.getSessionId()).thenReturn(new SessionId("abcdef"));
		when(driver2.getCapabilities()).thenReturn(capabilities); 
		when(driver2.getSessionId()).thenReturn(new SessionId("ghijkl"));
	}
	
	@Test(groups={"ut"})
	public void testGetSessionInformationFromGrid() throws UnsupportedOperationException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {
		

		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "abcdef"));
		
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_VERSION, "50.0");
		
		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);
		
		connector.getSessionInformationFromGrid(driver);
		
		verify(logger).info("Brower firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");
		
		// check sessionId is set when test is started
		verify(connector).setSessionId(any(SessionId.class));
		Assert.assertEquals(connector.getNodeUrl(), "http://localhost:4321");
	}
	
	/**
	 * 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testGetSessionInformationFromGridWithSecondDriver() throws UnsupportedOperationException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {
		
		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "abcdef"), String.format(GRID_STATUS_WITH_SESSION, "ghijkl"));
		
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_VERSION, "50.0");
		
		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);

		// 2 drivers created inside the same test
		connector.getSessionInformationFromGrid(driver);
		connector.getSessionInformationFromGrid(driver2);
		
		// issue #242: check sessionId is set only once when first driver is created
		verify(connector).setSessionId(any(SessionId.class));
		Assert.assertEquals(connector.getSessionId().toString(), "abcdef");
		Assert.assertEquals(connector.getNodeUrl(), "http://localhost:4321");
		
		// check that the driver session id is displayed in logs to help debugging
		verify(logger).info("Brower firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");
		verify(logger).info("Brower firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session ghijkl");
	}
	
	/**
	 * With simple selenium grid, upload is not available
	 * @throws ClientProtocolException 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDoNothing() throws ClientProtocolException, IOException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector("http://localhost:6666");
		connector.uploadMobileApp(new DesiredCapabilities());
		
		verify(client, never()).execute((HttpHost)any(HttpHost.class), any(HttpRequest.class));
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridNotPresent() throws ClientProtocolException, IOException, UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.STATUS_SERVLET)).thenReturn(getRequest);
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridPresent() throws ClientProtocolException, IOException, UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.STATUS_SERVLET, 200, "{\"value\": {\"ready\": true}}");	
		
		Assert.assertTrue(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridInError() throws ClientProtocolException, IOException, UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.STATUS_SERVLET, 500, "some text");	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testStopSession() throws ClientProtocolException, IOException, UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		connector.setNodeUrl("http://localhost:5555");
		createServerMock("http://localhost:5555", "DELETE", "/se/grid/node/session/1234", 200, "some text");	
		
		Assert.assertTrue(connector.stopSession("1234"));
	}
}
