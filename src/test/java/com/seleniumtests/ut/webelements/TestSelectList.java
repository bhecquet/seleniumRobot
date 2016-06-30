package com.seleniumtests.ut.webelements;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;
import com.seleniumtests.it.driver.TestDriver;
import com.seleniumtests.it.driver.TestUiSelect;

public class TestSelectList {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	private static TestUiSelect testSelectIt;
	
	@BeforeClass(groups={"ut"})
	public static void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		testSelectIt = new TestUiSelect(driver, testPage);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testSelect() {
		testSelectIt.testIsTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultiple() {
		testSelectIt.testIsMultipleTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectValue() {
		testSelectIt.testIsValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleValue() {
		testSelectIt.testIsMultipleValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectCorresponding() {
		testSelectIt.testIsCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleCorresponding() {
		testSelectIt.testIsMultipleCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectIndex() {
		testSelectIt.testIsIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleIndex() {
		testSelectIt.testIsMultipleIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectAll() {
		testSelectIt.testIsAllDeselected();
	}

	
	@Test(groups={"ut"})
	public void testDeselectIndex() {
		testSelectIt.testIsIndexDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectText() {
		testSelectIt.testIsTextDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectValue() {
		testSelectIt.testIsValueDeselect();
	}
	
}
