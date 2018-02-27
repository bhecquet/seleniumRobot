package com.seleniumtests.it.driver.support;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.server.WebServer;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public abstract class GenericMultiBrowserTest {
	
	protected BrowserType browserType;
	protected WebServer server;
	protected String localAddress;
	protected WebDriver driver;
	protected DriverTestPage testPage;
	protected List<BrowserType> installedBrowsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
	protected static final Logger logger = SeleniumRobotLogger.getLogger(GenericMultiBrowserTest.class);
	

	public GenericMultiBrowserTest(WebDriver driver, DriverTestPage testPage) throws Exception {
		this.driver = driver;
		this.testPage = testPage;
	}

	public GenericMultiBrowserTest(BrowserType browserType) throws Exception {
		this.browserType = browserType; 
	}
	
	/**
	 * Method for returning mapping of files stored in resources, with path on server
	 * @return
	 */
	protected Map<String, String> getPageMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/tu/test.html", "/test.html");
		mapping.put("/tu/testIFrame.html", "/testIFrame.html");
		mapping.put("/tu/testIFrame2.html", "/testIFrame2.html");
		mapping.put("/tu/ffLogo1.png", "/ffLogo1.png");
		mapping.put("/tu/ffLogo2.png", "/ffLogo2.png");
		mapping.put("/tu/googleSearch.png", "/googleSearch.png");
		mapping.put("/tu/jquery.min.js", "/jquery.min.js");
		
		return mapping;
	}
	

	public void initThreadContext(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, null);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
	}

	@BeforeClass(groups={"it"})
	public void exposeTestPage(final ITestContext testNGCtx) throws Exception {

        // skip following if driver is already defined from an other test
        if (driver != null) {
			return;
		}
		if (browserType == null || !installedBrowsers.contains(browserType)) {
			return;
		}
		
		localAddress = Inet4Address.getLocalHost().getHostAddress();
        server = new WebServer(localAddress, getPageMapping());
        server.expose();
        logger.info(String.format("exposing server on http://%s:%d", localAddress, server.getServerHost().getPort()));

		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserType.getBrowserType());
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		
		// grid support
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		
		testPage = new DriverTestPage(true, String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort()));
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterClass(groups={"it"})
	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
		driver = null;
	}
	
	@BeforeMethod(groups={"it"}) 
	public void skipIfDriverNull() {
		if (driver == null) {
			throw new SkipException("skipped, browser not installed: " + browserType);
		}
	}
	

	@AfterMethod(groups={"it"})
	public void cleanAlert() {
		if (driver == null) {
			return;
		}
		try {
			driver.switchTo().alert().accept();
		} catch (WebDriverException e) {
			
		}
	}
}
