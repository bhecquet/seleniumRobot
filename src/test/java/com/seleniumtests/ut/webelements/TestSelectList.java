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
	private static TestDriver testSelectIt;
	
	@BeforeClass(groups={"ut"})
	public static void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testSelect() {
		TestUiSelect.testIsTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultiple() {
		TestUiSelect.testIsMultipleTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectValue() {
		TestUiSelect.testIsValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleValue() {
		TestUiSelect.testIsMultipleValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectCorresponding() {
		TestUiSelect.testIsCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleCorresponding() {
		TestUiSelect.testIsMultipleCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectIndex() {
		TestUiSelect.testIsIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleIndex() {
		TestUiSelect.testIsMultipleIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectAll() {
		TestUiSelect.testIsAllDeselected();
	}

	
	@Test(groups={"ut"})
	public void testDeselectIndex() {
		TestUiSelect.testIsIndexDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectText() {
		TestUiSelect.testIsTextDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectValue() {
		TestUiSelect.testIsValueDeselect();
	}
	
}
