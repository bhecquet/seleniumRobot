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

	@BeforeClass(groups={"it", "ut"})
	public void initDriver(final ITestContext testNGCtx) {
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
			// ignore
		}
	}

	/*
	 * Test SelectList and MultipleSelectList
	 */
	@Test(groups={"it"})
	public void testSelectByText() {
		DriverTestPage.selectList.selectByText("option2");
        Assert.assertEquals(DriverTestPage.selectList.getSelectedText(), "option2");
	}
	
	@Test(groups={"it"})
	public void testSelectByTextWithMultipleOptions() {
		DriverTestPage.selectMultipleList.deselectAll();
		Assert.assertTrue(DriverTestPage.selectMultipleList.isMultiple());
		String[] toSelect = {"option2", "option4"};
		DriverTestPage.selectMultipleList.selectByText(toSelect);
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedTexts()[0], toSelect[0]);
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedTexts()[1], toSelect[1]);
	}
	
	@Test(groups={"it"})
	public void testSelectByValue() {
		DriverTestPage.selectList.selectByValue("opt2");
        Assert.assertEquals(DriverTestPage.selectList.getSelectedValue(), "opt2");
	}
	
	@Test(groups={"it"})
	public void testSelectByMultipleValues() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByValue("opt2", "opt4");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[0], "opt2");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[1], "opt4");
	}
	
	@Test(groups={"it"})
	public void testGetFirstSelectedOption() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByValue("opt2", "opt4");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getFirstSelectedOption().getText(), "option2");
	}
	
	@Test(groups={"it"})
	public void testGetFirstSelectedOptionNoSelection() {
		DriverTestPage.selectMultipleList.deselectAll();
		Assert.assertNull(DriverTestPage.selectMultipleList.getFirstSelectedOption());
	}
	

	@Test(groups={"it"})
	public void testGetAllSelectedOptions() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByValue("opt2", "opt4");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getAllSelectedOptions().get(0).getText(), "option2");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getAllSelectedOptions().get(1).getText(), "option4");
	}
	
	
	@Test(groups={"it"})
	public void testSelectByCorrespondingText() {
		DriverTestPage.selectList.selectByCorrespondingText("option 2");
        Assert.assertEquals(DriverTestPage.selectList.getSelectedValue(), "opt2");
	}
	
	@Test(groups={"it"})
	public void testSelectByCorrespondingTextWithMultipleOptions() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByCorrespondingText("option 2", "opti4");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[0], "opt2");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[1], "opt4");
	}

	@Test(groups={"it"})
	public void testDeselectByCorrespondingTextWithMultipleOptions() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByCorrespondingText("option 2", "opti4");
		DriverTestPage.selectMultipleList.deselectByCorrespondingText("opti4");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues().length, 1);
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[0], "opt2");
	}
	
	@Test(groups={"it"})
	public void testDeselectAll() {
		DriverTestPage.selectMultipleList.selectByCorrespondingText("opt1", "opt2", "opt3", "opt4");
		DriverTestPage.selectMultipleList.deselectAll();
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues().length, 0);
	}
	
	@Test(groups={"it"})
	public void testSelectByIndex() {
		DriverTestPage.selectList.selectByIndex(1);
        Assert.assertEquals(DriverTestPage.selectList.getSelectedValue(), "opt2");
	}
	
	@Test(groups={"it"})
	public void testSelectByMultipleIndex() {
		DriverTestPage.selectMultipleList.deselectAll();
		int[] toSelect = {1, 2};
		DriverTestPage.selectMultipleList.selectByIndex(toSelect);
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[0], "opt2");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues()[1], "opt3");
	}
	
	@Test(groups={"it"})
	public void testDeselectByMultipleIndex() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByIndex(1);
		DriverTestPage.selectMultipleList.deselectByIndex(1);
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues().length, 0);
	}
	
	@Test(groups={"it"})
	public void testDeselectByText() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByText("option1");
		DriverTestPage.selectMultipleList.deselectByText("option1");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues().length, 0);
	}
	
	@Test(groups={"it"})
	public void testDeselectByValue() {
		DriverTestPage.selectMultipleList.deselectAll();
		DriverTestPage.selectMultipleList.selectByValue("opt1");
		DriverTestPage.selectMultipleList.deselectByValue("opt1");
        Assert.assertEquals(DriverTestPage.selectMultipleList.getSelectedValues().length, 0);
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
