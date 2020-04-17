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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tools.ant.filters.StringInputStream;
import org.mockito.Mock;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.customexception.ScenarioException;

import io.appium.java_client.remote.MobileCapabilityType;


@PrepareForTest({HttpClients.class, Unirest.class})
public class TestSeleniumRobotGridConnector extends ConnectorsTest {

	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private GetRequest getRequest;
	
	@Mock
	private StatusLine statusLine;
	
	private DesiredCapabilities capabilities = new DesiredCapabilities();
	
	@BeforeMethod(groups={"ut"})
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(HttpClients.class);
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)any(), any())).thenReturn(response);
		
		PowerMockito.mockStatic(Unirest.class);
	}
	
	@Test(groups={"ut"})
	public void testSendApp() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();
		capabilities.setCapability(MobileCapabilityType.APP, appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = new StringInputStream("file:app/zip");
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(capabilities);
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), "file:app/zip/" + appFile.getName());
	}
	
	/**
	 * When APP is an HTTP link, do nothing
	 * @throws ClientProtocolException 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDontSendAppWhenHttp() throws ClientProtocolException, IOException {
		
		// prepare app key
		capabilities.setCapability(MobileCapabilityType.APP, "http://server:port/data/application.apk");
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(capabilities);
		
		verify(client, never()).execute((HttpHost)any(), any());
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), "http://server:port/data/application.apk");
	}
	
	/**
	 * When APP is null, do nothing
	 * @throws ClientProtocolException 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDontSendAppWhenAppIsNull() throws ClientProtocolException, IOException {
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(new DesiredCapabilities());
		
		verify(client, never()).execute((HttpHost)any(), any());
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), null);
	}
	
	/**
	 * Test killing process
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testKillProcess() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.setNodeUrl("http://localhost:4321");
		connector.killProcess("myProcess");	
	}
	
	/**
	 * Test killing process when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testKillProcessWithoutNodeUrl() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.killProcess("myProcess");	
	}
	
	@Test(groups={"ut"})
	public void testGetProcessList() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "100,200");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.setNodeUrl("http://localhost:4321");
		Assert.assertEquals(connector.getProcessList("myProcess"), Arrays.asList(100, 200));
	}
	

	/**
	 * Test getting process list when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListWithoutNodeUrl() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "100,200");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.getProcessList("myProcess");
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridNotPresent() throws ClientProtocolException, IOException, UnirestException {
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.CONSOLE_SERVLET)).thenReturn(getRequest);
		when(getRequest.asString()).thenThrow(UnirestException.class);
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridInError() throws ClientProtocolException, IOException, UnirestException {
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 500, "some text");	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get true if the grid is ACTIVE and at least one node is present
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridActive() throws ClientProtocolException, IOException, UnirestException {
		
		String hubStatus = "{" +
				"\"http:\\u002f\\u002fnode1.company.com:5555\": {\r\n" + 
				"    \"busy\": false,\r\n" + 
				"    \"lastSessionStart\": \"2018-08-31T08:30:06Z\",\r\n" + 
				"    \"version\": \"3.14.0\",\r\n" + 
				"    \"usedTestSlots\": 0,\r\n" + 
				"    \"testSlots\": 1,\r\n" + 
				"    \"status\": \"ACTIVE\"\r\n" + 
				"  },\r\n" + 
				"  \"hub\": {\r\n" + 
				"    \"version\": \"3.14.0\",\r\n" + 
				"    \"status\": \"ACTIVE\"\r\n" + 
				"  },\r\n" + 
				"  \"success\": true\r\n" + 
				"}";
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertTrue(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the grid is INACTIVE
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridInactive() throws ClientProtocolException, IOException, UnirestException {
		
		String hubStatus = "{" +
				"\"http:\\u002f\\u002fnode1.company.com:5555\": {\r\n" + 
				"    \"busy\": false,\r\n" + 
				"    \"lastSessionStart\": \"2018-08-31T08:30:06Z\",\r\n" + 
				"    \"version\": \"3.14.0\",\r\n" + 
				"    \"usedTestSlots\": 0,\r\n" + 
				"    \"testSlots\": 1,\r\n" + 
				"    \"status\": \"INACTIVE\"\r\n" + 
				"  },\r\n" + 
				"  \"hub\": {\r\n" + 
				"    \"version\": \"3.14.0\",\r\n" + 
				"    \"status\": \"INACTIVE\"\r\n" + 
				"  },\r\n" + 
				"  \"success\": true\r\n" + 
				"}";
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the grid is ACTIVE and no node is present
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridActiveWithoutNodes() throws ClientProtocolException, IOException, UnirestException {
		
		String hubStatus = "{" +
				"  \"hub\": {\r\n" + 
				"    \"version\": \"3.14.0\",\r\n" + 
				"    \"status\": \"ACTIVE\"\r\n" + 
				"  },\r\n" + 
				"  \"success\": true\r\n" + 
				"}";
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the status returned by grid is invalid (no JSON)
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridActiveInvalidStatus() throws ClientProtocolException, IOException, UnirestException {
		
		String hubStatus = "null";
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the server returns code != 200
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridReplyInError() throws ClientProtocolException, IOException, UnirestException {
		
		String hubStatus = "Internal Server Error";
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 500, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
}
