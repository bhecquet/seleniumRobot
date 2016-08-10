package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestNewWindow {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	/**
	 * Click on link that opens a new window
	 * Then search an element into this window, close it and search an element into main window
	 */  
	@Test(groups={"it"})
	public void clickLink() {

		String mainHandle = null;
		try {
			testPage.link.click();
		
			// passage sur le nouvel onglet et recherche d'un élément
			mainHandle = testPage.selectNewWindow();
			Assert.assertEquals("a value", driver.findElement(By.id("textInIFrameWithValue")).getAttribute("value"));
		} finally {
			// retour sur l'onglet principal
			if (driver.getWindowHandles().size() > 1) {
				driver.close();
				if (mainHandle != null) {
					testPage.selectWindow(mainHandle);
				}
			}
		}
		Assert.assertTrue(testPage.link.getUrl().contains("testIFrame.html"));
	}
}
