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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TearDownService;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;

import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.runtime.CucumberException;

public class CucumberRunner {
	
	private static final Logger logger = TestLogging.getLogger(CucumberRunner.class);
    private Date start;
	private CustomTestNGCucumberRunner testNGCucumberRunner;
    
    @BeforeSuite(alwaysRun = true)
    public void beforeTestSuite(final ITestContext testContext) throws IOException {
        start = new Date();
        SeleniumTestsContextManager.initGlobalContext(testContext);
        SeleniumTestsContextManager.initThreadContext(testContext);
    }
    
    /**
     * Configure Test Params setting.
     *
     * @param  xmlTest
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTest(final ITestContext testContext, final XmlTest xmlTest) {
    	try {
	        SeleniumTestsContextManager.initTestLevelContext(testContext, xmlTest);
	        testNGCucumberRunner = new CustomTestNGCucumberRunner(this.getClass());
    	} catch (Exception e) {
    		logger.error(Thread.currentThread() + " Error on init: ", e);
    		for (StackTraceElement s : e.getStackTrace()) {
    			logger.error(Thread.currentThread() + " " + s.toString());
    		}
    	}
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeTestMethod(final Object[] parameters, final Method method, final ITestContext testContex) {
        logger.info(Thread.currentThread() + " Start method " + method.getName());
        SeleniumTestsContextManager.initThreadContext(testContex);
        SeleniumTestsContextManager.getThreadContext().setTestMethodSignature(buildMethodSignature(method, parameters));
    }

    @Test(groups = "cucumber", description = "Cucumber scenario", dataProvider = "scenarios")
    public void feature(CucumberScenarioWrapper cucumberScenarioWrapper) throws CucumberException {
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

    @AfterSuite(alwaysRun = true)
    public void afterTestSuite() {
        logger.info("Test Suite Execution Time: " + (new Date().getTime() - start.getTime()) / 1000 / 60 + " minutes.");
    }

    /**
     * clean up.
     *
     * @param  parameters
     * @param  method
     * @param  testContex
     * @param  xmlTest
     */
    @AfterMethod(alwaysRun = true)
    public void afterTestMethod(final Method method, final ITestContext testContex,
            final XmlTest xmlTest) {
        List<TearDownService> serviceList = SeleniumTestsContextManager.getThreadContext().getTearDownServices();
        if (serviceList != null && !serviceList.isEmpty()) {
            for (TearDownService service : serviceList) {
                service.tearDown();
            }
        }

        WebUIDriver.cleanUp();
        logger.info(Thread.currentThread() + " Finish method " + method.getName());
    }

    private String buildMethodSignature(final Method method, final Object[] parameters) {
        return method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "("
                + buildParameterString(parameters) + ")";
    }

    /**
     * Remove name space from parameters.
     *
     * @param   parameters
     *
     * @return
     */
    private String buildParameterString(final Object[] parameters) {
        StringBuilder parameter = new StringBuilder();

        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == null) {
                    parameter.append("null, ");
                } else if (parameters[i] instanceof java.lang.String) {
                    parameter.append("\"").append(parameters[i]).append("\", ");
                } else {
                    parameter.append(parameters[i]).append(", ");
                }
            }
        }

        if (parameter.length() > 0) {
            parameter.delete(parameter.length() - 2, parameter.length() - 1);
        }

        return parameter.toString();
    }
}


