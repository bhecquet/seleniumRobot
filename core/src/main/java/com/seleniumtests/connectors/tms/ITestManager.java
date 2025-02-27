package com.seleniumtests.connectors.tms;

import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.utils.TestNGResultUtils;
import org.json.JSONObject;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;

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
}
