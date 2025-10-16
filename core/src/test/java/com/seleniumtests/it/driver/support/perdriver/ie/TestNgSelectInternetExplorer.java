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
package com.seleniumtests.it.driver.support.perdriver.ie;

import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestNgSelect;

public class TestNgSelectInternetExplorer extends TestNgSelect {

	public TestNgSelectInternetExplorer() throws Exception {
		super(BrowserType.INTERNET_EXPLORER);
	}

	@Override
    @Test(groups= {"ie"})
	public void testSelectByText() {
		super.testSelectByText();
	}

	@Override
    @Test(groups= {"ie"})
	public void testSelectByTextAtBottomOfList() {
		super.testSelectByTextAtBottomOfList();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectByIndex() {
		super.testSelectByIndex();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectByValue() {
		super.testSelectByValue();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectByCorrespondingText() {
		super.testSelectByCorrespondingText();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectMultipleByText() {
		super.testSelectMultipleByText();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectMultipleByIndex() {
		super.testSelectMultipleByIndex();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectMultipleByValue() {
		super.testSelectMultipleByValue();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectMultipleByCorrespondingText() {
		super.testSelectMultipleByCorrespondingText();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByText() {
		super.testDeselectByText();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByIndex() {
		super.testDeselectByIndex();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByValue() {
		super.testDeselectByValue();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByCorrespondingText() {
		super.testDeselectByCorrespondingText();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByTextNonMultipleSelect() {
		super.testDeselectByTextNonMultipleSelect();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByIndexNonMultipleSelect() {
		super.testDeselectByIndexNonMultipleSelect();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByValueNonMultipleSelect() {
		super.testDeselectByValueNonMultipleSelect();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNonMultipleSelect() {
		super.testDeselectByCorrespondingTextNonMultipleSelect();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByTextNotSelected() {
		super.testDeselectByTextNotSelected();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByIndexNotSelected() {
		super.testDeselectByIndexNotSelected();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByValueNotSelected() {
		super.testDeselectByValueNotSelected();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testDeselectByCorrespondingTextNotSelected() {
		super.testDeselectByCorrespondingTextNotSelected();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidText() {
		super.testDeselectByInvalidText();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidIndex() {
		super.testDeselectByInvalidIndex();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidValue() {
		super.testDeselectByInvalidValue();
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectNotMultiple() {
		super.testSelectNotMultiple();
	}
	 
	@Override
    @Test(groups= {"ie"})
	public void testSelectMultiple() {
		super.testSelectMultiple(); 
	}
	
	@Override
    @Test(groups= {"ie"})
	public void testSelectSameTextMultipleTimes() {
		super.testSelectSameTextMultipleTimes();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidText() {
		super.testSelectByInvalidText();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidTexts() {
		super.testSelectByInvalidTexts();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndex() {
		super.testSelectInvalidIndex();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndexes() {
		super.testSelectInvalidIndexes();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValue() {
		super.testSelectByInvalidValue();
	}
	
	@Override
    @Test(groups= {"ie"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValues() {
		super.testSelectByInvalidValues();
	}
	
}
