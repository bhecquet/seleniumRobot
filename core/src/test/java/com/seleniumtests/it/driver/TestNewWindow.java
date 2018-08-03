/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestNewWindow extends GenericTest {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}

	@AfterClass(groups={"it"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
		WebUIDriver.cleanUpWebUIDriver();
	}
	
	/**
	 * Click on link that opens a new window
	 * Then search an element into this window, close it and search an element into main window
	 */  
	@Test(groups={"it"})
	public void clickLink() {

		String mainHandle = null;
		try {
			DriverTestPage.link.click();
		
			// go to new opened window
			mainHandle = testPage.selectNewWindow();
			Assert.assertEquals("a value", driver.findElement(By.id("textInIFrameWithValue")).getAttribute("value"));
		} finally {
			// go back to main window
			if (driver.getWindowHandles().size() > 1) {
				driver.close();
				if (mainHandle != null) {
					testPage.selectWindow(mainHandle);
				}
			}
		}
		Assert.assertTrue(DriverTestPage.link.getUrl().contains("testIFrame.html"));
	}
	
	/**
	 * test correction of bug #47 where error is raised when closing window through a click
	 * @throws Exception 
	 */
	@Test(groups={"it"})
	public void testClosingWindow() throws Exception {
		String mainHandle = null;
		
		
		try {
			DriverTestPage.link.click();
			
		
			// go to new opened window
			mainHandle = testPage.selectNewWindow();
			new DriverSubTestPage(false); // check we are on the page
			DriverSubTestPage.closeButton.click();
			testPage.selectWindow(mainHandle);
		} finally {
			// go back to main window
			if (driver.getWindowHandles().size() > 1) {
				driver.close();
				if (mainHandle != null) {
					testPage.selectWindow(mainHandle);
				}
			}
		}
	}
}
