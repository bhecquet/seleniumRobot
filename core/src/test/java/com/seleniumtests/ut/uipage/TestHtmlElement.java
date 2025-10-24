/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
	
	@Override
    @Test(groups={"ut"})
	public void testClickActionCheckbox() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testAutoScrolling() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testClickActionRadio() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testDoubleClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testUploadFileWithRobot() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testUploadFileWithRobotKeyboard() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"ut"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}

	@Override
	@Test(groups={"ut"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Override
    @Test(groups={"ut"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Override
    @Test(groups={"ut"})
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Override
    @Test(groups={"ut"})
	public void testOnBlur() {
		super.testOnBlur();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindElements() {
		super.testFindElements();
	}

	@Override
	@Test(groups={"ut"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Override
    @Test(groups={"ut"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindHtmlElementsByWithSimilarElements() {
		super.testFindHtmlElementsByWithSimilarElements();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Override
    @Test(groups={"ut"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Override
    @Test(groups={"ut"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Override
    @Test(groups={"ut"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Override
    @Test(groups={"ut"})
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}

	@Override
	@Test(groups= {"ut"})
	public void testUploadFile() throws AWTException, InterruptedException {
		super.testUploadFile();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Override
    @Test(groups={"ut"})
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testIsElementPresentAndDisplayed() {
		super.testIsElementPresentAndDisplayed();
	}

	@Override
    @Test(groups={"ut"})
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Override
    @Test(groups={"ut"})
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Override
    @Test(groups={"ut"})
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Override
    @Test(groups={"ut"})
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Override
    @Test(groups={"ut"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Override
	@Test(groups= {"ut"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}
	
	@Test(groups= {"nogroup"})
	public void test() {
		super.testTextElementInsideHtmlElementIsPresent();
	}

}
