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
package com.seleniumtests.it.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
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
			if (osUtil.isProcessRunning("dbus-daemon")) {
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
	public void testGetProcessNameFromPid() {
		if (OSUtility.isWindows()) {
			Assert.assertEquals(osUtil.getProgramNameFromPid(processId), "javaw.exe");
		}
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
}
