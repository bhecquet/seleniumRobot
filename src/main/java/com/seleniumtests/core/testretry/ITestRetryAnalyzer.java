package com.seleniumtests.core.testretry;

import org.testng.ITestResult;


public interface ITestRetryAnalyzer {
    boolean retryPeek(ITestResult var1);

    int getCount();
}
