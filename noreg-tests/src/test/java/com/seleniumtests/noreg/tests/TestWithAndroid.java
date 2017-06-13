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

public class TestWithAndroid extends SeleniumTestPlan {
	
	private static WebServer server;
	private static String localAddress;
	
	@BeforeClass(groups={"tnr"})
	public static void exposeTestPage() throws Exception {
        server = new WebServer("/index.html");
        server.expose();
        
        localAddress = System.getProperty("localAddress");
        if (localAddress == null) {
        	logger.warn("localAddress system property has not been defined, reverse to default value may cause test to fail");
        	logger.warn("Define it with '-DlocalAddress=A.B.C.D' when launching test");
        	localAddress = "10.0.2.2"; // default localhost for android emulator
        }
	}

	@Test(groups={"tnr"})
	public void testSearch() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("browser");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("android");
		DemoPage demo = new DemoPage(String.format("http://10.0.2.2:%d/index.html", server.getServerHost().getPort())).fillForm();
		Assert.assertEquals(demo.getText(), "hello");
	}
	
	@Test(groups={"tnr"})
	public void testMobileApp() throws Exception {
		
		File apk = File.createTempFile("application-", ".apk");
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("apps/android/TestRoom.Android-Signed.apk"), apk);

		SeleniumTestsContextManager.getThreadContext().setAppActivity("TestRoom");
		SeleniumTestsContextManager.getThreadContext().setAppPackage("TestRoom.Android");
		SeleniumTestsContextManager.getThreadContext().setApp(apk.getAbsolutePath());
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("android");
		DemoPage demo = new DemoPage(String.format("http://10.0.2.2:%d/index.html", server.getServerHost().getPort())).fillForm();
		Assert.assertEquals(demo.getText(), "hello");
	}
	
	@AfterClass(groups={"tnr"})
	public static void stop() throws Exception {
		server.stop();
	}
}
