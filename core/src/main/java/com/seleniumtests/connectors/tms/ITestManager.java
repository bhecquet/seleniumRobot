package com.seleniumtests.connectors.tms;

import org.json.JSONObject;
import org.testng.ITestResult;

public interface ITestManager {

    void recordResult();

    void recordResult(ITestResult testResult);

    void recordResultFiles();

    void recordResultFiles(ITestResult testResult);

    void login();

    void init(JSONObject connectParams);

    void logout();

    String getType();

    Integer getTestCaseId(ITestResult testNGResult);

    Integer getDatasetId(ITestResult testNGResult);

    /**
     * returns the URL to access the test case
     */
    String getTestCaseUrl(ITestResult testResult);
}
