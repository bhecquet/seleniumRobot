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
package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestUiActions extends GenericMultiBrowserTest {

	public TestUiActions(BrowserType browserType) {
		super(browserType, "DriverTestPage"); 
	}
	
	public TestUiActions() throws Exception {
		super(null, "DriverTestPage");
	}

	/**
	 * Test composite action with standard selenium syntax and native driver
	 */
	
	public void testNewAction() {
		try {
			new Actions(driver.getWebDriver()).moveToElement(driver.findElement(By.id("carre2"))).click().build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "coucou");
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "");
		}
	}

	/**
	 * Test composite action with seleniumRobot syntax (HtmlElement) and native driver
	 */
	
	public void testNewActionWithHtmlElement() {
		try {
			new Actions(driver.getWebDriver()).moveToElement(DriverTestPage.redSquare).click().build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "coucou");
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "");
		}
	}

	/**
	 * Test composite action with standard selenium syntax and custom driver
	 */
	
	public void testMoveClick() {
		try {
			new Actions(driver).moveToElement(driver.findElement(By.id("carre2"))).click().build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "coucou");
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "");
		}
	}
	
	
	public void testSendKeys() {
		try {
			new Actions(driver).moveToElement(driver.findElement(By.id("text2"))).click().sendKeys("youpi").build().perform();
			Assert.assertEquals(driver.findElement(By.id("text2")).getDomProperty("value"), "youpi");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}
	
	
	public void testSendKeysWithHtmlElement() {
		try {
			new Actions(driver).moveToElement(DriverTestPage.textElement).click().sendKeys("youpi2").build().perform();
			Assert.assertEquals(DriverTestPage.textElement.getDomProperty("value"), "youpi2");
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}
	
	
	public void testSendKeysWithHtmlElementNotPresent() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		try {
			new Actions(driver).moveToElement(DriverTestPage.textElementNotPresent).click().sendKeys("youpi2").build().perform();
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}
}
