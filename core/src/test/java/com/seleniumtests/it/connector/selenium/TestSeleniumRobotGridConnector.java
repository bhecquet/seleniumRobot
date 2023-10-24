package com.seleniumtests.it.connector.selenium;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.seleniumtests.core.SeleniumTestsContextManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageProcessor;

import io.appium.java_client.remote.MobileCapabilityType;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

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

	@Test(groups={"it"})
	public void testGridLaunchWithMultipleThreads() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, hubUrl);
			
			ReporterTest.executeSubTest(2, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverManualSteps", "testDriver"});
			
			String logs = ReporterTest.readSeleniumRobotLogFile();
			
			// check drivers are created in parallel
			int firstDriverCreation = logs.indexOf("driver creation took"); // written when driver is created
			int firstDriverInit = logs.indexOf("Socket timeout for driver communication updated");
			int secondDriverCreation = logs.lastIndexOf("driver creation took");
			int secondDriverInit = logs.lastIndexOf("Socket timeout for driver communication updated"); // written before creating driver
			
			Assert.assertTrue(secondDriverInit < firstDriverCreation);
			Assert.assertTrue(secondDriverInit > firstDriverInit);
			Assert.assertTrue(secondDriverCreation > firstDriverCreation);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	@Test(groups={"it"})
	public void testUploadMobileApp() throws ClientProtocolException, IOException, UnirestException {
		
		File app = GenericTest.createFileFromResource("clirr-differences.xml");
		
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, app.getAbsolutePath());
		connector.uploadMobileApp(caps);
		String url = (String) caps.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP);
		
		String fileContent = Unirest.get("http://localhost:4454/grid/admin/FileServlet")
				.queryString("file", url)
				.asString()
				.getBody();
		
		Assert.assertEquals(fileContent, FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	
	@Test(groups={"it"})
	public void testUploadFileToNode() throws ClientProtocolException, IOException, UnirestException {
		
		File app = GenericTest.createFileFromResource("clirr-differences.xml");
		
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, app.getAbsolutePath());
		String filePath = connector.uploadFileToNode(app.getAbsolutePath(), true);
		Assert.assertTrue(filePath.contains("upload/file"));
		
		String fileContent = Unirest.get("http://localhost:5565/extra/FileServlet")
				.queryString("file", "clirr-differences.xml")
				.asString()
				.getBody();
		
		Assert.assertEquals(fileContent, FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	

	@Test(groups={"it"})
	public void testDownloadFileFromNode() throws ClientProtocolException, IOException, UnirestException {
		
		File app = GenericTest.createFileFromResource("clirr-differences.xml");
		
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, app.getAbsolutePath());
		String filePath = connector.uploadFileToNode(app.getAbsolutePath(), true);
		File downloaded = connector.downloadFileFromNode("upload" + filePath.split("upload")[1] + "/" + app.getName());
		
		Assert.assertEquals(FileUtils.readFileToString(downloaded, StandardCharsets.UTF_8), FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	
	@Test(groups={"it"})
	public void testUploadFileToNode2() throws ClientProtocolException, IOException, UnirestException {
		
		File app = GenericTest.createFileFromResource("clirr-differences.xml");
		
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, app.getAbsolutePath());
		String filePath = connector.uploadFileToNode(app.getAbsolutePath(), false);
		Assert.assertEquals(filePath, "");
		
		String fileContent = Unirest.get("http://localhost:5565/extra/FileServlet/")
				.queryString("file", "clirr-differences.xml")
				.asString()
				.getBody();
		
		Assert.assertEquals(fileContent, FileUtils.readFileToString(app, StandardCharsets.UTF_8));
	}
	
//	@Test(groups={"it"})
//	public void testUploadFile() throws ClientProtocolException, IOException {
//		
//		File app = GenericTest.createFileFromResource("clirr-differences.xml");
//		
//		connector.uploadFile(app.getAbsolutePath());
//	}

	@Test(groups={"it"})
	public void testGetMouseCoordinates() throws ClientProtocolException, IOException {
		Point coords = connector.getMouseCoordinates();
		Assert.assertNotEquals(coords, new Point(0, 0));
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	@Test(groups={"it"})
	public void testLeftClick() throws ClientProtocolException, IOException {
		connector.leftClic(100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	@Test(groups={"it"})
	public void testLeftClickMainScreen() throws ClientProtocolException, IOException {
		connector.leftClic(true, 100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testDoubleClick() throws ClientProtocolException, IOException {
		connector.doubleClick(100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testDoubleClickMainScreen() throws ClientProtocolException, IOException {
		connector.doubleClick(true, 100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testRightClick() throws ClientProtocolException, IOException {
		connector.rightClic(100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	
	@Test(groups={"it"})
	public void testRightClickMainScreen() throws ClientProtocolException, IOException {
		connector.rightClic(true, 100, 100);
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testDesktopScreenshot() throws ClientProtocolException, IOException {
		
		new File("d:\\tmp\\out.png").delete();
		FileUtility.writeImage("d:\\tmp\\out.png", ImageProcessor.loadFromB64String(connector.captureDesktopToBuffer()));
		
		File image = new File("d:\\tmp\\out.png");
		Assert.assertTrue(image.exists());
		Assert.assertTrue(image.length() > 100);
	}
	
	@Test(groups={"it"})
	public void testDesktopScreenshotMainScreen() throws ClientProtocolException, IOException {
		
		new File("d:\\tmp\\out.png").delete();
		FileUtility.writeImage("d:\\tmp\\out.png", ImageProcessor.loadFromB64String(connector.captureDesktopToBuffer(true)));
		
		File image = new File("d:\\tmp\\out.png");
		Assert.assertTrue(image.exists());
		Assert.assertTrue(image.length() > 100);	
	}

	@Test(groups={"it"})
	public void testSendKeys() throws ClientProtocolException, IOException {
		connector.sendKeysWithKeyboard(Arrays.asList(KeyEvent.VK_F1));
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testdisplayRunningStep() throws ClientProtocolException, IOException {
		connector.startVideoCapture();
		connector.displayRunningStep("coucou");
		WaitHelper.waitForMilliSeconds(5000);
		connector.stopVideoCapture("d:\\tmp\\out.avi");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testWriteText() throws ClientProtocolException, IOException {
		connector.writeText("foo");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testKillProcess() throws ClientProtocolException, IOException {
		connector.killProcess("foo");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());
	}
	
	@Test(groups={"it"})
	public void testExecuteCommand() throws ClientProtocolException, IOException {
		String reply = connector.executeCommand("echo", "hello");
		
		// no error encountered
		verify(gridLogger, never()).warn(anyString());
		verify(gridLogger, never()).error(anyString());

		Assert.assertEquals(reply, "hello");
	}

	@Test(groups={"it"})
	public void testGetProcessList() throws ClientProtocolException, IOException {
		
		List<Integer> processes = connector.getProcessList("conhost");
		
		Assert.assertTrue(processes.size() > 0);
	}
	
	@Test(groups={"it"})
	public void testVideoCapture() throws ClientProtocolException, IOException {

		connector.setSessionId(new SessionId("video"));
		
		connector.startVideoCapture();
		WaitHelper.waitForMilliSeconds(5000);
		connector.stopVideoCapture("d:\\tmp\\out.avi");
		
	}
}
