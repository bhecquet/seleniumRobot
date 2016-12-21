/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.PerformanceReporter;
import com.seleniumtests.reporter.SeleniumTestsReporter2;
import com.seleniumtests.reporter.TestListener;

public class TestPerformanceReporter extends MockitoTest {
	
	private PerformanceReporter reporter;

	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 */
	private XmlSuite executeSubTest(String[] testClasses) {
		TestListener testListener = new TestListener();
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		for (String testClass: testClasses) {
			XmlTest test = new XmlTest(suite);
			test.setName(testClass.substring(testClass.lastIndexOf(".") + 1));
			List<XmlClass> classes = new ArrayList<XmlClass>();
			classes.add(new XmlClass(testClass));
			test.setXmlClasses(classes) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.addListener((IReporter)reporter);
		tng.addListener((IInvokedMethodListener)testListener);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
	
	@AfterMethod(alwaysRun=true)
	private void deleteGeneratedFiles() {
		if (reporter == null) {
			return;
		}
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		for (String filePath: reporter.getGeneratedFiles()) {
			new File(outDir + File.separator + filePath).delete();
		}
		
	}
	
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
		
		reporter = spy(new PerformanceReporter());

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check all files are generated with the right name
		Assert.assertTrue(reporter.getGeneratedFiles().contains("PERF-com.seleniumtests.it.reporter.StubTestClass.testAndSubActions.xml"));
		Assert.assertTrue(reporter.getGeneratedFiles().contains("PERF-com.seleniumtests.it.reporter.StubTestClass.testInError.xml"));
		Assert.assertTrue(reporter.getGeneratedFiles().contains("PERF-com.seleniumtests.it.reporter.StubTestClass.testWithException.xml"));
		
		// check at least one generation occured for each part of the report
		verify(reporter).generateReport(anyList(), anyList(), anyString()); // 1 time only
	}
	
	/**
	 * Check all steps of test case are available
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportWithSteps(ITestContext testContext) throws Exception {
		
		reporter = new PerformanceReporter();

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check content of summary report file
		String jmeterReport = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "PERF-com.seleniumtests.it.reporter.StubTestClass.testAndSubActions.xml"));
		
		Assert.assertTrue(jmeterReport.contains("<testsuite errors=\"0\" failures=\"1\" hostname=\"\" name=\"testAndSubActions\" tests=\"2\" time=\"15.26\" timestamp="));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.reporter.StubTestClass\" name=\"step 1\" time=\"1.23\">"));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.reporter.StubTestClass\" name=\"step 2\" time=\"14.03\">"));
	}
	
	/**
	 * Check that when a step contains an exception, this one is written in file
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorWithException(ITestContext testContext) throws Exception {
		
		reporter = new PerformanceReporter();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check content of summary report file
		String jmeterReport = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "PERF-com.seleniumtests.it.reporter.StubTestClass.testAndSubActions.xml"));
		
		Assert.assertTrue(jmeterReport.contains("<error message=\"driver exception"));
		Assert.assertTrue(jmeterReport.contains("<![CDATA[class org.openqa.selenium.WebDriverException: driver exception"));
		Assert.assertTrue(jmeterReport.contains("at com.seleniumtests.it.reporter.StubTestClass.testAndSubActions(StubTestClass.java:37)"));
	}
	
	/**
	 * Check that when a step is failed without exception, a generic message is written in file
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorWithoutException(ITestContext testContext) throws Exception {
		
		reporter = new PerformanceReporter();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check content of summary report file
		String jmeterReport = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "PERF-com.seleniumtests.it.reporter.StubTestClass.testInError.xml"));
		
		Assert.assertTrue(jmeterReport.contains("<error message=\"Step in error\" type=\"\">"));
		Assert.assertTrue(jmeterReport.contains("<![CDATA[Error message not available]]>"));
	}
}
