/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.it.driver;

import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.MarionetteDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.webelements.htmlelements.HtmlElement;

public class TestDriver {
	
	

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	public TestDriver() throws Exception {
	}
	
	public TestDriver(WebDriver driver, DriverTestPage testPage) throws Exception {
		TestDriver.driver = driver;
		TestDriver.testPage = testPage;
	}
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterMethod(alwaysRun=true)
	public void cleanAlert() {
		try {
			driver.switchTo().alert().accept();
		} catch (WebDriverException e) {
			
		}
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	/**
	 * Is browser able to clic on moving elements
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMovingElement() throws Exception {
		testPage.startButton.click();
		testPage.greenSquare.click();
		driver.switchTo().alert().accept();
		
	}
	
	@Test(groups={"it"}, expectedExceptions=UnhandledAlertException.class)
	public void testFindWithAlert() {
		testPage.startButton.click();
		testPage.greenSquare.click();
		testPage.redSquare.click();
		
		if (((CustomEventFiringWebDriver)driver).getWebDriver() instanceof MarionetteDriver) {
			throw new UnhandledAlertException("fake exception as firefox / marionette does not raise any exception");
		}
	}
   
	
	/**
	 * Test native click
	 */
   
	@Test(groups={"it"})
	public void testClickDiv() {
		try {
			testPage.redSquare.click();
			Assert.assertEquals("coucou", testPage.textElement.getValue());
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
   
	@Test(groups={"it"})
	public void testClickRadio() {
		try {
			testPage.radioElement.click();
			Assert.assertTrue(testPage.radioElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testClickCheckBox() {
		try {
			testPage.checkElement.click();
			Assert.assertTrue(testPage.checkElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
  
	
	/**
	 * Test javascript actions
	 */
	@Test(groups={"it"})
	public void testClickJsDiv() {
		try {
			testPage.redSquare.simulateClick();
			Assert.assertEquals("coucou", testPage.textElement.getValue());
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
   
	@Test(groups={"it"})
	public void testClickJsRadio() {
		try {
			testPage.radioElement.simulateClick();
			Assert.assertTrue(testPage.radioElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testClickJsCheckbox() {
		try {
			testPage.checkElement.simulateClick();
			Assert.assertTrue(testPage.checkElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testSendKeys() {
		try {
			testPage.textElement.sendKeys("youpi");
			Assert.assertEquals(testPage.textElement.getValue(), "youpi");
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testSendKeysJs() {
		try {
			testPage.textElement.simulateSendKeys("youpi");
			Assert.assertEquals(testPage.textElement.getValue(), "youpi");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
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
			Assert.assertEquals("input", driver.findElement(By.name("q")).getTagName());
		} finally {
			// retour sur l'onglet principal
			if (driver.getWindowHandles().size() > 1) {
				driver.close();
				if (mainHandle != null) {
					testPage.selectWindow(mainHandle);
				}
			}
		}
		Assert.assertEquals(testPage.link.getUrl(), "http://www.google.fr/");
	}
	
	/**
	 * Changing data in an input field should throw onBlur event
	 */  
	@Test(groups={"it"})
	public void testOnBlur() {
		testPage.onBlurField.sendKeys("onBlur done");
		Assert.assertEquals(testPage.onBlurFieldDest.getValue(), "onBlur done");
	}
	
	@Test(groups={"it"})
	public void testFindElements() {
		// 2 éléments à trouver
		Assert.assertEquals(new HtmlElement("", By.name("divFindName")).findElements().size(), 2);
		
		// 3 éléments dont l'un dans une branche
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findElements().size(), 4);
	}

	/**
	 * Search an element inside an other one
	 */
	@Test(groups={"it"})
	public void testFindSubElement() {
		Assert.assertEquals(testPage.parent.findElement(By.className("myClass")).getText(), "first child");
	}
	
	/**
	 * Search the n th element inside an other one
	 */
	@Test(groups={"it"})
	public void testFindNthSubElement() {
		Assert.assertEquals(testPage.parent.findElement(By.className("myClass"), 1).getText(), "fourth child");
		Assert.assertEquals(testPage.child.getText(), "fourth child");
	}
	
	/**
	 * Search the n th element corresponding to locator
	 */
	@Test(groups={"it"})
	public void testFindNthElement() {
		Assert.assertEquals(testPage.divFindName.getText(), "an other text");
	}

	/**
	 * test specific HtmlElements actions
	 */
	@Test(groups={"it"})
	public void testFindPattern1() {
		Assert.assertTrue(testPage.link.findLink("href").startsWith("http://www.google.fr"));
	}
	
	@Test(groups={"it"})
	public void testFindPattern2() {
		Assert.assertTrue(testPage.linkPopup.findLink("onclick").startsWith("http://www.google.fr"));
	}
	
	@Test(groups={"it"}) 
	public void testFindPattern3() {
		Assert.assertTrue(testPage.linkPopup2.findLink("onclick").startsWith("http://www.google.fr"));
	}
	
	/**
	 * text search
	 */
	@Test(groups={"it"})
	public void testFindPattern4() {
		Assert.assertEquals(new HtmlElement("", By.id("divFind2")).findPattern(Pattern.compile("an (\\w+) text"), "text"), "other");
	}
	
	/**
	 * Check we wait enough for element to be displayed
	 */
	@Test(groups={"it"}) 
	public void testDelay() {
		try {
			testPage.delayButton.click();
			Assert.assertEquals(new HtmlElement("", By.id("newEl")).getValue(), "my value");
		} finally {
			testPage.delayButtonReset.click();
		}
		
	}
	
	/**
	 * Test that it's possible to use an hidden element. Make it appear before using it
	 */
	@Test(groups={"it"})
	public void testHiddenElement() { 
		testPage.hiddenCheckBox.click();
		Assert.assertTrue(testPage.hiddenCheckBox.isSelected());
		Assert.assertTrue(testPage.hiddenCheckBox.isDisplayed());
	}
	
	@Test(groups={"it"})
	public void testIsElementPresent1() {
		try {
			testPage.delayButton.click();
			Assert.assertFalse(new HtmlElement("", By.id("newEl")).isElementPresent());
			WaitHelper.waitForSeconds(3);
			Assert.assertTrue(new HtmlElement("", By.id("newEl")).isElementPresent());
		} finally {
			testPage.delayButtonReset.click();
		}
	}

//	@Test(groups={"it"})
//	public void testFindImageElement() {
//		
//		try {
//			driver.executeScript("window.scrollTo(0, 0);");
//			driver.findImageElement(new File(Thread.currentThread().getContextClassLoader().getResource("googleSearch.png").getFile())).click();
//			Assert.assertEquals("image", driver.findElement(By.id("text2")).getAttribute("value"));
//		} finally {
//			driver.findElement(By.id("button2")).click();
//		}
//	}
	
	/**
	 * Vérifie qu'avant d'agir sur un élément, on positionne la fenêtre du navigateur pour qu'il soit visible
	 */
	@Test(groups={"it"})
	public void testAutoScrolling() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
		new HtmlElement("", By.id("buttonScroll")).click();
		Assert.assertFalse(((JavascriptExecutor) driver).executeScript("return window.pageYOffset;").equals(0L));
	}
	
	
}
