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

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.connectors.selenium.SeleniumBrowserstackGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import io.appium.java_client.android.options.UiAutomator2Options;
import kong.unirest.core.GetRequest;
import kong.unirest.core.UnirestException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestSeleniumBrowserstackGridConnector extends ConnectorsTest {

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
	
	@Mock
	private RemoteWebDriver driver2;
	
	private Logger gridLogger;
	
	private final MutableCapabilities capabilities = new MutableCapabilities();
	private MutableCapabilities mobileCapabilities;
	private MockedStatic<HttpClients> mockedHttpClient;
	
	@BeforeMethod(groups={"ut"})
	private void init() throws IOException {
		mockedHttpClient = mockStatic(HttpClients.class);

		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute(any(HttpHost.class), any(HttpRequest.class))).thenReturn(response);
		when(driver.getCapabilities()).thenReturn(capabilities); 
		when(driver.getSessionId()).thenReturn(new SessionId("abcdef"));
		when(driver2.getCapabilities()).thenReturn(capabilities); 
		when(driver2.getSessionId()).thenReturn(new SessionId("ghijkl"));
		gridLogger = spy(SeleniumRobotLogger.getLogger(SeleniumBrowserstackGridConnector.class));
		mobileCapabilities = new UiAutomator2Options();

		SeleniumBrowserstackGridConnector.setLogger(gridLogger);
		SeleniumGridConnector.setLogger(gridLogger);
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		if (mockedHttpClient != null) {
			mockedHttpClient.close();
		}
	}

	@Test(groups={"ut"})
	public void testUploadMobileApp() throws IOException, URISyntaxException {

		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		appFile.deleteOnExit();
		((UiAutomator2Options)mobileCapabilities).setApp(appFile.getAbsolutePath());

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		createServerMock("https://api-cloud.browserstack.com", "POST", "/app-automate/upload", 200, """
				{
				    "app_url":"bs://f7c874f21852ba57957a3fdc33f47514288c4ba4"
				}""");

		UiAutomator2Options newCapabilities = (UiAutomator2Options) connector.uploadMobileApp(mobileCapabilities);
		Assert.assertEquals(newCapabilities.getApp().orElse("not found"), "bs://f7c874f21852ba57957a3fdc33f47514288c4ba4");
	}

	@Test(groups={"ut"})
	public void testUploadMobileAppWithProxy() throws IOException, URISyntaxException {
		try {
			System.setProperty("https.proxyHost", "localhost");
			System.setProperty("https.proxyPort", "8081");
			// prepare app file
			File appFile = File.createTempFile("app", ".apk");
			appFile.deleteOnExit();
			((UiAutomator2Options) mobileCapabilities).setApp(appFile.getAbsolutePath());

			SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
			createServerMock("https://api-cloud.browserstack.com", "POST", "/app-automate/upload", 200, """
					{
						"app_url":"bs://f7c874f21852ba57957a3fdc33f47514288c4ba4"
					}""");

			UiAutomator2Options newCapabilities = (UiAutomator2Options) connector.uploadMobileApp(mobileCapabilities);
			Assert.assertEquals(newCapabilities.getApp().orElse("not found"), "bs://f7c874f21852ba57957a3fdc33f47514288c4ba4");
		} finally {
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
		}
	}


	@Test(groups={"ut"})
	public void testUploadFile() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.uploadFile("");
		verify(gridLogger).warn("file upload is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testUploadFileToNode() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.uploadFileToNode("", false);
		verify(gridLogger).warn("file upload is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromNode() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.downloadFileFromNode("");
		verify(gridLogger).warn("file download is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testListFilesToDownload() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.listFilesToDownload();
		verify(gridLogger).warn("list files to download is not available on browserstack");
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromName() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.downloadFileFromName("", new File("foo.txt"));
		verify(gridLogger).warn("download file from name is not available on browserstack");
	}

	@Test(groups={"ut"})
	public void testKillProcess() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.killProcess("foo");
		verify(gridLogger).warn("kill is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testExecuteCommand() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.executeCommand("foo", "bar");
		verify(gridLogger).warn("executeCommand is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testExecuteCommand2() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.executeCommand("foo", 2, "bar");
		verify(gridLogger).warn("executeCommand is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testUploadFileToBrowser() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.uploadFileToBrowser("foo", "AAA");
		verify(gridLogger).warn("file upload to browser is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testLeftClick() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.leftClic(true, 0, 0);
		verify(gridLogger).warn("left click is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testLeftClick2() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.leftClic(0, 0);
		verify(gridLogger).warn("left click is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testDoubleClick() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.doubleClick(true, 0, 0);
		verify(gridLogger).warn("double click is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testDoubleClick2() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.doubleClick(0, 0);
		verify(gridLogger).warn("double click is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testRightClick() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.rightClic(true, 0, 0);
		verify(gridLogger).warn("right click is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testRightClick2() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.rightClic(0, 0);
		verify(gridLogger).warn("right click is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testGetMouseCoordinates() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		Point point = connector.getMouseCoordinates();
		verify(gridLogger).warn("getMouseCoordinates is only available with seleniumRobot grid");
		Assert.assertEquals(point.getX(), 0);
		Assert.assertEquals(point.getY(), 0);
	}

	@Test(groups={"ut"})
	public void testCaptureDesktopToBuffer() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.captureDesktopToBuffer();
		verify(gridLogger).warn("captureDesktopToBuffer is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testCaptureDesktopToBuffer2() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.captureDesktopToBuffer(false);
		verify(gridLogger).warn("captureDesktopToBuffer is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testSendKeysWithKeyboard() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.sendKeysWithKeyboard(new ArrayList<>());
		verify(gridLogger).warn("send keys is only available with seleniumRobot grid");
	}

	@Test(groups={"ut"})
	public void testStopSession() throws IOException, URISyntaxException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.stopSession("123");
		verify(gridLogger).warn("stopSession is not available on browserstack");
	}

	/**
	 * Stop video capture
	 */
	@Test(groups={"ut"})
	public void testStopVideoCapture() throws UnsupportedOperationException, IOException, URISyntaxException {
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.setVideoUrl("https://automate.browserstack.com/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167");
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		GetRequest req = (GetRequest) createServerMock("https://automate.browserstack.com", "GET", "/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167", 200, stream);

		File out = connector.stopVideoCapture("out.avi");

		// no error encountered
		verify(req).basicAuth("user", "token");
		Assert.assertNotNull(out);
		Assert.assertTrue(out.getName().endsWith(".mp4")); // browserstack records mp4 files
		Assert.assertEquals(out.getAbsolutePath(), stream.getAbsolutePath().replace(".avi", ".mp4"));
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}

	/**
	 * Stop video capture, using proxy
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureWithProxy() throws UnsupportedOperationException, IOException, URISyntaxException {
		try {
			System.setProperty("https.proxyHost", "localhost");
			System.setProperty("https.proxyPort", "8080");
			SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
			connector.setVideoUrl("https://automate.browserstack.com/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167");
			File stream = createImageFromResource("tu/video/videoCapture.avi");
			GetRequest req = (GetRequest) createServerMock("https://automate.browserstack.com", "GET", "/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167", 200, stream);

			File out = connector.stopVideoCapture("out.avi");

			// no error encountered
			verify(req).basicAuth("user", "token");
			Assert.assertNotNull(out);
			verify(unirestConfig).proxy("localhost", 8080);
			verify(unirestInstance).config();

			verify(gridLogger, never()).warn(anyString());
			verify(gridLogger, never()).error(anyString());
		} finally {
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
		}
	}

	/**
	 * Test Stop video capture with status code different from 200
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureError500() throws UnsupportedOperationException, IOException, URISyntaxException {
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.setVideoUrl("https://automate.browserstack.com/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167");
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		createServerMock("https://automate.browserstack.com", "GET", "/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167", 500, stream);

		File out = connector.stopVideoCapture("out.mp4");

		Assert.assertNull(out);
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger).error(eq("stop video capture error: {}"), any(File.class));
	}

	/**
	 * Test Stop video capture when connection error occurs
	 */
	@Test(groups={"ut"})
	public void testStopVideoCaptureNoConnection() throws UnsupportedOperationException, IOException, URISyntaxException {
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		connector.setVideoUrl("https://automate.browserstack.com/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167");
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		GetRequest req = (GetRequest) createServerMock("https://automate.browserstack.com", "GET", "/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167", 500, stream);
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
	public void testStopVideoCaptureWithoutVideoUrl() throws UnsupportedOperationException, URISyntaxException, IOException {
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		File stream = createImageFromResource("tu/video/videoCapture.avi");
		createServerMock("https://automate.browserstack.com", "GET", "/sessions/abc/video?token=cde&source=rest_api&diff=860746.273516167", 500, stream);

		connector.stopVideoCapture("out.mp4");
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridNotPresent() throws UnirestException, URISyntaxException, MalformedURLException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		GetRequest req = (GetRequest) createServerMock("https://api.browserstack.com", "GET", "/automate/projects.json", 200, "{}");
		when(req.asJson()).thenThrow(new UnirestException("connection error"));

		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridPresent() throws UnirestException, URISyntaxException, MalformedURLException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		createServerMock("https://api.browserstack.com", "GET", "/automate/projects.json", 200, "{}");

		Assert.assertTrue(connector.isGridActive());
	}

	@Test(groups={"ut"})
	public void testIsGridActiveWithGridPresentWithProxy() throws UnirestException, URISyntaxException, MalformedURLException {
		try {
			System.setProperty("https.proxyHost", "localhost");
			System.setProperty("https.proxyPort", "8080");
			SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
			createServerMock("https://api.browserstack.com", "GET", "/automate/projects.json", 200, "{}");

			Assert.assertTrue(connector.isGridActive());
			verify(unirestConfig).proxy("localhost", 8080);
		} finally {
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
		}
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridInError() throws UnirestException, URISyntaxException, MalformedURLException {

		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());
		createServerMock("https://api.browserstack.com", "GET", "/automate/projects.json", 500, "{}");

		Assert.assertFalse(connector.isGridActive());
	}

	private static final String BUILD_RESPONSE = """
    [
    {
        "automation_build":{
            "name":"myTest",
            "hashed_id":"ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7",
            "duration":15611,
            "status":"running",
            "build_tag":null,
            "public_url":"https://automate.browserstack.com/dashboard/v2/public-build/MkdsKzRhanlWQmtpc"
        }
    }]""";
	
	private static final String SESSION_RESPONSE = """
    [
    {
        "automation_session": {
            "name": "pricing_session",
            "duration": 44,
            "os": "windows",
            "os_version": "11.0",
            "browser_version": null,
            "browser": null,
            "status": "done",
            "hashed_id": "550709149fe79e949363b581e774d5ebffa1b8fe",
            "reason": "CLIENT_STOPPED_SESSION",
            "build_name": "pricing_build",
            "project_name": "pricing_project",
            "test_priority": null,
            "logs": "https://automate.browserstack.com/builds/abcdefg/sessions/hijklm/logs",
            "browser_url": "https://automate.browserstack.com/builds/abcdefg/sessions/hijklm",
            "public_url": "https://automate.browserstack.com/builds/abcdefg/sessions/hijklm?auth_token=myToken",
            "video_url": "https://automate.browserstack.com/sessions/hijklm/video?token=tok--77b9f745d91d9b99572a9e3c98dd001347f1b62c&source=rest_api&diff=860746.273516167",
            "browser_console_logs_url": "https://automate.browserstack.com/s3-upload/bs-selenium-logs-aps/s3.ap-south-1/hijklm/hijklm-console-logs-v2.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIA2XUQHUQMHPMYEM4V%2F20200918%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Date=20200918T102438Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=3bfd4e34fd6aa46bead6e16e418f6bee5b7798d7ef82a7e9546980dd7e93e917",
            "har_logs_url": "https://automate.browserstack.com/s3-upload/bs-selenium-logs-euw/s3.eu-west-1/hijklm/hijklm-har-logs.txt?",
            "selenium_logs_url": "https://automate.browserstack.com/s3-upload/bs-selenium-logs-euw/s3.eu-west-1/hijklm/hijklm-selenium-logs.txt?"
        }
    }]""";

	@Test(groups={"ut"})
	public void testGetSessionInformationFromGrid() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

		createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
		createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, SESSION_RESPONSE);
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);

		Assert.assertEquals(connector.getVideoUrl(), "https://automate.browserstack.com/sessions/hijklm/video?token=tok--77b9f745d91d9b99572a9e3c98dd001347f1b62c&source=rest_api&diff=860746.273516167");
		Assert.assertEquals(connector.getLogsUrl(), "https://automate.browserstack.com/builds/abcdefg/sessions/hijklm/logs");
		Assert.assertEquals(connector.getHarLogsUrl(), "https://automate.browserstack.com/s3-upload/bs-selenium-logs-euw/s3.eu-west-1/hijklm/hijklm-har-logs.txt?");
		Assert.assertEquals(connector.getSeleniumLogsUrl(), "https://automate.browserstack.com/s3-upload/bs-selenium-logs-euw/s3.eu-west-1/hijklm/hijklm-selenium-logs.txt?");
		Assert.assertNull(connector.getAppiumLogsUrl());

		// check sessionId is set when test is started
		Assert.assertEquals(driver.getCapabilities().getCapability(SeleniumRobotCapabilityType.GRID_HUB).toString(), "https://user:token@hub.browserstack.com/wd/hub");
		Assert.assertEquals(driver.getCapabilities().getCapability(SeleniumRobotCapabilityType.SESSION_ID).toString(), "abcdef");
	}

	@Test(groups={"ut"})
	public void testGetSessionInformationFromGridWithProxy() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {

		try {
			System.setProperty("https.proxyHost", "localhost");
			System.setProperty("https.proxyPort", "8081");
			capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myTest"); // we want to find the build
			capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

			createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
			createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, SESSION_RESPONSE);
			SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

			connector.getSessionInformationFromGrid(driver);

			Assert.assertEquals(connector.getVideoUrl(), "https://automate.browserstack.com/sessions/hijklm/video?token=tok--77b9f745d91d9b99572a9e3c98dd001347f1b62c&source=rest_api&diff=860746.273516167");
			verify(unirestConfig, times(2)).proxy("localhost", 8081); // 2 calls (sessions.json / builds.json)

		} finally {
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
		}
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*Could not find build ID.*")
	public void testGetSessionInformationFromGridBuildNotFound() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myOtherTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

		createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
		createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, SESSION_RESPONSE);
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*Couldn't get build id.*")
	public void testGetSessionInformationFromGridBuildError() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myOtherTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

		GetRequest req = (GetRequest) createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
		when(req.asJson()).thenThrow(new UnirestException("connection error"));
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*Couldn't get build id.*")
	public void testGetSessionInformationFromGridNoBuilds() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myOtherTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

		createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, "{}");
		createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, SESSION_RESPONSE);
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*Could not find session.*")
	public void testGetSessionInformationFromGridSessionNotfound() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "other_session"); // we want to find the session

		createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
		createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, SESSION_RESPONSE);
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*Couldn't find session.*")
	public void testGetSessionInformationFromGridSessionError() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

		createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
		GetRequest req = (GetRequest) createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, SESSION_RESPONSE);

		when(req.asJson()).thenThrow(new UnirestException("connection error"));
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*Couldn't find session.*")
	public void testGetSessionInformationFromGridNoSession() throws UnsupportedOperationException, SecurityException, IllegalArgumentException, UnirestException, URISyntaxException, MalformedURLException {
		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, "myTest"); // we want to find the build
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, "pricing_session"); // we want to find the session

		createServerMock("https://api.browserstack.com", "GET", "/automate/builds.json", 200, BUILD_RESPONSE);
		createServerMock("https://api.browserstack.com", "GET", "/automate/builds/ca9cccc228cf0e3ff3cb90dd62e2e2bfb4b20bc7/sessions.json?status=running", 200, "{}");
		SeleniumBrowserstackGridConnector connector = new SeleniumBrowserstackGridConnector(new URI("https://user:token@hub.browserstack.com/wd/hub").toURL());

		connector.getSessionInformationFromGrid(driver);
	}


}
