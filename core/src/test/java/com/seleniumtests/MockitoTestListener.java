package com.seleniumtests;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.video.VideoCaptureMode;
import org.htmlunit.xpath.operations.Bool;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class MockitoTestListener implements ITestListener, IInvokedMethodListener {

    private static ThreadLocal<Boolean> mocksInitialized = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult testResult) {
        if (System.getProperty("mockTestExecutionMethod") != null && mocksInitialized.get() == null) {
            initMocks();
        }
    }

    private static void initMocks() {

        try {
            String classAndMethod = System.getProperty("mockTestExecutionMethod");
            Class clazz = Class.forName(classAndMethod.split("#")[0]);
            Method initMethod = clazz.getDeclaredMethod(classAndMethod.split("#")[1]);
            initMethod.invoke(clazz.getConstructor().newInstance());
            mocksInitialized.set(true);
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize mock", e);
        }
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
        if (System.getProperty("mockTestExecutionMethod") != null && mocksInitialized.get() == null) {
//            initThreadContext(context, testResult);
            initMocks();
        }
    }

    public void initThreadContext(final ITestContext testNGCtx, final ITestResult testResult) {
        SeleniumTestsContextManager.initGlobalContext(testNGCtx);
        SeleniumTestsContextManager.initThreadContext(testNGCtx, testResult);
        SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
        SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
        SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
    }
}
