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
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;
import org.apache.commons.io.FileUtils;
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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

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
	
	private final Capabilities capabilities = new DesiredCapabilities();
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
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		if (mockedHttpClient != null) {
			mockedHttpClient.close();
		}
	}
	
	@Test(groups={"ut"})
	public void testGetSessionInformationFromGrid() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {

		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "abcdef"));
		
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_VERSION, "51.0");
		
		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);
		
		connector.getSessionInformationFromGrid(driver);
		
		verify(logger).info("Browser firefox (51.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");
		
		// check sessionId is set when test is started
		verify(connector).setSessionId(any(SessionId.class));
		Assert.assertEquals(connector.getNodeUrl(), "http://localhost:4321");
	}

	@Test(groups={"ut"})
	public void testGetSessionInformationFromGridMultipleTries() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {

		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "aaaaa"), String.format(GRID_STATUS_WITH_SESSION, "abcdef"));

		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_VERSION, "50.0");

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));

		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);

		connector.getSessionInformationFromGrid(driver);

		verify(logger).info("Browser firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");

		// check sessionId is set when test is started
		verify(connector).setSessionId(any(SessionId.class));
		Assert.assertEquals(connector.getNodeUrl(), "http://localhost:4321");
	}

	@Test(groups={"ut"}, expectedExceptions = {WebDriverException.class})
	public void testGetSessionInformationFromGridNotGet() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {

		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, String.format(GRID_STATUS_WITH_SESSION, "aaaaa"));

		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_VERSION, "50.0");

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));

		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);

		connector.getSessionInformationFromGrid(driver);
	}

	@Test(groups={"ut"})
	public void testGetSessionInformationFromGridWithSecondDriver() throws UnsupportedOperationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, UnirestException {
		
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
		verify(logger).info("Browser firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session abcdef");
		verify(logger).info("Browser firefox (50.0) created in 0.0 secs on node localhost [http://localhost:4321] with session ghijkl");
	}
	
	/**
	 * With simple selenium grid, upload is not available
	 */
	@Test(groups={"ut"})
	public void testDoNothing() throws IOException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector("http://localhost:6666");
		connector.uploadMobileApp(new DesiredCapabilities());
		
		verify(client, never()).execute(any(HttpHost.class), any(HttpRequest.class));
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridNotPresent() throws UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.STATUS_SERVLET)).thenReturn(getRequest);
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridPresent() throws UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.STATUS_SERVLET, 200, "{\"value\": {\"ready\": true}}");	
		
		Assert.assertTrue(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testIsGridActiveWithGridInError() throws UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		createServerMock("GET", SeleniumGridConnector.STATUS_SERVLET, 500, "some text");	
		
		Assert.assertFalse(connector.isGridActive());
	}
	
	@Test(groups={"ut"})
	public void testStopSession() throws UnirestException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector(SERVER_URL);
		connector.setNodeUrl("http://localhost:5555");
		createServerMock("http://localhost:5555", "DELETE", "/se/grid/node/session/1234", 200, "some text");	
		
		Assert.assertTrue(connector.stopSession("1234"));
	}


	@Test(groups={"ut"})
	public void testListFilesToDownload() {

		createServerMock("GET", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "names": [
				      "Red-blue-green-channel.jpg"
				    ]
				  }
				}""");

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		connector.setNodeUrl("http://localhost:4421");
		connector.setSessionId(new SessionId("1234"));

		List<String> fileNames = connector.listFilesToDownload();
		Assert.assertEquals(fileNames.size(), 1);
		Assert.assertEquals(fileNames.get(0), "Red-blue-green-channel.jpg");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testListFilesToDownloadNodeUrlNull() {

		createServerMock("GET", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "names": [
				      "Red-blue-green-channel.jpg"
				    ]
				  }
				}""");

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		
		connector.listFilesToDownload();
	}

	@Test(groups={"ut"})
	public void testListFilesToDownloadNoFile() {

		createServerMock("GET", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "names": [
				    ]
				  }
				}""");

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		connector.setNodeUrl("http://localhost:4421");
		connector.setSessionId(new SessionId("1234"));

		List<String> fileNames = connector.listFilesToDownload();
		Assert.assertEquals(fileNames.size(), 0);
	}

	@Test(groups={"ut"})
	public void testListFilesToDownloadError() {

		createServerMock("GET", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "message": "error",
				  }
				}""");

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		connector.setNodeUrl("http://localhost:4421");
		connector.setSessionId(new SessionId("1234"));

		List<String> fileNames = connector.listFilesToDownload();
		Assert.assertEquals(fileNames.size(), 0);
	}

	@Test(groups={"ut"})
	public void testListFilesToDownloadError2() {

		kong.unirest.HttpRequest<?> request = createServerMock("GET", "/session/1234/se/files", 500, "{}");
		when(request.asJson()).thenThrow(UnirestException.class);

		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		connector.setNodeUrl("http://localhost:4421");
		connector.setSessionId(new SessionId("1234"));

		List<String> fileNames = connector.listFilesToDownload();
		Assert.assertEquals(fileNames.size(), 0);
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromName() throws IOException, NoSuchFieldException, IllegalAccessException {

		kong.unirest.HttpRequest<?> request = createServerMock("POST", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "filename": "foo.txt",
				    "contents": "UEsDBBQACAAIAC12ZloAAAAAAAAAAAAAAAAHACAAZm9vLnR4dHV4CwABBAAAAAAEAAAAAFVUDQAH56fJZ+enyWfbp8lnS8vPBwBQSwcIIWVzjAUAAAADAAAAUEsBAhQDFAAIAAgALXZmWiFlc4wFAAAAAwAAAAcAGAAAAAAAAAAAALaBAAAAAGZvby50eHR1eAsAAQQAAAAABAAAAABVVAUAAeenyWdQSwUGAAAAAAEAAQBNAAAAWgAAAAAA"
				  }
				}""");

		File file = configureAndDownload(logger);
		Assert.assertTrue(file.exists());
		Assert.assertEquals(FileUtils.readFileToString(file, StandardCharsets.UTF_8), "foo");
		verify(request).header("Content-Type", "application/json; charset=utf-8");
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromNameException1() throws IOException, NoSuchFieldException, IllegalAccessException {

		kong.unirest.HttpRequest<?> request = createServerMock("POST", "/session/1234/se/files", 500, "{}", "requestBodyEntity");
		when(request.asJson()).thenThrow(UnirestException.class);

		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		File file = configureAndDownload(logger);
		Assert.assertNull(file);
		verify(logger).error("Cannot download file {}: {}",	"foo.txt",null);
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromNameException2() throws IOException, NoSuchFieldException, IllegalAccessException {

		kong.unirest.HttpRequest<?> request = createServerMock("POST", "/session/1234/se/files", 500, "{}", "requestBodyEntity");
		when(request.asJson()).thenThrow(new RuntimeException("foo"));

		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		File file = configureAndDownload(logger);
		Assert.assertNull(file);
		verify(logger).error("Error downloading file: {}","foo");
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromNameInvalidPath() throws NoSuchFieldException, IllegalAccessException {

		createServerMock("POST", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "filename": "foo.txt",
				    "contents": "UEsDBBQACAAIAC12ZloAAAAAAAAAAAAAAAAHACAAZm9vLnR4dHV4CwABBAAAAAAEAAAAAFVUDQAH56fJZ+enyWfbp8lnS8vPBwBQSwcIIWVzjAUAAAADAAAAUEsBAhQDFAAIAAgALXZmWiFlc4wFAAAAAwAAAAcAGAAAAAAAAAAAALaBAAAAAGZvby50eHR1eAsAAQQAAAAABAAAAABVVAUAAeenyWdQSwUGAAAAAAEAAQBNAAAAWgAAAAAA"
				  }
				}""");
		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);

		connector.setNodeUrl("http://localhost:4421");
		connector.setSessionId(new SessionId("1234"));

		File file = connector.downloadFileFromName("foo.txt", new File("G:\\somefolder"));
		Assert.assertNull(file);
		verify(logger).error("Error downloading file: {}",
				"Cannot invoke \"java.io.File.exists()\" because \"dir\" is null");
	}

	@Test(groups={"ut"})
	public void testDownloadFileFromNameNoFile() throws IOException, NoSuchFieldException, IllegalAccessException {

		createServerMock("POST", "/session/1234/se/files", 200, """
				{
				  "value": {}
				}""");

		File file = configureAndDownload(logger);
		Assert.assertNull(file);
	}

	/**
	 * Selenium grid returns an error with message
	 */
	@Test(groups={"ut"})
	public void testDownloadFileFromNameWithError() throws IOException, NoSuchFieldException, IllegalAccessException {

		createServerMock("POST", "/session/1234/se/files", 200, """
				{
				  "value": {
				    "message": "error message"
				  }
				}""");


		Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumGridConnector.class));
		File file = configureAndDownload(logger);
		Assert.assertNull(file);
		verify(logger).warn("Error downloading file: {}",
				"error message");
	}

	private static File configureAndDownload(Logger logger) throws NoSuchFieldException, IllegalAccessException, IOException {
		SeleniumGridConnector connector = spy(new SeleniumGridConnector(SERVER_URL));
		Field loggerField = SeleniumGridConnector.class.getDeclaredField("logger");
		loggerField.setAccessible(true);
		loggerField.set(connector, logger);

		connector.setNodeUrl("http://localhost:4421");
		connector.setSessionId(new SessionId("1234"));

		Path tempsDir = Files.createTempDirectory("sel");
		return connector.downloadFileFromName("foo.txt", tempsDir.toFile());
	}
}
