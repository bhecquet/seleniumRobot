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
package com.seleniumtests.it.reporter;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.stubclasses.StubTestClass;

public class TestSeleniumTestsReporter2 extends ReporterTest {

	/**
	 * Check summary format in multithread
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultithreadReport() throws Exception {

		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));

		// issue #331: check that result files have been generated only once
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
		Assert.assertTrue(StringUtils.countMatches(logs, "testInError/PERF-result.xml") == 1);
		Assert.assertTrue(StringUtils.countMatches(logs, "testAndSubActions/PERF-result.xml") == 1);
		Assert.assertTrue(StringUtils.countMatches(logs, "testWithException/PERF-result.xml") == 3); // once per retry
		
		// issue  #312: check that result files have been generated before test end
		Assert.assertTrue(StringUtils.indexOf(logs, "testInError/PERF-result.xml") < StringUtils.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(StringUtils.indexOf(logs, "testAndSubActions/PERF-result.xml") < StringUtils.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(StringUtils.indexOf(logs, "testWithException/PERF-result.xml") < StringUtils.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
	}
	
	/**
	 * Check that test report do not display tabs when no snapshot comparison is requested
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNoSnapshotComparison() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		// no bullet as no snapshot comparison is done
		String summaryReport = readSummaryFile();
		Assert.assertFalse(summaryReport.contains("<i class=\"fas fa-circle "));
		
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\"  style=\"display: none;\" >"));
		Assert.assertFalse(detailedReportContent.contains("</button> Snapshot comparison"));
		
	}
	
	/**
	 * issue #351: Check that when snapshot server is used, but a problem occurs posting information, snapshot tab is not displayed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonErrorDuringTransfer() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 500, "Internal Server Error", "body");	
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check result is ok and comparison result is shown through green bullet
			String summaryReport = readSummaryFile();
			Assert.assertFalse(summaryReport.contains("<i class=\"fas fa-circle circle")); // no snapshot comparison has been performed
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// no snapshot tab displayed
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\"  style=\"display: none;\" >"));
			
			// message saying that error occured when contacting snapshot server
			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("request to http://localhost:4321 failed: Internal Server Error"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a green bullet should be visible on summary result when comparison is OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonOkDisplayOnly() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check result is ok and comparison result is shown through green bullet
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSuccess\" data-toggle=\"tooltip\" title=\"snapshot comparison successfull\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			
			// successful step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-info\">Comparison successful</div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box success\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-success \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Test case where no snapshot has been sent to server, 'snapshot comparison' step should be green with message stating that no picture has been processed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotNoComparisonDisplayOnly() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': []}");		
			
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check result is ok and comparison result is shown through blue bullet (no comparison to do)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			
			// successful step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">No comparison to do (no snapshots)</div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box success\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab active / skipped
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * Result remains OK as behaviour is "displayOnly"
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoDisplayOnly() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false, 'computingError': []}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			// check result is ok and comparison result is shown through red bullet (comparison KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			
			// failed step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Comparison failed: </div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-failed \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * Result is KO as behavior is 'changeTestResult'
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoChangeTestResult() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			// check result is KO (due to option 'changeTestResult') and comparison result is shown through red bullet (comparison KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ko\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			
			// failed step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Comparison failed: </div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-failed \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoAddTestResult() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\"></i><a href='testAndSubActions/TestReport.html' info=\"ok\" "));
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleFailed\" data-toggle=\"tooltip\" title=\"snapshot comparison failed\"></i><a href='snapshots-testAndSubActions/TestReport.html' info=\"ko\""));
			
			String detailedReportContent = readTestMethodResultFile("snapshots-testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			
			// failed step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Comparison failed: </div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-failed \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// check execution logs contains the exception (but not logs)
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\"><div>class com.seleniumtests.customexception.ScenarioException: Snapshot comparison failed</div>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	@Test(groups={"it"})
	public void testSnapshotComparisonSkipDisplayOnly() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error computing']}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check result is ok and comparison result is shown through red bullet (comparison KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-skipped \" id=\"snapshot-tab\"")); // tab is in blue as comparison skipped
			
			// failed step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Comparison skipped: &quot;error computing&quot;</div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * Result remains OK as behaviour is "displayOnly"
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonSkipChangeTestResult() throws Exception {
	
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error computing']}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check result is OK (even with option 'changeTestResult' because comparison is skipped) and comparison result is shown through blue bullet (comparison Skipped)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\">"));
			Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\""));
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-skipped \" id=\"snapshot-tab\"")); // tab is in blue as comparison skipped
			
			// failed step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Comparison skipped: &quot;error computing&quot;</div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used, we see a tab pointing to snapshot comparison results
	 * Moreover, a red bullet should be visible on summary result when comparison is KO
	 * 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonSkipAddTestResult() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error computing']}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\"></i><a href='testAndSubActions/TestReport.html' info=\"ok\" "));
			Assert.assertTrue(summaryReport.contains("<i class=\"fas fa-circle circleSkipped\" data-toggle=\"tooltip\" title=\"snapshot comparison skipped\"></i><a href='snapshots-testAndSubActions/TestReport.html' info=\"skipped\""));
			
			String detailedReportContent = readTestMethodResultFile("snapshots-testAndSubActions");
			
			// tabs are shown
			Assert.assertTrue(detailedReportContent.contains("<div id=\"tabs\" style=\"display: block;\" >"));
			
			// failed step has been added for comparison
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Comparison skipped: &quot;error computing&quot;</div>"));
			Assert.assertTrue(detailedReportContent.matches(".*<div class=\"box collapsed-box failed\">.*?<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Snapshot comparison.*"));
			
			// snapshot tab not active
			Assert.assertTrue(detailedReportContent.contains("<a class=\"nav-link  tab-skipped \" id=\"snapshot-tab\" data-toggle=\"tab\" href=\"#snapshots\" role=\"tab\" aria-controls=\"profile\" aria-selected=\"false\">Snapshots</a>"));
			
			
			// iframe present with the right test case id
			Assert.assertTrue(detailedReportContent.contains("<iframe src=\"http://localhost:4321/snapshot/compare/stepList/15/?header=true\" id=\"snapshot-iframe\" frameborder=\"0\"></iframe>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check summary format in monothread
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMonothreadReport() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
		
		// issue #331: check that result files have been generated at least twice (one during test run and one at the end)
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
		Assert.assertTrue(StringUtils.countMatches(logs, "testInError/PERF-result.xml") == 1);
		Assert.assertTrue(StringUtils.countMatches(logs, "testAndSubActions/PERF-result.xml") == 1);
		Assert.assertTrue(StringUtils.countMatches(logs, "testWithException/PERF-result.xml") == 3); // once per retry

		// issue  #312: check that result files have been generated before test end (meaning they are generated after the test execution
		Assert.assertTrue(StringUtils.indexOf(logs, "testInError/PERF-result.xml") < StringUtils.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(StringUtils.indexOf(logs, "testAndSubActions/PERF-result.xml") < StringUtils.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		Assert.assertTrue(StringUtils.indexOf(logs, "testWithException/PERF-result.xml") < StringUtils.indexOf(logs, "SeleniumRobotTestListener: Test Suite Execution Time"));
		
		// issue #319: check that if no test info is recorded, columns are not there / Last State info is always there
		Assert.assertTrue(mainReportContent.contains("<td class=\"info\">"));
		Assert.assertTrue(mainReportContent.contains("<th> Last State </th>"));
	}
	
	/**
	 * Check "Last State" is always there even if nothing needs to be displayed (should never happen as we should have at least the last screen capture
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMonothreadReportTestOk() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		// issue #319: check that if no test info is recorded, columns are not there / Last State info is always there
		Assert.assertTrue(mainReportContent.contains("<td class=\"info\">"));
		Assert.assertTrue(mainReportContent.contains("<th> Last State </th>"));
	}
	
	/**
	 * Check generic steps are logged
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testGenericSteps() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForGenericSteps"}, ParallelMode.METHODS, new String[] {"testDriver"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testDriver/TestReport\\.html' info=\"ok\".*?>testDriver</a>.*"));

		String detailedReportContent1 = readTestMethodResultFile("testDriver");
		
		// check generic steps are logged
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> sendKeysToField with args: (textElement, foo, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> _reset"));
	}
	
	/**
	 * Check single test report format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultithreadTestReport() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.TESTS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
		
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
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testTestReportContainsOnlyItsAfterMethodSteps() throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForIssue143"});
		
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
	 * issue #251: check error message is displayed for any action that failed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDetailedReportWithOneStepFailed() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testOkWithOneStepFailed"});
		
		// check content of summary report file
		String detailedReportContent = readTestMethodResultFile("testOkWithOneStepFailed");
		Assert.assertTrue(detailedReportContent.contains("<li class=\"header-failed\">failAction <br/>class com.seleniumtests.customexception.DriverExceptions: fail</li>"));
		
	}
	
	/**
	 * issue #251: check error message is displayed for any action that failed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDetailedReportWithOneSubStepFailed() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testOkWithOneSubStepFailed"});
		
		// check content of summary report file
		String detailedReportContent = readTestMethodResultFile("testOkWithOneSubStepFailed");
		
		// failed action is visible as failed
		Assert.assertTrue(detailedReportContent.contains("<li class=\"header-failed\">failAction <br/>class com.seleniumtests.customexception.DriverExceptions: fail</li>"));
		
		// parent action is OK, so it should not be marked as failed
		Assert.assertTrue(detailedReportContent.contains("<li>addWithCatchedError with args: (1, )</li>"));
		
	}
	
	/**
	 * A snapshot is taken when soft assertion is enabled and assertion fails
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDetailedReportContainsCaptureOnSoftAssertionEnabled() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverWithAssert"});
			
			String detailedReportContent = readTestMethodResultFile("testDriverWithAssert");
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
			
			// check that with assertion error, snapshot is present
			Assert.assertTrue(detailedReportContent.contains("!!!FAILURE ALERT!!! - Assertion Failure: expected [true] but found [false]</div><div class=\"row\"><div class=\"message-snapshot col\"><div class=\"text-center\"><a href=\"#\" onclick=\"$('#imagepreview').attr('src', $('#"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}
	
	@Test(groups={"it"})
	public void testDetailedReportDoesNotContainCaptureOnSoftAssertionDisabled() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverWithAssert"});
			
			String detailedReportContent = readTestMethodResultFile("testDriverWithAssert");
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
			
			// check that with assertion error, snapshot is present
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">Assertion Failure: expected [true] but found [false]</div>"));
			Assert.assertFalse(detailedReportContent.contains("<div class=\"message-error\">!!!FAILURE ALERT!!! - Assertion Failure: expected [true] but found [false]</div>"
					+ "<div class=\"message-snapshot\">Output 'drv:main' browser: Current Window: Test page:"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}
	
	/**
	 * Check behaviour when Assert is used in test scenario (not in webpage)
	 * Assertion in scenario should be attached to the previous step which will be marked as failed
	 * Test end will also be in red
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDetailedReportWithSoftAssertInScenario() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testWithAssertInTest"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
			
			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertInTest");
			
			// check step with assertion inside is failed
			Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\">"
					+ "<button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> assertAction"));
			Assert.assertTrue(detailedReportContent.contains("</div><div class=\"box-body\"><ul><div class=\"message-error\">!!!FAILURE ALERT!!! - Assertion Failure: false error expected [true] but found [false]</div>"));
			
			// that assertion raised in test scenario is attached to previous step
			Assert.assertTrue(detailedReportContent.matches(".*<div class\\=\"box collapsed-box failed\"><div class\\=\"box-header with-border\">"
					+ "<button type\\=\"button\" class\\=\"btn btn-box-tool\" data-widget\\=\"collapse\"><i class\\=\"fas fa-plus\"></i></button><span class=\"step-title\"> getResult  - \\d+\\.\\d+ secs\\s*</span></div><div class\\=\"box-body\">"
					+ "<ul><div class\\=\"message-error\">!!!FAILURE ALERT!!! - Assertion Failure: Error in result expected \\[1\\] but found \\[2\\]</div>.*"));
			
			// check last step shows the assertion					
			Assert.assertTrue(detailedReportContent.matches(".*<div class\\=\"box collapsed-box failed\">.*?<i class\\=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> Test end - \\d+\\.\\d+ secs\\s*</span></div><div class\\=\"box-body\"><ul><div class\\=\"message-log\">Test is KO with error: class java.lang.AssertionError: !!! Many Test Failures \\(2\\)<br/>"
					+ "<br/>class java.lang.AssertionError: <br/>\\.<br/>Failure 1 of 2.*Failure 2 of 2.*"
					+ "<div class\\=\"message-error\">\\s+class java.lang.AssertionError: !!! Many Test Failures \\(2\\)<br/>.*"));
			
			// check last step before test end is OK because no error occurs in it
			Assert.assertTrue(detailedReportContent.matches(".*<div class\\=\"box collapsed-box success\">.*?<i class\\=\"fas fa-plus\"></i></button><span class=\"step-title\"> add with args: \\(3, \\).*"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}
	
	/**
	 * Check that when an assert is raised in sub step, the root step is marked as failed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDetailedReportWithSoftAssertInSubStep() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testWithAssertInSubStep"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
			
			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertInSubStep");
			
			// check that sub step failure (with assertion) caused the step to fail itself				
			Assert.assertTrue(detailedReportContent.matches(".*<div class\\=\"box collapsed-box failed\">.*?<i class\\=\"fas fa-plus\"></i>" // => step failed
					+ "</button><span class=\"step-title\"> assertWithSubStep  - \\d+\\.\\d+ secs</span></div><div class\\=\"box-body\"><ul><li>doNothing </li>"
					+ "<ul><li>doNothing on HtmlElement none, by=\\{By\\.id: none\\} </li><div class\\=\"row\"></div></ul>"
					+ "<li>assertAction </li><ul>" // => sub step with error
					+ "<div class\\=\"message-error\">!!!FAILURE ALERT!!! - Assertion Failure: false error expected \\[true\\] but found \\[false\\].*")); // error displayed
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}
	
	/**
	 * Check behaviur when hard Assert is used in test scenario (not in webpage)
	 * Test stops on first assertion and step is failed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDetailedReportWithHardAssertInScenario() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testWithAssertInTest"});

			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
			
			// check content of summary report file
			String detailedReportContent = readTestMethodResultFile("testWithAssertInTest");
			
			// check step with assertion inside is failed
			Assert.assertTrue(detailedReportContent.matches(".*<div class\\=\"box collapsed-box failed\">.*?<i class\\=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> getResult  - \\d+\\.\\d+ secs</span></div><div class\\=\"box-body\">.*?"
					+ "<div class\\=\"message-error\">\\s*Assertion Failure: Error in result expected \\[1\\] but found \\[2\\].*"));

			// Test end step also displays the error
			Assert.assertTrue(detailedReportContent.matches(".*<div class\\=\"box collapsed-box failed\">.*?<i class\\=\"fas fa-plus\"></i>"
					+ "</button><span class=\"step-title\"> Test end - \\d+\\.\\d+ secs</span></div><div class\\=\"box-body\"><ul><div class\\=\"message-log\">Test is KO with error: class java.lang.AssertionError: Error in result expected \\[1\\] but found \\[2\\]</div>"
					+ "<div class\\=\"message-log\">\\[NOT RETRYING\\] due to failed Assertion</div>.*?"
					+ "<div class\\=\"message-error\">\\s+class java.lang.AssertionError: Error in result expected \\[1\\] but found \\[2\\].*"
					));
			
			// test is stopped after assertion raised in test. AssertAction which would be executed later is never reached
			Assert.assertFalse(detailedReportContent.contains("</button><span class=\"step-title\"> assertAction"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}
	
	/**
	 * Check summary format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithSteps() throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testOk"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
		
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
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testFailsOnlyOnceAndRetriedOk() throws Exception {
		
		StubTestClass.failed = false;
		
		// execute only the test that fails the first time it's executed
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithExceptionOnFirstExec"});
		
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
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportWithResourcesFromCDN() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.OPTIMIZE_REPORTS, "true");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
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
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportWithResourcesFromLocal() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.OPTIMIZE_REPORTS, "false");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
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
	
	@Test(groups={"it"})
	public void testKeepAllResults(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testWithException2"});
			
			
			// issue #346: check all reports are generated
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			Assert.assertTrue(detailedReportContent.contains("<h4> Test Details"));
			
			String detailedReportContent2 = readTestMethodResultFile("testWithException2");

			// check the message and that no previous execution result is visible
			Assert.assertTrue(detailedReportContent2.contains("Previous execution results"));
			Assert.assertTrue(detailedReportContent2.contains("<h4> Test Details"));
			Assert.assertTrue(detailedReportContent2.contains("<a href=\"retry-testWithException2-1.zip\">retry-testWithException2-1.zip</a>"));
			
	
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}
	
	@Test(groups={"it"})
	public void testKeepAllResultsNoParallel(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[] {"testAndSubActions", "testWithException2"});
			
			String detailedReportContent = readTestMethodResultFile("testWithException2");
			
			// check the message and that no previous execution result is visible
			Assert.assertTrue(detailedReportContent.contains("Previous execution results"));
			Assert.assertTrue(detailedReportContent.contains("<a href=\"retry-testWithException2-1.zip\">retry-testWithException2-1.zip</a>"));

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
	@Test(groups={"it"})
	public void testKeepAllResultsWithoutRetry(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			String detailedReportContent = readTestMethodResultFile("testAndSubActions");
			
			// check the message and that no previous execution result is visible
			Assert.assertFalse(detailedReportContent.contains("Previous execution results"));
			Assert.assertFalse(detailedReportContent.contains("<a href=\"retry-testWithException-1.zip\">retry-testAndSubActions-1.zip</a>"));
			
			// issue #379: we souhld have not Previous result box
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<div class=\"box collapsed-box"), 8);
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}
	
	/**
	 * Check that logs of a failed attempt are not kept in the result directory (KEEP_ALL_RESULTS=false)
	 */
	@Test(groups={"it"})
	public void testDoNotKeepAllResults(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "false");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithException"});
			
			String detailedReportContent = readTestMethodResultFile("testWithException");
			
			// check the message and that no previous execution result is visible
			Assert.assertTrue(detailedReportContent.contains("No previous execution results, you can enable it via parameter '-DkeepAllResults=true'"));
			Assert.assertFalse(detailedReportContent.contains("<a href=\"retry-testWithException-1.zip\">retry-testWithException-1.zip</a>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}
	
	/**
	 * Check if test description made available by TestNG annotation is displayed in summary and detailed report
	 */
	@Test(groups={"it"})
	public void testTestDescription() throws Exception { 
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
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
	@Test(groups={"it"})
	public void testAllScreenshotsArePresent() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty("startLocation", "beforeMethod");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		
		// check all files are displayed
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/N-A_2-1_Pre_test_step._beforeMethod"));
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/N-A_2-1_Pre_test_step._beforeMethod-"));
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/test1Listener5_3-1_Test_end"));
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/test1Listener5_3-1_Test_end"));

		
	}
	
	/**
	 * Check that automatic steps create all steps in report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testAutomaticSteps() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testFailedWithException/TestReport\\.html'.*?>testFailedWithException</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testFailedWithSoftAssertDisabled/TestReport\\.html'.*?>testFailedWithSoftAssertDisabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testFailedWithSoftAssertEnabled/TestReport\\.html'.*?>testFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testMultipleFailedWithSoftAssertEnabled/TestReport\\.html'.*?>testMultipleFailedWithSoftAssertEnabled</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testOk/TestReport\\.html'.*?>testOk</a>.*"));
		

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent2 = readTestMethodResultFile("testFailedWithSoftAssertDisabled");
		Assert.assertTrue(detailedReportContent2.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button><span class=\"step-title\"> assertAction"));
		Assert.assertFalse(detailedReportContent2.contains("</button><span class=\"step-title\"> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent2.contains("</button><span class=\"step-title\"> Test end"));
		
		// check that with soft assertion, all steps are displayed
		String detailedReportContent3 = readTestMethodResultFile("testFailedWithSoftAssertEnabled");
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> assertAction"));
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent3.contains("</button><span class=\"step-title\"> Test end"));
		
		// check that with error, remaining steps are skipped
		String detailedReportContent1 = readTestMethodResultFile("testFailedWithException");
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> failAction"));
		Assert.assertFalse(detailedReportContent1.contains("</button><span class=\"step-title\"> add with args: (1, )"));
		Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> Test end"));

		// check "openPage" prints the page name
		String detailedReportContent4 = readTestMethodResultFile("testOk");
		Assert.assertTrue(detailedReportContent4.contains("</button><span class=\"step-title\"> openPage with args: (null, )"));
		Assert.assertTrue(detailedReportContent4.contains("<div class=\"box-body\"><ul><li>Opening page CalcPage</li>"));
		Assert.assertFalse(detailedReportContent4.contains("</button><span class=\"step-title\"> failAction"));
		Assert.assertTrue(detailedReportContent4.contains("</button><span class=\"step-title\"> add with args: (1, 1, )"));
		Assert.assertTrue(detailedReportContent4.contains("</button><span class=\"step-title\"> Test end"));
		
	}
	
	@Test(groups={"it"})
	public void testAttachmentRenaming() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});

		String detailedReportContent1 = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshot/testAndSubActions_0-1_step_1--rtened.png\""));		
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/testAndSubActions_0-1_step_1--tened.html' target=html>"));	
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_0-1_step_1--tened.html").toFile().exists());
	}
	
	@Test(groups={"it"})
	public void testAttachmentRenamingWithOptimizeReports() throws Exception {
		try {
			System.setProperty("optimizeReports", "true");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		} finally {
			System.clearProperty("optimizeReports");
		}
		
		String detailedReportContent1 = readTestMethodResultFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshot/testAndSubActions_0-1_step_1--rtened.png\""));		
		Assert.assertTrue(detailedReportContent1.contains(" | <a href='htmls/testAndSubActions_0-1_step_1--tened.html.zip' target=html>"));	
		
		// check file has been moved / compressed
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_0-1_step_1--tened.html.zip").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "htmls", "testAndSubActions_0-1_step_1--tened.html").toFile().exists());
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
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testOk/TestReport\\.html'.*?>testOk</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testWithAssert/TestReport\\.html'.*?>testWithAssert</a>.*"));
		
		
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
		
		// check execution logs are in error
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button> Execution logs"));
		
		// test first step is OK and second one is failed (this shows indirectly that internal step is marked as failed
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test start"));
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> assert exception"));
		
		// check exception is present in step
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"message-log\">Test is KO with error: class java.lang.AssertionError: false error expected [true] but found [false]</div>"));
		
	}
	
	/**
	 * Check that manual also mask password if user requests it (gives password to mask in report)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testManualStepsPasswordMasking() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassManualSteps"}, ParallelMode.METHODS, new String[] {"testOkPassword"});

		// check that without soft assertion, 'add' step is skipped
		String detailedReportContent1 = readTestMethodResultFile("testOkPassword");
		
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
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test1/TestReport\\.html' info=\"ok\".*?>test1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test4/TestReport\\.html' info=\"ko\".*?>test4</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test3/TestReport\\.html' info=\"skipped\".*?>test3</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test5/TestReport\\.html' info=\"ko\".*?>test5</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test2/TestReport\\.html' info=\"skipped\".*?>test2</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test6/TestReport\\.html' info=\"ok\".*?>test6</a>.*"));
		Assert.assertFalse(mainReportContent.contains("$testResult.getAttribute(\"methodName\")")); // check all test methods are filled
	}
	
	/**
	 * Check format of messages in detailed report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsMessageStyles() throws Exception {
		
		try {
			System.setProperty("customTestReports", "PERF::xml::reporter/templates/report.perf.vm,SUP::xml::reporter/templates/report.supervision.vm");
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
			
			
			// check style of messages
			String detailedReportContent = readTestMethodResultFile("testInError");
			
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
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		// check content of summary report file
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");

		Assert.assertTrue(detailedReportContent.matches(
				".*<ul>"													// root step
					+ "<li>click button</li>"
					+ "<li>sendKeys to text field</li>"
					+ "<li>step 1.3: open page</li>"					// sub-step
					+ "<ul>"
						+ "<li>click link</li>"							// action in sub step
						+ "<div class\\=\"message-log\">a message</div>"	// message in sub step
						+ "<li>sendKeys to password field</li>"			// action in sub step
					+ "<div class\\=\"row\"></div></ul><div class=\"row\">" 
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">.*src\\=\"screenshot/testAndSubActions_0-1_step_1--rtened\\.png\" style\\=\"width: 300px\">.*"
					));
		
	}
	
	/**
	 * Check logs are written in file
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithLogs() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		// check content of detailed report file
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		
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
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		
		// step in warning because some actions are failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box warning\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> step 1 - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> step 2 - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end - "));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button> Execution logs"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is OK</div>"));
		
		// check failed steps are in red
		String detailedReportContent2 = readTestMethodResultFile("testInError");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent2.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end"));
		
		// check logs are written only once 
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "[main] ScenarioLogger: Test is OK</div>"), 1);
		
	}
	
	@Test(groups={"it"})
	public void testReportContainsCustomScreenshot() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
		
		// read 'testDriver' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriverCustomSnapshot");
		
		Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/my_snapshot"));	
		Assert.assertTrue(detailedReportContent1.contains("<a href='htmls/my_snapshot"));	
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"text-center\">drv:main-my snapshot: my snapshot</div>"));	
		Assert.assertTrue(detailedReportContent1.contains(" src=\"screenshots/my_snapshot-"));	
	}
	
	/**
	 * Check test information shows a link to last test step
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsTestEndScreenshotQuicklink() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
		
		// read 'testDriverCustomSnapshot' report. This contains calls to HtmlElement actions
		String detailedReportContent1 = readTestMethodResultFile("testDriverCustomSnapshot");
		
		Assert.assertTrue(detailedReportContent1.matches(".*<th>Last State</th><td><a href=\"screenshots/testDriverCustomSnapshot_7-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a></td>.*"));
		
		// check shortcut to last state is displayed
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<td class=\"info\"><a href=\"testDriverCustomSnapshot/screenshots/testDriverCustomSnapshot_7-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a></td>.*"));
		
	}
	
	/**
	 * Check that video capture file is not present in result if not requested
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsNoVideoCapture() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "false");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriver");
			
			Assert.assertFalse(detailedReportContent1.contains("Video capture: <a href='videoCapture.avi'>file</a>"));
			
			// check shortcut to video is NOT present in detailed report
			Assert.assertFalse(detailedReportContent1.matches(".*<th>Last State</th><td><a href=\"screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"videoCapture.avi\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*"));
			
			// check shortcut to video is NOT present in summary report
			String mainReportContent = readSummaryFile();
			Assert.assertFalse(mainReportContent.matches(".*<td class=\"info\"><a href=\"testDriver/screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"testDriver/videoCapture.avi\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
		
	}
	
	/**
	 * Check that video capture file is present in result if requested
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsVideoCapture() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriver");
			
			Assert.assertTrue(detailedReportContent1.contains("Video capture: <a href='videoCapture.avi'>file</a>"));
			
			// check shortcut to video is present in detailed report
			Assert.assertTrue(detailedReportContent1.matches(".*<th>Last State</th><td><a href=\"screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"videoCapture.avi\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*"));
			
			// check shortcut to video is present in summary report
			String mainReportContent = readSummaryFile();
			Assert.assertTrue(mainReportContent.matches(".*<td class=\"info\"><a href=\"testDriver/screenshots/testDriver_8-1_Test_end--\\w+.png\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a><a href=\"testDriver/videoCapture.avi\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a></td>.*"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
		
	}
	

	/**
	 * Check reference image for step is displayed when "recordResult" option is set and video capture is enabled
	 * This reference is only shown when test fails
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsStepReferenceForFailedStep() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			
			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));	
					
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");
			
			// reference and current state should be displayed
			Assert.assertTrue(detailedReportContent1.matches(".* class\\=\"step-title\"> _writeSomethingOnNonExistentElement  - .*"
					+ "at com\\.seleniumtests\\.it\\.driver\\.support\\.pages\\.DriverTestPage\\._writeSomethingOnNonExistentElement\\(DriverTestPage\\.java.*?"
					+ "<div class\\=\"row\">"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">"
					+ "<a href\\=\"#\" onclick\\=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class\\=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Valid-reference</div>.*")); // the reference
			
			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 1);
			
			// check step beginning state is present with valid path
			Assert.assertTrue(detailedReportContent1.contains("src=\"screenshots/testDriverShortKo_4-1__writeSomethingOnNonExistent-ideo-1.jpg\""));
			
			// check no picture extracted from video is kept
			Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "video").toFile().listFiles().length, 0);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	@Test(groups={"it"})
	public void testReportDoeNotContainStepReferenceForFailedStepWhenVideoDisabled() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			
			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));	
					
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");
			
			// no image reference
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);
			
			// check step beginning state is not present with valid path
			Assert.assertFalse(detailedReportContent1.contains("src=\"screenshots/testDriverShortKo_4-1__writeSomethingOnNonExistent-ideo-1.jpg\""));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check step state is not displayed when reference image does not exist on server
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDoesNotContainStepImageWithoutReference() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			
			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 404, createImageFromResource("tu/ffLogo1.png"));	
		
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");
			
			// reference and current state should be displayed
			Assert.assertFalse(detailedReportContent1.matches(".* class\\=\"step-title\"> _writeSomethingOnNonExistentElement  - .*"
					+ "at com\\.seleniumtests\\.it\\.driver\\.support\\.pages\\.DriverTestPage\\._writeSomethingOnNonExistentElement\\(DriverTestPage\\.java.*?"
					+ "<div class\\=\"row\">"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">"
					+ "<a href\\=\"#\" onclick\\=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class\\=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Valid-reference</div>.*")); // the reference
			
			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
		
	}
	
	/**
	 * Check reference is not get if server recording is disabled
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDoesNotContainStepReferenceWhenRecordingDisabled() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			
			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));	
			
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");
			
			// reference and current state should be displayed
			Assert.assertFalse(detailedReportContent1.matches(".* class\\=\"step-title\"> _writeSomethingOnNonExistentElement  - .*"
					+ "at com\\.seleniumtests\\.it\\.driver\\.support\\.pages\\.DriverTestPage\\._writeSomethingOnNonExistentElement\\(DriverTestPage\\.java.*?"
					+ "<div class\\=\"row\">"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">"
					+ "<a href\\=\"#\" onclick\\=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class\\=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Valid-reference</div>.*")); // the reference
			
			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
		
	}
	
	/**
	 * In case test is OK, no reference image is displayed in report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDoesNotContainReferenceStep() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			
			// simulate the case where a reference exists
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, createImageFromResource("tu/ffLogo1.png"));	
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShort");
			
			// reference and current state should be displayed
			Assert.assertFalse(detailedReportContent1.matches(".*<div class\\=\"row\">"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">"
					+ "<a href\\=\"#\" onclick\\=\"\\$\\('#imagepreview'\\).*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Step beginning state</div>" // the current state
					+ "<div class\\=\"text-center font-weight-lighter\"></div>.*"
					+ "<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">.*"
					+ "<img id.*"
					+ "<div class\\=\"text-center\">Valid-reference</div>.*")); // the reference
			
			// only one extraction of step state is presented (the one for failed step)
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Step beginning state</div>"), 0);
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
		
	}
	
	/**
	 * issue #406
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsVideoCaptureOnRetry() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");
			
			Assert.assertTrue(detailedReportContent1.contains("Video capture: <a href='videoCapture.avi'>file</a>"));	
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
		
	}
	
	/**
	 * Check that when driver starts in beforeMethod, it's possible to have video
	 * issue #406
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsVideoCaptureStartedOnBeforeMethodOnRetry() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForVideoTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverShortKo");
			
			Assert.assertTrue(detailedReportContent1.contains("Video capture: <a href='videoCapture.avi'>file</a>"));	
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
		
	}
	
	/**
	 * Check that only one video capture file is present in result even if several drivers are used
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsOneVideoCaptureWithMultipleDrivers() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testMultipleDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testMultipleDriver");
			
			Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "Video capture: <a href='videoCapture.avi'>file</a>"), 1);	
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
		
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
			System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, "direct");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriver");
			
			Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,], )</li>"));	
			Assert.assertTrue(detailedReportContent1.contains("Network capture 'main' browser: <a href='main-networkCapture.har'>HAR file</a>"));
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "main-networkCapture.har").toFile().exists());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.CAPTURE_NETWORK);
			System.clearProperty(SeleniumTestsContext.WEB_PROXY_TYPE);
		}
		
	}
	
	/**
	 * Check that HAR capture file is present in result when using multiple browsers, one for each browser
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsHarCaptureMultipleBrowsers() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.CAPTURE_NETWORK, "true");
			System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, "direct");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testMultipleDriver"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testMultipleDriver");
			
			Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,], )</li>"));	
			Assert.assertTrue(detailedReportContent1.contains("Network capture 'main' browser: <a href='main-networkCapture.har'>HAR file</a>"));
			Assert.assertTrue(detailedReportContent1.contains("Network capture 'second' browser: <a href='second-networkCapture.har'>HAR file</a>"));
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMultipleDriver", "main-networkCapture.har").toFile().exists());
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMultipleDriver", "second-networkCapture.har").toFile().exists());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.CAPTURE_NETWORK);
			System.clearProperty(SeleniumTestsContext.WEB_PROXY_TYPE);
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
			String detailedReportContent1 = readTestMethodResultFile("testDriver");
			
			Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,], )</li>"));	
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
		String detailedReportContent1 = readTestMethodResultFile("testDriver");
		 
		Assert.assertTrue(detailedReportContent1.contains("<li>sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,], )</li>"));
		Assert.assertTrue(detailedReportContent1.contains("<li>click on ButtonElement Reset, by={By.id: button2} </li>"));
		Assert.assertTrue(detailedReportContent1.contains("<div class=\"text-center\">drv:main: Current Window: Test page</div>"));
		Assert.assertTrue(detailedReportContent1.matches(".*<img id\\=\".*?\" src\\=\"screenshots/testDriver_3-1_openPage_with_args.*<div class\\=\"text-center\">drv:main: Current Window: Test page</div>.*"));
		
		// check that only on reference to 'click' is present for this buttonelement. This means that only the replayed action has been logged, not the ButtonElement.click() one
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "click on"), 1);
		
		// read the 'testDriverNativeActions' test result to see if native actions are also logged (overrideSeleniumNativeAction is true)
		String detailedReportContent2 = readTestMethodResultFile("testDriverNativeActions");
		System.out.println(detailedReportContent2);
		Assert.assertTrue(detailedReportContent2.contains("<li>sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [some text,], )</li>"));
		Assert.assertTrue(detailedReportContent2.contains("<li>click on HtmlElement , by={By.id: button2} </li>"));
		
		// read the 'testDriverNativeActionsWithoutOverride' test result to see if native actions are not logged (overrideSeleniumNativeAction is false)
		String detailedReportContent3 = readTestMethodResultFile("testDriverNativeActionsWithoutOverride");
		
		// logging is not done via HtmlElement
		Assert.assertFalse(detailedReportContent3.contains("<li>sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [some text,], )</li>"));
		Assert.assertFalse(detailedReportContent3.contains("<li>click on HtmlElement , by={By.id: button2} </li>"));
		
		// check that without override, native actions are logged
		Assert.assertTrue(detailedReportContent3.contains("<ul><li>sendKeys on Element located by id: text2 with args: ([some text,], )</li>"));
		Assert.assertTrue(detailedReportContent3.contains("<ul><li>click on Element located by id: button2 </li>"));
		Assert.assertTrue(detailedReportContent3.contains("<ul><li>selectByVisibleText on Select with args: (option1, )</li>"));
				
		// check composite actions. We must have the moveToElement, click and sendKeys actions 
		Assert.assertTrue(detailedReportContent1.contains("<ul><li>moveToElement with args: (TextFieldElement Text, by={By.id: text2}, )</li><li>sendKeys with args: ([composite,], )</li><li>moveToElement with args: (ButtonElement Reset, by={By.id: button2}, )</li><li>click </li>"));
		
		// check PictureElement action is logged
		Assert.assertTrue(detailedReportContent1.contains("<ul><li>clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li>"));
		
		// check that when logging PictureElement action which uses composite actions, those are not logged
		Assert.assertFalse(detailedReportContent1.contains("<ul><li>clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )</li><li>moveToElement with args:"));
		
		// no action is logged when step fails (findElement exception). Ok because logging is done on action, not search 
		
		
		// check that seleniumRobot actions are logged only once when overrideNativeAction is enabled (issue #88)
		String detailedReportContent4 = readTestMethodResultFile("testDriverWithHtmlElementWithoutOverride");
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
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		String detailedReportContent = readTestMethodResultFile("testInError");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box-body\"><ul><div class=\"message-log\">Test is KO with error: class java.lang.AssertionError: error</div>"));
		System.out.println(detailedReportContent);
		// Check exception is logged and filtered
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error\"><div>class java.lang.AssertionError: error</div>"
								+ "<div class=\"stack-element\"></div>"
								+ "<div class=\"stack-element\">at com.seleniumtests.it.stubclasses.StubTestClass.testInError\\(StubTestClass.java:\\d+\\)</div>"
								+ "<div class=\"stack-element\">at java.util.ArrayList.forEach.*?</div>"
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
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithTestValues() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		String detailedReportContent = readTestMethodResultFile("testInError");
		
		// Check error is present is Last test step
		Assert.assertTrue(detailedReportContent.contains("<table class=\"table table-bordered table-condensed\"><tr><th width=\"15%\">Key</th><th width=\"60%\">Message</th><th width=\"25%\">Value</th></tr><tr><td>key</td><td>we found a value of</td><td>10</td></tr></table>"));
	}
	
	@Test(groups={"it"})
	public void testReportDetailsContainsParentConfigurations() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener1"});
		
		String detailedReportContent = readTestMethodResultFile("test1Listener1");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button><span class=\"step-title\"> Pre test step: beforeTestInParent - "), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button><span class=\"step-title\"> Pre test step: beforeTest -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</button><span class=\"step-title\"> Post test step: afterClassInParent - "), 1);
		
	}
		
	
	/**
	 * Check all steps are present in detailed report file. For cucumber, check that method name is the Scenario name, not the "feature" generic method
	 * Test OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberStart() throws Exception {
		
		executeSubCucumberTests("core_3", 1);

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_3/TestReport\\.html'.*?>core_3</a>.*"));
		
		String detailedReportContent = readTestMethodResultFile("core_3");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> write (\\w+) with args: (tutu, )"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is OK</div>"));
	}
	
	/**
	 * Same as above, but for new cucumber annotations
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberStart2() throws Exception {
		
		executeSubCucumberTests("core_7", 1);
		
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_7/TestReport\\.html'.*?>core_7</a>.*"));
		
		String detailedReportContent = readTestMethodResultFile("core_7");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> write2 (\\w+) with args: (tutu, )"));
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is OK</div>"));
	}
	
	/**
	 * issue #362: check that with scenario outline, we have the 2 results
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberScenarioOutline() throws Exception {
		
		executeSubCucumberTests("core_ .*", 1);
		
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core__tata/TestReport\\.html'.*?>core__tata</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core__titi/TestReport\\.html'.*?>core__titi</a>.*"));
		
	}
	
	/**
	 * issue #362: check that with scenario outline, we have the 2 results even if name is the same
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberScenarioOutlineUniqueName() throws Exception {
		
		executeSubCucumberTests("core_unique_name", 1);
		
		String mainReportContent = readSummaryFile();

		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_unique_name-_tata_/TestReport\\.html'.*?>core_unique_name-_tata_</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='core_unique_name-_titi_/TestReport\\.html'.*?>core_unique_name-_titi_</a>.*"));
		
	}
	
	/**
	 * issue #362: check that with scenario outline, we have the 2 results even if name is the same
	 * issue #366: also check with an accent so that we can verify it's removed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberScenarioOutlineUniqueLongName() throws Exception {
		
		executeSubCucumberTests("a very long scenàrio outline name which should not have been created but is there but we should not strip it only display a message saying its much too long", 1);
		
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='a_very_long_scenario_outline_name_which_should_not_have_been_created_but_is_there_but_we_should/TestReport\\.html'.*?>a_very_long_scenario_outline_name_which_should_not_have_been_created_but_is_there_but_we_should</a>.*"));
		
		readTestMethodResultFile("a_very_long_scenario_outline_name_which_should_not_have_been_created_but_is_there_but_we_should");
	}
	
	/**
	 * Check all steps are present in detailed report file. For cucumber, check that method name is the Scenario name, not the "feature" generic method
	 * Test OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCucumberScenarioWithSpecialName() throws Exception {
		
		executeSubCucumberTests("my beautiful scenario ?? ok ??", 1);
		
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='my_beautiful_scenario_.._ok_..-/TestReport\\.html'.*?>my_beautiful_scenario_.._ok_..-</a>.*"));
	
		String detailedReportContent = readTestMethodResultFile("my_beautiful_scenario_.._ok_..-");
		
		// Check each step is recorded in file: 2 test steps + test end + logs
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box success\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> write (\\w+) with args: (tatu, )"));
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
		
		String mainReportContent = readSummaryFile();
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
		
		String detailedReportContent = readTestMethodResultFile("testWithException");
		
		// check step 1 has been encoded
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-log\">Test is KO with error: class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;"));
		
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
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testHtmlCharacterEscapeMultipleExceptions(ITestContext testContext) throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});
		
		String detailedReportContent = readTestMethodResultFile("testWithChainedException");

		// check exception stack trace is encoded with the 2 exceptions
		Assert.assertTrue(detailedReportContent.contains("<div>class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href='http://someurl/link' style='background-color: red;'&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;</div>"));
		Assert.assertTrue(detailedReportContent.contains("Caused by root &lt;error&gt;</div>"));
		
	}
	

	/**
	 * Check that information recorded during test, by calling 'SeleniumRobotTestPlan.addTestInfo(key, value)' are added to summary and test report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testWithTestInfo() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithInfo1", "testWithInfo2", "testAndSubActions"});
		
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
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultiSuitesdReport() throws Exception {
		
		executeMultiSuites(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, new String[] {"testAndSubActions"});
		
		// check content of summary report file
		String mainReportContent = readSummaryFile();
		
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions/TestReport\\.html'.*?>testAndSubActions</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testAndSubActions-1/TestReport\\.html'.*?>testAndSubActions-1</a>.*"));
	}
	

	/**
	 * Check that when an action fails, a warning is displayed in step and logs
	 * This helps in the case the action error is catched
	 */
	@Test(groups={"it"})
	public void testLogActionErrorsAsWarning(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKoWithCatchException"});
			String detailedReportContent = readTestMethodResultFile("testDriverShortKoWithCatchException");
			
			// test all error log is displayed in execution logs
			Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found</div>"));
			Assert.assertTrue(detailedReportContent.contains("<div>at com.seleniumtests.it.driver.support.pages.DriverTestPage._writeSomethingOnNonExistentElementWithCatch"));
			Assert.assertTrue(detailedReportContent.contains("<div>For documentation on this error, please visit: https://www.seleniumhq.org/exceptions/no_such_element.html</div>")); // checks that line not showing thread name are in logs
			
			Assert.assertTrue(detailedReportContent.contains("<div class=\"message-warning\">Warning: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found<br/>" + 
					"For documentation on this error, please visit: https://www.seleniumhq.org/exceptions/no_such_element.html<br/>")); // warning displayed in step
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
	}
	
	/**
	 * Check call to 'isDisplayedRetry' when element is not present should create a failed step with warning, but no exception displayed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNoFailedStepForIsDisplayedRetry() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.REPLAY_TIME_OUT, "3");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverIsDisplayedRetry"});
			
			// read 'testDriver' report. This contains calls to HtmlElement actions
			String detailedReportContent1 = readTestMethodResultFile("testDriverIsDisplayedRetry");
			Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-warning\">Warning: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found")); // warning
			Assert.assertFalse(detailedReportContent1.contains("<div class=\"message-error\">class org.openqa.selenium.NoSuchElementException: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found<br/>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.REPLAY_TIME_OUT);
		}
	}
	
	
	@Test(groups={"it"})
	public void testNoDescription(ITestContext testContext) throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[] {"testNoDescription"});
		
		String summaryReport = readSummaryFile();
		Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"no description available\""));
		
		String detailedReportContent = readTestMethodResultFile("testNoDescription");
		Assert.assertFalse(detailedReportContent.contains("<th width=\"200px\">Description</th>"));
	}
	
	/**
	 * Check that if a user param is set from command line, description can use it
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDescriptionWithUserParam(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty("url", "http://mysite.com");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[] {"testWithDescription"});
			
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
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDescriptionWithParamCreatedInTest(ITestContext testContext) throws Exception {
	
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[] {"testWithParamCreatedInTest"});
		
		String summaryReport = readSummaryFile();
		Assert.assertTrue(summaryReport.contains("info=\"ok\" data-toggle=\"tooltip\" title=\"a test on Bob account account-12345\""));
		
		String detailedReportContent = readTestMethodResultFile("testWithParamCreatedInTest");
		Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>a test on Bob account account-12345</td>"));
		
	}
	
	/**
	 * Test interpolation of method parameters when they are referenced as 'arg0', 'arg1', ..., 'argN' in description
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDescriptionWithDataProvider(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[] {"testDataProvider"});
		

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
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDescriptionWithLineBreak(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty("url", "http://mysite.com");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[] {"testWithLineBreaksInDescription"});
			
			String summaryReport = readSummaryFile();
			Assert.assertTrue(summaryReport.contains("info=\"ko\" data-toggle=\"tooltip\" title=\"a test with param http://mysite.comand line breaks\""));
			
			String detailedReportContent = readTestMethodResultFile("testWithLineBreaksInDescription");
			Assert.assertTrue(detailedReportContent.contains("<th width=\"200px\">Description</th><td>a test with param http://mysite.com<br/>and line breaks</td>"));
		} finally {
			System.clearProperty("url");
		}	
	}
	
	/**
	 * Check error cause is displayed in report
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testStepAnnotationWithError() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[] {"testCauseWithErrorAndDetails"});

		String detailedReportContent = readTestMethodResultFile("testCauseWithErrorAndDetails");
		
		// Only failing step contains the information message
		Assert.assertTrue(detailedReportContent.matches(".*<i class\\=\"fas fa-plus\"></i></button><span class=\"step-title\"> add - 0\\.\\d+ secs</span></div><div class\\=\"box-body\"><div class\\=\"step-info\"><i class\\=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: Check your scripts</span></div>.*"));
		Assert.assertFalse(detailedReportContent.matches(".*<i class\\=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end - 0\\.\\d+ secs</span></div><div class\\=\"box-body\"><div class\\=\"step-info\"><i class\\=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: Check your scripts</span></div>.*"));
	}
	
	@Test(groups={"it"})
	public void testStepAnnotationWithErrorNoDetails() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[] {"testCauseWithErrorNoDetails"});
		
		String detailedReportContent = readTestMethodResultFile("testCauseWithErrorNoDetails");
		
		// Only failing step contains the information message
		Assert.assertTrue(detailedReportContent.matches(".*<i class\\=\"fas fa-plus\"></i></button><span class=\"step-title\"> add - 0\\.\\d+ secs</span></div><div class\\=\"box-body\"><div class\\=\"step-info\"><i class\\=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: </span></div>.*"));
		Assert.assertFalse(detailedReportContent.matches(".*<i class\\=\"fas fa-plus\"></i></button><span class=\"step-title\"> Test end - 0\\.\\d+ secs</span></div><div class\\=\"box-body\"><div class\\=\"step-info\"><i class\\=\"fas fa-info-circle\"></i><span>Possibly caused by REGRESSION: </span></div>.*"));
	}
	
	@Test(groups={"it"})
	public void testStepAnnotationNoErrors() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[] {"testCauseNoError"});
		
		String detailedReportContent = readTestMethodResultFile("testCauseNoError");
		
		// add step is not in error, error cause is not displayed
		Assert.assertFalse(detailedReportContent.contains("<div class=\"step-info\"><i class=\"fas fa-info-circle\"></i>"));
		
	}
	
	@Test(groups={"it"})
	public void testStepAnnotationNoErrorCause() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForStepsAnnotation"}, ParallelMode.METHODS, new String[] {"testNoCauseAndError"});
		
		String detailedReportContent = readTestMethodResultFile("testNoCauseAndError");
		
		// add step is not in error, error cause is not displayed
		Assert.assertFalse(detailedReportContent.contains("<div class=\"step-info\"><i class=\"fas fa-info-circle\"></i>"));
		
	}
}
