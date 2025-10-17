/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import com.seleniumtests.util.logging.SeleniumRobotLogger;
import io.appium.java_client.android.options.UiAutomator2Options;
import kong.unirest.Headers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridException;

import kong.unirest.GetRequest;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
public class TestSeleniumRobotGridConnector extends ConnectorsTest {
	
	
	private static final String GRID_STATUS_WITH_SESSION_OTHER_NODE = "{"
			+ "  \"value\": {"
			+ "    \"ready\": true,"
			+ "    \"message\": \"Selenium Grid ready.\","
			+ "    \"nodes\": ["
			+ "      {"
			+ "        \"id\": \"9f05db23-5533-48c2-8637-0822f8a41d78\","
			+ "        \"uri\": \"http:\\u002f\\u002flocalhost2:5555\","
			+ "        \"maxSessions\": 3,"
			+ "        \"osInfo\": {"
			+ "          \"arch\": \"amd64\","
			+ "          \"name\": \"Windows 10\","
			+ "          \"version\": \"10.0\""
			+ "        },"
			+ "        \"heartbeatPeriod\": 60000,"
			+ "        \"availability\": \"UP\","
			+ "        \"version\": \"4.2.2 (revision 683ccb65d6)\","
			+ "        \"slots\": ["
			+ "          {"
			+ "            \"id\": {"
			+ "              \"hostId\": \"9f05db23-5533-48c2-8637-0822f8a41d78\","
			+ "              \"id\": \"6fd05336-ded3-4602-a1b8-03d94b3f56fa\""
			+ "            },"
			+ "            \"lastStarted\": \"1970-01-01T00:00:00Z\","
			+ "            \"session\": {"
			+ "				 \"sessionId\": \"%s\","
			+ "				 \"uri\": \"http:\\u002f\\u002flocalhost2:4321\""
			+ "            },"
			+ "            \"stereotype\": {"
			+ "              \"beta\": true,"
			+ "              \"browserName\": \"MicrosoftEdge\","
			+ "              \"browserVersion\": \"103.0\","
			+ "              \"edge_binary\": \"C:\\u002fProgram Files (x86)\\u002fMicrosoft\\u002fEdge Beta\\u002fApplication\\u002fmsedge.exe\","
			+ "              \"max-sessions\": 5,"
			+ "              \"nodeTags\": ["
			+ "                \"toto\""
			+ "              ],"
			+ "              \"platform\": \"Windows 10\","
			+ "              \"platformName\": \"Windows 10\","
			+ "              \"restrictToTags\": false,"
			+ "              \"se:webDriverExecutable\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\","
			+ "              \"webdriver-executable\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\","
			+ "              \"webdriver.edge.driver\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\""
			+ "            }"
			+ "          }"
			+ "        ]"
			+ "      }"
			+ "    ]"
			+ "  }"
			+ "}";
	
	
	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private RemoteWebDriver driver2;
	
	@Mock
	private GetRequest getRequest;
	
	@Mock
	private StatusLine statusLine;
	
	private SeleniumRobotGridConnector connector;
	private Logger gridLogger;

	private MockedStatic<HttpClients> mockedHttpClients;
	
	private MutableCapabilities capabilities;
	private MutableCapabilities mobileCapabilities;

	@BeforeMethod(groups={"ut"})
	private void init() throws IOException {

		capabilities = new MutableCapabilities();
		mobileCapabilities = new UiAutomator2Options();
		mockedHttpClients = mockStatic(HttpClients.class);
		mockedHttpClients.when(HttpClients::createDefault).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)any(), any())).thenReturn(response);
		when(driver.getCapabilities()).thenReturn(capabilities);
		when(driver.getSessionId()).thenReturn(new SessionId("abcdef"));
		when(driver2.getCapabilities()).thenReturn(capabilities);
		when(driver2.getSessionId()).thenReturn(new SessionId("ghijkl"));

		connector = new SeleniumRobotGridConnector(SERVER_URL + "/wd/hub");
		connector.setNodeUrl("http://localhost:4321");
		connector.setSessionId(new SessionId("1234"));
		gridLogger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		SeleniumRobotGridConnector.setLogger(gridLogger);

	}

	@AfterMethod(alwaysRun = true)
	private void reset () {
		mockedHttpClients.close();
	}
	
	@Test(groups={"ut"})
	public void testSendApp() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();
		((UiAutomator2Options)mobileCapabilities).setApp(appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = IOUtils.toInputStream("file:app/zip", StandardCharsets.UTF_8);
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);

		mobileCapabilities = connector.uploadMobileApp(new MutableCapabilities(mobileCapabilities));
		Assert.assertEquals(new UiAutomator2Options(mobileCapabilities).getApp().orElse(null), "file:app/zip/" + appFile.getName());
	}

	@Test(groups={"ut"})
	public void testSendAppNoApp() throws UnsupportedOperationException, IOException {

		// prepare response
		InputStream is = IOUtils.toInputStream("file:app/zip", StandardCharsets.UTF_8);
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		mobileCapabilities.setCapability("foo", "bar");

		mobileCapabilities = connector.uploadMobileApp(new MutableCapabilities(mobileCapabilities));
		Assert.assertEquals(mobileCapabilities.getCapability("appium:foo"), "bar");
	}

	@Test(groups={"ut"})
	public void testSendAppNotMobile() throws UnsupportedOperationException, IOException {

		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();

		// prepare response
		InputStream is = IOUtils.toInputStream("file:app/zip", StandardCharsets.UTF_8);
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);

		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability("appium:app", appFile.getAbsolutePath());

		mobileCapabilities = connector.uploadMobileApp(caps);

		// nothing has been uploaded
		Assert.assertEquals(new UiAutomator2Options(mobileCapabilities).getApp().orElse(null), appFile.getAbsolutePath());
	}
	
	@Test(groups={"ut"}, expectedExceptions = SeleniumGridException.class)
	public void testSendAppInError() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();
		((UiAutomator2Options)mobileCapabilities).setApp(appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = IOUtils.toInputStream("file:app/zip", StandardCharsets.UTF_8);
		when(statusLine.getStatusCode()).thenReturn(500);
		when(entity.getContent()).thenReturn(is);
		
		connector.uploadMobileApp(mobileCapabilities);
	}
	
	/**
	 * When APP is an HTTP link, do nothing
	 */
	@Test(groups={"ut"})
	public void testDontSendAppWhenHttp() throws IOException {
		
		// prepare app key
		((UiAutomator2Options)mobileCapabilities).setApp("http://server:port/data/application.apk");
		
		connector.uploadMobileApp(mobileCapabilities);
		
		verify(client, never()).execute((HttpHost)any(), any());
		Assert.assertEquals(((UiAutomator2Options)mobileCapabilities).getApp().orElse(null), "http://server:port/data/application.apk");
	}
	
	/**
	 * When APP is null, do nothing
	 */
	@Test(groups={"ut"})
	public void testDontSendAppWhenAppIsNull() throws IOException {
		
		connector.uploadMobileApp(new DesiredCapabilities());
		
		verify(client, never()).execute((HttpHost)any(), any());
		Assert.assertEquals(((UiAutomator2Options)mobileCapabilities).getApp().orElse(""), "");
	}
	
	/**
	 * Test get mouse coordinates
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinates() throws UnsupportedOperationException {
		
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "13,15");	
		
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
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinatesWrontFormat() throws UnsupportedOperationException {
		
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "13");	
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 0);
		Assert.assertEquals(coords.y, 0);
		
		// no error encountered
		verify(req).queryString("action", "mouseCoordinates");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("mouse coordinates '{}' are invalid",
				"13");
	}
	
	/**
	 * Test get mouse coordinates with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinatesError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 0);
		Assert.assertEquals(coords.y, 0);
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("Mouse coordinates error: {}",
				"");
	}
	
	/**
	 * Test get mouse coordinates when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testGetMouseCoordinatesNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		Point coords = connector.getMouseCoordinates();	
		Assert.assertEquals(coords.x, 0);
		Assert.assertEquals(coords.y, 0);
		
		// error connecting to node
		verify(gridLogger).warn("Could not get mouse coordinates: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetMouseCoordinatesWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.getMouseCoordinates();		
	}
	
	/**
	 * Test left click
	 */
	@Test(groups={"ut"})
	public void testLeftClick() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testLeftClickOnMainScreen() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testLeftClickError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.leftClic(0, 0);	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("Left click error: {}",
				"");
	}
	
	/**
	 * Test left click when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testLeftClickNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.leftClic(0, 0);	
		
		// error connecting to node
		verify(gridLogger).warn("Could not click left: {}}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testLeftClickWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.leftClic(0, 0);		
	}
	
	/**
	 * Test double click
	 */
	@Test(groups={"ut"})
	public void testDoubleClick() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testDoubleClickOnMainScreen() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testDoubleClickError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.doubleClick(0, 0);	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("Double click error: {}",
				"");
	}
	
	/**
	 * Test double click when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testDoubleClickNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.doubleClick(0, 0);	
		
		// error connecting to node
		verify(gridLogger).warn("Could not double click: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDoubleClickWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.doubleClick(0, 0);		
	}
	
	/**
	 * Test right click
	 */
	@Test(groups={"ut"})
	public void testRightClick() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testRightClickOnMainScreen() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testRightClickError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.rightClic(0, 0);	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("Right click error: {}",
				"");
	}
	
	/**
	 * Test right click when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testRightClickNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.rightClic(0, 0);	
		
		// error connecting to node
		verify(gridLogger).warn("Could not click right: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testRightClickWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.rightClic(0, 0);		
	}
	
	@Test(groups={"ut"})
	public void testCaptureDesktop() throws UnsupportedOperationException {
		
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABCDE");	
		
		String b64Img = connector.captureDesktopToBuffer();
		Assert.assertEquals(b64Img, "ABCDE");
		
		// no error encountered
		verify(req).queryString("action", "screenshot");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"})
	public void testCaptureDesktopError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "ABCDE");	
		
		String b64Img = connector.captureDesktopToBuffer();
		Assert.assertEquals(b64Img, "");
		
		// error on capture
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("capture desktop error: {}",
				"ABCDE");
	}
	
	@Test(groups={"ut"})
	public void testCaptureDesktopNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "ABCDE");	
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		String b64Img = connector.captureDesktopToBuffer();
		Assert.assertEquals(b64Img, "");
		
		// error on capture
		verify(gridLogger).warn("Could not capture desktop: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testCaptureDesktopWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.captureDesktopToBuffer();		
	}
	

	/**
	 * Test upload file
	 */
	@Test(groups={"ut"})
	public void testUploadFile() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testUploadFileError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.uploadFileToBrowser("foo", "ABCDE");		
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("Error uploading file: {}",
				"");
	}
	
	/**
	 * Test upload file when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testUploadFileNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "", "requestBodyEntity");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.uploadFileToBrowser("foo", "ABCDE");		
		
		// error connecting to node
		verify(gridLogger).warn("Cannot upload file: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testUploadFileWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.uploadFileToBrowser("foo", "ABCDE");		
	}
	

	/**
	 * Test download file
	 */
	@Test(groups={"ut"})
	public void testDownloadFile() throws UnsupportedOperationException, IOException {
		
		File f = File.createTempFile("foo",  ".bar");
		FileUtils.write(f, "ABCD", StandardCharsets.UTF_8);
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.FILE_SERVLET, 200, f);	
		
		File file = connector.downloadFileFromNode("upload/foo");
		
		Assert.assertEquals(FileUtils.readFileToString(file, StandardCharsets.UTF_8), "ABCD");
		
		// no error encountered
		verify(req).queryString("file", "file:upload/foo");
		verify(req).asFile(anyString(), eq(StandardCopyOption.REPLACE_EXISTING));
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test download file when requested path is not in upload folder
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "File path foo is invalid, only path in 'upload' folder are allowed")
	public void testDownloadFileWrongPath() throws UnsupportedOperationException, IOException {
		
		File f = File.createTempFile("foo",  ".bar");
		FileUtils.write(f, "ABCD", StandardCharsets.UTF_8);
		createGridServletServerMock("GET", SeleniumRobotGridConnector.FILE_SERVLET, 200, f);
		
		connector.downloadFileFromNode("foo");
	}
	
	/**
	 * Test download file with status code different from 200
	 */
	@Test(groups={"ut"}, expectedExceptions = SeleniumGridException.class, expectedExceptionsMessageRegExp = "Error downloading file upload/foo: .*")
	public void testDownloadFileError500() throws UnsupportedOperationException, IOException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.FILE_SERVLET, 500, File.createTempFile("foo",  ".bar"));	

		connector.downloadFileFromNode("upload/foo");
	}
	
	/**
	 * Test download file when connection error occurs
	 */
	@Test(groups={"ut"}, expectedExceptions = SeleniumGridException.class, expectedExceptionsMessageRegExp = "Cannot download file: upload/foo: connection error")
	public void testDownloadFileNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<?> req = createGridServletServerMock("GET", SeleniumRobotGridConnector.FILE_SERVLET, 500, File.createTempFile("foo",  ".bar"), "requestBodyEntity");
		when(req.asFile(anyString(), any(StandardCopyOption.class))).thenThrow(new UnirestException("connection error"));

		connector.downloadFileFromNode("upload/foo");
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class, expectedExceptionsMessageRegExp = "You cannot download file before driver has been created and corresponding node instanciated")
	public void testDownloadFileWithoutNodeUrl() throws UnsupportedOperationException, IOException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.FILE_SERVLET, 200, File.createTempFile("foo",  ".bar"));	

		connector.setNullNodeUrl();
		connector.downloadFileFromNode("upload/foo");
	}

	
	/**
	 * Test send keys with keyboard
	 */
	@Test(groups={"ut"})
	public void testSendKeysWithKeyboard() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));	
		
		// no error encountered
		verify(req).queryString("action", "sendKeys");
		verify(req).queryString("keycodes", "48,10");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test send keys with keyboard with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testSendKeysWithKeyboardError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("Send keys error: {}",
				"");
	}
	
	/**
	 * Test send keys with keyboard when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testSendKeysWithKeyboardNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));		
		
		// error connecting to node
		verify(gridLogger).warn("Could send keys: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeysWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNullNodeUrl();
		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_0, KeyEvent.VK_ENTER));	
	}
	
	/**
	 * Test write text
	 */
	@Test(groups={"ut"})
	public void testWriteText() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.writeText("foo");	
		
		// no error encountered
		verify(req).queryString("action", "writeText");
		verify(req).queryString("text", "foo");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
		verify(gridLogger).info("writing text: {}", "fo****");
	}
	
	@Test(groups={"ut"})
	public void testWriteShortText() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.writeText("f");	
		
		// no error encountered
		verify(req).queryString("action", "writeText");
		verify(req).queryString("text", "f");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
		verify(gridLogger).info("writing text: {}",
				"f****");
	}
	
	/**
	 * Test write text with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testWriteTextError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.writeText("foo");	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(anyString(), anyString());
	}
	
	/**
	 * Test write text when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testWriteTextNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.writeText("foo");			
		
		// error connecting to node
		verify(gridLogger).warn("Could not write text: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testWriteTextWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.writeText("foo");	
	}
	
	/**
	 * Test display running step
	 */
	@Test(groups={"ut"})
	public void testDisplayRunningStep() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
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
	 */
	@Test(groups={"ut"})
	public void testDisplayRunningStepError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	
		
		connector.displayRunningStep("foo");	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("display running step error: {}",
				"");
	}
	
	/**
	 * Testdisplay running step when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testDisplayRunningStepNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));
		
		connector.displayRunningStep("foo");			
		
		// error connecting to node
		verify(gridLogger).warn("Could not display running step: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDisplayRunningStepWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.displayRunningStep("foo");	
	}
	
	/**
	 * Test killing process
	 */
	@Test(groups={"ut"})
	public void testKillProcess() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody) createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.killProcess("myProcess");	

		// no error encountered
		verify(req).queryString("action", "kill");
		verify(req).queryString("process", "myProcess");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test kill process with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testKillProcessError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.killProcess("myProcess");	
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("kill process error: {}",
				"");
	}
	
	/**
	 * Test kill process when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testKillProcessNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.killProcess("myProcess");		
		
		// error connecting to node
		verify(gridLogger).warn("Could not kill process {}: {}",
				"myProcess",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}
	
	/**
	 * Test killing process when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testKillProcessWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.killProcess("myProcess");	
	}
	
	@Test(groups={"ut"})
	public void testGetProcessList() throws UnsupportedOperationException {
		
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "100,200");	
		
		Assert.assertEquals(connector.getProcessList("myProcess"), Arrays.asList(100, 200));
		
		// no error encountered
		verify(req).queryString("action", "processList");
		verify(req).queryString("name", "myProcess");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"ut"})
	public void testGetProcessListEmpty() throws UnsupportedOperationException {
		
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, " ");	
		
		Assert.assertEquals(connector.getProcessList("myProcess"), new ArrayList<>());
		
		// no error encountered
		verify(req).queryString("action", "processList");
		verify(req).queryString("name", "myProcess");
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test get process list with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testGetProcessListError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.getProcessList("myProcess");
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("get process list error: {}",
				"");
	}
	
	/**
	 * Test get process list when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testGetProcessListNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.getProcessList("myProcess");	
		
		// error connecting to node
		verify(gridLogger).warn("Could not get process list of {}: {}",
				"myProcess",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test getting process list when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetProcessListWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "100,200");	

		connector.setNullNodeUrl();
		connector.getProcessList("myProcess");
	}

	/**
	 * Start video capture
	 */
	@Test(groups={"ut"})
	public void testStartVideoCapture() throws UnsupportedOperationException {
		
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.startVideoCapture();
		
		// no error encountered
		verify(req).queryString("action", "startVideoCapture");
		verify(req).queryString("session", new SessionId("1234"));
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Start video capture with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testStartVideoCaptureError500() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");	

		connector.startVideoCapture();
		
		// error clicking
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error("start video capture error: {}",
				"");
	}
	
	/**
	 * Test Start video capture when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testStartVideoCaptureNoConnection() throws UnsupportedOperationException {
		
		HttpRequest<?> req = createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asString()).thenThrow(new UnirestException("connection error"));

		connector.startVideoCapture();
		
		// error connecting to node
		verify(gridLogger).warn("Could start video capture: {}",
				"connection error");
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Start video capture when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartVideoCaptureWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		connector.setNullNodeUrl();
		connector.startVideoCapture();
	}
	/**
	 * Stop video capture
	 */
	@Test(groups={"ut"})
	public void testStopVideoCapture() throws UnsupportedOperationException, IOException {

		Headers headers = new Headers();
		headers.add("Content-Type", "video/x-msvideo");
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, stream, headers);

		File out = connector.stopVideoCapture("out.avi");
		
		// no error encountered
		verify(req).queryString("action", "stopVideoCapture");
		verify(req).queryString("session", new SessionId("1234"));
		Assert.assertNotNull(out);
		Assert.assertTrue(out.getName().endsWith(".avi"));
		Assert.assertEquals(out, stream);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	/**
	 * Stop video capture
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureMp4() throws UnsupportedOperationException, IOException {

		Headers headers = new Headers();
		headers.add("Content-Type", "video/mp4");
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		GetRequest req = (GetRequest) createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, stream, headers);

		File out = connector.stopVideoCapture("out.avi");

		// no error encountered
		verify(req).queryString("action", "stopVideoCapture");
		verify(req).queryString("session", new SessionId("1234"));
		Assert.assertNotNull(out);
		Assert.assertEquals(out.getAbsolutePath(), stream.getAbsolutePath().replace(".avi", ".mp4"));
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Stop video capture with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureError500() throws UnsupportedOperationException, IOException {

		File stream = createImageFromResource("tu/video/videoCapture.avi");
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, stream);	
		
		File out = connector.stopVideoCapture("out.mp4");
		
		Assert.assertNull(out);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(eq("stop video capture error: {}"), any(File.class));
	}
	
	/**
	 * Test Stop video capture when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureNoConnection() throws UnsupportedOperationException, IOException {
		
		HttpRequest<?> req = createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 500, "");
		when(req.asFile(anyString())).thenThrow(new UnirestException("connection error"));

		Files.deleteIfExists(new File("out.mp4").toPath());
		File out = connector.stopVideoCapture("out.mp4");
		
		// error connecting to node
		Assert.assertNull(out);
		verify(gridLogger).warn("Video file not get due to {}",
				"java.util.concurrent.ExecutionException");
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Test Stop video capture when node is still unknown (driver not initialized). In this case, ScenarioException must be thrown
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testStopVideoCaptureWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	

		connector.setNullNodeUrl();
		connector.stopVideoCapture("out.mp4");
	}
	

	@Test(groups={"ut"})
	public void testExecuteCommand() throws UnsupportedOperationException {
		
		HttpRequestWithBody req = (HttpRequestWithBody)createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "foo");	
		
		connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
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
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testExecuteCommandWithoutNodeUrl() throws UnsupportedOperationException {
		
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "foo");	
		
		connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.executeCommand("myProcess", "arg1");
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridNotPresent() {
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.STATUS_SERVLET)).thenReturn(getRequest);
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridInError() {
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.STATUS_SERVLET, 500, "some text");	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	
	/**
	 * Check that we get true if the grid is ACTIVE and at least one node is present
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridActive() {
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "abcdef"));	
		
		Assert.assertTrue(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the grid is INACTIVE
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridInactive() {
		
		String hubStatus = """
{
"http:\\u002f\\u002fnode1.company.com:5555": {
    "busy": false,
    "lastSessionStart": "2018-08-31T08:30:06Z",
    "version": "3.14.0",
    "usedTestSlots": 0,
    "testSlots": 1,
    "status": "INACTIVE"
  },
  "hub": {
    "version": "3.14.0",
    "status": "INACTIVE"
  },
  "success": true
}""";
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the grid is ACTIVE and no node is present
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridActiveWithoutNodes() {
		
		String hubStatus = """
{" +
  "hub": {
    "version": "3.14.0",
    "status": "ACTIVE"
  },
  "success": true
}""";
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the status returned by grid is invalid (no JSON)
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridActiveInvalidStatus() {
		
		String hubStatus = "null";
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	/**
	 * Check that we get false if the server returns code != 200
	 */
	@Test(groups={"ut"})
	public void testIsGridActiveWithRobotGridReplyInError()  {
		
		String hubStatus = "Internal Server Error";
		
		connector = new SeleniumRobotGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "some text");	
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 500, hubStatus);	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testGetSessionInformationFromGrid() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {
		
		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "abcdef"));
		
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		capabilities.setCapability(CapabilityType.BROWSER_VERSION, "50.0");
		
		connector = spy(new SeleniumRobotGridConnector(SERVER_URL));
		
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);
		
		connector.getSessionInformationFromGrid(driver);
		
		verify(logger).info("Brower firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");
		
		// check sessionId is set when test is started
		verify(connector).setSessionId(any(SessionId.class));
		Assert.assertEquals(connector.getNodeServletUrl(), "http://localhost:4331");
		verify(connector).setNodeUrl("http://localhost:4321");
	}


	@Test(groups={"ut"})
	public void testGetSessionInformationFromGridMultipleTries() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {

		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "aaaaa"), String.format(GRID_STATUS_WITH_SESSION, "abcdef"));

		capabilities.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		capabilities.setCapability(CapabilityType.BROWSER_VERSION, "50.0");

		connector = spy(new SeleniumRobotGridConnector(SERVER_URL));

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

	@Test(groups={"ut"}, expectedExceptions = {WebDriverException.class})
	public void testGetSessionInformationFromGridNotGet() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {

		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "aaaaa"));

		capabilities.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		capabilities.setCapability(CapabilityType.BROWSER_VERSION, "50.0");

		connector = spy(new SeleniumRobotGridConnector(SERVER_URL));

		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"})
	public void testGetSessionInformationFromGridWithSecondDriver() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {
		
		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "abcdef"), String.format(GRID_STATUS_WITH_SESSION_OTHER_NODE, "ghijkl"));
		
		capabilities.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		capabilities.setCapability(CapabilityType.BROWSER_VERSION, "50.0");
		
		connector = spy(new SeleniumRobotGridConnector(SERVER_URL));
		
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);
		
		// 2 drivers created inside the same test
		connector.getSessionInformationFromGrid(driver);
		Assert.assertEquals(connector.getNodeServletUrl(), "http://localhost:4331");
		connector.getSessionInformationFromGrid(driver2);
		
		// check that second driver opening on an other node (this should not happen) won't change the node servlet URL
		Assert.assertEquals(connector.getNodeServletUrl(), "http://localhost:4331");
		
		// issue #242: check sessionId is set only once when first driver is created
		verify(connector).setSessionId(any(SessionId.class));
		Assert.assertEquals(connector.getSessionId().toString(), "abcdef");
		Assert.assertEquals(connector.getNodeUrl(), "http://localhost:4321");
		
		// check that the driver session id is displayed in logs to help debugging
		verify(logger).info("Brower firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");
		verify(logger).info("Brower firefox (50.0) created in 0.0 secs on node localhost2 [http://localhost:4321] with session ghijkl");
	}
}
