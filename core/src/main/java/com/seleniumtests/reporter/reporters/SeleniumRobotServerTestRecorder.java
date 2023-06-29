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

import org.apache.commons.io.FileUtils;
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
		executionLogs.put("logs", SeleniumRobotLogger.getTestLogs().get(TestNGResultUtils.getUniqueTestName(testResult)));
		
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
			Integer sessionId = serverConnector.createSession(testContext.getName());
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
				Integer testCaseInSessionId = serverConnector.createTestCaseInSession(sessionId, testCaseId, getVisualTestName(testResult));
				serverConnector.addLogsToTestCaseInSession(testCaseInSessionId, generateExecutionLogs(testResult).toString());
				
				List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
				if (testSteps == null) {
					continue;
				}
				
				recordSteps(serverConnector, sessionId, testCaseInSessionId, testSteps, testResult);
				
				logger.info(String.format("Snapshots has been recorded with TestCaseSessionId: %d", testCaseInSessionId));
				TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, testCaseInSessionId);
				TestNGResultUtils.setSeleniumServerReportCreated(testResult, true);
			}
		}
	}

	/**
	 * Record test steps to server
	 * @param serverConnector
	 * @param sessionId
	 * @param testCaseInSessionId
	 * @param testSteps
	 */
	private void recordSteps(SeleniumRobotSnapshotServerConnector serverConnector, Integer sessionId, Integer testCaseInSessionId, List<TestStep> testSteps, ITestResult testResult) {
		for (TestStep testStep: testSteps) {
			
			logger.info(String.format("Recording step %s on server", testStep.getName()));
			
			// record test step
			Integer testStepId = serverConnector.createTestStep(testStep.getName(), testCaseInSessionId);
			String stepLogs = testStep.toJson().toString();
			
			Integer stepResultId = serverConnector.recordStepResult(!testStep.getFailed(), stepLogs, testStep.getDuration(), sessionId, testCaseInSessionId, testStepId);
			testStep.setStepResultId(stepResultId);
			
			// sends all snapshots that are flagged as comparable
			for (Snapshot snapshot: new ArrayList<>(testStep.getSnapshots(true))) {
				
				if (snapshot.getCheckSnapshot().recordSnapshotOnServerForComparison() && SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot()) {
					if (snapshot.getName() == null || snapshot.getName().isEmpty()) {
						logger.warn("Snapshot hasn't any name, it won't be sent to server");
						continue;
					} 
					
					recordSnapshot(serverConnector, sessionId, testCaseInSessionId, stepResultId, snapshot);
					
				// record reference image on server if step is successful
				} else if (snapshot.getCheckSnapshot().recordSnapshotOnServerForReference() && SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerRecordResults()) {
					recordReference(serverConnector, testResult, testStep, stepResultId, snapshot);
				}
			}
		}
	}

	/**
	 * record a snpashot on server, for snapshot comparison
	 * @param serverConnector
	 * @param sessionId
	 * @param testCaseInSessionId
	 * @param stepResultId
	 * @param snapshot
	 */
	private void recordSnapshot(SeleniumRobotSnapshotServerConnector serverConnector, Integer sessionId,
			Integer testCaseInSessionId, Integer stepResultId, Snapshot snapshot) {
		try {
			serverConnector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, snapshot.getCheckSnapshot().getExcludeElementsRect());
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
			testStep.getSnapshots().remove(snapshot);

		} else {
			try {
				// move snapshot to "screenshots" directory as "video" directory will be removed at the end of the test
				snapshot.relocate(TestNGResultUtils.getSeleniumRobotTestContext(testResult).getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR + "/" + snapshot.getScreenshot().getImageName());
				File referenceSnapshot = serverConnector.getReferenceSnapshot(stepResultId);
				
				if (referenceSnapshot != null) {
					logger.info("Step KO: reference snapshot got from server");
					Path newPath = Paths.get(TestNGResultUtils.getSeleniumRobotTestContext(testResult).getScreenshotOutputDirectory(), referenceSnapshot.getName()).toAbsolutePath(); 
					FileUtils.moveFile(referenceSnapshot, newPath.toFile());
					testStep.addSnapshot(new Snapshot(new ScreenShot(newPath.getParent().getParent().relativize(newPath).toString()), "Valid-reference", SnapshotCheckType.FALSE), 0, null);
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
