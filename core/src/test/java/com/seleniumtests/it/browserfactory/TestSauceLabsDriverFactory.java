package com.seleniumtests.it.browserfactory;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;

public class TestSauceLabsDriverFactory extends GenericTest {

	/**
	 * For this test to run, you must add the following JVM parameters
	 * -Dapp=<application.apk>
	 * -DappPackage=<package>
	 * -DappActivity=<activity>
	 * -DappWaitActivity=<waitActivity> # if any
	 * -DwebDriverGrid=https://<user>:<key>@ondemand.eu-central-1.saucelabs.com:443/wd/hub
	 * -Dhttps.proxyHost=<proxy_host> -Dhttps.proxyPort=<proxy_port>
	 */
	@Test(groups="no-test")
	public void testSauceLabs() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);


		SeleniumTestsContextManager.getThreadContext().setPlatform("android 11");
		SeleniumTestsContextManager.getThreadContext().setDeviceName(".*google.*");
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("saucelabs");
		SeleniumTestsContextManager.getThreadContext().configureContext(Reporter.getCurrentTestResult());
		
		WebDriver driver = WebUIDriver.getWebDriver(true);
		WebDriver realDriver = ((CustomEventFiringWebDriver)driver).getWebDriver();

		
		
	}
}
