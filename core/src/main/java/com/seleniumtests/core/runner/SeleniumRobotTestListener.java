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
package com.seleniumtests.core.runner;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.TestResult;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

import com.google.common.collect.Iterables;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.reporter.logger.ArchiveMode;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.reporter.reporters.ReporterControler;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.Unirest;

public class SeleniumRobotTestListener implements ITestListener, IInvokedMethodListener, ISuiteListener, IExecutionListener, IConfigurationListener {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestListener.class);
	private static ScenarioLogger scenarioLogger = ScenarioLogger.getScenarioLogger(SeleniumRobotTestListener.class);
	
	private static List<ISuite> suiteList = Collections.synchronizedList(new ArrayList<>());
	private Date start;

	protected String buildMethodSignature(final Method method) {
        return method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "()";
    }

	@Override
	public void onTestStart(ITestResult result) {
		// nothing to do
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		System.out.println("success");
		
		// test is success, so it will not be retried
		TestNGResultUtils.setNoMoreRetry(result, true);
		
		generateTempReport(result);
	}

	/**
	 * Called when test really failed
	 */
	@Override
	public synchronized void onTestFailure(ITestResult testResult) {
		if (testResult.getMethod().getRetryAnalyzer(testResult) != null && !(testResult.getMethod().getRetryAnalyzer(testResult) instanceof DisabledRetryAnalyzer)) {
			TestRetryAnalyzer testRetryAnalyzer = (TestRetryAnalyzer) testResult.getMethod().getRetryAnalyzer(testResult);

			logger.info(testResult.getMethod() + " Failed in " + (testRetryAnalyzer.getCount()) + " times");
		}		
		
		generateTempReport(testResult);
	}

	/**
	 * Called when test is explicitly skipped or when a test is failed but will be retried
	 */
	@Override
	public void onTestSkipped(ITestResult testResult) {
		// nothing to do
		System.out.println("skipped");
		
		// be sure that the result contains context. It can happen when the test is never executed
		// initialize it from the method context as it's the closest for our test
		if (TestNGResultUtils.getSeleniumRobotTestContext(testResult) == null) {
			SeleniumTestsContextManager.insertThreadContext(
					testResult.getMethod(),
					testResult,
					testResult.getTestContext()
					);
		}
		
		generateTempReport(testResult);
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// nothing to do
		
	}
	
	/**
	 * Generate the current test report as soon as test is executed
	 * If requested, all test data will be copied into a zip file
	 */
	private void generateTempReport(ITestResult testResult) {
		try {
			new ReporterControler().generateReport(
					Arrays.asList(testResult.getTestContext().getCurrentXmlTest().getSuite()), 
					Arrays.asList(testResult.getTestContext().getSuite()), 
					SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(),
					testResult);
			
			if (SeleniumTestsContextManager.getThreadContext().getKeepAllResults() && testResult.getMethod().getRetryAnalyzer() != null) {
				TestRetryAnalyzer testRetryAnalyzer = (TestRetryAnalyzer) testResult.getMethod().getRetryAnalyzer();
				
				// test will be retried, store the result before it is replaced
				if (testRetryAnalyzer.willBeRetried(testResult)) {
				
					// save result to zip, for future use
					String zipFileName = String.format("retry-%s-%d.zip", 
								CommonReporter.getTestName(testResult), 
								testRetryAnalyzer.getCount());
					FileUtility.zipFolder(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()), 
							Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), zipFileName).toFile(),
							FileFilterUtils.notFileFilter(FileFilterUtils.and(FileFilterUtils.prefixFileFilter("retry-", null), FileFilterUtils.suffixFileFilter(".zip"))),
							true);
				}
			}
			
			
		} catch (Throwable e) {
			logger.error("Error generating temp report", e);
		}
	}

	@Override
	public void onStart(ITestContext context) {
        start = new Date();
        
        // global context of the test
        SeleniumTestsContextManager.initGlobalContext(context);		
	}

	/**
	 * Filter TestNG results to keep only the last one for each test execution
	 * E.g: if test failed and is retried (ok / ko / skip) keep the last state only
	 * changed with issue #148
	 */
	@Override
	public void onFinish(ITestContext context) {
		
		// get an ordered list of test results so that we keep the last one of each test
		List<ITestResult> allResults = new ArrayList<>();
		allResults.addAll(context.getFailedTests().getAllResults());
		allResults.addAll(context.getSkippedTests().getAllResults());
		allResults.addAll(context.getPassedTests().getAllResults());
		
		allResults = allResults.stream()
				.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
				.collect(Collectors.toList());
		
		// contains only the results to keep
		Map<String, ITestResult> uniqueResults = new HashMap<>();
		for (ITestResult result: allResults) {
			String hash = TestNGResultUtils.getHashForTest(result);
			uniqueResults.put(hash, result);
		}
		
		// remove results we do not want from context
		List<ITestResult> resultsToKeep = new ArrayList<>(uniqueResults.values());
		
		for (ITestResult result: context.getFailedTests().getAllResults()) {
			if (!resultsToKeep.contains(result)) {
				context.getFailedTests().removeResult(result);
			}
		}
		for (ITestResult result: context.getSkippedTests().getAllResults()) {
			if (!resultsToKeep.contains(result)) {
				context.getSkippedTests().removeResult(result);
			}
		}
		for (ITestResult result: context.getPassedTests().getAllResults()) {
			if (!resultsToKeep.contains(result)) {
				context.getPassedTests().removeResult(result);
			}
		}
	}

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		// nothing to do
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		// nothing to do
	}

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		Reporter.setCurrentTestResult(testResult);
		
		// issue #94: add ability to request thread context from a method annotated with @BeforeMethod
		// for each beforemethod, store the current context so that it can be edited by the configuration method
		if (method.isConfigurationMethod()) {
			configureThreadContextBeforeInvoke(method, testResult, context);
		}

		if (method.isTestMethod()) {
			executeBeforeTestMethod(method, testResult, context);	
		}
	}
	
	/**
	 * Do after configuration or method call
	 * issue #150: intercept any exception so that \@AfterMethod gets called even if this method fails (see {@link Invoker.class.invokeMethod()}) 
	 */
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		Reporter.setCurrentTestResult(testResult);
		
		try {
			if (method.isConfigurationMethod()) {
				configureThreadContextAfterInvoke(method, testResult, context); 
			}
			
			if (method.isTestMethod()) {
		        executeAfterTestMethod(method, testResult, context);
			}
		} catch (Exception e) {
			logger.error(String.format("error while finishing invocation of %s : %s", method.getTestMethod().getQualifiedName(), e.getMessage()));
		}
	}
	
	private void unreserveVariables() {
		try {
			SeleniumRobotVariableServerConnector variableServer = SeleniumTestsContextManager.getThreadContext().getVariableServer();
			if (variableServer != null) {
				variableServer.unreserveVariables(new ArrayList<>(SeleniumTestsContextManager.getThreadContext().getConfiguration().values()));
			}
		} catch (Exception e) {
			logger.error("could not unreserve variable: " + e.getMessage());
		}
	}

	@Override
	public void onStart(ISuite suite) {

		SeleniumTestsContextManager.initGlobalContext(suite);
    	SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(),
										 SeleniumTestsContextManager.getGlobalContext().getDefaultOutputDirectory()); 
		
    	SeleniumTestsContextManager.generateApplicationPath(suite.getXmlSuite());
    	logger.info(String.format("Application %s version: %s (%s)", SeleniumTestsContextManager.getApplicationName(), 
    									SeleniumTestsContextManager.getApplicationVersion(),
    									SeleniumTestsContextManager.getApplicationFullVersion()));
    	logger.info(String.format("Core version: %s (%s)", SeleniumTestsContextManager.getCoreVersion(), SeleniumTestsContextManager.getCoreFullVersion()));

		
	}

	@Override
	public void onFinish(ISuite suite) {
		if (start != null) {
			logger.info("Test Suite Execution Time: " + (new Date().getTime() - start.getTime()) / 1000 / 60 + " minutes.");
		} else {
			logger.warn("No test executed");
		}	
		suiteList.add(suite);
	}
	
	@Override
	public void onExecutionStart() {
		suiteList = Collections.synchronizedList(new ArrayList<>());
		Unirest.config().reset();
		Unirest.config().followRedirects(true);
	}
	

	/**
	 * Do we archive test results
	 * if never is set, whatever else is set, no archiving will be done
	 * if always is set, and not never, archiving will always be done
	 * @param testSkipped
	 * @param testFailed
	 * @return
	 */
	private boolean doArchive(boolean testSkipped, boolean testFailed) {
		List<ArchiveMode> archiveModes = SeleniumTestsContextManager.getGlobalContext().getArchive();
		
		if (archiveModes.contains(ArchiveMode.NEVER)) {
			return false;
		}
		
		else if (testSkipped && archiveModes.contains(ArchiveMode.ON_SKIP)
				|| testFailed && archiveModes.contains(ArchiveMode.ON_ERROR)
				|| (!(testFailed || testSkipped) && archiveModes.contains(ArchiveMode.ON_SUCCESS))
				|| archiveModes.contains(ArchiveMode.ALWAYS)
				) {
			return true;
		}
		return false;
		
	}

	@Override
	public void onExecutionFinish() {
        try {
			Unirest.shutDown();
		} catch (Exception e) {
			logger.error("Cannot stop unirest", e);
		}
        
        boolean failed = false;
        boolean skipped = false;
        for (ISuite suite : suiteList) {
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {
				if (!r.getTestContext().getFailedTests().getAllResults().isEmpty()) {
					failed = true;
				}
				else if (!r.getTestContext().getSkippedTests().getAllResults().isEmpty()) {
					skipped = true;
				}
			}
        }
        
        // archive results
        if (doArchive(skipped, failed) 
        		&& SeleniumTestsContextManager.getGlobalContext().getArchiveToFile() != null) {
        	try {
				FileUtility.zipFolder(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()), 
									  new File(SeleniumTestsContextManager.getGlobalContext().getArchiveToFile()));
				logger.info("Archiving OK => " + SeleniumTestsContextManager.getGlobalContext().getArchiveToFile());
        	} catch (Exception e) {
        		logger.error(String.format("Archiving KO [%s] => %s", e.getMessage(), SeleniumTestsContextManager.getGlobalContext().getArchiveToFile()));
        	}
		}
	}
	
	@Override
	public void onConfigurationSuccess(ITestResult itr) {
	}

	@Override
	public void onConfigurationFailure(ITestResult itr) {
		logger.error(String.format("Error on configuration method %s.%s: %s", itr.getMethod().getTestClass().getName(), itr.getMethod().getMethodName(), itr.getThrowable().getMessage()));
	}

	@Override
	public void onConfigurationSkip(ITestResult itr) {
		logger.error(String.format("Skip on method %s.%s", itr.getMethod().getTestClass().getName(), itr.getMethod().getMethodName()));
	}
	

	/**
	 * On test end, will take a snap shot and store it
	 */
	private void logLastStep(ITestResult testResult) {
		
		// finalize manual steps if we use this mode
		try {
			TestTasks.addStep(null);
		} catch (ConfigurationException e) {}
		
		TestStep tearDownStep = new TestStep("Test end", testResult, new ArrayList<>(), true);
		TestStepManager.setCurrentRootTestStep(tearDownStep);
		
		if (testResult.isSuccess()) {
			scenarioLogger.log("Test is OK");
		} else if (testResult.getStatus() == ITestResult.FAILURE) {
			
			// issue #289: allow retry in case SO_TIMEOUT is raised
			if (SeleniumTestsContextManager.getThreadContext().getRunMode() != DriverMode.LOCAL
					&& testResult.getThrowable() != null 
					&& testResult.getThrowable() instanceof WebDriverException 
					&& testResult.getThrowable().getMessage().contains("SO_TIMEOUT")) {
				logger.info("Test is retried due to SO_TIMEOUT");
				increaseMaxRetry();
			}
				
			String error = testResult.getThrowable() != null ? testResult.getThrowable().getMessage(): "no error found";
			scenarioLogger.log("Test is KO with error: " + error);
		} else {
			scenarioLogger.log("Test has not started or has been skipped");
		}
		
		File videoFile = WebUIDriver.logFinalDriversState();
		if (videoFile != null) {
			Path pathAbsolute = Paths.get(videoFile.getAbsolutePath());
	        Path pathBase = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
	        Path pathRelative = pathBase.relativize(pathAbsolute);
	        
	        if (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.TRUE
	        		|| (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.ON_SUCCESS && testResult.isSuccess())
	        		|| (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.ON_ERROR && !testResult.isSuccess())) {
	        	scenarioLogger.logFile(pathRelative.toFile(), "Video capture");
	        	logger.info("Video file copied to " + pathAbsolute.toFile().getAbsolutePath());
			} else {
				pathAbsolute.toFile().delete();
			}
		}
		
		tearDownStep.updateDuration();
		TestStepManager.logTestStep(tearDownStep);
		
	}
	

	/**
	 * In case test result is SUCCESS but some softAssertions were raised, change test result to 
	 * FAILED
	 * 
	 * @param result
	 */
	public void changeTestResult(final ITestResult result) {
		List<Throwable> verificationFailures = SeleniumTestsContextManager.getThreadContext().getVerificationFailures(Reporter.getCurrentTestResult());

		int size = verificationFailures.size();
		if (size == 0 || result.getStatus() == TestResult.FAILURE) {
			return;
		}

		result.setStatus(TestResult.FAILURE);

		if (size == 1) {
			result.setThrowable(verificationFailures.get(0));
		} else {
			
			StringBuilder stackString = new StringBuilder("!!! Many Test Failures (").append(size).append(")\n\n");
			
			for (int i = 0; i < size - 1; i++) {
				CommonReporter.generateTheStackTrace(verificationFailures.get(i), String.format("%n.%nFailure %d of %d%n", i + 1, size), stackString, "text");
			}
			
			Throwable last = verificationFailures.get(size - 1);
			stackString.append(String.format("%n.%nFailure %d of %d%n", size, size));
			stackString.append(last.toString());

			// set merged throwable
			Throwable merged = new AssertionError(stackString.toString());
			merged.setStackTrace(last.getStackTrace());

			result.setThrowable(merged);
		}

		// move test from passedTests to failedTests if test is not already in failed tests
		if (result.getTestContext().getPassedTests().getAllMethods().contains(result.getMethod())) {
			result.getTestContext().getPassedTests().removeResult(result);
			result.getTestContext().getFailedTests().addResult(result, result.getMethod());
		}
	}	

	/**
	 * put in thread context the test / class / method context that may have already be defined in other \@BeforeXXX method
	 */
	private void configureThreadContextBeforeInvoke(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		ConfigurationMethod configMethod = (ConfigurationMethod)method.getTestMethod();
		SeleniumTestsContextManager.insertThreadContext(method.getTestMethod(), testResult, context);
		
		// issue #137: block driver creation outside of @BeforeMethod / @AfterMethod so that a driver may not remain open without being used
		// other reason is that context for Class/Test/Group is shared among several test methods
		// WebDriver.cleanup() is called after @AfterMethod
		if (configMethod.isBeforeMethodConfiguration() || configMethod.isAfterMethodConfiguration()) { // TODO: to activate but for now, creating driver without having called "updateThreadContext" may raise exception (seen with HTMLUnit and proxy parameter 
			SeleniumTestsContextManager.getThreadContext().setDriverCreationBlocked(false);
		} else {
			SeleniumTestsContextManager.getThreadContext().setDriverCreationBlocked(true);
		}
	}
	

	/**
	 * Put back modified thread context to test / class / method context
	 * @param method
	 * @param testResult
	 * @param context
	 */
	private void configureThreadContextAfterInvoke(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		ConfigurationMethod configMethod = (ConfigurationMethod)method.getTestMethod();
		SeleniumTestsContextManager.saveThreadContext(method, testResult, context);
	}
	
	/**
	 * Execute the actions before test method
	 */
	private void executeBeforeTestMethod(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		TestNGResultUtils.setTestMethodName(testResult, TestNGResultUtils.getTestName(testResult));
		TestNGResultUtils.setUniqueTestName(testResult, TestNGResultUtils.getTestName(testResult));// initialize it so that it's always set
		
		SeleniumTestsContextManager.insertThreadContext(method.getTestMethod(), testResult, context);
		
    	if (testResult.getMethod().getRetryAnalyzer(testResult) == null || testResult.getMethod().getRetryAnalyzer(testResult) instanceof DisabledRetryAnalyzer) {
    		testResult.getMethod().setRetryAnalyzerClass(TestRetryAnalyzer.class);
    		((TestRetryAnalyzer)testResult.getMethod().getRetryAnalyzer(testResult)).setMaxCount(SeleniumTestsContextManager.getThreadContext().getTestRetryCount());
		}	
    	
    	// unique method name is the test name plus an index in case DataProvider is used
    	TestNGResultUtils.setUniqueTestName(testResult, SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir());
		logger.info(SeleniumRobotLogger.START_TEST_PATTERN + TestNGResultUtils.getUniqueTestName(testResult));
	}
	

	/**
	 * Finalize test method execution
	 * - update result if some tests where skipped or failed, or success but with some asserts
	 * - set status of the last step and log it
	 * - dereserve test variables
	 * - record test method context
	 * @param method
	 * @param testResult
	 * @param context
	 */
	private void executeAfterTestMethod(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		logger.info(SeleniumRobotLogger.END_TEST_PATTERN + TestNGResultUtils.getUniqueTestName(testResult));

		Reporter.setCurrentTestResult(testResult);

		// Handle Soft CustomAssertion
		if (method.isTestMethod()) {
			changeTestResult(testResult);
		}
		
		// store context in test result
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		
		if (testResult.getThrowable() != null) {
			logger.error(testResult.getThrowable().getMessage());
			
			// when error occurs, exception raised is not added to the step if this error is outside of a PageObject
			// we add it there as an exception always terminates the test (except for soft assert, but this case is handled in SoftAssertion.aj)
			TestStep lastStep = TestStepManager.getCurrentRootTestStep();
			if (lastStep == null) {
				// when steps are automatic, they are closed (lastStep is null) once method is finished
				try {
					lastStep = Iterables.getLast(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps());
				} catch (NoSuchElementException e) {} 
			}
			
			if (lastStep != null) {
				lastStep.setFailed(true);
				lastStep.setActionException(testResult.getThrowable());
			}
		}
		
		// capture snap shot at the end of the test
		logLastStep(testResult);
		
		// unreserve variables
		unreserveVariables();
	}
	
	/**
     * Allow to increment the maxRetry in case an event occurs
     * Increment will be allowed up to 2 times the total defined by configuration (default is 2)
     */
	public static void increaseMaxRetry() {
		int maxAllowedRetry = Math.max(SeleniumTestsContextManager.getThreadContext().getTestRetryCount() * 2, SeleniumTestsContext.DEFAULT_TEST_RETRY_COUNT);
    	
    	try {
    		TestRetryAnalyzer retryAnalyzer = (TestRetryAnalyzer)Reporter.getCurrentTestResult().getMethod().getRetryAnalyzer();
    		
    		if (retryAnalyzer != null && retryAnalyzer.getMaxCount() < maxAllowedRetry) {
        		retryAnalyzer.setMaxCount(retryAnalyzer.getMaxCount() + 1);
        	} else {
        		logger.info("cannot increase max retry, limit is reached");
        	}
	    } catch (ClassCastException | NullPointerException e) {
			logger.error("Retry analyzer is not a TestRetryAnalyzer instance");
		}
	}

	/**
	 * For test
	 * @return
	 */
	public static List<ISuite> getSuiteList() {
		return suiteList;
	}
}
