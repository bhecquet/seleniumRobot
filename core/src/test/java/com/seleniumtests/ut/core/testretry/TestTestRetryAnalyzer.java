package com.seleniumtests.ut.core.testretry;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ApplicationError;
import com.seleniumtests.customexception.CannotRunOnSameSeleniumGridNodeException;
import com.seleniumtests.customexception.SeleniumGridNodeNotAvailable;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestTestRetryAnalyzer extends GenericTest {

    @Test(groups = "ut")
    public void testRetry() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        Assert.assertTrue(testRetryAnalyzer.retry(testResult));
        Assert.assertTrue(testRetryAnalyzer.retry(testResult));
        Assert.assertFalse(testRetryAnalyzer.retry(testResult));
    }

    @Test(groups = "ut")
    public void testRetryAssertionException() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        testResult.setThrowable(new AssertionError());
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        Assert.assertFalse(testRetryAnalyzer.retry(testResult));
    }

    @Test(groups = "ut")
    public void testRetryApplicationError() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        testResult.setThrowable(new ApplicationError("error in application"));
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        Assert.assertFalse(testRetryAnalyzer.retry(testResult));
    }

    @Test(groups = "ut")
    public void testRetrySeleniumGridNodeNotAvailable() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        testResult.setThrowable(new SeleniumGridNodeNotAvailable("no node found"));
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        Assert.assertFalse(testRetryAnalyzer.retry(testResult));
    }

    @Test(groups = "ut")
    public void testRetryCannotRunOnSameSeleniumGridNodeException() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        testResult.setThrowable(new CannotRunOnSameSeleniumGridNodeException("cannot attach to driver"));
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        Assert.assertTrue(testRetryAnalyzer.retry(testResult));
    }

    @Test(groups = "ut")
    public void testWillBeRetried() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        TestNGResultUtils.setRetry(testResult, 1);
        TestNGResultUtils.setNoMoreRetry(testResult, false);
        Assert.assertTrue(testRetryAnalyzer.willBeRetried(testResult));
    }

    /**
     * max count reached => no retry
     */
    @Test(groups = "ut")
    public void testWillNotBeRetried() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(1);
        TestNGResultUtils.setRetry(testResult, 1);
        TestNGResultUtils.setNoMoreRetry(testResult, false);
        Assert.assertFalse(testRetryAnalyzer.willBeRetried(testResult));
    }

    /**
     * max count not reached but noMoreRetry set to true
     */
    @Test(groups = "ut")
    public void testWillBeNotRetried2() {
        ITestResult testResult = Reporter.getCurrentTestResult();
        TestRetryAnalyzer testRetryAnalyzer = new TestRetryAnalyzer(2);
        TestNGResultUtils.setRetry(testResult, 1);
        TestNGResultUtils.setNoMoreRetry(testResult, true);
        Assert.assertFalse(testRetryAnalyzer.willBeRetried(testResult));
    }
}
