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
package com.seleniumtests.it.driver;

import java.awt.AWTException;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
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
	

	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void reset() {
		if (driver != null) {
			DriverTestPage.logoText.clear();
			DriverTestPage.textElement.clear();
			((CustomEventFiringWebDriver)driver).scrollTop();
		}
	}
	
	
//	/**
//	 * Is browser able to clic on moving elements
//	 * @throws Exception
//	 */
//	@Test(groups={"it", "ut"})
//	public void testMovingElement() throws Exception {
//		DriverTestPage.startButton.click();
//		DriverTestPage.greenSquare.click();
//		driver.switchTo().alert().accept();
//		
//	}
	
	/**
	 * Check for issue #47 where ReplayAction aspect raised an error when switching to default context after click with alert present
	 */
	@Test(groups={"it", "ut"})
	public void testAlertDisplay() {
		try {
			DriverTestPage.greenSquare.click();
			driver.switchTo().alert().accept();
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
	
	/**
	 * deactivated as it depends on browser
	 */
	@Test(groups={"it", "ut"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		DriverTestPage.startButton.click();
		DriverTestPage.greenSquare.click();
		DriverTestPage.redSquare.click();
		
		if (((CustomEventFiringWebDriver)driver).getWebDriver() instanceof FirefoxDriver && FirefoxDriverFactory.isMarionetteMode()) {
			throw new UnhandledAlertException("fake exception as firefox / marionette does not raise any exception");
		}
	}
   
	
	/**
	 * Test native click
	 */
   
	@Test(groups={"it", "ut"})
	public void testClickDiv() {
		try {
			DriverTestPage.redSquare.click();
			Assert.assertEquals("coucou", DriverTestPage.textElement.getValue());
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}

	@Test(groups={"it", "ut"})
	public void testDoubleClickDiv() {
		try {
			DriverTestPage.redSquare.simulateDoubleClick();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "double coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
   
	@Test(groups={"it", "ut"})
	public void testClickRadio() {
		try {
			DriverTestPage.radioElement.click();
			Assert.assertTrue(DriverTestPage.radioElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	@Test(groups={"it", "ut"})
	public void testClickCheckBox() {
		try {
			DriverTestPage.checkElement.click();
			Assert.assertTrue(DriverTestPage.checkElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
  
	/**
	 * Test javascript actions
	 */
	@Test(groups={"it", "ut"})
	public void testClickJsDiv() {
		try {
			DriverTestPage.redSquare.simulateClick();
			Assert.assertEquals("coucou", DriverTestPage.textElement.getValue());
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
   
	@Test(groups={"it", "ut"})
	public void testClickJsRadio() {
		try {
			DriverTestPage.radioElement.simulateClick();
			Assert.assertTrue(DriverTestPage.radioElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	@Test(groups={"it", "ut"})
	public void testClickJsCheckbox() {
		try {
			DriverTestPage.checkElement.simulateClick();
			Assert.assertTrue(DriverTestPage.checkElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	/**
	 * Test composite actions
	 */
	@Test(groups={"it", "ut"})
	public void testClickActionDiv() {
		try {
			DriverTestPage.redSquare.clickAction();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testDoubleClickActionDiv() {
		try {
			DriverTestPage.redSquare.doubleClickAction();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "double coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testClickActionRadio() {
		try {
			DriverTestPage.radioElement.clickAction();
			Assert.assertTrue(DriverTestPage.radioElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testClickActionCheckbox() {
		try {
			DriverTestPage.checkElement.clickAction();
			Assert.assertTrue(DriverTestPage.checkElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	@Test(groups={"it", "ut"})
	public void testSendKeys() {
		try {
			DriverTestPage.textElement.sendKeys("youpi@[]é");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[]é");
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	@Test(groups={"it", "ut"})
	public void testSendKeysJs() {
		try {
			DriverTestPage.textElement.simulateSendKeys("youpi@[]é");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[]é");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}

	
	/**
	 * Changing data in an input field should throw onBlur event
	 */  
	@Test(groups={"it", "ut"})
	public void testOnBlur() {
		DriverTestPage.onBlurField.sendKeys("onBlur done");
		Assert.assertEquals(DriverTestPage.onBlurFieldDest.getValue(), "onBlur done");
	}
	
	@Test(groups={"it", "ut"})
	public void testFindElements() {
		// 2 elements to find
		Assert.assertEquals(new HtmlElement("", By.name("divFindName")).findElements().size(), 2);
		
		// 4 elements to find, one in a branch
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findElements().size(), 4);
	}
	
	/**
	 * Check that if no element is returned, no error is raised but we should have searched several times
	 */
	@Test(groups={"it", "ut"})
	public void testFindElementsNotExist() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		Assert.assertEquals(new HtmlElement("", By.name("foobar")).findElements().size(), 0);
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}
	
	/**
	 * Tests finding sub-elements of an HTMLElement
	 */
	@Test(groups={"it", "ut"})
	public void testFindElementsBy() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElements(By.className("myClass")).size(), 2);
	}
	
	/**
	 * Tests NOT finding sub-elements of an HTMLElement
	 * No exception should be raised but search should be done several times
	 */
	@Test(groups={"it", "ut"})
	public void testFindElementsByNotExist() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findElements(By.id("foobarId")).size(), 0);
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}
	
	/**
	 * Tests finding sub-elements of an HTMLElement
	 */
	@Test(groups={"it", "ut"})
	public void testFindHtmlElementsBy() {
		List<WebElement> htmlElements = new HtmlElement("", By.id("parent")).findHtmlElements(By.className("myClass"));
		Assert.assertEquals(htmlElements.size(), 2);
		Assert.assertTrue(htmlElements.get(0) instanceof HtmlElement);
		Assert.assertEquals(htmlElements.get(0).getText(), "first child");
	}
	
	/**
	 * issue #314: check that we search element effectively inside the parent
	 */
	@Test(groups={"it", "ut"})
	public void testFindHtmlElementsByWithSimilarElements() {
		List<WebElement> htmlElements = new HtmlElement("", By.id("parent")).findHtmlElements(By.tagName("div"));
		Assert.assertEquals(htmlElements.size(), 3);
		Assert.assertTrue(htmlElements.get(0) instanceof HtmlElement);
		Assert.assertEquals(htmlElements.get(0).getText(), "first child");
	}
	
	/**
	 * Tests finding sub-elements of an HTMLElement inside frame
	 * issue #314: search for an element into iframe. Search sub-elements of this element.
	 * 				We search in the second table because the bug relies in the fact that findHtmlElements do return elements which do not correspond the the elements returned
	 * 				by a simple driver.findElements(By.tagname('a'))
	 */
	@Test(groups={"it", "ut"})
	public void testFindHtmlElementsByInsideFrame() {
		List<WebElement> htmlElements = new HtmlElement("", By.id("tableIframe2"), DriverTestPage.iframe).findHtmlElements(By.tagName("td"));
		Assert.assertEquals(htmlElements.size(), 2);
		Assert.assertTrue(htmlElements.get(0) instanceof HtmlElement);
		Assert.assertEquals(htmlElements.get(0).getText(), "Value 3");
	}
	
	/**
	 * Tests NOT finding sub-elements of an HTMLElement
	 * No exception should be raised but search should be done several times
	 */
	@Test(groups={"it", "ut"})
	public void testFindHtmlElementsByNotExist() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findHtmlElements(By.id("foobarId")).size(), 0);
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}


	/**
	 * Search an element inside an other one
	 */
	@Test(groups={"it", "ut"})
	public void testFindSubElement() {
		Assert.assertEquals(DriverTestPage.parent.findElement(By.className("myClass")).getText(), "first child");
	}
	
	/**
	 * Search the n th element inside an other one
	 */
	@Test(groups={"it", "ut"})
	public void testFindNthSubElement() {
		Assert.assertEquals(DriverTestPage.parent.findElement(By.className("myClass"), 1).getText(), "fourth child");
		Assert.assertEquals(DriverTestPage.child.getText(), "fourth child");
	}
	
	/**
	 * Search the n th element corresponding to locator
	 */
	@Test(groups={"it", "ut"})
	public void testFindNthElement() {
		Assert.assertEquals(DriverTestPage.divFindName.getText(), "an other text");
	}

	/**
	 * test specific HtmlElements actions
	 */
	@Test(groups={"it", "ut"})
	public void testFindPattern1() {
		Assert.assertTrue(DriverTestPage.link2.findLink("href").startsWith("http://www.google.fr"));
	}
	
	@Test(groups={"it", "ut"})
	public void testFindPattern2() {
		Assert.assertTrue(DriverTestPage.linkPopup.findLink("onclick").startsWith("http://www.google.fr"));
	}
	
	@Test(groups={"it", "ut"}) 
	public void testFindPattern3() {
		Assert.assertTrue(DriverTestPage.linkPopup2.findLink("onclick").startsWith("http://www.google.fr"));
	}
	
	/**
	 * text search
	 */
	@Test(groups={"it", "ut"})
	public void testFindPattern4() {
		Assert.assertEquals(new HtmlElement("", By.id("divFind2")).findPattern(Pattern.compile("an (\\w+) text"), "text"), "other");
	}
	
	/**
	 * Check we wait enough for element to be displayed
	 */
	@Test(groups={"it", "ut"}) 
	public void testDelay() {
		try {
			DriverTestPage.delayButton.click();
			Assert.assertEquals(new HtmlElement("", By.id("newEl")).getValue(), "my value");
		} finally {
			DriverTestPage.delayButtonReset.click();
		}
		
	}
	
	/**
	 * Test that it's possible to use an hidden element. Make it appear before using it
	 */
	@Test(groups={"it", "ut"})
	public void testHiddenElement() { 
		DriverTestPage.hiddenCheckBox.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckBox.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckBox.isDisplayed());
	}
	@Test(groups={"it", "ut"})
	public void testHiddenElementByDisplay() { 
		DriverTestPage.hiddenCheckboxByDisplay.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByDisplay.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByDisplay.isDisplayed());
	}
	@Test(groups={"it", "ut"})
	public void testHiddenElementByOpacity() { 
		DriverTestPage.hiddenCheckboxByOpacity.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByOpacity.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByOpacity.isDisplayed());
	}
	@Test(groups={"it", "ut"})
	public void testHiddenElementByVisibility() { 
		DriverTestPage.hiddenCheckboxByVisibility.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByVisibility.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByVisibility.isDisplayed());
	}

	
	
	/**
	 * issue #194: check that the WebDriverWait timeout is the one really applied
	 */
	@Test(groups={"it", "ut"})
	public void testWebDriverWaitWithLowTimeout() {
		long start = new Date().getTime();
		try {
			new WebDriverWait(driver, 2).until(ExpectedConditions.visibilityOf(new HtmlElement("", By.id("someNonExistentId"))));
		} catch (TimeoutException e) {}
		
		// we cannot check precise timing as it depends on the hardware, but we should never wait more that 10 secs (the default timeout for searching element is 30 secs)
		Assert.assertTrue(new Date().getTime() - start < 10000);
	}
	
	@Test(groups={"it", "ut"})
	public void testSearchDoneSeveralTimes() {

		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		try {
			new HtmlElement("", By.id("someNonExistentId")).getText();
		} catch (NoSuchElementException e) {}
		
		// Check we wait at least for the timeout set
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}
	
	@Test(groups={"it", "ut"})
	public void testIsElementPresent1() {		
		
		try {
			DriverTestPage.delayButton.click();
			Assert.assertFalse(new HtmlElement("", By.id("newEl")).isElementPresent(1));
			WaitHelper.waitForSeconds(3);
			Assert.assertTrue(new HtmlElement("", By.id("newEl")).isElementPresent(4));
		} finally {
			DriverTestPage.delayButtonReset.click();
		}
	}
	
	@Test(groups={"it", "ut"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {	
		LocalDateTime start = LocalDateTime.now();
		try {
			new HtmlElement("element", By.id("notPresent"), (Integer)null, 5).getValue();
		} finally {
			Assert.assertTrue(LocalDateTime.now().minusSeconds(7).isBefore(start));
			Assert.assertTrue(LocalDateTime.now().minusSeconds(4).isAfter(start));
		}
	}

//	@Test(groups={"it", "ut"})
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
	@Test(groups={"it", "ut"})
	public void testAutoScrolling() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
		DriverTestPage.scrollButton.click();
		Assert.assertFalse(((JavascriptExecutor) driver).executeScript("return window.pageYOffset;").equals(0L));
	}
	
	/**
	 * Test file upload in case selenium method does not work. This uses the java Robot version which controls mouse and keyboard
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	@Test(groups= {"it", "ut"})
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "envSpecific.ini";
//		DriverTestPage.upload.click();
		DriverTestPage.uploadedFile.click(); // when executing both testUploadFileWithRobotXX tests, the second one fails on firefox because focus is on '<input type="file">' element
		DriverTestPage.upload.clickAction(); // due to restrictions clicking a <input type="file"> element with firefox, use clickAction instead
		testPage.uploadFile(path);
		
		Assert.assertEquals(DriverTestPage.uploadedFile.getAttribute("value"), "envSpecific.ini");
	}
	
	/**
	 * Force driver to use full keyboard typing when writing text instead of using copy-paste feature
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	@Test(groups= {"it", "ut"})
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "objectMapping.ini";
//		DriverTestPage.upload.click();
		DriverTestPage.uploadedFile.click(); // when executing both testUploadFileWithRobotXX tests, the second one fails on firefox because focus is on '<input type="file">' element
		DriverTestPage.upload.clickAction(); // due to restrictions clicking a <input type="file"> element with firefox, use clickAction instead
		CustomEventFiringWebDriver.uploadFileUsingKeyboardTyping(new File(path));
		WaitHelper.waitForSeconds(1);
		Assert.assertEquals(DriverTestPage.uploadedFile.getAttribute("value"), "objectMapping.ini");
	}
	
	/**
	 * Test file upload with standard selenium method
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	@Test(groups= {"it", "ut"})
	public void testUploadFile() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "config.ini";
		DriverTestPage.upload.sendKeys(path);
		
		Assert.assertEquals(DriverTestPage.uploadedFile.getAttribute("value"), "config.ini");
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	@Test(groups={"it", "ut"})
	public void testFindFirstElement() {
		Assert.assertEquals(DriverTestPage.multiElementFirstText.getValue(), "0 text field");
	}
	
	/**
	 * Check that if FIRST_VISIBLE is specified, first visible element is returned
	 * refresh page to be sure element has not been made visible
	 */
	@Test(groups={"it", "ut"})
	public void testFindFirstVisibleElement() {
		driver.navigate().refresh();
		Assert.assertEquals(DriverTestPage.multiElementFirstVisibleText.getValue(), "second text field");
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	@Test(groups={"it", "ut"})
	public void testFindFirstElementWithParent() {
		Assert.assertEquals(DriverTestPage.multiElementFirstTextWithParent.getValue(), "0 text field");
	}
	
	/**
	 * Check that if FIRST_VISIBLE is specified, first visible element is returned. In this case, FIRST_VISIBLE has only been applied to the parent element
	 * refresh page to be sure element has not been made visible
	 */
	@Test(groups={"it", "ut"})
	public void testFindFirstVisibleElementWithParent() {
		driver.navigate().refresh();
		Assert.assertEquals(DriverTestPage.multiElementFirstVisibleTextWithParent.getValue(), "first text field");
	}
	
	/**
	 * get findElements inside an other one using findElements(By) method
	 */
	@Test(groups={"it", "ut"})
	public void testFindElementsUnderAnOtherElement() {
		Assert.assertEquals(DriverTestPage.divByClass.findElements(By.className("someClass")).size(), 4);
	}
	
	/**
	 * get findElements inside an other one using findElements() method
	 */
	@Test(groups={"it", "ut"})
	public void testFindElementsInsideParent() {
		Assert.assertEquals(DriverTestPage.parent.findElement(By.className("myClass")).findElements().size(), 2);
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	@Test(groups={"it", "ut"})
	public void testFindLastElement() {
		Assert.assertEquals(DriverTestPage.multiElementLastText.getValue(), "last text field");
	}
	
	/**
	 * issue #166: Check that when searching an element by XPath, and this element is specified as being located in an other element, 
	 * we still get the element with an xpath relative to the parent element
	 */
	@Test(groups={"it", "ut"})
	public void testFindSubElementByXpath() {
		Assert.assertEquals(DriverTestPage.optionByXpath.getText(), "option1Parent");
	}
	
	/**
	 * Check that when searching an element by XPath, and this element is specified as being located in an other element, 
	 * we still get the element with an xpath relative to the parent element. In this case, the xpath is already specified as relative. No modification should
	 * be done by robot
	 */
	@Test(groups={"it", "ut"})
	public void testFindSubElementByRelativeXpath() {
		Assert.assertEquals(DriverTestPage.optionByRelativeXpath.getText(), "option1Parent");
	}
	
	/**
	 * Check search by XPath without parent is correctly performed (no change due to correction of issue #166)
	 */
	@Test(groups={"it", "ut"})
	public void testFindElementByXpath() {
		Assert.assertEquals(DriverTestPage.searchByXpath.getText(), "option1");
	}
	
	@Test(groups={"it", "ut"})
	public void testIsElementPresent() {
		Assert.assertTrue(DriverTestPage.textElement.isElementPresent(2));
	}
	
	/**
	 * Element is present and displayed => returns true
	 */
	@Test(groups={"it", "ut"})
	public void testIsElementPresentAndDisplayed() {
		Assert.assertTrue(DriverTestPage.textElement.isElementPresentAndDisplayed(2));
	}
	
	/**
	 * Element is not present => returns false
	 */
	@Test(groups={"it", "ut"})
	public void testIsElementNotPresentAndNotDisplayed() {
		Assert.assertFalse(new HtmlElement("", By.id("divNotFound")).isElementPresentAndDisplayed(2));
	}
	
	/**
	 * Element is not present => returns false
	 */
	@Test(groups={"it", "ut"})
	public void testIsElementPresentAndNotDisplayed() {
		Assert.assertFalse(new HtmlElement("", By.id("deu")).isElementPresentAndDisplayed(2));
	}
	
	@Test(groups={"it", "ut"})
	public void testIsElementNotPresent() {
		Assert.assertFalse(new HtmlElement("", By.id("divNotFound")).isElementPresent(2));
	}
	
	/**
	 * issue #286: check that element is seen as present
	 */
	@Test(groups={"it", "ut"})
	public void testTextElementInsideHtmlElementIsPresent() {
		Assert.assertEquals(DriverTestPage.optionOfSelectListIFrameByText.getText(), "option1 frame");
		Assert.assertTrue(DriverTestPage.optionOfSelectListIFrameByText.isElementPresent(2));
	}

	/**
	 * issue #286: check that element is not present
	 */
	@Test(groups={"it", "ut"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		Assert.assertFalse(DriverTestPage.wrongElementOfSelectListIFrameByText.isElementPresent(2));
	}
	

	/**
	 * issue #286: check that no error is raised if frame cannot be found
	 */
	@Test(groups={"it", "ut"})
	public void testElementWithIFrameAbsent() {
		Assert.assertFalse(DriverTestPage.elementNotPresentInIFrame.isElementPresent(2));
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTextElementInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.textElement2.getValue(), "default");
	}
	
	@Test(groups={"it", "ut"})
	public void testFindRadioElementInsideHtmlElement() {
		try {
			DriverTestPage.radioElement2.click();
			Assert.assertTrue(new HtmlElement("", By.id("radioClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testFindCheckElementInsideHtmlElement() {
		try {
			DriverTestPage.checkElement2.click();
			Assert.assertTrue(new HtmlElement("", By.id("checkboxClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testFindButtonElementInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.resetButton2.getText(), "reset button");
	}
	
	@Test(groups={"it", "ut"})
	public void testFindLinkElementInsideHtmlElement() {
		Assert.assertTrue(DriverTestPage.linkElement2.getUrl().toLowerCase().contains("http://www.googlefrance.fr"));
	}
	
	@Test(groups={"it", "ut"})
	public void testFindSelectElementInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.selectList2.getOptions().size(), 2);
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTableInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.table2.getRowCount(), 2);
	}
	
	/*
	 * Use elements searched by index inside other elements 
	 */
	
	@Test(groups={"it", "ut"})
	public void testFindTextElementsInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.textElement3.getValue(), "default");
	}
	
	@Test(groups={"it", "ut"})
	public void testFindRadioElementsInsideHtmlElement() {
		try {
			DriverTestPage.radioElement3.click();
			Assert.assertTrue(new HtmlElement("", By.id("radioClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testFindCheckElementsInsideHtmlElement() {
		try {
			DriverTestPage.checkElement3.click();
			Assert.assertTrue(new HtmlElement("", By.id("checkboxClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	@Test(groups={"it", "ut"})
	public void testFindButtonElementsInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.resetButton3.getText(), "reset button");
	}
	
	@Test(groups={"it", "ut"})
	public void testFindLinkElementsInsideHtmlElement() {
		Assert.assertTrue(DriverTestPage.linkElement3.getUrl().toLowerCase().contains("http://www.googlefrance.fr"));
	}
	
	@Test(groups={"it", "ut"})
	public void testFindSelectElementsInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.selectList3.getOptions().size(), 2);
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTablesInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.table3.getRowCount(), 2);
	}
	
	/**
	 * issue #262: check we can scroll and click to an element inside div
	 */
	@Test(groups={"it", "ut"})
	public void testScrollIntoDiv() {
		try {
			DriverTestPage.greenBox.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "greenbox");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
	
	/**
	 * issue #274: check that scrolling is correct and we can click on an element at the bottom of the page
	 */
	@Test(groups={"it", "ut"})
	public void testScrollToBottom() {
		try {
			DriverTestPage.bigFooterButton.click();
			DriverTestPage.bottomSquare.click();
			Assert.assertEquals("coucou bottom", DriverTestPage.textElement.getValue());
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals("", DriverTestPage.textElement.getValue());
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test(groups= {"it", "ut"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		DriverTestPage page = new DriverTestPage();
		page._goToNewPage();

		// close window
		DriverSubTestPage.closeButton.click();
		Alert alert = page.waitForAlert(3);
		if (alert != null) {
			alert.accept();
		}
		
		try {
			driver.getCurrentUrl();
		} catch (NoSuchWindowException e) {
		}
		
		Assert.assertNotNull(WebUIDriver.getWebDriver(false));
	}
	
}
