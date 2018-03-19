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
import java.lang.reflect.Method;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.reporters.SeleniumTestsReporter2;

public class TestSeleniumTestsReporter2 extends ReporterTest {
	
	private SeleniumTestsReporter2 reporter;

	@BeforeMethod(groups={"it"})
	public void setLogs(Method method, ITestContext context) {
		TestLogging.reset();
	}
	
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
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-\\d+\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-\\d+\\.html'.*?>testInError</a>.*"));
	}
	
	/**
	 * Check summary format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultithreadTestReport() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, XmlSuite.ParallelMode.TESTS);
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-\\d+\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-\\d+\\.html'.*?>testInError</a>.*"));
	}
	
	/**
	 * Check summary format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithSteps() throws Exception {

		System.setProperty("optimizeReports", "true");
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-1\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-2\\.html'.*?>testInError</a>.*"));
		
		// check number of steps is correctly computed. "test1" has 2 main steps, "testInError" has 1 step
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-1\">5</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\" class=\"failedSteps\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-1\">6</td>"));
		
		// for second test, test is reported KO whereas all steps are OK because we do not use LogAction.aj
		// which handles assertion errors and report them in test steps
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-2\">4</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-2\" class=\"failedSteps\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-2\">5</td>"));
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
		String detailedReportContent1 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent1.contains("<h4> Test Details - testAndSubActions</h4><pre>a test with steps</pre>"));
		
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
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
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-1\\.html'.*?>testFailedWithException</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-2\\.html'.*?>testFailedWithSoftAssertDisabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-3\\.html'.*?>testFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-4\\.html'.*?>testMultipleFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-5\\.html'.*?>testOk</a>.*"));
		

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		Assert.assertTrue(detailedReportContent2.contains("</button> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button> assertAction"));
		Assert.assertFalse(detailedReportContent2.contains("</button> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button> Test end"));
		
		// check that with soft assertion, all steps are displayed
		String detailedReportContent3 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-3.html"));
		Assert.assertTrue(detailedReportContent3.contains("</button> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button> assertAction"));
		Assert.assertTrue(detailedReportContent3.contains("</button> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button> Test end"));
		
		// check that with error, remaining steps are skipped
		String detailedReportContent1 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		Assert.assertTrue(detailedReportContent1.contains("</button> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button> failAction"));
		Assert.assertFalse(detailedReportContent1.contains("</button> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button> Test end"));
		
	}
	
	@Test(groups={"it"})
	public void testSnapshotRenaming() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check that with error, remaining steps are skipped
		String detailedReportContent1 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		Assert.assertTrue(detailedReportContent1.contains("Output: null:  | <a href='screenshot/testAndSubActions_1-1_step_1-img_with_very_very_ve.png'"));		
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
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-1\\.html'.*?>testOk</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-2\\.html'.*?>testWithAssert</a>.*"));
		
		
		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent1 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
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
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
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
	 * Check state and style of all tests
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithDependantTests() throws Exception {
	
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass2"});
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport-\\d.html'>test1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleFailed\"></i><a href='SeleniumTestReport-\\d.html'>test4</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSkipped\"></i><a href='SeleniumTestReport-\\d.html'>test3</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleFailed\"></i><a href='SeleniumTestReport-\\d.html'>test5</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSkipped\"></i><a href='SeleniumTestReport-\\d.html'>test2</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport-\\d.html'>test1</a>.*"));
		Assert.assertFalse(mainReportContent.contains("$testResult.getAttribute(\"methodName\")")); // check all test methods are filled
	}
	
	/**
	 * Check format of messages in detailed report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsMessageStyles() throws Exception {
		
		reporter = spy(new SeleniumTestsReporter2());
		System.setProperty("customTestReports", "PERF::xml::reporter/templates/report.perf.vm,SUP::xml::reporter/templates/report.supervision.vm");

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		
		// check style of messages
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">click ok</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-warning\">Warning: Some warning message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">Some Info message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Some Error message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Some log message</div>"));
		Assert.assertTrue(detailedReportContent.contains("<table class=\"table table-bordered table-condensed\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td>key</td><td>we found a value of</td><td>10</td></tr></table>"));
		Assert.assertTrue(detailedReportContent.contains("<li>send keyboard action</li>"));
		
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
					+ "<div class=\"message-snapshot\">Output: null:  | <a href='screenshot/testAndSubActions_1-1_step_1-img_with_very_very_ve.png' class='lightbox'>Application Snapshot</a></div>"
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
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
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
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
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
		
		reporter = new SeleniumTestsReporter2();
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"});
		
		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,])</li>"));
		Assert.assertTrue(detailedReportContent1.contains("<li>click on ButtonElement Reset, by={By.id: button2} </li>"));
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-snapshot\">Output: Current Window: Test page: <a href="));
		
		// check that only on reference to 'click' is present for this buttonelement. This means that only the replayed action has been logged, not the ButtonElement.click() one
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "click on"), 1);
		
		// read the 'testDriverNativeActions' test result to see if native actions are also logged (overrideSeleniumNativeAction is true)
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent2.contains("<li>sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [some text,])</li>"));
		Assert.assertTrue(detailedReportContent2.contains("<li>click on HtmlElement , by={By.id: button2} </li>"));
		
		// read the 'testDriverNativeActionsWithoutOverride' test result to see if native actions are not logged (overrideSeleniumNativeAction is false)
		String detailedReportContent3 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-3.html"));
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
		Assert.assertTrue(detailedReportContent1.contains("<ul><li>clickAt on Picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li>"));
		
		// check that when logging PictureElement action which uses composite actions, those are not logged
		Assert.assertTrue(!detailedReportContent1.contains("<ul><li>clickAt on Picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li><li>moveToElement with args:"));
		
		// no action is logged when step fails (findElement exception). Ok because logging is done on action, not search 
		
		
		// check that seleniumRobot actions are logged only once when overrideNativeAction is enabled (issue #88)
		String detailedReportContent4 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-4.html"));
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
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box-body\"><ul><div class=\"message-log\">Test is KO with error: error</div>"));
		
		// /!\: lines in error message may change
		// Check exception is logged and filtered
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error\"><div>class java.lang.AssertionError: error</div>"
								+ "<div class=\"stack-element\"></div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.stubclasses.StubTestClass.testInError\\(StubTestClass.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.TestSeleniumTestsReporter2.testReportDetailsWithErrors\\(TestSeleniumTestsReporter2.java:\\d+\\)</div>.*"));
		
		Assert.assertTrue(detailedReportContent.contains("</ul><div class=\"message-error\">				java.lang.AssertionError: error			</div></div>"));
	}
	
	/**
	 * Check BeforeXXX configuration error is recorded in detailed file
	 * - in execution logs
	 * - a configuration step is displayed
	 * Check that overall step is skipped
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithBeforeConfigurationError() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForConfigurationError1"}); 
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		// check main result is skipped with step failed in red
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\" class=\"failedSteps\">1</td>"));
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");

		// check test is skipped as before method failed
		Assert.assertTrue(detailedReportContent.contains("<header class='main-header header-skipped'>"));
		
		// Check details of the configuration error is displayed in report (this behaviour is controled by TestNG which adds exception from BeforeXXX to test method throwable)
		Assert.assertTrue(detailedReportContent.contains("<div>class com.seleniumtests.customexception.ConfigurationException: Some error before method</div>"));
		
		// check we have a step for BeforeMethod and it's marked as failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Pre test step: beforeMethod"));
		
		// Check details of the configuration error is displayed in report
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">				com.seleniumtests.customexception.ConfigurationException: Some error before method"));
				
	}
	
	/**
	 * Check AfterXXX configuration error is recorded in detailed file
	 * - a specific step is displayed
	 * - logs of this specific step is present in execution logs
	 * Check that overall test is OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithAfterConfigurationError() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForConfigurationError2"}); 
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		// check main result is skipped with step failed in red
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\" class=\"failedSteps\">1</td>"));
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check details of the configuration error is displayed in report
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">				com.seleniumtests.customexception.ConfigurationException: Some error after method"));
		
		// check test is still OK as only after method failed
		Assert.assertTrue(detailedReportContent.contains("<header class='main-header header-success'>"));
		
		// check execution log does not contain our post configuration step
		Assert.assertFalse(detailedReportContent.contains("<div>class com.seleniumtests.customexception.ConfigurationException: Some error after method</div>"));
		
		// check we have a step for AfterMethod and it's marked as failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Post test step: afterMethod"));
		
		// check logs written in @AfterXXX are present in execution logs
		Assert.assertTrue(detailedReportContent.contains("[main] TestLogging: some warning</div>"));
	}
	
	/**
	 * Check that all configuration steps are logged in detailed report as pre / post test actions
	 * Also check that configuration step name does not contain method arguments
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsAllConfigurationSteps() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener1"}); 
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent.contains("</i></button> Pre test step: beforeMethod -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Pre test step: beforeTest -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Pre test step: beforeClass -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Post test step: afterMethod -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Post test step: afterClass -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Post test step: afterTest -"));
		
		// check reference to configuration methods for class / test / method are in both results (some are common)
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Pre test step: beforeMethod -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Pre test step: beforeTest -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Pre test step: beforeClass -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Post test step: afterMethod -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Post test step: afterClass -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Post test step: afterTest -"));
	}
	
	/**
	 * detailed report should contain only configuration steps corresponding to the test method / test class / test (TestNG test)
	 * By default, test context contains all configuration methods. Check we filter them and we have only one configuration step even if it's retried
	 * (case where test method fails and is retried, \@BeforeMethod is then called several times
	 */
	@Test(groups={"it"})
	public void testReportDetailsOnlyTestConfigurationSteps() throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</i></button> Pre test step: set -"), 1);
		
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "</i></button> Pre test step: set -"), 1);
		
		String detailedReportContent3 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-3.html"));
		detailedReportContent3 = detailedReportContent3.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "</i></button> Pre test step: set -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "</i></button> Post test step: reset -"), 1);
		
		// in case of test method error, it is retried so each Before/After method is also replayed. Check it's the last one we have
		Assert.assertTrue(detailedReportContent3.contains("<div class=\"message-info\">before count: 2</div>"));
		Assert.assertTrue(detailedReportContent3.contains("<div class=\"message-info\">after count: 3</div>"));
	}
	
	/**
	 * Check test values are displayed (call to TestLogging.logTestValue()) shown as a table
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithTestValues() throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<table class=\"table table-bordered table-condensed\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td>key</td><td>we found a value of</td><td>10</td></tr></table>"));
	}
	
	@Test(groups={"it"})
	public void testReportDetailsContainsParentConfigurations() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener1"});
		
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
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
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-1\\.html'.*?>core_3</a>.*"));
	
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
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
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='SeleniumTestReport-1\\.html'.*?>core_3</a>.*"));
	}
}
