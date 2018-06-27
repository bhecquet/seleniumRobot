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
package com.seleniumtests.it.driver;

import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;

public class TestAngularControls extends GenericMultiBrowserTest {

	private static boolean angularWindowActive = false;
	
	public TestAngularControls() throws Exception {
		super(BrowserType.CHROME, "DriverSubAngularTestPage"); 
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
		angularPage.selectList.selectByText("Option 1");
		Assert.assertEquals(angularPage.selectList.getSelectedText(), "Option 1");
	}
	
	@Test(groups={"it"})
	public void testSelectByIndex() {
		angularPage.selectList.selectByIndex(2);
		Assert.assertEquals(angularPage.selectList.getSelectedText(), "Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectByValue() {
		angularPage.selectList.selectByValue("option3");
		Assert.assertEquals(angularPage.selectList.getSelectedText(), "Option 3");
	}
	
	@Test(groups={"it"})
	public void testSelectByCorrespondingText() {
		angularPage.selectList.selectByCorrespondingText("ption 1");
		Assert.assertEquals(angularPage.selectList.getSelectedText(), "Option 1");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByText() {
		angularPage.selectMultipleList.selectByText(new String[] {"Multiple Option 1", "Multiple Option 2"});
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByIndex() {
		angularPage.selectMultipleList.selectByIndex(new int[] {0, 2});
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "None");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByValue() {
		angularPage.selectMultipleList.selectByValue(new String[] {"option1", "option2"});
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByCorrespondingText() {
		angularPage.selectMultipleList.selectByCorrespondingText(new String[] {"ple Option 1", "ple Option 2"});
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 2);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
		Assert.assertEquals(selectedTexts[1], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testDeselectByText() {
		angularPage.selectMultipleList.selectByText(new String[] {"Multiple Option 1", "Multiple Option 2"});
		angularPage.selectMultipleList.deselectByText("Multiple Option 1");
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts[0], "Multiple Option 2");
		Assert.assertEquals(selectedTexts.length, 1);
	}
	
	@Test(groups={"it"})
	public void testDeselectByIndex() {
		angularPage.selectMultipleList.selectByIndex(new int[] {0, 2});
		angularPage.selectMultipleList.deselectByIndex(2);
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "None");
	}
	
	@Test(groups={"it"})
	public void testDeselectByValue() {
		angularPage.selectMultipleList.selectByValue(new String[] {"option1", "option2"});
		angularPage.selectMultipleList.deselectByValue("option1");
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testDeselectByCorrespondingText() {
		angularPage.selectMultipleList.selectByCorrespondingText(new String[] {"ple Option 1", "ple Option 2"});
		angularPage.selectMultipleList.deselectByCorrespondingText("ple Option 1");
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 2");
	}
	
	@Test(groups={"it"})
	public void testSelectNotMultiple() {
		Assert.assertFalse(angularPage.selectList.isMultiple());
	}
	 
	@Test(groups={"it"})
	public void testSelectMultiple() { 
		Assert.assertTrue(angularPage.selectMultipleList.isMultiple());
	}
	
	/**
	 * Check that if we specify the same option 2 times, it's not deselected
	 */
	@Test(groups={"it"})
	public void testSelectSameTextMultipleTimes() {
		angularPage.selectMultipleList.selectByText(new String[] {"Multiple Option 1", "Multiple Option 1"});
		String[] selectedTexts = angularPage.selectMultipleList.getSelectedTexts();
		Assert.assertEquals(selectedTexts.length, 1);
		Assert.assertEquals(selectedTexts[0], "Multiple Option 1");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidText() {
		angularPage.selectList.selectByText("Option 12");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidTexts() {
		angularPage.selectList.selectByText(new String[] {"Option 12"});
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndex() {
		angularPage.selectList.selectByIndex(20);
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndexes() {
		angularPage.selectList.selectByIndex(new int[] {10, 20});
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValue() {
		angularPage.selectList.selectByValue("option30");
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValues() {
		angularPage.selectList.selectByValue(new String[] {"option30"});
	}
	
	
}
