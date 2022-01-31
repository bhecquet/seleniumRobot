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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.mockito.Mock;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridException;

import io.appium.java_client.remote.MobileCapabilityType;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;


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
	
	private SeleniumGridConnector connector;
	private Logger gridLogger;
	
	private DesiredCapabilities capabilities = new DesiredCapabilities();
	
	@BeforeMethod(groups={"ut"})
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(HttpClients.class);
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)any(), any())).thenReturn(response);

		connector = new SeleniumRobotGridConnector(SERVER_URL + "/wd/hub");
		connector.setNodeUrl("http://localhost:4321");
		connector.setSessionId(new SessionId("1234"));
		gridLogger = spy(connector.getLogger());
		connector.setLogger(gridLogger);

	}
	
	@Test(groups={"ut"})
	public void testSendApp() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();
		capabilities.setCapability(MobileCapabilityType.APP, appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = IOUtils.toInputStream("file:app/zip", Charset.forName("UTF-8"));
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		
		connector.uploadMobileApp(capabilities);
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), "file:app/zip/" + appFile.getName());
	}
	
	@Test(groups={"ut"}, expectedExceptions = SeleniumGridException.class)
	public void testSendAppInError() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();
		capabilities.setCapability(MobileCapabilityType.APP, appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = IOUtils.toInputStream("file:app/zip", Charset.forName("UTF-8"));
		when(statusLine.getStatusCode()).thenReturn(500);
		when(entity.getContent()).thenReturn(is);
		
		connector.uploadMobileApp(capabilities);
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
		
		connector.uploadMobileApp(new DesiredCapabilities());
		
		verify(client, never()).execute((HttpHost)any(), any());
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), null);
	}
	
	/**
	 * Test get mouse coordinates
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinates() throws UnsupportedOperationException, IOException {
		
		GetRequest req = (GetRequest) createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "13,15");	
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 13);
		Assert.assertEquals(coords.y, 15);

		// no error encountered
		verify(req).queryString("action", "mouseCoordinates");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test mouse coordinates with an invalid response
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinatesWrontFormat() throws UnsupportedOperationException, IOException {
		
		GetRequest req = (GetRequest) createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "13");	
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 0);
		Assert.assertEquals(coords.y, 0);
		
		// no error encountered
		verify(req).queryString("action", "mouseCoordinates");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test get mouse coordinates with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinatesError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 0);
		Assert.assertEquals(coords.y, 0);
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test get mouse coordinates when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinatesNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 0);
		Assert.assertEquals(coords.y, 0);
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetMouseCoordinatesWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.getMouseCoordinates();		
	}
	
	/**
	 * Test left click
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testLeftClick() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.leftClic(100, 0);	

		// no error encountered
		verify(req).queryString("action", "leftClic");
		verify(req).queryString("x", 100);
		verify(req).queryString("y", 0);
		verify(req).queryString("onlyMainScreen", false);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test left click on main screen
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testLeftClickOnMainScreen() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.leftClic(true, 100, 0);	
		
		// no error encountered
		verify(req).queryString("action", "leftClic");
		verify(req).queryString("x", 100);
		verify(req).queryString("y", 0);
		verify(req).queryString("onlyMainScreen", true);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test left click with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testLeftClickError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.leftClic(0, 0);	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test left click when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testLeftClickNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.leftClic(0, 0);	
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testLeftClickWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.leftClic(0, 0);		
	}
	
	/**
	 * Test double click
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDoubleClick() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.doubleClick(100, 0);	
		
		// no error encountered
		verify(req).queryString("action", "doubleClick");
		verify(req).queryString("x", 100);
		verify(req).queryString("y", 0);
		verify(req).queryString("onlyMainScreen", false);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test double click on main screen
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDoubleClickOnMainScreen() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.doubleClick(true, 100, 0);	
		
		// no error encountered
		verify(req).queryString("action", "doubleClick");
		verify(req).queryString("x", 100);
		verify(req).queryString("y", 0);
		verify(req).queryString("onlyMainScreen", true);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test double click with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDoubleClickError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.doubleClick(0, 0);	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test double click when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDoubleClickNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.doubleClick(0, 0);	
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDoubleClickWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.doubleClick(0, 0);		
	}
	
	/**
	 * Test right click
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testRightClick() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.rightClic(100, 0);	
		
		// no error encountered
		verify(req).queryString("action", "rightClic");
		verify(req).queryString("x", 100);
		verify(req).queryString("y", 0);
		verify(req).queryString("onlyMainScreen", false);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test right click on main screen
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testRightClickOnMainScreen() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.rightClic(true, 100, 0);	
		
		// no error encountered
		verify(req).queryString("action", "rightClic");
		verify(req).queryString("x", 100);
		verify(req).queryString("y", 0);
		verify(req).queryString("onlyMainScreen", true);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test right click with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testRightClickError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.rightClic(0, 0);	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test right click when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testRightClickNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.rightClic(0, 0);	
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testRightClickWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.rightClic(0, 0);		
	}
	
	@Test(groups={"ut"})
	public void testCaptureDesktop() throws UnsupportedOperationException, IOException {
		
		GetRequest req = (GetRequest) createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABCDE");	
		
		String b64Img = connector.captureDesktopToBuffer();
		Assert.assertEquals(b64Img, "ABCDE");
		
		// no error encountered
		verify(req).queryString("action", "screenshot");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"})
	public void testCaptureDesktopError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "ABCDE");	
		
		String b64Img = connector.captureDesktopToBuffer();
		Assert.assertEquals(b64Img, "");
		
		// error on capture
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	@Test(groups={"ut"})
	public void testCaptureDesktopNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "ABCDE");	
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		String b64Img = connector.captureDesktopToBuffer();
		Assert.assertEquals(b64Img, "");
		
		// error on capture
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testCaptureDesktopWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.captureDesktopToBuffer();		
	}
	

	/**
	 * Test upload file
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testUploadFile() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.uploadFileToBrowser("foo", "ABCDE");	
		
		// no error encountered
		verify(req).queryString("action", "uploadFile");
		verify(req).queryString("name", "foo");
		verify(req).header(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
		verify(req).body(new byte[] {(byte) 0x00, (byte) 0x10, (byte) 0x83});
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test upload file with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testUploadFileError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.uploadFileToBrowser("foo", "ABCDE");		
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test upload file when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testUploadFileNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "", "requestBodyEntity");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.uploadFileToBrowser("foo", "ABCDE");		
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testUploadFileWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.uploadFileToBrowser("foo", "ABCDE");		
	}
	
	/**
	 * Test send keys with keyboard
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testSendKeysWithKeyboard() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));	
		
		// no error encountered
		verify(req).queryString("action", "sendKeys");
		verify(req).queryString("keycodes", "48,10");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test send keys with keyboard with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testSendKeysWithKeyboardError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test send keys with keyboard when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testSendKeysWithKeyboardNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));		
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeysWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));	
	}
	
	/**
	 * Test write text
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testWriteText() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.writeText("foo");	
		
		// no error encountered
		verify(req).queryString("action", "writeText");
		verify(req).queryString("text", "foo");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test write text with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testWriteTextError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.writeText("foo");	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test write text when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testWriteTextNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.writeText("foo");			
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testWriteTextWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.writeText("foo");	
	}
	
	/**
	 * Test display running step
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDisplayRunningStep() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.displayRunningStep("foo");	
		
		// no error encountered
		verify(req).queryString("action", "displayRunningStep");
		verify(req).queryString("stepName", "foo");
		verify(req).queryString("session", new SessionId("1234"));
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Testdisplay running step with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDisplayRunningStepError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.displayRunningStep("foo");	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Testdisplay running step when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testDisplayRunningStepNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.displayRunningStep("foo");			
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDisplayRunningStepWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.displayRunningStep("foo");	
	}
	
	/**
	 * Test killing process
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testKillProcess() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.killProcess("myProcess");	

		// no error encountered
		verify(req).queryString("action", "kill");
		verify(req).queryString("process", "myProcess");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test kill process with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testKillProcessError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.killProcess("myProcess");	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test kill process when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testKillProcessNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.killProcess("myProcess");		
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test killing process when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testKillProcessWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.killProcess("myProcess");	
	}
	
	@Test(groups={"ut"})
	public void testGetProcessList() throws UnsupportedOperationException, IOException {
		
		GetRequest req = (GetRequest) createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "100,200");	
		
		Assert.assertEquals(connector.getProcessList("myProcess"), Arrays.asList(100, 200));
		
		// no error encountered
		verify(req).queryString("action", "processList");
		verify(req).queryString("name", "myProcess");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test get process list with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testGetProcessListError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.getProcessList("myProcess");
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test get process list when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testGetProcessListNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.getProcessList("myProcess");	
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test getting process list when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "100,200");	
		
		connector.setNodeUrl(null);
		connector.getProcessList("myProcess");
	}

	/**
	 * Start video capture
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testStartVideoCapture() throws UnsupportedOperationException, IOException {
		
		GetRequest req = (GetRequest) createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.startVideoCapture();
		
		// no error encountered
		verify(req).queryString("action", "startVideoCapture");
		verify(req).queryString("session", new SessionId("1234"));
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Start video capture with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testStartVideoCaptureError500() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.startVideoCapture();
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test Start video capture when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testStartVideoCaptureNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.startVideoCapture();
		
		// error connecting to node
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Start video capture when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartVideoCaptureWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.startVideoCapture();
	}
	/**
	 * Stop video capture
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testStopVideoCapture() throws UnsupportedOperationException, IOException {
		
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		GetRequest req = (GetRequest) createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, stream);	
		
		File out = connector.stopVideoCapture("out.mp4");
		
		// no error encountered
		verify(req).queryString("action", "stopVideoCapture");
		verify(req).queryString("session", new SessionId("1234"));
		Assert.assertNotNull(out);
		Assert.assertEquals(out, stream);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Stop video capture with status code different from 200
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureError500() throws UnsupportedOperationException, IOException {

		File stream = createImageFromResource("tu/video/videoCapture.avi");
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, stream);	
		
		File out = connector.stopVideoCapture("out.mp4");
		
		Assert.assertNull(out);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString());
	}
	
	/**
	 * Test Stop video capture when connection error occurs
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<HttpRequest> req = createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asFile(anyString())).thenThrow(new UnirestException("connection error"));

		new File("out.mp4").delete();
		File out = connector.stopVideoCapture("out.mp4");
		
		// error connecting to node
		Assert.assertNull(out);
		verify(gridLogger).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Stop video capture when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testStopVideoCaptureWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNodeUrl(null);
		connector.stopVideoCapture("out.mp4");
	}
	

	@Test(groups={"ut"})
	public void testExecuteCommand() throws UnsupportedOperationException, IOException {
		
		HttpRequestWithBody req = (HttpRequestWithBody)createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "foo");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.setSessionId(new SessionId("1234"));
		connector.setNodeUrl("http://localhost:4321");
		Assert.assertEquals(connector.executeCommand("myProcess", "arg1", "arg2"), "foo");
		verify(req).queryString("arg0", "arg1");
		verify(req).queryString("arg1", "arg2");
		verify(req).queryString("name", "myProcess");
		verify(req).queryString("action", "command");
	}
	/**
	 * Test executing command when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testExecuteCommandWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "foo");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.executeCommand("myProcess", "arg1");
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridNotPresent() throws ClientProtocolException, IOException {
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.CONSOLE_SERVLET)).thenReturn(getRequest);
		when(getRequest.asString()).thenThrow(UnirestException.class);
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridInError() throws ClientProtocolException, IOException {
		
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
	public void testIsGridActiveWithRobotGridActive() throws ClientProtocolException, IOException {
		
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
	public void testIsGridActiveWithRobotGridInactive() throws ClientProtocolException, IOException {
		
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
	public void testIsGridActiveWithRobotGridActiveWithoutNodes() throws ClientProtocolException, IOException {
		
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
	public void testIsGridActiveWithRobotGridActiveInvalidStatus() throws ClientProtocolException, IOException {
		
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
	public void testIsGridActiveWithRobotGridReplyInError() throws ClientProtocolException, IOException {
		
		String hubStatus = "Internal Server Error";
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 500, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
}
