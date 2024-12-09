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
import java.util.List;

import javax.imageio.ImageIO;

import com.seleniumtests.it.driver.support.pages.*;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.CaptureVideo;
import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.WebTestPageServer;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.SeleniumGridNodeNotAvailable;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

@CaptureVideo
public abstract class GenericMultiBrowserTest extends MockitoTest {
	
	protected BrowserType browserType;
	
	private WebTestPageServer server;
	private String localAddress;
	protected WebDriver driver;
	protected DriverTestPage testPage;
	protected DriverTestPageNativeActions testPageNativeActions;
	protected DriverTestPageObjectFatory testPageObjectFactory;
	protected DriverTestPageWithoutFixedPattern testPageWithoutPattern;
	protected DriverSubAngularTestPage angularPage;
	protected DriverTestAngularFrame angularFramePage;
	protected DriverScrollingTestPage scrollingTestPage;
	protected DriverTestPageSalesforceLightning lightningPage;
	protected DriverPDFPage testPdfPage;
	protected ITestContext testNGCtx;
	private SeleniumGridConnector seleniumGridConnector;
	private String testPageName;
	protected String testPageUrl;
	private boolean targetSeleniumGrid = false;
	
	protected List<BrowserType> installedBrowsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
	protected static final Logger logger = SeleniumRobotLogger.getLogger(GenericMultiBrowserTest.class);
	

	public GenericMultiBrowserTest(WebDriver driver, DriverTestPage testPage) throws Exception {
		this.driver = driver;
		this.testPage = testPage;
		this.testPageName = "DriverTestPage";
	}

	public GenericMultiBrowserTest(BrowserType browserType, String testPageName) throws Exception {
		this(browserType, testPageName, false);
	}
	
	public GenericMultiBrowserTest(BrowserType browserType, String testPageName, boolean targetSeleniumGrid) throws Exception {
		this.browserType = browserType; 
		this.testPageName = testPageName;
		this.targetSeleniumGrid = targetSeleniumGrid;
	}
	
	@BeforeMethod(groups={"ut", "it", "upload", "ie"})  
	public void initBeforeMethod() {
		if (browserType == null || !installedBrowsers.contains(browserType)) {
			return;
		}

//		SeleniumTestsContextManager.getThreadContext().setDebug("gui"); // for testing issue #294
		SeleniumTestsContextManager.getThreadContext().setDebug("driver");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
//		SeleniumTestsContextManager.getThreadContext().setBetaBrowser(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserType.getBrowserType());
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(15);
		
		// edge IE mode forced
		SeleniumTestsContextManager.getThreadContext().setEdgeIeMode(true);
//		SeleniumTestsContextManager.getThreadContext().setDebug("driver");

		// grid support
		if (targetSeleniumGrid) {
			SeleniumGridDriverFactory.setRetryTimeout(1);
			SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub");
			SeleniumTestsContextManager.getThreadContext().setRunMode("grid");

			// restore the grid connector as it's not in context for this test
			if (driver != null && !SeleniumTestsContextManager.getThreadContext().getWebDriverGrid().isEmpty()) {
				SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(seleniumGridConnector);
			}
			
			// Skip test when local grid is not present
			if (!new SeleniumRobotGridConnector("http://127.0.0.1:4444/wd/hub").isGridActive()) {
				throw new SkipException("no local seleniumrobot grid available");
			}
		}
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
		
		server = new WebTestPageServer();
		server.exposeTestPage();
		localAddress = server.getLocalAddress();
		
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
				case "DriverPDFPage":
					testPageUrl = String.format("http://%s:%d/testpdf.html", localAddress, server.getServerHost().getPort());
					testPdfPage = new DriverPDFPage(testPageUrl);
					break;
			}
		} catch (SeleniumGridNodeNotAvailable e) {
			throw new SkipException("No grid available, tests won't be run");
		} catch (Exception e) {
			logger.error("Error opening page");
			logger.error(WebUIDriver.getWebDriver(true).getPageSource());
			throw e;
		}
		driver = WebUIDriver.getWebDriver(true);
		seleniumGridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
	}
	

	@AfterClass(groups={"it", "ut", "upload", "ie"}, alwaysRun=true)
	public void stop() throws Exception {
		if (server != null) {
			server.stopServer();
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
