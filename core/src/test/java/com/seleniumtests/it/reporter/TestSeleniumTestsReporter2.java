/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.reporter;

import com.seleniumtests.connectors.extools.FFMpeg;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.it.stubclasses.StubTestClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class TestSeleniumTestsReporter2 extends ReporterTest {

	private static String videoFileName = "videoCapture.avi";

	@BeforeClass(alwaysRun = true, groups = "ut")
	public static void init() {
		try {
			new FFMpeg();
			videoFileName = "videoCapture.mp4";
		} catch (Exception e) {
			// nothing to do
		}
	}

	@Test(groups = {"it"})
	public void testDriverLogBrowserFileInTestEndStep() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForGenericSteps"}, ParallelMode.METHODS, new String[]{"testDriver"});
		
		// check content of Test end steps
		String testOkDetailedReport = readTestMethodResultFile("testDriver");
		
		Assert.assertTrue(testOkDetailedReport.matches(".*<span class=\"step-title\"> Test end -.*<div class=\"message-snapshot\">Browser log file: <a href='driver-log-browser.txt'>file</a></div><div class=\"message-har\">Network capture 'main' browser:.*"));
	}
	
	@Test(groups = {"it"})
	public void testErrorOnVariableServerWithTestNameInReport() throws Exception {
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, SERVER_URL);
			
			configureMockedVariableServerConnection();
			createServerMock(SERVER_URL,
				"GET",
				SeleniumRobotVariableServerConnector.VARIABLE_API_URL,
				500,
                    List.of(
                            "VARIABLE NOT FOUND"
                    ),
				"request"
			);
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testSkipped", "testInError"});
			
			// check content of summary report file
			String mainReportContent = readSummaryFile();
			String testOkDetailedReport = readTestMethodResultFile("testAndSubActions");
			String testKoDetailedReport = readTestMethodResultFile("testInError");
			String testSkipDetailedReport = readTestMethodResultFile("testSkipped");
			
			Assert.assertTrue(mainReportContent.matches(".*class=\"testSkipped\".*<a href='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
			Assert.assertTrue(mainReportContent.matches(".*class=\"testSkipped\".*<a href='testInError/TestReport\\.html'.*?>testInError</a>.*"));
			Assert.assertTrue(mainReportContent.matches(".*class=\"testSkipped\".*<a href='testSkipped/TestReport\\.html'.*?>testSkipped</a>.*"));
			Assert.assertTrue(testOkDetailedReport.matches(".*Execution logs {28}</div><div class=\"box-body logs\"><div class=\"message-error\"><div>class com.seleniumtests.customexception.SeleniumRobotServerException: An error occurred while fetching variables from the SeleniumRobot Server. Test execution is skipped.</div>.*"));
			Assert.assertTrue(testKoDetailedReport.matches(".*Execution logs {28}</div><div class=\"box-body logs\"><div class=\"message-error\"><div>class com.seleniumtests.customexception.SeleniumRobotServerException: An error occurred while fetching variables from the SeleniumRobot Server. Test execution is skipped.</div>.*"));
			Assert.assertTrue(testSkipDetailedReport.matches(".*Execution logs {28}</div><div class=\"box-body logs\"><div class=\"message-error\"><div>class com.seleniumtests.customexception.SeleniumRobotServerException: An error occurred while fetching variables from the SeleniumRobot Server. Test execution is skipped.</div>.*"));
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty("mockTestExecutionMethod");
		}
	}
	
	/**
	 * Check summary format in multithread
	 */
	@Test(groups = {"it"})
	public void testMultithreadReport() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testInError/TestReport\\.html'.*?>testInError</a>.*"));

		// issue #331: check that result files have been generated only once
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
        Assert.assertEquals(StringUtils.countMatches(logs, "testInError/PERF-result.xml"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testInError/detailed-result.json"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testAndSubActions/PERF-result.xml"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testAndSubActions/detailed-result.json"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testWithException/PERF-result.xml"), 3); // once per retry
        Assert.assertEquals(StringUtils.countMatches(logs, "testWithException/detailed-result.json"), 3); // once per retry

		// issue  #312: check that result files have been generated before test end
		Assert.assertTrue(Strings.CS.indexOf(logs, "testInError/PERF-result.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testInError/detailed-result.json.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testAndSubActions/PERF-result.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testAndSubActions/detailed-result.json") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testWithException/PERF-result.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testWithException/detailed-result.json") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
	}

	/**
	 * Test that if a test is called with "invocationCount", we have as many results as invocations
	 */
	@Test(groups = {"it"})
	public void testMultithreadReportWithInvocationCount() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testOkWithInvocationCount"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		// check we have the 3 executions in report
		Assert.assertTrue(mainReportContent.matches(".*<a href='testOkWithInvocationCount/TestReport\\.html'.*?>testOkWithInvocationCount</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testOkWithInvocationCount-1/TestReport\\.html'.*?>testOkWithInvocationCount-1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testOkWithInvocationCount-2/TestReport\\.html'.*?>testOkWithInvocationCount-2</a>.*"));

		String detailedReportContent = readTestMethodResultFile("testOkWithInvocationCount");
		Assert.assertTrue(detailedReportContent.contains("Start method testOkWithInvocationCount"));

		// check each step is seen only once
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<span class=\"step-title\"> Pre test step: setCount"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<span class=\"step-title\"> Pre test step: slow"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<span class=\"step-title\"> Pre test step: set "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<span class=\"step-title\"> step 1"), 1);

		String detailedReportContent1 = readTestMethodResultFile("testOkWithInvocationCount-1");
		Assert.assertTrue(detailedReportContent1.contains("Start method testOkWithInvocationCount-1"));
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "<span class=\"step-title\"> Pre test step: setCount"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "<span class=\"step-title\"> Pre test step: slow"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "<span class=\"step-title\"> Pre test step: set "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "<span class=\"step-title\"> step 1"), 1);

		String detailedReportContent2 = readTestMethodResultFile("testOkWithInvocationCount-2");
		Assert.assertTrue(detailedReportContent2.contains("Start method testOkWithInvocationCount-2"));
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "<span class=\"step-title\"> Pre test step: setCount"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "<span class=\"step-title\"> Pre test step: slow"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "<span class=\"step-title\"> Pre test step: set "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "<span class=\"step-title\"> step 1"), 1);
	}

	/**
	 * Check that test report do not display tabs when no snapshot comparison is requested
	 */
	@Test(groups = {"it"})
	public void testNoSnapshotComparison() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

		// no bullet as no snapshot comparison is done
		String summaryReport = readSummaryFile();
		Assert.assertFalse(summaryReport.contains("<i class=\"fas fa-circle "));

		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\"  style=\"display: none;\" >"));
		Assert.assertFalse(detailedReportContent.contains("</button> Snapshot comparison"));

	}

	/**
	 * issue #351: Check that when snapshot server is used, but a problem occurs posting information, snapshot tab is not displayed
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonErrorDuringTransfer() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 500, "Internal Server Error", "body");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is ok and comparison result is not shown
			String summaryReport = readSummaryFile();
			Assert.assertFalse(summaryReport.contains("<i class=\"fas fa-circle circle")); // no snapshot comparison has been performed
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");

			// no snapshot tab displayed
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\"  style=\"display: none;\" >"));

			// message saying that error occurred when contacting snapshot server
			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("request to http://localhost:4321 failed: Internal Server Error"));

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a green bullet should be visible on summary result when comparison is OK
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonOkDisplayOnly() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is ok and comparison result is shown through green bullet
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSuccess\" data-toggle=\"tooltip\" title=\"snapshot comparison successfull\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-success \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Test case where no snapshot has been sent to server, 'snapshot comparison' step should be green with message stating that no picture has been processed
	 */
	@Test(groups = {"it"})
	public void testSnapshotNoComparisonDisplayOnly() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': []}");


			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is ok and comparison result is shown through blue bullet (no comparison to do)
			String summaryReport = readSummaryFile();

			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));

			// snapshot tab active / skipped
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * Result remains OK as behaviour is "displayOnly"
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonKoDisplayOnly() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false, 'computingError': []}");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is ok and comparison result is shown through red bullet (comparison KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-failed \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * Result is KO as behavior is 'changeTestResult'
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonKoChangeTestResult() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is KO (due to option 'changeTestResult') and comparison result is shown through red bullet (comparison KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ko\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-failed \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonKoAddTestResult() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\"></i><a href='testAndSubActions/TestReport.html' info=\"ok\" "));
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\"></i><a href='snapshots-testAndSubActions/TestReport.html' info=\"ko\""));

			String detailedReportContent = readTestMethodResultFile("snapshots-testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-failed \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// check execution logs contains the exception (but not logs)
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\"><div>class com.seleniumtests.customexception.ScenarioException: Snapshot comparison failed</div>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	@Test(groups = {"it"})
	public void testSnapshotComparisonSkipDisplayOnly() throws Exception {

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error computing']}");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is ok and comparison result is shown through blue bullet (comparison skipped)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-skipped \" id=\"snapshot-tab\"")); // tab is in blue as comparison skipped

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * Result remains OK as behaviour is "displayOnly"
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonSkipChangeTestResult() throws Exception {

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error computing']}");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check result is OK (even with option 'changeTestResult' because comparison is skipped) and comparison result is shown through blue bullet (comparison Skipped)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-skipped \" id=\"snapshot-tab\"")); // tab is in blue as comparison skipped

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));

			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 */
	@Test(groups = {"it"})
	public void testSnapshotComparisonSkipAddTestResult() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error computing']}");

			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\"></i><a href='testAndSubActions/TestReport.html' info=\"ok\" "));
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\"></i><a href='snapshots-testAndSubActions/TestReport.html' info=\"skipped\""));

			String detailedReportContent = readTestMethodResultFile("snapshots-testAndSubActions");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));

			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));


			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Check summary format in monothread
	 */
	@Test(groups = {"it"})
	public void testMonothreadReport() throws Exception {
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testInError/TestReport\\.html'.*?>testInError</a>.*"));

		// issue #331: check that result files have been generated at least twice (one during test run and one at the end)
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
        Assert.assertEquals(StringUtils.countMatches(logs, "testInError/PERF-result.xml"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testInError/detailed-result.json"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testAndSubActions/PERF-result.xml"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testAndSubActions/detailed-result.json"), 1);
        Assert.assertEquals(StringUtils.countMatches(logs, "testWithException/PERF-result.xml"), 3); // once per retry
        Assert.assertEquals(StringUtils.countMatches(logs, "testWithException/detailed-result.json"), 3); // once per retry

		// issue  #312: check that result files have been generated before test end (meaning they are generated after the test execution
		Assert.assertTrue(Strings.CS.indexOf(logs, "testInError/PERF-result.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testInError/detailed-result.json") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testAndSubActions/PERF-result.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testAndSubActions/detailed-result.json") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testWithException/PERF-result.xml") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(Strings.CS.indexOf(logs, "testWithException/detailed-result.json") < Strings.CS.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));

		// issue #319: check that if no test info is recorded, columns are not there / Last State info is always there
		Assert.assertTrue(mainReportContent.contains("<td class=\"info\">"));
		Assert.assertTrue(mainReportContent.contains("<th> Last State </th>"));
	}

	/**
	 * Check "Last State" is always there even if nothing needs to be displayed (should never happen as we should have at least the last screen capture
	 */
	@Test(groups = {"it"})
	public void testMonothreadReportTestOk() throws Exception {
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		// issue #319: check that if no test info is recorded, columns are not there / Last State info is always there
		Assert.assertTrue(mainReportContent.contains("<td class=\"info\">"));
		Assert.assertTrue(mainReportContent.contains("<th> Last State </th>"));
	}

	@Test(groups = {"it"})
	public void testDownloadedFileInReport() throws Exception {
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDownloadFile"});

		// file present in report dir (not in 'downloads' subdir because we log it
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDownloadFile", "nom-du-fichier.pdf").toFile().exists());

		// issue #331: check that result files have been generated at least twice (one during test run and one at the end)
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
		Assert.assertTrue(logs.contains("try downloading file nom-du-fichier.pdf"));

		// check file is present in report
		String detailedReportContent = readTestMethodResultFile("testDownloadFile");
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-snapshot\">PDF example: <a href='nom-du-fichier.pdf'>file</a></div>"));
	}

	/**
	 * Check if param "Gridnode" is ok
	 */
	@Test(groups = {"it"})
	public void testGridnodeExist() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4321/wd/hub");

			createGridHubMockWithNodeOK();

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShort"});

			// check content of summary report file
			String mainReportContent = readTestMethodResultFile("testDriverShort");

			Assert.assertTrue(mainReportContent.contains("<th>Grid node</th>" +
					"<td>localhost</td>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	@Test(groups = {"it"})
	public void testGridnodeNotPresentForSkippedTest() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4321/wd/hub");

			createGridHubMockWithNodeOK();

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo", "testDriverShortSkipped"});

			// check content of summary report file
			String mainReportContent = readTestMethodResultFile("testDriverShortSkipped");

			// grid node information not present when test is skipped
			Assert.assertFalse(mainReportContent.contains("<th>Grid node</th>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	@Test(groups = {"it"})
	public void testGridnodeExistForLocal() throws Exception {
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShort"});

		// check content of summary report file
		String mainReportContent = readTestMethodResultFile("testDriverShort");

		Assert.assertTrue(mainReportContent.contains("<th>Grid node</th>" +
				"<td>LOCAL</td>"));
	}

	/**
	 * Check generic steps are logged
	 */
	@Test(groups = {"it"})
	public void testGenericSteps() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForGenericSteps"}, ParallelMode.METHODS, new String[]{"testDriver"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testDriver/TestReport\\.html' info=\"ok\".*?>testDriver</a>.*"));

		String detailedReportContent = readTestMethodResultFile("testDriver");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");


		// check generic steps are logged
		Assert.assertTrue(detailedReportContent.contains("</button><span class=\"step-title\"> sendKeysToField with args: (textElement, foo, )"));
		Assert.assertTrue(detailedReportContent.contains("</button><span class=\"step-title\"> _reset"));
	}

	/**
	 * Check single test report format when tests have steps
	 */
	@Test(groups = {"it"})
	public void testMultithreadTestReport() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.TESTS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testInError/TestReport\\.html'.*?>testInError</a>.*"));

		// check content for details results
		String detailedReportContent1 = readTestMethodResultFile("testAndSubActions");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Pre test step: setCount"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Pre test step: set "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "step 1 -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "step 2 -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Test end"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Post test step: reset"), 1);

		// check content for details results
		String detailedReportContent2 = readTestMethodResultFile("testInError");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Pre test step: setCount"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Pre test step: set "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "step 1 -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Test end"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Post test step: reset"), 1);

		// check content for details results
		String detailedReportContent3 = readTestMethodResultFile("testWithException");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "Pre test step: setCount"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "Pre test step: set "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "step 1 -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "Test end"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "Post test step: reset"), 1);
	}

	/**
	 * Check issue #143 where all \@AfterMethod calls are displayed in all test if its first parameter is not a method reference
	 */
	@Test(groups = {"it"})
	public void testTestReportContainsOnlyItsAfterMethodSteps() throws Exception {
		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForIssue143"});

		String detailedReportContent1 = readTestMethodResultFile("testOk1");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Post test step: reset2"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Post test step: reset "), 1);

		String detailedReportContent2 = readTestMethodResultFile("testOk2");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Post test step: reset2"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "Post test step: reset "), 1);

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("When using @BeforeMethod / @AfterMethod in tests")); // check error message is shown when parameter is not given to Before / AfterMethod
		Assert.assertTrue(logs.contains("com.seleniumtests.it.stubclasses.StubTestClassForIssue143.reset")); // check method name is displayed
	}

	/**
	 * Check issue #141 where \@AfterMethod calls are displayed as many times as test retries
	 */
	@Test(groups = {"it"})
	public void testRetriedTestReportContainsOnlyItsAfterMethod() throws Exception {
		System.setProperty("testRetryCount", "1");
		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForIssue141"});

		// check we only display the second call
		String detailedReportContent1 = readTestMethodResultFile("testOk1");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

		Assert.assertFalse(detailedReportContent1.matches("<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> after method 1 </div>"));
		Assert.assertTrue(detailedReportContent1.matches(".*<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> after method 2 </div>.*"));

	}

	/**
	 * issue #251: check error message is displayed for any action that failed
	 */
	@Test(groups = {"it"})
	public void testDetailedReportWithOneStepFailed() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[]{"testOkWithOneStepFailed"});

		// check content of summary report file
		String detailedReportContent = readTestMethodResultFile("testOkWithOneStepFailed");
		Assert.assertTrue(detailedReportContent.contains("<li class=\"header-failed\">failAction<br/>class com.seleniumtests.customexception.DriverExceptions: fail</li>"));

	}

	/**
	 * issue #251: check error message is displayed for any action that failed
	 */
	@Test(groups = {"it"})
	public void testDetailedReportWithOneSubStepFailed() throws Exception {
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[]{"testOkWithOneSubStepFailed"});

		// check content of summary report file
		String detailedReportContent = readTestMethodResultFile("testOkWithOneSubStepFailed");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// failed action is visible as failed
		Assert.assertTrue(detailedReportContent.contains("<li class=\"header-failed\">failAction<br/>class com.seleniumtests.customexception.DriverExceptions: fail</li>"));

		// parent action is OK, so it should not be marked as failed
		Assert.assertTrue(detailedReportContent.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-2\">\\d+:\\d+:\\d+.\\d+</span> addWithCatchedError with args: \\(1, \\) </div></li>.*"));

	}

	/**
	 * A snapshot is taken when soft assertion is enabled and assertion fails
	 */
	@Test(groups = {"it"})
	public void testDetailedReportContainsCaptureOnSoftAssertionEnabled() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverWithAssert"});

			String detailedReportContent = readTestMethodResultFile("testDriverWithAssert");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);

			// check that with assertion error, snapshot is present
			Assert.assertTrue(detailedReportContent.contains("!!!FAILURE ALERT!!! - Assertion Failure: expected [true] but found [false] </div><div class=\"row\"><div class=\"message-snapshot col\"><div class=\"text-center\"><a href=\"#\" onclick=\"$('#imagepreview').attr('src', $('#"));

		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}

	@Test(groups = {"it"})
	public void testDetailedReportDoesNotContainCaptureOnSoftAssertionDisabled() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverWithAssert"});

			String detailedReportContent = readTestMethodResultFile("testDriverWithAssert");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);

			// check that with assertion error, snapshot is present
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Assertion Failure: expected \\[true] but found \\[false] </div>.*"));
			Assert.assertFalse(detailedReportContent.contains("<div class=\"message-error\">!!!FAILURE ALERT!!! - Assertion Failure: expected [true] but found [false]</div>"
					+ "<div class=\"message-snapshot\">Output 'drv:main' browser: Current Window: Test page:"));

		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}

	/**
	 * In case an image cannot be found, check object file and scene file are present in report
	 */
	@Test(groups = {"it"})
	public void testDetailedReportContainsSearchedAndSceneImage() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverPictureElementNotFound"});

		String detailedReportContent = readTestMethodResultFile("testDriverPictureElementNotFound");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// check there is only one message for image not found
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "clickAt on Picture picture from resource tu/images/vosAlertes.png with args: (0, -30, )"), 1);
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-snapshot\">searched picture: <a href='img"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-snapshot\">scene to search in: <a href='"));
	}
	/**
	 * In case an image is found, check report displays the matching level
	 */
	@Test(groups = {"it"})
	public void testDetailedReportContainsMatchIndex() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverPictureElement"});

		String detailedReportContent = readTestMethodResultFile("testDriverPictureElement");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// check there is only one message for image not found
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )"), 1);
		Assert.assertTrue(detailedReportContent.contains("Object found with match value: "));
	}

	/**
	 * Check behaviour when Assert is used in test scenario (not in webpage)
	 * Assertion in scenario should be attached to the previous step which will be marked as failed
	 * Test end will also be in red
	 */
	@Test(groups = {"it"})
	public void testDetailedReportWithSoftAssertInScenario() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[]{"testWithAssertInTest"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);

			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertInTest");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// check step with assertion inside is failed
			Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\">"
					+ "<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> assertAction"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> !!!FAILURE ALERT!!! - Assertion Failure: false error expected \\[true] but found \\[false] </div>.*"));

			// that assertion raised in test scenario is attached to previous step
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\">"
					+ "<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> getResult - \\d+.\\d+ secs</span></div><div class=\"box-body\">"
					+ "<ul><div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> !!!FAILURE ALERT!!! - Assertion Failure: Error in result expected \\[1] but found \\[2] </div>.*"));

			// check last step shows the assertion
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> Test end - \\d+.\\d+ secs</span></div><div class=\"box-body\"><ul><div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is KO with error: class java.lang.AssertionError: !!! Many Test Failures \\(2\\)<br/>.*?"
					+ "<br/><br/>class java.lang.AssertionError: <br/>\\.<br/>Failure 1 of 2.*Failure 2 of 2.*"));

			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is KO with error: class java.lang.AssertionError: !!! Many Test Failures \\(2\\)<br/>.*"));

			// check last step before test end is OK because no error occurs in it
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box success\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> add with args: \\(3, \\).*"));

		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}

	/**
	 * Check behaviour when Assert is used in test scenario (not in webpage)
	 * Assertion in scenario should be attached, when there is no step after (final checks) should be displayed in a specific step
	 * Test end will also be in red
	 */
	@Test(groups = {"it"})
	public void testDetailedReportWithSoftAssertAtScenarioEnd() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[]{"testWithAssertOnTestEnd"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);

			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertOnTestEnd");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// check last step shows the assertion
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> Test end - \\d+.\\d+ secs</span></div><div class=\"box-body\"><ul><div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is KO with error: class java.lang.AssertionError: Error in result expected \\[1] but found \\[2] </div>.*"));


			// that assertion raised in test scenario is attached to previous step
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\">"
					+ "<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> getResult - \\d+.\\d+ secs</span></div><div class=\"box-body\">"
					+ "<ul><div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> !!!FAILURE ALERT!!! - Assertion Failure: Error in result expected \\[1] but found \\[2] </div>.*"));


		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}

	/**
	 * Check that when an assert is raised in sub step, the root step is marked as failed
	 */
	@Test(groups = {"it"})
	public void testDetailedReportWithSoftAssertInSubStep() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[]{"testWithAssertInSubStep"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);

			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertInSubStep");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");


			// check that sub step failure (with assertion) caused the step to fail itself
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i>" // => step failed
					+ "</button><span class=\"step-title\"> assertWithSubStep - \\d+.\\d+ secs</span></div><div class=\"box-body\"><ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-2\">\\d+:\\d+:\\d+.\\d+</span> doNothing </div></li>"
					+ "<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> doNothing on HtmlElement none, by=\\{By.id: none} </div></li><div class=\"row\"></div></ul>"
					+ "<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-2\">\\d+:\\d+:\\d+.\\d+</span> assertAction </div></li><ul>" // => sub step with error
					+ "<div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> !!!FAILURE ALERT!!! - Assertion Failure: false error expected \\[true] but found \\[false].*")); // error displayed

		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}

	/**
	 * Check behaviur when hard Assert is used in test scenario (not in webpage)
	 * Test stops on first assertion and step is failed
	 */
	@Test(groups = {"it"})
	public void testDetailedReportWithHardAssertInScenario() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[]{"testWithAssertInTest"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);

			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertInTest");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");


			// check step with assertion inside is failed
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> getResult - \\d+.\\d+ secs</span></div><div class=\"box-body\">.*?"
					+ "<div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Assertion Failure: Error in result expected \\[1] but found \\[2].*"));

			// Test end step also displays the error
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> Test end - \\d+.\\d+ secs</span></div><div class=\"box-body\"><ul>.*?"
					+ "<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> \\[NOT RETRYING] due to failed Assertion </div>.*?"
					+ "<div class=\"message-error\"> class java.lang.AssertionError: Error in result expected \\[1] but found \\[2].*"
			));

			// test is stopped after assertion raised in test. AssertAction which would be executed later is never reached
			Assert.assertFalse(detailedReportContent.contains("</button><span class=\"step-title\"> assertAction"));

		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}

	/**
	 * Check summary format when tests have steps
	 */
	@Test(groups = {"it"})
	public void testReportSummaryContentWithSteps() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException", "testOk"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testInError/TestReport\\.html'.*?>testInError</a>.*"));

		// check number of steps is correctly computed. "test1" has 2 main steps and no failed step, "testInError" has 1 step
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-1\">7</td>"));

		// for second test, test is reported KO whereas all steps are OK because we do not use LogAction.aj
		// which handles assertion errors and report them in test steps
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-2\">6<sup><a href=\"#\" data-toggle=\"tooltip\" class=\"failedStepsTooltip\" title=\"1 step(s) failed\">*</a></sup></td>"));

		// 'testOk' has no failed steps, no additional information is present
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-3\">6</td>"));


		// check full log file is there
		Assert.assertTrue(mainReportContent.contains("<a href=\"seleniumRobot.log\""));
	}

	/**
	 * issue #148: Check that when test is retried and retry is OK, summary is correct
	 */
	@Test(groups = {"it"})
	public void testFailsOnlyOnceAndRetriedOk() throws Exception {

		StubTestClass.failed = false;

		// execute only the test that fails the first time it's executed
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testWithExceptionOnFirstExec"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		// only the last execution (ok) is shown
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, ">testWithExceptionOnFirstExec</a>"), 1);
		Assert.assertFalse(mainReportContent.contains("info=\"skipped\" data-toggle=\"tooltip\""));

		// check log contain the 2 executions
		String detailedReportContent1 = readTestMethodResultFile("testWithExceptionOnFirstExec");
		Assert.assertTrue(detailedReportContent1.contains("Test is KO with error: class com.seleniumtests.customexception.DriverExceptions: some exception"));
		Assert.assertTrue(detailedReportContent1.contains("Test is OK"));
	}

	/**
	 * Check resources referenced in header are get from CDN and resources files are not copied to ouput folder
	 */
	@Test(groups = {"it"})
	public void testReportWithResourcesFromCDN() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.OPTIMIZE_REPORTS, "true");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});
		} finally {
			System.clearProperty(SeleniumTestsContext.OPTIMIZE_REPORTS);
		}

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.contains("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap"));

		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "AdminLTE.min.css").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "bootstrap.min.css").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "webfonts").toFile().exists());

		String detailedReportContent = readTestMethodResultFile("testAndSubActions");

		Assert.assertTrue(detailedReportContent.contains("<script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js\""));
		Assert.assertTrue(detailedReportContent.contains("<script src=\"https://cdn.jsdelivr.net/npm/iframe-resizer@4.2.10/js/iframeResizer.min.js\">"));
		Assert.assertTrue(detailedReportContent.contains("<link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.15.3/css/all.css\">"));
		Assert.assertTrue(detailedReportContent.contains("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css\""));
	}

	/**
	 * Check resources referenced in header are get from local and resources files are  copied to ouput folder
	 */
	@Test(groups = {"it"})
	public void testReportWithResourcesFromLocal() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.OPTIMIZE_REPORTS, "false");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});
		} finally {
			System.clearProperty(SeleniumTestsContext.OPTIMIZE_REPORTS);
		}

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.contains("<script src=\"resources/templates/bootstrap.bundle.min.js\"></script>"));

		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "AdminLTE.min.css").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "bootstrap.min.css").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources", "templates", "webfonts").toFile().exists());

		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent.contains("<script src=\"resources/iframeResizer.min.js\"></script>"));
	}

	@Test(groups = {"it"})
	public void testKeepAllResults() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testWithException2"});


			// issue #346: check all reports are generated
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			Assert.assertTrue(detailedReportContent.contains("<h4> Test Details"));

			String detailedReportContent2 = readTestMethodResultFile("testWithException2");

			// check the message and that previous execution result is visible
			Assert.assertTrue(detailedReportContent2.contains("Previous execution results"));
			Assert.assertTrue(detailedReportContent2.contains("<h4> Test Details"));
			Assert.assertTrue(detailedReportContent2.contains("<a href='retry-testWithException2-1.zip'>file</a>"));
			Assert.assertTrue(detailedReportContent2.indexOf("<a href='retry-testWithException2-1.zip'>file</a>") > detailedReportContent2.indexOf("<span class=\"step-title\"> Post test step: reset"));

		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}

	@Test(groups = {"it"})
	public void testKeepAllResultsNoParallel() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[]{"testAndSubActions", "testWithException2"});

			String detailedReportContent = readTestMethodResultFile("testWithException2");

			// check the message and that no previous execution result is visible
			Assert.assertTrue(detailedReportContent.contains("Previous execution results"));
			Assert.assertTrue(detailedReportContent.contains("<a href='retry-testWithException2-1.zip'>file</a>"));

			// issue #379: we souhld have Previous result box
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<div class=\"box collapsed-box"), 8);


		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}

	/**
	 * Previous execution results cannot be generated without retry / or test OK
	 * Check the line in report is not present
	 */
	@Test(groups = {"it"})
	public void testKeepAllResultsWithoutRetry() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions"});

			String detailedReportContent = readTestMethodResultFile("testAndSubActions");

			// check the message and that previous execution result is visible
			Assert.assertTrue(detailedReportContent.contains("Previous execution results"));
			Assert.assertFalse(detailedReportContent.contains("<a href='retry-testAndSubActions-1.zip'>file</a>"));

		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}

	/**
	 * Check that logs of a failed attempt are not kept in the result directory (KEEP_ALL_RESULTS=false)
	 */
	@Test(groups = {"it"})
	public void testDoNotKeepAllResults() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "false");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testWithException"});

			String detailedReportContent = readTestMethodResultFile("testWithException");

			// check the message and that no previous execution result is visible
			Assert.assertTrue(detailedReportContent.contains("No previous execution results, you can enable it via parameter '-DkeepAllResults=true'"));
			Assert.assertFalse(detailedReportContent.contains("<a href='retry-testWithException-1.zip'>file</a>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}

	/**
	 * Check if test description made available by TestNG annotation is displayed in summary and detailed report
	 */
	@Test(groups = {"it"})
	public void testTestDescription() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		// if description is available, it's displayed
		// else, "no description available" is shown
		Assert.assertTrue(mainReportContent.contains("data-toggle=\"tooltip\" title=\"a test with steps\">testAndSubActions</a>"));
		Assert.assertTrue(mainReportContent.contains("data-toggle=\"tooltip\" title=\"no description available\">testInError</a>"));
		Assert.assertTrue(mainReportContent.contains("data-toggle=\"tooltip\" title=\"no description available\">testWithException</a>"));

		// Check description is displayed if available
		String detailedReportContent1 = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent1.contains("<h4> Test Details - testAndSubActions</h4>"));
		Assert.assertTrue(detailedReportContent1.contains("<th width=\"200px\">Description</th><td>a test with steps</td>"));

		String detailedReportContent2 = readTestMethodResultFile("testInError");
		Assert.assertFalse(detailedReportContent2.contains("<h4> Test Details - testInError</h4><pre>"));

	}


	/**
	 * Check that with driver starting and operations in BeforeMethod method, screenshots are available in HTML report
	 */
	@Test(groups = {"it"})
	public void testAllScreenshotsArePresent() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty("startLocation", "beforeMethod");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}

		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");

		// check all files are displayed
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/beforeMethod_2-1_Pre_test_step._beforeMethod"));
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/beforeMethod_2-1_Pre_test_step._beforeMethod-"));
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/test1Listener5_3-1_Test_end"));
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/test1Listener5_3-1_Test_end"));


	}

	/**
	 * Check that automatic steps create all steps in report
	 */
	@Test(groups = {"it"})
	public void testAutomaticSteps() throws Exception {

		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClass3"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testFailedWithException/TestReport\\.html'.*?>testFailedWithException</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testFailedWithSoftAssertDisabled/TestReport\\.html'.*?>testFailedWithSoftAssertDisabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testFailedWithSoftAssertEnabled/TestReport\\.html'.*?>testFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testMultipleFailedWithSoftAssertEnabled/TestReport\\.html'.*?>testMultipleFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testOk/TestReport\\.html'.*?>testOk</a>.*"));


		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent2 = readTestMethodResultFile("testFailedWithSoftAssertDisabled");
		detailedReportContent2 = detailedReportContent2.replaceAll("\\s+", " ");


		Assert.assertTrue(detailedReportContent2.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button><span class=\"step-title\"> assertAction"));
		Assert.assertFalse(detailedReportContent2.contains("</button><span class=\"step-title\"> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button><span class=\"step-title\"> Test end"));

		// check that with soft assertion, all steps are displayed
		String detailedReportContent3 = readTestMethodResultFile("testFailedWithSoftAssertEnabled");
		detailedReportContent3 = detailedReportContent3.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> assertAction"));
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> Test end"));

		// check that with error, remaining steps are skipped
		String detailedReportContent1 = readTestMethodResultFile("testFailedWithException");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> failAction"));
		Assert.assertFalse(detailedReportContent1.contains("</button><span class=\"step-title\"> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> Test end"));

		// check "openPage" prints the page name
		String detailedReportContent4 = readTestMethodResultFile("testOk");
		detailedReportContent4 = detailedReportContent4.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent4.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent4.matches(".*<div class=\"box-body\"><ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Opening page CalcPage </div></li>.*"));
		Assert.assertFalse(detailedReportContent4.contains("</button><span class=\"step-title\"> failAction"));
		Assert.assertTrue(detailedReportContent4.contains("</button><span class=\"step-title\"> add with args: (1, 1, )"));
		Assert.assertTrue(detailedReportContent4.contains("</button><span class=\"step-title\"> Test end"));

	}

	@Test(groups = {"it"})
	public void testAttachmentRenaming() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		String detailedReportContent1 = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/testAndSubActions_0-1_step_1--rtened.png\""));
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/testAndSubActions_0-1_step_1--tened.html' target=html>"));

		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_0-1_step_1--tened.html").toFile().exists());
	}

	/**
	 * Check that manual steps create all steps in report
	 * manual step option is set inside the StubTestClassManualSteps.testOk() method
	 * check the failed test case where step should be marked as KO
	 * Also, error in step should be presented
	 */
	@Test(groups = {"it"})
	public void testManualSteps() throws Exception {

		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClassManualSteps"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testOk/TestReport\\.html'.*?>testOk</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testWithAssert/TestReport\\.html'.*?>testWithAssert</a>.*"));

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent1 = readTestMethodResultFile("testOk");
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> Test start"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> add some values"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> minus 2"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> do nothing"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> Test end"));

		// check that configuration steps are automatically added
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> Pre test step: set - "));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> Post test step: teardown -"));

		// assert automatic steps are not present
		Assert.assertFalse(detailedReportContent1.contains("</button><span class=\"step-title\"> add with args"));

		// check we also get actions
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "doNothing on HtmlElement none"), 3);

		// ----- check manual steps errors ------
		String detailedReportContent2 = readTestMethodResultFile("testWithAssert");
		detailedReportContent2 = detailedReportContent2.replaceAll("\\s+", " ");

		// check execution logs are in error
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button> Execution logs"));

		// test first step is OK and second one is failed (this shows indirectly that internal step is marked as failed
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test start"));
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> assert exception"));

		// check exception is present in step
		Assert.assertTrue(detailedReportContent2.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is KO with error: class java.lang.AssertionError: false error expected \\[true] but found \\[false] </div>.*"));

	}

	/**
	 * Check that manual also mask password if user requests it (gives password to mask in report)
	 */
	@Test(groups = {"it"})
	public void testManualStepsPasswordMasking() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassManualSteps"}, ParallelMode.METHODS, new String[]{"testOkPassword"});

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent1 = readTestMethodResultFile("testOkPassword");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");
		// if step specifies string to mask, hide it
		Assert.assertFalse(detailedReportContent1.contains("<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">17:14:43.505</span> password is aPassPhrase </div>"));
		Assert.assertTrue(detailedReportContent1.matches(".*<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> password is \\*\\*\\*\\*\\*\\* </div>.*"));

		// if step does not specifies string to mask, it's displayed
		Assert.assertTrue(detailedReportContent1.matches(".*<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> password is anOtherPassPhrase </div>.*"));
	}

	/**
	 * Check state and style of all tests
	 */
	@Test(groups = {"it"})
	public void testReportSummaryContentWithDependantTests() throws Exception {

		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClass2"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='test1/TestReport\\.html' info=\"ok\".*?>test1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='test4/TestReport\\.html' info=\"ko\".*?>test4</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='test3/TestReport\\.html' info=\"skipped\".*?>test3</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='test5/TestReport\\.html' info=\"ko\".*?>test5</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='test2/TestReport\\.html' info=\"skipped\".*?>test2</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='test6/TestReport\\.html' info=\"ok\".*?>test6</a>.*"));
		Assert.assertFalse(mainReportContent.contains("$testResult.getAttribute(\"methodName\")")); // check all test methods are filled
	}

	/**
	 * Check format of messages in detailed report
	 */
	@Test(groups = {"it"})
	public void testReportDetailsMessageStyles() throws Exception {
		try {
			System.setProperty("customTestReports", "PERF::xml::reporter/templates/report.perf.vm,SUP::xml::reporter/templates/report.supervision.vm");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

			// check style of messages
			String detailedReportContent = readTestMethodResultFile("testInError");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click ok </div>.*"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-warning message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Warning: Some warning message </div>.*"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-info message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Some Info message </div>.*"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Some Error message </div>.*"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Some log message </div>.*"));
			Assert.assertTrue(detailedReportContent.matches(".*<table class=\"table table-bordered table-sm\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> key </div></td><td>we found a value of</td><td>10</td></tr></table>.*"));
			Assert.assertTrue(detailedReportContent.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> send keyboard action </div></li>.*"));
		} finally {
			System.clearProperty("customTestReports");
		}
	}

	/**
	 * Check format of steps inside steps
	 * test1 in com.seleniumtests.it.stubclasses.StubTestClass defines steps inside other steps
	 */
	@Test(groups = {"it"})
	public void testReportDetailsWithSubSteps() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		// check content of summary report file
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");
		try {
			Assert.assertTrue(detailedReportContent.matches(
					".*<ul>.*?"                                                    // root step
							+ "click button.*?"
							+ "sendKeys to text field.*?"
							+ "step 1.3: open page.*?"// sub-step
							+ "<ul>.*?"
							+ "click link.*?"                            // action in sub step
							+ "<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> a message </div>.*?"    // message in sub step
							+ "sendKeys to password field.*?"            // action in sub step
							+ "<div class=\"row\"></div></ul><div class=\"row\">.*?"
							+ "<div class=\"message-snapshot col\"><div class=\"text-center\">.*src=\"screenshots/testAndSubActions_0-1_step_1--rtened\\.png\" style=\"width: 300px\">.*"
			));
		} catch (AssertionError e) {
			logger.error("------ detailed report --------");
			logger.error(detailedReportContent);
			throw e;
		}

	}

	/**
	 * Check logs are written in file
	 */
	@Test(groups = {"it"})
	public void testReportDetailsWithLogs() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		// check content of detailed report file
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");

		// check log presence
		Assert.assertTrue(detailedReportContent.contains("[main] SeleniumRobotTestListener: Start method testAndSubActions</div>"));

	}

	/**
	 * Check all steps are present in detailed report file
	 * Test OK
	 */
	@Test(groups = {"it"})
	public void testReportDetailsSteps() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Check each step is recorded in file: 2 test steps + test end + logs

		// step in warning because some actions are failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box warning\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> step 1 - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> step 2 - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button> Execution logs"));
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is OK </div>.*"));

		// check failed steps are in red
		String detailedReportContent2 = readTestMethodResultFile("testInError");
		detailedReportContent2 = detailedReportContent2.replaceAll("\\s+", " ");

		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end"));

		// check logs are written only once
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "[main] ScenarioLogger: Test is OK</div>"), 1);

	}

	/**
	 * Test the case where "testName" is specified on Test annotation
	 */
	@Test(groups = {"it"})
	public void testReportWithCustomTestName() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testOkWithTestName", "testOkWithTestNameAndDataProvider"});


		// check visual name is used in summary (string interpolation with dataprovider, and raw test name)
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.contains("<a href='testOkWithTestName/TestReport.html' info=\"ok\" data-toggle=\"tooltip\" title=\"no description available\">A test which is &lt;OK&gt; &eacute;&amp;</a>"));
		Assert.assertTrue(mainReportContent.contains("<a href='testOkWithTestNameAndDataProvider/TestReport.html' info=\"ok\" data-toggle=\"tooltip\" title=\"no description available\">A test which is OK (data2, data3)</a>"));

		// detailed reports should also display the custom test name
		String detailedReportContent = readTestMethodResultFile("testOkWithTestName");
		Assert.assertTrue(detailedReportContent.contains("<h4> Test Details - A test which is &lt;OK&gt; &eacute;&amp;</h4>"));

		String detailedReportContent2 = readTestMethodResultFile("testOkWithTestNameAndDataProvider");
		Assert.assertTrue(detailedReportContent2.contains("<h4> Test Details - A test which is OK (data2, data3) with params: (data2,data3)</h4>"));
		String detailedReportContent3 = readTestMethodResultFile("testOkWithTestNameAndDataProvider-1");
		Assert.assertTrue(detailedReportContent3.contains("<h4> Test Details - A test which is OK (data4, data5) with params: (data4,data5)</h4>"));
	}

	/**
	 * Test parameter masking for test method and test step
	 * Only test method parameter is set as "to mask", but check all steps using the parameter will mask it
	 */
	@Test(groups = {"it"})
	public void testReportParametersFromDataProvider() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testOkWithPasswordDataProvider"});

		// Check password is masked
		String detailedReportContent = readTestMethodResultFile("testOkWithPasswordDataProvider");
		Assert.assertTrue(detailedReportContent.contains("<h4> Test Details - testOkWithPasswordDataProvider with params: (12,******)</h4>"));
		Assert.assertTrue(detailedReportContent.contains("class=\"step-title\"> add with args: (1, ******, )"));
		String detailedReportContent2 = readTestMethodResultFile("testOkWithPasswordDataProvider-1");
		Assert.assertTrue(detailedReportContent2.contains("<h4> Test Details - testOkWithPasswordDataProvider-1 with params: (13,12345)</h4>"));
		Assert.assertTrue(detailedReportContent2.contains("class=\"step-title\"> add with args: (1, 12345, )"));

		// test with "null" parameter
		String detailedReportContent3 = readTestMethodResultFile("testOkWithPasswordDataProvider-2");
		Assert.assertTrue(detailedReportContent3.contains("<h4> Test Details - testOkWithPasswordDataProvider-2 with params: (14,null)</h4>"));
	}

	@Test(groups = {"it"})
	public void testReportContainsCustomScreenshot() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverCustomSnapshot"});

		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriverCustomSnapshot");

		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/my_snapshot"));
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/my_snapshot"));
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"text-center\">drv:main-my snapshot: my snapshot</div>"));
		Assert.assertTrue(detailedReportContent1.contains(" src=\"screenshots/my_snapshot-"));
	}

	/**
	 * Check test information shows a link to last test step
	 */
	@Test(groups = {"it"})
	public void testReportContainsTestEndScreenshotQuicklink() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverCustomSnapshot"});

		// read 'testDriverCustomSnapshot' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriverCustomSnapshot");

		Assert.assertTrue(detailedReportContent1.matches(".*<th>Last State</th><td><a href=\"screenshots/testDriverCustomSnapshot_7-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a></td>.*"));

		// check shortcut to last state is displayed
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<td class=\"info\"><a href=\"testDriverCustomSnapshot/screenshots/testDriverCustomSnapshot_7-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a></td>.*"));

	}

	/**
	 * Check that video capture file is not present in result if not requested
	 */
	@Test(groups = {"it"})
	public void testReportContainsNoVideoCapture() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "false");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriver"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriver");

			Assert.assertFalse(detailedReportContent1.contains(String.format("Video capture: <a href='%s'>file</a>", videoFileName)));

			// check shortcut to video is NOT present in detailed report
			Assert.assertFalse(detailedReportContent1.matches(String.format(".*<th>Last State</th><td><a href=\"screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"%s\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*", videoFileName)));

			// check shortcut to video is NOT present in summary report
			String mainReportContent = readSummaryFile();
			Assert.assertFalse(mainReportContent.matches(String.format(".*<td class=\"info\"><a href=\"testDriver/screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"testDriver/%s\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*", videoFileName)));

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}

	}

	/**
	 * Check that video capture file is present in result if requested
	 */
	@Test(groups = {"it"})
	public void testReportContainsVideoCapture() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriver"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriver");

			Assert.assertTrue(detailedReportContent1.contains(String.format("Video capture: <a href='%s'>file</a>", videoFileName)));

			// check video has chapters (expect ffmpeg is installed)
			File videoFile = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "videoCapture.mp4").toFile();
			Assert.assertTrue(videoFile.exists());
			String ffmpegOut = new FFMpeg().runFFmpegCommand(List.of("-i", videoFile.getAbsolutePath()));
			Assert.assertTrue(ffmpegOut.contains("Chapter #0:0:"));
			Assert.assertTrue(ffmpegOut.contains("Chapter #0:9:"));
			Assert.assertTrue(ffmpegOut.contains("title           : _sendKeysComposite"));

			// check shortcut to video is present in detailed report
			Assert.assertTrue(detailedReportContent1.matches(String.format(".*<th>Last State</th><td><a href=\"screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"%s\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*", videoFileName)));

			// check shortcut to video is present in summary report
			String mainReportContent = readSummaryFile();
			Assert.assertTrue(mainReportContent.matches(String.format(".*<td class=\"info\"><a href=\"testDriver/screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"testDriver/%s\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*", videoFileName)));

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}

	}


	/**
	 * Check reference image for step is displayed when "recordResult" option is set and video capture is enabled
	 * This reference is only shown when test fails
	 */
	@Test(groups = {"it"})
	public void testReportContainsStepReferenceForFailedStep() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();

			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));


			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");

			// reference and current state should be displayed
			Assert.assertTrue(detailedReportContent1.matches(".* class=\"step-title\"> _writeSomethingOnNonExistentElement - .*"
					+ "at com\\.seleniumtests\\.it\\.driver\\.support\\.pages\\.DriverTestPage\\._writeSomethingOnNonExistentElement\\(DriverTestPage\\.java.*?"
					+ "<div class=\"row\">"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">"
					+ "<a href=\"#\" onclick=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class=\"text-center\">drv:main-Step start state 4: Step start state 4</div>" // the current state
					+ "<div class=\"text-center font-weight-lighter\"><a href=.*target=url>URL</a> \\| <a href='htmls/Step_start_state_4.*?</div>.*"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Valid-reference</div>.*")); // the reference

			// 1 step reference for each step
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step start state"), 4);

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * When results are not recorded on selenium server, reference images are not recorded
	 */
	@Test(groups = {"it"})
	public void testReferenceImageNotRecorded() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "false");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			configureMockedSnapshotServerConnection();

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");

			// reference is not displayed
			Assert.assertFalse(detailedReportContent1.contains("Step beginning state"));

			// no pictures has been created from video
			Assert.assertNull(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "video").toFile().listFiles());

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check step state is not displayed when reference image does not exist on server
	 */
	@Test(groups = {"it"})
	public void testReportDoesNotContainStepImageWithoutReference() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();

			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 404, createImageFromResource("tu/ffLogo1.png"));

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");

			// reference and current state should be displayed
			Assert.assertFalse(detailedReportContent1.matches(".* class=\"step-title\"> _writeSomethingOnNonExistentElement {2}- .*"
					+ "at com\\.seleniumtests\\.it\\.driver\\.support\\.pages\\.DriverTestPage\\._writeSomethingOnNonExistentElement\\(DriverTestPage\\.java.*?"
					+ "<div class=\"row\">"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">"
					+ "<a href=\"#\" onclick=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Valid-reference</div>.*")); // the reference

			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);


		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}

	}

	/**
	 * Check reference is not get if server recording is disabled
	 */
	@Test(groups = {"it"})
	public void testReportDoesNotContainStepReferenceWhenRecordingDisabled() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();

			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));


			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");

			// reference and current state should be displayed
			Assert.assertFalse(detailedReportContent1.matches(".* class=\"step-title\"> _writeSomethingOnNonExistentElement {2}- .*"
					+ "at com\\.seleniumtests\\.it\\.driver\\.support\\.pages\\.DriverTestPage\\._writeSomethingOnNonExistentElement\\(DriverTestPage\\.java.*?"
					+ "<div class=\"row\">"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">"
					+ "<a href=\"#\" onclick=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Valid-reference</div>.*")); // the reference

			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);


		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}

	}

	/**
	 * In case test is OK, no reference image is displayed in report
	 */
	@Test(groups = {"it"})
	public void testReportDoesNotContainReferenceStep() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");

			configureMockedSnapshotServerConnection();

			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShort"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShort");

			// reference and current state should be displayed
			Assert.assertFalse(detailedReportContent1.matches(".*<div class=\"row\">"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">"
					+ "<a href=\"#\" onclick=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class=\"message-snapshot col\"><div class=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class=\"text-center\">Valid-reference</div>.*")); // the reference

			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);


		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}

	}

	/**
	 * issue #406
	 */
	@Test(groups = {"it"})
	public void testReportContainsVideoCaptureOnRetry() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");

			Assert.assertTrue(detailedReportContent1.contains(String.format("Video capture: <a href='%s'>file</a>", videoFileName)));

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}

	}

	/**
	 * Check that when driver starts in beforeMethod, it's possible to have video
	 * issue #406
	 */
	@Test(groups = {"it"})
	public void testReportContainsVideoCaptureStartedOnBeforeMethodOnRetry() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForVideoTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKo"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");

			Assert.assertTrue(detailedReportContent1.contains(String.format("Video capture: <a href='%s'>file</a>", videoFileName)));

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}

	}

	/**
	 * Check that only one video capture file is present in result even if several drivers are used
	 */
	@Test(groups = {"it"})
	public void testReportContainsOneVideoCaptureWithMultipleDrivers() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testMultipleDriver"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testMultipleDriver");

			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, String.format("Video capture: <a href='%s'>file</a>", videoFileName)), 1);

		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}

	}

	/**
	 * Check that HAR capture file is present in result
	 */
	@Test(groups = {"it"})
	public void testReportContainsHarCapture() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriver"});

		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriver");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent1.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on TextFieldElement Text, by=\\{By.id: text2} with args: \\(true, true, \\[a text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent1.contains("Network capture 'main' browser: <a href='main-networkCapture.har'>HAR file</a>"));
		// check HAR capture is in the "Test end" step
		Assert.assertTrue(detailedReportContent1.indexOf("Network capture 'main'") > detailedReportContent1.indexOf("\"step-title\"> Test end"));
		Assert.assertTrue(detailedReportContent1.indexOf("Network capture 'main'") < detailedReportContent1.indexOf("\"step-title\"> Post test step"));
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "main-networkCapture.har").toFile().exists());
	}

	/**
	 * Check that HAR capture file is present in result when using multiple browsers, one for each browser
	 */
	@Test(groups = {"it"})
	public void testReportContainsHarCaptureMultipleBrowsers() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testMultipleDriver"});

		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testMultipleDriver");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent1.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on TextFieldElement Text, by=\\{By.id: text2} with args: \\(true, true, \\[a text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent1.contains("Network capture 'main' browser: <a href='main-networkCapture.har'>HAR file</a>"));
		Assert.assertTrue(detailedReportContent1.contains("Network capture 'second' browser: <a href='second-networkCapture.har'>HAR file</a>"));
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMultipleDriver", "main-networkCapture.har").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMultipleDriver", "second-networkCapture.har").toFile().exists());
	}

	@Test(groups = {"it"})
	public void testReportContainsWcagResults() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverWithWcag"});

		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriverWithWcag");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent1.matches(".*<div class=\"message-warning message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Warning: \\d+ violations found, see attached file </div>.*"));
		Assert.assertTrue(detailedReportContent1.contains("WCAG report: <a href='wcag/file"));
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithWcag", "wcag").toFile().listFiles().length, 1);

	}

	/**
	 * Check all actions done with driver are correctly displayed. This indirectly test the LogAction aspect
	 * We check
	 * - all HtmlElement action logging
	 * - all composite actions logging
	 * - all PictureElement action logging
	 */
	@Test(groups = {"it"})
	public void testReportContainsDriverActions() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriver"});

		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriver");
		detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent1.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on TextFieldElement Text, by=\\{By.id: text2} with args: \\(true, true, \\[a text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent1.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click on ButtonElement Reset, by=\\{By.id: button2} </div></li>.*"));
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"text-center\">drv:main: Current Window: Test page</div>"));
		Assert.assertTrue(detailedReportContent1.matches(".*<img id=\".*?\" src=\"screenshots/Step_start_state_3.*<div class=\"text-center\">drv:main-Step start state 3: Step start state 3</div>.*"));

		// check that only on reference to 'click' is present for this buttonelement. This means that only the replayed action has been logged, not the ButtonElement.click() one
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "click on"), 1);

		// check composite actions. We must have the moveToElement, click and sendKeys actions
		Assert.assertTrue(detailedReportContent1.matches(".*<span class=\"step-title\"> _sendKeysComposite - \\d+.\\d+ secs</span></div>" +
				"<div class=\"box-body\"><ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-2\">\\d+:\\d+:\\d+.\\d+</span> Composite moveToElement,sendKeys,on element 'TextFieldElement Text, by=\\{By.id: text2}' </div></li>" +
				"<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> moveToElement with args: \\(TextFieldElement Text, by=\\{By.id: text2}, \\) </div></li>" +
				"<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys with args: \\(\\[composite,], \\) </div></li><div class=\"row\"></div></ul>" +
				"<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-2\">\\d+:\\d+:\\d+.\\d+</span> Composite moveToElement,click,on element 'ButtonElement Reset, by=\\{By.id: button2}' </div></li>" +
				"<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> moveToElement with args: \\(ButtonElement Reset, by=\\{By.id: button2}, \\) </div></li>" +
				"<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click </div></li>.*"));


		// check PictureElement action is logge
		Assert.assertTrue(detailedReportContent1.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> clickAt on Picture picture from resource tu/images/logo_text_field.png with args: \\(0, -30, \\) </div></li>.*"));

		// check that when logging PictureElement action which uses composite actions, those are not logged
		Assert.assertFalse(detailedReportContent1.contains("<ul><li>clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li><li>moveToElement with args:"));

	}

	/**
	 * Check all native actions done with driver are correctly displayed. This indirectly test the LogAction aspect
	 * Done without overriding native actions
	 * We check
	 * - all HtmlElement action logging
	 * - all composite actions logging
	 * - all PictureElement action logging
	 */
	@Test(groups = {"it"})
	public void testReportContainsDriverNativeActionsNoOverride() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverNativeActionsWithoutOverride", "testDriverWithHtmlElementWithoutOverride"});

		// read the 'testDriverNativeActionsWithoutOverride' test result to see if native actions are not logged (overrideSeleniumNativeAction is false)
		String detailedReportContent = readTestMethodResultFile("testDriverNativeActionsWithoutOverride");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// logging is not done via HtmlElement
		Assert.assertFalse(detailedReportContent.contains("<li>sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [some text,], )</li>"));
		Assert.assertFalse(detailedReportContent.contains("<li>click on HtmlElement , by={By.id: button2} </li>"));

		// check that without override, native actions are logged
		Assert.assertTrue(detailedReportContent.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on Element located by id: text2 with args: \\(\\[some text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click on Element located by id: button2 </div></li>.*"));
		Assert.assertTrue(detailedReportContent.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> selectByVisibleText on Element located by Select id: select with args: \\(option1, \\) </div></li>.*"));

		// no action is logged when step fails (findElement exception). Ok because logging is done on action, not search

		// check that seleniumRobot actions are logged only once when overrideNativeAction is enabled (issue #88)
		String detailedReportContent4 = readTestMethodResultFile("testDriverWithHtmlElementWithoutOverride");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent4, "click on ButtonElement Reset, by={By.id: button2}"), 1);
	}

	/**
	 * Check all native selenium actions done with driver are correctly displayed. This indirectly test the LogAction aspect
	 * We check
	 * - all HtmlElement action logging
	 * - all composite actions logging
	 * - all PictureElement action logging
	 */
	@Test(groups = {"it"})
	public void testReportContainsDriverNativeActionsWithOverride() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverNativeActions"});

		// read the 'testDriverNativeActions' test result to see if native actions are also logged (overrideSeleniumNativeAction is true)
		String detailedReportContent2 = readTestMethodResultFile("testDriverNativeActions");
		System.out.println(detailedReportContent2);

		detailedReportContent2 = detailedReportContent2.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent2.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on HtmlElement , by=\\{By.id: text2} with args: \\(true, true, \\[some text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent2.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click on HtmlElement , by=\\{By.id: button2} </div></li>.*"));

	}

	/**
	 * Test reporting on PageObjectFactory (use of @FindBy)
	 * selenium override is enabled
	 */
	@Test(groups = {"it"})
	public void testReportContainsDriverNativeActionsWithOverrideOnPageObjectFactory() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverNativeActionsOnPageObjectFactory"});

		String detailedReportContent = readTestMethodResultFile("testDriverNativeActionsOnPageObjectFactory");

		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		Assert.assertTrue(detailedReportContent.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on HtmlElement , by=\\{By.id: text2} with args: \\(true, true, \\[some text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent.matches(".*<li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click on HtmlElement , by=\\{By.id: button2} </div></li>.*"));

	}
	/**
	 * Test reporting on PageObjectFactory (use of @FindBy)
	 * selenium override is disabled
	 */
	@Test(groups = {"it"})
	public void testReportContainsDriverNativeActionsWithoutOverrideOnPageObjectFactory() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverNativeActionsOnPageObjectFactoryWithoutOverride"});

		String detailedReportContent = readTestMethodResultFile("testDriverNativeActionsOnPageObjectFactoryWithoutOverride");

		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// check that without override, native actions are logged
		Assert.assertTrue(detailedReportContent.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on Element located by id: text2 with args: \\(\\[some text,], \\) </div></li>.*"));
		Assert.assertTrue(detailedReportContent.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> click on Element located by id: button2 </div></li>.*"));
		Assert.assertTrue(detailedReportContent.matches(".*<ul><li><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> selectByVisibleText on Element located by Select id: select with args: \\(option1, \\) </div></li>.*"));

	}

	/**
	 * Test display of failed actions / steps
	 */
	@Test(groups = {"it"})
	public void testReportContainsDriverFailedActions() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.REPLAY_TIME_OUT, "3");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverFailed"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverFailed");
			detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

			Assert.assertTrue(detailedReportContent1.matches(".*<li class=\"header-failed\"><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> sendKeys on TextFieldElement Text, by=\\{By.id: text___} with args: \\(true, true, \\[a text,], \\).*"));
		} finally {
			System.clearProperty(SeleniumTestsContext.REPLAY_TIME_OUT);
		}


	}

	/**
	 * Check all errors are recorded in detailed file
	 * - in execution logs
	 * - in Test end step
	 */
	@Test(groups = {"it"})
	public void testReportDetailsWithErrors() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		String detailedReportContent = readTestMethodResultFile("testInError");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is KO with error: class java.lang.AssertionError: error </div>.*"));

		// Check exception is logged and filtered
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error\"><div>class java.lang.AssertionError: error</div>"
				+ "<div class=\"stack-element\"></div>"
				+ "<div class=\"stack-element\">at com.seleniumtests.it.stubclasses.StubTestClass.testInError\\(StubTestClass.java:\\d+\\)</div>.*"
				+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.ReporterTest.executeSubTest\\(ReporterTest.java:\\d+\\)</div>"
				+ "<div class=\"stack-element\">at com.seleniumtests.it.reporter.TestSeleniumTestsReporter2.testReportDetailsWithErrors\\(TestSeleniumTestsReporter2.java:\\d+\\)</div>.*"));

		// error message of the assertion is displayed in step
		Assert.assertTrue(detailedReportContent.matches(".*</ul><div class=\"message-error\">\\s+class java.lang.AssertionError: error\\s+</div></div>.*"));

		// check that when test is KO, error cause is displayed
		Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: Test is KO with error: class java.lang.AssertionError: "));

		//
		Assert.assertTrue(detailedReportContent.contains("<th>Last State</th><td><a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"error\"></i></a></td>"));

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.contains("<td class=\"info\"><a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"error\"></i></a></td>"));
		Assert.assertTrue(mainReportContent.contains("<td class=\"info\"><a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"some exception\"></i></a></td>"));
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "<td class=\"info\"><a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\""), 2); // only the failed tests have error log
	}

	/**
	 * Check test values are displayed (call to logger.logTestValue()) shown as a table
	 */
	@Test(groups = {"it"})
	public void testReportDetailsWithTestValues() throws Exception {
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testAndSubActions", "testInError", "testWithException"});

		String detailedReportContent = readTestMethodResultFile("testInError");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.matches(".*<table class=\"table table-bordered table-sm\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> key </div></td><td>we found a value of</td><td>10</td></tr></table>.*"));
	}

	@Test(groups = {"it"})
	public void testReportDetailsContainsParentConfigurations() throws Exception {

		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForListener1"});

		String detailedReportContent = readTestMethodResultFile("test1Listener1");

		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button><span class=\"step-title\"> Pre test step: beforeTestInParent - "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button><span class=\"step-title\"> Pre test step: beforeTest -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button><span class=\"step-title\"> Post test step: afterClassInParent - "), 1);

	}


	/**
	 * Check all steps are present in detailed report file. For cucumber, check that method name is the Scenario name, not the "feature" generic method
	 * Test OK
	 */
	@Test(groups = {"it"})
	public void testCucumberStart2() throws Exception {

		executeSubCucumberTests("core_7", 1);

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href='core_7/TestReport\\.html'.*?>core_7</a>.*"));

		String detailedReportContent = readTestMethodResultFile("core_7");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> ^write2 (\\w+)$ with args: (tutu, )"));
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is OK </div>.*"));
	}

	/**
	 * issue #362: check that with scenario outline, we have the 2 results
	 */
	@Test(groups = {"it"})
	public void testCucumberScenarioOutline() throws Exception {

		executeSubCucumberTests("core_ .*", 1);

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href='core__tata/TestReport\\.html'.*?>core__tata</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='core__titi/TestReport\\.html'.*?>core__titi</a>.*"));

	}

	/**
	 * issue #362: check that with scenario outline, we have the 2 results even if name is the same
	 */
	@Test(groups = {"it"})
	public void testCucumberScenarioOutlineUniqueName() throws Exception {

		executeSubCucumberTests("core_unique_name", 1);

		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='core_unique_name-_tata_/TestReport\\.html'.*?>core_unique_name-_tata_</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='core_unique_name-_titi_/TestReport\\.html'.*?>core_unique_name-_titi_</a>.*"));

	}

	/**
	 * issue #362: check that with scenario outline, we have the 2 results even if name is the same
	 * issue #366: also check with an accent so that we can verify it's removed
	 */
	@Test(groups = {"it"})
	public void testCucumberScenarioOutlineUniqueLongName() throws Exception {

		executeSubCucumberTests("a very long scenàrio outline name which should not have been created but is there but we should not strip it only display a message saying its much too long", 1);

		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='a_very_long_scenario_outline_name_which_should_not_have_been_created_but_is_there_but_we_should/TestReport\\.html'.*?>a_very_long_scenario_outline_name_which_should_not_have_been_created_but_is_there_but_we_should</a>.*"));

		readTestMethodResultFile("a_very_long_scenario_outline_name_which_should_not_have_been_created_but_is_there_but_we_should");
	}

	/**
	 * Check all steps are present in detailed report file. For cucumber, check that method name is the Scenario name, not the "feature" generic method
	 * Test OK
	 * Check if it's possible to have '??' is scenario name
	 */
	@Test(groups = {"it"})
	public void testCucumberScenarioWithSpecialName() throws Exception {

		executeSubCucumberTests("my beautiful scenario ?? ok ??", 1);

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href='my_beautiful_scenario_.._ok_..-/TestReport\\.html'.*?>my_beautiful_scenario_.._ok_..-</a>.*"));

		String detailedReportContent = readTestMethodResultFile("my_beautiful_scenario_.._ok_..-");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> ^write (\\w+)$ with args: (tatu, )"));
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is OK </div>.*"));

	}

	/**
	 * Check that test name is correctly reported in cucumber mode when threads are used
	 * Test OK
	 */
	@Test(groups = {"it"})
	public void testCucumberMultiThread() throws Exception {

		executeSubCucumberTests("core_3,core_4", 5);

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href='core_3/TestReport\\.html'.*?>core_3</a>.*"));
	}

	/**
	 * Test that HTML report is correctly encoded
	 */
	@Test(groups = {"it"})
	public void testHtmlCharacterEscape() throws Exception {
		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});

		String detailedReportContent = readTestMethodResultFile("testWithException");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// check step 1 has been encoded
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-log message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Test is KO with error: class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;.*"));

		// check logs are also encoded
		Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: Test is KO with error: class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;"));

		// check exception stack trace is encoded
		Assert.assertTrue(detailedReportContent.contains("class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;"));

		// check no HTML code remains in file
		Assert.assertFalse(detailedReportContent.contains("<strong>"));

		// check exception info is correctly encoded
		Assert.assertTrue(detailedReportContent.contains("<th>Last State</th><td><a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"&amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;\"></i></a></td>"));
	}

	/**
	 * Test that HTML report is correctly encoded with 2 exceptions
	 */
	@Test(groups = {"it"})
	public void testHtmlCharacterEscapeMultipleExceptions() throws Exception {
		executeSubTest(new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});

		String detailedReportContent = readTestMethodResultFile("testWithChainedException");

		// check exception stack trace is encoded with the 2 exceptions
		Assert.assertTrue(detailedReportContent.contains("<div>class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;</div>"));
		Assert.assertTrue(detailedReportContent.contains("Caused by root &lt;error&gt;</div>"));

	}


	/**
	 * Check that information recorded during test, by calling 'SeleniumRobotTestPlan.addTestInfo(key, value)' are added to summary and test report
	 */
	@Test(groups = {"it"})
	public void testWithTestInfo() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[]{"testWithInfo1", "testWithInfo2", "testAndSubActions"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<td class=\"info\"></td><td class=\"info\"></td>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<td class=\"info\">12</td><td class=\"info\"></td>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<td class=\"info\"></td><td class=\"info\"><a href=\"http://foo/bar/12345\">12345</a></td>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<th> bug&eacute; &lt;&quot;ID&quot;&gt; </th><th> user ID </th>.*"));

		String detailedReportContent = readTestMethodResultFile("testWithInfo1");
		Assert.assertTrue(detailedReportContent.contains("<th>bug&eacute; &lt;&quot;ID&quot;&gt;</th><td>12</td>"));
	}

	/**
	 * issue #99: Check summary with multiple suites executing the same test. Both test should be presented
	 */
	@Test(groups = {"it"})
	public void testMultiSuitesdReport() throws Exception {

		executeMultiSuites(new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, new String[]{"testAndSubActions"});

		// check content of summary report file
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testAndSubActions-1/TestReport\\.html'.*?>testAndSubActions-1</a>.*"));
	}


	/**
	 * Check that when an action fails, a warning is displayed in step and logs
	 * This helps in the case the action error is catched
	 */
	@Test(groups = {"it"})
	public void testLogActionErrorsAsWarning() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverShortKoWithCatchException"});
			String detailedReportContent = readTestMethodResultFile("testDriverShortKoWithCatchException");
			detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

			// test all error log is displayed in execution logs
			Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found</div>"));
			Assert.assertTrue(detailedReportContent.contains("<div>at com.seleniumtests.it.driver.support.pages.DriverTestPage._writeSomethingOnNonExistentElementWithCatch"));
			Assert.assertTrue(detailedReportContent.contains("<div>For documentation on this error, please visit: https://www.selenium.dev/documentation/webdriver/troubleshooting/errors#no-such-element-exception</div>")); // checks that line not showing thread name are in logs
			Assert.assertTrue(detailedReportContent.contains("Warning: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found<br/>"
					+ "For documentation on this error, please visit: https://www.selenium.dev/documentation/webdriver/troubleshooting/errors#no-such-element-exception<br/>")); // warning displayed in step
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
	}

	/**
	 * Check call to 'isDisplayedRetry' when element is not present should create a failed step with warning, but no exception displayed
	 */
	@Test(groups={"it"})
	public void testNoFailedStepForIsDisplayedRetry() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.REPLAY_TIME_OUT, "3");

			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverIsDisplayedRetry"});

			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverIsDisplayedRetry");
			detailedReportContent1 = detailedReportContent1.replaceAll("\\s+", " ");

			Assert.assertTrue(detailedReportContent1.matches(".*<div class=\"message-warning message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Warning: Searched element \\[TextFieldElement Text, by=\\{By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be foun.*")); // warning
			Assert.assertFalse(detailedReportContent1.contains("<div class=\"message-error\">class org.openqa.selenium.NoSuchElementException: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found<br/>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.REPLAY_TIME_OUT);
		}
	}


	@Test(groups = {"it"})
	public void testNoDescription() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[]{"testNoDescription"});

		String summaryReport = readSummaryFile();
		Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"no description available\""));

		String detailedReportContent = readTestMethodResultFile("testNoDescription");
		Assert.assertFalse(detailedReportContent.contains("<th width=\"200px\">Description</th>"));
	}

	/**
	 * Check that if a user param is set from command line, description can use it
	 */
	@Test(groups = {"it"})
	public void testDescriptionWithUserParam() throws Exception {

		try {
			System.setProperty("url", "http://mysite.com");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[]{"testWithDescription"});

			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"a test with param http://mysite.com\""));

			String detailedReportContent = readTestMethodResultFile("testWithDescription");
			Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>a test with param http://mysite.com</td>"));
		} finally {
			System.clearProperty("url");
		}

	}

	/**
	 * Test that a param added inside test can also be used
	 */
	@Test(groups = {"it"})
	public void testDescriptionWithParamCreatedInTest() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[]{"testWithParamCreatedInTest"});

		String summaryReport = readSummaryFile();
		Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"a test on Bob account account-12345\""));

		String detailedReportContent = readTestMethodResultFile("testWithParamCreatedInTest");
		Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>a test on Bob account account-12345</td>"));

	}

	/**
	 * Test interpolation of method parameters when they are referenced as 'arg0', 'arg1', ..., 'argN' in description
	 */
	@Test(groups = {"it"})
	public void testDescriptionWithDataProvider() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[]{"testDataProvider"});


		String summaryReport = readSummaryFile();
		Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"a test with param data2 and data1 from dataprovider\""));
		Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"a test with param data3 and data4 from dataprovider\""));

		String detailedReportContent = readTestMethodResultFile("testDataProvider");
		Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>a test with param data2 and data1 from dataprovider</td>"));

		String detailedReportContent2 = readTestMethodResultFile("testDataProvider-1");
		Assert.assertTrue(detailedReportContent2.contains("<th width=\"200px\">Description</th><td>a test with param data3 and data4 from dataprovider</td>"));
	}

	/**
	 * Test interpolation of method parameters when they are referenced as 'arg0', 'arg1', ..., 'argN' in description
	 */
	@Test(groups = {"it"})
	public void testDescriptionWithLineBreak() throws Exception {

		try {
			System.setProperty("url", "http://mysite.com");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[]{"testWithLineBreaksInDescription"});

			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("info=\"ko\" data-toggle=\"tooltip\" title=\"a test with param http://mysite.comand line breaks\"")); // line break is removed by 'summaryReport' call

			String detailedReportContent = readTestMethodResultFile("testWithLineBreaksInDescription");
			Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>a test with param http://mysite.com<br/>and line breaks</td>"));
		} finally {
			System.clearProperty("url");
		}
	}

	/**
	 * Test special characters are correctly handled in description
	 */
	@Test(groups = {"it"})
	public void testDescriptionWithSpecialCharacters() throws Exception {

		try {
			System.setProperty("url", "http://mysite.com");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[]{"testWithDescriptionAndSpecialCharacters"});

			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"This test is always &lt;OK&gt; &amp; &quot;green&quot;\""));

			String detailedReportContent = readTestMethodResultFile("testWithDescriptionAndSpecialCharacters");
			Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>This test is always &lt;OK&gt; &amp; &quot;green&quot;</td>"));
		} finally {
			System.clearProperty("url");
		}
	}

	/**
	 * Check error cause is displayed in report
	 */
	@Test(groups = {"it"})
	public void testStepAnnotationWithError() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[]{"testCauseWithErrorAndDetails"});

		String detailedReportContent = readTestMethodResultFile("testCauseWithErrorAndDetails");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Only failing step contains the information message
		Assert.assertTrue(detailedReportContent.matches(".*<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> add - 0\\.\\d+ secs</span></div><div class=\"box-body\"><div class=\"step-info\"><i class=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: Check your scripts</span></div>.*"));
		Assert.assertFalse(detailedReportContent.matches(".*<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end - 0\\.\\d+ secs</span></div><div class=\"box-body\"><div class=\"step-info\"><i class=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: Check your scripts</span></div>.*"));
	}

	@Test(groups = {"it"})
	public void testStepAnnotationWithErrorNoDetails() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[]{"testCauseWithErrorNoDetails"});

		String detailedReportContent = readTestMethodResultFile("testCauseWithErrorNoDetails");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// Only failing step contains the information message
		Assert.assertFalse(detailedReportContent.matches(".*<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end - 0\\.\\d+ secs</span></div><div class=\"box-body\"><div class=\"step-info\"><i class=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: </span></div>.*"));
	}

	@Test(groups = {"it"})
	public void testStepAnnotationNoErrors() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[]{"testCauseNoError"});

		String detailedReportContent = readTestMethodResultFile("testCauseNoError");

		// add step is not in error, error cause is not displayed
		Assert.assertFalse(detailedReportContent.contains("<div class=\"step-info\"><i class=\"fas fa-info-circle\"></i>"));

	}

	@Test(groups = {"it"})
	public void testStepAnnotationNoErrorCause() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[]{"testNoCauseAndError"});

		String detailedReportContent = readTestMethodResultFile("testNoCauseAndError");

		// add step is not in error, error cause is not displayed
		Assert.assertFalse(detailedReportContent.contains("<div class=\"step-info\"><i class=\"fas fa-info-circle\"></i>"));

	}

	@Test(groups = {"it"})
	public void testReportWithLighthouseExecution() throws Exception {

		executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverWithLighthouse"});

		String detailedReportContent = readTestMethodResultFile("testDriverWithLighthouse");
		detailedReportContent = detailedReportContent.replaceAll("\\s+", " ");

		// check lighthouse report is included
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-snapshot\">Lighthouse JSON http://\\d+.\\d+.\\d+.\\d+:\\d+/test.html: <a href='lighthouse/http.\\d+.\\d+.\\d+.\\d+.\\d+test.html-\\w+.json'>file</a></div>.*"));
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-snapshot\">Lighthouse HTML http://\\d+.\\d+.\\d+.\\d+:\\d+/test.html: <a href='lighthouse/http.\\d+.\\d+.\\d+.\\d+.\\d+test.html-\\w+.html'>file</a></div>.*"));

		// check accessibility is displayed
		Assert.assertTrue(detailedReportContent.matches(".*<tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td><div class=\"message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> accessibility </div></td><td>accessibility</td><td>.*"));

		// check lighthouse error is included
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error message-conf\"><span class=\"stepTimestamp mr-1\">\\d+:\\d+:\\d+.\\d+</span> Lighthouse did not execute correctly </div>.*"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-snapshot\">Lighthouse logs some.bad.url: <a href='some.bad.url"));
	}

}



































