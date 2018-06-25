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
package com.seleniumtests.ut.uipage;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;

public class TestCachedHtmlElement extends GenericTest {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(groups={"ut"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testGetText() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getText(), "option1 option2 option numero 3");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClick() {
		new CachedHtmlElement(testPage.selectList.getElement()).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSubmit() {
		new CachedHtmlElement(testPage.selectList.getElement()).submit();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeys() {
		new CachedHtmlElement(testPage.textElement.getElement()).sendKeys("foo");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClear() {
		new CachedHtmlElement(testPage.textElement.getElement()).clear();
	}

	@Test(groups={"ut"})
	public void testTagName() {
		Assert.assertEquals(new CachedHtmlElement(testPage.selectList.getElement()).getTagName(), "select");
	}
	
	
	
}
