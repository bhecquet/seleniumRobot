/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import org.apache.commons.lang3.SystemUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestDesktopDrivers extends GenericDriverTest {
	
	
	@Test(groups={"it"}, enabled=false)
	public void testFirefoxStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().equals("about:blank") || driver.getCurrentUrl().contains("http"));
	}
	
	@Test(groups={"it"})
	public void testChromeStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertEquals(driver.getCurrentUrl(), "data:,");
	}
	
	@Test(groups={"it"})
	public void testSafariStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		if (SystemUtils.IS_OS_MAC_OSX) {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setBrowser("*safari");
			driver = WebUIDriver.getWebDriver(true);
			Assert.assertEquals(driver.getCurrentUrl(), "data:,");
		}
	}
	
	@Test(groups={"it"})
	public void testIEStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*iexplore");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().contains("http://localhost:") || driver.getCurrentUrl().contains("about:blank"));
	}
	
	@Test(groups={"it"})
	public void testEdgeStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		if (!SystemUtils.IS_OS_WINDOWS_10) {
			throw new SkipException("This test can only be done on Windows 10");
		}
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*edge");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().contains("http://localhost:") || driver.getCurrentUrl().contains("about:start"));
	}
}
