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
package com.seleniumtests.it.driver.support.perdriver.testangularcontrols;

import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestAngularControls;

public class TestAngularControlsInternetExplorer extends TestAngularControls {

	public TestAngularControlsInternetExplorer() throws Exception {
		super(BrowserType.INTERNET_EXPLORER);
	}

	/**
	 * Check that when element is not found, message states it
	 */
	@Test(groups={"it"}, expectedExceptions = NoSuchElementException.class, expectedExceptionsMessageRegExp = "Searched element \\[SelectList list, by\\=\\{By.id: angularSelectNotHere\\}\\] from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage' could not be found.*")
	public void testSelectNotFound() {
		super.testSelectNotFound();
	}

	@Test(groups= {"ie"})
	public void testSelectByText() {
		super.testSelectByText();
	}

	@Test(groups= {"ie"})
	public void testSelectByTextAtBottomOfList() {
		super.testSelectByTextAtBottomOfList();
	}
	
	@Test(groups= {"ie"})
	public void testSelectByIndex() {
		super.testSelectByIndex();
	}
	
	@Test(groups= {"ie"})
	public void testSelectByValue() {
		super.testSelectByValue();
	}
	
	@Test(groups= {"ie"})
	public void testSelectByCorrespondingText() {
		super.testSelectByCorrespondingText();
	}
	
	@Test(groups= {"ie"})
	public void testSelectMultipleByText() {
		super.testSelectMultipleByText();
	}
	
	@Test(groups= {"ie"})
	public void testSelectMultipleByIndex() {
		super.testSelectMultipleByIndex();
	}
	
	@Test(groups= {"ie"})
	public void testSelectMultipleByValue() {
		super.testSelectMultipleByValue();
	}
	
	@Test(groups= {"ie"})
	public void testSelectMultipleByCorrespondingText() {
		super.testSelectMultipleByCorrespondingText();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByText() {
		super.testDeselectByText();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByIndex() {
		super.testDeselectByIndex();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByValue() {
		super.testDeselectByValue();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByCorrespondingText() {
		super.testDeselectByCorrespondingText();
	}
	
	@Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByTextNonMultipleSelect() {
		super.testDeselectByTextNonMultipleSelect();
	}
	
	@Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByIndexNonMultipleSelect() {
		super.testDeselectByIndexNonMultipleSelect();
	}
	
	@Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByValueNonMultipleSelect() {
		super.testDeselectByValueNonMultipleSelect();
	}
	
	@Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNonMultipleSelect() {
		super.testDeselectByCorrespondingTextNonMultipleSelect();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByTextNotSelected() {
		super.testDeselectByTextNotSelected();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByIndexNotSelected() {
		super.testDeselectByIndexNotSelected();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByValueNotSelected() {
		super.testDeselectByValueNotSelected();
	}
	
	@Test(groups= {"ie"})
	public void testDeselectByCorrespondingTextNotSelected() {
		super.testDeselectByCorrespondingTextNotSelected();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularMultipleSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with text: Multiple Option 10. Known options are: None;Multiple Option 1;Multiple Option 2;Multiple Option 3;Multiple Option 4;Multiple Option 5;Multiple Option 6;Multiple Option 7;Multiple Option 8;Multiple Option 9.*")
	public void testDeselectByInvalidText() {
		super.testDeselectByInvalidText();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularMultipleSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with index: 20.*")
	public void testDeselectByInvalidIndex() {
		super.testDeselectByInvalidIndex();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularMultipleSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with value: option10. Known options are: ;option1;option2;option3;option1;option2;option3;option1;option2;option3.*")
	public void testDeselectByInvalidValue() {
		super.testDeselectByInvalidValue();
	}
	
	@Test(groups={"it"})
	public void testSelectNotMultiple() {
		super.testSelectNotMultiple();
	}
	 
	@Test(groups={"it"})
	public void testSelectMultiple() {
		super.testSelectMultiple(); 
	}
	
	@Test(groups={"it"})
	public void testSelectSameTextMultipleTimes() {
		super.testSelectSameTextMultipleTimes();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with text: Option 12. Known options are: None;Option 1;Option 2;Option 3;Option 4;Option 5;Option 6;Option 7;Option 8;Option 9.*")
	public void testSelectByInvalidText() {
		super.testSelectByInvalidText();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with text: Option 12. Known options are: None;Option 1;Option 2;Option 3;Option 4;Option 5;Option 6;Option 7;Option 8;Option 9.*")
	public void testSelectByInvalidTexts() {
		super.testSelectByInvalidTexts();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp ="'SelectList list, by\\=\\{By.id: angularSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with index: 20.*")
	public void testSelectInvalidIndex() {
		super.testSelectInvalidIndex();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp ="'SelectList list, by\\=\\{By.id: angularSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with index: 10.*")
	public void testSelectInvalidIndexes() {
		super.testSelectInvalidIndexes();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with value: option30. Known options are: ;option1;option2;option3;option3;option3;option3;option3;option3;option3.*")
	public void testSelectByInvalidValue() {
		super.testSelectByInvalidValue();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class, expectedExceptionsMessageRegExp = "'SelectList list, by\\=\\{By.id: angularSelect\\}' from page 'com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage': Cannot find option with value: option30. Known options are: ;option1;option2;option3;option3;option3;option3;option3;option3;option3.*")
	public void testSelectByInvalidValues() {
		super.testSelectByInvalidValues();
	}
	
	@Test(groups= {"ie"})
	public void testCheckBox() {
		super.testCheckBox();
	}
	
	@Test(groups= {"ie"})
	public void testUncheckCheckBox() {
		super.testUncheckCheckBox();
	}

	@Test(groups= {"ie"})
	public void testRadio() {
		super.testRadio();
	}

	@Test(groups= {"ie"})
	public void testTextField() {
		super.testTextField();
	}

	@Test(groups= {"ie"})
	public void testClearDatePicker() {
		super.testClearDatePicker();
	}

	@Test(groups= {"ie"})
	public void testSendKeysDatePicker() {
		super.testSendKeysDatePicker();
	}
}
