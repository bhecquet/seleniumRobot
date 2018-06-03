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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.reporters.SeleniumTestsReporter2;

public class TestSeleniumTestsReporter2 extends ReporterTest {
	
	private SeleniumTestsReporter2 reporter;
	
	/**
	 * Disabled because now, it's not easy to get the SeleniumRobotReporterInstance created by testNg
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=false)
	public void testReportGeneration() throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClass2"});

		// check at least one generation occured for each part of the report
		verify(reporter).generateReport(anyList(), anyList(), anyString()); // 1 time only
		verify(reporter).generateSuiteSummaryReport(anyList());				// 1 call
		verify(reporter, times(9)).generatePanel(any(VelocityEngine.class),any(ITestResult.class)); 	// 1 call per test method => 8 calls
		verify(reporter, times(9)).generateExecutionReport(any(ITestResult.class));
		verify(reporter).copyResources();

		// check report is complete without error (issue #100)
		Assert.assertEquals(reporter.getGenerationErrorMessage(), null, "error during generation: " + reporter.getGenerationErrorMessage());		
	}
	
	/**
	 * Check summary format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultithreadReport() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
	}
	
	/**
	 * Check summary format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultithreadTestReport() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, XmlSuite.ParallelMode.TESTS, new String[] {});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
	}
	
	/**
	 * Check issue #143 where all \@AfterMethod calls are displayed in all test if its first parameter is not a method reference 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testTestReportContainsOnlyItsAfterMethodSteps() throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForIssue143"});
		
		String detailedReportContent1 = readTestMethodResultFile("testOk1");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Post test step: reset2"), 1);
		
		String detailedReportContent2 = readTestMethodResultFile("testOk2");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Post test step: reset2"), 1);
		
		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("When using @AfterMethod in tests")); // check error message is shown
		Assert.assertTrue(logs.contains("public void com.seleniumtests.it.stubclasses.StubTestClassForIssue143.reset()")); // check method name is displayed
	}
	
	/**
	 * Check issue #141 where \@AfterMethod calls are displayed as many times as test retries
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetriedTestReportContainsOnlyItsAfterMethod() throws Exception {
		System.setProperty("testRetryCount", "1");
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForIssue141"});
		
		// check we only display the second call 
		String detailedReportContent1 = readTestMethodResultFile("testOk1");
		Assert.assertFalse(detailedReportContent1.contains("<div class=\"message-info\">after method 1</div>"));
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-info\">after method 2</div>"));
		
	}
	
	/**
	 * Check summary format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithSteps() throws Exception {

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
		
		// check number of steps is correctly computed. "test1" has 2 main steps, "testInError" has 1 step
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-1\">5</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\" class=\"failedSteps\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-1\">6</td>"));
		
		// for second test, test is reported KO whereas all steps are OK because we do not use LogAction.aj
		// which handles assertion errors and report them in test steps
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-2\">4</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-2\" class=\"failedSteps\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-2\">5</td>"));
		
		// check full log file is there
		Assert.assertTrue(mainReportContent.contains("<a href=\"seleniumRobot.log\""));
	}
	
	/**
	 * Check resources referenced in header are get from CDN and resources files are not copied to ouput folder
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportWithResourcesFromCDN() throws Exception {
		
		try {
			System.setProperty("optimizeReports", "true");
			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		} finally {
			System.clearProperty("optimizeReports");
		}
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.contains("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com"));
		
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "AdminLTE.min.css").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "bootstrap.min.css").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "fonts").toFile().exists());
	}
	
	/**
	 * Check resources referenced in header are get from local and resources files are  copied to ouput folder
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportWithResourcesFromLocal() throws Exception {
		
		try {
			System.setProperty("optimizeReports", "false");
			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		} finally {
			System.clearProperty("optimizeReports");
		}
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.contains("<script src=\"resources/templates/bootstrap.min.js\"></script>"));
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "AdminLTE.min.css").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "bootstrap.min.css").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "fonts").toFile().exists());
	}
	
	/**
	 * Check if test description made available by TestNG annotation is displayed in summary and detailed report
	 */
	@Test(groups={"it"})
	public void testTestDescription() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		
		// if description is available, it's displayed
		// else, "no description available" is shown
		Assert.assertTrue(mainReportContent.contains("data-toggle=\"tooltip\" title=\"a test with steps\">testAndSubActions</a>"));
		Assert.assertTrue(mainReportContent.contains("data-toggle=\"tooltip\" title=\"no description available\">testInError</a>"));
		Assert.assertTrue(mainReportContent.contains("data-toggle=\"tooltip\" title=\"no description available\">testWithException</a>"));
		
		// Check description is displayed if available
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
		detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent1.contains("<h4> Test Details - testAndSubActions</h4><pre>a test with steps</pre>"));
		
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "TestReport.html").toFile());
		Assert.assertFalse(detailedReportContent2.contains("<h4> Test Details - testInError</h4><pre>"));
		
	}
	
	/**
	 * Check that automatic steps create all steps in report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testAutomaticSteps() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testFailedWithException/TestReport\\.html'.*?>testFailedWithException</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testFailedWithSoftAssertDisabled/TestReport\\.html'.*?>testFailedWithSoftAssertDisabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testFailedWithSoftAssertEnabled/TestReport\\.html'.*?>testFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testMultipleFailedWithSoftAssertEnabled/TestReport\\.html'.*?>testMultipleFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testOk/TestReport\\.html'.*?>testOk</a>.*"));
		

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testFailedWithSoftAssertDisabled", "TestReport.html").toFile());
		Assert.assertTrue(detailedReportContent2.contains("</button> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button> assertAction"));
		Assert.assertFalse(detailedReportContent2.contains("</button> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button> Test end"));
		
		// check that with soft assertion, all steps are displayed
		String detailedReportContent3 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testFailedWithSoftAssertEnabled", "TestReport.html").toFile());
		Assert.assertTrue(detailedReportContent3.contains("</button> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button> assertAction"));
		Assert.assertTrue(detailedReportContent3.contains("</button> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button> Test end"));
		
		// check that with error, remaining steps are skipped
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testFailedWithException", "TestReport.html").toFile());
		Assert.assertTrue(detailedReportContent1.contains("</button> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button> failAction"));
		Assert.assertFalse(detailedReportContent1.contains("</button> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button> Test end"));
		
	}
	
	@Test(groups={"it"})
	public void testAttachmentRenaming() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check that with error, remaining steps are skipped
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
		Assert.assertTrue(detailedReportContent1.contains(" | <a href='screenshot/testAndSubActions_1-1_step_1-img_with_very_very_ve.png'"));		
		Assert.assertTrue(detailedReportContent1.contains(" | <a href='htmls/testAndSubActions_1-1_step_1-html_with_very_very_v.html' target=html>"));	
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_1-1_step_1-html_with_very_very_v.html").toFile().exists());
	}
	
	@Test(groups={"it"})
	public void testAttachmentRenamingWithOptimizeReports() throws Exception {
		try {
			System.setProperty("optimizeReports", "true");
			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		} finally {
			System.clearProperty("optimizeReports");
		}
		
		// check that with error, remaining steps are skipped
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
		Assert.assertTrue(detailedReportContent1.contains(" | <a href='screenshot/testAndSubActions_1-1_step_1-img_with_very_very_ve.png'"));		
		Assert.assertTrue(detailedReportContent1.contains(" | <a href='htmls/testAndSubActions_1-1_step_1-html_with_very_very_v.html.zip' target=html>"));	
		
		// check file has been moved
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_1-1_step_1-html_with_very_very_v.html.zip").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_1-1_step_1-html_with_very_very_v.html").toFile().exists());
	}
	
	/**
	 * Check that manual steps create all steps in report
	 * manual step option is set inside the StubTestClassManualSteps.testOk() method
	 * check the failed test case where step should be marked as KO
	 * Also, error in step should be presented
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testManualSteps() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassManualSteps"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testOk/TestReport\\.html'.*?>testOk</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testWithAssert/TestReport\\.html'.*?>testWithAssert</a>.*"));
		
		
		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testOk", "TestReport.html").toFile());
		Assert.assertTrue(detailedReportContent1.contains("</button> Test start"));
		Assert.assertTrue(detailedReportContent1.contains("</button> add some values"));
		Assert.assertTrue(detailedReportContent1.contains("</button> minus 2"));
		Assert.assertTrue(detailedReportContent1.contains("</button> do nothing"));
		Assert.assertTrue(detailedReportContent1.contains("</button> Test end"));
		
		// check that configuration steps are automatically added
		Assert.assertTrue(detailedReportContent1.contains("</button> Pre test step: set - "));
		Assert.assertTrue(detailedReportContent1.contains("</button> Post test step: teardown -"));
		
		// assert automatic steps are not present
		Assert.assertFalse(detailedReportContent1.contains("</button> add with args"));
		
		// check we also get actions
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "doNothing on HtmlElement none"), 3);
		
		// ----- check manual steps errors ------
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithAssert", "TestReport.html").toFile());
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "");
		
		// check execution logs are in error
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\">			<div class=\"box-header with-border\">			<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Execution logs"));
		
		// test first step is OK and second one is failed (this shows indirectly that internal step is marked as failed
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box success\">			<div class=\"box-header with-border\">			<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Test start"));
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\">			<div class=\"box-header with-border\">			<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> assert exception"));
		
		// check exception is present in step
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"message-log\">Test is KO with error: false error expected [true] but found [false]</div>"));
		
	}
	
	/**
	 * Check that manual also mask password if user requests it (gives password to mask in report)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testManualStepsPasswordMasking() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassManualSteps"}, ParallelMode.METHODS, new String[] {"testOkPassword"});

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testOkPassword", "TestReport.html").toFile());
		
		// if step specifies string to mask, hide it
		Assert.assertFalse(detailedReportContent1.contains("<div class=\"message-info\">password is aPassPhrase</div>"));	
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-info\">password is ******</div>"));
		
		// if step does not specifies string to mask, it's displayed
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-info\">password is anOtherPassPhrase</div>"));
	}
	
	/**
	 * Check state and style of all tests
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithDependantTests() throws Exception {
	
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass2"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*class\\=\"fa fa-circle circleSuccess\"></i><a href\\='test1/TestReport\\.html'.*?>test1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class\\=\"fa fa-circle circleFailed\"></i><a href\\='test4/TestReport\\.html'.*?>test4</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class\\=\"fa fa-circle circleSkipped\"></i><a href\\='test3/TestReport\\.html'.*?>test3</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class\\=\"fa fa-circle circleFailed\"></i><a href\\='test5/TestReport\\.html'.*?>test5</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class\\=\"fa fa-circle circleSkipped\"></i><a href\\='test2/TestReport\\.html'.*?>test2</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class\\=\"fa fa-circle circleSuccess\"></i><a href\\='test6/TestReport\\.html'.*?>test6</a>.*"));
		Assert.assertFalse(mainReportContent.contains("$testResult.getAttribute(\"methodName\")")); // check all test methods are filled
	}
	
	/**
	 * Check format of messages in detailed report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsMessageStyles() throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());
		try {
			System.setProperty("customTestReports", "PERF::xml::reporter/templates/report.perf.vm,SUP::xml::reporter/templates/report.supervision.vm");
	
			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
			
			
			// check style of messages
			String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "TestReport.html").toFile());
			detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
			
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">click ok</div>"));
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-warning\">Warning: Some warning message</div>"));
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">Some Info message</div>"));
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Some Error message</div>"));
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Some log message</div>"));
			Assert.assertTrue(detailedReportContent.contains("<table class=\"table table-bordered table-condensed\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td>key</td><td>we found a value of</td><td>10</td></tr></table>"));
			Assert.assertTrue(detailedReportContent.contains("<li>send keyboard action</li>"));
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	/**
	 * Check format of steps inside steps
	 * test1 in com.seleniumtests.it.stubclasses.StubTestClass defines steps inside other steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithSubSteps() throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check content of summary report file
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
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
					+ "<div class=\"message-snapshot\">Output: null:  | <a href='htmls/testAndSubActions_1-1_step_1-html_with_very_very_v.html' target=html>Application HTML Source</a> | <a href='screenshot/testAndSubActions_1-1_step_1-img_with_very_very_ve.png' class='lightbox'>Application Snapshot</a></div>"
				+ "</ul>"));
		
	}
	
	/**
	 * Check logs are written in file
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithLogs() throws Exception {
		
		reporter = new SeleniumTestsReporter2();
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check content of detailed report file
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// check log presence
		Assert.assertTrue(detailedReportContent.contains("[main] SeleniumRobotTestListener: Start method testAndSubActions</div>"));
		
	}
	
	/**
	 * Check all steps are present in detailed report file
	 * Test OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsSteps() throws Exception {
		
		reporter = new SeleniumTestsReporter2();
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> step 1 - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> step 2 - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Test end - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Execution logs"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is OK</div>"));
		
		// check logs are written only once 
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "[main] TestLogging: Test is OK</div>"), 1);
		
	}
	
	@Test(groups={"it"})
	public void testReportContainsCustomScreenshot() throws Exception {
		
		System.setProperty(SeleniumTestsContext.CAPTURE_NETWORK, "true");
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
		
		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverCustomSnapshot", "TestReport.html").toFile());
		detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent1.contains("<a href='screenshots/my_snapshot"));	
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/my_snapshot"));	
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-snapshot\">Output: my snapshot:"));	
	}
	
	
	/**
	 * Check that HAR capture file is present in result
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsHarCapture() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.CAPTURE_NETWORK, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "TestReport.html").toFile());
			detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
			
			Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,])</li>"));	
			Assert.assertTrue(detailedReportContent1.contains("Network capture: <a href='networkCapture.har'>HAR file</a>"));
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "networkCapture.har").toFile().exists());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.CAPTURE_NETWORK);
		}
		
	}
	
	/**
	 * Check that HAR capture file is not present in result if option is disabled
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDoNotContainsHarCapture() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.CAPTURE_NETWORK, "false");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "TestReport.html").toFile());
			detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
			
			Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,])</li>"));	
			Assert.assertFalse(detailedReportContent1.contains("Network capture: <a href='networkCapture.har'>HAR file</a>"));
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "networkCapture.har").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.CAPTURE_NETWORK);
		}
		
	}
	
	/**
	 * Check all actions done with driver are correctly displayed. This indirectly test the LogAction aspect
	 * We check 
	 * - all HtmlElement action logging
	 * - all composite actions logging
	 * - all PictureElement action logging
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsDriverActions() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver", "testDriverNativeActions", "testDriverNativeActionsWithoutOverride", "testDriverWithHtmlElementWithoutOverride"});
		
		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "TestReport.html").toFile());
		detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,])</li>"));
		Assert.assertTrue(detailedReportContent1.contains("<li>click on ButtonElement Reset, by={By.id: button2} </li>"));
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-snapshot\">Output: Current Window: Test page: <a href="));
		
		// check that only on reference to 'click' is present for this buttonelement. This means that only the replayed action has been logged, not the ButtonElement.click() one
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "click on"), 1);
		
		// read the 'testDriverNativeActions' test result to see if native actions are also logged (overrideSeleniumNativeAction is true)
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverNativeActions", "TestReport.html").toFile());
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent2.contains("<li>sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [some text,])</li>"));
		Assert.assertTrue(detailedReportContent2.contains("<li>click on HtmlElement , by={By.id: button2} </li>"));
		
		// read the 'testDriverNativeActionsWithoutOverride' test result to see if native actions are not logged (overrideSeleniumNativeAction is false)
		String detailedReportContent3 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverNativeActionsWithoutOverride", "TestReport.html").toFile());
		detailedReportContent3 = detailedReportContent3.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// logging is not done via HtmlElement
		Assert.assertFalse(detailedReportContent3.contains("<li>sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [some text,])</li>"));
		Assert.assertFalse(detailedReportContent3.contains("<li>click on HtmlElement , by={By.id: button2} </li>"));
		
		// check that without override, native actions are logged
		Assert.assertTrue(detailedReportContent3.contains("<ul><li>sendKeys on Element located by id: text2 with args: ([some text,])</li></ul>"));
		Assert.assertTrue(detailedReportContent3.contains("<ul><li>click on Element located by id: button2 </li></ul>"));
		Assert.assertTrue(detailedReportContent3.contains("<ul><li>selectByVisibleText on Select with args: (option1, )</li></ul>"));
				
		// check composite actions. We must have the moveToElement, click and sendKeys actions 
		Assert.assertTrue(detailedReportContent1.contains("<ul><li>moveToElement with args: (TextFieldElement Text, by={By.id: text2}, )</li><li>sendKeys with args: ([composite,])</li><li>moveToElement with args: (ButtonElement Reset, by={By.id: button2}, )</li><li>click </li></ul>"));
		
		// check PictureElement action is logged
		Assert.assertTrue(detailedReportContent1.contains("<ul><li>clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li>"));
		
		// check that when logging PictureElement action which uses composite actions, those are not logged
		Assert.assertFalse(detailedReportContent1.contains("<ul><li>clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li><li>moveToElement with args:"));
		
		// no action is logged when step fails (findElement exception). Ok because logging is done on action, not search 
		
		
		// check that seleniumRobot actions are logged only once when overrideNativeAction is enabled (issue #88)
		String detailedReportContent4 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithHtmlElementWithoutOverride", "TestReport.html").toFile());
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent4, "<li>click on ButtonElement Reset, by={By.id: button2} </li>"), 1);
		
		// TODO: spliter ce test en plusieurs 
		
	}
	
	/**
	 * Check all errors are recorded in detailed file
	 * - in execution logs
	 * - in Test end step
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithErrors() throws Exception {
		
		reporter = new SeleniumTestsReporter2();
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "TestReport.html").toFile());
		
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box-body\"><ul><div class=\"message-log\">Test is KO with error: error</div>"));
		
		// Check exception is logged and filtered
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error\"><div>class java.lang.AssertionError: error</div>"
								+ "<div class=\"stack-element\"></div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.stubclasses.StubTestClass.testInError\\(StubTestClass.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.TestSeleniumTestsReporter2.testReportDetailsWithErrors\\(TestSeleniumTestsReporter2.java:\\d+\\)</div>.*"));
		
		// error message of the assertion is displayed in step
		Assert.assertTrue(detailedReportContent.matches(".*</ul><div class=\"message-error\">\\s+class java.lang.AssertionError: error\\s+</div></div>.*"));
		
		// check that when test is KO, error cause is displayed
		Assert.assertTrue(detailedReportContent.contains("[main] TestLogging: Test is KO with error: "));
	}
	
	
	/**
	 * Check test values are displayed (call to TestLogging.logTestValue()) shown as a table
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithTestValues() throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "TestReport.html").toFile());
		
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<table class=\"table table-bordered table-condensed\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td>key</td><td>we found a value of</td><td>10</td></tr></table>"));
	}
	
	@Test(groups={"it"})
	public void testReportDetailsContainsParentConfigurations() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener1"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "test1Listener1", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button> Pre test step: beforeTestInParent - "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button> Pre test step: beforeTest -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button> Post test step: afterClassInParent - "), 1);
		
	}
		
	
	/**
	 * Check all steps are present in detailed report file. For cucumber, check that method name is the Scenario name, not the "feature" generic method
	 * Test OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberStart() throws Exception {
		
		executeSubCucumberTests("core_3", 1);

		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_3/TestReport\\.html'.*?>core_3</a>.*"));
	
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "core_3", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> write (\\w+) with args: (tutu, )"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is OK</div>"));
	}
	/**
	 * Check that test name is correctly reported in cucumber mode when threads are used
	 * Test OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberMultiThread() throws Exception {
		
		executeSubCucumberTests("core_3,core_4", 5);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_3/TestReport\\.html'.*?>core_3</a>.*"));
	}
	

	/**
	 * Test that HTML report is correctly encoded
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testHtmlCharacterEscape(ITestContext testContext) throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// check step 1 has been encoded
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is KO with error: some exception with &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;"));
		
		// check logs are also encoded
		Assert.assertTrue(detailedReportContent.contains("[main] TestLogging: Test is KO with error: some exception with &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;"));
		
		// check exception stack trace is encoded
		Assert.assertTrue(detailedReportContent.contains("class com.seleniumtests.customexception.DriverExceptions: some exception with &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;"));
		
		// check no HTML code remains in file
		Assert.assertFalse(detailedReportContent.contains("<strong>"));
	}
}
