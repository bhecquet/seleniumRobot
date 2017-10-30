package com.seleniumtests.noreg.tests;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumTestPlan;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.noreg.support.WebServer;
import com.seleniumtests.noreg.webpage.DemoPage;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class TestWithAllBrowsers extends SeleniumTestPlan {
	
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
        	localAddress = "127.0.0.1"; // default localhost for android emulator
        }
	}

	@Test(groups={"tnr"}, dataProvider="browsers")
	public void testSearch(String browser) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser(browser);
		DemoPage demo = new DemoPage(String.format("http://%s:%d/index.html", localAddress, server.getServerHost().getPort())).fillForm();
		Assert.assertEquals(demo.getText(), "hello");
	}
	
	@DataProvider
    public Object[][] browsers() {
		List<Object[]> browsers = new ArrayList<>();
		for (BrowserType browser: OSUtilityFactory.getInstance().getInstalledBrowsers()) {
			if (browser == BrowserType.NONE || browser == BrowserType.PHANTOMJS) {
				continue;
			}
			browsers.add(new Object[] {browser.getBrowserType()});
		}
//		browsers.add(new Object[] {"chrome"});
        return browsers.toArray(new Object[][]{});
    }
	
	@AfterClass(groups={"tnr"})
	public static void stop() throws Exception {
		server.stop();
	}
}
