/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestUiActions extends GenericTest {
	
	private WebDriver driver;
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		driver = WebUIDriver.getWebDriver(true);
		if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			driver.get("file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile());
		} else {
			driver.get("file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile());
		}
	}
	
	/**
	 * Test with the real driver, it should try to use new actions
	 * TODO: this test takes 30 secs because new actions are not currently (3.4.0) supported by chromedriver, so we fall back to old behaviour
	 * @throws Exception 
	 */
	@Test(groups={"it"}, enabled=false)
	public void testNewAction() {
		try {
			new Actions(((CustomEventFiringWebDriver)driver).getWebDriver()).moveToElement(driver.findElement(By.id("carre2"))).click().build().perform();
			Assert.assertEquals("coucou", driver.findElement(By.id("text2")).getAttribute("value"));
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals("", driver.findElement(By.id("text2")).getAttribute("value"));
		}
	}
	
	/**
	 * TODO: same as test above
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=false)
	public void testNewActionWithHtmlElement() throws Exception {
		try {
			DriverTestPage testPage = new DriverTestPage(false);
			new Actions(((CustomEventFiringWebDriver)driver).getWebDriver()).moveToElement(testPage.redSquare).click().build().perform();
			Assert.assertEquals("coucou", driver.findElement(By.id("text2")).getAttribute("value"));
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals("", driver.findElement(By.id("text2")).getAttribute("value"));
		}
	}

	@Test(groups={"it"})
	public void testMoveClick() {
		try {
			new Actions(driver).moveToElement(driver.findElement(By.id("carre2"))).click().build().perform();
			Assert.assertEquals("coucou", driver.findElement(By.id("text2")).getAttribute("value"));
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals("", driver.findElement(By.id("text2")).getAttribute("value"));
		}
	}
	
	@Test(groups={"it"}) 
	public void testSendKeys() {
		try {
			new Actions(driver).moveToElement(driver.findElement(By.id("text2"))).click().sendKeys("youpi").build().perform();
			Assert.assertEquals("youpi", driver.findElement(By.id("text2")).getAttribute("value"));
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}
	
	@Test(groups={"it"}) 
	public void testSendKeysWithHtmlElement() throws Exception {
		DriverTestPage testPage = new DriverTestPage(false);
		try {
			new Actions(driver).moveToElement(testPage.textElement).click().sendKeys("youpi2").build().perform();
			Assert.assertEquals("youpi2", testPage.textElement.getAttribute("value"));
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}
	
	@AfterClass(groups={"it"})
	public void destroyDriver() {
		if (driver != null) {
			WebUIDriver.cleanUp();
			WebUIDriver.cleanUpWebUIDriver();
		}
	}
}
