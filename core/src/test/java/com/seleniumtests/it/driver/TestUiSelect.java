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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestUiSelect extends GenericTest {
	
	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	public TestUiSelect() throws Exception {
	}
	
	public TestUiSelect(WebDriver driver, DriverTestPage testPage) throws Exception {
		TestUiSelect.driver = driver;
		TestUiSelect.testPage = testPage;
	}

	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterMethod(groups={"it"})
	public void cleanAlert() {
		try {
			driver.switchTo().alert().accept();
		} catch (WebDriverException e) {
			
		}
	}
	
	/*
	 * Test SelectList and MultipleSelectList
	 */
	@Test(groups={"it"})
	public void testIsTextSelect() {
			testPage.selectList.selectByText("option2");
			Assert.assertTrue(testPage.selectList.getSelectedText().equals("option2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleTextSelect() {
			testPage.selectMultipleList.deselectAll();
			String[] toSelect = {"option2", "option4"};
			testPage.selectMultipleList.selectByText(toSelect);
			Assert.assertTrue(toSelect[0].equals(testPage.selectMultipleList.getSelectedTexts()[0]));
			Assert.assertTrue(toSelect[1].equals(testPage.selectMultipleList.getSelectedTexts()[1]));
	}
	
	@Test(groups={"it"})
	public void testIsValueSelect() {
		testPage.selectList.selectByValue("opt2");
		Assert.assertTrue(testPage.selectList.getSelectedValue().equals("opt2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleValueSelect() {
			testPage.selectMultipleList.deselectAll();
			String[] toSelect = {"opt2", "opt4"};
			testPage.selectMultipleList.selectByValue(toSelect);
			Assert.assertTrue(toSelect[0].equals(testPage.selectMultipleList.getSelectedValues()[0]));
			Assert.assertTrue(toSelect[1].equals(testPage.selectMultipleList.getSelectedValues()[1]));
	}
	
	@Test(groups={"it"})
	public void testGetFirstSelectedOption() {
		testPage.selectMultipleList.deselectAll();
		String[] toSelect = {"opt2", "opt4"};
		testPage.selectMultipleList.selectByValue(toSelect);
		Assert.assertTrue("option2".equals(testPage.selectMultipleList.getFirstSelectedOption().getText()));
	}
	
	@Test(groups={"it"})
	public void testGetFirstSelectedOptionNoSelection() {
		testPage.selectMultipleList.deselectAll();
		Assert.assertNull(testPage.selectMultipleList.getFirstSelectedOption());
	}
	

	@Test(groups={"it"})
	public void testGetAllSelectedOptions() {
		testPage.selectMultipleList.deselectAll();
		String[] toSelect = {"opt2", "opt4"};
		testPage.selectMultipleList.selectByValue(toSelect);
		Assert.assertTrue("option2".equals(testPage.selectMultipleList.getAllSelectedOptions().get(0).getText()));
		Assert.assertTrue("option4".equals(testPage.selectMultipleList.getAllSelectedOptions().get(1).getText()));
	}
	
	
	@Test(groups={"it"})
	public void testIsCorrespondingTextSelect() {
			testPage.selectList.selectByCorrespondingText("option 2");
			Assert.assertTrue(testPage.selectList.getSelectedValue().equals("opt2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleCorrespondingTextSelect() {
			testPage.selectMultipleList.deselectAll();
			String[] toSelect = {"option 2", "opti4"};
			testPage.selectMultipleList.selectByCorrespondingText(toSelect);
			String[] toFind = {"opt2", "opt4"};
			Assert.assertTrue(toFind[0].equals(testPage.selectMultipleList.getSelectedValues()[0]));
			Assert.assertTrue(toFind[1].equals(testPage.selectMultipleList.getSelectedValues()[1]));
	}
	
	@Test(groups={"it"})
	public void testIsAllDeselected() {
		String[] toSelect = {"opt1", "opt2", "opt3", "opt4"};
		testPage.selectMultipleList.selectByCorrespondingText(toSelect);
		testPage.selectMultipleList.deselectAll();
		Assert.assertTrue(testPage.selectMultipleList.getSelectedValues().length == 0);
	}
	
	@Test(groups={"it"})
	public void testIsIndexSelect() {
		testPage.selectList.selectByIndex(1);
		Assert.assertTrue(testPage.selectList.getSelectedValue().equals("opt2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleIndexSelect() {
		testPage.selectMultipleList.deselectAll();
		int[] toSelect = {1, 2};
		testPage.selectMultipleList.selectByIndex(toSelect);
		Assert.assertTrue(testPage.selectMultipleList.getSelectedValues()[0].equals("opt2"));
		Assert.assertTrue(testPage.selectMultipleList.getSelectedValues()[1].equals("opt3"));
	}
	
	@Test(groups={"it"})
	public void testIsIndexDeselect() {
		testPage.selectMultipleList.deselectAll();
		testPage.selectMultipleList.selectByIndex(1);
		testPage.selectMultipleList.deselectByIndex(1);
		Assert.assertTrue(testPage.selectMultipleList.getSelectedValues().length == 0);
	}
	
	@Test(groups={"it"})
	public void testIsTextDeselect() {
		testPage.selectMultipleList.deselectAll();
		testPage.selectMultipleList.selectByText("option1");
		testPage.selectMultipleList.deselectByText("option1");
		Assert.assertTrue(testPage.selectMultipleList.getSelectedValues().length == 0);
	}
	
	@Test(groups={"it"})
	public void testIsValueDeselect() {
		testPage.selectMultipleList.deselectAll();
		testPage.selectMultipleList.selectByValue("opt1");
		testPage.selectMultipleList.deselectByValue("opt1");
		Assert.assertTrue(testPage.selectMultipleList.getSelectedValues().length == 0);
	}	
	
	// test of select as UL/LI lists
	@Test(groups={"it"})
	public void testSelectUlList() {
		testPage.ulliListTrigger.click();
		testPage.selectUlLiList.selectByText("English");
		Assert.assertEquals(testPage.ulliListTrigger.getValue(), "English");
	}
	
	@Test(groups={"it"})
	public void testInputInSelectUlList() {
		testPage.ulliListTrigger.click();
		
		// should not raise any exception
		testPage.textInselectUlLiList.sendKeys("text");
	}
}
