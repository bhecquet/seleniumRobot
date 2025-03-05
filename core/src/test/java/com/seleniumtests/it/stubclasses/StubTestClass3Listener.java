package com.seleniumtests.it.stubclasses;


import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.customexception.ApplicationError;
import com.seleniumtests.util.logging.ScenarioLogger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class StubTestClass3Listener implements IInvokedMethodListener {

    private static ScenarioLogger scenarioLogger = ScenarioLogger.getScenarioLogger(StubTestClass3Listener.class);
    /**
     * This afterInvocation aims at raising ApplicationError when error occurs in 'testWithApplicationError'
     * This will mimic the user behaviour where, on error raised during test, an analysis on page shows that this is due to application
     * (e.g: error message displayed)
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

        if ("testWithApplicationError".equals(method.getTestMethod().getMethodName())) {
            scenarioLogger.error("Application failed");
            testResult.setThrowable(new ApplicationError("error on application"));
        }
    }

}
