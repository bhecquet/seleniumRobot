/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.seleniumtests.core.runner;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import cucumber.api.testng.CucumberFeatureWrapper;

public class CucumberRunner extends SeleniumRobotRunner {
	
    
	private CustomTestNGCucumberRunner testNGCucumberRunner;
    
    /**
     * Configure Test Params setting.
     *
     * @param  xmlTest
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTest() {
    	try {
	        testNGCucumberRunner = new CustomTestNGCucumberRunner(this.getClass());
    	} catch (Exception e) {
    		logger.error(Thread.currentThread() + " Error on init: ", e);
    		for (StackTraceElement s : e.getStackTrace()) {
    			logger.error(Thread.currentThread() + " " + s.toString());
    		}
    	}
    	SeleniumRobotRunner.setCucumberTest(true);
    }

    @Test(groups = "cucumber", description = "Cucumber scenario", dataProvider = "scenarios")
    public void feature(CucumberScenarioWrapper cucumberScenarioWrapper) {
    	testNGCucumberRunner.runScenario(cucumberScenarioWrapper);
    	logger.info(Thread.currentThread() + "Start scenario: " + cucumberScenarioWrapper);
    }

    /**
     * @return returns two dimensional array of {@link CucumberFeatureWrapper} objects.
     */
    @DataProvider
    public Object[][] scenarios() {
        return testNGCucumberRunner.provideScenarios();
    }

    @AfterTest
    public void tearDown() {
        testNGCucumberRunner.finish();
    }

}


