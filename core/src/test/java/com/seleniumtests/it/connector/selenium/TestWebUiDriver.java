package com.seleniumtests.it.connector.selenium;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestWebUiDriver extends GenericTest {
    
    
    @Test(groups={"it"}, enabled=true)
    public void testMultipleBrowserCreationGridMode() {
        
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
        SeleniumTestsContextManager.getThreadContext().setNodeTags("***REMOVED***");
        SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("***REMOVED***");
        
        // creates the first driver
        WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", null);
        driver1.get("chrome://settings/");
        
        // creates the second driver
        WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "second", null);
        driver2.get("about:config");
        
        // last created driver has the focus
        Assert.assertEquals(WebUIDriver.getWebDriver(false), driver2);
    }
}
