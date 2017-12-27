package com.seleniumtests.it.driver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.openqa.selenium.WebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;


public class TestHtmlUnitBrowserSnapshot extends MockitoTest {
	
	private static WebDriver driver;
	private DriverTestPage testPage;
	private final String browserName = "htmlunit";
	
	@BeforeMethod(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserName);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterMethod(groups={"it"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	/**
	 * Check no error is raised
	 */
	@Test(groups= {"it"})
	public void testHtmlUnitCapture() {
		ScreenshotUtil screenshotUtil = new ScreenshotUtil();
		ScreenShot screenshot = screenshotUtil.captureWebPageSnapshot();
		Assert.assertNull(screenshot.getImagePath());

	}
	
}
