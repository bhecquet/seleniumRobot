/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.it.reporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.reporters.CustomReporter;

/**
 * Test that default reporting contains an XML file per test (CustomReporter.java) with default test reports defined in SeleniumTestsContext.DEFAULT_CUSTOM_TEST_REPORTS
 * @author s047432
 *
 */
public class TestPerformanceReporter extends ReporterTest {
	
	private CustomReporter reporter;

	
	@BeforeMethod(groups={"it"})
	private void deleteGeneratedFiles() throws IOException {
		FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()));
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});

		// check all files are generated with the right name
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "PERF-result.xml").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "PERF-result.xml").toFile().exists());

	}
	
	/**
	 * Check all steps of test case are available
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportWithSteps(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		// check content of summary report file
		String jmeterReport = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile());
		
		Assert.assertTrue(jmeterReport.contains("<testsuite errors=\"0\" failures=\"1\" hostname=\"\" name=\"testAndSubActions\" tests=\"6\" time=\"15"));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 3: step 1\" time=\"1.23\">"));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 4: step 2\" time=\"14.03\">"));
		
		// check report contains configuration steps and not internal configuration steps (call to configure() method should not be the first step)
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 2: Pre test step: set\" time="));
	}
	
	/**
	 * Check that when a step contains an exception, this one is written in file
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorWithException(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		// check content of summary report file
		String jmeterReport = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile());
		
		Assert.assertTrue(jmeterReport.contains("<error message=\"driver exception"));
		Assert.assertTrue(jmeterReport.contains("<![CDATA[class org.openqa.selenium.WebDriverException: driver exception"));
		Assert.assertTrue(jmeterReport.contains("at com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions(StubTestClass.java"));
	}	

	/**
	 * Chack that if several custom reports are specified through custom reports, they are all available
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultipleReportsWithSteps(ITestContext testContext) throws Exception {

		try {
			System.setProperty("customTestReports", "PERF::xml::reporter/templates/report.perf.vm,PERF2::json::ti/report.test.vm");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			String jmeterReport1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile());
			String jmeterReport2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF2-result.json").toFile());
			
			Assert.assertTrue(jmeterReport1.contains("<testsuite errors=\"0\" failures=\"1\" hostname=\"\" name=\"testAndSubActions\" tests=\"6\" time=\"15"));
			Assert.assertTrue(jmeterReport2.contains("\"suiteName\": \"testAndSubActions\""));
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	/**
	 * Test that performance reporter is correctly encoded
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testXmlCharacterEscape(ITestContext testContext) throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile());
		
		// check step 1 has been encoded
		Assert.assertTrue(detailedReportContent.contains("name=\"Step 1: step 1 &lt;&gt;&quot;&apos;&amp;/\""));
	}
}
