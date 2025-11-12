package com.seleniumtests.it.connector.selenium;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.osutility.OSUtility;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageProcessor;

import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;

/**
 * All these tests need to be executed with a selenium robot grid started on localhost, port 4444
 * In a continuous delivery process, they won't be executed
 *
 * to use an other grid than predefined (hub: http://127.0.0.1:4444/wd/hub and node: http://127.0.0.1:5555), use java properties: -DhubUrl=<> -DnodeUrl=<>
 *
 */
public class TestSeleniumRobotGridConnector extends MockitoTest {

	private SeleniumGridConnector connector;
	private Logger gridLogger;

	private String hubUrl;
	private String nodeUrl;

	@BeforeMethod(groups={"it"})
	public void initConnector(ITestContext ctx) {
		initThreadContext(ctx);
		hubUrl = System.getProperty("hubUrl");
		if (hubUrl == null) {
			hubUrl = "http://127.0.0.1:4444/wd/hub";
		}
		connector = new SeleniumRobotGridConnector(hubUrl);
		
		if (!connector.isGridActive()) {
			throw new SkipException("no seleniumrobot grid available");
		}
		nodeUrl = System.getProperty("nodeUrl");
		if (nodeUrl == null) {
			nodeUrl = "http://127.0.0.1:5555";
		}
		connector.setNodeUrl(nodeUrl);
		gridLogger = spy(SeleniumRobotGridConnector.getLogger());
	}

	/**
	 * Check it's possible to attach to an existing browser using SeleniumRobot grid
	 */
	@Test(groups={"it"})
	public void testMultipleBrowserCreationGridMode() {

		WebDriver driver1 = null;
		WebDriver driver2 = null;
		try {
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
			SeleniumTestsContextManager.getThreadContext().setWebDriverGrid(hubUrl);
			int port = GenericDriverTest.findFreePort();
			SeleniumTestsContextManager.getThreadContext().setChromeOptions("--remote-debugging-port=" + port);

			// creates the first driver
			driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", null);
			driver1.get("chrome://settings/");

			// creates the second driver
			SeleniumTestsContextManager.getThreadContext().setChromeOptions(null);
			driver2 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "second", port);
			driver2.get("about:config");

			// last created driver has the focus
			Assert.assertEquals(WebUIDriver.getWebDriver(false), driver2);

			// check second driver is attached to the first created browser
			Assert.assertEquals(driver1.getCurrentUrl(), driver2.getCurrentUrl());
		} finally {
			if (driver1 != null) {
				driver1.quit();
			}
			if (driver2 != null) {
				driver2.quit();
			}
		}
	}


	@Test(groups={"it"})
	public void testGridLaunchWithMultipleThreads() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, hubUrl);
			
			ReporterTest.executeSubTest(2, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverManualSteps", "testDriver"});
			
			String logs = ReporterTest.readSeleniumRobotLogFile();
			
			// check drivers are created in parallel
			int firstDriverCreation = logs.indexOf("driver creation took"); // written when driver is created
			int firstDriverInit = logs.indexOf("Start creating *chrome driver");
			int secondDriverCreation = logs.lastIndexOf("driver creation took");
			int secondDriverInit = logs.lastIndexOf("Start creating *chrome driver"); // written before creating driver
			
			Assert.assertTrue(secondDriverInit < firstDriverCreation);
			Assert.assertTrue(secondDriverInit > firstDriverInit);
			Assert.assertTrue(secondDriverCreation > firstDriverCreation);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	@Test(groups={"it"})
	public void testUploadMobileApp() throws IOException, UnirestException, URISyntaxException {

		File app = GenericTest.createFileFromResource("tu/env.ini");

		UiAutomator2Options caps = new UiAutomator2Options();
		caps.setApp(app.getAbsolutePath());
		caps = new UiAutomator2Options(connector.uploadMobileApp(caps));
		String url = caps.getApp().orElse(null);

		String fileContent = Unirest.get(String.format("http://%s:%d/grid/admin/FileServlet", new URI(hubUrl).toURL().getHost(), new URI(hubUrl).toURL().getPort() + 10))
				.queryString("file", url)
				.asString()
				.getBody();
		
		Assert.assertEquals(fileContent, FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	
	@Test(groups={"it"})
	public void testUploadFileToNode() throws IOException, UnirestException, URISyntaxException {
		
		File app = GenericTest.createFileFromResource("tu/env.ini");

		UiAutomator2Options caps = new UiAutomator2Options();

		caps.setApp(app.getAbsolutePath());
		String filePath = connector.uploadFileToNode(app.getAbsolutePath(), true);
		Assert.assertTrue(filePath.contains("upload/file"));
		String partialPath = "upload" + filePath.split("upload")[1] + "/" + app.getName();
		String fileContent = Unirest.get(String.format("http://%s:%d/extra/FileServlet", new URI(nodeUrl).toURL().getHost(), new URI(nodeUrl).toURL().getPort() + 10))
				.queryString("file", "file:" + partialPath)
				.asString()
				.getBody();
		
		Assert.assertEquals(fileContent, FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	

	@Test(groups={"it"})
	public void testDownloadFileFromNode() throws IOException, UnirestException {
		
		File app = GenericTest.createFileFromResource("tu/env.ini");

		UiAutomator2Options caps = new UiAutomator2Options();
		caps.setApp(app.getAbsolutePath());
		String filePath = connector.uploadFileToNode(app.getAbsolutePath(), true);
		File downloaded = connector.downloadFileFromNode("upload" + filePath.split("upload")[1] + "/" + app.getName());
		
		Assert.assertEquals(FileUtils.readFileToString(downloaded, StandardCharsets.UTF_8), FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	
	@Test(groups={"it"})
	public void testUploadFileToNode2() throws IOException, UnirestException, URISyntaxException {
		
		File app = GenericTest.createFileFromResource("tu/env.ini");

		UiAutomator2Options caps = new UiAutomator2Options();

		caps.setApp(app.getAbsolutePath());
		String filePath = connector.uploadFileToNode(app.getAbsolutePath(), false);
		Assert.assertTrue(filePath.startsWith("file:upload/file/"));
		
		String fileContent = Unirest.get(String.format("http://%s:%d/extra/FileServlet", new URI(nodeUrl).toURL().getHost(), new URI(nodeUrl).toURL().getPort() + 10))
				.queryString("file", filePath + "/" + app.getName())
				.asString()
				.getBody();
		
		Assert.assertEquals(fileContent, FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}

	@Test(groups={"it"})
	public void testGetMouseCoordinates() {
		Point coords = connector.getMouseCoordinates();
		Assert.assertNotEquals(coords, new Point(0, 0));
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	@Test(groups={"it"})
	public void testLeftClick() {
		connector.leftClic(100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	@Test(groups={"it"})
	public void testLeftClickMainScreen() {
		connector.leftClic(true, 100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testDoubleClick() {
		connector.doubleClick(100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testDoubleClickMainScreen() {
		connector.doubleClick(true, 100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testRightClick() {
		connector.rightClic(100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	
	@Test(groups={"it"})
	public void testRightClickMainScreen() {
		connector.rightClic(true, 100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testDesktopScreenshot() throws IOException {
		
		new File("d:\\tmp\\out.png").delete();
		FileUtility.writeImage("d:\\tmp\\out.png", ImageProcessor.loadFromB64String(connector.captureDesktopToBuffer()));
		
		File image = new File("d:\\tmp\\out.png");
		Assert.assertTrue(image.exists());
		Assert.assertTrue(image.length() > 100);
	}
	
	@Test(groups={"it"})
	public void testDesktopScreenshotMainScreen() throws IOException {
		
		new File("d:\\tmp\\out.png").delete();
		FileUtility.writeImage("d:\\tmp\\out.png", ImageProcessor.loadFromB64String(connector.captureDesktopToBuffer(true)));
		
		File image = new File("d:\\tmp\\out.png");
		Assert.assertTrue(image.exists());
		Assert.assertTrue(image.length() > 100);	
	}

	@Test(groups={"it"})
	public void testSendKeys() {
		connector.sendKeysWithKeyboard(List.of(KeyEvent.VK_F1));
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testdisplayRunningStep()  {
		connector.startVideoCapture();
		connector.displayRunningStep("coucou");
		WaitHelper.waitForMilliSeconds(5000);
		connector.stopVideoCapture("d:\\tmp\\out.avi");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testWriteText() {
		connector.writeText("foo");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testKillProcess() {
		connector.killProcess("foo");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testExecuteCommand() {

		String reply;
		if (OSUtility.isWindows()) {
			reply = connector.executeCommand("powershell.exe", "Write-Host \"hello\"");
		} else {
			reply = connector.executeCommand("echo", "hello");
		}

		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());

		Assert.assertEquals(reply.trim(), "hello");
	}

	@Test(groups={"it"})
	public void testGetProcessList() {
		
		List<Integer> processes = connector.getProcessList("conhost");

        Assert.assertFalse(processes.isEmpty());
	}
	
	@Test(groups={"it"})
	public void testVideoCapture() throws IOException {

		Files.deleteIfExists(Paths.get("d:\\tmp\\out.avi"));
		Files.deleteIfExists(Paths.get("d:\\tmp\\out.mp4"));
		connector.setSessionId(new SessionId("video"));
		
		connector.startVideoCapture();
		WaitHelper.waitForMilliSeconds(5000);
		File videoFile = connector.stopVideoCapture("d:\\tmp\\out.avi");
		Assert.assertNotNull(videoFile);
		
	}
}
