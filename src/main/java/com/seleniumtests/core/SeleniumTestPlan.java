/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

package com.seleniumtests.core;

import java.io.IOException;

import java.lang.reflect.Method;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import org.testng.ITestContext;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

import org.testng.xml.XmlTest;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;

/**
 * This class initializes context, sets up and tears down and clean up drivers An STF test should extend this class.
 */
public abstract class SeleniumTestPlan {
    private static final Logger logger = TestLogging.getLogger(SeleniumTestPlan.class);
    private Date start;

    @BeforeSuite(alwaysRun = true)
    public void beforeTestSuite(final ITestContext testContext) throws IOException {
        start = new Date();
        SeleniumTestsContextManager.initGlobalContext(testContext);
        SeleniumTestsContextManager.initThreadContext(testContext, null);
    }

    /**
     * Configure Test Params setting.
     *
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTest(final ITestContext testContext, final XmlTest xmlTest) {
        SeleniumTestsContextManager.initTestLevelContext(testContext, xmlTest);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeTestMethod(final Object[] parameters, final Method method, final ITestContext testContex,
            final XmlTest xmlTest) {
        logger.info(Thread.currentThread() + " Start method " + method.getName());
        SeleniumTestsContextManager.initThreadContext(testContex, xmlTest);
        SeleniumTestsContextManager.getThreadContext().setTestMethodSignature(buildMethodSignature(method, parameters));
    }

    @AfterSuite(alwaysRun = true)
    public void afterTestSuite() {
        logger.info("Test Suite Execution Time: " + (new Date().getTime() - start.getTime()) / 1000 / 60 + " minutes.");
    }

    /**
     * clean up.
     */
    @AfterMethod(alwaysRun = true)
    public void afterTestMethod(final Object[] parameters, final Method method, final ITestContext testContex,
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
     * @return	parameters as string
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
