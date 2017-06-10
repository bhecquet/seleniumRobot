package com.seleniumtests.noreg.tests;

import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
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
	
	@BeforeClass(groups={"tnr"})
	public static void exposeTestPage() throws Exception {
        server = new WebServer("/index.html");
        server.expose();
	}

	@Test(groups={"tnr"})
	public void testSearch() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("browser");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("android");
		DemoPage demo = new DemoPage(String.format("http://10.0.2.2:%d/index.html", server.getServerHost().getPort())).fillForm();
		Assert.assertEquals(demo.getText(), "hello");
	}
	
	@AfterClass(groups={"tnr"})
	public static void stop() throws Exception {
		server.stop();
	}
}
