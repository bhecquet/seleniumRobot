/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestAngularFrame;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.it.driver.support.server.WebServer;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public abstract class GenericMultiBrowserTest {
	
	protected BrowserType browserType;
	protected WebServer server;
	protected String localAddress;
	protected WebDriver driver;
	protected DriverTestPage testPage;
	protected DriverTestPageWithoutFixedPattern testPageWithoutPattern;
	protected DriverSubAngularTestPage angularPage;
	protected DriverTestAngularFrame angularFramePage;
	private String testPageName;
	
	protected List<BrowserType> installedBrowsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
	protected static final Logger logger = SeleniumRobotLogger.getLogger(GenericMultiBrowserTest.class);
	

	public GenericMultiBrowserTest(WebDriver driver, DriverTestPage testPage) throws Exception {
		this.driver = driver;
		this.testPage = testPage;
		this.testPageName = "DriverTestPage";
	}

	public GenericMultiBrowserTest(BrowserType browserType, String testPageName) throws Exception {
		this.browserType = browserType; 
		this.testPageName = testPageName;
	}
	
	/**
	 * Method for returning mapping of files stored in resources, with path on server
	 * @return
	 */
	protected Map<String, String> getPageMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/tu/test.html", "/test.html");
		mapping.put("/tu/testWithoutFixedPattern.html", "/testWithoutFixedPattern.html");
		mapping.put("/tu/testIFrame.html", "/testIFrame.html");
		mapping.put("/tu/testAngularIFrame.html", "/testAngularIFrame.html");
		mapping.put("/tu/testIFrame2.html", "/testIFrame2.html");
		mapping.put("/tu/ffLogo1.png", "/ffLogo1.png");
		mapping.put("/tu/ffLogo2.png", "/ffLogo2.png");
		mapping.put("/tu/googleSearch.png", "/googleSearch.png");
		mapping.put("/tu/jquery.min.js", "/jquery.min.js");
		
		// angular app
		mapping.put("/tu/angularApp/index.html", "/angularApp/index.html");
		mapping.put("/tu/angularApp/inline.bundle.js", "/angularApp/inline.bundle.js");
		mapping.put("/tu/angularApp/main.bundle.js", "/angularApp/main.bundle.js");
		mapping.put("/tu/angularApp/polyfills.bundle.js", "/angularApp/polyfills.bundle.js");
		mapping.put("/tu/angularApp/styles.bundle.css", "/angularApp/styles.bundle.css");
		
		return mapping;
	}
	

	public void initThreadContext(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, null, null, null);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
	}

	@BeforeClass(groups={"it", "ut"})
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
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// grid support
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		
		switch (testPageName) {
		case "DriverTestPageWithoutFixedPattern":
			testPageWithoutPattern = new DriverTestPageWithoutFixedPattern(true, String.format("http://%s:%d/testWithoutFixedPattern.html", localAddress, server.getServerHost().getPort()));
			break;
		case "DriverTestPage":
			testPage = new DriverTestPage(true, String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort()));
			break;
		case "DriverTestAngularFrame":
			angularFramePage = new DriverTestAngularFrame(true, String.format("http://%s:%d/testAngularIFrame.html", localAddress, server.getServerHost().getPort()));
			break;
		case "DriverSubAngularTestPage":
			angularPage = new DriverSubAngularTestPage(true, String.format("http://%s:%d/angularApp/index.html", localAddress, server.getServerHost().getPort()));
		}
		
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterClass(groups={"it", "ut"})
	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
		if (driver != null) {
			WebUIDriver.cleanUp();
			WebUIDriver.cleanUpWebUIDriver();
		}
		driver = null;
	}
	
	@BeforeMethod(groups={"it", "ut"}) 
	public void skipIfDriverNull() {
		if (driver == null) {
			throw new SkipException("skipped, browser not installed: " + browserType);
		}
	}
	

	@AfterMethod(groups={"it", "ut"})
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
