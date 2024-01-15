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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions;

public class TestNativeSeleniumActions extends GenericMultiBrowserTest {
	
	public TestNativeSeleniumActions(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestNativeSeleniumActions() throws Exception {
		super(BrowserType.INTERNET_EXPLORER, "DriverTestPageNativeActions");  
	}

	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void reset() {
		if (driver != null && WebUIDriver.getWebDriver(false) != null) {
			DriverTestPageNativeActions.textElement.clear();
			((CustomEventFiringWebDriver)driver).scrollTop();
		}
	}
	
	/**
	 * issue #228: check that searching a non existing element inside frame does not lose frame focus
	 */
	@Test(groups={"it"})
	public void testIsTextSelect() {

		testPageNativeActions.switchToFirstFrameByNameOrId();
		testPageNativeActions.switchToSubFrame();
		testPageNativeActions.getElementInsideFrameOfFrame();
		try {
			testPageNativeActions.getDriver().findElement(By.id("foobar"));
		} catch (Exception e) {
			
		}
		testPageNativeActions.getElementInsideFrameOfFrame();
	}
	
	/**
	 * issue #275: check that with a frame selected in one test (currentFrame inside {@link com.seleniumtests.uipage.aspects.SeleniumNativeActions} aspect), this 
	 * variable is reset for next step
	 * @throws Exception
	 */
	@Test(groups={"it"}, dependsOnMethods="testIsTextSelect")
	public void testDriverNativeActionsWithFrame1() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		testPageNativeActions.switchToFirstFrameByIndex();
		
		// close driver so that it can be recreated in testDriverNativeActionsWithFrame2(). This is the way real tests will execute, one driver for each test
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"it"}, dependsOnMethods="testDriverNativeActionsWithFrame1")
	public void testDriverNativeActionsWithFrame2() throws Exception {
		
		// create the driver that as been closed in previous test method
		testPageNativeActions = new DriverTestPageNativeActions(true, testPageUrl);
		driver = WebUIDriver.getWebDriver(true);
		
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		testPageNativeActions.sendKeys()
							.reset();
	}
	
}
