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
package com.seleniumtests.it.driver.support.perdriver.safari;

import java.awt.AWTException;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverSafari extends TestDriver {

	public TestDriverSafari() throws Exception {
		super(BrowserType.SAFARI);
	}
	@Override
    @Test(groups={"it", "ut"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Override
    @Test(groups={"it", "ut"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Override
    @Test(groups={"it", "ut"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Override
    @Test(groups={"it", "ut"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Override
    @Test(groups={"it", "ut"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Override
    @Test(groups={"it", "ut"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Override
    @Test(groups={"it", "ut"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Override
    @Test(groups={"it", "ut"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testClickActionDiv() {
		super.testClickActionDiv();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testRightClickActionDiv() {
		super.testRightClickActionDiv();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testDoubleClickActionDiv() {
		super.testDoubleClickActionDiv();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testClickActionRadio() {
		super.testClickActionRadio();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testClickActionCheckbox() {
		super.testClickActionCheckbox();
	}

	@Override
    @Test(groups={"it"})
	public void testClickWithMouse() {
		super.testClickWithMouse();
	}

	@Override
    @Test(groups={"it"})
	public void testRightClickWithMouse() {
		super.testRightClickWithMouse();
	}
   
	@Override
    @Test(groups={"it", "ut"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Override
    @Test(groups={"it", "ut"})
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
    @Test(groups={"it", "ut"})
	public void testOnBlur() {
		super.testOnBlur();
	}

	@Override
    @Test(groups={"it", "ut"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindElements() {
		super.testFindElements();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Override
    @Test(groups={"it", "ut"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Override
    @Test(groups={"it", "ut"})
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
    @Test(groups={"it", "ut"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Override
    @Test(groups={"it", "ut"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Override
    @Test(groups={"it", "ut"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrolling() {
		super.testAutoScrolling();
	}
	
	@Override
    @Test(groups= {"it", "ut"})
	public void testUploadFileWithRobot() {
		super.testUploadFileWithRobot();
	}
	
	@Override
    @Test(groups= {"it", "ut"})
	public void testUploadFileWithRobotKeyboard() {
		super.testUploadFileWithRobotKeyboard();
	}
	
	@Override
    @Test(groups= {"it", "ut"})
	public void testUploadFile() {
		super.testUploadFile();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Override
    @Test(groups={"it", "ut"})
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
    @Test(groups={"it", "ut"})
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Override
    @Test(groups= {"it", "ut"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}
}
