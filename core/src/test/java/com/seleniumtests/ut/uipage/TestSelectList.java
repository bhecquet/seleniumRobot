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
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.TestUiSelect;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestSelectList extends GenericTest {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	private static TestUiSelect testSelectIt;
	
	@BeforeClass(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		testSelectIt = new TestUiSelect(driver, testPage);
	}
	
	@AfterClass(groups={"ut"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
		WebUIDriver.cleanUpWebUIDriver();
	}
	
	@Test(groups={"ut"})
	public void testSelect() {
		testSelectIt.testIsTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultiple() {
		testSelectIt.testIsMultipleTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectValue() {
		testSelectIt.testIsValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleValue() {
		testSelectIt.testIsMultipleValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectCorresponding() {
		testSelectIt.testIsCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleCorresponding() {
		testSelectIt.testIsMultipleCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectIndex() {
		testSelectIt.testIsIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleIndex() {
		testSelectIt.testIsMultipleIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectAll() {
		testSelectIt.testIsAllDeselected();
	}

	
	@Test(groups={"ut"})
	public void testDeselectIndex() {
		testSelectIt.testIsIndexDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectText() {
		testSelectIt.testIsTextDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectValue() {
		testSelectIt.testIsValueDeselect();
	}
	

	@Test(groups={"ut"})
	public void testIsMultipleValueSelect() {
		testSelectIt.testIsMultipleValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testGetFirstSelectedOption() {
		testSelectIt.testGetFirstSelectedOption();
	}
	
	@Test(groups={"ut"})
	public void testGetFirstSelectedOptionNoSelection() {
		testSelectIt.testGetFirstSelectedOptionNoSelection();
	}
	
	
}
