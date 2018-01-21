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
package com.seleniumtests.ut.uipage.htmlelements;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestTableWithDriver extends GenericDriverTest {
	
	private DriverTestPage testPage;

	@BeforeMethod(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		testPage = new DriverTestPage(true);
	}
	
	/* 
	 * the following 2 tests has been written to show a bug when a Table is reused among 2 test cases
	 * In the second test case, if rows are not refreshed explicitly, the reference the old driver instance,
	 * which is closed after the first test, is used
	 * We get: org.openqa.selenium.remote.SessionNotFoundException: Session is closed
	 */
	@Test(groups={"ut"})
	public void testTableRefresh1() {
		Assert.assertEquals(testPage.table.getRows().get(1).findElements(By.tagName("td")).size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testTableRefresh2() {
		Assert.assertEquals(testPage.table.getRows().get(1).findElements(By.tagName("td")).size(), 2);
	}
}
