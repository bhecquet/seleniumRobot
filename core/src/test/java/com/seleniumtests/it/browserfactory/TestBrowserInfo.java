/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.browserfactory;

import java.io.IOException;
import java.util.ArrayList;
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
		BrowserInfo browserInfo = new BrowserInfo(BrowserType.CHROME, "148.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "chromedriver_148.0_chrome-148-149");
		
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
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows 10");
		}

		BrowserInfo browserInfo = new BrowserInfo(BrowserType.EDGE, "148.0", "/some/path", false);
		browserInfo.getDriverFileName();
		
		Assert.assertTrue(browserInfo.isDriverFileSearched());
		Assert.assertEquals(browserInfo.getDriverFileName(), "edgedriver_148.0_edge-148-149");
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
		
		// there should be one PID for the driver server
		List<Long> pids = driver.getDriverPids();
		Assert.assertEquals(driver.getDriverPids().size(), 1);
		Assert.assertTrue(OSUtilityFactory.getInstance().getProgramNameFromPid((Long)pids.getFirst()).contains("IEDriverServer"));
	}
	
	@Test(groups={"it"})
	public void testGetChromePid(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		driver = WebUIDriver.getWebDriver(true);
		
		// there should be one PID for the driver server
		List<Long> pids = driver.getDriverPids();
		Assert.assertEquals(driver.getDriverPids().size(), 1);
		Assert.assertTrue(OSUtilityFactory.getInstance().getProgramNameFromPid((Long)pids.getFirst()).contains("chromedriver"));
	}
	
	@Test(groups={"it"})
	public void testGetFirefoxPid(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		
		// there should be one PID for the driver server
		List<Long> pids = driver.getDriverPids();
		Assert.assertEquals(driver.getDriverPids().size(), 1);
		Assert.assertTrue(OSUtilityFactory.getInstance().getProgramNameFromPid((Long)pids.getFirst()).contains("geckodriver"));
	}
	

	@Test(groups={"it"})
	public void testGetAllProcess(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		
		List<Long> pids = driver.getDriverPids();
		List<Long> allPids = driver.getBrowserInfo().getAllBrowserSubprocessPids(pids);
		
		// one pid for driver and at least one for browser (chrome starts several processes)
		Assert.assertTrue(allPids.size() >= 2);
	}
	
	/**
     * Test that getChildProcessPid returns child processes of the driver process.
     * When a browser is started, the driver process (e.g. chromedriver) spawns browser sub-processes.
     */
    @Test(groups = {"it"})
    public void testGetChildProcessPid(final ITestContext testNGCtx) throws IOException {
        initThreadContext(testNGCtx);
        SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
        driver = WebUIDriver.getWebDriver(true);

        List<Long> driverPids = driver.getDriverPids();
        Assert.assertFalse(driverPids.isEmpty(), "Driver PIDs should not be empty");

        // Get child processes of the driver process (chromedriver), without filtering by name
        Long driverPid = driverPids.get(0);
        List<Long> childPids = OSUtilityFactory.getInstance().getChildProcessPid(driverPid, null, new ArrayList<>());

        // chromedriver should have spawned at least one child process (the browser itself)
        Assert.assertFalse(childPids.isEmpty(), "chromedriver should have at least one child process");

        // Verify that the driver PID itself is not in the child list
        Assert.assertFalse(childPids.contains(driverPid), "Child PIDs should not contain the parent PID");
    }

    /**
     * Test that getChildProcessPid filters by process name correctly
     */
    @Test(groups = {"it"})
    public void testGetChildProcessPidFilteredByName(final ITestContext testNGCtx) throws IOException {
        initThreadContext(testNGCtx);
        SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
        driver = WebUIDriver.getWebDriver(true);

        List<Long> driverPids = driver.getDriverPids();
        Long driverPid = driverPids.get(0);

        // Search for chrome child processes by name
        String chromeBinaryName = SystemUtils.IS_OS_WINDOWS ? "chrome.exe" : "chrome";
        List<Long> chromePids = OSUtilityFactory.getInstance().getChildProcessPid(driverPid, chromeBinaryName, new ArrayList<>());

        // chromedriver should have spawned at least one chrome process
        Assert.assertFalse(chromePids.isEmpty(), "There should be at least one chrome child process");
    }

    /**
     * Test that getChildProcessPid excludes existing PIDs
     */
    @Test(groups = {"it"})
    public void testGetChildProcessPidExcludesExistingPids(final ITestContext testNGCtx) throws IOException {
        initThreadContext(testNGCtx);
        SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
        driver = WebUIDriver.getWebDriver(true);

        List<Long> driverPids = driver.getDriverPids();
        Long driverPid = driverPids.get(0);

        // First, get all child PIDs
        List<Long> allChildPids = OSUtilityFactory.getInstance().getChildProcessPid(driverPid, null, new ArrayList<>());
        Assert.assertFalse(allChildPids.isEmpty(), "Should have child processes");

        // Now get child PIDs excluding the ones we already found
        List<Long> newChildPids = OSUtilityFactory.getInstance().getChildProcessPid(driverPid, null, allChildPids);

        // All previously found PIDs should be excluded
        for (Long pid : allChildPids) {
            Assert.assertFalse(newChildPids.contains(pid), "Existing PID " + pid + " should be excluded");
        }
    }

    /**
     * Test that getChildProcessPid returns empty list for an invalid/non-existent parent PID
     */
    @Test(groups = {"it"})
    public void testGetChildProcessPidWithInvalidParent() throws IOException {
        // Use a PID that is very unlikely to exist
        List<Long> childPids = OSUtilityFactory.getInstance().getChildProcessPid(999999999L, null, new ArrayList<>());
        Assert.assertTrue(childPids.isEmpty(), "Invalid parent PID should return empty list");
    }

}
