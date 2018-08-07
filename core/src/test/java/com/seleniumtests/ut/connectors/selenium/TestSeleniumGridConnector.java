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
package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.mockito.Mock;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.ut.connectors.ConnectorsTest;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


@PrepareForTest({HttpClients.class, TestLogging.class, Unirest.class})
public class TestSeleniumGridConnector extends ConnectorsTest {

	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private StatusLine statusLine;
	
	@Mock
	private RemoteWebDriver driver;
	
	private Capabilities capabilities = new DesiredCapabilities();
	
	@BeforeMethod(groups={"ut"})
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(HttpClients.class);
		PowerMockito.mockStatic(TestLogging.class);
		PowerMockito.mockStatic(Unirest.class);
		
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)any(HttpHost.class), any(HttpRequest.class))).thenReturn(response);
		when(driver.getCapabilities()).thenReturn(capabilities); 
		when(driver.getSessionId()).thenReturn(new SessionId("0"));
	}
	
	@Test(groups={"ut"})
	public void testRunTest() throws UnsupportedOperationException, IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {
		
		createServerMock("GET", "/grid/api/testsession/", 200, "{'proxyId': 'http://localhost:43210'}");	
		
		// prepare app file
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.VERSION, "50.0");
		
		// prepare response
		InputStream is = new StringInputStream("{'proxyId':'proxy//node:0'}");
		when(entity.getContent()).thenReturn(is);
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);
		
		connector.runTest(driver);
		
		verify(logger).info("WebDriver is running on node localhost, firefox 50.0, session 0");
		Assert.assertEquals(connector.getNodeUrl(), "http://localhost:43210");
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
}
