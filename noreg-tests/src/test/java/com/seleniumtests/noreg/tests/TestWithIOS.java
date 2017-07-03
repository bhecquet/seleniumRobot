package com.seleniumtests.noreg.tests;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumTestPlan;
import com.seleniumtests.noreg.support.WebServer;
import com.seleniumtests.noreg.webpage.DemoPage;
import com.seleniumtests.noreg.webpage.TestRoomHome;

public class TestWithIOS extends SeleniumTestPlan {
	
	private static WebServer server;
	
	@BeforeClass(groups={"tnr"})
	public static void exposeTestPage() throws Exception {
        server = new WebServer("/index.html");
        server.expose();
	}

	@Test(groups={"tnr"})
	public void testSearch() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("safari");
		SeleniumTestsContextManager.getThreadContext().setDeviceName("iPhone SE");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("iOS");
		DemoPage demo = new DemoPage(String.format("http://localhost:%d/index.html", server.getServerHost().getPort())).fillForm();
		Assert.assertEquals(demo.getText(), "hello");
	}
	
	@Test(groups={"tnr"})
	public void testMobileApp() throws Exception {
		
		File app = File.createTempFile("application-", ".zip");
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("apps/ios/TestRoom.iOS.app.zip"), app);

		SeleniumTestsContextManager.getThreadContext().setApp(app.getAbsolutePath());
		SeleniumTestsContextManager.getThreadContext().setDeviceName("iPhone SE");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("iOS");
		TestRoomHome demo = new TestRoomHome().fillForm();
		Assert.assertEquals(demo.getText(), "hello");
	}
	
	@AfterClass(groups={"tnr"})
	public static void stop() throws Exception {
		server.stop();
	}
}
