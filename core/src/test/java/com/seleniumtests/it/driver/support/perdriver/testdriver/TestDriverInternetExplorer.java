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
import java.awt.event.KeyEvent;
import java.util.Arrays;

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
	
	@Test(groups= {"ie"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Test(groups= {"ie"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Test(groups= {"ie"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Test(groups= {"ie"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Test(groups= {"ie"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Test(groups= {"ie"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}

	@Test(groups= {"ie"})
	public void testDoubleClickDiv() {
		super.testDoubleClickDiv();
	}
   
	@Test(groups= {"ie"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Test(groups= {"ie"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
	
	@Test(groups= {"ie"})
	public void testClickActionDiv() {
		super.testClickActionDiv();
	}
	
	@Test(groups= {"ie"})
	public void testDoubleClickActionDiv() {
		super.testDoubleClickActionDiv();
	}
	
	@Test(groups= {"ie"})
	public void testClickActionRadio() {
		super.testClickActionRadio();
	}
	
	@Test(groups= {"ie"})
	public void testClickActionCheckbox() {
		super.testClickActionCheckbox();
	}

	@Test(groups={"it"})
	public void testClickWithMouse() {
		super.testClickWithMouse();
	}
   
	@Test(groups= {"ie"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Test(groups= {"ie"})
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Test(groups={"it"})
	public void testSendKeysKeyboard() {
		super.testSendKeysKeyboard();
	}

	@Test(groups= {"ie"})
	public void testOnBlur() {
		super.testOnBlur();
	}
	
	@Test(groups= {"ie"})
	public void testFindElements() {
		super.testFindElements();
	}
	
	@Test(groups= {"ie"}, expectedExceptions = NoSuchElementException.class)
	public void testElementNotPresent() {
		super.testElementNotPresent();
	}

	@Test(groups= {"ie"})
	public void testFindElementsNotExist() {
		super.testFindElementsNotExist();
	}
	@Test(groups= {"ie"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}

	@Test(groups= {"ie"})
	public void testFindElementsByNotExist() {
		super.testFindElementsByNotExist();
	}

	@Test(groups= {"ie"})
	public void testFindHtmlElementsBy() {
		super.testFindHtmlElementsBy();
	}

	@Test(groups= {"ie"})
	public void testFindHtmlElementsByNotExist() {
		super.testFindHtmlElementsByNotExist();
	}

	@Test(groups= {"ie"})
	public void testFindHtmlElementsByInsideFrame() {
		super.testFindHtmlElementsByInsideFrame();
	}

	@Test(groups= {"ie"})
	public void testFindHtmlElementsByWithSimilarElements() {
		super.testFindHtmlElementsByWithSimilarElements();
	}

	@Test(groups= {"ie"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Test(groups= {"ie"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Test(groups= {"ie"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Test(groups= {"ie"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Test(groups= {"ie"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Test(groups= {"ie"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Test(groups= {"ie"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Test(groups= {"ie"})
	public void testHiddenElementByDisplay() { 
		super.testHiddenElementByDisplay();
	}
	
	@Test(groups= {"ie"})
	public void testHiddenElementByOpacity() { 
		super.testHiddenElementByOpacity();
	}
	
	@Test(groups= {"ie"})
	public void testHiddenElementByVisibility() { 
		super.testHiddenElementByVisibility();
	}

	@Test(groups= {"ie"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Test(groups= {"ie"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}

	@Test(groups= {"ie"})
	public void testAutoScrolling() {
		super.testAutoScrolling();
	}
	
	@Test(groups= {"ie"})
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		super.testUploadFileWithRobot();
	}
	
	@Test(groups= {"ie"})
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		super.testUploadFileWithRobotKeyboard();
	}
	
	/**
	 * Retry file upload if something goes wrong
	 * @param testNGCtx
	 * @throws Exception
	 */
	@Test(groups= {"ie"})
	public void testUploadFile() throws AWTException, InterruptedException {
		try {
			super.testUploadFile();
		} catch (Throwable e) {
			logger.warn("test upload failed and retried due to session timeout exception");
			CustomEventFiringWebDriver.sendKeysToDesktop(Arrays.asList(KeyEvent.VK_ESCAPE), DriverMode.LOCAL, null);
//			try {
//				stop();
//				exposeTestPage(testNGCtx);
//			} catch (Exception e1) {
//				throw e;
//			}
			super.testUploadFile();
		}
		
	}
	
	@Test(groups= {"ie"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Test(groups= {"ie"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Test(groups= {"ie"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Test(groups= {"ie"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Test(groups= {"ie"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Test(groups= {"ie"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}

	@Test(groups= {"ie"})
	public void testIsElementNotPresentWithIndex() {
		super.testIsElementNotPresentWithIndex();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementPresentAndNotDisplayedWithIndex() {
		super.testIsElementPresentAndNotDisplayedWithIndex();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementPresentAndDisplayed() {
		super.testIsElementPresentAndDisplayed();
	}

	@Test(groups= {"ie"})
	public void testIsElementNotPresentAndNotDisplayed() {
		super.testIsElementNotPresentAndNotDisplayed();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementPresentAndNotDisplayed() {
		super.testIsElementPresentAndNotDisplayed();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}

	@Test(groups= {"ie"})
	public void testTextElementInsideHtmlElementIsPresent() {
		super.testTextElementInsideHtmlElementIsPresent();
	}
	
	@Test(groups= {"ie"})
	public void testTextElementInsideHtmlElementIsNotPresent() {
		super.testTextElementInsideHtmlElementIsNotPresent();
	}

	@Test(groups= {"ie"})
	public void testElementWithIFrameAbsent() {
		super.testElementWithIFrameAbsent();
	}
	
	@Test(groups= {"ie"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}
	
	@Test(groups= {"ie"})
	public void testScrollIntoDiv() {
		super.testScrollIntoDiv();
	}

	@Test(groups= {"ie"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}

	@Test(groups= {"ie"})
	public void testCaptureWhenWindowIsClosed() throws Exception {
		super.testCaptureWhenWindowIsClosed();
	}
}
