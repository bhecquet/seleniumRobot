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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.app.VelocityEngine;
import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumTestsReporter2;
import com.seleniumtests.reporter.TestLogging;

public class TestSeleniumTestsReporter2 extends MockitoTest {
	
	private SeleniumTestsReporter2 reporter;

	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 * @throws IOException 
	 */
	private XmlSuite executeSubTest(String[] testClasses) throws IOException {
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
		tng.addListener((ITestListener)reporter);
		tng.addListener((IInvokedMethodListener)reporter);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		TestLogging.parseLogFile();
		
		return suite;
	}
	
	@Test(groups={"it"})
	public void testReportGeneration() throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass", "com.seleniumtests.it.reporter.StubTestClass2"});
		
		// check data stored in reporter
		Assert.assertEquals(reporter.getFailedTests().get("StubTestClass").size(), 2);
		Assert.assertEquals(reporter.getFailedTests().get("StubTestClass2").size(), 2);
		Assert.assertEquals(reporter.getSkippedTests().get("StubTestClass").size(), 0);
		Assert.assertEquals(reporter.getSkippedTests().get("StubTestClass2").size(), 2);
		Assert.assertEquals(reporter.getPassedTests().get("StubTestClass").size(), 1);
		Assert.assertEquals(reporter.getPassedTests().get("StubTestClass2").size(), 2);
		
		int errorNb = reporter.getFailedTests().get("StubTestClass").size() 
					+ reporter.getFailedTests().get("StubTestClass2").size()
					+ reporter.getSkippedTests().get("StubTestClass").size()
					+ reporter.getSkippedTests().get("StubTestClass2").size();
		int successNb = reporter.getPassedTests().get("StubTestClass").size()
				    + reporter.getPassedTests().get("StubTestClass2").size();
		
		// check at least one generation occured for each part of the report
		verify(reporter).generateReport(anyList(), anyList(), anyString()); // 1 time only
		verify(reporter).generateSuiteSummaryReport(anyList());				// 1 call
		verify(reporter, times(successNb + errorNb)).generatePanel(any(VelocityEngine.class),any(ITestResult.class)); 	// 1 call per test method => 8 calls
		verify(reporter, times(successNb + errorNb)).generateExecutionReport(any(ITestResult.class));
		verify(reporter).copyResources();

		// check report is complete without error
		Assert.assertEquals(reporter.getGenerationErrorMessage(), null, "error during generation: " + reporter.getGenerationErrorMessage());		
	}
	
	/**
	 * Check summary format when tests have steps
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithSteps(ITestContext testContext) throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		
		Assert.assertTrue(mainReportContent.contains("<a href='SeleniumTestReport-1.html'>testAndSubActions</a>"));
		Assert.assertTrue(mainReportContent.contains("<a href='SeleniumTestReport-2.html'>testInError</a>"));
		
		// check number of steps is correctly computed. "test1" has 2 main steps, "testInError" has 1 step
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-1\">2</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-1\">3</td>"));
		
		// for second test, test is reported KO whereas all steps are OK because we do not use LogAction.aj
		// which handles assertion errors and report them in test steps
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-2\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-2\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-2\">2</td>"));
	}
	
	/**
	 * Check state and style of all tests
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithDependantTests(ITestContext testContext) throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass2"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport-\\d.html'>test1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleFailed\"></i><a href='SeleniumTestReport-\\d.html'>test4</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSkipped\"></i><a href='SeleniumTestReport-\\d.html'>test3</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleFailed\"></i><a href='SeleniumTestReport-\\d.html'>test5</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSkipped\"></i><a href='SeleniumTestReport-\\d.html'>test2</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport-\\d.html'>test1</a>.*"));
	}
	
	/**
	 * Check format of messages in detailed report
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsMessageStyles(ITestContext testContext) throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check style of messages
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">click ok</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-warning\">Warning: Some warning message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">Some Info message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Some Error message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Some log message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<li>send keyboard action</li>"));
	}
	
	/**
	 * Check format of steps inside steps
	 * test1 in com.seleniumtests.it.reporter.StubTestClass defines steps inside other steps
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithSubSteps(ITestContext testContext) throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check content of summary report file
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent.contains(
				"<ul>"													// root step
					+ "<li>click button</li>"
					+ "<li>sendKeys to text field</li>"
					+ "<li>step 1.3: open page</li>"					// sub-step
					+ "<ul>"
						+ "<li>click link</li>"							// action in sub step
						+ "<div class=\"message-log\">a message</div>"	// message in sub step
						+ "<li>sendKeys to password field</li>"			// action in sub step
					+ "</ul>"
				+ "</ul>"));
	}
	
	/**
	 * Check logs are written in file
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithLogs(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter2();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		// check content of detailed report file
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// check log presence
		Assert.assertTrue(detailedReportContent.contains("<div> StubParentClass: Start method testAndSubActions</div>"));
	}
	
	/**
	 * Check all steps are present in detailed report file
	 * Test OK
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsSteps(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter2();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> step 1 - 1.23 secs"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> step 2 - 14.03 secs"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Test end - 0.0 secs"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Execution logs"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is OK</div>"));
	}
	
	/**
	 * Check all errors are recorded in detailed file
	 * - in execution logs
	 * - in Test end step
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithErrors(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter2();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box-body\"><ul><div class=\"message-log\">Test is KO with error: error</div>"));
		
		// Check exception is logged and filtered
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\"><div>class java.lang.AssertionError: error</div>"
								+ "<div class=\"stack-element\"></div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.StubTestClass.testInError(StubTestClass.java:63)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.TestSeleniumTestsReporter2.executeSubTest(TestSeleniumTestsReporter2.java:80)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.TestSeleniumTestsReporter2.testReportDetailsWithErrors(TestSeleniumTestsReporter2.java:282)</div>"));
		
	}
}
