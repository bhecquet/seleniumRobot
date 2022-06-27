package com.seleniumtests.it.browserfactory;

import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;

public class TestPerfectoDriverFactory extends GenericTest {

	/**
	 * For this test to run, you must add the following JVM parameters
	 * -DcloudApiKey=<token>
	 * -Dapp=<application.apk>
	 * -DappPackage=<package>
	 * -DappActivity=<activity>
	 * -DwebDriverGrid=https://<cloudname>.perfectomobile.com/nexperience/perfectomobile/wd/hub
	 * -Dhttps.proxyHost=<proxy_host> -Dhttps.proxyPort=<proxy_port>
	 */
	@Test(groups="no-test")
	public void testPerfecto() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		
		SeleniumTestsContextManager.getThreadContext().setPlatform("Android 12.0");
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("perfecto");
		SeleniumTestsContextManager.getThreadContext().configureContext(Reporter.getCurrentTestResult());
		
		WebDriver driver = WebUIDriver.getWebDriver(true);
		driver.getWindowHandle();
		
		
	}
}
