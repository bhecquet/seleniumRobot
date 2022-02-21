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

import java.io.IOException;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestCustomEventFiringWebDriver2 extends GenericMultiBrowserTest {
	
	
	private final String browserName = "chrome";
	
	public TestCustomEventFiringWebDriver2(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestCustomEventFiringWebDriver2() throws Exception {
		super(BrowserType.CHROME, "DriverTestPage");  
	}
	
	@BeforeMethod(groups={"it"})
	public void initDriver(final ITestContext testNGCtx, final ITestResult testResult) throws Exception {
		initThreadContext(testNGCtx, null, testResult);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserName);
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(0); // no cropping at all
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(0); // no cropping at all
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
//		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
//		SeleniumTestsContextManager.getThreadContext().setFirefoxBinary("path to firefox");
		new DriverTestPage(true, testPageUrl); // start displaying page
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"it"})
	public void testFixedHeaderFooterDimensions() throws IOException {
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getTopFixedHeaderSize(), (Long)7L);
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBottomFixedFooterSize(), (Long)6L);
	}
	
	/**
	 * issue #481: check that when header or footer has a height > 50% of viewport height, it's not considered as a header anymore
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedBigHeaderFooterDimensionsFullHeight() throws IOException {
		driver.manage().window().setSize(new Dimension(500, 700));
		testPage.veryBigHeaderButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getTopFixedHeaderSize(), (Long)0L);
	}
	
	@Test(groups={"it"})
	public void testFixedBigHeaderFooterDimensionsFullHeight2() throws IOException {
		driver.manage().window().setSize(new Dimension(500, 700));
		testPage.veryBigFooterButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBottomFixedFooterSize(), (Long)0L);
	}
	
	/**
	 * issue #481: check that when header or footer has a height < 50% of viewport height, it's considered as a header / footer
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedBigHeaderFooterDimensionsPartialHeight() throws IOException {
		testPage.veryBigHeaderButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getTopFixedHeaderSize(), (Long)300L);
		testPage.veryBigFooterButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBottomFixedFooterSize(), (Long)300L);
	}
	
	/**
	 * issue #443
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedHeaderFooterDimensionsPartialWidth() throws IOException {
		driver.manage().window().setSize(new Dimension(500, 700));
		testPage.bigHeaderButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getTopFixedHeaderSize(), (Long)40L);
		testPage.bigFooterButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBottomFixedFooterSize(), (Long)50L);
	}
	
	/**
	 * issue #443: Check that if footer is less than 50% of page width, we do not consider it as fixed
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedHeaderFooterDimensionsFullWidth() throws IOException {
		testPage.bigHeaderButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getTopFixedHeaderSize(), (Long)7L);
		testPage.bigFooterButton.click();
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBottomFixedFooterSize(), (Long)5L); // should be 6, but has green line is only 1 pixel height, when bigFooter is up, computation fails
	}

}
