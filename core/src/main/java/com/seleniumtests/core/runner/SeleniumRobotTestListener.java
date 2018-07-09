package com.seleniumtests.core.runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.Invoker;
import org.testng.internal.ResultMap;
import org.testng.internal.TestResult;

import com.google.common.collect.Iterables;
import com.mashape.unirest.http.Unirest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.reporter.logger.ArchiveMode;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import net.lightbody.bmp.core.har.Har;

public class SeleniumRobotTestListener implements ITestListener, IInvokedMethodListener2, ISuiteListener, IExecutionListener, IConfigurationListener {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestListener.class);
	
	public static final String TEST_CONTEXT = "testContext";
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
		// nothing to do
	}

	@Override
	public synchronized void onTestFailure(ITestResult testResult) {
		if (testResult.getMethod().getRetryAnalyzer() != null) {
			TestRetryAnalyzer testRetryAnalyzer = (TestRetryAnalyzer) testResult.getMethod().getRetryAnalyzer();

			// test will be retried
			if (testRetryAnalyzer.retryPeek(testResult)) {
				testResult.setStatus(ITestResult.SKIP);
				Reporter.setCurrentTestResult(null);
			} 

			logger.info(testResult.getMethod() + " Failed in " + testRetryAnalyzer.getCount() + " times");
		}		
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		// nothing to do
		System.out.println("skipped");
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// nothing to do
		
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
		TestLogging.setCurrentTestResult(testResult);
		
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
    	logger.info(String.format("Application %s version: %s", SeleniumTestsContextManager.getApplicationName(), SeleniumTestsContextManager.getApplicationVersion()));
    	logger.info("Core version: " + SeleniumTestsContextManager.getCoreVersion());

		
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
	}

	@Override
	public void onExecutionFinish() {
        try {
			Unirest.shutdown();
		} catch (IOException e) {
			logger.error("Cannot stop unirest", e);
		}
        
        boolean failed = false;
        for (ISuite suite : suiteList) {
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {
				if (!r.getTestContext().getFailedTests().getAllResults().isEmpty()) {
					failed = true;
					break;
				}
			}
        }
        
        // archive results
        if ((SeleniumTestsContextManager.getThreadContext().getArchive() == ArchiveMode.TRUE
        		|| (SeleniumTestsContextManager.getThreadContext().getArchive() == ArchiveMode.ON_SUCCESS && !failed)
        		|| (SeleniumTestsContextManager.getThreadContext().getArchive() == ArchiveMode.ON_ERROR && failed)) 
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
		
		TestStep tearDownStep = new TestStep("Test end", testResult, new ArrayList<>());
		TestLogging.setCurrentRootTestStep(tearDownStep);
		
		if (testResult.isSuccess()) {
			TestLogging.log("Test is OK");
		} else if (testResult.getStatus() == ITestResult.FAILURE) {
			String error = testResult.getThrowable() != null ? testResult.getThrowable().getMessage(): "no error found";
			TestLogging.log("Test is KO with error: " + error);
		} else {
			TestLogging.log("Test has not started or has been skipped");
		}

		if (WebUIDriver.getWebDriver(false) != null) {
			try {
				for (ScreenShot screenshot: new ScreenshotUtil().captureWebPageSnapshots(true)) {
					TestLogging.logScreenshot(screenshot);
				}
			} catch (Exception e) {
				TestLogging.log("Error while logging: " + e.getMessage());
			}
		}
		
		if (WebUIDriver.getWebUIDriver(false) != null) {
			try {
		    	// stop HAR capture
				if (WebUIDriver.getWebUIDriver(false).getConfig().getBrowserMobProxy() != null) {
					Har har = WebUIDriver.getWebUIDriver(false).getConfig().getBrowserMobProxy().endHar();
					TestLogging.logNetworkCapture(har);
				}
				
				// stop video capture
				if (WebUIDriver.getWebUIDriver(false).getConfig().getVideoRecorder() != null) {
					File videoFile = null;
					try {
						videoFile = CustomEventFiringWebDriver.stopVideoCapture(SeleniumTestsContextManager.getThreadContext().getRunMode(), 
																				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
																				WebUIDriver.getWebUIDriver(false).getConfig().getVideoRecorder());
						
						if (videoFile != null) {
							Path pathAbsolute = Paths.get(videoFile.getAbsolutePath());
					        Path pathBase = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
					        Path pathRelative = pathBase.relativize(pathAbsolute);
					        
					        if (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.TRUE
					        		|| (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.ON_SUCCESS && testResult.isSuccess())
					        		|| (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.ON_ERROR && !testResult.isSuccess())) {
					        	TestLogging.logFile(pathRelative.toFile(), "Video capture");
							} else {
								pathAbsolute.toFile().delete();
							}
						}
						
	
					} catch (IOException e) {
						logger.error("cannot attach video capture", e);
					}		
				}
			} catch (Exception e) {
				TestLogging.log("Error while logging: " + e.getMessage());
				WebUIDriver.getWebUIDriver(false).getConfig().setVideoRecorder(null);
				WebUIDriver.getWebUIDriver(false).getConfig().setBrowserMobProxy(null);
			}
			WebUIDriver.cleanUpWebUIDriver();
		}
		
		tearDownStep.updateDuration();
		TestLogging.logTestStep(tearDownStep);
		
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
				CommonReporter.generateTheStackTrace(verificationFailures.get(i), String.format("Failure %d of %d%n", i + 1, size), stackString);
			}
			
			Throwable last = verificationFailures.get(size - 1);
			stackString.append(String.format("%n.%nFailure %d of %d%n", size, size));
			stackString.append(last.toString());

			// set merged throwable
			Throwable merged = new AssertionError(stackString.toString());
			merged.setStackTrace(last.getStackTrace());

			result.setThrowable(merged);
		}

		// move test for passedTests to failedTests if test is not already in failed tests
		if (result.getTestContext().getPassedTests().getAllMethods().contains(result.getMethod())) {
			result.getTestContext().getPassedTests().removeResult(result);
			result.getTestContext().getFailedTests().addResult(result, result.getMethod());
		}
	}	

	/**
	 * put in thread context the test / class / method context that may have already be defined in other \@BeforeXXX method
	 */
	private void configureThreadContextBeforeInvoke(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		SeleniumTestsContext currentBeforeContext = null;
		ConfigurationMethod configMethod = (ConfigurationMethod)method.getTestMethod();
		
		// handle some before methods
		if (configMethod.isBeforeTestConfiguration()) {
			currentBeforeContext = SeleniumTestsContextManager.storeTestContext(context);
		} else if (configMethod.isBeforeClassConfiguration()) {
			currentBeforeContext = SeleniumTestsContextManager.storeClassContext(context, method.getTestMethod().getTestClass().getName());
		} else if (configMethod.isBeforeMethodConfiguration()) {
			try {
				String className = ((Method)(testResult.getParameters()[0])).getDeclaringClass().getName();
				String methodName = ((Method)(testResult.getParameters()[0])).getName();
				currentBeforeContext = SeleniumTestsContextManager.storeMethodContext(context, className, methodName);
			} catch (Exception e) {
				throw new ScenarioException("When using @BeforeMethod in tests, this method MUST have a 'java.lang.reflect.Method' object as first argument. Example: \n\n"
						+ "@BeforeMethod\n" + 
						"public void beforeMethod(Method method) {\n"
						+ "    SeleniumTestsContextManager.getThreadContext().setAttribute(\"some attribute\", \"attribute value\");\n"
						+ "}\n\n");
			}
			
		// handle some after methods. No change in context in after method will be recorded
		} else if (configMethod.isAfterMethodConfiguration()) {
			// beforeMethod, testMethod and afterMethod run in the same thread, so it's safe to take the current context
			currentBeforeContext = SeleniumTestsContextManager.getThreadContext();
			
			try {
				((Method)(testResult.getParameters()[0])).getName();
			} catch (Exception e) {
				logger.error("\n\n\n---------------------------------------------------------------------------------------------------\n"
						+ configMethod.getConstructorOrMethod().getMethod().toGenericString()
						+ "\nWhen using @AfterMethod in tests, this method MUST have a 'java.lang.reflect.Method' object as first argument. Example: \n\n"
						+ "@AfterMethod\n" + 
						"public void afterMethod(Method method) {\n"
						+ "    ... some code here ...\n"
						+ "}\n\n"
						+ "Else, this method will be displayed in each test result even if it does not belong to the test itself\n"
						+ "---------------------------------------------------------------------------------------------------\n\n\n");
			}
			
		} else if (configMethod.isAfterClassConfiguration()) {
			currentBeforeContext = SeleniumTestsContextManager.getClassContext(context, method.getTestMethod().getTestClass().getName());
		} else if (configMethod.isAfterTestConfiguration()) {
			currentBeforeContext = SeleniumTestsContextManager.getTestContext(context);
		}
		if (currentBeforeContext != null) {
			SeleniumTestsContextManager.setThreadContext(currentBeforeContext);
		} else {
			SeleniumTestsContextManager.initThreadContext();
		}
		
		// issue #136: block driver creation outside of @BeforeMethod / @AfterMethod so that a driver may not remain open without being used
		// other reason is that context for Class/Test/Group is shared among several test methods
		// WebDriver.cleanup() is called after @AfterMethod
//		if (configMethod.isBeforeMethodConfiguration() || configMethod.isAfterMethodConfiguration()) { // TODO: to activate but for now, creating driver without having called "updateThreadContext" may raise exception (seen with HTMLUnit and proxy parameter 
		if (configMethod.isAfterMethodConfiguration()) {
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
		
		// store the current thread context back to test/class/method context as it may have been modified in "Before" methods
		if (configMethod.isBeforeTestConfiguration()) {
			SeleniumTestsContextManager.setTestContext(context, SeleniumTestsContextManager.getThreadContext());
			
		} else if (configMethod.isBeforeClassConfiguration()) {
			SeleniumTestsContextManager.setClassContext(context, method.getTestMethod().getTestClass().getName(), SeleniumTestsContextManager.getThreadContext());
			
		} else if (configMethod.isBeforeMethodConfiguration()) {
			try {
				String className = ((Method)(testResult.getParameters()[0])).getDeclaringClass().getName();
				String methodName = ((Method)(testResult.getParameters()[0])).getName();
				SeleniumTestsContextManager.setMethodContext(context, className, methodName, SeleniumTestsContextManager.getThreadContext());
			} catch (Exception e) {}
		}
		
		// reparse logs in case some new logs have been written
		if (configMethod.isAfterClassConfiguration()
				|| configMethod.isAfterTestConfiguration()
				|| configMethod.isAfterMethodConfiguration()) {
			SeleniumRobotLogger.parseLogFile();
		}
	}
	
	/**
	 * Execute the actions before test method
	 */
	private void executeBeforeTestMethod(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		testResult.setAttribute(SeleniumRobotLogger.METHOD_NAME, TestNGResultUtils.getTestName(testResult));
		testResult.setAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME, TestNGResultUtils.getTestName(testResult)); // initialize it so that it's always set
		
		// when @BeforeMethod has been used, threadContext is already initialized and may have been updated. Do not overwrite options
		// only reconfigure it
		String className = method.getTestMethod().getTestClass().getName();
		
		// create a new context from the method context so that the same test method with different data do not share the context (issue #115)
		SeleniumTestsContext currentContext = new SeleniumTestsContext(SeleniumTestsContextManager.getMethodContext(context, 
				className, 
				testResult.getAttribute(SeleniumRobotLogger.METHOD_NAME).toString(), 
				true));
		
		// allow driver to be created		
		currentContext.setDriverCreationBlocked(false);
		
		SeleniumTestsContextManager.setThreadContext(currentContext);

		SeleniumTestsContextManager.updateThreadContext(testResult);
		
        SeleniumTestsContextManager.getThreadContext().setTestMethodSignature((String)testResult.getAttribute(SeleniumRobotLogger.METHOD_NAME));
    	
    	if (testResult.getMethod().getRetryAnalyzer() == null) {
    		testResult.getMethod().setRetryAnalyzer(new TestRetryAnalyzer(SeleniumTestsContextManager.getThreadContext().getTestRetryCount()));
		}	
    	
    	// unique method name is the test name plus an index
    	testResult.setAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME, SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir());
		logger.info(SeleniumRobotLogger.START_TEST_PATTERN + testResult.getAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME));
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
		logger.info(SeleniumRobotLogger.END_TEST_PATTERN + testResult.getAttribute(SeleniumRobotLogger.UNIQUE_METHOD_NAME));

		Reporter.setCurrentTestResult(testResult);

		// Handle Soft CustomAssertion
		if (method.isTestMethod()) {
			changeTestResult(testResult);
		}
		
		if (testResult.getThrowable() != null) {
			logger.error(testResult.getThrowable().getMessage());
			
			// when error occurs, exception raised is not added to the step if this error is outside of a PageObject
			// we add it there as an exception always terminates the test (except for soft assert, but this case is handled in SoftAssertion.aj)
			TestStep lastStep = TestLogging.getCurrentRootTestStep();
			if (lastStep == null) {
				// when steps are automatic, they are closed (lastStep is null) once method is finished
				try {
					lastStep = Iterables.getLast(TestLogging.getTestsSteps().get(testResult));
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
		
		// store context in test result
		testResult.setAttribute(TEST_CONTEXT, SeleniumTestsContextManager.getThreadContext());
		
		// parse logs of this test method
		try {
			SeleniumRobotLogger.parseLogFile();
		} catch (Exception e) {
			logger.error("log parsing failed: " + e.getMessage());
		}
	}

}
