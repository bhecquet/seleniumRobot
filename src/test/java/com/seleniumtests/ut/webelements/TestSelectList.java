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

public class TestSelectList {

	private static WebDriver driver;
	private static DriverTestPage testPage;
	private static TestDriver testDriverIt;
	
	@BeforeClass(groups={"ut"})
	public static void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		testDriverIt = new TestDriver(driver, testPage);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testSelect() {
		testDriverIt.testIsTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultiple() {
		testDriverIt.testIsMultipleTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectValue() {
		testDriverIt.testIsValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleValue() {
		testDriverIt.testIsMultipleValueSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectCorresponding() {
		testDriverIt.testIsCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleCorresponding() {
		testDriverIt.testIsMultipleCorrespondingTextSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectIndex() {
		testDriverIt.testIsIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testSelectMultipleIndex() {
		testDriverIt.testIsMultipleIndexSelect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectAll() {
		testDriverIt.testIsAllDeselected();
	}

	
	@Test(groups={"ut"})
	public void testDeselectIndex() {
		testDriverIt.testIsIndexDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectText() {
		testDriverIt.testIsTextDeselect();
	}
	
	@Test(groups={"ut"})
	public void testDeselectValue() {
		testDriverIt.testIsValueDeselect();
	}
	
}
