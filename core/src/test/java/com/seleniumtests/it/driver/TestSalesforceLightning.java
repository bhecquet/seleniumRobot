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

import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPageSalesforceLightning;

/**
 * These tests need to connect to Salesforce which may be accessible through a proxy ony
 * If so, use 'salesforceProxyType' property for launch, so that tests can access Salesforce
 */
public class TestSalesforceLightning extends GenericMultiBrowserTest {

	public TestSalesforceLightning() throws Exception {
		super(BrowserType.FIREFOX, "DriverTestPageSalesforceLightning", null, System.getProperty("salesforceProxyType"));
	}
	
	public TestSalesforceLightning(BrowserType browserType) throws Exception {
		super(browserType, "DriverTestPageSalesforceLightning", null, System.getProperty("salesforceProxyType"));
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
   

	@Test(groups = "it")
	public void testSelectByText() {
		DriverTestPageSalesforceLightning.combobox.selectByText("New");
		Assert.assertEquals(DriverTestPageSalesforceLightning.combobox.getSelectedText(), "New");
	}
	
	@Test(groups = "it")
	public void testSelectByIndex() {
		DriverTestPageSalesforceLightning.combobox.selectByIndex(2);
		Assert.assertEquals(DriverTestPageSalesforceLightning.combobox.getSelectedText(), "Finished");
	}
	
	@Test(groups = "it", expectedExceptions = UnsupportedOperationException.class)
	public void testSelectByValue() {
		DriverTestPageSalesforceLightning.combobox.selectByValue("New");
	}
	
	@Test(groups = "it")
	public void testSelectByCorrespondingText() {
		DriverTestPageSalesforceLightning.combobox.selectByCorrespondingText("Ne");
		Assert.assertEquals(DriverTestPageSalesforceLightning.combobox.getSelectedText(), "New");
	}
	
	
	@Test(groups = "it", expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectByText() {
		DriverTestPageSalesforceLightning.combobox.deselectByText("New");
	}
	
	@Test(groups = "it", expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectByIndex() {
		DriverTestPageSalesforceLightning.combobox.deselectByIndex(2);
	}
	
	@Test(groups = "it", expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectByValue() {
		DriverTestPageSalesforceLightning.combobox.deselectByValue("In Progress");
	}
	
	
	@Test(groups = "it")
	public void testSelectNotMultiple() {
		Assert.assertFalse(DriverTestPageSalesforceLightning.combobox.isMultiple());
	}
	
	/**
	 * Check trying to select an element which does not exist raises an error
	 */
	@Test(groups = "it", expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidText() {
		DriverTestPageSalesforceLightning.combobox.selectByText("Option 12");
	}
	
	@Test(groups = "it", expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidTexts() {
		DriverTestPageSalesforceLightning.combobox.selectByText(new String[] {"Option 12"});
	}
	
	@Test(groups = "it", expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndex() {
		DriverTestPageSalesforceLightning.combobox.selectByIndex(20);
	}
	
	@Test(groups = "it", expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndexes() {
		DriverTestPageSalesforceLightning.combobox.selectByIndex(new int[] {10, 20});
	}
	
	@Test(groups = "it", expectedExceptions = UnsupportedOperationException.class)
	public void testSelectByInvalidValue() {
		DriverTestPageSalesforceLightning.combobox.selectByValue("option30");
	}
	
	@Test(groups = "it", expectedExceptions = UnsupportedOperationException.class)
	public void testSelectByInvalidValues() {
		DriverTestPageSalesforceLightning.combobox.selectByValue(new String[] {"option30"});
	}
	
}
