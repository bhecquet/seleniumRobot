package com.seleniumtests.ut.uipage.htmlelements;

import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;

public class TestTableWithDriver extends GenericDriverTest {
	
	private DriverTestPage testPage;

	@BeforeClass(groups={"ut"})
	public static void initPage(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
	}
		
	@BeforeMethod(alwaysRun = true)
	public void initDriver() throws Exception {
		driver = WebUIDriver.getWebDriver(true);
		testPage = new DriverTestPage(true);
	}
	
	/* 
	 * the following 2 tests has been written to show a bug when a Table is reused among 2 test cases
	 * In the second test case, if rows are not refreshed explicitly, the reference the old driver instance,
	 * which is closed after the first test, is used
	 * We get: org.openqa.selenium.remote.SessionNotFoundException: Session is closed
	 */
	@Test(groups={"ut"})
	public void testTableRefresh1() {
		Assert.assertEquals(testPage.table.getRows().get(1).findElements(By.tagName("td")).size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testTableRefresh2() {
		Assert.assertEquals(testPage.table.getRows().get(1).findElements(By.tagName("td")).size(), 2);
	}
}
