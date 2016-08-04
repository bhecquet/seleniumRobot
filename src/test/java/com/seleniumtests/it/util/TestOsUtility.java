package com.seleniumtests.it.util;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.ProcessInfo;

public class TestOsUtility {

	private OSUtility osUtil;
	
	@BeforeClass(groups={"it"})
	public void testInitialization() {
		osUtil = new OSUtility();
	}
	
	@Test(groups={"it"})
	public void testProcessList() {
		List<ProcessInfo> plist= osUtil.getRunningProcessList();
		System.out.println("=== process found : ");
		for (ProcessInfo p : plist) {
			System.out.println(p);
		}
	}
	
	@Test(groups={"it"})
	public void testWindowsProcessList() {
		if (osUtil.isWindows()) {
			if (osUtil.isProcessRunning("svchost")) {
				return;
			}
			Assert.fail("no SVCHost process found");
		}
	}

	@Test(groups={"it"})
	public void testUnixProcessList() {
		if (!osUtil.isWindows()) {
			if (osUtil.isProcessRunning("dbus-daemon")) {
				return;
			}
			Assert.fail("no dbus process found");
		}
	}
	
	@Test(groups={"it"})
	public void testIsProcessNotRunning() {		
		Assert.assertFalse(osUtil.isProcessRunning("anUnknownProcess"), String.format("process anUnknownProcess should not be found"));
	}
	
	
}
