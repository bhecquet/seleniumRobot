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
package com.seleniumtests.reporter.reporters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.reporter.logger.FileContent;
import com.seleniumtests.reporter.logger.GenericFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.json.JSONObject;
import org.openqa.selenium.Rectangle;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGContextUtils;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.ExceptionUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumRobotServerTestRecorder extends CommonReporter implements IReporter {

	public SeleniumRobotSnapshotServerConnector getServerConnector() {
		return SeleniumRobotSnapshotServerConnector.getInstance();
	}
	
	/**
	 * Generate result for a single test method
	 * @param ve			velocity engine used to generate file
	 * @param testResult	result for this test method
	 */
	public JSONObject generateExecutionLogs(final ITestResult testResult) {
		
		JSONObject executionLogs = new JSONObject();
		executionLogs.put("logs", SeleniumRobotLogger.getTestLogs(TestNGResultUtils.getUniqueTestName(testResult)));
		
		// exception handling
		StringBuilder stackString = new StringBuilder();
		if (testResult.getThrowable() != null) {
			ExceptionUtility.generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString, "html");
		}
		
		executionLogs.put("stacktrace", stackString.toString());

		return executionLogs;
	}
	

	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport, boolean finalGeneration) {
		ITestContext testCtx = SeleniumTestsContextManager.getGlobalContext().getTestNGContext();
		
		if (testCtx == null) {
			logger.error("Looks like your class does not extend from SeleniumTestPlan!");
			return;
		}
		
		// issue #81: use global context because these parameters are known from there (thread context is too narrow)
		if (!Boolean.TRUE.equals(SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerActive())
				|| !SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerRecordResults() 
				&& !SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot()) {
			return;
		}
		
		// check that seleniumRobot server is alive
		SeleniumRobotSnapshotServerConnector serverConnector = getServerConnector();
		if (!serverConnector.getActive()) {
			logger.info("selenium-robot-server not found or down");
			return;
		} else {
			try {
				// do not create application / version / environment from script, they should already be present or created by user to avoid fill database with wrong data
				// create session only if it has not been before
				for (ITestContext testContext: resultSet.keySet()) {
					recordTestSession(testContext);
				}
			} catch (SeleniumRobotServerException | ConfigurationException e) {
				logger.error("Error contacting selenium robot server", e);
				return;
			}
		}
		
		try {
			recordResults(serverConnector, resultSet);
		} catch (SeleniumRobotServerException | ConfigurationException e) {
			logger.error("Error recording result on selenium robot server", e);
		}
	}
	
	public void recordTestSession(ITestContext testContext) {
		SeleniumRobotSnapshotServerConnector serverConnector = getServerConnector();
		if (TestNGContextUtils.getTestSessionCreated(testContext) == null) {

			String browserOrApp;
			if (SeleniumTestsContextManager.isAppTest()) {
				String appPath = SeleniumTestsContextManager.getGlobalContext().getApp() == null ? "noApp": SeleniumTestsContextManager.getGlobalContext().getApp();
				browserOrApp = "APP:" + new File(appPath).getName();
			} else {
				BrowserType browser = SeleniumTestsContextManager.getGlobalContext().getBrowser();
				browser = browser == null ? BrowserType.NONE : browser;
				browserOrApp = "BROWSER:" + browser;
			}

			Integer sessionId = serverConnector.createSession(testContext.getName(), browserOrApp, SeleniumTestsContextManager.getThreadContext().getStartedBy());
			TestNGContextUtils.setTestSessionCreated(testContext, sessionId);
		}
	}


	
	private void recordResults(SeleniumRobotSnapshotServerConnector serverConnector, Map<ITestContext, Set<ITestResult>> resultSet) {
		
		for (Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {

			List<ITestResult> methodResults = entry.getValue().stream()
						.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
						.collect(Collectors.toList());
			
			// test case in seleniumRobot naming
			for (ITestResult testResult: methodResults) {
				
				// do not record this result twice if it's already recorded
				if (TestNGResultUtils.isSeleniumServerReportCreated(testResult) 
					// NoMoreRetry is set to false when test is being retried => we do not want to record a temp result, only the last one
					|| (TestNGResultUtils.getNoMoreRetry(testResult) != null && !TestNGResultUtils.getNoMoreRetry(testResult))) {
					continue;
				}
				
				// issue #81: recreate test context from this context (due to multithreading, this context may be null if parallel testing is used)
				SeleniumTestsContextManager.setThreadContextFromTestResult(entry.getKey(), testResult);
				
				// skipped tests has never been executed and so attribute (set in TestListener) has not been applied
				String testName = getTestCaseName(testResult);
				
				// get sessionId from context
				Integer sessionId = TestNGContextUtils.getTestSessionCreated(entry.getKey());
				
				// record test case
				Integer testCaseId = serverConnector.createTestCase(testName);
				String gridNode = getGridNode();
				Integer testCaseInSessionId = serverConnector.createTestCaseInSession(sessionId,
						testCaseId,
						getVisualTestName(testResult),
						TestNGResultUtils.getTestStatusString(testResult),
						gridNode,
						TestNGResultUtils.getTestDescription(testResult));
				logger.info(String.format("Result for '%s' will be visible at: %s/snapshot/testResults/result/%d/", testName, serverConnector.getUrl(), testCaseInSessionId));
				serverConnector.addLogsToTestCaseInSession(testCaseInSessionId, generateExecutionLogs(testResult).toString());

				addPreviousExecutionResults(testResult);
				List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
				if (testSteps == null) {
					continue;
				}
				
				recordSteps(serverConnector, testCaseInSessionId, testSteps, testResult);
				recordLogs(serverConnector, testCaseInSessionId, testResult);
				recordTestInfos(serverConnector, testCaseInSessionId, testResult);
				
				logger.info(String.format("Snapshots has been recorded with TestCaseSessionId: %d", testCaseInSessionId));
				TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, testCaseInSessionId);
				TestNGResultUtils.setSeleniumServerReportCreated(testResult, true);
			}
		}
	}

	private static String getGridNode() {
		String gridNode = SeleniumTestsContextManager.getThreadContext().getRunMode().toString();
		if (SeleniumTestsContextManager.getThreadContext().getRunMode().equals(DriverMode.GRID) && SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector() != null) { // in case test is skipped, connector is null
			gridNode = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector().getNodeHost();
		}
		return gridNode;
	}

	/**
	 * Record test steps to server
	 * @param serverConnector
	 * @param testCaseInSessionId
	 * @param testSteps
	 */
	private void recordSteps(SeleniumRobotSnapshotServerConnector serverConnector, Integer testCaseInSessionId, List<TestStep> testSteps, ITestResult testResult) {
		for (TestStep testStep: testSteps) {
			
			logger.info(String.format("Recording step %s on server", testStep.getName()));
			
			// record test step
			Integer testStepId = serverConnector.createTestStep(testStep.getName(), testCaseInSessionId);
			
			Integer stepResultId = serverConnector.recordStepResult(testStep, testCaseInSessionId, testStepId);
			testStep.setStepResultId(stepResultId);
			
			// sends all snapshots that are flagged as comparable, when user request them to be compared
			for (Snapshot snapshot: new ArrayList<>(testStep.getSnapshots(true))) {
				
				if (snapshot.getCheckSnapshot().recordSnapshotOnServerForComparison() && SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot()) {
					if (snapshot.getName() == null || snapshot.getName().isEmpty()) {
						logger.warn("Snapshot hasn't any name, it won't be sent to server");
						continue;
					} 
					
					recordSnapshot(serverConnector, stepResultId, snapshot);
					
				// record reference image on server if step is successful
				} else if (snapshot.getCheckSnapshot().recordSnapshotOnServerForReference() && SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerRecordResults()) {
					recordReference(serverConnector, testResult, testStep, stepResultId, snapshot);
				}
			}
			
			// record all attachments (after references so that video files are not included if step is OK)
			recordAllAttachments(serverConnector, stepResultId, testStep);

			// update logs on server, with id of attachments
			String jsonStep = testStep.toJson().toString();
			serverConnector.updateStepResult(jsonStep, stepResultId);
		}
	}

	/**
	 * Add a step, so that previous execution results are recorded on server
	 * @param testResult
	 */
	private static void addPreviousExecutionResults(ITestResult testResult) {
		TestStep testStep = new TestStep("No previous execution results, you can enable it via parameter '-DkeepAllResults=true'");
		testStep.setFailed(false);
		List<File> executionResults = new ArrayList<>();
		if (SeleniumTestsContextManager.getGlobalContext().getKeepAllResults()) {
			testStep.setName("Previous execution results");
			executionResults = FileUtils.listFiles(new File(TestNGResultUtils.getSeleniumRobotTestContext(testResult).getOutputDirectory()),
							FileFilterUtils.suffixFileFilter(".zip"), null).stream()
					.collect(Collectors.toList());
			logger.info("recording previous execution results");

			for (File executionResultFile: executionResults) {
				try {
					testStep.addFile(new GenericFile(executionResultFile, executionResultFile.getName().replace(".zip", ""), false));
				} catch (IOException e) {
					logger.warn("Could not add previous execution result: " + e.getMessage());
				}
			}
		}
		TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps().add(testStep);

	}

	/**
	 * Record logs to server
	 * @param serverConnector
	 * @param testCaseInSessionId
	 */
	private void recordLogs(SeleniumRobotSnapshotServerConnector serverConnector, Integer testCaseInSessionId, ITestResult testResult) {
		try {
			serverConnector.uploadLogs(SeleniumRobotLogger.getTestLogsFile(getTestName(testResult)), testCaseInSessionId);
		} catch (SeleniumRobotServerException | ConfigurationException e) {
			logger.error("Error uploading file: " + e.getMessage(), e);
		}
	}

	private void recordTestInfos(SeleniumRobotSnapshotServerConnector serverConnector, Integer testCaseInSessionId, ITestResult testResult) {
		try {
			serverConnector.recordTestInfo(TestNGResultUtils.getTestInfo(testResult), testCaseInSessionId);
		} catch (SeleniumRobotServerException | ConfigurationException e) {
			logger.error("Error sending test infos: " + e.getMessage(), e);
		}
	}

	/**
	 * Records all attachments on server, except the reference pictures and pictures for comparison, as they are recorded by other means
	 * @param serverConnector
	 * @param stepResultId
	 * @param testStep
	 */
	private void recordAllAttachments(SeleniumRobotSnapshotServerConnector serverConnector, Integer stepResultId, TestStep testStep) {

		List<FileContent> attachments;
		// in case snapshot comparison is not requested, we still want captured pictures to be displayed in report
		if (SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot()) {
			attachments = testStep.getAllAttachments(false, SnapshotCheckType.NONE, SnapshotCheckType.REFERENCE_ONLY);
		} else {
			attachments = testStep.getAllAttachments(false, SnapshotCheckType.NONE, SnapshotCheckType.REFERENCE_ONLY, SnapshotCheckType.FULL);
		}

		// upload all files to server
		for (FileContent file: attachments) {
			try {
				Integer fileId = serverConnector.uploadFile(file.getFile(), stepResultId);
				file.setId(fileId);

			} catch (SeleniumRobotServerException | ConfigurationException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * record a snpashot on server, for snapshot comparison
	 * @param serverConnector
	 * @param stepResultId
	 * @param snapshot
	 */
	private void recordSnapshot(SeleniumRobotSnapshotServerConnector serverConnector, Integer stepResultId, Snapshot snapshot) {
		try {
			serverConnector.createSnapshot(snapshot, stepResultId, snapshot.getCheckSnapshot().getExcludeElementsRect());
			logger.info("Check snapshot created");
		} catch (SeleniumRobotServerException e) {
			logger.error("Could not create snapshot on server", e);
		}		
	}
	/**
	 * record a step reference snapshot, when step is OK
	 * @param serverConnector
	 * @param testResult
	 * @param testStep
	 * @param stepResultId
	 * @param snapshot
	 */
	private void recordReference(SeleniumRobotSnapshotServerConnector serverConnector, ITestResult testResult,
			TestStep testStep, Integer stepResultId, Snapshot snapshot) {
		if (Boolean.FALSE.equals(testStep.getFailed())) {
			try {
				serverConnector.createStepReferenceSnapshot(snapshot, stepResultId);
				logger.info("Step OK: reference created");
			} catch (SeleniumRobotServerException e) {
				logger.error("Could not create reference snapshot on server", e);
			}
			
			// remove this snapshot, extracted from video as it won't be used anymore
			testStep.removeSnapshot(snapshot);

		} else {
			try {
				// move snapshot to "screenshots" directory as "video" directory will be removed at the end of the test
				snapshot.relocate(TestNGResultUtils.getSeleniumRobotTestContext(testResult).getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR + "/" + snapshot.getScreenshot().getImageName());
				File referenceSnapshot = serverConnector.getReferenceSnapshot(stepResultId);
				
				if (referenceSnapshot != null) {
					logger.info("Step KO: reference snapshot got from server");
					testStep.addSnapshot(new Snapshot(new ScreenShot(referenceSnapshot), "Valid-reference", SnapshotCheckType.NONE), 0, null);
					snapshot.setDisplayInReport(true); // change flag so that it's displayed in report (by default reference image extracted from video are not displayed)
				}
			} catch (SeleniumRobotServerException e) {
				logger.error("Could not get reference snapshot from server", e);
			} catch (IOException e) {
				logger.error("Could not copy reference snapshot", e);
			}
		}
	}


}
