package com.seleniumtests.it.driver.support.perdriver.testangularcontrols;

import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestAngularControls;
import com.seleniumtests.it.driver.support.pages.DriverSubAngularTestPage;

public class TestAngularControlsChrome extends TestAngularControls {

	public TestAngularControlsChrome() throws Exception {
		super(BrowserType.CHROME);
	}

	@Test(groups={"it"})
	public void testSelectByText() {
		super.testSelectByText();
	}

	@Test(groups={"it"})
	public void testSelectByTextAtBottomOfList() {
		super.testSelectByTextAtBottomOfList();
	}
	
	@Test(groups={"it"})
	public void testSelectByIndex() {
		super.testSelectByIndex();
	}
	
	@Test(groups={"it"})
	public void testSelectByValue() {
		super.testSelectByValue();
	}
	
	@Test(groups={"it"})
	public void testSelectByCorrespondingText() {
		super.testSelectByCorrespondingText();
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByText() {
		super.testSelectMultipleByText();
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByIndex() {
		super.testSelectMultipleByIndex();
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByValue() {
		super.testSelectMultipleByValue();
	}
	
	@Test(groups={"it"})
	public void testSelectMultipleByCorrespondingText() {
		super.testSelectMultipleByCorrespondingText();
	}
	
	@Test(groups={"it"})
	public void testDeselectByText() {
		super.testDeselectByText();
	}
	
	@Test(groups={"it"})
	public void testDeselectByIndex() {
		super.testDeselectByIndex();
	}
	
	@Test(groups={"it"})
	public void testDeselectByValue() {
		super.testDeselectByValue();
	}
	
	@Test(groups={"it"})
	public void testDeselectByCorrespondingText() {
		super.testDeselectByCorrespondingText();
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByTextNonMultipleSelect() {
		super.testDeselectByTextNonMultipleSelect();
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByIndexNonMultipleSelect() {
		super.testDeselectByIndexNonMultipleSelect();
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByValueNonMultipleSelect() {
		super.testDeselectByValueNonMultipleSelect();
	}
	
	@Test(groups={"it"}, expectedExceptions=UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNonMultipleSelect() {
		super.testDeselectByCorrespondingTextNonMultipleSelect();
	}
	
	@Test(groups={"it"})
	public void testDeselectByTextNotSelected() {
		super.testDeselectByTextNotSelected();
	}
	
	@Test(groups={"it"})
	public void testDeselectByIndexNotSelected() {
		super.testDeselectByIndexNotSelected();
	}
	
	@Test(groups={"it"})
	public void testDeselectByValueNotSelected() {
		super.testDeselectByValueNotSelected();
	}
	
	@Test(groups={"it"})
	public void testDeselectByCorrespondingTextNotSelected() {
		super.testDeselectByCorrespondingTextNotSelected();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidText() {
		super.testDeselectByInvalidText();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testDeselectByInvalidIndex() {
		super.testDeselectByInvalidIndex();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
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
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidText() {
		super.testSelectByInvalidText();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidTexts() {
		super.testSelectByInvalidTexts();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndex() {
		super.testSelectInvalidIndex();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectInvalidIndexes() {
		super.testSelectInvalidIndexes();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValue() {
		super.testSelectByInvalidValue();
	}
	
	@Test(groups={"it"}, expectedExceptions=NoSuchElementException.class)
	public void testSelectByInvalidValues() {
		super.testSelectByInvalidValues();
	}
	
	@Test(groups= {"it"})
	public void testCheckBox() {
		super.testCheckBox();
	}
	
	@Test(groups= {"it"})
	public void testUncheckCheckBox() {
		super.testUncheckCheckBox();
	}

	@Test(groups= {"it"})
	public void testRadio() {
		super.testRadio();
	}

	@Test(groups= {"it"})
	public void testTextField() {
		super.testTextField();
	}

	@Test(groups= {"it"})
	public void testClearDatePicker() {
		super.testClearDatePicker();
	}
	
	@Test(groups= {"it"})
	public void testSendKeysDatePicker() {
		super.testSendKeysDatePicker();
	}
}
