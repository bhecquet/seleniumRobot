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
package com.seleniumtests.it.driver.support.perdriver.firefox;

import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestNgSelect;

public class TestNgSelectFirefox extends TestNgSelect {

	public TestNgSelectFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}

	@Override
    @Test(groups={"it"})
	public void testSelectByText() {
		super.testSelectByText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectByTextAtBottomOfList() {
		super.testSelectByTextAtBottomOfList();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectByIndex() {
		super.testSelectByIndex();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectByValue() {
		super.testSelectByValue();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectByCorrespondingText() {
		super.testSelectByCorrespondingText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectMultipleByText() {
		super.testSelectMultipleByText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectMultipleByIndex() {
		super.testSelectMultipleByIndex();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectMultipleByValue() {
		super.testSelectMultipleByValue();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectMultipleByCorrespondingText() {
		super.testSelectMultipleByCorrespondingText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByText() {
		super.testDeselectByText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByIndex() {
		super.testDeselectByIndex();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByValue() {
		super.testDeselectByValue();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByCorrespondingText() {
		super.testDeselectByCorrespondingText();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByTextNonMultipleSelect() {
		super.testDeselectByTextNonMultipleSelect();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByIndexNonMultipleSelect() {
		super.testDeselectByIndexNonMultipleSelect();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByValueNonMultipleSelect() {
		super.testDeselectByValueNonMultipleSelect();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNonMultipleSelect() {
		super.testDeselectByCorrespondingTextNonMultipleSelect();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByTextNotSelected() {
		super.testDeselectByTextNotSelected();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByIndexNotSelected() {
		super.testDeselectByIndexNotSelected();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByValueNotSelected() {
		super.testDeselectByValueNotSelected();
	}
	
	@Override
    @Test(groups={"it"})
	public void testDeselectByCorrespondingTextNotSelected() {
		super.testDeselectByCorrespondingTextNotSelected();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidText() {
		super.testDeselectByInvalidText();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidIndex() {
		super.testDeselectByInvalidIndex();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidValue() {
		super.testDeselectByInvalidValue();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectNotMultiple() {
		super.testSelectNotMultiple();
	}
	 
	@Override
    @Test(groups={"it"})
	public void testSelectMultiple() {
		super.testSelectMultiple(); 
	}
	
	@Override
    @Test(groups={"it"})
	public void testSelectSameTextMultipleTimes() {
		super.testSelectSameTextMultipleTimes();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidText() {
		super.testSelectByInvalidText();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidTexts() {
		super.testSelectByInvalidTexts();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndex() {
		super.testSelectInvalidIndex();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndexes() {
		super.testSelectInvalidIndexes();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValue() {
		super.testSelectByInvalidValue();
	}
	
	@Override
    @Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValues() {
		super.testSelectByInvalidValues();
	}

}
