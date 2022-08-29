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
package com.seleniumtests.it.util;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.ProcessInfo;

public class TestOsUtility extends GenericTest {

	private OSUtility osUtil;
	private Long processId;
	
	@BeforeClass(groups={"it"})
	public void testInitialization() {
		osUtil = OSUtilityFactory.getInstance();
		
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        processId = Long.parseLong(jvmName.substring(0, index));
	}
	
	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void destroyDriver() {
		if (WebUIDriver.getWebDriver(false) != null) {
			WebUIDriver.cleanUp();
		}
	}
	
	@Test(groups={"it"})
	public void testProcessList() {
		List<ProcessInfo> plist= osUtil.getRunningProcessList();
		for (ProcessInfo p : plist) {
			System.out.println(p);
		}
	}
	
	@Test(groups={"it"})
	public void testWindowsProcessList() {
		if (OSUtility.isWindows()) {
			if (osUtil.isProcessRunning("svchost")) {
				return;
			}
			Assert.fail("no SVCHost process found");
		}
	}

	@Test(groups={"it"})
	public void testLinuxProcessList() {
		if (OSUtility.isLinux()) {
			if (osUtil.isProcessRunning("dbus-daemon") || osUtil.isProcessRunning("/usr/sbin/cron")) {
				return;
			}
			Assert.fail("no dbus process found");
		}
	}

	@Test(groups={"it"})
	public void testMacProcessList() {
		if (OSUtility.isMac()) {
			if (osUtil.isProcessRunning("sysmond")) {
				return;
			}
			Assert.fail("no sysmond process found");
		}
	}
	
	@Test(groups={"it"})
	public void testIsProcessNotRunning() {		
		Assert.assertFalse(osUtil.isProcessRunning("anUnknownProcess"), String.format("process anUnknownProcess should not be found"));
	}
	

	@Test(groups={"it"})
	public void testIsProcessRunningByPid() {
		String processName = "";
		if (OSUtility.isWindows()) {
			processName = "svchost";
		} else if (OSUtility.isLinux()) {
			processName = "dbus-daemon";
		} else if (OSUtility.isMac()) {
			processName = "sysmond";
		}
			
		ProcessInfo pi = osUtil.getRunningProcess(processName);
		Assert.assertTrue(osUtil.isProcessRunningByPid(pi.getPid()));
			
	}
	
	@Test(groups={"it"})
	public void testIsProcessNotRunningByPid() {
		Assert.assertFalse(osUtil.isProcessRunningByPid("999999"));
	}
	
	@Test(groups={"it"})
	public void testIsChromeWebBrowserRunning() {
		osUtil.killAllWebBrowserProcess(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		WebUIDriver.getWebDriver(true);
		Assert.assertTrue(osUtil.isWebBrowserRunning(true));
	}
	
	@Test(groups={"it"})
	public void testIsFirefoxWebBrowserRunning() {
		osUtil.killAllWebBrowserProcess(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		WebUIDriver.getWebDriver(true);
		Assert.assertTrue(osUtil.isWebBrowserRunning(true));
	}
	
	@Test(groups={"it"})
	public void testIsInternetExplorerWebBrowserRunning() {
		if (OSUtility.isWindows()) {
			osUtil.killAllWebBrowserProcess(true);
			SeleniumTestsContextManager.getThreadContext().setBrowser("iexplore");
			WebUIDriver.getWebDriver(true);
			Assert.assertTrue(osUtil.isWebBrowserRunning(true));
		}
	}
	
	@Test(groups={"it"})
	public void testIsEdgeWebBrowserRunning() {
		if (OSUtility.isWindows10()) {
			osUtil.killAllWebBrowserProcess(true);
			SeleniumTestsContextManager.getThreadContext().setBrowser("edge");
			WebUIDriver.getWebDriver(true);
			Assert.assertTrue(osUtil.isWebBrowserRunning(true));
		}
	}
	
	@Test(groups={"it"})
	public void testIsSafariWebBrowserRunning() {
		if (OSUtility.isMac()) {
			osUtil.killAllWebBrowserProcess(true);
			SeleniumTestsContextManager.getThreadContext().setBrowser("safari");
			WebUIDriver.getWebDriver(true);
			Assert.assertTrue(osUtil.isWebBrowserRunning(true));
		}
	}
	
	@Test(groups={"it"})
	public void testWhichChromeWebBrowserRunning() {
		osUtil.killAllWebBrowserProcess(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		WebUIDriver.getWebDriver(true);
		List<ProcessInfo> pis = osUtil.whichWebBrowserRunning();
		for (ProcessInfo pi: pis) {
			if (pi.getName().contains("chrome")) {
				return;
			}
		} 
		Assert.fail("Chrome has not been found");
	}
	
	@Test(groups={"it"})
	public void testWhichFirefoxWebBrowserRunning() {
		osUtil.killAllWebBrowserProcess(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		WebUIDriver.getWebDriver(true);
		List<ProcessInfo> pis = osUtil.whichWebBrowserRunning(true);
		for (ProcessInfo pi: pis) {
			if (pi.getName().contains("firefox")) {
				return;
			}
		} 
		Assert.fail("Firefox has not been found");
	}
	
	@Test(groups={"it"})
	public void testWhichInternetExplorerWebBrowserRunning() {
		if (OSUtility.isWindows()) {
			osUtil.killAllWebBrowserProcess(true);
			SeleniumTestsContextManager.getThreadContext().setBrowser("iexplore");
			WebUIDriver.getWebDriver(true);
			List<ProcessInfo> pis = osUtil.whichWebBrowserRunning();
			for (ProcessInfo pi: pis) {
				if (pi.getName().contains("iexplore")) {
					return;
				}
			} 
			Assert.fail("Internet Explorer has not been found");
		}
	}
	
	@Test(groups={"it"})
	public void testWhichEdgeWebBrowserRunning() {
		if (OSUtility.isWindows10()) {
			osUtil.killAllWebBrowserProcess(true);
			SeleniumTestsContextManager.getThreadContext().setBrowser("edge");
			WebUIDriver.getWebDriver(true);
			List<ProcessInfo> pis = osUtil.whichWebBrowserRunning();
			for (ProcessInfo pi: pis) {
				if (pi.getName().contains("edge")) {
					return;
				}
			} 
			Assert.fail("Edge has not been found");
		}
	}
	
	@Test(groups={"it"})
	public void testWhichSafariWebBrowserRunning() {
		if (OSUtility.isMac()) {
			osUtil.killAllWebBrowserProcess(true);
			SeleniumTestsContextManager.getThreadContext().setBrowser("safari");
			WebUIDriver.getWebDriver(true);
			List<ProcessInfo> pis = osUtil.whichWebBrowserRunning();
			for (ProcessInfo pi: pis) {
				if (pi.getName().contains("safari")) {
					return;
				}
			} 
			Assert.fail("Safari has not been found");
		}
	}

	/**
	 * Here we test both browser and driver killing because this test is long and duplicating it would increase test time by 3 mins
	 */
	@Test(groups={"it"})
	public void testKillAllWebBrowserProcess() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		WebUIDriver.getWebDriver(true);
		WebUIDriver.getWebDriver(true, BrowserType.FIREFOX, "ff", null);
		if (OSUtility.isWindows()) {
			WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "ie", null);
		}
		if (OSUtility.isWindows10()) {
			WebUIDriver.getWebDriver(true, BrowserType.EDGE, "edge", null);
		}
		if (OSUtility.isMac()) {
			WebUIDriver.getWebDriver(true, BrowserType.SAFARI, "safari", null);
		}
		osUtil.killAllWebBrowserProcess(true);
		osUtil.killAllWebDriverProcess();
		
		Assert.assertNull(osUtil.getRunningProcess("chrome"));
		Assert.assertNull(osUtil.getRunningProcess("firefox"));
		Assert.assertNull(osUtil.getRunningProcess("msedge"));
		Assert.assertNull(osUtil.getRunningProcess("iexplore"));
		Assert.assertNull(osUtil.getRunningProcess("safari"));
		
		Assert.assertNull(osUtil.getRunningProcess("chromedriver"));
		Assert.assertNull(osUtil.getRunningProcess("geckodriver"));
		Assert.assertNull(osUtil.getRunningProcess("iedriverserver"));
		Assert.assertNull(osUtil.getRunningProcess("microsoftwebdriver"));
		Assert.assertNull(osUtil.getRunningProcess("edgedriver"));
		
	}
	
	
	@Test(groups={"it"})
	public void testGetProcessNameFromPid() {
		if (OSUtility.isWindows()) {
			Assert.assertTrue(osUtil.getProgramNameFromPid(processId).startsWith("java"));
		}
	}
	

	@Test(groups={"it"})
	public void testKillProcess() {
		if (OSUtility.isWindows()) {
			OSCommand.executeCommand("calc");
			WaitHelper.waitForSeconds(2);
			ProcessInfo pi = osUtil.getRunningProcess("calc");
			if (pi == null) {
				pi = osUtil.getRunningProcess("calculator"); // Windows 10
			}
			if (pi == null) {
				pi = osUtil.getRunningProcess("Calculator"); // Windows 10
			}
			if (pi == null) {
				pi = osUtil.getRunningProcess("win32calc"); // Windows 2016
			}
			if (pi == null) {
				throw new ConfigurationException("Cannot find process 'calc', 'win32calc' or 'calculator'");
			}
			osUtil.killProcess(pi.getPid(), true);
			Assert.assertNull(osUtil.getRunningProcess("calc"));
			Assert.assertNull(osUtil.getRunningProcess("calculator"));
			Assert.assertNull(osUtil.getRunningProcess("Calculator"));
			Assert.assertNull(osUtil.getRunningProcess("win32calc"));
		}
	}
	

	@Test(groups={"it"})
	public void testKillAllWebDriverProcess() {
		osUtil.killAllWebDriverProcess();
	}
	
	@Test(groups={"it"})
	public void testGetProcessNameFromNonExistingPid() {
		Assert.assertEquals(osUtil.getProgramNameFromPid(999999L), "");
	}
	
	@Test(groups={"it"})
	public void testGetProcessNameFromPidLinux() {
		if (OSUtility.isLinux()) {
			Assert.assertEquals(osUtil.getProgramNameFromPid(processId).trim(), "java");
		}
	}
	
	@Test(groups={"it"})
	public void testOsArchitecture() {
		if (OSUtility.isWindows()) {
			Assert.assertEquals(OSUtility.getArchitecture(), "amd64");
		}
	}
	
	@Test(groups={"it"})
	public void testOsBits() {
		if (OSUtility.isWindows()) {
			Assert.assertEquals(OSUtility.getOSBits(), "64");
		}
	}
}
