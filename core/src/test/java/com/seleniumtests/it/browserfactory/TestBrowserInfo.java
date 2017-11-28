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
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.FIREFOX, "47.0", "/some/path");
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		
		// for legacy version, no driver file name should be found
		Assert.assertNull(browserInfo.getDriverFileName());
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoMarionetteFirefox() {
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.FIREFOX, "48.0", "/some/path");
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "geckodriver");
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoChrome() {
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.CHROME, "58.0", "/some/path");
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "chromedriver_2.31_chrome-58-60");
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoInternetExplorer() {
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.INTERNET_EXPLORER, "11", "/some/path");
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "IEDriverServer_Win32");
		
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoEdge() {
		if (!SystemUtils.IS_OS_WINDOWS_10) {
			throw new SkipException("This test can only be done on Windows 10");
		}

		BrowserInfo browserInfo = new BrowserInfo(BrowserType.EDGE, "14393", "/some/path");
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "MicrosoftWebDriver_14393");
	}
	
	@Test(groups={"it"})
	public void testBrowserInfoSafari() {
		if (!SystemUtils.IS_OS_MAC_OSX) {
			throw new SkipException("This test can only be done on Mac OS X");
		}
		
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.SAFARI, "7.0", "/some/path");
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
		SeleniumTestsContextManager.getThreadContext().setBrowser("*iexplore");
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
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
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
