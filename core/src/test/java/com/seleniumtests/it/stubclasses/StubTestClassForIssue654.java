package com.seleniumtests.it.stubclasses;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.helper.WaitHelper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class StubTestClassForIssue654 extends StubParentClass {

    @Test(groups="stub")
    public void test1() throws Exception {
        WaitHelper.waitForSeconds(1);
    }
    @Test(groups="stub")
    public void test2() throws Exception {
        WaitHelper.waitForSeconds(1);
    }
    @Test(groups="stub")
    public void test3() throws Exception {
        WaitHelper.waitForSeconds(1);
    }
    @AfterMethod(alwaysRun = true)
    public void after(Method testMethod) throws Exception {
        switch (testMethod.getName()) {
            case "test1":
                WaitHelper.waitForSeconds(1);
                break;
            case "test2":
                WaitHelper.waitForSeconds(3);
                break;
            case "test3":
                WaitHelper.waitForSeconds(5);
                break;
        }
        logger.info(String.format("after %s finished", testMethod.getName()));
    }
}
