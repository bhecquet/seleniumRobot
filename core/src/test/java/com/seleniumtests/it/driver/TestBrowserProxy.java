/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestBrowserProxy extends GenericDriverTest {
	
	private static final String BROWSER = "chrome";

	public void initDriver(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@Test(groups={"it"})
	public void testFirefoxProxyAuto(final ITestContext testNGCtx) {
		String currentProxy = System.getProperty(SeleniumTestsContext.WEB_PROXY_TYPE);
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, BROWSER);
			System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, "autodetect");
			initDriver(testNGCtx);
			driver.get("http://www.google.com");
			logger.info(driver.findElement(By.tagName("body")).getText().toLowerCase());
			Assert.assertTrue(driver.findElement(By.tagName("body")).getText().toLowerCase().contains("gmail"), "Google home page has not been loaded");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);

			// restore Proxy type that may have been set from command line to adapt to specific execution environement
			if (currentProxy != null) {
				System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, currentProxy);
			} else {
				System.clearProperty(SeleniumTestsContext.WEB_PROXY_TYPE);
			}
		}
	}
	
	/**
	 * Test disabled as too long. To activate if some change is done on proxy
	 * @param testNGCtx
	 */
	@Test(groups={"it"}, expectedExceptions=TimeoutException.class, enabled=false)
	public void testFirefoxProxyDirect(final ITestContext testNGCtx) {

		String currentProxy = System.getProperty(SeleniumTestsContext.WEB_PROXY_TYPE);
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, BROWSER);
			System.setProperty(SeleniumTestsContext.PAGE_LOAD_TIME_OUT, "5");
			System.setProperty(SeleniumTestsContext.WEB_SESSION_TIME_OUT, "5");
			System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, "direct");
			initDriver(testNGCtx);
			driver.get("http://www.google.com");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);

			// restore Proxy type that may have been set from command line to adapt to specific execution environement
			if (currentProxy != null) {
				System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, currentProxy);
			} else {
				System.clearProperty(SeleniumTestsContext.WEB_PROXY_TYPE);
			}
			System.clearProperty(SeleniumTestsContext.PAGE_LOAD_TIME_OUT);
			System.clearProperty(SeleniumTestsContext.WEB_SESSION_TIME_OUT);
		}
	}
}
