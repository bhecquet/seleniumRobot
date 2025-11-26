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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage;

public class TestNgSelect extends GenericMultiBrowserTest {

	public TestNgSelect() throws Exception {
		super(null, "DriverSubAngularTestPage"); 
	}
	
	public TestNgSelect(BrowserType browserType) throws Exception {
		super(browserType, "DriverSubAngularTestPage"); 
	}
	
	@BeforeMethod(groups= {"it"})
	public void init() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
	}
	
	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void reset() {
		if (driver != null) {
			driver.navigate().refresh();
		}
	}
   

	
	public void testSelectByText() {
		DriverSubAngularTestPage.ngSelectList.selectByText("Python");
		Assert.assertEquals(DriverSubAngularTestPage.ngSelectList.getSelectedText(), "Python");
	}
	
	
	public void testSelectByTextAtBottomOfList() {
		DriverSubAngularTestPage.ngSelectList.selectByText("ReactJs");
		Assert.assertEquals(DriverSubAngularTestPage.ngSelectList.getSelectedText(), "ReactJs");
	}
	
	
	public void testSelectByIndex() {
		DriverSubAngularTestPage.ngSelectList.selectByIndex(2);
		Assert.assertEquals(DriverSubAngularTestPage.ngSelectList.getSelectedText(), "Java");
	}
	
	
	public void testSelectByValue() {
		DriverSubAngularTestPage.ngSelectList.selectByValue("Node Js");
		Assert.assertEquals(DriverSubAngularTestPage.ngSelectList.getSelectedText(), "Node Js");
	}
	
	
	public void testSelectByCorrespondingText() {
		DriverSubAngularTestPage.ngSelectList.selectByCorrespondingText("de_Js");
		Assert.assertEquals(DriverSubAngularTestPage.ngSelectList.getSelectedText(), "Node Js");
	}
	
	
	public void testSelectMultipleByText() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByText(new String[] {"Python", "Java"});
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Python");
		Assert.assertEquals(selectedTexts[1], "Java");
	}
	
	
	public void testSelectMultipleByIndex() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByIndex(new int[] {0, 2});
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Python");
		Assert.assertEquals(selectedTexts[1], "Java");
	}
	
	
	public void testSelectMultipleByValue() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByValue(new String[] {"Java", "Angular"});
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Java");
		Assert.assertEquals(selectedTexts[1], "Angular");
	}
	
	
	public void testSelectMultipleByCorrespondingText() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByCorrespondingText(new String[] {"ava", "gular"});
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Java");
		Assert.assertEquals(selectedTexts[1], "Angular");
	}
	
	
	public void testDeselectByText() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByText(new String[] {"Python", "Java"});
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByText("Python");
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts[0], "Java");
		Assert.assertEquals(selectedTexts.length, 1);
	}
	
	
	public void testDeselectByIndex() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByIndex(new int[] {0, 2});
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByIndex(2);
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Python");
	}
	
	
	public void testDeselectByValue() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByValue(new String[] {"Java", "Angular"});
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByValue("Java");
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Angular");
	}
	
	
	public void testDeselectByCorrespondingText() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByCorrespondingText(new String[] {"ava", "gular"});
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByCorrespondingText("ava");
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Angular");
	}
	
	/**
	 * Check deselecting a non multiple element raises an error
	 */
	
	public void testDeselectByTextNonMultipleSelect() {
		DriverSubAngularTestPage.ngSelectList.deselectByText("Java");
	}
	
	
	public void testDeselectByIndexNonMultipleSelect() {
		DriverSubAngularTestPage.ngSelectList.deselectByIndex(2);
	}
	
	
	public void testDeselectByValueNonMultipleSelect() {
		DriverSubAngularTestPage.ngSelectList.deselectByValue("Angular");
	}
	
	
	public void testDeselectByCorrespondingTextNonMultipleSelect() {
		DriverSubAngularTestPage.ngSelectList.deselectByCorrespondingText("ython");
	}
	
	/**
	 * Check deselecting a not selected element does not raise error
	 */
	
	public void testDeselectByTextNotSelected() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByText("Python");
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	
	public void testDeselectByIndexNotSelected() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByIndex(2);
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	
	public void testDeselectByValueNotSelected() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByValue("Java");
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	
	public void testDeselectByCorrespondingTextNotSelected() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByCorrespondingText("ular");
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 0);
	}
	
	/**
	 * Check deselecting an invalid value raises an error
	 */
	
	public void testDeselectByInvalidText() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByText("Multiple Option 10");
	}
	
	
	public void testDeselectByInvalidIndex() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByIndex(20);
	}
	
	
	public void testDeselectByInvalidValue() {
		DriverSubAngularTestPage.ngSelectMultipleList.deselectByValue("option10");
	}
	
	
	public void testSelectNotMultiple() {
		Assert.assertFalse(DriverSubAngularTestPage.ngSelectList.isMultiple());
	}
	 
	
	public void testSelectMultiple() { 
		Assert.assertTrue(DriverSubAngularTestPage.ngSelectMultipleList.isMultiple());
	}
	
	/**
	 * Check that if we specify the same option 2 times, it's not deselected
	 */
	
	public void testSelectSameTextMultipleTimes() {
		DriverSubAngularTestPage.ngSelectMultipleList.selectByText(new String[] {"Java", "Java"});
		String[] selectedTexts = DriverSubAngularTestPage.ngSelectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Java");
	}
	
	/**
	 * Check trying to select an element which does not exist raises an error
	 */
	
	public void testSelectByInvalidText() {
		DriverSubAngularTestPage.ngSelectList.selectByText("Option 12");
	}
	
	
	public void testSelectByInvalidTexts() {
		DriverSubAngularTestPage.ngSelectList.selectByText(new String[] {"Option 12"});
	}
	
	
	public void testSelectInvalidIndex() {
		DriverSubAngularTestPage.ngSelectList.selectByIndex(20);
	}
	
	
	public void testSelectInvalidIndexes() {
		DriverSubAngularTestPage.ngSelectList.selectByIndex(new int[] {10, 20});
	}
	
	
	public void testSelectByInvalidValue() {
		DriverSubAngularTestPage.ngSelectList.selectByValue("option30");
	}
	
	
	public void testSelectByInvalidValues() {
		DriverSubAngularTestPage.ngSelectList.selectByValue(new String[] {"option30"});
	}
	
}
