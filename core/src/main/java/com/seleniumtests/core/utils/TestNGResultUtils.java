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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.ITestResult;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.reporter.logger.StringInfo;
import com.seleniumtests.util.StringUtility;

public class TestNGResultUtils {
	

	private static final String UNIQUE_METHOD_NAME = "uniqueMethodName"; // unique name of the test (in case several tests have the same name)
	private static final String TEST_CONTEXT = "testContext";
	private static final String RETRY = "retry";						// index of the retry
	private static final String NO_MORE_RETRY = "noMoreRetry";			// set to true when is not going to be retried
	private static final String TEST_INFO = "testInfo";
	private static final String RECORDED_TO_SERVER = "recordedToServer";// true if the result has already been recorded to the seleniumRobot server
	private static final String METHOD_NAME = "methodName";				// name of the test method (or the cucumber scenario)

	private TestNGResultUtils() {
		// nothing to do
	}
	
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
    
    // did we already recorded this result to the server
    public static boolean isRecordedToServer(ITestResult testNGResult) {
    	Boolean alreadyRecorded = (Boolean) testNGResult.getAttribute(RECORDED_TO_SERVER);
    	if (alreadyRecorded == null) {
    		return false;
    	} else {
    		return alreadyRecorded;
    	}
    }
    
    public static void setRecordedToServer(ITestResult testNGResult, Boolean recordedToServer) {
    	testNGResult.setAttribute(RECORDED_TO_SERVER, recordedToServer);
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
}
