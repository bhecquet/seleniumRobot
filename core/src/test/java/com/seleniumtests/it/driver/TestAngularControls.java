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

import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage;

public class TestAngularControls extends GenericMultiBrowserTest {

	public TestAngularControls() throws Exception {
		super(null, "DriverSubAngularTestPage"); 
	}
	
	public TestAngularControls(BrowserType browserType) throws Exception {
		super(browserType, "DriverSubAngularTestPage"); 
	}
	
	@BeforeMethod(groups= {"it"})
	public void init() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
	}
	
	@AfterMethod(groups={"it"})
	public void reset() {
		driver.navigate().refresh();
	}
   

	@Test(groups={"it"})
	public void testSelectByText() {
		DriverSubAngularTestPage.selectList.selectByText("Option 1");
		Assert.assertEquals(DriverSubAngularTestPage.selectList.getSelectedText(), "Option 1");
	}
	
	@Test(groups={"it"})
	public void testSelectByIndex() {
		DriverSubAngularTestPage.selectList.selectByIndex(2);
		Assert.assertEquals(DriverSubAngularTestPage.selectList.getSelectedText(), "Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectByValue() {
		DriverSubAngularTestPage.selectList.selectByValue("option3");
		Assert.assertEquals(DriverSubAngularTestPage.selectList.getSelectedText(), "Option 3");
	}
	
	@Test(groups={"it"})
	public void testSelectByCorrespondingText() {
		DriverSubAngularTestPage.selectList.selectByCorrespondingText("ption 1");
		Assert.assertEquals(DriverSubAngularTestPage.selectList.getSelectedText(), "Option 1");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByText() {
		DriverSubAngularTestPage.selectMultipleList.selectByText(new String[] {"Multiple Option 1", "Multiple Option 2"});
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByIndex() {
		DriverSubAngularTestPage.selectMultipleList.selectByIndex(new int[] {0, 2});
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "None");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByValue() {
		DriverSubAngularTestPage.selectMultipleList.selectByValue(new String[] {"option1", "option2"});
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByCorrespondingText() {
		DriverSubAngularTestPage.selectMultipleList.selectByCorrespondingText(new String[] {"ple Option 1", "ple Option 2"});
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testDeselectByText() {
		DriverSubAngularTestPage.selectMultipleList.selectByText(new String[] {"Multiple Option 1", "Multiple Option 2"});
		DriverSubAngularTestPage.selectMultipleList.deselectByText("Multiple Option 1");
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts[0], "Multiple Option 2");
		Assert.assertEquals(selectedTexts.length, 1);
	}
	
	@Test(groups={"it"})
	public void testDeselectByIndex() {
		DriverSubAngularTestPage.selectMultipleList.selectByIndex(new int[] {0, 2});
		DriverSubAngularTestPage.selectMultipleList.deselectByIndex(2);
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "None");
	}
	
	@Test(groups={"it"})
	public void testDeselectByValue() {
		DriverSubAngularTestPage.selectMultipleList.selectByValue(new String[] {"option1", "option2"});
		DriverSubAngularTestPage.selectMultipleList.deselectByValue("option1");
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testDeselectByCorrespondingText() {
		DriverSubAngularTestPage.selectMultipleList.selectByCorrespondingText(new String[] {"ple Option 1", "ple Option 2"});
		DriverSubAngularTestPage.selectMultipleList.deselectByCorrespondingText("ple Option 1");
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 2");
	}
	
	/**
	 * Check deselecting a non multiple element raises an error
	 */
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByTextNonMultipleSelect() {
		DriverSubAngularTestPage.selectList.deselectByText("Multiple Option 1");
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByIndexNonMultipleSelect() {
		DriverSubAngularTestPage.selectList.deselectByIndex(2);
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByValueNonMultipleSelect() {
		DriverSubAngularTestPage.selectList.deselectByValue("option1");
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNonMultipleSelect() {
		DriverSubAngularTestPage.selectList.deselectByCorrespondingText("ple Option 1");
	}
	
	/**
	 * Check deselecting a not selected element does not raise error
	 */
	@Test(groups={"it"})
	public void testDeselectByTextNotSelected() {
		DriverSubAngularTestPage.selectMultipleList.deselectByText("Multiple Option 1");
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	@Test(groups={"it"})
	public void testDeselectByIndexNotSelected() {
		DriverSubAngularTestPage.selectMultipleList.deselectByIndex(2);
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	@Test(groups={"it"})
	public void testDeselectByValueNotSelected() {
		DriverSubAngularTestPage.selectMultipleList.deselectByValue("option1");
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	@Test(groups={"it"})
	public void testDeselectByCorrespondingTextNotSelected() {
		DriverSubAngularTestPage.selectMultipleList.deselectByCorrespondingText("ple Option 1");
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	/**
	 * Check deselecting an invalid value raises an error
	 */
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidText() {
		DriverSubAngularTestPage.selectMultipleList.deselectByText("Multiple Option 10");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidIndex() {
		DriverSubAngularTestPage.selectMultipleList.deselectByIndex(20);
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidValue() {
		DriverSubAngularTestPage.selectMultipleList.deselectByValue("option10");
	}
	
	@Test(groups={"it"})
	public void testSelectNotMultiple() {
		Assert.assertFalse(DriverSubAngularTestPage.selectList.isMultiple());
	}
	 
	@Test(groups={"it"})
	public void testSelectMultiple() { 
		Assert.assertTrue(DriverSubAngularTestPage.selectMultipleList.isMultiple());
	}
	
	/**
	 * Check that if we specify the same option 2 times, it's not deselected
	 */
	@Test(groups={"it"})
	public void testSelectSameTextMultipleTimes() {
		DriverSubAngularTestPage.selectMultipleList.selectByText(new String[] {"Multiple Option 1", "Multiple Option 1"});
		String[] selectedTexts = DriverSubAngularTestPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
	}
	
	/**
	 * Check trying to select an element which does not exist raises an error
	 */
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidText() {
		DriverSubAngularTestPage.selectList.selectByText("Option 12");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidTexts() {
		DriverSubAngularTestPage.selectList.selectByText(new String[] {"Option 12"});
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndex() {
		DriverSubAngularTestPage.selectList.selectByIndex(20);
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndexes() {
		DriverSubAngularTestPage.selectList.selectByIndex(new int[] {10, 20});
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValue() {
		DriverSubAngularTestPage.selectList.selectByValue("option30");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValues() {
		DriverSubAngularTestPage.selectList.selectByValue(new String[] {"option30"});
	}
	
	@Test(groups= {"it"})
	public void testCheckBox() {
		DriverSubAngularTestPage.checkbox.check();
		Assert.assertTrue(DriverSubAngularTestPage.checkbox.isSelected());
	}
	
	@Test(groups= {"it"})
	public void testUncheckCheckBox() {
		DriverSubAngularTestPage.checkbox.check();
		DriverSubAngularTestPage.checkbox.uncheck();
		Assert.assertFalse(DriverSubAngularTestPage.checkbox.isSelected());
	}

	@Test(groups= {"it"})
	public void testRadio() {
		DriverSubAngularTestPage.radio.check();
		Assert.assertTrue(DriverSubAngularTestPage.radio.isSelected());
	}
	
}
