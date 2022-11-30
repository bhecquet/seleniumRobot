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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.selenium.AxeBuilderOptions;
import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.WcagChecker;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestAxeCore extends GenericTest {
	
	private static WebDriver driver;


	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		setBrowser();
		new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@BeforeMethod(groups={"it"})
	public void initTimeouts(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);
	}
 
	public void setBrowser() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
	}

	@Test(groups={"it"})
	public void testWcagAnalysisDone() {
		Results result = WcagChecker.analyze(driver);
		Assert.assertEquals(result.getViolations().size(), 10);
	}
	
	@Test(groups={"it"})
	public void testWcagAnalysisDoneWithElements() {
		AxeBuilderOptions options = new AxeBuilderOptions();
		Results result = WcagChecker.analyze(driver, driver.findElement(By.id("image")));
		Assert.assertEquals(result.getViolations().size(), 1);
	}
	
}
