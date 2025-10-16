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
package com.seleniumtests.it.driver.support.perdriver.htmlunit;

import java.awt.AWTException;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverHtmlUnit extends TestDriver {

	public TestDriverHtmlUnit() throws Exception {
		super(BrowserType.HTMLUNIT);
	}
	
	@Override
    @Test(groups={"it"})
	public void testClickActionCheckbox() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testAutoScrolling() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testClickActionRadio() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testDoubleClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testUploadFileWithRobot() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testUploadFileWithRobotKeyboard() {
		// skip as htmlunit does not support it
	}

	@Override
    @Test(groups={"it"})
	public void testSendKeysKeyboard() {
		// skip => not supported
	}

	@Override
    @Test(groups={"it"})
	public void testClickWithMouse() {
		// skip => not supported
	}
	
	@Override
    @Test(groups={"it"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Override
    @Test(groups={"it"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Override
    @Test(groups={"it"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Override
    @Test(groups={"it"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Override
    @Test(groups={"it"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Override
    @Test(groups={"it"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Override
    @Test(groups={"it"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
   
	@Override
    @Test(groups={"it"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Override
    @Test(groups={"it"})
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Override
    @Test(groups={"it"})
	public void testOnBlur() {
		super.testOnBlur();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindElements() {
		super.testFindElements();
	}

	@Override
    @Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}

	@Override
    @Test(groups={"it"})
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Override
    @Test(groups={"it"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Override
    @Test(groups={"it"})
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Override
    @Test(groups={"it"})
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Override
    @Test(groups={"it"})
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Override
    @Test(groups={"it"})
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Override
    @Test(groups={"it"})
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
    @Test(groups={"it"})
	public void testActionDelay() {
		super.testActionDelay();
	}
	
	@Override
    @Test(groups={"it"})
	public void testActionNoDelay() {
		super.testActionNoDelay();
	}
	
	@Override
    @Test(groups={"it"})
	public void testActionNoDelay2() {
		super.testActionNoDelay2();
	}

	@Override
    @Test(groups={"it"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Override
    @Test(groups={"it"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Override
    @Test(groups={"it"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Override
    @Test(groups={"it"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Override
    @Test(groups={"it"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Override
    @Test(groups={"it"})
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Override
    @Test(groups={"it"})
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Override
    @Test(groups={"it"})
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}
	
	@Override
    @Test(groups={"it"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}
	
	@Override
    @Test(groups= {"it"})
	public void testUploadFile() throws AWTException, InterruptedException {
		super.testUploadFile();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Override
    @Test(groups={"it"})
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Override
    @Test(groups={"it"})
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
    @Test(groups={"it"})
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Override
    @Test(groups={"it"})
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Override
    @Test(groups={"it"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Override
    @Test(groups={"it"})
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"it"})
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Override
    @Test(groups={"it"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Override
    @Test(groups= {"it"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}


}
