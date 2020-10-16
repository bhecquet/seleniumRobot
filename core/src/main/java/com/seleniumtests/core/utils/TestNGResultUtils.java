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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.reporter.logger.StringInfo;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestNGResultUtils {

	protected static Logger logger = SeleniumRobotLogger.getLogger(TestNGResultUtils.class);

	private static final String UNIQUE_METHOD_NAME = "uniqueMethodName"; // unique name of the test (in case several tests have the same name)
	private static final String TEST_CONTEXT = "testContext";
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
	public static ITestResult copy(ITestResult toCopy, String name, String description) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		
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
		setSnapshotComparisonResult(newTestResult, false);
		
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
		
    	if (testNGResult.getParameters().length == 1 
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
	    			+ "-" + Arrays.hashCode(testMethodParams);
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
    public static Boolean getSnapshotComparisonResult(ITestResult testNGResult) {
    	return (Boolean) testNGResult.getAttribute(SNAPSHOT_COMPARISON_RESULT);
    }
    
    public static void setSnapshotComparisonResult(ITestResult testNGResult, Boolean result) {
    	testNGResult.setAttribute(SNAPSHOT_COMPARISON_RESULT, result);
    }
    
    // did we already recorded this result to the server
    public static boolean isSeleniumServerReportCreated(ITestResult testNGResult) {
    	Boolean alreadyRecorded = (Boolean) testNGResult.getAttribute(SELENIUM_SERVER_REPORT);
    	if (alreadyRecorded == null) {
    		return false;
    	} else {
    		return alreadyRecorded;
    	}
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
    	Boolean alreadyCreated = (Boolean) testNGResult.getAttribute(TEST_MANAGER_REPORT);
    	if (alreadyCreated == null) {
    		return false;
    	} else {
    		return alreadyCreated;
    	}
    }
   
    public static void setTestManagereportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(TEST_MANAGER_REPORT, recordedToServer);
    }
    
    public static boolean isBugtrackerReportCreated(ITestResult testNGResult) {
    	Boolean alreadyCreated = (Boolean) testNGResult.getAttribute(BUGTRACKER_REPORT);
    	if (alreadyCreated == null) {
    		return false;
    	} else {
    		return alreadyCreated;
    	}
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
    	Boolean alreadyCreated = (Boolean) testNGResult.getAttribute(HTML_REPORT);
    	if (alreadyCreated == null) {
    		return false;
    	} else {
    		return alreadyCreated;
    	}
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
    	Boolean alreadyCreated = (Boolean) testNGResult.getAttribute(CUSTOM_REPORT);
    	if (alreadyCreated == null) {
    		return false;
    	} else {
    		return alreadyCreated;
    	}
    }
    
    public static void setCustomReportCreated(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(CUSTOM_REPORT, recordedToServer);
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
    public static Map<String, StringInfo> getTestInfo(ITestResult testNGResult) {
    	Map<String, StringInfo> testInfo = (Map<String, StringInfo>) testNGResult.getAttribute(TEST_INFO);
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
		for (Entry<String, StringInfo> infoEntry: getTestInfo(testNGResult).entrySet()) {
			encodedTestInfos.put(StringUtility.encodeString(infoEntry.getKey(), format.toLowerCase()), 
									infoEntry.getValue().encode(format.toLowerCase()));
		}
		return encodedTestInfos;
    }
    
    public static void setTestInfo(ITestResult testNGResult, String key, StringInfo value) {
    	Map<String, StringInfo> testInfo = (Map<String, StringInfo>) testNGResult.getAttribute(TEST_INFO);
    	if (testInfo == null) {
    		testInfo = new HashMap<>();
    	} 
    	testInfo.put(key, value);
    	testNGResult.setAttribute(TEST_INFO, testInfo);
    }
    
    /**
     * Returns the ID of the test case for this test result or null if it's not defined
     * It assumes that test method has been annotated with 'testId' custom attribute {@code @Test(attributes = {@CustomAttribute(name = "testId", values = "12")})} 
     */
    public static Integer getTestCaseId(ITestResult testNGResult) {
    	for (CustomAttribute customAttribute: testNGResult.getMethod().getAttributes()) {
    		if ("testId".equals(customAttribute.name()) && customAttribute.values().length > 0) {
    			try {
    				return Integer.parseInt(customAttribute.values()[0]);
    			} catch (NumberFormatException e) {
    				logger.error(String.format("Could not parse %s as int for getting testId of test method %s", customAttribute.values()[0], testNGResult.getMethod().getMethodName()));
    			}
    		}
    	}
    	return null;
    	
    }
}
