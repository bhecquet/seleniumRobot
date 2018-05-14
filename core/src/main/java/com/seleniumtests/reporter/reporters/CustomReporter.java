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
package com.seleniumtests.reporter.reporters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class CustomReporter extends CommonReporter implements IReporter {
	
	private List<String> generatedFiles;


	public List<String> getGeneratedFiles() {
		return generatedFiles;
	}

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		generatedFiles = new ArrayList<>();
		
		Map<String, Integer> consolidatedResults = new HashMap<>();
		consolidatedResults.put("pass", 0);
		consolidatedResults.put("fail", 0);
		consolidatedResults.put("skip", 0);
		consolidatedResults.put("total", 0);
		
		for (ISuite suite: suites) {
			for (String suiteString: suite.getResults().keySet()) {
				ISuiteResult suiteResult = suite.getResults().get(suiteString);
				
				ITestContext context = suiteResult.getTestContext();
				
				Set<ITestResult> resultSet = new HashSet<>(); 
				resultSet.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
				
				
				consolidatedResults.put("fail", consolidatedResults.get("fail") + context.getFailedTests().getAllResults().size());
				consolidatedResults.put("pass", consolidatedResults.get("pass") + context.getPassedTests().getAllResults().size());
				consolidatedResults.put("skip", consolidatedResults.get("skip") + context.getSkippedTests().getAllResults().size());
				
				Integer total = consolidatedResults.get("pass") + consolidatedResults.get("fail") + consolidatedResults.get("skip");
				consolidatedResults.put("total", total);
				
				for (ITestResult testResult: resultSet) {
					
					// done in case it was null (issue #81)
					SeleniumTestsContextManager.setThreadContextFromTestResult(context, getTestName(testResult), getClassName(testResult), testResult);
					
					for (ReportInfo reportInfo: SeleniumTestsContextManager.getThreadContext().getCustomTestReports()) {
						generateTestReport(testResult, reportInfo);
					}
				}
			}
		}
		
		for (ReportInfo reportInfo: SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports()) {
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
			
			Long testDuration = 0L;
			Integer errors = 0;
			List<TestStep> testSteps = TestLogging.getTestsSteps().get(testResult);
			if (testSteps != null) {
				for (TestStep step: testSteps) {
					testDuration += step.getDuration();
					if (step.getFailed()) {
						errors++;
					}
				}
			}
			
			List<String> stack = null;
			if (testResult.getThrowable() != null) {
				StringBuilder stackString = new StringBuilder();
				generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString);
				stack = Arrays.asList(stackString.toString().split("\n"));
			}
	
			// if adding some information, don't forget to add them to velocity model for integration tests
			context.put("errors", 0);
			context.put("newline", "\n");
			context.put("failures", errors);
			context.put("hostname", testResult.getHost() == null ? "": testResult.getHost());
			context.put("suiteName", testResult.getAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME));
			context.put("className", testResult.getTestClass().getName());
			context.put("tests", testSteps == null ? 0: testSteps.size());
			context.put("duration", testDuration / 1000.0);
			context.put("time", testResult.getStartMillis());	
			context.put("testSteps", testSteps);	
			context.put("browser", SeleniumTestsContextManager.getThreadContext().getBrowser());	
			context.put("version", SeleniumTestsContextManager.getApplicationVersion());	
			context.put("parameters", SeleniumTestsContextManager.getThreadContext().getContextDataMap());
			context.put("stacktrace", stack);
			context.put("logs", 0);	
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			
			String fileName = reportInfo.prefix 
								+ "-result" 
								+ reportInfo.extension;
			PrintWriter fileWriter = createWriter(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), fileName);
			fileWriter.write(writer.toString());
			fileWriter.flush();
			fileWriter.close();
			generatedFiles.add(fileName);
		} catch (Exception e) {
			logger.error(String.format("Error generating test result %s: %s", testResult.getAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME), e.getMessage()));
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
