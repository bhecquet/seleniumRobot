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
package com.seleniumtests.core.testretry;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ApplicationError;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.SeleniumGridNodeNotAvailable;
import com.seleniumtests.util.logging.ScenarioLogger;

public class TestRetryAnalyzer implements IRetryAnalyzer {
	
	private static ScenarioLogger logger = ScenarioLogger.getScenarioLogger(TestRetryAnalyzer.class);

    private Integer count = 0;
    private int maxCount;


    public TestRetryAnalyzer() {

        // #637: be sure that retry analyzer max count is correctly set
        // in fact, testRestryAnalyzer is created when we call the "getRetryAnalyzer" method and uses the default contructor
    	maxCount = SeleniumTestsContextManager.getThreadContext().getTestRetryCount();
    }
    
    public TestRetryAnalyzer(int maxRetryCount) {
    	maxCount = maxRetryCount;
    }

    public void setMaxCount(final int count) {
        this.maxCount = count;
    }

    public int getCount() {
        return this.count;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    /**
     * Retry logic. We retry a test if
     * - it has not reached the maximum retry number
     * - the error of the test is not an AssertionError. These are functional errors and so should not be retried
     */
    @Override
    public synchronized boolean retry(final ITestResult result) {
    	String testClassName = String.format("%s.%s", result.getMethod().getRealClass().toString(),
                result.getMethod().getMethodName());
    	TestNGResultUtils.setRetry(result, count);
    	TestNGResultUtils.setNoMoreRetry(result, false);

        if (count < maxCount) {
        	
        	count++;
        	if (result.getThrowable() instanceof AssertionError) {
        		logger.log("[NOT RETRYING] due to failed Assertion");
            	TestNGResultUtils.setNoMoreRetry(result, true); 
        		return false;
        	} else if (result.getThrowable() instanceof SeleniumGridNodeNotAvailable) {
        		logger.log("[NOT RETRYING] due to grid node not available");
            	TestNGResultUtils.setNoMoreRetry(result, true);
        		return false;
            } else if (result.getThrowable() instanceof ApplicationError) {
        		logger.log("[NOT RETRYING] due to application error");
            	TestNGResultUtils.setNoMoreRetry(result, true);
        		return false;
        	}
        	
        	logger.log("[RETRYING] " + testClassName + " FAILED, " + "Retrying " + count + " time");
            return true;
        } 
        count++;
        
        logger.log(String.format("[NOT RETRYING] max retry count (%d) reached", maxCount));


    	TestNGResultUtils.setNoMoreRetry(result, true);
        return false;
    }

    /**
     * check whether the test will be retried / is retrying by comparing the count indiactor stored in test result with the max allowed retry count
     * Retry count is set only when retry method has been called
     * @return	true if the test is going to be retried
     */
    public boolean willBeRetried(final ITestResult result) {
    	Integer currentRetry = TestNGResultUtils.getRetry(result);
    	Boolean noMoreRetry = TestNGResultUtils.getNoMoreRetry(result);
    	if (currentRetry == null || (noMoreRetry != null && noMoreRetry)) {
    		return false;
    	}
    	
    	return currentRetry < maxCount;
    }
}
