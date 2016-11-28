package com.seleniumtests.it.driver;

import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;

public class TestTable extends GenericTest {
	
	private static DriverTestPage testPage;
	private static WebDriver driver;

	@BeforeClass(groups={"ut"})
	public static void initPage(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		driver = WebUIDriver.getWebDriver(true);
		testPage = new DriverTestPage(true);
	}
		

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	
	@Test(groups={"ut"})
	public void testGetContent() {
		Assert.assertEquals(testPage.table.getContent(0, 0), "Id");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetContentEmptyTable() {
		testPage.emptyTable.getContent(0, 0);
	}
	
	@Test(groups={"ut"}, expectedExceptions=IndexOutOfBoundsException.class)
	public void testGetContentOutsideRange() {
		Assert.assertEquals(testPage.table.getContent(10, 0), "Id");
	}
	
	@Test(groups={"ut"})
	public void testGetCellFromContent() {
		Assert.assertEquals(testPage.table.getCellFromContent(Pattern.compile("Jav.*"), 1).getText(), "Java");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testGetCellFromWrongContent() {
		testPage.table.getCellFromContent(Pattern.compile("Jai.*"), 1).getText();
	}
	
	@Test(groups={"ut"})
	public void testRowCount() {
		Assert.assertEquals(testPage.table.getRowCount(), 3);
	}
	
	@Test(groups={"ut"})
	public void testRowCountEmptyTable() {
		Assert.assertEquals(testPage.emptyTable.getRowCount(), 0);
	}
	
	@Test(groups={"ut"})
	public void testColCount() {
		Assert.assertEquals(testPage.table.getColumnCount(), 2);
	}
	
}
