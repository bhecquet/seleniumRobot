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
package com.seleniumtests.it.driver.support.perdriver.testdriver;

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
	@Test(groups="it")
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Test(groups="it", expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Test(groups="it")
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Test(groups="it")
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Test(groups="it")
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Test(groups="it")
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}

	@Test(groups="it")
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Test(groups="it")
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Test(groups="it")
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
	
	@Test(groups="it")
	public void testClickActionDiv() {
		super.testClickActionDiv();
	}
	
	@Test(groups="it")
	public void testDoubleClickActionDiv() {
		super.testDoubleClickActionDiv();
	}
	
	@Test(groups="it")
	public void testClickActionRadio() {
		super.testClickActionRadio();
	}
	
	@Test(groups="it")
	public void testClickActionCheckbox() {
		super.testClickActionCheckbox();
	}

	@Test(groups={"it-driver"})
	public void testClickWithMouse() {
		super.testClickWithMouse();
	}
   
	@Test(groups="it")
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Test(groups="it")
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Test(groups = "it")
	public void testSendKeysAction() {
		super.testSendKeysAction();
	}

	@Test(groups = "it")
	public void testSendKeysActionPause() {
		super.testSendKeysActionWithPause();
	}

	@Test(groups={"it-driver"})
	public void testSendKeysKeyboard() {
		super.testSendKeysKeyboard();
	}

	@Test(groups="it")
	public void testOnBlur() {
		super.testOnBlur();
	}

	@Test(groups="it", expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}
	
	@Test(groups="it")
	public void testFindElements() {
		super.testFindElements();
	}

	@Test(groups="it")
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Test(groups="it")
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Test(groups="it")
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Test(groups="it")
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Test(groups="it")
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Test(groups="it")
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Test(groups="it")
	public void testFindHtmlElementsByWithSimilarElements() {
		super.testFindHtmlElementsByWithSimilarElements();
	}

	@Test(groups="it")
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Test(groups="it")
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Test(groups="it")
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Test(groups="it")
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Test(groups="it")
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Test(groups="it") 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Test(groups="it")
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Test(groups="it") 
	public void testDelay() {
		super.testDelay();
	}
	
	@Test(groups="it")
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Test(groups="it")
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Test(groups="it")
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Test(groups="it")
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}

	@Test(groups="it")
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Test(groups="it")
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Test(groups="it")
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}

	@Test(groups="it")
	public void testAutoScrolling() {
		super.testAutoScrolling();
	}
	
	@Test(groups= {"it-driver"})
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		super.testUploadFileWithRobot();
	}
	
	@Test(groups= {"it-driver"})
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		super.testUploadFileWithRobotKeyboard();
	}
	
	@Test(groups= "it")
	public void testUploadFile() throws AWTException, InterruptedException {
		super.testUploadFile();
	}
	
	@Test(groups="it")
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Test(groups="it")
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Test(groups="it")
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Test(groups="it")
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Test(groups="it")
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Test(groups="it")
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Test(groups="it")
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Test(groups="it")
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Test(groups="it")
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Test(groups="it")
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Test(groups="it")
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Test(groups="it")
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Test(groups="it")
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Test(groups="it")
	public void testIsElementPresentAndDisplayed() {
		super.testIsElementPresentAndDisplayed();
	}

	@Test(groups="it")
	public void testIsElementPresentAndDisplayedWithDelay() {
		super.testIsElementPresentAndDisplayedWithDelay();
	}

	@Test(groups="it")
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Test(groups="it")
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Test(groups="it")
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Test(groups="it")
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Test(groups="it")
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Test(groups="it")
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Test(groups="it")
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Test(groups="it")
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Test(groups="it")
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Test(groups= "it")
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}
}
