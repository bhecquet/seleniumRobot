package com.seleniumtests.core.runner;

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

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TearDownService;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;

public class SeleniumRobotRunner {
	
	protected static final Logger logger = TestLogging.getLogger(SeleniumRobotRunner.class);
	private static boolean cucumberTest = false;
	private Date start;

	public static boolean isCucumberTest() {
		return cucumberTest;
	}
	
	public static void setCucumberTest(boolean cucumberTest) {
		SeleniumRobotRunner.cucumberTest = cucumberTest;
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
    public void afterTestMethod(final Method method) {
        List<TearDownService> serviceList = SeleniumTestsContextManager.getThreadContext().getTearDownServices();
        if (serviceList != null && !serviceList.isEmpty()) {
            for (TearDownService service : serviceList) {
                service.tearDown();
            }
        }

        WebUIDriver.cleanUp();
        logger.info(Thread.currentThread() + " Finish method " + method.getName());
    }
    
    @BeforeSuite(alwaysRun = true)
    public void beforeTestSuite(final ITestContext testContext) throws IOException {
        start = new Date();
        SeleniumTestsContextManager.initGlobalContext(testContext);
        SeleniumTestsContextManager.initThreadContext(testContext);
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterTestSuite() {
        logger.info("Test Suite Execution Time: " + (new Date().getTime() - start.getTime()) / 1000 / 60 + " minutes.");
    }
    
    @BeforeMethod(alwaysRun = true)
    public void beforeTestMethod(final Object[] parameters, final Method method, final ITestContext testContex) {
        logger.info(Thread.currentThread() + " Start method " + method.getName());
        SeleniumTestsContextManager.initThreadContext(testContex);
        SeleniumTestsContextManager.getThreadContext().setTestMethodSignature(buildMethodSignature(method, parameters));
    }   
}
