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
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPageShadowDom;
import com.seleniumtests.uipage.ByC;

public class TestShadowDom extends GenericTest {
	

	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		setBrowser();
		new DriverTestPageShadowDom(true);
		WebUIDriver.getWebDriver(true);
	}
	
	@BeforeMethod(groups={"it"})
	public void initTimeouts(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);
	}
 
	public void setBrowser() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
	}

	/**
	 * Check elements in shadow DOM cannot be accessed
	 */
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testAccessShadowElementDirectly() {
		DriverTestPageShadowDom.divPass1Direct.getText();
	}
	
	@Test(groups={"it"}) 
	public void testAccessShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass1Shadow.getText(), "This is a div with an ID of pass1Shadow");
	}
	
	
	@Test(groups={"it"})
	public void testAccessSubShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass1Shadow.getText(), "This is a div with an ID of pass1Shadow");
	}
	
	@Test(groups={"it"})
	public void testAccessMultipleSubShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divPass3MultipleShadow.getText(), "This is a DIV with the ID of pass3Shadow");
	}
	
	/**
	 * Shadow elements are located through the same locators, check that the first one is used
	 */
	@Test(groups={"it"})
	public void testAccessMultipleSameSubShadowElement() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(ByC.xTagName("div")).getText(), "This is a DIV with the ID of pass71Shadow");
	}
	
	/**
	 * When element is not found under a shadow root
	 */
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testAccessMultipleSameSubShadowElementNotFound() {
		Assert.assertEquals(DriverTestPageShadowDom.divMultipleShadowElements.findElement(By.tagName("span")).getText(), "This is a DIV with the ID of pass71Shadow");
	}

	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class)
	public void testAccessShadowElementNotFound() {
		DriverTestPageShadowDom.shadowElementNotFound.getText();
	}
	
	
	@Test(groups={"it"})
	public void testAccessShadowElementInScroll() {

		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(30);
		DriverTestPageShadowDom.textInScroll.sendKeys("hello!");
		Assert.assertEquals(DriverTestPageShadowDom.textInScroll.getValue(), "hello!");
	}
}
