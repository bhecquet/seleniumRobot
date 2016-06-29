package com.seleniumtests.ut.webelements;

import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;
import com.seleniumtests.it.driver.TestDriver;
import com.seleniumtests.webelements.htmlelements.HtmlElement;
import com.seleniumtests.webelements.htmlelements.LabelElement;

public class TestHtmlElement {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	private static TestDriver testDriverIt;
	
	@BeforeClass(groups={"ut"})
	public static void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		testDriverIt = new TestDriver(driver, testPage);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testClickDiv() {
		testDriverIt.testClickDiv();
	}
	
	
	
	@Test(groups={"ut"})
	public void testClickRadio() {
		testDriverIt.testClickRadio();
	}
   
	@Test(groups={"ut"})
	public void testClickCheckBox() {
		testDriverIt.testClickCheckBox();
	}
	
	/**
	 * Test javascript actions
	 */
	@Test(groups={"ut"})
	public void testClickJsDiv() {
		testDriverIt.testClickJsDiv();
	}
   
	@Test(groups={"ut"})
	public void testClickJsRadio() {
		testDriverIt.testClickJsRadio();
	}
   
	@Test(groups={"ut"})
	public void testClickJsCheckbox() {
		testDriverIt.testClickJsCheckbox();
	}
   
	@Test(groups={"ut"})
	public void testSendKeys() {
		testDriverIt.testSendKeys();
	}
   
	@Test(groups={"ut"})
	public void testSendKeysJs() {
		testDriverIt.testSendKeysJs();
	}
	
	@Test(groups={"ut"})
	public void testOnBlur() {
		testDriverIt.testOnBlur();
	}
	
	@Test(groups={"ut"})
	public void testFindElements() {
		testDriverIt.testFindElements();
	}

	/**
	 * Search an element inside an other one
	 */
	@Test(groups={"ut"})
	public void testFindSubElement() {
		testDriverIt.testFindSubElement();
	}
	
	/**
	 * Search the n th element inside an other one
	 */
	@Test(groups={"ut"})
	public void testFindNthSubElement() {
		testDriverIt.testFindNthSubElement();
	}
	
	/**
	 * Search the n th element corresponding to locator
	 */
	@Test(groups={"ut"})
	public void testFindNthElement() {
		testDriverIt.testFindNthElement();
	}

	/**
	 * test specific HtmlElements actions
	 */
	@Test(groups={"ut"})
	public void testFindPattern1() {
		testDriverIt.testFindPattern1();
	}
	
	@Test(groups={"ut"})
	public void testFindPattern2() {
		testDriverIt.testFindPattern2();
	}
	
	@Test(groups={"ut"}) 
	public void testFindPattern3() {
		testDriverIt.testFindPattern3();
	}
	
	/**
	 * text search
	 */
	@Test(groups={"ut"})
	public void testFindPattern4() {
		testDriverIt.testFindPattern4();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresent() {
		Assert.assertTrue(testPage.textElement.isElementPresent(2));
	}
	
	@Test(groups={"ut"})
	public void testIsElementNotPresent() {
		Assert.assertFalse(new HtmlElement("", By.id("divNotFound")).isElementPresent(2));
	}
	
}
