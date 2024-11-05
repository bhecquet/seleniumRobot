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
package com.seleniumtests.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.Logger;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector.SnapshotComparisonResult;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.core.testanalysis.ErrorCause;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
import com.seleniumtests.reporter.info.Info;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.ExceptionUtility;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestNGResultUtils {

	protected static Logger logger = SeleniumRobotLogger.getLogger(TestNGResultUtils.class);

	private static final String UNIQUE_METHOD_NAME = "uniqueMethodName"; // unique name of the test (in case several tests have the same name)
	private static final String VISUAL_TEST_NAME = "visualTestName"; 	// Name that will be displayed in reports for user (HTML and JUnit report)
	private static final String TEST_CONTEXT = "testContext";
	private static final String LINKED_TEST_METHOD = "linkedTestMethod"; // the test method associated to this configuration method. Obviously, this is null for all test methods 
	private static final String RETRY = "retry";						// index of the retry
	private static final String NO_MORE_RETRY = "noMoreRetry";			// set to true when is not going to be retried
	private static final String TEST_INFO = "testInfo";
	private static final String SELENIUM_SERVER_REPORT = "seleniumServerReport";// true if the result has already been recorded to the seleniumRobot server
	private static final String SELENIUM_SERVER_REPORT_TEST_CASE_SESSION_ID = "seleniumServerReportTcsId"; // ID of the TestCaseInSession when snapshot comparison has been done
	private static final String HTML_REPORT = "htmlReport";				// true if the HTML result has already been generated
	private static final String TEST_MANAGER_REPORT = "testManagerReport";// true if the result has already been recorded on test manager
	private static final String BUGTRACKER_REPORT = "bugtrackerReport";// true if the failure has already been recorded on bugtracker
	private static final String CUSTOM_REPORT = "customReport";			// true if the custom result has already been generated
	private static final String METHOD_NAME = "methodName";				// name of the test method (or the cucumber scenario)
	private static final String SNAPSHOT_COMPARISON_RESULT = "snapshotComparisonResult";	// the result of snapshot comparison, when enabled
	private static final String DESCRIPTION = "description";			// description of the test method, if any
	private static final String ERROR_CAUSES = "errorCauses"; 			// list of causes of the test error
	private static final String ERROR_CAUSE_IN_LAST_STEP = "errorCauseInLastStep"; // true when we have searched for error cause in the last step
	private static final String ERROR_CAUSE_IN_REFERENCE = "errorCauseInReference"; // true when we have searched for error cause by comparing reference picture of the failed step
	private static final String FINISHED = "finished"; // true when all after methods has been executed

	private TestNGResultUtils() {
		// nothing to do
	}
	
	/**
	 * Copy a TestResult into an other one
	 * @param toCopy
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static ITestResult copy(ITestResult toCopy, String name, String description) throws NoSuchFieldException, IllegalAccessException {
		
		
		Field methodNameField = BaseTestMethod.class.getDeclaredField("m_methodName");
		methodNameField.setAccessible(true);
		ITestNGMethod newMethod = toCopy.getMethod().clone();
		methodNameField.set(newMethod, name);	
		newMethod.setDescription(description);

		ITestResult newTestResult = TestResult.newTestResultFrom((TestResult) toCopy, newMethod, toCopy.getTestContext(), toCopy.getStartMillis());
		newTestResult.setEndMillis(toCopy.getEndMillis());
		newTestResult.setThrowable(toCopy.getThrowable());

		newTestResult.setParameters(toCopy.getParameters());
		for (String attributeName: toCopy.getAttributeNames()) {
			newTestResult.setAttribute(attributeName, toCopy.getAttribute(attributeName));
		}
		
		// reset flags of generated results
		setHtmlReportCreated(newTestResult, false);
		setCustomReportCreated(newTestResult, false);
		setSnapshotComparisonResult(newTestResult, ITestResult.SKIP);
		
		// create testResult own context
		SeleniumTestsContext newTestContext = new SeleniumTestsContext(getSeleniumRobotTestContext(toCopy), false);
		newTestContext.createTestSpecificOutputDirectory(newTestResult);
		setSeleniumRobotTestContext(newTestResult, newTestContext);
		
		// change name
		setTestMethodName(newTestResult, getTestName(newTestResult));
		setUniqueTestName(newTestResult, newTestContext.getRelativeOutputDir());
		
		return newTestResult;
	}
	
	/**
	 * Returns the test name (will be method name for SeleniumTestPlan test and scenario name for cucumber test)
	 * @param testNGResult
	 * @return
	 */
	public static String getTestName(ITestResult testNGResult) {
		if (testNGResult == null) {
			return null;
		}
		
    	if (testNGResult.getParameters().length > 0 
    			&& testNGResult.getParameters()[0] instanceof CucumberScenarioWrapper 
//    			&& "com.seleniumtests.core.runner.CucumberTestPlan".equals(testNGResult.getMethod().getTestClass().getName()) // prevents from doing unit tests
    			) {
			return testNGResult.getParameters()[0].toString();
    		
    	} else {
    		// issue #137: in case we are in a BeforeMethod, take class name and method name from TestMethod
    		if (testNGResult.getMethod().isBeforeMethodConfiguration()) {
    			Method testMethod = (Method)(testNGResult.getParameters()[0]);
    			return "before-" + testMethod.getName();
    		} else {
    			return testNGResult.getMethod().getMethodName();
    		}
    	}
	}
	
	/**
	 * Returns a "visual" name for this test.
	 * In case "testName" is not specified for @Test annotation, then, we return the "unique test name"
	 * If "testName" is specified, then, we return it, possibly, interpolating string
	 * @param testNGResult
	 * @return
	 */
	public static String getVisualTestName(ITestResult testNGResult) {
		if (testNGResult == null) {
			return null;
		}
		
		Test testAnnotation = testNGResult.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
		String testName = getUniqueTestName(testNGResult);
		if (testAnnotation != null && !testAnnotation.testName().isEmpty()) {
			int i = 0;
	    	for (Object parameter: testNGResult.getParameters()) {
	    		String key = String.format("arg%d", i++);
	    		getSeleniumRobotTestContext(testNGResult).getConfiguration().put(key, new TestVariable(key, parameter.toString()));
	    	}
	    	testName = StringUtility.interpolateString(testAnnotation.testName(), getSeleniumRobotTestContext(testNGResult));
		} 
		
		// store it for usage in reports (velocity access the attribute directly)
		testNGResult.setAttribute(VISUAL_TEST_NAME, testName);

		return testName;
	}
	
    /**
     * Generate a String which is unique for each combination of suite/testNG test/class/test method/parameters
     * @return
     */
    public static String getHashForTest(ITestResult testNGResult) {

    	String uniqueIdentifier;
    	if (testNGResult != null) {
    		
    		String suiteName = testNGResult.getTestContext().getSuite().getName();
    		String xmlTestName = testNGResult.getTestContext().getName();
			String className = testNGResult.getMethod().getRealClass().getName();
			String testMethodName = getTestName(testNGResult);
			Object[] testMethodParams = testNGResult.getParameters();
			
    		// issue #137: in case we are in a BeforeMethod, take class name and method name from TestMethod
    		if (testNGResult.getMethod().isBeforeMethodConfiguration()) {
    			Method testMethod = (Method)(testNGResult.getParameters()[0]);
    			className = testMethod.getDeclaringClass().getName();
    			testMethodName = "before-" + testMethod.getName();
    			testMethodParams = testMethod.getParameters();
    		}
        	
    		String testNameModified = StringUtility.replaceOddCharsFromFileName(testMethodName);
    		
			uniqueIdentifier = suiteName
	    			+ "-" + xmlTestName
	    			+ "-" + className
	    			+ "-" + testNameModified
	    			+ "-" + Arrays.hashCode(testMethodParams)
					+ "-" + ObjectUtils.identityToString(testNGResult.getMethod()); // #626: add ID of the test method itself.
			// During a test retry, method object remains the same so that we build the same output folder when test is retried
			// During a new invocation (through invocationCount for example), method object is different among executions


    	} else {
    		uniqueIdentifier = "null-null-null-null-0";
    	}
    	
    	return uniqueIdentifier;
    }
    
    /**
     * Returns the unique test name (it's the test name as returned by {@link TestNGResultUtils.getTestName()}) plus an index depending on the execution order of the method
     * (This index is calculated in {@link SeleniumTestsContext.hashTest()}. Resulting name is the folder name and becomes the unique test name, put into testResult attributes
     * @return
     */
    public static String getUniqueTestName(ITestResult testNGResult) {
    	return (String) testNGResult.getAttribute(UNIQUE_METHOD_NAME);
    }
    
    public static void setUniqueTestName(ITestResult testNGResult, String name) {
    	testNGResult.setAttribute(UNIQUE_METHOD_NAME, name);
    }
    
    public static String getTestMethodName(ITestResult testNGResult) {
    	return (String) testNGResult.getAttribute(METHOD_NAME);
    }
    
    public static void setTestMethodName(ITestResult testNGResult, String name) {
    	testNGResult.setAttribute(METHOD_NAME, name);
    }
    
    public static ITestNGMethod getLinkedTestMethod(ITestResult testNGResult) {
    	return (ITestNGMethod) testNGResult.getAttribute(LINKED_TEST_METHOD);
    }
    
    public static void setLinkedTestMethod(ITestResult testNGResult, ITestNGMethod testMethod) {
		if (testMethod == null) {
			return;
		}
    	testNGResult.setAttribute(LINKED_TEST_METHOD, testMethod);
    }
    
    public static SeleniumTestsContext getSeleniumRobotTestContext(ITestResult testNGResult) {
    	return (SeleniumTestsContext) testNGResult.getAttribute(TEST_CONTEXT);
    }
    
    public static void setSeleniumRobotTestContext(ITestResult testNGResult, SeleniumTestsContext context) {
    	testNGResult.setAttribute(TEST_CONTEXT, context);
    }
    
    // number of retry already done
    public static Integer getRetry(ITestResult testNGResult) {
    	return (Integer) testNGResult.getAttribute(RETRY);
    }
    
    public static void setRetry(ITestResult testNGResult, Integer retry) {
    	testNGResult.setAttribute(RETRY, retry);
    }
    
    // TestCaseInSession id as stored in snapshot server
    public static Integer getSnapshotTestCaseInSessionId(ITestResult testNGResult) {
    	return (Integer) testNGResult.getAttribute(SELENIUM_SERVER_REPORT_TEST_CASE_SESSION_ID);
    }
    
    public static void setSnapshotTestCaseInSessionId(ITestResult testNGResult, Integer sessionId) {
    	testNGResult.setAttribute(SELENIUM_SERVER_REPORT_TEST_CASE_SESSION_ID, sessionId);
    }
    
    // the snapshot comparison result
    public static Integer getSnapshotComparisonResult(ITestResult testNGResult) {
    	return (Integer) testNGResult.getAttribute(SNAPSHOT_COMPARISON_RESULT);
    }
    
    public static void setSnapshotComparisonResult(ITestResult testNGResult, int result) {
    	testNGResult.setAttribute(SNAPSHOT_COMPARISON_RESULT, result);
    }
    
    // did we already recorded this result to the server
    public static boolean isSeleniumServerReportCreated(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, SELENIUM_SERVER_REPORT);
    }
    
    public static void setSeleniumServerReportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(SELENIUM_SERVER_REPORT, recordedToServer);
    }
    
    /**
     * 
     * @param testNGResult
     * @return true if the result has already been recorded to test manager
     */
    public static boolean isTestManagerReportCreated(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, TEST_MANAGER_REPORT);
    }
   
    public static void setTestManagereportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(TEST_MANAGER_REPORT, recordedToServer);
    }
    
    public static boolean isBugtrackerReportCreated(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, BUGTRACKER_REPORT);
    }

    public static void setBugtrackerReportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(BUGTRACKER_REPORT, recordedToServer);
    }
    
    /**
     * 
     * @param testNGResult
     * @return true if the HTML result has already been created for this result
     */
    public static boolean isHtmlReportCreated(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, HTML_REPORT);
    }
    
    public static void setHtmlReportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(HTML_REPORT, recordedToServer);
    }
    
    /**
     * 
     * @param testNGResult
     * @return true if the Custom result has already been created for this result
     */
    public static boolean isCustomReportCreated(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, CUSTOM_REPORT);
    }
    
    public static void setCustomReportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(CUSTOM_REPORT, recordedToServer);
    }
    
    private static boolean isReportCreated(ITestResult testNGResult, String attributeName) {
    	Boolean alreadyCreated = (Boolean) testNGResult.getAttribute(attributeName);
    	if (alreadyCreated == null) {
    		return false;
    	} else {
    		return alreadyCreated;
    	}
    }

    /**
     * whether retry will be allowed for this test
     * @param testNGResult
     * @return	true if no more retry is planned (test is finally failed) or when test is sucessful
     * 			false if test has failed and will be retried (current status is then skipped)
     * 			null if we never ask for a retry for this test
     */
    public static Boolean getNoMoreRetry(ITestResult testNGResult) {
    	return (Boolean) testNGResult.getAttribute(NO_MORE_RETRY);
    }
    
    public static void setNoMoreRetry(ITestResult testNGResult, Boolean noMoreRetry) {
    	testNGResult.setAttribute(NO_MORE_RETRY, noMoreRetry);
    }
    
    // information about test
    public static Map<String, Info> getTestInfo(ITestResult testNGResult) {
    	Map<String, Info> testInfo = (Map<String, Info>) testNGResult.getAttribute(TEST_INFO);
    	if (testInfo != null) {
    		return testInfo;
    	} else {
    		return new HashMap<>();
    	}
    }
    
    /**
     * returns test information encoded in the requested format
     * @param testNGResult
     * @param format		either "xml", "json", "html", "csv"
     * @return
     */
    public static Map<String, String> getTestInfoEncoded(ITestResult testNGResult, String format) {

    	Map<String, String> encodedTestInfos = new HashMap<>();
		for (Entry<String, Info> infoEntry: getTestInfo(testNGResult).entrySet()) {
			encodedTestInfos.put(StringUtility.encodeString(infoEntry.getKey(), format.toLowerCase()), 
									infoEntry.getValue().encode(format.toLowerCase()));
		}
		return encodedTestInfos;
    }
    
    public static void setTestInfo(ITestResult testNGResult, String key, Info value) {
    	Map<String, Info> testInfo = (Map<String, Info>) testNGResult.getAttribute(TEST_INFO);
    	if (testInfo == null) {
    		testInfo = new HashMap<>();
    	} 
    	testInfo.put(key, value);
    	testNGResult.setAttribute(TEST_INFO, testInfo);
    }
    
    /**
     * Returns the test description, interpolating variable
     * @param testNGResult
     * @return
     */
    public static String getTestDescription(ITestResult testNGResult) {
    	int i = 0;
    	for (Object parameter: testNGResult.getParameters()) {
    		String key = String.format("arg%d", i++);
    		getSeleniumRobotTestContext(testNGResult).getConfiguration().put(key, new TestVariable(key, parameter == null ? "null" : parameter.toString()));
    	}
    	String description = StringUtility.interpolateString(testNGResult.getMethod().getDescription(), getSeleniumRobotTestContext(testNGResult));
    	testNGResult.setAttribute(DESCRIPTION, description); // store it so that it's used in reports
    	return description;
    }

    /**
     * Change the test result when snapshot comparison fails
     * These comparison are done for every test execution (every retry). At this point, snapshot are not recorded on server. This will be recorded in SeleniumRobotServerTestRecorder
     * only with the last test execution.
     * @param testResult
     */
	public static void changeTestResultWithSnapshotComparison(final ITestResult testResult) {
		
		if (testResult.getStatus() == ITestResult.FAILURE  // test is already failed
				|| !Boolean.TRUE.equals(SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerActive())
				|| SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour() == SnapshotComparisonBehaviour.DISPLAY_ONLY // as the comparison result is only displayed, do not retry
				|| SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour() == SnapshotComparisonBehaviour.ADD_TEST_RESULT // complicated to set the test failed, and then success again
				|| !SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot()) {
			return;
		}
		
		SeleniumRobotSnapshotServerConnector serverConnector = SeleniumRobotSnapshotServerConnector.getInstance();
		
		List<TestStep> testSteps = getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
		if (testSteps == null) {
			return;
		}
		
		for (TestStep testStep: testSteps) {
			for (Snapshot snapshot: new ArrayList<>(testStep.getSnapshots())) {
				if (snapshot.getCheckSnapshot().recordSnapshotOnServerForComparison()) {
					if (snapshot.getName() == null || snapshot.getName().isEmpty()) {
						logger.warn("Snapshot hasn't any name, it won't be sent to server");
						continue;
					} 
					
					try {
						SnapshotComparisonResult comparisonResult = serverConnector.checkSnapshotHasNoDifferences(snapshot, CommonReporter.getTestCaseName(testResult), testStep.getName());
						if (comparisonResult == SnapshotComparisonResult.KO) {
							testResult.setStatus(ITestResult.FAILURE);
							testResult.setThrowable(new ScenarioException("Snapshot comparison failed"));
							
							// move test from passedTests to failedTests if test is not already in failed tests
							if (testResult.getTestContext().getPassedTests().getAllMethods().contains(testResult.getMethod())) {
								testResult.getTestContext().getPassedTests().removeResult(testResult);
								testResult.getTestContext().getFailedTests().addResult(testResult);
							}
							return;
						}
					} catch (SeleniumRobotServerException e) {
						logger.error("Could not create snapshot on server", e);
					}
				}
			}
		}
		


	}	
	

	/**
	 * In case test result is SUCCESS but some softAssertions were raised, change test result to 
	 * FAILED
	 * 
	 * @param result
	 */
	public static void changeTestResultWithSoftAssertion(ITestResult result) {
		List<Throwable> verificationFailures = SeleniumTestsContextManager.getThreadContext().getVerificationFailures(Reporter.getCurrentTestResult());

		int size = verificationFailures.size();
		if (size == 0 || result.getStatus() == ITestResult.FAILURE) {
			return;
		}

		result.setStatus(ITestResult.FAILURE);

		if (size == 1) {
			result.setThrowable(verificationFailures.get(0));
		} else {
			
			StringBuilder stackString = new StringBuilder("!!! Many Test Failures (").append(size).append(")\n\n");
			
			for (int i = 0; i < size - 1; i++) {
				ExceptionUtility.generateTheStackTrace(verificationFailures.get(i), String.format("%n.%nFailure %d of %d%n", i + 1, size), stackString, "text");
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
			result.getTestContext().getFailedTests().addResult(result);
		}
	}	
	

    @SuppressWarnings("unchecked")
	public static List<ErrorCause> getErrorCauses(ITestResult testNGResult) {
    	if (testNGResult.getAttribute(ERROR_CAUSES) == null) {
    		return new ArrayList<>();
    	} else {
    		return (List<ErrorCause>) testNGResult.getAttribute(ERROR_CAUSES);
    	}
    }
    
    /**
     * Store the list of detected error causes
     * @param testNGResult
     * @param errorCauses
     */
    public static void setErrorCauses(ITestResult testNGResult, List<ErrorCause> errorCauses) {
    	testNGResult.setAttribute(ERROR_CAUSES, errorCauses);
    }
    
    public static boolean isErrorCauseSearchedInLastStep(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, ERROR_CAUSE_IN_LAST_STEP);
    }
    
    public static void setErrorCauseSearchedInLastStep(ITestResult testNGResult, Boolean errorCauseInLastStep) {
    	testNGResult.setAttribute(ERROR_CAUSE_IN_LAST_STEP, errorCauseInLastStep);
    }
    
    public static boolean isErrorCauseSearchedInReferencePicture(ITestResult testNGResult) {
    	return isReportCreated(testNGResult, ERROR_CAUSE_IN_REFERENCE);
    }
    
    public static void setErrorCauseSearchedInReferencePicture(ITestResult testNGResult, Boolean errorCauseInReferencePicture) {
    	testNGResult.setAttribute(ERROR_CAUSE_IN_REFERENCE, errorCauseInReferencePicture);
    }

	public static void setFinished(ITestResult testNGResult, Boolean finished) {
		testNGResult.setAttribute(FINISHED, finished);
	}

	public static boolean isFinished(ITestResult testNGResult) {
		Boolean finished = (Boolean) testNGResult.getAttribute(FINISHED);
		return finished == null ? false: finished;
	}

	/**
	 * Returns the string representation of the status: SUCCESS, ERROR, SKIPPED, ...
	 * @param testNGResult
	 * @return
	 */
	public static String getTestStatusString(ITestResult testNGResult) {

		switch (testNGResult.getStatus()) {
			case -1:
				return "CREATED";
			case 1:
				return "SUCCESS";
			case 2:
				return "FAILURE";
			case 3:
				return "SKIP";
			case 4:
				return "SUCCESS_PERCENTAGE_FAILURE";
			case 16:
				return "STARTED";
			default:
				return "UNKNOWN";
		}
	}
}
