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
package com.seleniumtests.ut.uipage;

import java.awt.AWTException;

import com.seleniumtests.CaptureVideo;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

@CaptureVideo(enabled = false)
public class TestHtmlElement extends TestDriver {

	public TestHtmlElement() throws Exception {
		super(BrowserType.HTMLUNIT);
	}
	
	@Test(groups={"ut"})
	public void testClickActionCheckbox() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testAutoScrolling() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testClickActionRadio() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testDoubleClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testUploadFileWithRobot() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testUploadFileWithRobotKeyboard() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Test(groups={"ut"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Test(groups={"ut"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Test(groups={"ut"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Test(groups={"ut"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Test(groups={"ut"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}
	
	@Test(groups={"ut"})
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Test(groups={"ut"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Test(groups={"ut"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
   
	@Test(groups={"ut"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Test(groups={"ut"})
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Test(groups={"ut"})
	public void testOnBlur() {
		super.testOnBlur();
	}
	
	@Test(groups={"ut"})
	public void testFindElements() {
		super.testFindElements();
	}

	@Test(groups={"ut"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}

	@Test(groups={"ut"})
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Test(groups={"ut"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Test(groups={"ut"})
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Test(groups={"ut"})
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Test(groups={"ut"})
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Test(groups={"ut"})
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Test(groups={"ut"})
	public void testFindHtmlElementsByWithSimilarElements() {
		super.testFindHtmlElementsByWithSimilarElements();
	}

	@Test(groups={"ut"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Test(groups={"ut"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Test(groups={"ut"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Test(groups={"ut"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Test(groups={"ut"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Test(groups={"ut"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Test(groups={"ut"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Test(groups={"ut"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Test(groups={"ut"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Test(groups={"ut"})
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Test(groups={"ut"})
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Test(groups={"ut"})
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}
	
	@Test(groups={"ut"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Test(groups={"ut"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}
	
	@Test(groups= {"ut"})
	public void testUploadFile() throws AWTException, InterruptedException {
		super.testUploadFile();
	}
	
	@Test(groups={"ut"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Test(groups={"ut"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Test(groups={"ut"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Test(groups={"ut"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Test(groups={"ut"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Test(groups={"ut"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Test(groups={"ut"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Test(groups={"ut"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Test(groups={"ut"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Test(groups={"ut"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Test(groups={"ut"})
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresentAndDisplayed() {
		super.testIsElementPresentAndDisplayed();
	}

	@Test(groups={"ut"})
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Test(groups={"ut"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Test(groups={"ut"})
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Test(groups={"ut"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Test(groups={"ut"})
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Test(groups={"ut"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Test(groups={"ut"})
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Test(groups={"ut"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Test(groups= {"ut"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}
	
	@Test(groups= {"nogroup"})
	public void test() {
		super.testTextElementInsideHtmlElementIsPresent();
	}

}
