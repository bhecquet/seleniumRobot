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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestStep.StepStatus;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for generating test report
 * @author behe
 *
 */
public class SeleniumTestsReporter2 extends CommonReporter implements IReporter {

	private static final String RESOURCES_FOLDER = "resources";
	private static final String STATUS = "status";
	private static final String HEADER = "header";
	private static final String APPLICATION = "application";
	private static final String APPLICATION_TYPE = "applicationType";
	private static final String METHOD_RESULT_FILE_NAME = "methodResultFileName";

	protected PrintWriter mOut;

	private String outputDirectory;
	private String generationErrorMessage = null;

	public SeleniumTestsReporter2() {
		setOutputDirectory(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath());
	}
	
	/**
	 * Copy resources necessary for result file
	 * @throws IOException
	 */
	public void copyResources() throws IOException {
		
		List<String> styleFiles = Arrays.asList("seleniumRobot.css", "seleniumtests_test1.gif",
											"seleniumtests_test2.gif", "seleniumtests_test3.gif", "seleniumRobot.js");
		styleFiles = new ArrayList<>(styleFiles);
		
		if (!SeleniumTestsContextManager.getGlobalContext().getOptimizeReports()) {
			styleFiles.add("bootstrap.min.css");
			styleFiles.add("bootstrap.min.css.map");
			styleFiles.add("bootstrap.bundle.min.js");
			styleFiles.add("bootstrap.bundle.min.js.map");
			styleFiles.add("Chart.min.js");
			styleFiles.add("jquery-3.4.1.min.js");
			styleFiles.add("AdminLTE.min.css");
			styleFiles.add("lobsterTwo.css");
			styleFiles.add("css/font-awesome.min.css");
			styleFiles.add("fonts/fontawesome-webfont.eot");
			styleFiles.add("fonts/fontawesome-webfont.svg");
			styleFiles.add("fonts/fontawesome-webfont.ttf");
			styleFiles.add("fonts/fontawesome-webfont.woff");
			styleFiles.add("fonts/fontawesome-webfont.woff2");
			styleFiles.add("fonts/FontAwesome.otf");
		} 
		
		for (String fileName: styleFiles) {
			File destFile = Paths.get(outputDirectory, RESOURCES_DIR, "templates", fileName).toFile();
			
			// do not copy resources if it has already been done
			if (destFile.exists()) {
				break;
			}
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporter/templates/" + fileName), 
					destFile);
		}
	}
	
	/**
	 * Returns the test status as a string
	 * @param testResult
	 * @return
	 */
	private String getTestStatus(ITestResult testResult) {
		String testStatus = SKIPPED_TEST;
		if (testResult.getStatus() == ITestResult.SUCCESS) {
			testStatus = PASSED_TEST;
		} else if (testResult.getStatus() == ITestResult.FAILURE) {
			testStatus = FAILED_TEST;
		}
		return testStatus;
	}

	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, final String outdir, boolean optimizeReport, boolean finalGeneration) {
		ITestContext testCtx = SeleniumTestsContextManager.getGlobalContext().getTestNGContext();
		if (testCtx == null) {
			logger.error("Looks like your class does not extend from SeleniumTestPlan!");
			return;
		}
		
		setOutputDirectory(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath());     
		
		// Generate general report
		Map<ITestContext, List<ITestResult>> methodResultsMap = new HashMap<>(); 
		
		try {
			methodResultsMap = generateSuiteSummaryReport(resultSet, optimizeReport);
			copyResources();
			logger.info("Completed Summary Report generation.");

		} catch (IOException e) {
			logger.error("Error writing summary report", e);
		}  
		
		// Generate test method reports for each result which has not already been generated
		for (Map.Entry<ITestContext, List<ITestResult>> entry: methodResultsMap.entrySet()) {
			for (ITestResult testResult: entry.getValue()) {
				
				// do not generate twice the same file, except at the end of test suites execution so that final results contains all information
				// HTML report created after each test method cannot contain @AfterClass post steps because they have not already been executed
				// so we need to regenerate after all tests have executed
				if (!TestNGResultUtils.isHtmlReportCreated(testResult) || finalGeneration) {
					generateSingleTestReport(testResult, optimizeReport);
				}
			}
		}
	}
	
	/**
	 * Generate HTML report for a single test
	 * @param testResult
	 * @param resourcesFromCdn		if true (optimizeReport), resources are linked from CDN
	 */
	public void generateSingleTestReport(ITestResult testResult) {
		generateSingleTestReport(testResult, false);
	}
	public void generateSingleTestReport(ITestResult testResult, boolean resourcesFromCdn) {

		// issue #81: recreate test context from this context (due to multithreading, this context may be null if parallel testing is done)
		SeleniumTestsContext testContext = SeleniumTestsContextManager.setThreadContextFromTestResult(testResult.getTestContext(), testResult);
		
		try {
			copyResources();
			
			// issue #284: copy resources specific to the single test report. They are moved here so that the file can be used without global resources
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporter/templates/seleniumRobot_solo.css"), Paths.get(testContext.getOutputDirectory(), RESOURCES_FOLDER, "seleniumRobot_solo.css").toFile());
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporter/templates/app.min.js"), Paths.get(testContext.getOutputDirectory(), RESOURCES_FOLDER, "app.min.js").toFile());
			if (!SeleniumTestsContextManager.getGlobalContext().getOptimizeReports()) {
				FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporter/templates/iframeResizer.min.js"), Paths.get(testContext.getOutputDirectory(), RESOURCES_FOLDER, "iframeResizer.min.js").toFile());
			}
			
			generateExecutionReport(testContext, testResult, getTestStatus(testResult), resourcesFromCdn);

			logger.info("Completed Test Report Generation.");
			
			// do not recreate this report anymore
			TestNGResultUtils.setHtmlReportCreated(testResult, true);
		} catch (Exception e) {
			logger.error("Error writing test report: " + getTestName(testResult), e);
		}  
	}
	
	public void generateExecutionReport(SeleniumTestsContext testContext, ITestResult testResult, String testStatus, boolean resourcesFromCdn) throws IOException  {
	
		VelocityEngine ve = initVelocityEngine();

		Template t = ve.getTemplate("/reporter/templates/report.test.vm");
		VelocityContext context = new VelocityContext();

		context.put("staticPathPrefix", "../");
		
		boolean displaySnapshots = testContext.getSeleniumRobotServerCompareSnapshot() && TestNGResultUtils.getSnapshotTestCaseInSessionId(testResult) != null && TestNGResultUtils.getSnapshotComparisonResult(testResult) != null;
		context.put("snapshots", displaySnapshots);
		context.put("snapshotServer", testContext.getSeleniumRobotServerUrl());
		context.put("snapshotComparisonResult", TestNGResultUtils.getSnapshotComparisonResult(testResult));
		context.put("snapshotSessionId", TestNGResultUtils.getSnapshotTestCaseInSessionId(testResult));
		
		// optimize reports means that resources are get from internet
		context.put("localResources", !resourcesFromCdn);
		context.put(HEADER, testStatus);
		
		// test header
		Object[] testParameters = testResult.getParameters();
		StringBuilder testName = new StringBuilder(getTestName(testResult));
		
		// issue #163: add test parameter to test name
		if (testParameters.length > 0) {
			testName.append(" with params: (");
			
			int i = 0;
			
			for (Object param: testParameters) {
				testName.append(param.toString());
				if (i < testParameters.length - 1) {
					testName.append(",");
				}
				i++;
			}
			testName.append(")");
		}
		
		context.put("testName", testName);
		context.put("description", StringUtility.encodeString(TestNGResultUtils.getTestDescription(testResult), "html"));
		context.put("testInfos", TestNGResultUtils.getTestInfoEncoded(testResult, "html"));
		
		// Application information
		fillContextWithTestParams(context, testResult);      
		
		// test steps
		List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
		if (testSteps == null) {
			testSteps = new ArrayList<>();
		}
		
		List<List<Object>> steps = new ArrayList<>();
		for (TestStep testStep: testSteps) {
			
			List<Object> stepInfo = new ArrayList<>();
			TestStep encodedTestStep = testStep.encode("html");
			
			stepInfo.add(encodedTestStep.getName());
			
			// step status
			StepStatus stepStatus = encodedTestStep.getStepStatus();
			if (stepStatus == StepStatus.FAILED) {
				stepInfo.add(FAILED_TEST);
			} else if (stepStatus == StepStatus.WARNING) {
				stepInfo.add(WARN_TEST);
			} else {
				stepInfo.add(PASSED_TEST);
			}
			stepInfo.add(encodedTestStep.getDuration() / (double)1000);
			stepInfo.add(encodedTestStep);	
			steps.add(stepInfo);
		}
		context.put("steps", steps);
		
		// logs
		String logs = SeleniumRobotLogger.getTestLogs().get(getTestName(testResult));
		if (logs == null) {
			logs = "";
		}
		
		// exception handling
		String[] stack = null;
		if (testResult.getThrowable() != null) {
			StringBuilder stackString = new StringBuilder();
			generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString, "html");
			stack = stackString.toString().split("\n");
		}
		
		// encode logs
		List<String> logLines = new ArrayList<>();
		for (String line: logs.split("\n")) {
			logLines.add(StringEscapeUtils.escapeHtml4(line));
		}
		context.put(STATUS, getTestStatus(testResult));
		context.put("stacktrace", stack);
		context.put("logs", logLines);
		
		// previous execution logs
		if (SeleniumTestsContextManager.getGlobalContext().getKeepAllResults()) {
			List<String> executionResults = FileUtils.listFiles(new File(TestNGResultUtils.getSeleniumRobotTestContext(testResult).getOutputDirectory()), 
														FileFilterUtils.suffixFileFilter(".zip"), null).stream()
					.map(File::getName)
					.collect(Collectors.toList());
			if (!executionResults.isEmpty()) {
				context.put("files", executionResults);
				context.put("title", "Previous execution results");
			}
		} else {
			context.put("title", "No previous execution results, you can enable it via parameter '-DkeepAllResults=true'");
		}

		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		generateReport(Paths.get(testContext.getOutputDirectory(), "TestReport.html").toFile(), writer.toString());
	}

	/**
	 * Generate summary report for all test methods
	 * @param suites
	 * @param suiteName
	 * @param map
	 * @return	map containing test results
	 */
	public Map<ITestContext, List<ITestResult>> generateSuiteSummaryReport(Map<ITestContext, Set<ITestResult>> resultSet, boolean resourcesFromCdn) {
			
		// build result list for each TestNG test
		Map<ITestContext, List<ITestResult>> methodResultsMap = new LinkedHashMap<>();
		Map<ITestResult, Map<String, String>> testInfosMap = new HashMap<>();
		Map<ITestResult, List<TestStep>> allSteps = new HashMap<>();
		Set<String> allInfoKeys = new HashSet<>();
		
		for (Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {
			 List<ITestResult> methodResults = entry.getValue().stream()
						.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
						.collect(Collectors.toList());
			
			for (ITestResult result: methodResults) {
				SeleniumTestsContext testContext = TestNGResultUtils.getSeleniumRobotTestContext(result);
				
				String fileName;
				if (testContext != null) {
					fileName = testContext.getRelativeOutputDir() + "/TestReport.html";
					allSteps.put(result, testContext.getTestStepManager().getTestSteps());
				} else {
					fileName = getTestName(result) + "/TestReport.html";
					allSteps.put(result, new ArrayList<>());
				}

				result.setAttribute(METHOD_RESULT_FILE_NAME, fileName);
				TestNGResultUtils.setUniqueTestName(result, getTestName(result)); // be sure test name is initialized
				
				// add test info
				Map<String, String> testInfos = TestNGResultUtils.getTestInfoEncoded(result, "html");
				testInfosMap.put(result, testInfos);
				allInfoKeys.addAll(testInfos.keySet());
				
			}
			
			methodResultsMap.put(entry.getKey(), methodResults);
		}

		try {
			VelocityEngine ve = initVelocityEngine();

			Template t = ve.getTemplate("/reporter/templates/report.part.suiteSummary.vm");
			VelocityContext context = new VelocityContext();
			
			List<String> allSortedInfoKeys = new ArrayList<>(allInfoKeys);
			allSortedInfoKeys.sort(null);

			context.put("staticPathPrefix", "");
			
			// optimize reports means that resources are get from internet
			context.put("localResources", !resourcesFromCdn);

			synchronized (allSteps) { // as we use a synchronizedList and we iterate on it
				context.put("tests", methodResultsMap);
				context.put("steps", allSteps);
				context.put("infos", testInfosMap);
				context.put("infoKeys", allSortedInfoKeys);

				
			}
			StringWriter writer = new StringWriter();

			t.merge(context, writer);
			generateReport(Paths.get(getOutputDirectory(), "SeleniumTestReport.html").toFile(), writer.toString());

			
		} catch (Exception e) {
			generationErrorMessage = "generateSuiteSummaryReport error:" + e.getMessage();
			logger.error("generateSuiteSummaryReport error: ", e);
		}
		
		return methodResultsMap;

	}

	/**
	 * @param   tests
	 *
	 * @return
	 */
	protected Collection<ITestResult> getResultSet(final IResultMap tests, final ITestNGMethod method) {
		Set<ITestResult> r = new TreeSet<>();
		for (ITestResult result : tests.getAllResults()) {
			if (result.getMethod().getMethodName().equals(method.getMethodName())) {
				r.add(result);
			}
		}

		return r;
	}

	public void setOutputDirectory(final String outtimestamped) {
		this.outputDirectory = outtimestamped;
	}
	
	public String getOutputDirectory() {	
		return outputDirectory;
	}

	public String getGenerationErrorMessage() {
		return generationErrorMessage;
	}
	
	/**
	 * Fill velocity context with test context
	 * @param velocityContext
	 */
	private void fillContextWithTestParams(VelocityContext velocityContext, ITestResult testResult) {
		SeleniumTestsContext selTestContext = TestNGResultUtils.getSeleniumRobotTestContext(testResult);

		if (selTestContext != null) {
			String browser = selTestContext.getBrowser().getBrowserType();

			String app = selTestContext.getApp();
			String appPackage = selTestContext.getAppPackage();
			TestType testType = selTestContext.getTestType();

			if (browser != null) {
				browser = browser.replace("*", "");
			}

			String browserVersion = selTestContext.getWebBrowserVersion();
			if (browserVersion != null) {
				browser = browser + browserVersion;
			}
			velocityContext.put(APPLICATION, "");
			
			if (testType == null) {
				velocityContext.put(APPLICATION_TYPE, "Error in initialization");
		
			
			// Log URL for web test and app info for app test
			} else if (testType.family().equals(TestType.WEB)) {
				velocityContext.put(APPLICATION_TYPE, "Browser");
				velocityContext.put(APPLICATION, browser);
			} else if (testType.family().equals(TestType.APP)) {
				
				// Either app Or app package and app activity is specified to run test on app
				if (StringUtils.isNotBlank(appPackage)) {
					velocityContext.put(APPLICATION_TYPE, "App Package");
					velocityContext.put(APPLICATION, appPackage);
				} else  if (StringUtils.isNotBlank(app)) {
					velocityContext.put(APPLICATION_TYPE, "App");
					velocityContext.put(APPLICATION, app);
				} 
			} else if (testType.family().equals(TestType.NON_GUI)) {
				velocityContext.put(APPLICATION_TYPE, "");

			} else {
				velocityContext.put(APPLICATION_TYPE, "Invalid Test type");
			}
		}  
	}

}

