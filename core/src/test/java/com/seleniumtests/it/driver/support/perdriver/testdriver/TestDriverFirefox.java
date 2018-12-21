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

import org.openqa.selenium.UnhandledAlertException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverFirefox extends TestDriver {

	public TestDriverFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}
	@Test(groups={"it", "ut"})
	public void testAlertDisplay() {
		super.testAlertDisplay();
	}
	
	@Test(groups={"it", "ut"}, expectedExceptions=UnhandledAlertException.class, enabled=false)
	public void testFindWithAlert() {
		super.testFindWithAlert();
	}
   
	@Test(groups={"it", "ut"})
	public void testClickDiv() {
		super.testClickDiv();
	}
   
	@Test(groups={"it", "ut"})
	public void testClickRadio() {
		super.testClickRadio();
	}
   
	@Test(groups={"it", "ut"})
	public void testClickCheckBox() {
		super.testClickCheckBox();
	}
  
	@Test(groups={"it", "ut"})
	public void testClickJsDiv() {
		super.testClickJsDiv();
	}
   
	@Test(groups={"it", "ut"})
	public void testClickJsRadio() {
		super.testClickJsRadio();
	}
   
	@Test(groups={"it", "ut"})
	public void testClickJsCheckbox() {
		super.testClickJsCheckbox();
	}
	
	@Test(groups={"it", "ut"})
	public void testClickActionDiv() {
		super.testClickActionDiv();
	}
	
	@Test(groups={"it", "ut"})
	public void testDoubleClickActionDiv() {
		super.testDoubleClickActionDiv();
	}
	
	@Test(groups={"it", "ut"})
	public void testClickActionRadio() {
		super.testClickActionRadio();
	}
	
	@Test(groups={"it", "ut"})
	public void testClickActionCheckbox() {
		super.testClickActionCheckbox();
	}
   
	@Test(groups={"it", "ut"})
	public void testSendKeys() {
		super.testSendKeys();
	}
   
	@Test(groups={"it", "ut"})
	public void testSendKeysJs() {
		super.testSendKeysJs();
	}

	@Test(groups={"it", "ut"})
	public void testOnBlur() {
		super.testOnBlur();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindElements() {
		super.testFindElements();
	}

	@Test(groups={"it", "ut"})
	public void testFindSubElement() {
		super.testFindSubElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindNthSubElement() {
		super.testFindNthSubElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindNthElement() {
		super.testFindNthElement();
	}

	@Test(groups={"it", "ut"})
	public void testFindPattern1() {
		super.testFindPattern1();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindPattern2() {
		super.testFindPattern2();
	}
	
	@Test(groups={"it", "ut"}) 
	public void testFindPattern3() {
		super.testFindPattern3();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindPattern4() {
		super.testFindPattern4();
	}
	
	@Test(groups={"it", "ut"}) 
	public void testDelay() {
		super.testDelay();
	}
	
	@Test(groups={"it", "ut"})
	public void testHiddenElement() {
		super.testHiddenElement(); 
	}
	
	@Test(groups={"it", "ut"})
	public void testWebDriverWaitWithLowTimeout() {
		super.testWebDriverWaitWithLowTimeout();
	}
	
	@Test(groups={"it", "ut"})
	public void testSearchDoneSeveralTimes() {
		super.testSearchDoneSeveralTimes();
	}
	
	@Test(groups={"it", "ut"})
	public void testIsElementPresent1() {
		super.testIsElementPresent1();
	}

	@Test(groups={"it", "ut"})
	public void testAutoScrolling() {
		super.testAutoScrolling();
	}
	
	@Test(groups= {"it", "ut"})
	public void testUploadFileWithRobot() throws AWTException, InterruptedException {
		super.testUploadFileWithRobot();
	}
	
	@Test(groups= {"it", "ut"})
	public void testUploadFileWithRobotKeyboard() throws AWTException, InterruptedException {
		super.testUploadFileWithRobotKeyboard();
	}
	
	@Test(groups= {"it", "ut"})
	public void testUploadFile() throws AWTException, InterruptedException {
		super.testUploadFile();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindFirstElement() {
		super.testFindFirstElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindFirstVisibleElement() {
		super.testFindFirstVisibleElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindFirstElementWithParent() {
		super.testFindFirstElementWithParent();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindFirstVisibleElementWithParent() {
		super.testFindFirstVisibleElementWithParent();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindElementsUnderAnOtherElement() {
		super.testFindElementsUnderAnOtherElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindElementsInsideParent() {
		super.testFindElementsInsideParent();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindLastElement() {
		super.testFindLastElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindSubElementByXpath() {
		super.testFindSubElementByXpath();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindSubElementByRelativeXpath() {
		super.testFindSubElementByRelativeXpath();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindElementByXpath() {
		super.testFindElementByXpath();
	}
	
	@Test(groups={"it", "ut"})
	public void testIsElementPresent() {
		super.testIsElementPresent();
	}
	
	@Test(groups={"it", "ut"})
	public void testIsElementNotPresent() {
		super.testIsElementNotPresent();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTextElementInsideHtmlElement() {
		super.testFindTextElementInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindRadioElementInsideHtmlElement() {
		super.testFindRadioElementInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindCheckElementInsideHtmlElement() {
		super.testFindCheckElementInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindButtonElementInsideHtmlElement() {
		super.testFindButtonElementInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindLinkElementInsideHtmlElement() {
		super.testFindLinkElementInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindSelectElementInsideHtmlElement() {
		super.testFindSelectElementInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTableInsideHtmlElement() {
		super.testFindTableInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTextElementsInsideHtmlElement() {
		super.testFindTextElementsInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindRadioElementsInsideHtmlElement() {
		super.testFindRadioElementsInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindCheckElementsInsideHtmlElement() {
		super.testFindCheckElementsInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindButtonElementsInsideHtmlElement() {
		super.testFindButtonElementsInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindLinkElementsInsideHtmlElement() {
		super.testFindLinkElementsInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindSelectElementsInsideHtmlElement() {
		super.testFindSelectElementsInsideHtmlElement();
	}
	
	@Test(groups={"it", "ut"})
	public void testFindTablesInsideHtmlElement() {
		super.testFindTablesInsideHtmlElement();
	}

}
