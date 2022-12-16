/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverScrollingTestPage;
import com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestAngularFrame;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions;
import com.seleniumtests.it.driver.support.pages.DriverTestPageObjectFatory;
import com.seleniumtests.it.driver.support.pages.DriverTestPageSalesforceLightning;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.it.driver.support.server.WebServer;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public abstract class GenericMultiBrowserTest extends MockitoTest {
	
	protected BrowserType browserType;
	protected WebServer server;
	protected String localAddress;
	protected WebDriver driver;
	protected DriverTestPage testPage;
	protected DriverTestPageNativeActions testPageNativeActions;
	protected DriverTestPageObjectFatory testPageObjectFactory;
	protected DriverTestPageWithoutFixedPattern testPageWithoutPattern;
	protected DriverSubAngularTestPage angularPage;
	protected DriverTestAngularFrame angularFramePage;
	protected DriverScrollingTestPage scrollingTestPage;
	protected DriverTestPageSalesforceLightning lightningPage;
	protected ITestContext testNGCtx;
	private String testPageName;
	protected String testPageUrl;
	
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
		mapping.put("/tu/testScrolling.html", "/testScrolling.html");
		mapping.put("/tu/testWithoutFixedPattern.html", "/testWithoutFixedPattern.html");
		mapping.put("/tu/testIFrame.html", "/testIFrame.html");
		mapping.put("/tu/testAngularIFrame.html", "/testAngularIFrame.html");
		mapping.put("/tu/testIFrame2.html", "/testIFrame2.html");
		mapping.put("/tu/testIFrame3.html", "/testIFrame3.html");
		mapping.put("/tu/ffLogo1.png", "/ffLogo1.png");
		mapping.put("/tu/ffLogo2.png", "/ffLogo2.png");
		mapping.put("/tu/googleSearch.png", "/googleSearch.png");
		mapping.put("/tu/images/bouton_enregistrer.png", "/images/bouton_enregistrer.png");
		mapping.put("/tu/jquery.min.js", "/jquery.min.js");
		
		// Site remasterisé
		mapping.put("/tu/pagesApp/test.html", "/testIA.html");
		mapping.put("/tu/pagesApp/testCharging.html", "/testCharging.html");
		mapping.put("/tu/pagesApp/testMissingElement.html", "/testMissingElement.html");

		
//		// angular app
//		mapping.put("/tu/angularApp/index.html", "/angularApp/index.html");
//		mapping.put("/tu/angularApp/inline.bundle.js", "/angularApp/inline.bundle.js");
//		mapping.put("/tu/angularApp/main.bundle.js", "/angularApp/main.bundle.js");
//		mapping.put("/tu/angularApp/polyfills.bundle.js", "/angularApp/polyfills.bundle.js");
//		mapping.put("/tu/angularApp/styles.bundle.css", "/angularApp/styles.bundle.css");
//		
		// angular app v9
		mapping.put("/tu/angularAppv9/index.html", "/angularApp/index.html");
		mapping.put("/tu/angularAppv9/runtime-es2015.js", "/angularApp/runtime-es2015.js");
		mapping.put("/tu/angularAppv9/runtime-es5.js", "/angularApp/runtime-es5.js");
		mapping.put("/tu/angularAppv9/main-es5.js", "/angularApp/main-es5.js");
		mapping.put("/tu/angularAppv9/main-es2015.js", "/angularApp/main-es2015.js");
		mapping.put("/tu/angularAppv9/polyfills-es2015.js", "/angularApp/polyfills-es2015.js");
		mapping.put("/tu/angularAppv9/polyfills-es5.js", "/angularApp/polyfills-es5.js");
		mapping.put("/tu/angularAppv9/styles.css", "/angularApp/styles.css");
		
		return mapping;
	}
	
	@BeforeMethod(groups={"ut", "it", "upload", "ie"})  
	public void initBeforeMethod() {
		if (browserType == null || !installedBrowsers.contains(browserType)) {
			return;
		}

//		SeleniumTestsContextManager.getThreadContext().setDebug("gui"); // for testing issue #294
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserType.getBrowserType());
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(15);

		// grid support
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
	}
	
	
	@BeforeClass(groups={"it", "ut", "upload", "ie"})
	public void exposeTestPage(final ITestContext testNGCtx) throws Exception {

        // skip following if driver is already defined from an other test
        if (driver != null) {
			return;
		}
		if (browserType == null || !installedBrowsers.contains(browserType)) {
			return;
		}
		
		localAddress = Inet4Address.getLocalHost().getHostAddress();
//		localAddress = Inet4Address.getByName("localhost").getHostAddress();
        server = new WebServer(localAddress, getPageMapping());
        server.expose();
        logger.info(String.format("exposing server on http://%s:%d", localAddress, server.getServerHost().getPort()));

		initThreadContext(testNGCtx);
		initBeforeMethod();
		
		try {
			switch (testPageName) {
				case "DriverTestPageWithoutFixedPattern":
					testPageUrl = String.format("http://%s:%d/testWithoutFixedPattern.html", localAddress, server.getServerHost().getPort());
					testPageWithoutPattern = new DriverTestPageWithoutFixedPattern(true, testPageUrl);
					break;
				case "DriverTestPage":
					testPageUrl = String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort());
					testPage = new DriverTestPage(true, testPageUrl);
					break;
				case "DriverTestPageNativeActions":
					testPageUrl = String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort());
					testPageNativeActions = new DriverTestPageNativeActions(true, testPageUrl);
					break;
				case "DriverTestPageObjectFactory":
					testPageUrl = String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort());
					testPageObjectFactory = new DriverTestPageObjectFatory(true, testPageUrl);
					break;
				case "DriverTestAngularFrame":
					testPageUrl = String.format("http://%s:%d/testAngularIFrame.html", localAddress, server.getServerHost().getPort());
					angularFramePage = new DriverTestAngularFrame(true, testPageUrl);
					break;
				case "DriverSubAngularTestPage":
					testPageUrl = String.format("http://%s:%d/angularApp/index.html", localAddress, server.getServerHost().getPort());
					angularPage = new DriverSubAngularTestPage(true, testPageUrl);
					break;
				case "DriverScrollingTestPage":
					testPageUrl = String.format("http://%s:%d/testScrolling.html", localAddress, server.getServerHost().getPort());
					scrollingTestPage = new DriverScrollingTestPage(true, testPageUrl);
					break;
				case "DriverTestPageSalesforceLightning":
					lightningPage = new DriverTestPageSalesforceLightning();
					break;
			}
		} catch (Exception e) {
			logger.error("Error opening page");
			logger.error(WebUIDriver.getWebDriver(true).getPageSource());
			throw e;
		}
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterClass(groups={"it", "ut", "upload", "ie"}, alwaysRun=true)
	public void stop() throws Exception {
		if (server != null) {
			logger.info("stopping web server");
			server.stop();
		}
		if (WebUIDriver.getWebDriver(false) != null) {
			logger.info("closing driver after all tests");
			WebUIDriver.cleanUp();
		}
		driver = null;
	}
	
	@BeforeMethod(groups={"it", "ut", "upload", "ie"}) 
	public void skipIfDriverNull() {
		if (driver == null) {
			throw new SkipException("skipped, browser not installed: " + browserType);
		}
	}
	

	@AfterMethod(groups={"it", "ut", "upload", "ie"})
	public void cleanAlert() {
		GenericTest.resetTestNGREsultAndLogger();
		if (driver == null) {
			return;
		}
		try {
			driver.switchTo().alert().accept();
		} catch (WebDriverException e) {
			
		}

	}

	/**
	 * Takes a screenshot and write it to logs as base64 string
	 * @throws AWTException
	 * @throws IOException
	 */
	//@AfterMethod(groups={"it", "ut", "upload", "ie"}, alwaysRun = true)
	protected void takeScreenshot(ITestResult testResult) throws AWTException, IOException {
		
		if (testResult.getStatus() == ITestResult.FAILURE) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			
		    Rectangle screenRect = new Rectangle(0, 0, 0, 0);
		    for (GraphicsDevice gd : ge.getScreenDevices()) {
		      screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
		    }
		    
		    BufferedImage bi = new Robot().createScreenCapture(screenRect);
		    
		    ByteArrayOutputStream os = new ByteArrayOutputStream();
		    OutputStream b64 = new Base64OutputStream(os);
		    
		    ImageIO.write(bi, "png", b64);
		    logger.error(os.toString("UTF-8"));
		}
	}
	
}
