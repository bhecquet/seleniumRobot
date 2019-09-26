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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.reporter.logger.TestLogging;
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

	/**
	 * Generate all test reports
	 */
	@Override
	public void generateReport(final List<XmlSuite> xml, final List<ISuite> suites, final String outdir) {
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
				serverConnector.createApplication();
				serverConnector.createVersion();
				serverConnector.createEnvironment();
				serverConnector.createSession();
			} catch (SeleniumRobotServerException | ConfigurationException e) {
				logger.error("Error contacting selenium robot serveur", e);
				return;
			}
		}
		
		try {
			for (ISuite suite : suites) {
				Map<String, ISuiteResult> tests = suite.getResults();
				
				recordSuiteResults(serverConnector, tests);
			}
		} catch (SeleniumRobotServerException | ConfigurationException e) {
			logger.error("Error contacting selenium robot serveur", e);
			return;
		}
	
	}
	
	private void recordSuiteResults(SeleniumRobotSnapshotServerConnector serverConnector, Map<String, ISuiteResult> tests) {
	
		for (ISuiteResult r : tests.values()) {
			ITestContext context = r.getTestContext();
			
			Collection<ITestResult> methodResults = new ArrayList<>();
			methodResults.addAll(context.getFailedTests().getAllResults());
			methodResults.addAll(context.getPassedTests().getAllResults());
			methodResults.addAll(context.getSkippedTests().getAllResults());
			
			methodResults = methodResults.stream()
					.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
					.collect(Collectors.toList());
			
			// test case in seleniumRobot naming
			for (ITestResult testResult: methodResults) {
				
				// issue #81: recreate test context from this context (due to multithreading, this context may be null if parallel testing is used)
				SeleniumTestsContextManager.setThreadContextFromTestResult(context, getTestName(testResult), getClassName(testResult), testResult);
				
				// skipped tests has never been executed and so attribute (set in TestListener) has not been applied
				String testName = getTestName(testResult);
				
				// record test case
				serverConnector.createTestCase(testName);
				serverConnector.createTestCaseInSession();
				serverConnector.addLogsToTestCaseInSession(generateExecutionLogs(testResult).toString());
				
				List<TestStep> testSteps = TestLogging.getTestsSteps().get(testResult);
				if (testSteps == null) {
					continue;
				}
				
				for (TestStep testStep: testSteps) {
					
					// record test step
					serverConnector.createTestStep(testStep.getName());
					String stepLogs = testStep.toJson().toString();
					
					serverConnector.recordStepResult(!testStep.getFailed(), stepLogs, testStep.getDuration());
					
					if (!testStep.getSnapshots().isEmpty()) {
						serverConnector.createSnapshot(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), 
																testStep.getSnapshots().get(0).getScreenshot().getImagePath()
																).toFile());
					}
				}
			}
		}
	}


}
