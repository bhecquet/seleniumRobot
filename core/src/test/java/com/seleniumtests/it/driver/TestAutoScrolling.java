package com.seleniumtests.it.driver;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestAutoScrolling extends GenericMultiBrowserTest {
	
	public TestAutoScrolling(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestAutoScrolling() throws Exception {
		super(BrowserType.CHROME, "DriverTestPage");  
	}
	
	/**
	 * issue #262: check we can scroll and click to an element inside div
	 */
//	@Test(groups={"it", "ut"})
//	public void testScrollIntoDiv() {
//		try {
//			DriverTestPage.greenBox.click();
//			Assert.assertEquals(DriverTestPage.textElement.getValue(), "greenbox");
//		} finally {
//			DriverTestPage.resetButton.click();
//			Assert.assertEquals("", DriverTestPage.textElement.getValue());
//		}
//	}
}
