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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class CustomReporter extends CommonReporter implements IReporter {
	
	private List<String> generatedFiles;


	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}


	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport,	boolean finalGeneration) {
		generatedFiles = new ArrayList<>();
		
		Map<String, Integer> consolidatedResults = new HashMap<>();
		consolidatedResults.put("pass", 0);
		consolidatedResults.put("fail", 0);
		consolidatedResults.put("skip", 0);
		consolidatedResults.put("total", 0);
		
		for (Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {
			for (ITestResult testResult: entry.getValue()) {
				if (testResult.isSuccess()) {
					consolidatedResults.put("pass", consolidatedResults.get("pass") + 1);
				} else if (testResult.getStatus() == ITestResult.FAILURE) {
					consolidatedResults.put("fail", consolidatedResults.get("fail") + 1);
				} else if (testResult.getStatus() == ITestResult.SKIP) {
					consolidatedResults.put("skip", consolidatedResults.get("skip") + 1);
				}
				consolidatedResults.put("total", consolidatedResults.get("total") + 1);
				
				// done in case it was null (issue #81)
				SeleniumTestsContext testContext = SeleniumTestsContextManager.setThreadContextFromTestResult(entry.getKey(), getTestName(testResult), getClassName(testResult), testResult);
				

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
	 * @param ve
	 * @param testResult
	 */
	private void generateTestReport(ITestResult testResult, ReportInfo reportInfo) {
		try {

			VelocityEngine ve;
			try {
				ve = initVelocityEngine();
			} catch (Exception e) {
				throw new ScenarioException("Error generating test results");
			}
			
			Template t = ve.getTemplate(reportInfo.getTemplatePath());
			VelocityContext context = new VelocityContext();
			
			String reportFormat = reportInfo.getExtension().substring(1);
			Long testDuration = 0L;
			Integer errors = 0;
			String failedStep = "";
			List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
			List<TestStep> newTestSteps = new ArrayList<>();
			if (testSteps != null) {
				for (TestStep step: testSteps) {
					testDuration += step.getDuration();
					if (step.getFailed()) {
						failedStep = step.getName();
						errors++;
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
			if (testResult.getStatus() == ITestResult.SUCCESS) {
				errors = 0;
			}
			
			List<String> stack = null;
			if (testResult.getThrowable() != null) {
				StringBuilder stackString = new StringBuilder();
				generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString, reportFormat.toLowerCase());
				stack = Arrays.asList(stackString.toString().split("\n"));
			}
			
			SeleniumTestsContext seleniumTestsContext = TestNGResultUtils.getSeleniumRobotTestContext(testResult);
	
			// if adding some information, don't forget to add them to velocity model for integration tests
			context.put("errors", 0);
			context.put("newline", "\n");
			context.put("failures", errors);
			context.put("hostname", testResult.getHost() == null ? "": testResult.getHost());
			context.put("suiteName", TestNGResultUtils.getUniqueTestName(testResult));
			context.put("className", testResult.getTestClass().getName());
			context.put("tests", newTestSteps == null ? 0: newTestSteps.size());
			context.put("duration", testDuration / 1000.0);
			context.put("time", testResult.getStartMillis());	
			context.put("startDate", new Date(testResult.getStartMillis()));	
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
			String logs = SeleniumRobotLogger.getTestLogs().get(getTestName(testResult));
			try {
				context.put("logs", logs == null ? "Test skipped": StringUtility.encodeString(logs, reportFormat.toLowerCase()));
			} catch (CustomSeleniumTestsException e) {
				context.put("logs", logs);
			}

			context.put("testInfos", TestNGResultUtils.getTestInfoEncoded(testResult, reportFormat));
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			
			String fileName = reportInfo.prefix 
								+ "-result" 
								+ reportInfo.extension;
			PrintWriter fileWriter = createWriter(seleniumTestsContext.getOutputDirectory(), fileName);
			fileWriter.write(writer.toString());
			fileWriter.flush();
			fileWriter.close();
			generatedFiles.add(fileName);
			TestNGResultUtils.setCustomReportCreated(testResult, true);
		} catch (Exception e) {
			logger.error(String.format("Error generating test result %s: %s", TestNGResultUtils.getUniqueTestName(testResult), e.getMessage()));
		}
	}
	
	private void generateSummaryReport(Map<String, Integer> consolidatedResults, ReportInfo reportInfo) {
		
		try {
			VelocityEngine ve;
			try {
				ve = initVelocityEngine();
			} catch (Exception e) {
				throw new ScenarioException("Error generating test results");
			}
			
			Template t = ve.getTemplate(reportInfo.templatePath);
			VelocityContext context = new VelocityContext();
			
			for (Entry<String, Integer> entry: consolidatedResults.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}

			context.put("driverUsages", StatisticsStorage.getDriverUsage());
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			
			String fileName = reportInfo.prefix + reportInfo.getExtension();
			PrintWriter fileWriter = createWriter(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), fileName);
			fileWriter.write(writer.toString());
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			logger.error(String.format("Error generating test summary: %s", e.getMessage()));
		}
	}



}
