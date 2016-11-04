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
package com.seleniumtests.reporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ResultMap;
import org.testng.internal.TestResult;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.SeleniumTestsPageListener;
import com.seleniumtests.core.testretry.ITestRetryAnalyzer;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;

public class SeleniumTestsReporter2 extends CommonReporter implements IReporter, ITestListener {

	private static final String HEADER = "header";
	private static final String APPLICATION = "application";
	private static final String APPLICATION_TYPE = "applicationType";
	private static final String FAILED_TEST = "failed";
	private static final String SKIPPED_TEST = "skipped";
	private static final String PASSED_TEST = "passed";
	private static final String RESOURCES_DIR = "resources";
	private static final String METHOD_RESULT_FILE_NAME = "methodResultFileName";

	private Map<String, Boolean> isRetryHandleNeeded = new HashMap<>();

	private Map<String, IResultMap> failedTests = new HashMap<>();

	private Map<String, IResultMap> skippedTests = new HashMap<>();

	private Map<String, IResultMap> passedTests = new HashMap<>();
	protected PrintWriter mOut;

	private String outputDirectory;
	private String resources;
	private String generationErrorMessage = null;
	
	/**
	 * In case test result is SUCCESS but some softAssertions were raised, change test result to 
	 * FAILED
	 * 
	 * @param result
	 */
	public void changeTestResult(final ITestResult result) {
		List<Throwable> verificationFailures = SeleniumTestsContextManager.getThreadContext().getVerificationFailures(Reporter.getCurrentTestResult());

		int size = verificationFailures.size();
		if (size == 0) {
			return;
		} else if (result.getStatus() == TestResult.FAILURE) {
			return;
		}

		result.setStatus(TestResult.FAILURE);

		if (size == 1) {
			result.setThrowable(verificationFailures.get(0));
		} else {

			// create failure message with all failures and stack traces barring last failure)
			StringBuilder failureMessage = new StringBuilder("!!! Many Test Failures (").append(size).append(
					"):nn");
			for (int i = 0; i < size - 1; i++) {
				failureMessage.append("Failure ").append(i + 1).append(" of ").append(size).append(":n");

				Throwable t = verificationFailures.get(i);
				String fullStackTrace = Utils.stackTrace(t, false)[1];
				failureMessage.append(fullStackTrace).append("nn");
			}

			// final failure
			Throwable last = verificationFailures.get(size - 1);
			failureMessage.append("Failure ").append(size).append(" of ").append(size).append(":n");
			failureMessage.append(last.toString());

			// set merged throwable
			Throwable merged = new Throwable(failureMessage.toString());
			merged.setStackTrace(last.getStackTrace());

			result.setThrowable(merged);
		}

		// move test for passedTests to failedTests if test is not already in failed tests
		if (result.getTestContext().getPassedTests().getAllMethods().contains(result.getMethod())) {
			result.getTestContext().getPassedTests().removeResult(result);
			result.getTestContext().getFailedTests().addResult(result, result.getMethod());
		}

	}

	@Override
	public void afterInvocation(final IInvokedMethod method, final ITestResult result) {
		Reporter.setCurrentTestResult(result);

		// Handle Soft CustomAssertion
		if (method.isTestMethod()) {
			changeTestResult(result);
		}
	}

	
	/**
	 * Copy resources necessary for result file
	 * @throws IOException
	 */
	public void copyResources() throws IOException {
		
		String[] styleFiles = new String[] {"bootstrap.min.css", "bootstrap.min.js", "Chart.min.js", "jQuery-2.2.0.min.js",
											"seleniumRobot.css", "app.min.js", "seleniumRobot_solo.css", "seleniumtests_test1.gif",
											"seleniumtests_test2.gif", "seleniumtests_test3.gif", "AdminLTE.min.css",
											"seleniumRobot.js"};
		for (String fileName: styleFiles) {
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporter/templates/" + fileName), 
											Paths.get(outputDirectory, RESOURCES_DIR, "templates", fileName).toFile());
		}
	}

	/**
	 * Completes HTML stream.
	 *
	 * @param  out
	 */
	protected void endHtml() {
		//Add footer
		
		try {
			VelocityEngine ve = initVelocityEngine();

			Template t = ve.getTemplate( "reporter/templates/report.part.test.footer.vm");
			StringWriter writer = new StringWriter();
			VelocityContext context = new VelocityContext();
			t.merge( context, writer );

			mOut.write(writer.toString());
			mOut.flush();
			mOut.close();
		} catch (Exception e) {
			logger.error("error writing result file end: " + e.getMessage());
		}
	}

	/**
	 * Generate result for a single test method
	 * @param ve			velocity engine used to generate file
	 * @param testResult	result for this test method
	 */
	public void generatePanel(final VelocityEngine ve, final ITestResult testResult) {

		try {
			Template t = ve.getTemplate( "reporter/templates/report.part.test.step.vm" );
			VelocityContext context = new VelocityContext();
			
			List<TestStep> testSteps = TestLogging.getTestsSteps().get(testResult);
			if (testSteps == null) {
				return;
			}
			
			for (TestStep testStep: testSteps) {
				
				// step status
				if (testStep.getFailed()) {
					context.put("status", FAILED_TEST);
				} else {
					context.put("status", PASSED_TEST);
				}
				
				context.put("stepName", testStep.getName());
				context.put("stepDuration", testStep.getDuration() / (double)1000);
				context.put("step", testStep);	
				
				StringWriter writer = new StringWriter();
				t.merge( context, writer );
				mOut.write(writer.toString());
			}

		} catch (Exception e) {
			generationErrorMessage = "generatePanel, Exception creating a singleTest:" + e.getMessage();
			logger.error("Exception creating a singleTest.", e);
		}
	}
	
	/**
	 * Generate result for a single test method
	 * @param ve			velocity engine used to generate file
	 * @param testResult	result for this test method
	 */
	public void generateExecutionLogs(final VelocityEngine ve, final ITestResult testResult) {
		
		try {
			Template t = ve.getTemplate( "reporter/templates/report.part.test.logs.vm" );
			VelocityContext context = new VelocityContext();
			
			// add logs
			String logs = TestLogging.getTestLogs().get(testResult.getName());
			if (logs == null) {
				return;
			}
			
			// exception handling
			String[] stack = null;
			if (testResult.getThrowable() != null) {
				StringBuilder stackString = new StringBuilder();
				generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString);
				stack = stackString.toString().split("\n");
			}
			
			String[] logLines = logs.split("\n");
			context.put("status", getTestStatus(testResult));
			context.put("stacktrace", stack);
			context.put("logs", logLines);
			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			mOut.write(writer.toString());
			
			
		} catch (Exception e) {
			generationErrorMessage = "generateExecutionLogs, Exception creating execution logs:" + e.getMessage();
			logger.error("Exception creating execution logs.", e);
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
		
		File f = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		setOutputDirectory(f.getAbsolutePath());
		setResources(getOutputDirectory() + "/" + RESOURCES_DIR);      
		
		// Generate general report
		Map<ITestContext, List<ITestResult>> methodResultsMap = new HashMap<>(); 
		try {
			mOut = createWriter(getOutputDirectory(), "SeleniumTestReport.html");
			startHtml(null, mOut, "complete");
			methodResultsMap = generateSuiteSummaryReport(suites);
			endHtml();
			mOut.flush();
			mOut.close();
			copyResources();
			logger.info("Completed Report Generation.");

		} catch (IOException e) {
			logger.error("Error writing summary report", e);
		}  
		
		// Generate test method reports
		for (Map.Entry<ITestContext, List<ITestResult>> entry: methodResultsMap.entrySet()) {
			for (ITestResult testResult: entry.getValue()) {
				try {
					mOut = createWriter(getOutputDirectory(), (String)testResult.getAttribute(METHOD_RESULT_FILE_NAME));
					startHtml(getTestStatus(testResult), mOut, "simple");
					generateExecutionReport(testResult);
					endHtml();
					logger.info("Completed Report Generation.");
				} catch (IOException e) {
					logger.error("Error writing test report: " + testResult.getName(), e);
				}  
			}
		}

	}

	/**
	 * Generate summary report for all test methods
	 * @param suites
	 * @param suiteName
	 * @param map
	 * @return	map containing test results
	 */
	public Map<ITestContext, List<ITestResult>> generateSuiteSummaryReport(final List<ISuite> suites) {
		
		// build result list for each TestNG test
		Map<ITestContext, List<ITestResult>> methodResultsMap = new HashMap<>();
		Integer fileIndex = 0;
		
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {
				ITestContext context = r.getTestContext();
				List<ITestResult> resultList = new ArrayList<>();
				
				for (ITestNGMethod method: context.getAllTestMethods()) {
					fileIndex++;
					String fileName = "SeleniumTestReport-" + fileIndex + ".html";
					
					Collection<ITestResult> methodResults = getResultSet(context.getFailedTests(), method);
					methodResults.addAll(getResultSet(context.getPassedTests(), method));
					methodResults.addAll(getResultSet(context.getSkippedTests(), method));
					
					if (!methodResults.isEmpty()) {
						methodResults.toArray(new ITestResult[] {})[0].setAttribute(METHOD_RESULT_FILE_NAME, fileName);
						resultList.add(methodResults.toArray(new ITestResult[] {})[0]);
					}
				}
				methodResultsMap.put(context, resultList);
			}
		}

		try {
			VelocityEngine ve = initVelocityEngine();

			Template t = ve.getTemplate("/reporter/templates/report.part.suiteSummary.vm");
			VelocityContext context = new VelocityContext();

			context.put("tests", methodResultsMap);
			context.put("steps", TestLogging.getTestsSteps());

			StringWriter writer = new StringWriter();
			t.merge(context, writer);
			mOut.write(writer.toString());

		} catch (Exception e) {
			generationErrorMessage = "generateSuiteSummaryReport error:" + e.getMessage();
			logger.error("generateSuiteSummaryReport error: ", e);
		}
		
		return methodResultsMap;

	}


	protected ITestResult getFailedOrSkippedResult(final ITestContext ctx, final ITestNGMethod method) {
		List<ITestResult> res = new LinkedList<>();
		res.addAll(failedTests.get(ctx.getName()).getResults(method));
		if (!res.isEmpty()) {
			return res.get(0);
		}

		res.addAll(ctx.getPassedTests().getResults(method));
		if (!res.isEmpty()) {
			return res.get(0);
		}

		res.addAll(skippedTests.get(ctx.getName()).getResults(method));
		if (!res.isEmpty()) {
			return res.get(0);
		}

		return null;
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

	@Override
	public void onFinish(final ITestContext arg0) {
		if (isRetryHandleNeeded.get(arg0.getName())) {
			removeIncorrectlySkippedTests(arg0, failedTests.get(arg0.getName()));
			removeFailedTestsInTestNG(arg0);
		} else {
			failedTests.put(arg0.getName(), arg0.getFailedTests());
			skippedTests.put(arg0.getName(), arg0.getSkippedTests());
			passedTests.put(arg0.getName(), arg0.getPassedTests());
		}
	}

	@Override
	public void onStart(final ITestContext arg0) {
		isRetryHandleNeeded.put(arg0.getName(), false);
		failedTests.put(arg0.getName(), new ResultMap());
		skippedTests.put(arg0.getName(), new ResultMap());
		passedTests.put(arg0.getName(), new ResultMap());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(final ITestResult arg0) { 
		// overriden
	}

	/**
	 * At the end of a failed test. 
	 * Log a screenshot and retry the test.
	 * 
	 * @param argO
	 * 
	 **/
	@Override
	public synchronized void onTestFailure(final ITestResult testResult) {
		if (testResult.getMethod().getRetryAnalyzer() != null) {
			ITestRetryAnalyzer testRetryAnalyzer = (ITestRetryAnalyzer) testResult.getMethod().getRetryAnalyzer();

			if (testRetryAnalyzer.retryPeek(testResult)) {
				testResult.setStatus(ITestResult.SKIP);
				Reporter.setCurrentTestResult(null);
			} else {
				IResultMap rMap = failedTests.get(testResult.getTestContext().getName());
				rMap.addResult(testResult, testResult.getMethod());
				failedTests.put(testResult.getTestContext().getName(), rMap);
			}

			logger.info(testResult.getMethod() + " Failed in " + testRetryAnalyzer.getCount() + " times");
			isRetryHandleNeeded.put(testResult.getTestContext().getName(), true);
		}

		// capture snap shot
		logLastStep(testResult);
	}

	@Override
	public void onTestSkipped(final ITestResult testResult) {
		// overriden
	}

	@Override
	public void onTestStart(final ITestResult testResult) {
		// overriden
	}

	/**
	 * At the end of a successful test. 
	 * Log a screenshot.
	 * 
	 * @param argO
	 * 
	 **/
	@Override
	public void onTestSuccess(final ITestResult testResult) {
		// capture snap shot at the end of the test
		logLastStep(testResult);
	}
	
	/**
	 * On test end, will take a snap shot and store it
	 */
	private void logLastStep(ITestResult testResult) {
		TestStep tearDownStep = new TestStep("Test end");
		TestLogging.setCurrentRootTestStep(tearDownStep);
		TestLogging.log(String.format("Test is %s", testResult.isSuccess() ? "OK": "KO with error: " + testResult.getThrowable().getMessage()));
		
		if (WebUIDriver.getWebDriver(false) != null) {
			ScreenShot screenShot = new ScreenshotUtil().captureWebPageSnapshot();
			TestLogging.logScreenshot(screenShot, false);
		}
		TestLogging.logTestStep(tearDownStep);
	}

	/**
	 * Remote failed test cases in TestNG.
	 *
	 * @param   tc
	 *
	 * @return
	 */
	private void removeFailedTestsInTestNG(final ITestContext tc) {
		IResultMap returnValue = tc.getFailedTests();
		ResultMap removeMap = new ResultMap();
		for (ITestResult result : returnValue.getAllResults()) {
			boolean isFailed = false;
			for (ITestResult resultToCheck : failedTests.get(tc.getName()).getAllResults()) {
				if (result.getMethod().equals(resultToCheck.getMethod())
						&& result.getEndMillis() == resultToCheck.getEndMillis()) {
					isFailed = true;
					break;
				}
			}

			if (!isFailed) {
				logger.info("Removed failed cases:" + result.getMethod().getMethodName());
				removeMap.addResult(result, result.getMethod());
			}
		}

		for (ITestResult result : removeMap.getAllResults()) {
			ITestResult removeResult = null;
			for (ITestResult resultToCheck : returnValue.getAllResults()) {
				if (result.getMethod().equals(resultToCheck.getMethod())
						&& result.getEndMillis() == resultToCheck.getEndMillis()) {
					removeResult = resultToCheck;
					break;
				}
			}

			if (removeResult != null) {
				returnValue.getAllResults().remove(removeResult);
			}
		}
	}

	/**
	 * Remove retrying failed test cases from skipped test cases.
	 *
	 * @param   tc
	 * @param   map
	 *
	 * @return
	 */
	private void removeIncorrectlySkippedTests(final ITestContext tc, final IResultMap map) {
		List<ITestNGMethod> failsToRemove = new ArrayList<>();
		IResultMap returnValue = tc.getSkippedTests();

		for (ITestResult result : returnValue.getAllResults()) {
			for (ITestResult resultToCheck : map.getAllResults()) {
				if (resultToCheck.getMethod().equals(result.getMethod())) {
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}

			for (ITestResult resultToCheck : tc.getPassedTests().getAllResults()) {
				if (resultToCheck.getMethod().equals(result.getMethod())) {
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}
		}

		for (ITestNGMethod method : failsToRemove) {
			returnValue.removeResult(method);
		}

		skippedTests.put(tc.getName(), tc.getSkippedTests());

	}

	public void setOutputDirectory(final String outtimestamped) {
		this.outputDirectory = outtimestamped;
	}

	public void setResources(final String resources) {
		this.resources = resources;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}

	public File getReportLocation() {
		return report;
	}

	public String getResources() {
		return resources;
	}

	/**
	 * Begin HTML file
	 * @param testPassed	true if test is OK, false if test is KO, null if test is skipped
	 * @param out
	 * @param type
	 */
	protected void startHtml(final String testStatus, final PrintWriter out, final String type) {
		try {
			VelocityEngine ve = initVelocityEngine();

			Template t = ve.getTemplate("/reporter/templates/report.part.header.vm");
			VelocityContext context = new VelocityContext();

			String userName = System.getProperty("user.name");
			context.put("userName", userName);
			context.put("currentDate", new Date().toString());

			DriverMode mode = SeleniumTestsContextManager.getGlobalContext().getRunMode();
			String hubUrl = SeleniumTestsContextManager.getGlobalContext().getWebDriverGrid();
			context.put("gridHub", "<a href='" + hubUrl + "' target=hub>" + hubUrl + "</a>");

			context.put("mode", mode.toString());

			StringBuilder sbGroups = new StringBuilder();
			sbGroups.append("envt,test");

			List<SeleniumTestsPageListener> pageListenerList = PluginsHelper.getInstance().getPageListeners();
			if (pageListenerList != null && !pageListenerList.isEmpty()) {
				for (SeleniumTestsPageListener abstractPageListener : pageListenerList) {
					sbGroups.append(",").append(abstractPageListener.getClass().getSimpleName());
				}
			}
			context.put("groups", sbGroups.toString());
			context.put("report", type);

			if (type == "simple"){
				context.put(HEADER, testStatus);
			}
			StringWriter writer = new StringWriter();
			t.merge(context, writer);
			out.write(writer.toString());

		} catch (Exception e) {
			generationErrorMessage = "startHtml error:" + e.getMessage();
			logger.error("startHtml error:", e);
		}

	}

	public Map<String, IResultMap> getFailedTests() {
		return failedTests;
	}

	public Map<String, IResultMap> getSkippedTests() {
		return skippedTests;
	}

	public Map<String, IResultMap> getPassedTests() {
		return passedTests;
	}

	public String getGenerationErrorMessage() {
		return generationErrorMessage;
	}
	
	/**
	 * Fill velocity context with test context
	 * @param velocityContext
	 */
	private void fillContextWithTestParams(VelocityContext velocityContext) {
		SeleniumTestsContext selTestContext = SeleniumTestsContextManager.getThreadContext();

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
			
			// Log URL for web test and app info for app test
			if (testType.family().equals(TestType.WEB)) {
				velocityContext.put(APPLICATION_TYPE, "Browser :");
				velocityContext.put(APPLICATION, browser);
			} else if (testType.family().equals(TestType.APP)) {
				
				// Either app Or app package and app activity is specified to run test on app
				if (StringUtils.isNotBlank(appPackage)) {
					velocityContext.put(APPLICATION_TYPE, "App Package :");
					velocityContext.put(APPLICATION, appPackage);
				} else  if (StringUtils.isNotBlank(app)) {
					velocityContext.put(APPLICATION_TYPE, "App :");
					velocityContext.put(APPLICATION, app);
				} 
			} else if (testType.family().equals(TestType.NON_GUI)) {
				velocityContext.put(APPLICATION_TYPE, "");

			} else {
				velocityContext.put(APPLICATION_TYPE, "Invalid Test type");
			}
		}  
	}

	/**
	 * Method for generating a report for test method
	 * @param suite			suite this test belongs to
	 * @param testContext
	 */
	public void generateExecutionReport(ITestResult testResult) {
		try {
			VelocityEngine ve = initVelocityEngine();
			Template t = ve.getTemplate( "reporter/templates/report.part.test.vm" );
			
			// create a context and add data
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("testName", testResult.getName());
			
			// Application information
			fillContextWithTestParams(velocityContext);       
			
			// write file
			StringWriter writer = new StringWriter();
			t.merge( velocityContext, writer );
			mOut.write(writer.toString());
			
			generatePanel(ve, testResult);
			generateExecutionLogs(ve, testResult);
			
			
			
		} catch (Exception e) {
			logger.error("Error generating execution report: " + e.getMessage());
		}
	}
}

