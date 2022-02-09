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
	
//	public TestUiSelect()  {
//	}
//	
//	public TestUiSelect(WebDriver driver, DriverTestPage testPage) throws Exception {
//		TestUiSelect.driver = driver;
//		TestUiSelect.testPage = testPage;
//	}

	@BeforeClass(groups={"it", "ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		setBrowser();
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}

	public void setBrowser() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
	}
	
	@AfterMethod(groups={"it", "ut"}, alwaysRun=true)
	public void cleanAlert() {
		try {
			if (driver != null) {
				driver.switchTo().alert().accept();
			}
		} catch (WebDriverException e) {
			
		}
	}
	
	/*
	 * Test SelectList and MultipleSelectList
	 */
	@Test(groups={"it"})
	public void testIsTextSelect() {
			DriverTestPage.selectList.selectByText("option2");
			Assert.assertTrue(DriverTestPage.selectList.getSelectedText().equals("option2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleTextSelect() {
			DriverTestPage.selectMultipleList.deselectAll();
			String[] toSelect = {"option2", "option4"};
			DriverTestPage.selectMultipleList.selectByText(toSelect);
			Assert.assertTrue(toSelect[0].equals(DriverTestPage.selectMultipleList.getSelectedTexts()[0]));
			Assert.assertTrue(toSelect[1].equals(DriverTestPage.selectMultipleList.getSelectedTexts()[1]));
	}
	
	@Test(groups={"it"})
	public void testIsValueSelect() {
		DriverTestPage.selectList.selectByValue("opt2");
		Assert.assertTrue(DriverTestPage.selectList.getSelectedValue().equals("opt2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleValueSelect() {
			DriverTestPage.selectMultipleList.deselectAll();
			String[] toSelect = {"opt2", "opt4"};
			DriverTestPage.selectMultipleList.selectByValue(toSelect);
			Assert.assertTrue(toSelect[0].equals(DriverTestPage.selectMultipleList.getSelectedValues()[0]));
			Assert.assertTrue(toSelect[1].equals(DriverTestPage.selectMultipleList.getSelectedValues()[1]));
	}
	
	@Test(groups={"it"})
	public void testGetFirstSelectedOption() {
		DriverTestPage.selectMultipleList.deselectAll();
		String[] toSelect = {"opt2", "opt4"};
		DriverTestPage.selectMultipleList.selectByValue(toSelect);
		Assert.assertTrue("option2".equals(DriverTestPage.selectMultipleList.getFirstSelectedOption().getText()));
	}
	
	@Test(groups={"it"})
	public void testGetFirstSelectedOptionNoSelection() {
		DriverTestPage.selectMultipleList.deselectAll();
		Assert.assertNull(DriverTestPage.selectMultipleList.getFirstSelectedOption());
	}
	

	@Test(groups={"it"})
	public void testGetAllSelectedOptions() {
		DriverTestPage.selectMultipleList.deselectAll();
		String[] toSelect = {"opt2", "opt4"};
		DriverTestPage.selectMultipleList.selectByValue(toSelect);
		Assert.assertTrue("option2".equals(DriverTestPage.selectMultipleList.getAllSelectedOptions().get(0).getText()));
		Assert.assertTrue("option4".equals(DriverTestPage.selectMultipleList.getAllSelectedOptions().get(1).getText()));
	}
	
	
	@Test(groups={"it"})
	public void testIsCorrespondingTextSelect() {
			DriverTestPage.selectList.selectByCorrespondingText("option 2");
			Assert.assertTrue(DriverTestPage.selectList.getSelectedValue().equals("opt2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleCorrespondingTextSelect() {
			DriverTestPage.selectMultipleList.deselectAll();
			String[] toSelect = {"option 2", "opti4"};
			DriverTestPage.selectMultipleList.selectByCorrespondingText(toSelect);
			String[] toFind = {"opt2", "opt4"};
			Assert.assertTrue(toFind[0].equals(DriverTestPage.selectMultipleList.getSelectedValues()[0]));
			Assert.assertTrue(toFind[1].equals(DriverTestPage.selectMultipleList.getSelectedValues()[1]));
	}
	
	@Test(groups={"it"})
	public void testIsAllDeselected() {
		String[] toSelect = {"opt1", "opt2", "opt3", "opt4"};
		DriverTestPage.selectMultipleList.selectByCorrespondingText(toSelect);
		DriverTestPage.selectMultipleList.deselectAll();
		Assert.assertTrue(DriverTestPage.selectMultipleList.getSelectedValues().length == 0);
	}
	
	@Test(groups={"it"})
	public void testIsIndexSelect() {
		DriverTestPage.selectList.selectByIndex(1);
		Assert.assertTrue(DriverTestPage.selectList.getSelectedValue().equals("opt2"));
	}
	
	@Test(groups={"it"})
	public void testIsMultipleIndexSelect() {
		DriverTestPage.selectMultipleList.deselectAll();
		int[] toSelect = {1, 2};
		DriverTestPage.selectMultipleList.selectByIndex(toSelect);
		Assert.assertTrue(DriverTestPage.selectMultipleList.getSelectedValues()[0].equals("opt2"));
		Assert.assertTrue(DriverTestPage.selectMultipleList.getSelectedValues()[1].equals("opt3"));
	}
	
	@Test(groups={"it"})
	public void testIsIndexDeselect() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByIndex(1);
		DriverTestPage.selectMultipleList.deselectByIndex(1);
		Assert.assertTrue(DriverTestPage.selectMultipleList.getSelectedValues().length == 0);
	}
	
	@Test(groups={"it"})
	public void testIsTextDeselect() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByText("option1");
		DriverTestPage.selectMultipleList.deselectByText("option1");
		Assert.assertTrue(DriverTestPage.selectMultipleList.getSelectedValues().length == 0);
	}
	
	@Test(groups={"it"})
	public void testIsValueDeselect() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByValue("opt1");
		DriverTestPage.selectMultipleList.deselectByValue("opt1");
		Assert.assertTrue(DriverTestPage.selectMultipleList.getSelectedValues().length == 0);
	}	
	
	// test of select as UL/LI lists
	@Test(groups={"it"})
	public void testSelectUlList() {
		DriverTestPage.ulliListTrigger.click();
		DriverTestPage.selectUlLiList.selectByText("English");
		Assert.assertEquals(DriverTestPage.ulliListTrigger.getValue(), "English");
	}
	
	@Test(groups={"it"})
	public void testSelectUlListByValue() {
		DriverTestPage.ulliListTrigger.click();
		DriverTestPage.selectUlLiList.selectByValue("deu");
		Assert.assertEquals(DriverTestPage.ulliListTrigger.getValue(), "Deutsch");
	}
	
	@Test(groups={"it"})
	public void testSelectUlListByIndex() {
		DriverTestPage.ulliListTrigger.click();
		DriverTestPage.selectUlLiList.selectByIndex(0);
		Assert.assertEquals(DriverTestPage.ulliListTrigger.getValue(), "English");
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeSelectUlListByIndex() {
		DriverTestPage.ulliListTrigger.click();
		DriverTestPage.selectUlLiList.selectByIndex(0);
		Assert.assertEquals(DriverTestPage.ulliListTrigger.getValue(), "English");
		DriverTestPage.selectUlLiList.deselectByIndex(0);
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeSelectUlListByValue() {
		DriverTestPage.ulliListTrigger.click();
		DriverTestPage.selectUlLiList.selectByIndex(0);
		Assert.assertEquals(DriverTestPage.ulliListTrigger.getValue(), "English");
		DriverTestPage.selectUlLiList.deselectByValue("eng");
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeSelectUlListByText() {
		DriverTestPage.ulliListTrigger.click();
		DriverTestPage.selectUlLiList.selectByIndex(0);
		Assert.assertEquals(DriverTestPage.ulliListTrigger.getValue(), "English");
		DriverTestPage.selectUlLiList.deselectByText("English");
	}
	
	@Test(groups={"it"})
	public void testInputInSelectUlList() {
		DriverTestPage.ulliListTrigger.click();
		
		// should not raise any exception
		DriverTestPage.textInselectUlLiList.sendKeys("text");
	}
}
