package com.seleniumtests.it.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.util.OSUtility;

public class TestOsUtility {

	
	@Test(groups={"it"})
	public void testWindowsProcessList() {
		if (OSUtility.isWindows()) {
			for (String process: OSUtility.getRunningProcessList()) {
				if (process.startsWith("svchost.exe")) {
					return;
				}
			}
			Assert.fail("no SVCHost process found");
		}
	}
	
	@Test(groups={"it"})
	public void testIsProcessRunning() {
		String processName;
		if (OSUtility.isWindows()) {
			processName = "svchost.exe";
		} else {
			processName = "dbus";
		}
			
		Assert.assertTrue(OSUtility.isProcessRunning(processName), String.format("process %s not found", processName));
	}
	
	@Test(groups={"it"})
	public void testIsProcessNotRunning() {		
		Assert.assertFalse(OSUtility.isProcessRunning("anUnknownProcess"), String.format("process anUnknownProcess should not be found"));
	}
	
	@Test(groups={"it"})
	public void testUnixProcessList() {
		if (!OSUtility.isWindows()) {
			for (String process: OSUtility.getRunningProcessList()) {
				if (process.contains("dbus")) {
					return;
				}
			}
			Assert.fail("no DBUS process found");
		}
	}
}
