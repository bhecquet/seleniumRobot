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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.runner.cucumber.CustomCucumberPropertiesProvider;

import io.cucumber.testng.CucumberPropertiesProvider;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;

/**
 * This class initializes context, sets up and tears down and clean up drivers An STF test should extend this class.
 */

public class CucumberTestPlan extends SeleniumRobotTestPlan {
	

    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass(ITestContext context) {
        CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(context.getCurrentXmlTest());
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass(), properties);
		setCucumberTest(true);
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void runScenario(CucumberScenarioWrapper scenarioWrapper) {
        testNGCucumberRunner.runScenario(scenarioWrapper.getPickle());
    }

    @DataProvider
    public Object[][] scenarios() throws IOException {
    	if (testNGCucumberRunner == null) {
            return new Object[0][0];
        }
    	Object[][] scenariosTmp = testNGCucumberRunner.provideScenarios();
    	
    	List<Object[]> scenarios = new ArrayList<>();
    	for (Object[] paramsForTest: scenariosTmp) {
    		scenarios.add(new Object[] {new CucumberScenarioWrapper((PickleWrapper)paramsForTest[0], (FeatureWrapper)paramsForTest[1])});
    	}
    	Object[][] result = new Object[scenariosTmp.length][];
    	return scenarios.toArray(result);
    	
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
    	if (testNGCucumberRunner == null) {
            return;
        }
        testNGCucumberRunner.finish();
    }


	
}
