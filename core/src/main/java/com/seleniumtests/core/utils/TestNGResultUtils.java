/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import java.util.Arrays;

import org.testng.ITestResult;

import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.util.StringUtility;

public class TestNGResultUtils {

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
    		return testNGResult.getMethod().getMethodName();
    	}
	}
	
    /**
     * Generate a String which is unique for each combination of suite/testNG test/class/test method/parameters
     * @return
     */
    public static String getHashForTest(ITestResult testNGResult) {

    	String uniqueIdentifier;
    	if (testNGResult != null) {
    		
        	
    		String testNameModified = StringUtility.replaceOddCharsFromFileName(getTestName(testNGResult));
    		
    		uniqueIdentifier = testNGResult.getTestContext().getSuite().getName()
	    			+ "-" + testNGResult.getTestContext().getName()
	    			+ "-" + testNGResult.getMethod().getRealClass().getName()
	    			+ "-" + testNameModified
	    			+ "-" + Arrays.hashCode(testNGResult.getParameters());
    	} else {
    		uniqueIdentifier = "null-null-null-null-0";
    	}
    	
    	return uniqueIdentifier;
    }
}
