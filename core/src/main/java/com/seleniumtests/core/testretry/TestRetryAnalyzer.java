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

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.reporter.logger.TestLogging;

public class TestRetryAnalyzer implements IRetryAnalyzer {

    private Integer count = 0;
    private int maxCount = 2;

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
    	result.setAttribute(SeleniumRobotTestListener.RETRY, count);
    	result.setAttribute(SeleniumRobotTestListener.NO_MORE_RETRY, false);

        if (count < maxCount) {
        	
        	count++;
        	if (result.getThrowable() instanceof AssertionError) {
        		TestLogging.log("[NOT RETRYING] due to failed Assertion");
            	result.setAttribute(SeleniumRobotTestListener.NO_MORE_RETRY, true);
        		return false;
        	}
        	
	        TestLogging.log("[RETRYING] " + testClassName + " FAILED, " + "Retrying " + count + " time");
            return true;
        } 
        count++;
        
        TestLogging.log(String.format("[NOT RETRYING] max retry count (%d) reached", maxCount));

        return false;
    }

    
  
    
    /**
     * check whether the test will be retried / is retrying by comparing the count indiactor stored in test result with the max allowed retry count
     */
    public boolean retryPeek(final ITestResult result) {
    	Integer currentRetry = (Integer) result.getAttribute(SeleniumRobotTestListener.RETRY);
    	Boolean noMoreRetry = (Boolean) result.getAttribute(SeleniumRobotTestListener.NO_MORE_RETRY);
    	if (currentRetry == null || (noMoreRetry != null && noMoreRetry)) {
    		return false;
    	}
    	
    	return currentRetry < maxCount;
    }
}
