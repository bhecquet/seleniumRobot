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
package com.seleniumtests.it.core;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestCucumberRunner extends GenericMultiBrowserTest {


	public TestCucumberRunner(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestCucumberRunner() throws Exception {
		super(BrowserType.CHROME, "DriverTestPage");  
	}
	
	@BeforeClass(groups={"it", "ut", "upload"})
	public void closeDriver() {
		if (WebUIDriver.getWebDriver(false) != null) {
			logger.info("closing driver as it's recreated by each test");
			WebUIDriver.cleanUp();
		}
		driver = null;
	}

	@BeforeMethod(groups={"it", "ut", "upload"}) 
	public void skipIfDriverNull() {
		// override default behaviour
	}
	
	
	/**
	 * Check that generic cucumber steps defined in fixture sub-classes can be used
	 * Moreover, we check that user defined parameters can be used through notation '{{ var }}' 
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testEnglishGenericSteps(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			
			// add a user defined variable
			System.setProperty("url", testPageUrl);
			ReporterTest.executeSubCucumberTests("scenario1", 1);
			
			String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
			Assert.assertTrue(mainReportContent.contains(">scenario1</a>"));
			
			// all methods are OK
			Assert.assertEquals(StringUtils.countMatches(mainReportContent, "info=\"ok\""), 1);
		} finally {

			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
	}
}
