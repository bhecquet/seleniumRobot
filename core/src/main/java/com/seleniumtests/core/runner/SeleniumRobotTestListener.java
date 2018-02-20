package com.seleniumtests.core.runner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener2;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.ResultMap;
import org.testng.internal.TestResult;

import com.google.common.collect.Iterables;
import com.mashape.unirest.http.Unirest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TearDownService;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumRobotTestListener implements ITestListener, IInvokedMethodListener2, ISuiteListener, IExecutionListener {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestListener.class);
	private static Map<Thread, Boolean> cucumberTest = Collections.synchronizedMap(new HashMap<>());
	private Date start;
	
	private Map<String, Boolean> isRetryHandleNeeded = new HashMap<>();
	private Map<String, IResultMap> failedTests = new HashMap<>();
	private Map<String, IResultMap> skippedTests = new HashMap<>();
	private Map<String, IResultMap> passedTests = new HashMap<>();
	
	private static SeleniumRobotTestListener currentListener;
	
	public SeleniumRobotTestListener() {
		currentListener = this;
	}

	public Map<String, Boolean> getIsRetryHandleNeeded() {
		return isRetryHandleNeeded;
	}

	protected String buildMethodSignature(final Method method) {
        return method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "()";
    }

	@Override
	public void onTestStart(ITestResult result) {
		// nothing to do
	}

	@Override
	public void onTestSuccess(ITestResult result) {
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
			} else {
				IResultMap rMap = failedTests.get(testResult.getTestContext().getName());
				rMap.addResult(testResult, testResult.getMethod());
				failedTests.put(testResult.getTestContext().getName(), rMap);
			}

			logger.info(testResult.getMethod() + " Failed in " + testRetryAnalyzer.getCount() + " times");
			isRetryHandleNeeded.put(testResult.getTestContext().getName(), true);
		}		
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		// nothing to do
		
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// nothing to do
		
	}

	@Override
	public void onStart(ITestContext context) {
        start = new Date();
        SeleniumTestsContextManager.initGlobalContext(context);
        SeleniumTestsContextManager.initThreadContext(context, null);  
        
        isRetryHandleNeeded.put(context.getName(), false);
		failedTests.put(context.getName(), new ResultMap());
		skippedTests.put(context.getName(), new ResultMap());
		passedTests.put(context.getName(), new ResultMap());
		
	}

	@Override
	public void onFinish(ITestContext context) {
		if (isRetryHandleNeeded.get(context.getName())) {
			removeIncorrectlySkippedTests(context, failedTests.get(context.getName()));
			removeFailedTestsInTestNG(context);
		} else {
			failedTests.put(context.getName(), context.getFailedTests());
			skippedTests.put(context.getName(), context.getSkippedTests());
			passedTests.put(context.getName(), context.getPassedTests());
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
		
//		// issue #94: add ability to request thread context from a method annotated with @BeforeMethod
//		if (method.isConfigurationMethod() && ((ConfigurationMethod)method.getTestMethod()).isBeforeMethodConfiguration()) {
//			SeleniumTestsContextManager.initThreadContext(context, null);
//		}
		
		if (method.isTestMethod()) {

	    	if (SeleniumRobotTestPlan.isCucumberTest()) {
	    		testResult.setAttribute(SeleniumRobotLogger.METHOD_NAME, testResult.getParameters()[0].toString());
	    		
	    		logger.info(SeleniumRobotLogger.START_TEST_PATTERN + testResult.getParameters()[0].toString());
	            SeleniumTestsContextManager.initThreadContext(context, testResult.getParameters()[0].toString());
	            SeleniumTestsContextManager.getThreadContext().setTestMethodSignature(testResult.getParameters()[0].toString());
	    		
	    	} else {
	    		testResult.setAttribute(SeleniumRobotLogger.METHOD_NAME, method.getTestMethod().getMethodName());
	    		
		        logger.info(SeleniumRobotLogger.START_TEST_PATTERN + method.getTestMethod().getMethodName());
		        SeleniumTestsContextManager.initThreadContext(context, method.getTestMethod().getMethodName());
		        SeleniumTestsContextManager.getThreadContext().setTestMethodSignature(
		        		buildMethodSignature(method.getTestMethod().getConstructorOrMethod().getMethod()));
	    	}
	    	
	    	if (testResult.getMethod().getRetryAnalyzer() == null) {
	    		testResult.getMethod().setRetryAnalyzer(new TestRetryAnalyzer());
			}	
		}
		
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
		
		Reporter.setCurrentTestResult(testResult);
		
		if (method.isTestMethod()) {
	        List<TearDownService> serviceList = SeleniumTestsContextManager.getThreadContext().getTearDownServices();
	        if (serviceList != null && !serviceList.isEmpty()) {
	            for (TearDownService service : serviceList) {
	                service.tearDown();
	            }
	        }

	        logger.info(SeleniumRobotLogger.END_TEST_PATTERN + testResult.getAttribute(SeleniumRobotLogger.METHOD_NAME));

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
	        
		}
	}
	
	private void unreserveVariables() {
		SeleniumRobotVariableServerConnector variableServer = SeleniumTestsContextManager.getThreadContext().getVariableServer();
		if (variableServer != null) {
			variableServer.unreserveVariables(new ArrayList<>(SeleniumTestsContextManager.getThreadContext().getConfiguration().values()));
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
	}
	
	@Override
	public void onExecutionStart() {
		// nothing to do at startup
	}

	@Override
	public void onExecutionFinish() {
		try {
			SeleniumRobotLogger.parseLogFile();
		} catch (IOException e) {
			logger.error("cannot read log file", e);
		}
        try {
			Unirest.shutdown();
		} catch (IOException e) {
			logger.error("Cannot stop unirest", e);
		}
	}
	
	

	/**
	 * On test end, will take a snap shot and store it
	 */
	private void logLastStep(ITestResult testResult) {
		
		// finalize manual steps if we use this mode
		try {
			TestTasks.addStep(null);
		} catch (ConfigurationException e) {}
		
		TestStep tearDownStep = new TestStep("Test end", testResult);
		TestLogging.setCurrentRootTestStep(tearDownStep);
		TestLogging.log(String.format("Test is %s", testResult.isSuccess() ? "OK": "KO with error: " + testResult.getThrowable().getMessage()));
		
		if (WebUIDriver.getWebDriver(false) != null) {
			for (ScreenShot screenshot: new ScreenshotUtil().captureWebPageSnapshots(true)) {
				TestLogging.logScreenshot(screenshot);
			}
		}
		TestLogging.logTestStep(tearDownStep);
		
        WebUIDriver.cleanUp();
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
	 * Remove retrying failed test cases from skipped test cases.
	 *
	 * @param   tc
	 * @param   failedTests
	 *
	 * @return
	 */
	private void removeIncorrectlySkippedTests(final ITestContext tc, final IResultMap failedTests) {
		List<ITestNGMethod> failsToRemove = new ArrayList<>();
		IResultMap returnValue = tc.getSkippedTests();

		for (ITestResult result : returnValue.getAllResults()) {
			for (ITestResult resultToCheck : failedTests.getAllResults()) {
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


	/**
	 * Remove failed test cases in TestNG.
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

}
