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
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
	

	@AfterMethod(groups={"it", "ie"}, alwaysRun=true)
	public void reset() {
		if (driver != null) {
			driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
			DriverTestPage.logoText.clear();
			DriverTestPage.textElement.clear();
			((CustomEventFiringWebDriver)driver).scrollTop();
		}
	}
	
	
//	/**
//	 * Is browser able to clic on moving elements
//	 * @throws Exception
//	 */
//	
//	public void testMovingElement() throws Exception {
//		DriverTestPage.startButton.click();
//		DriverTestPage.greenSquare.click();
//		driver.switchTo().alert().accept();
//		
//	}
	
	/**
	 * Check for issue #47 where ReplayAction aspect raised an error when switching to default context after click with alert present
	 */
	
	public void testAlertDisplay() {
		try {
			DriverTestPage.greenSquare.click();
			driver.switchTo().alert().accept();
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}
	
	/**
	 * deactivated as it depends on browser
	 */
	
	public void testFindWithAlert() {
		DriverTestPage.startButton.click();
		DriverTestPage.greenSquare.click();
		DriverTestPage.redSquare.click();
		
		if (((CustomEventFiringWebDriver)driver).getOriginalDriver() instanceof FirefoxDriver && FirefoxDriverFactory.isMarionetteMode()) {
			throw new UnhandledAlertException("fake exception as firefox / marionette does not raise any exception");
		}
	}
   
	
	/**
	 * Test native click
	 */
   
	
	public void testClickDiv() {
		try {
			DriverTestPage.redSquare.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}

	
	public void testSendKeysElementNotFound() {
		testPage._writeSomethingOnNonExistentElement();
	}

	
	public void testDoubleClickDiv() {
		try {
			DriverTestPage.redSquare.simulateDoubleClick();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "double coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals( DriverTestPage.textElement.getValue(), "");
		}
	}
	
	public void testClickRadio() {
		try {
			DriverTestPage.radioElement.click();
			Assert.assertTrue(DriverTestPage.radioElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	
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
	
	public void testClickJsDiv() {
		try {
			DriverTestPage.redSquare.simulateClick();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}
   
	
	public void testClickJsRadio() {
		try {
			DriverTestPage.radioElement.simulateClick();
			Assert.assertTrue(DriverTestPage.radioElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	
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
	
	public void testClickActionDiv() {
		try {
			DriverTestPage.redSquare.clickAction();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}

	public void testRightClickActionDiv() {
		try {
			DriverTestPage.redSquare.rightClickAction();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "right coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals( DriverTestPage.textElement.getValue(), "");
		}
	}

	public void testDoubleClickActionDiv() {
		try {
			DriverTestPage.redSquare.doubleClickAction();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "double coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}
	
	
	public void testClickActionRadio() {
		try {
			DriverTestPage.radioElement.clickAction();
			Assert.assertTrue(DriverTestPage.radioElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	
	public void testClickActionCheckbox() {
		try {
			DriverTestPage.checkElement.clickAction();
			Assert.assertTrue(DriverTestPage.checkElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	public void testClickWithMouse() {
		try {
			DriverTestPage.checkElement.clickMouse();
			Assert.assertTrue(DriverTestPage.checkElement.isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}

	public void testRightClickWithMouse() {
		try {
			DriverTestPage.redSquare.rightClickMouse();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "right coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals( DriverTestPage.textElement.getValue(), "");
		}
	}
   
	
	public void testSendKeys() {
		try {
			DriverTestPage.textElement.sendKeys("youpi@[]é");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[]é");
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
   
	
	public void testSendKeysJs() {
		try {
			DriverTestPage.textElement.simulateSendKeys("youpi@[]é");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[]é");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}

	public void testSendKeysAction() {
		try {
			DriverTestPage.textElement.sendKeysAction("youpi@[]é");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[]é");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}

	public void testSendKeysActionWithPause() {
		try {
			long startTime = System.nanoTime();
			DriverTestPage.textElement.sendKeysAction(500, "youpi@[]", " meduse");
			long endTime = System.nanoTime();

			long nanoDuration = endTime - startTime;
			long secondDuration = TimeUnit.SECONDS.convert(nanoDuration, TimeUnit.NANOSECONDS);

			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[] meduse");

			Assert.assertTrue(secondDuration < 11);
			Assert.assertTrue(secondDuration > 7);
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}

	public void testSendKeysKeyboard() {
		try {
			DriverTestPage.textElement.sendKeysKeyboard("youpi@[]é");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi@[]é");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}

	
	/**
	 * Changing data in an input field should throw onBlur event
	 */  
	
	public void testOnBlur() {
		DriverTestPage.onBlurField.sendKeys("onBlur done");
		Assert.assertEquals(DriverTestPage.onBlurFieldDest.getValue(), "onBlur done");
	}
	
	
	public void testFindElements() {
		// 2 elements to find
		Assert.assertEquals(new HtmlElement("", By.name("divFindName")).findElements().size(), 2);
		
		// 4 elements to find, one in a branch
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findElements().size(), 4);
	}
	
	/**
	 * Check that if no element is returned, no error is raised but we should have searched several times
	 */
	
	public void testFindElementsNotExist() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		Assert.assertEquals(new HtmlElement("", By.name("foobar")).findElements().size(), 0);

		long delay = new Date().getTime() - start;
		logger.info("Action duration: " + delay + " ms");
		Assert.assertTrue(delay > 6500); // using timing is not a problem here as in case of slowness, search will be longer
	}
	
	/**
	 * Check setting the delay for click action is correctly handled
	 */
	public void testActionDelay() {
		SeleniumTestsContextManager.getThreadContext().setActionDelay(5000);
		long start = new Date().getTime();
		DriverTestPage.redSquare.click();
		DriverTestPage.resetButton.click();
		DriverTestPage.resetButton.click();
		long delay = new Date().getTime() - start;
		logger.info("Action duration: " + delay + " ms");

		// using timing is not a problem here as in case of slowness, search will be longer
		Assert.assertTrue(delay > 14000); // 3 commands with wait
	}
	/**
	 * If no delay is specified, we go as quick as possible
	 */
	public void testActionNoDelay() {
		SeleniumTestsContextManager.getThreadContext().setActionDelay(0);
		long start = new Date().getTime();
		DriverTestPage.redSquare.click();
		DriverTestPage.resetButton.click();
		DriverTestPage.resetButton.click();
		long delay = new Date().getTime() - start;
		logger.info("Action duration: " + delay + " ms");
		Assert.assertTrue(delay < 1500); // 3 commands without wait
	}
	/**
	 * Some actions (we do not test all) do not define a wait
	 */
	public void testActionNoDelay2() {
		SeleniumTestsContextManager.getThreadContext().setActionDelay(5000);
		long start = new Date().getTime();
		DriverTestPage.redSquare.getCoordinates();
		DriverTestPage.resetButton.getCenter();
		long delay = new Date().getTime() - start;
		logger.info("Action duration: " + delay + " ms");
		Assert.assertTrue(delay < 4000); // 2 commands without wait, we should be lower than the defined action delay
	}
	
	/**
	 * Tests finding sub-elements of an HTMLElement
	 */
	
	public void testFindElementsBy() {
		Assert.assertEquals(new HtmlElement("", By.id("parent")).findElements(By.className("myClass")).size(), 2);
	}
	
	/**
	 * Tests NOT finding sub-elements of an HTMLElement
	 * No exception should be raised but search should be done several times
	 */
	public void testFindElementsByNotExist() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findElements(By.id("foobarId")).size(), 0);

		// no problem to use timing here, has in case of slowness, assertion will still be true
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}
	
	/**
	 * Tests finding sub-elements of an HTMLElement
	 */
	public void testFindHtmlElementsBy() {
		List<WebElement> htmlElements = new HtmlElement("", By.id("parent")).findHtmlElements(By.className("myClass"));
		Assert.assertEquals(htmlElements.size(), 2);
		Assert.assertTrue(htmlElements.get(0) instanceof HtmlElement);
		Assert.assertEquals(htmlElements.get(0).getText(), "first child");
	}
	
	/**
	 * issue #314: check that we search element effectively inside the parent
	 */
	public void testFindHtmlElementsByWithSimilarElements() {
		List<WebElement> htmlElements = new HtmlElement("", By.id("parent")).findHtmlElements(By.tagName("div"));
		Assert.assertEquals(htmlElements.size(), 4);
		Assert.assertTrue(htmlElements.get(0) instanceof HtmlElement);
		Assert.assertEquals(htmlElements.get(0).getText(), "first child");
	}
	
	/**
	 * Tests finding sub-elements of an HTMLElement inside frame
	 * issue #314: search for an element into iframe. Search sub-elements of this element.
	 * 				We search in the second table because the bug relies in the fact that findHtmlElements do return elements which do not correspond the the elements returned
	 * 				by a simple driver.findElements(By.tagname('a'))
	 */
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
	public void testFindHtmlElementsByNotExist() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		Assert.assertEquals(new HtmlElement("", By.className("myClass")).findHtmlElements(By.id("foobarId")).size(), 0);

		// no problem to use timing here, has in case of slowness, assertion will still be true
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}


	/**
	 * Search an element inside an other one
	 */
	public void testFindSubElement() {
		Assert.assertEquals(DriverTestPage.parent.findElement(By.className("myClass")).getText(), "first child");
	}
	
	/**
	 * Search the n th element inside an other one
	 */
	public void testFindNthSubElement() {
		Assert.assertEquals(DriverTestPage.parent.findElement(By.className("myClass"), 1).getText(), "fourth child");
		Assert.assertEquals(DriverTestPage.child.getText(), "fourth child");
	}
	
	/**
	 * Search the n th element corresponding to locator
	 */
	
	public void testFindNthElement() {
		Assert.assertEquals(DriverTestPage.divFindName.getText(), "an other text");
	}

	/**
	 * test specific HtmlElements actions
	 */
	
	public void testFindPattern1() {
		Assert.assertTrue(DriverTestPage.link2.findLink("href").startsWith("http://www.google.fr"));
	}
	
	
	public void testFindPattern2() {
		Assert.assertTrue(DriverTestPage.linkPopup.findLink("onclick").startsWith("http://www.google.fr"));
	}
	
	
	public void testFindPattern3() {
		Assert.assertTrue(DriverTestPage.linkPopup2.findLink("onclick").startsWith("http://www.google.fr"));
	}
	
	/**
	 * text search
	 */
	
	public void testFindPattern4() {
		Assert.assertEquals(new HtmlElement("", By.id("divFind2")).findPattern(Pattern.compile("an (\\w+) text"), "text"), "other");
	}
	
	/**
	 * Check we wait enough for element to be displayed
	 */
	
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
	
	public void testHiddenElement() { 
		DriverTestPage.hiddenCheckBox.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckBox.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckBox.isDisplayed());
	}
	
	public void testHiddenElementByDisplay() { 
		DriverTestPage.hiddenCheckboxByDisplay.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByDisplay.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByDisplay.isDisplayed());
	}
	
	public void testHiddenElementByOpacity() { 
		DriverTestPage.hiddenCheckboxByOpacity.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByOpacity.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByOpacity.isDisplayed());
	}
	
	public void testHiddenElementByVisibility() { 
		DriverTestPage.hiddenCheckboxByVisibility.click();
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByVisibility.isSelected());
		Assert.assertTrue(DriverTestPage.hiddenCheckboxByVisibility.isDisplayed());
	}

	
	
	/**
	 * issue #194: check that the WebDriverWait timeout is the one really applied
	 */
	public void testWebDriverWaitWithLowTimeout() {
		long delay = 100000;

		// depending on hardware, test may fail, so we retry several times
		for (int i=0; i < 3; i++) {
			long start = new Date().getTime();
			try {
				new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOf(new HtmlElement("", By.id("someNonExistentId"))));
			} catch (TimeoutException e) {
			}

			// we cannot check precise timing as it depends on the hardware, but we should never wait more that 10 secs (the default timeout for searching element is 30 secs)
			delay = new Date().getTime() - start;
			logger.info("wait delay: {} ms", delay);
			if (delay < 10000) {
				break;
			}
		}
		Assert.assertTrue(delay < 10000);
	}
	
	
	public void testSearchDoneSeveralTimes() {

		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(7);
		long start = new Date().getTime();
		try {
			new HtmlElement("", By.id("someNonExistentId")).getText();
		} catch (NoSuchElementException e) {}
		
		// Check we wait at least for the timeout set
		Assert.assertTrue(new Date().getTime() - start > 6500);
	}
	
	/**
	 * Test element present timing
	 */
	
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
	
	/**
	 * Test element detection timing
	 */
	
	public void testElementNotPresent() {	
		LocalDateTime start = LocalDateTime.now();
		try {
			DriverTestPage.elementNotPresentWithTimeout.getValue();
		} catch (NoSuchElementException e) {
			Assert.assertTrue(e.getMessage().contains("Searched element [HtmlElement element, by={By.id: notPresent}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found"));
			throw e;
		} finally {
			Assert.assertTrue(LocalDateTime.now().minusSeconds(8).isBefore(start));
			Assert.assertTrue(LocalDateTime.now().minusSeconds(4).isAfter(start));
		}
	}

//	
//	public void testFindImageElement() {
//		
//		try {
//			driver.executeScript("window.scrollTo(0, 0);");
//			driver.findImageElement(new File(Thread.currentThread().getContextClassLoader().getResource("googleSearch.png").getFile())).click();
//			Assert.assertEquals("image", driver.findElement(By.id("text2")).getDomProperty("value"));
//		} finally {
//			driver.findElement(By.id("button2")).click();
//		}
//	}
	
	/**
	 * Vérifie qu'avant d'agir sur un élément, on positionne la fenêtre du navigateur pour qu'il soit visible
	 */
	
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
	
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "envSpecific.ini";
//		DriverTestPage.upload.click();
		DriverTestPage.uploadedFile.click(); // when executing both testUploadFileWithRobotXX tests, the second one fails on firefox because focus is on '<input type="file">' element
		DriverTestPage.upload.clickAction(); // due to restrictions clicking a <input type="file"> element with firefox, use clickAction instead
		WaitHelper.waitForSeconds(10); // sometimes, upload popup needs time to display
		testPage.uploadFile(path);
		
		Assert.assertEquals(DriverTestPage.uploadedFile.getDomProperty("value"), "envSpecific.ini");
	}
	
	/**
	 * Force driver to use full keyboard typing when writing text instead of using copy-paste feature
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		String path = Paths.get(SeleniumTestsContextManager.getConfigPath(), "spec", "envSpecific2.ini").toString();
//		DriverTestPage.upload.click();
		DriverTestPage.uploadedFile.click(); // when executing both testUploadFileWithRobotXX tests, the second one fails on firefox because focus is on '<input type="file">' element
		DriverTestPage.upload.clickAction(); // due to restrictions clicking a <input type="file"> element with firefox, use clickAction instead
		WaitHelper.waitForSeconds(10); // sometimes, upload popup needs time to display
		CustomEventFiringWebDriver.uploadFileUsingKeyboardTyping(new File(path));
		WaitHelper.waitForSeconds(1);
		Assert.assertEquals(DriverTestPage.uploadedFile.getDomProperty("value"), "envSpecific2.ini");
	}
	
	/**
	 * Test file upload with standard selenium method
	 * @throws AWTException
	 * @throws InterruptedException
	 */
	
	public void testUploadFile() throws AWTException, InterruptedException {
		String path = SeleniumTestsContextManager.getConfigPath() + File.separator + "config.ini";
		DriverTestPage.upload.sendKeys(path);
		
		Assert.assertEquals(DriverTestPage.uploadedFile.getDomProperty("value"), "config.ini");
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	
	public void testFindFirstElement() {
		Assert.assertEquals(DriverTestPage.multiElementFirstText.getValue(), "0 text field");
	}
	
	/**
	 * Check that if FIRST_VISIBLE is specified, first visible element is returned
	 * refresh page to be sure element has not been made visible
	 */
	
	public void testFindFirstVisibleElement() {
		driver.navigate().refresh();
		Assert.assertEquals(DriverTestPage.multiElementFirstVisibleText.getValue(), "second text field");
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	
	public void testFindFirstElementWithParent() {
		Assert.assertEquals(DriverTestPage.multiElementFirstTextWithParent.getValue(), "0 text field");
	}
	
	/**
	 * Check that if FIRST_VISIBLE is specified, first visible element is returned. In this case, FIRST_VISIBLE has only been applied to the parent element
	 * refresh page to be sure element has not been made visible
	 */
	
	public void testFindFirstVisibleElementWithParent() {
		driver.navigate().refresh();
		Assert.assertEquals(DriverTestPage.multiElementFirstVisibleTextWithParent.getValue(), "first text field");
	}
	
	/**
	 * get findElements inside an other one using findElements(By) method
	 */
	
	public void testFindElementsUnderAnOtherElement() {
		Assert.assertEquals(DriverTestPage.divByClass.findElements(By.className("someClass")).size(), 4);
	}
	
	/**
	 * get findElements inside an other one using findElements() method
	 */
	
	public void testFindElementsInsideParent() {
		Assert.assertEquals(DriverTestPage.parent.findElement(By.className("myClass")).findElements().size(), 2);
	}
	
	/**
	 * Check that if no index is specified, first element is get
	 */
	
	public void testFindLastElement() {
		Assert.assertEquals(DriverTestPage.multiElementLastText.getValue(), "last text field");
	}
	
	/**
	 * issue #166: Check that when searching an element by XPath, and this element is specified as being located in an other element, 
	 * we still get the element with an xpath relative to the parent element
	 */
	
	public void testFindSubElementByXpath() {
		Assert.assertEquals(DriverTestPage.optionByXpath.getText(), "option1Parent");
	}
	
	/**
	 * Check that when searching an element by XPath, and this element is specified as being located in an other element, 
	 * we still get the element with an xpath relative to the parent element. In this case, the xpath is already specified as relative. No modification should
	 * be done by robot
	 */
	
	public void testFindSubElementByRelativeXpath() {
		Assert.assertEquals(DriverTestPage.optionByRelativeXpath.getText(), "option1Parent");
	}
	
	/**
	 * Check search by XPath without parent is correctly performed (no change due to correction of issue #166)
	 */
	
	public void testFindElementByXpath() {
		Assert.assertEquals(DriverTestPage.searchByXpath.getText(), "option1");
	}
	
	
	public void testIsElementPresent() {
		Assert.assertTrue(DriverTestPage.textElement.isElementPresent(2));
	}
	
	/**
	 * issue #355: check that we get "false" when element is not present and index is given 
	 */
	
	public void testIsElementNotPresentWithIndex() {
		Assert.assertFalse(DriverTestPage.textElementNotPresentFirstVisible.isElementPresent(2));
	}

	/**
	 * issue #355: Element is present but not displayed => returns false
	 */
	
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		Assert.assertFalse(new HtmlElement("", By.id("deu"), HtmlElement.FIRST_VISIBLE).isElementPresent(2));
	}
	
	/**
	 * Element is present and displayed => returns true
	 */
	
	public void testIsElementPresentAndDisplayed() {
		Assert.assertTrue(DriverTestPage.textElement.isElementPresentAndDisplayed(2));
	}

	public void testIsElementPresentAndDisplayedWithDelay() {

		try {
			DriverTestPage.delayHiddenButton.click();
			Assert.assertFalse(new HtmlElement("", By.id("hiddenContent")).isElementPresentAndDisplayed(1));
			WaitHelper.waitForSeconds(3);
			Assert.assertTrue(new HtmlElement("", By.id("hiddenContent")).isElementPresentAndDisplayed(4));
		} finally {
			DriverTestPage.delayHiddenButtonReset.click();
		}
	}
	
	/**
	 * Element is not present => returns false
	 */
	
	public void testIsElementNotPresentAndNotDisplayed() {
		Assert.assertFalse(new HtmlElement("", By.id("divNotFound")).isElementPresentAndDisplayed(2));
	}
	
	/**
	 * Element is not present => returns false
	 */
	
	public void testIsElementPresentAndNotDisplayed() {
		Assert.assertFalse(new HtmlElement("", By.id("deu")).isElementPresentAndDisplayed(2));
	}
	
	
	public void testIsElementNotPresent() {
		Assert.assertFalse(new HtmlElement("", By.id("divNotFound")).isElementPresent(2));
	}

	public void testWaitForElementPresentAndDisplayedWithDelay() {

		try {
			DriverTestPage.delayHiddenButton.click();
			new HtmlElement("", By.id("hiddenContent")).waitForPresentAndDisplayed(8);
		} finally {
			DriverTestPage.delayHiddenButtonReset.click();
		}
	}

	public void testWaitForElementNotPresentAndNotDisplayed() {
		Instant start = Instant.now();
		boolean exceptionRaised = false;
		try {
			new HtmlElement("", By.id("divNotFound")).waitForPresentAndDisplayed(2);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
		Assert.assertTrue(Instant.now().isBefore(start.plusSeconds(5)));
	}

	// Will raise TimeoutException
	public void testWaitForElementPresentAndNotDisplayed() {
		Instant start = Instant.now();
		boolean exceptionRaised = false;
		try {
			new HtmlElement("", By.id("deu")).waitForPresentAndDisplayed(2);
		} catch (TimeoutException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
		Assert.assertTrue(Instant.now().isBefore(start.plusSeconds(5)));
	}
	
	/**
	 * issue #286: check that element is seen as present
	 */
	
	public void testTextElementInsideHtmlElementIsPresent() {
		Assert.assertEquals(DriverTestPage.optionOfSelectListIFrameByText.getText(), "option1 frame");
		Assert.assertTrue(DriverTestPage.optionOfSelectListIFrameByText.isElementPresent(2));
	}

	/**
	 * issue #286: check that element is not present
	 */
	
	public void testTextElementInsideHtmlElementIsNotPresent() {
		Assert.assertFalse(DriverTestPage.wrongElementOfSelectListIFrameByText.isElementPresent(2));
	}
	

	/**
	 * issue #286: check that no error is raised if frame cannot be found
	 */
	
	public void testElementWithIFrameAbsent() {
		Assert.assertFalse(DriverTestPage.elementNotPresentInIFrame.isElementPresent(2));
	}
	
	
	public void testFindTextElementInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.textElement2.getValue(), "default");
	}
	
	
	public void testFindRadioElementInsideHtmlElement() {
		try {
			DriverTestPage.radioElement2.click();
			Assert.assertTrue(new HtmlElement("", By.id("radioClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	
	public void testFindCheckElementInsideHtmlElement() {
		try {
			DriverTestPage.checkElement2.click();
			Assert.assertTrue(new HtmlElement("", By.id("checkboxClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	
	public void testFindButtonElementInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.resetButton2.getText(), "reset button");
	}
	
	
	public void testFindLinkElementInsideHtmlElement() {
		Assert.assertTrue(DriverTestPage.linkElement2.getUrl().toLowerCase().contains("http://www.googlefrance.fr"));
	}
	
	
	public void testFindSelectElementInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.selectList2.getOptions().size(), 2);
	}
	
	
	public void testFindTableInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.table2.getRowCount(), 2);
	}
	
	/*
	 * Use elements searched by index inside other elements 
	 */
	
	
	public void testFindTextElementsInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.textElement3.getValue(), "default");
	}
	
	
	public void testFindRadioElementsInsideHtmlElement() {
		try {
			DriverTestPage.radioElement3.click();
			Assert.assertTrue(new HtmlElement("", By.id("radioClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	
	public void testFindCheckElementsInsideHtmlElement() {
		try {
			DriverTestPage.checkElement3.click();
			Assert.assertTrue(new HtmlElement("", By.id("checkboxClickParent")).isSelected());
		} finally {
			DriverTestPage.resetButton.click();
		}
	}
	
	
	public void testFindButtonElementsInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.resetButton3.getText(), "reset button");
	}
	
	
	public void testFindLinkElementsInsideHtmlElement() {
		Assert.assertTrue(DriverTestPage.linkElement3.getUrl().toLowerCase().contains("http://www.googlefrance.fr"));
	}
	
	
	public void testFindSelectElementsInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.selectList3.getOptions().size(), 2);
	}
	
	
	public void testFindTablesInsideHtmlElement() {
		Assert.assertEquals(DriverTestPage.table3.getRowCount(), 2);
	}
	
	/**
	 * issue #262: check we can scroll and click to an element inside div
	 */
	
	public void testScrollIntoDiv() {
		try {
			DriverTestPage.greenBox.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "greenbox");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}
	
	/**
	 * issue #274: check that scrolling is correct and we can click on an element at the bottom of the page
	 */
	
	public void testScrollToBottom() {
		try {
			DriverTestPage.bigFooterButton.click();
			DriverTestPage.bottomSquare.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "coucou bottom");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "");
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	
	public void testCaptureWhenWindowIsClosed() throws Exception {
		DriverTestPage page = new DriverTestPage();
		page._goToNewPage();

		// close window
		DriverSubTestPage.closeButton.click();
		Alert alert = page.waitForAlert(3);
		if (alert != null) {
			try {
				alert.accept();
			} catch (Exception e) {
				// in case window has closed, alert is not present anymore
			}
		}
		
		try {
			driver.getCurrentUrl();
		} catch (NoSuchWindowException e) {
		}
		
		Assert.assertNotNull(WebUIDriver.getWebDriver(false));
	}
	
}
