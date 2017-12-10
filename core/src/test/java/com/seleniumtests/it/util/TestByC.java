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
package com.seleniumtests.it.util;

import java.awt.AWTException;
import java.io.File;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;
import com.seleniumtests.util.helper.WaitHelper;

public class TestByC extends GenericTest {
	
	

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	public TestByC() throws Exception {
	}
	
	public TestByC(WebDriver driver, DriverTestPage testPage) throws Exception {
		TestByC.driver = driver;
		TestByC.testPage = testPage;
	}
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	
	@AfterClass(groups={"it"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelForward() {
		new TextFieldElement("", ByC.labelForward("By id forward", "input")).sendKeys("element found by label");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by label");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelForwardWithoutTagName() {
		new TextFieldElement("", ByC.labelForward("By id forward")).sendKeys("element found by label without tagname");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by label without tagname");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByLabelForward() {
		Assert.assertTrue(new TextFieldElement("", ByC.labelForward("By id forward", "input")).findElements().size() > 3);
	}

	@Test(groups={"it"})
	public void testFindElementByPartialLabelForward() {
		new TextFieldElement("", ByC.partialLabelForward("By id for", "input")).sendKeys("element found by partial label");
		Assert.assertEquals(testPage.textSelectedId.getValue(), "element found by partial label");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelBackward() {
		new TextFieldElement("", ByC.labelBackward("By id backward", "input")).sendKeys("element found by label backward");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by label backward");
	}
	
	@Test(groups={"it"})
	public void testFindElementByLabelBackwardWithoutTagName() {
		new TextFieldElement("", ByC.labelBackward("By id backward")).sendKeys("element found by label backward without tagname");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by label backward without tagname");
	}
	
	@Test(groups={"it"})
	public void testFindElementsByLabelBackward() {
		Assert.assertTrue(new TextFieldElement("", ByC.labelBackward("By id backward", "input")).findElements().size() > 3);
	}

	@Test(groups={"it"})
	public void testFindElementByPartialLabelBackward() {
		new TextFieldElement("", ByC.partialLabelBackward("By id back", "input")).sendKeys("element found by partial label backward");
		Assert.assertEquals(testPage.textSelectedText.getValue(), "element found by partial label backward");
	}
	
	
}
