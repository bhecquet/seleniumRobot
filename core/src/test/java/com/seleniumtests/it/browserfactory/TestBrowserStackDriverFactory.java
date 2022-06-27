package com.seleniumtests.it.browserfactory;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;

public class TestBrowserStackDriverFactory extends GenericTest {

	/**
	 * For this test to run, you must add the following JVM parameters
	 * -Dapp=<application.apk>
	 * -DappPackage=<package>
	 * -DappActivity=<activity>
	 * -DappWaitActivity=<waitActivity> # if any
	 * -DwebDriverGrid=https://<user>:<key>@hub.browserstack.com/wd/hub
	 * -Dhttps.proxyHost=<proxy_host> -Dhttps.proxyPort=<proxy_port>
	 */
	@Test(groups="no-test")
	public void testBrowserStack() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);

		SeleniumTestsContextManager.getThreadContext().setPlatform("android 11.0");
		SeleniumTestsContextManager.getThreadContext().setDeviceName("Google Pixel 4");
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("browserstack");
		SeleniumTestsContextManager.getThreadContext().configureContext(Reporter.getCurrentTestResult());
		
		WebDriver driver = WebUIDriver.getWebDriver(true);
		WebDriver realDriver = ((CustomEventFiringWebDriver)driver).getWebDriver();
		driver.findElements(By.xpath("//*")).get(0).getText();
		
		
	}
}
