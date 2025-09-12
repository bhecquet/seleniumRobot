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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.ProcessInfo;

public class TestDesktopDrivers extends GenericDriverTest {
	
	
	@Test(groups={"it"})
	public void testFirefoxStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().equals("about:blank") || driver.getCurrentUrl().contains("http"));
		
		// issue #280: check BrowserInfo exists
		Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.FIREFOX);
	}
	
	@Test(groups={"it"})
	public void testChromeStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertEquals(driver.getCurrentUrl(), "data:,");
		
		// issue #280: check BrowserInfo exists
		Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.CHROME);
	}

	@Test(groups={"it"})
	public void testChromeBetaStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {

		// check Chrome is available and Edge beta is installed
		OSUtility.refreshBrowserList(true);
		Assert.assertTrue(OSUtility.getInstalledBrowsersWithVersion(true).get(BrowserType.CHROME).size() > 1);
		Assert.assertTrue(OSUtility.getInstalledBrowsersWithVersion(true).get(BrowserType.CHROME).get(1).getPath().contains("Beta"));

		// initial list of chrome processes in case some are running
		List<String> executingEdge = OSUtilityFactory.getInstance().getRunningProcesses("chrome")
					.stream()
					.map(ProcessInfo::getPid)
					.collect(Collectors.toList());
		
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setBetaBrowser(true);
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertEquals(driver.getCurrentUrl(), "data:,");
		
		
		// issue #280: check BrowserInfo exists
		Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.CHROME);
		
		// check that Edge Beta has been started
		List<String> newEdge = OSUtilityFactory.getInstance().getRunningProcesses("chrome")
			.stream()
			.map(ProcessInfo::getPid)
			.filter(pid -> !executingEdge.contains(pid))
			.collect(Collectors.toList());
		
		if (SystemUtils.IS_OS_WINDOWS) {
			Assert.assertTrue(OSCommand.executeCommandAndWait(String.format("wmic process where \"ProcessID=%s\" get ExecutablePath", newEdge.get(0)), true).contains("Beta"));
		}

	}
	
	@Test(groups={"it"})
	public void testSafariStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		if (SystemUtils.IS_OS_MAC_OSX) {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setBrowser("*safari");
			driver = WebUIDriver.getWebDriver(true);
			Assert.assertEquals(driver.getCurrentUrl(), "data:,");
			
			// issue #280: check BrowserInfo exists
			Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
			Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.SAFARI);
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
		
		// issue #280: check BrowserInfo exists
		Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.INTERNET_EXPLORER);
	}
	
	@Test(groups={"it"})
	public void testEdgeStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		if (!SystemUtils.IS_OS_WINDOWS_10) {
			throw new SkipException("This test can only be done on Windows 10");
		}

		// check Edge is available
		Assert.assertTrue(OSUtility.getInstalledBrowsersWithVersion(true).get(BrowserType.EDGE).size() > 0);
		
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*edge");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().contains("http://localhost:") || driver.getCurrentUrl().contains("about:start") || driver.getCurrentUrl().contains("data"));
		
		// issue #280: check BrowserInfo exists
		Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.EDGE);
	}
	
	@Test(groups={"it"})
	public void testEdgeBetaStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		if (!SystemUtils.IS_OS_WINDOWS_10) {
			throw new SkipException("This test can only be done on Windows 10");
		}
		
		// check Edge is available and Edge beta is installed
		OSUtility.refreshBrowserList(true);
		Assert.assertTrue(OSUtility.getInstalledBrowsersWithVersion(true).get(BrowserType.EDGE).size() > 1);
		Assert.assertTrue(OSUtility.getInstalledBrowsersWithVersion(true).get(BrowserType.EDGE).get(1).getPath().contains("Beta"));

		// initial list of edge processes in case some are running
		List<String> executingEdge = OSUtilityFactory.getInstance().getRunningProcesses("msedge")
					.stream()
					.map(ProcessInfo::getPid)
					.collect(Collectors.toList());
		
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*edge");
		SeleniumTestsContextManager.getThreadContext().setBetaBrowser(true);
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().contains("http://localhost:") || driver.getCurrentUrl().contains("about:start") || driver.getCurrentUrl().contains("data"));
		
		
		// issue #280: check BrowserInfo exists
		Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getBrowserInfo());
		Assert.assertEquals(((CustomEventFiringWebDriver)driver).getBrowserInfo().getBrowser(), BrowserType.EDGE);
		
		// check that Edge Beta has been started
		List<String> newEdge = OSUtilityFactory.getInstance().getRunningProcesses("msedge")
			.stream()
			.map(ProcessInfo::getPid)
			.filter(pid -> !executingEdge.contains(pid))
			.collect(Collectors.toList());
		
		Assert.assertTrue(OSCommand.executeCommandAndWait(String.format("wmic process where \"ProcessID=%s\" get ExecutablePath", newEdge.get(0))).contains("Beta"));
		
		
	}
}
