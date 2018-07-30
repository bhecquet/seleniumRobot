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
package com.seleniumtests.it.driver;

import java.awt.AWTException;
import java.io.File;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.util.helper.WaitHelper;

public class TestDriver extends GenericMultiBrowserTest {

	public TestDriver(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestDriver(BrowserType browserType) throws Exception {
		super(browserType, "DriverTestPage"); 
	}
	
	public TestDriver() throws Exception {
		super(null, "DriverTestPage");
	}
	
	
//	/**
//	 * Is browser able to clic on moving elements
//	 * @throws Exception
//	 */
//	@Test(groups={"it"})
//	public void testMovingElement() throws Exception {
//		testPage.startButton.click();
//		testPage.greenSquare.click();
//		driver.switchTo().alert().accept();
//		
//	}
	
	/**
	 * Check for issue #47 where ReplayAction aspect raised an error when switching to default context after click with alert present
	 */
	@Test(groups={"it"})
	public void testAlertDisplay() {
		try {
			testPage.greenSquare.click();
			driver.switchTo().alert().accept();
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
	
	/**
	 * deactivated as it depends on browser
	 */
	@Test(groups={"it"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		testPage.startButton.click();
		testPage.greenSquare.click();
		testPage.redSquare.click();
		
		if (((CustomEventFiringWebDriver)driver).getWebDriver() instanceof FirefoxDriver && FirefoxDriverFactory.isMarionetteMode()) {
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
	
	/**
	 * Test composite actions
	 */
	@Test(groups={"it"})
	public void testClickActionDiv() {
		try {
			testPage.redSquare.clickAction();
			Assert.assertEquals(testPage.textElement.getValue(), "coucou");
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
	
	@Test(groups={"it"})
	public void testDoubleClickActionDiv() {
		try {
			testPage.redSquare.doubleClickAction();
			Assert.assertEquals(testPage.textElement.getValue(), "double coucou");
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
	
	@Test(groups={"it"})
	public void testClickActionRadio() {
		try {
			testPage.radioElement.clickAction();
			Assert.assertTrue(testPage.radioElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
	
	@Test(groups={"it"})
	public void testClickActionCheckbox() {
		try {
			testPage.checkElement.clickAction();
			Assert.assertTrue(testPage.checkElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testSendKeys() {
		try {
			testPage.textElement.sendKeys("youpi@[]é");
			Assert.assertEquals(testPage.textElement.getValue(), "youpi@[]é");
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testSendKeysJs() {
		try {
			testPage.textElement.simulateSendKeys("youpi@[]é");
			Assert.assertEquals(testPage.textElement.getValue(), "youpi@[]é");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
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
		Assert.assertTrue(testPage.link2.findLink("href").startsWith("http://www.google.fr"));
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
			Assert.assertFalse(new HtmlElement("", By.id("newEl")).isElementPresent(1));
			WaitHelper.waitForSeconds(3);
			Assert.assertTrue(new HtmlElement("", By.id("newEl")).isElementPresent(1));
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
	
	/**
	 * Test file upload in case selenium method does not work. This uses the java Robot version which controls mouse and keyboard
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	@Test(groups= {"it"})
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "config.ini";
//		testPage.upload.click();
		testPage.upload.clickAction(); // due to restrictions clicking a <input type="file"> element with firefox, use clickAction instead
		testPage.uploadFile(path);
		
		Assert.assertEquals(testPage.uploadedFile.getAttribute("value"), "config.ini");
	}
	
	/**
	 * Force driver to use full keyboard typing when writing text instead of using copy-paste feature
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	@Test(groups= {"it"})
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "config.ini";
//		testPage.upload.click();
		testPage.upload.clickAction(); // due to restrictions clicking a <input type="file"> element with firefox, use clickAction instead
		((CustomEventFiringWebDriver)driver).uploadFileUsingKeyboardTyping(new File(path));
		
		Assert.assertEquals(testPage.uploadedFile.getAttribute("value"), "config.ini");
	}
	
	/**
	 * Test file upload with standard selenium method
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	@Test(groups= {"it"})
	public void testUploadFile() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "config.ini";
		testPage.upload.sendKeys(path);
		
		Assert.assertEquals(testPage.uploadedFile.getAttribute("value"), "config.ini");
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	@Test(groups={"it"})
	public void testFindFirstElement() {
		Assert.assertEquals(testPage.multiElementFirstText.getValue(), "0 text field");
	}
	
	/**
	 * Check that if FIRST_VISIBLE is specified, first visible element is returned
	 * refresh page to be sure element has not been made visible
	 */
	@Test(groups={"it"})
	public void testFindFirstVisibleElement() {
		driver.navigate().refresh();
		Assert.assertEquals(testPage.multiElementFirstVisibleText.getValue(), "second text field");
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	@Test(groups={"it"})
	public void testFindFirstElementWithParent() {
		Assert.assertEquals(testPage.multiElementFirstTextWithParent.getValue(), "0 text field");
	}
	
	/**
	 * Check that if FIRST_VISIBLE is specified, first visible element is returned. In this case, FIRST_VISIBLE has only been applied to the parent element
	 * refresh page to be sure element has not been made visible
	 */
	@Test(groups={"it"})
	public void testFindFirstVisibleElementWithParent() {
		driver.navigate().refresh();
		Assert.assertEquals(testPage.multiElementFirstVisibleTextWithParent.getValue(), "first text field");
	}
	
	/**
	 * get findElements inside an other one using findElements(By) method
	 */
	@Test(groups={"it"})
	public void testFindElementsUnderAnOtherElement() {
		Assert.assertEquals(testPage.divByClass.findElements(By.className("someClass")).size(), 4);
	}
	
	/**
	 * get findElements inside an other one using findElements() method
	 */
	@Test(groups={"it"})
	public void testFindElementsInsideParent() {
		Assert.assertEquals(testPage.parent.findElement(By.className("myClass")).findElements().size(), 2);
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	@Test(groups={"it"})
	public void testFindLastElement() {
		Assert.assertEquals(testPage.multiElementLastText.getValue(), "last text field");
	}
	
	/**
	 * issue #166: Check that when searching an element by XPath, and this element is specified as being located in an other element, 
	 * we still get the element with an xpath relative to the parent element
	 */
	@Test(groups={"it"})
	public void testFindSubElementByXpath() {
		Assert.assertEquals(testPage.optionByXpath.getText(), "option1Parent");
	}
	
	/**
	 * Check that when searching an element by XPath, and this element is specified as being located in an other element, 
	 * we still get the element with an xpath relative to the parent element. In this case, the xpath is already specified as relative. No modification should
	 * be done by robot
	 */
	@Test(groups={"it"})
	public void testFindSubElementByRelativeXpath() {
		Assert.assertEquals(testPage.optionByRelativeXpath.getText(), "option1Parent");
	}
	
	/**
	 * Check search by XPath without parent is correctly performed (no change due to correction of issue #166)
	 */
	@Test(groups={"it"})
	public void testFindElementByXpath() {
		Assert.assertEquals(testPage.searchByXpath.getText(), "option1");
	}
	
}
