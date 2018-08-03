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

import java.util.regex.Pattern;

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

public class TestTable extends GenericTest {

	@BeforeClass(groups={"it"})
	public void initPage(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		new DriverTestPage(true);
	}
		

	@AfterClass(groups={"it"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
		WebUIDriver.cleanUpWebUIDriver();
	}
	
	
	@Test(groups={"it"})
	public void testGetContent() {
		Assert.assertEquals(DriverTestPage.table.getContent(0, 0), "Id");
	}
	
	@Test(groups={"it"}, expectedExceptions=ScenarioException.class)
	public void testGetContentEmptyTable() {
		DriverTestPage.emptyTable.getContent(0, 0);
	}
	
	@Test(groups={"it"}, expectedExceptions=IndexOutOfBoundsException.class)
	public void testGetContentOutsideRange() {
		Assert.assertEquals(DriverTestPage.table.getContent(10, 0), "Id");
	}
	
	@Test(groups={"it"})
	public void testGetCellFromContent() {
		Assert.assertEquals(DriverTestPage.table.getCellFromContent(Pattern.compile("Jav.*"), 1).getText(), "Java");
	}
	
	@Test(groups={"it"}, expectedExceptions=ScenarioException.class)
	public void testGetCellFromWrongContent() {
		DriverTestPage.table.getCellFromContent(Pattern.compile("Jai.*"), 1).getText();
	}
	
	@Test(groups={"it"})
	public void testRowCount() {
		Assert.assertEquals(DriverTestPage.table.getRowCount(), 3);
	}
	
	@Test(groups={"it"})
	public void testRowCountEmptyTable() {
		Assert.assertEquals(DriverTestPage.emptyTable.getRowCount(), 0);
	}
	
	@Test(groups={"it"})
	public void testColCount() {
		Assert.assertEquals(DriverTestPage.table.getColumnCount(), 2);
	}
	
}
