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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumRobotServerTestRecorder extends CommonReporter implements IReporter {

	public SeleniumRobotSnapshotServerConnector getServerConnector() {
		return new SeleniumRobotSnapshotServerConnector(
				SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerActive(),
				SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerUrl()
				);
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
			generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString, "html");
		}
		
		executionLogs.put("stacktrace", stackString.toString());

		return executionLogs;
	}
	

	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport) {
		ITestContext testCtx = SeleniumTestsContextManager.getGlobalContext().getTestNGContext();
		
		if (testCtx == null) {
			logger.error("Looks like your class does not extend from SeleniumTestPlan!");
			return;
		}
		
		// issue #81: use global context because these parameters are known from there (thread context is too narrow)
		if (!SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerActive() 
				|| !SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerRecordResults() 
				&& !SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerCompareSnapshot()) {
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
				serverConnector.createSession();
			} catch (SeleniumRobotServerException | ConfigurationException e) {
				logger.error("Error contacting selenium robot serveur", e);
				return;
			}
		}
		
		try {
			recordResults(serverConnector, resultSet);
		} catch (SeleniumRobotServerException | ConfigurationException e) {
			logger.error("Error contacting selenium robot serveur", e);
			return;
		}
	}


	
	private void recordResults(SeleniumRobotSnapshotServerConnector serverConnector, Map<ITestContext, Set<ITestResult>> resultSet) {
		
		for (Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {
			List<ITestResult> methodResults = new ArrayList<>();

			methodResults = entry.getValue().stream()
						.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
						.collect(Collectors.toList());
			
			// test case in seleniumRobot naming
			for (ITestResult testResult: methodResults) {
				
				// do not record this result twice if it's already recorded
				if (TestNGResultUtils.isSeleniumServerReportCreated(testResult) 
					// NoMoreRetry is set to false when test is being retried
					|| (TestNGResultUtils.getNoMoreRetry(testResult) != null && TestNGResultUtils.getNoMoreRetry(testResult) == false)) {
					continue;
				}
				
				// issue #81: recreate test context from this context (due to multithreading, this context may be null if parallel testing is used)
				SeleniumTestsContext testContext = SeleniumTestsContextManager.setThreadContextFromTestResult(entry.getKey(), getTestName(testResult), getClassName(testResult), testResult);
				
				// skipped tests has never been executed and so attribute (set in TestListener) has not been applied
				String testName = getTestName(testResult);
				
				// record test case
				serverConnector.createTestCase(testName);
				serverConnector.createTestCaseInSession();
				serverConnector.addLogsToTestCaseInSession(generateExecutionLogs(testResult).toString());
				
				List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
				if (testSteps == null) {
					continue;
				}
				
				synchronized (testSteps) {
					for (TestStep testStep: testSteps) {
						
						// record test step
						serverConnector.createTestStep(testStep.getName());
						String stepLogs = testStep.toJson().toString();
						
						serverConnector.recordStepResult(!testStep.getFailed(), stepLogs, testStep.getDuration());
						
						// sends all snapshots that are flagged as comparable
						for (Snapshot snapshot: testStep.getSnapshots()) {
							
							if (snapshot.isCheckSnapshot() == SnapshotCheckType.TRUE) {
								if (snapshot.getName() == null || snapshot.getName().isEmpty()) {
									logger.warn("Snapshot hasn't any name, it won't be sent to server");
									continue;
								}
								
								serverConnector.createSnapshot(Paths.get(testContext.getOutputDirectory(), 
										snapshot.getScreenshot().getImagePath()
										).toFile());
							}
						}
					}
				}
				
				TestNGResultUtils.setSeleniumServerReportCreated(testResult, true);
			}
		}
	}


}
