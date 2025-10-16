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
package com.seleniumtests.it.driver.support.perdriver.ie;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.it.driver.TestDriver;


public class TestDriverInternetExplorer extends TestDriver {

	public TestDriverInternetExplorer() throws Exception {
		super(BrowserType.INTERNET_EXPLORER);
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Override
    @Test(groups= {"ie"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}

	@Override
    @Test(groups= {"ie"})
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testClickActionDiv() {
		super.testClickActionDiv();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDoubleClickActionDiv() {
		super.testDoubleClickActionDiv();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testClickActionRadio() {
		super.testClickActionRadio();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testClickActionCheckbox() {
		super.testClickActionCheckbox();
	}

	@Override
    @Test(groups={"it"})
	public void testClickWithMouse() {
		super.testClickWithMouse();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Override
    @Test(groups= {"ie"})
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Override
    @Test(groups = {"it", "ut"})
	public void testSendKeysAction() {
		super.testSendKeysAction();
	}

	@Override
    @Test(groups = {"it", "ut"})
	public void testSendKeysActionWithPause() {
		super.testSendKeysActionWithPause();
	}

	@Override
    @Test(groups={"it"})
	public void testSendKeysKeyboard() {
		super.testSendKeysKeyboard();
	}

	@Override
    @Test(groups= {"ie"})
	public void testOnBlur() {
		super.testOnBlur();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindElements() {
		super.testFindElements();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Override
    @Test(groups= {"ie"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindHtmlElementsByWithSimilarElements() {
		super.testFindHtmlElementsByWithSimilarElements();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindHtmlElements() {
		super.testFindHtmlElements();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindHtmlElementsInsideFrame() {
		super.testFindHtmlElementsInsideFrame();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindHtmlElementsNotExist() {
		super.testFindHtmlElementsNotExist();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Override
    @Test(groups= {"ie"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Override
    @Test(groups= {"ie"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Override
    @Test(groups= {"ie"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}

	@Override
    @Test(groups= {"ie"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}

	@Override
    @Test(groups= {"ie"})
	public void testAutoScrolling() {
		super.testAutoScrolling();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		super.testUploadFileWithRobot();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		super.testUploadFileWithRobotKeyboard();
	}
	
	/**
	 * Retry file upload if something goes wrong
	 */
	@Override
    @Test(groups= {"ie"})
	public void testUploadFile() throws AWTException, InterruptedException {
		try {
			super.testUploadFile();
		} catch (Throwable e) {
			logger.warn("test upload failed and retried due to session timeout exception");
			CustomEventFiringWebDriver.sendKeysToDesktop(List.of(KeyEvent.VK_ESCAPE), DriverMode.LOCAL, null);
//			try {
//				stop();
//				exposeTestPage(testNGCtx);
//			} catch (Exception e1) {
//				throw e;
//			}
			super.testUploadFile();
		}
		
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Override
    @Test(groups= {"ie"})
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testIsElementPresentAndDisplayed() {
		super.testIsElementPresentAndDisplayed();
	}
	@Override
    @Test(groups={"it", "ut"})
	public void testWaitForElementPresentAndDisplayedWithDelay() {
		super.testWaitForElementPresentAndDisplayedWithDelay();
	}
	@Override
    @Test(groups={"it", "ut"})
	public void testWaitForElementNotPresentAndNotDisplayed() {
		super.testWaitForElementNotPresentAndNotDisplayed();
	}
	@Override
    @Test(groups={"it", "ut"})
	public void testWaitForElementPresentAndNotDisplayed() {
		super.testWaitForElementPresentAndNotDisplayed();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementPresentAndDisplayedWithDelay() {
		super.testIsElementPresentAndDisplayedWithDelay();
	}

	@Override
    @Test(groups= {"ie"})
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Override
    @Test(groups= {"ie"})
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Override
    @Test(groups= {"ie"})
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Override
    @Test(groups= {"ie"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Override
    @Test(groups= {"ie"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}
}
