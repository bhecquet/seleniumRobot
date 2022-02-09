package com.seleniumtests.ut.uipage;

import java.time.Duration;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.ExpectedConditionsC;

public class TestExpectedConditionsC extends GenericTest {
	
	DriverTestPage page;
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		page = new DriverTestPage(true); // start displaying page
	}
	
	@AfterClass(groups={"ut"}, alwaysRun=true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testPresenceOfPresentElement() {
		WebElement element = new WebDriverWait(page.getDriver(), Duration.ofSeconds(2)).until(ExpectedConditionsC.presenceOfElementLocated(DriverTestPage.textElement));
		Assert.assertEquals(element.getTagName(), "input");
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void testPresenceOfNotPresentElement() {
		new WebDriverWait(page.getDriver(), Duration.ofSeconds(2)).until(ExpectedConditionsC.presenceOfElementLocated(DriverTestPage.textElementNotPresent));
	}
	
	@Test(groups={"ut"}, expectedExceptions = TimeoutException.class)
	public void testAbsenceOfPresentElement() {
		new WebDriverWait(page.getDriver(), Duration.ofSeconds(2)).until(ExpectedConditionsC.absenceOfElementLocated(DriverTestPage.textElement));
	}
	
	@Test(groups={"ut"})
	public void testAbsenceOfNotPresentElement() {
		Boolean elementAbsent = new WebDriverWait(page.getDriver(), Duration.ofSeconds(2)).until(ExpectedConditionsC.absenceOfElementLocated(DriverTestPage.textElementNotPresent));
		Assert.assertTrue(elementAbsent);

	}
	

}
