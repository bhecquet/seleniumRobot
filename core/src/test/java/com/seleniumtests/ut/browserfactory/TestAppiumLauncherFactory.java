package com.seleniumtests.ut.browserfactory;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.AppiumLauncherFactory;
import com.seleniumtests.browserfactory.mobile.ExistingAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.GridAppiumLauncher;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.TestType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAppiumLauncherFactory extends GenericTest {

    @Test(groups = "ut")
    public void testGetInstanceLocalAndroidApp() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        SeleniumTestsContextManager.getThreadContext().setRunMode("local");
        SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4723/");

        Assert.assertTrue(AppiumLauncherFactory.getInstance() instanceof ExistingAppiumLauncher);
    }

    @Test(groups = "ut", expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "'appiumServerUrl' parameter MUST be set")
    public void testGetInstanceLocalNoAppiumUrl() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        SeleniumTestsContextManager.getThreadContext().setRunMode("local");

        AppiumLauncherFactory.getInstance();
    }

    @Test(groups = "ut", expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "AppiumLauncher can only be used in mobile / windows app testing")
    public void testGetInstanceLocalWeb() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        SeleniumTestsContextManager.getThreadContext().setRunMode("local");
        SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4723/");

        AppiumLauncherFactory.getInstance();
    }

    @Test(groups = "ut")
    public void testGetInstanceGridAndroidApp() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        SeleniumTestsContextManager.getThreadContext().setRunMode("grid");

        Assert.assertTrue(AppiumLauncherFactory.getInstance() instanceof GridAppiumLauncher);
    }

    @Test(groups = "ut", expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "AppiumLauncher can only be used in local and grid mode")
    public void testGetInstanceSauceLabAndroidApp() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        SeleniumTestsContextManager.getThreadContext().setRunMode("saucelabs");

        AppiumLauncherFactory.getInstance();
    }
}
