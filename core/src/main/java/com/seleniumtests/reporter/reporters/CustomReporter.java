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

import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.seleniumtests.driver.DriverMode;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.StatisticsStorage;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.ExceptionUtility;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class CustomReporter extends CommonReporter implements IReporter {
	
	private static final String FIELD_TOTAL = "total";
	private static final String FIELD_SKIP = "skip";
	private static final String FIELD_FAIL = "fail";
	private static final String FIELD_PASS = "pass";
	private List<String> generatedFiles;


	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}


	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport,	boolean finalGeneration) {
		generatedFiles = new ArrayList<>();
		
		Map<String, Integer> consolidatedResults = new HashMap<>();
		consolidatedResults.put(FIELD_PASS, 0);
		consolidatedResults.put(FIELD_FAIL, 0);
		consolidatedResults.put(FIELD_SKIP, 0);
		consolidatedResults.put(FIELD_TOTAL, 0);
		
		for (Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {
			for (ITestResult testResult: entry.getValue()) {
				if (testResult.isSuccess()) {
					consolidatedResults.put(FIELD_PASS, consolidatedResults.get(FIELD_PASS) + 1);
				} else if (testResult.getStatus() == ITestResult.FAILURE) {
					consolidatedResults.put(FIELD_FAIL, consolidatedResults.get(FIELD_FAIL) + 1);
				} else if (testResult.getStatus() == ITestResult.SKIP) {
					consolidatedResults.put(FIELD_SKIP, consolidatedResults.get(FIELD_SKIP) + 1);
				}
				consolidatedResults.put(FIELD_TOTAL, consolidatedResults.get(FIELD_TOTAL) + 1);
				
				// done in case it was null (issue #81)
				SeleniumTestsContext testContext = SeleniumTestsContextManager.setThreadContextFromTestResult(entry.getKey(), testResult);
				

				if (!TestNGResultUtils.isCustomReportCreated(testResult)) {
					for (ReportInfo reportInfo: testContext.getCustomTestReports()) {
						generateTestReport(testResult, reportInfo);
					}
				}
			}
		}
		
		for (ReportInfo reportInfo: SeleniumTestsContextManager.getGlobalContext().getCustomSummaryReports()) {
			generateSummaryReport(consolidatedResults, reportInfo);
		}
		
	}
	
	/**
	 * Generates report for a single test
	 * @param testResult
	 */
	private void generateTestReport(ITestResult testResult, ReportInfo reportInfo) {
		try {

			VelocityEngine ve = createVelocityEngine();
			
			Template t = ve.getTemplate(reportInfo.getTemplatePath());
			VelocityContext context = new VelocityContext();
			
			String reportFormat = reportInfo.getExtension().substring(1);
			Long testDuration = 0L;
			Integer errors = 0;
			Integer failures = 0;
			String failedStep = "";
			List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
			List<TestStep> newTestSteps = new ArrayList<>();
			if (testSteps != null) {
				for (TestStep step: testSteps) {
					testDuration += step.getDuration();
					if (Boolean.TRUE.equals(step.getFailed()) && !step.isTestEndStep()) {
						failedStep = step.getName();
						if (step.getActionException() != null && step.getActionException() instanceof AssertionError) {
							failures++;
						} else {
							errors++;
						}
					}
					
					// encode each step
					if ("xml".equalsIgnoreCase(reportFormat.toLowerCase()) 
							|| "json".equalsIgnoreCase(reportFormat.toLowerCase())
							|| "html".equalsIgnoreCase(reportFormat.toLowerCase())
							|| "csv".equalsIgnoreCase(reportFormat.toLowerCase())
							) {
						newTestSteps.add(step.encode(reportFormat.toLowerCase()));
					} else {
						newTestSteps.add(step);
					}
				}
			}
			
			// issue #227: number of errors set to -1 to distinguish skipped tests
			if (testResult.getStatus() == ITestResult.SKIP) {
				errors = -1;
			}
			
			// issue #228: if test is OK, set number of errors to 0 even if one step is failed
			// because test result published by this report must conform to other results
			else if (testResult.getStatus() == ITestResult.SUCCESS) {
				errors = 0;
			}
			
			// #573: if test is KO, be sure errors is > 0
			else if (testResult.getStatus() == ITestResult.FAILURE && errors + failures < 1) {
				errors = 1;
			}
			
			List<String> stack = null;
			if (testResult.getThrowable() != null) {
				StringBuilder stackString = new StringBuilder();
				ExceptionUtility.generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString, reportFormat.toLowerCase());
				stack = Arrays.asList(stackString.toString().split("\n"));
			}
			
			SeleniumTestsContext seleniumTestsContext = TestNGResultUtils.getSeleniumRobotTestContext(testResult);
	
			// if adding some information, don't forget to add them to velocity model for integration tests
			if (seleniumTestsContext.getRunMode().equals(DriverMode.GRID)) {
				// in case test is skipped, connector is null
				context.put("gridnode", seleniumTestsContext.getSeleniumGridConnector() != null ? seleniumTestsContext.getSeleniumGridConnector().getNodeHost(): "N/A");

			} else {
				context.put("gridnode", seleniumTestsContext.getRunMode());
			}
			context.put("errors", errors);
			context.put("newline", "\n");
			context.put("failures", failures);
			context.put("hostname", testResult.getHost() == null ? "": testResult.getHost());
			context.put("suiteName", StringUtility.encodeString(TestNGResultUtils.getVisualTestName(testResult), reportFormat.toLowerCase()));
			context.put("className", testResult.getTestClass().getName());
			context.put("tests", newTestSteps == null ? 0: newTestSteps.size());
			context.put("duration", testDuration / 1000.0);
			context.put("time", newTestSteps == null || newTestSteps.size() == 0 ? testResult.getStartMillis(): newTestSteps.get(0).getStartDate().toInstant().toEpochMilli());
			context.put("startDate", newTestSteps == null || newTestSteps.size() == 0 ? new Date(testResult.getStartMillis()): newTestSteps.get(0).getStartDate());
			context.put("testSteps", newTestSteps);	
			context.put("retries", TestNGResultUtils.getRetry(testResult) == null ? 0: TestNGResultUtils.getRetry(testResult));	
			context.put("browser", seleniumTestsContext.getBrowser());	
			context.put("mobileApp", StringUtility.encodeString(seleniumTestsContext.getApp(), reportFormat.toLowerCase()));	
			context.put("device", StringUtility.encodeString(seleniumTestsContext.getDeviceName() == null ? "": seleniumTestsContext.getDeviceName(), reportFormat.toLowerCase()));
			if (seleniumTestsContext.getTestType().isMobile()) {
				context.put("platform", seleniumTestsContext.getPlatform() + " " + seleniumTestsContext.getMobilePlatformVersion());
			} else {
				context.put("platform", seleniumTestsContext.getPlatform());
			}
			context.put("version", SeleniumTestsContextManager.getApplicationVersion());	
			context.put("coreVersion", SeleniumTestsContextManager.getCoreVersion());	
			context.put("parameters", seleniumTestsContext.getContextDataMap());
			context.put("stacktrace", stack);
			context.put("failedStep", StringUtility.encodeString(failedStep, reportFormat.toLowerCase()));
			String logs = SeleniumRobotLogger.getTestLogs(getTestName(testResult));
			logger.info("test name: " + getTestName(testResult));
			getTestLogs(context, reportFormat, logs);

			context.put("testInfos", TestNGResultUtils.getTestInfoEncoded(testResult, reportFormat));
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			
			String fileName = reportInfo.prefix 
								+ "-result" 
								+ reportInfo.extension;
			
			generateReport(Paths.get(seleniumTestsContext.getOutputDirectory(), fileName).toFile(), writer.toString());
			
			generatedFiles.add(fileName);
			TestNGResultUtils.setCustomReportCreated(testResult, true);
		} catch (Exception e) {
			logger.error(String.format("Error generating test result %s: %s", TestNGResultUtils.getUniqueTestName(testResult), e.getMessage()));
		}
	}


	private void getTestLogs(VelocityContext context, String reportFormat, String logs) {
		try {
			context.put("logs", logs == null || logs.isEmpty() ? "Test skipped": StringUtility.encodeString(logs, reportFormat.toLowerCase()));
		} catch (CustomSeleniumTestsException e) {
			context.put("logs", logs);
		}
	}


	private VelocityEngine createVelocityEngine() {
		VelocityEngine ve;
		try {
			ve = initVelocityEngine();
		} catch (Exception e) {
			throw new ScenarioException("Error generating test results");
		}
		return ve;
	}
	
	private void generateSummaryReport(Map<String, Integer> consolidatedResults, ReportInfo reportInfo) {
		
		try {
			VelocityEngine ve = createVelocityEngine();
			
			Template t = ve.getTemplate(reportInfo.templatePath);
			VelocityContext context = new VelocityContext();
			
			for (Entry<String, Integer> entry: consolidatedResults.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}

			context.put("driverUsages", StatisticsStorage.getDriverUsage());
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			
			String fileName = reportInfo.prefix + reportInfo.getExtension();
		
			generateReport(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), fileName).toFile(), writer.toString());
		} catch (Exception e) {
			logger.error(String.format("Error generating test summary: %s", e.getMessage()));
		}
	}



}
