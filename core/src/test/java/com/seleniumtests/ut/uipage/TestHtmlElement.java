/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.ut.uipage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;
import com.seleniumtests.it.driver.TestDriver;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class TestHtmlElement extends GenericTest {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	private static TestDriver testDriverIt;
	
	@BeforeClass(groups={"ut"})
	public static void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		testDriverIt = new TestDriver(driver, testPage);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
//	@Test(groups={"ut"})
//	public void testClickDiv() {
//		testDriverIt.testClickDiv();
//	}
//	
//	
//	
//	@Test(groups={"ut"})
//	public void testClickRadio() {
//		testDriverIt.testClickRadio();
//	}
//   
//	@Test(groups={"ut"})
//	public void testClickCheckBox() {
//		testDriverIt.testClickCheckBox();
//	}
//	
//	/**
//	 * Test javascript actions
//	 */
//	@Test(groups={"ut"})
//	public void testClickJsDiv() {
//		testDriverIt.testClickJsDiv();
//	}
//   
//	@Test(groups={"ut"})
//	public void testClickJsRadio() {
//		testDriverIt.testClickJsRadio();
//	}
//   
//	@Test(groups={"ut"})
//	public void testClickJsCheckbox() {
//		testDriverIt.testClickJsCheckbox();
//	}
//   
//	@Test(groups={"ut"})
//	public void testSendKeys() {
//		testDriverIt.testSendKeys();
//	}
//   
//	@Test(groups={"ut"})
//	public void testSendKeysJs() {
//		testDriverIt.testSendKeysJs();
//	}
//	
//	@Test(groups={"ut"})
//	public void testOnBlur() {
//		testDriverIt.testOnBlur();
//	}
//	
//	@Test(groups={"ut"})
//	public void testFindElements() {
//		testDriverIt.testFindElements();
//	}
//
//	/**
//	 * Search an element inside an other one
//	 */
//	@Test(groups={"ut"})
//	public void testFindSubElement() {
//		testDriverIt.testFindSubElement();
//	}
//	
//	/**
//	 * Search the n th element inside an other one
//	 */
//	@Test(groups={"ut"})
//	public void testFindNthSubElement() {
//		testDriverIt.testFindNthSubElement();
//	}
//	
//	/**
//	 * Search the n th element corresponding to locator
//	 */
//	@Test(groups={"ut"})
//	public void testFindNthElement() {
//		testDriverIt.testFindNthElement();
//	}
//
//	/**
//	 * test specific HtmlElements actions
//	 */
//	@Test(groups={"ut"})
//	public void testFindPattern1() {
//		testDriverIt.testFindPattern1();
//	}
//	
//	@Test(groups={"ut"})
//	public void testFindPattern2() {
//		testDriverIt.testFindPattern2();
//	}
//	
//	@Test(groups={"ut"}) 
//	public void testFindPattern3() {
//		testDriverIt.testFindPattern3();
//	}
//	
//	/**
//	 * text search
//	 */
//	@Test(groups={"ut"})
//	public void testFindPattern4() {
//		testDriverIt.testFindPattern4();
//	}
//	
//	@Test(groups={"ut"})
//	public void testIsElementPresent() {
//		Assert.assertTrue(testPage.textElement.isElementPresent(2));
//	}
//	
//	@Test(groups={"ut"})
//	public void testIsElementNotPresent() {
//		Assert.assertFalse(new HtmlElement("", By.id("divNotFound")).isElementPresent(2));
//	}
//	
	@Test(groups={"ut"})
	public void testFindTextElementInsideHtmlElement() {
		Assert.assertEquals(testPage.textElement2.getValue(), "default");
	}
	
	@Test(groups={"ut"})
	public void testFindRadioElementInsideHtmlElement() {
		try {
			testPage.radioElement2.click();
			Assert.assertTrue(new HtmlElement("", By.id("radioClickParent")).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testFindCheckElementInsideHtmlElement() {
		try {
			testPage.checkElement2.click();
			Assert.assertTrue(new HtmlElement("", By.id("checkboxClickParent")).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testFindButtonElementInsideHtmlElement() {
		Assert.assertEquals(testPage.resetButton2.getText(), "reset button");
	}
	
	@Test(groups={"ut"})
	public void testFindLinkElementInsideHtmlElement() {
		Assert.assertTrue(testPage.linkElement2.getUrl().contains("http://www.googleFrance.fr"));
	}
	
	@Test(groups={"ut"})
	public void testFindSelectElementInsideHtmlElement() {
		Assert.assertEquals(testPage.selectList2.getOptions().size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testFindTableInsideHtmlElement() {
		Assert.assertEquals(testPage.table2.getRowCount(), 2);
	}
	
	/*
	 * Use elements searched by index inside other elements 
	 */
	
	@Test(groups={"ut"})
	public void testFindTextElementsInsideHtmlElement() {
		Assert.assertEquals(testPage.textElement3.getValue(), "default");
	}
	
	@Test(groups={"ut"})
	public void testFindRadioElementsInsideHtmlElement() {
		try {
			testPage.radioElement3.click();
			Assert.assertTrue(new HtmlElement("", By.id("radioClickParent")).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testFindCheckElementsInsideHtmlElement() {
		try {
			testPage.checkElement3.click();
			Assert.assertTrue(new HtmlElement("", By.id("checkboxClickParent")).isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"ut"})
	public void testFindButtonElementsInsideHtmlElement() {
		Assert.assertEquals(testPage.resetButton3.getText(), "reset button");
	}
	
	@Test(groups={"ut"})
	public void testFindLinkElementsInsideHtmlElement() {
		Assert.assertTrue(testPage.linkElement3.getUrl().contains("http://www.googleFrance.fr"));
	}
	
	@Test(groups={"ut"})
	public void testFindSelectElementsInsideHtmlElement() {
		Assert.assertEquals(testPage.selectList3.getOptions().size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testFindTablesInsideHtmlElement() {
		Assert.assertEquals(testPage.table3.getRowCount(), 2);
	}
	
}
