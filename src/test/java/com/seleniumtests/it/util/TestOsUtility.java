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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.ProcessInfo;

public class TestOsUtility extends GenericTest {

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
