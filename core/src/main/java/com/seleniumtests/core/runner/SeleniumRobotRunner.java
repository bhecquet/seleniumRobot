/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import com.mashape.unirest.http.Unirest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TearDownService;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

@Listeners({com.seleniumtests.reporter.SeleniumTestsReporter2.class, 
			com.seleniumtests.reporter.TestListener.class, 
			com.seleniumtests.core.testretry.TestRetryListener.class, 
			com.seleniumtests.reporter.PerformanceReporter.class,
			com.seleniumtests.reporter.SeleniumRobotServerTestRecorder.class,
			com.seleniumtests.reporter.TestManagerReporter.class,
			com.seleniumtests.reporter.JsonReporter.class})
public class SeleniumRobotRunner {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotRunner.class);
	private static Map<Thread, Boolean> cucumberTest = Collections.synchronizedMap(new HashMap<>());
	private Date start;

	public static boolean isCucumberTest() {
		Boolean isCucumberT = SeleniumRobotRunner.cucumberTest.get(Thread.currentThread());
		if (isCucumberT == null) {
			return false;
		}
		return isCucumberT;
	}
	
	/**
	 * Configure cucumberTest in case we use several threads. This should be called inside the thread running the test method
	 */
	public void configureCucumberTest() {
		setCucumberTest(false);
	}
	
	public static void setCucumberTest(boolean cucumberTestIn) {
		SeleniumRobotRunner.cucumberTest.put(Thread.currentThread(), cucumberTestIn);
	}
	
	protected String buildMethodSignature(final Method method, final Object[] parameters) {
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
	protected String buildParameterString(final Object[] parameters) {
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
	
	/**
     * clean up.
     */
    @AfterMethod(alwaysRun = true)
    public void afterTestMethod(final Method method, final ITestResult result) {
        List<TearDownService> serviceList = SeleniumTestsContextManager.getThreadContext().getTearDownServices();
        if (serviceList != null && !serviceList.isEmpty()) {
            for (TearDownService service : serviceList) {
                service.tearDown();
            }
        }

        WebUIDriver.cleanUp();
        logger.info(SeleniumRobotLogger.END_TEST_PATTERN + method.getName());
    }
    
    /**
     * Reinitialize SeleniumTestContext before each test so that we have a context 
     * corresponding to parameters owned by this test and not an other one
     * @param testContext
     * @throws IOException
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTest(final ITestContext testContext) {
        start = new Date();
        SeleniumTestsContextManager.initGlobalContext(testContext);
        SeleniumTestsContextManager.initThreadContext(testContext, null);  
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterTestSuite() {
        logger.info("Test Suite Execution Time: " + (new Date().getTime() - start.getTime()) / 1000 / 60 + " minutes.");
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
    
    @BeforeSuite(alwaysRun = true)
    public void showVersion(final ITestContext testContext) {
    	SeleniumTestsContextManager.initGlobalContext(testContext);
    	logger.info(String.format("Application %s version: %s", SeleniumTestsContextManager.getApplicationName(), SeleniumTestsContextManager.getApplicationVersion()));
    	logger.info("Core version: " + SeleniumTestsContextManager.getCoreVersion());
    	
    	SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(),
										SeleniumTestsContextManager.getGlobalContext().getDefaultOutputDirectory());
    }
    
    @BeforeMethod(alwaysRun = true)
    public void beforeTestMethod(final Object[] parameters, final Method method, final ITestContext testContext) {
    	configureCucumberTest();
    	if (!isCucumberTest()) {
	        logger.info(SeleniumRobotLogger.START_TEST_PATTERN + method.getName());
	        SeleniumTestsContextManager.initThreadContext(testContext, method.getName());
	        SeleniumTestsContextManager.getThreadContext().setTestMethodSignature(buildMethodSignature(method, parameters));
    	}
    }  
    
    /**
     * Get parameter from configuration
     * 
     * @param key
     * 
     * @return String
     */
    public static String param(String key) {
    	return PageObject.param(key);
    }
}
