package com.seleniumtests.noreg.tests;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumTestPlan;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.noreg.support.WebServer;
import com.seleniumtests.noreg.webpage.DemoPage;
import com.seleniumtests.util.osutility.OSUtilityFactory;

import cucumber.api.java.en_au.ButattheendofthedayIreckon;

public class TestWithAllBrowsers extends SeleniumTestPlan {
	
	private static WebServer server;
	
	@BeforeClass(groups={"tnr"})
	public static void exposeTestPage() throws Exception {
        server = new WebServer("/index.html");
        server.expose();
	}

	@Test(groups={"tnr"}, dataProvider="browsers")
	public void testSearch(String browser) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser(browser);
		DemoPage demo = new DemoPage(String.format("%s/index.html", server.getServerHost().toURI())).fillForm();
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
		//browsers.add(new Object[] {"phantomjs"});
        return browsers.toArray(new Object[][]{});
    }
	
	@AfterClass(groups={"tnr"})
	public static void stop() throws Exception {
		server.stop();
	}
}
