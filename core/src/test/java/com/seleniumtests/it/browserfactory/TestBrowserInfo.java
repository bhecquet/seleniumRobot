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
package com.seleniumtests.it.browserfactory;

import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class TestBrowserInfo extends GenericDriverTest {
	
	@Test(groups={"it"})
	public void testBrowserInfoLegacyFirefox() {
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.FIREFOX, "47.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		
		// for legacy version, no driver file name should be found
		Assert.assertNull(browserInfo.getDriverFileName());
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoMarionetteFirefox() {
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.FIREFOX, "48.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "geckodriver");
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoChrome() {
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.CHROME, "130.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "chromedriver_130.0_chrome-130_131");
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoInternetExplorer() {
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.INTERNET_EXPLORER, "11", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "IEDriverServer_Win32");
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoEdge() {
		if (!SystemUtils.IS_OS_WINDOWS_10) {
			throw new SkipException("This test can only be done on Windows 10");
		}

		BrowserInfo browserInfo = new BrowserInfo(BrowserType.EDGE, "130.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "edgedriver_130.0_edge-130-131");
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoSafari() {
		if (!SystemUtils.IS_OS_MAC_OSX) {
			throw new SkipException("This test can only be done on Mac OS X");
		}
		
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.SAFARI, "7.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertNull(browserInfo.getDriverFileName());
	}
	
	@Test(groups={"it"})
	public void testGetInternetExplorerPid(final ITestContext testNGCtx) {
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("iexploreEdge");
		SeleniumTestsContextManager.getThreadContext().setInitialUrl("https://www.google.fr");
		driver = WebUIDriver.getWebDriver(true);
		CustomEventFiringWebDriver efDriver = (CustomEventFiringWebDriver)driver;
		
		// there should be one PID for the driver server
		List<Long> pids = efDriver.getDriverPids();
		Assert.assertEquals(efDriver.getDriverPids().size(), 1);
		Assert.assertTrue(OSUtilityFactory.getInstance().getProgramNameFromPid((Long)pids.get(0)).contains("IEDriverServer"));
	}
	
	@Test(groups={"it"})
	public void testGetChromePid(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		driver = WebUIDriver.getWebDriver(true);
		CustomEventFiringWebDriver efDriver = (CustomEventFiringWebDriver)driver;
		
		// there should be one PID for the driver server
		List<Long> pids = efDriver.getDriverPids();
		Assert.assertEquals(efDriver.getDriverPids().size(), 1);
		Assert.assertTrue(OSUtilityFactory.getInstance().getProgramNameFromPid((Long)pids.get(0)).contains("chromedriver"));
	}
	
	@Test(groups={"it"})
	public void testGetFirefoxPid(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		CustomEventFiringWebDriver efDriver = (CustomEventFiringWebDriver)driver;
		
		// there should be one PID for the driver server
		List<Long> pids = efDriver.getDriverPids();
		Assert.assertEquals(efDriver.getDriverPids().size(), 1);
		Assert.assertTrue(OSUtilityFactory.getInstance().getProgramNameFromPid((Long)pids.get(0)).contains("geckodriver"));
	}
	

	@Test(groups={"it"})
	public void testGetAllProcess(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		CustomEventFiringWebDriver efDriver = (CustomEventFiringWebDriver)driver;
		
		List<Long> pids = efDriver.getDriverPids();
		List<Long> allPids = efDriver.getBrowserInfo().getAllBrowserSubprocessPids(pids);
		
		// one pid for driver and at least one for browser (chrome starts several processes)
		Assert.assertTrue(allPids.size() >= 2);
	}

}
