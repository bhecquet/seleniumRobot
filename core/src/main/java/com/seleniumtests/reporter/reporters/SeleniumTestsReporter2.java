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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.SeleniumTestsPageListener;
import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.reporter.PluginsHelper;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for generating test report
 * @author behe
 *
 */
public class SeleniumTestsReporter2 extends CommonReporter implements IReporter {

	private static final String STATUS = "status";
	private static final String HEADER = "header";
	private static final String APPLICATION = "application";
	private static final String APPLICATION_TYPE = "applicationType";
	private static final String METHOD_RESULT_FILE_NAME = "methodResultFileName";

	protected PrintWriter mOut;

	private String outputDirectory;
	private String resources;
	private String generationErrorMessage = null;

	
	/**
	 * Copy resources necessary for result file
	 * @throws IOException
	 */
	public void copyResources() throws IOException {
		
		List<String> styleFiles = Arrays.asList("seleniumRobot.css", "app.min.js", "seleniumRobot_solo.css", "seleniumtests_test1.gif",
											"seleniumtests_test2.gif", "seleniumtests_test3.gif", "seleniumRobot.js");
		styleFiles = new ArrayList<>(styleFiles);
		
		if (!SeleniumTestsContextManager.getGlobalContext().getOptimizeReports()) {
			styleFiles.add("bootstrap.min.css");
			styleFiles.add("bootstrap.min.js");
			styleFiles.add("Chart.min.js");
			styleFiles.add("jQuery-2.2.0.min.js");
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
				
				TestStep encodedTestStep = testStep.encode("html");
				// step status
				if (encodedTestStep.getFailed()) {
					context.put(STATUS, FAILED_TEST);
				} else {
					context.put(STATUS, PASSED_TEST);
				}
				
				context.put("stepName", encodedTestStep.getName());
				context.put("stepDuration", encodedTestStep.getDuration() / (double)1000);
				context.put("step", encodedTestStep);	
				
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
			String logs = SeleniumRobotLogger.getTestLogs().get(getTestName(testResult));
			if (logs == null) {
				logs = "";
			}
			
			
			// exception handling
			String[] stack = null;
			if (testResult.getThrowable() != null) {
				StringBuilder stackString = new StringBuilder();
				generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString);
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
				
				// issue #81: recreate test context from this context (due to multithreading, this context may be null if parallel testing is done
				SeleniumTestsContextManager.setThreadContextFromTestResult(entry.getKey(), getTestName(testResult), getClassName(testResult), testResult);
				
				try {
					mOut = createWriter(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "TestReport.html");
					startHtml(getTestStatus(testResult), mOut, "simple");
					generateExecutionReport(testResult);
					endHtml();
					logger.info("Completed Report Generation.");
				} catch (IOException e) {
					logger.error("Error writing test report: " + getTestName(testResult), e);
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
		Map<ITestContext, List<ITestResult>> methodResultsMap = new LinkedHashMap<>();
		
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {
				ITestContext context = r.getTestContext();
				List<ITestResult> resultList = new ArrayList<>();
				
				Collection<ITestResult> methodResults = new ArrayList<>();
				methodResults.addAll(context.getFailedTests().getAllResults());
				methodResults.addAll(context.getPassedTests().getAllResults());
				methodResults.addAll(context.getSkippedTests().getAllResults());

				methodResults = methodResults.stream()
							.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
							.collect(Collectors.toList());
				
				for (ITestResult result: methodResults) {
					SeleniumTestsContext testContext = (SeleniumTestsContext)result.getAttribute(SeleniumRobotTestListener.TEST_CONTEXT);
					
					String fileName;
					if (testContext != null) {
						fileName = testContext.getRelativeOutputDir() + "/TestReport.html";
					} else {
						fileName = getTestName(result) + "/TestReport.html";
					}
					result.setAttribute(METHOD_RESULT_FILE_NAME, fileName);
					result.setAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME, getTestName(result));
					
				}
				resultList.addAll(methodResults);
				
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

	public void setResources(final String resources) {
		this.resources = resources;
	}
	
	public String getOutputDirectory() {	
		return outputDirectory;
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
			context.put("staticPathPrefix", "complete".equals(type) ? "": "../");
			
			// optimize reports means that resources are get from internet
			context.put("localResources", !SeleniumTestsContextManager.getGlobalContext().getOptimizeReports());
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
			
			if (testType == null) {
				velocityContext.put(APPLICATION_TYPE, "Error in initialization");
		
			
			// Log URL for web test and app info for app test
			} else if (testType.family().equals(TestType.WEB)) {
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
			
			velocityContext.put("testName", testName);
			velocityContext.put("description", testResult.getMethod().getDescription());
			
			
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

