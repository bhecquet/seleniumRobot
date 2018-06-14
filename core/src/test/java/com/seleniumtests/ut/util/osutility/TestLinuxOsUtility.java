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
package com.seleniumtests.ut.util.osutility;

import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityUnix;
import com.seleniumtests.util.osutility.OSUtilityWindows;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

@PrepareForTest({Advapi32Util.class, OSUtilityUnix.class, OSUtilityFactory.class, OSCommand.class, Paths.class, BrowserInfo.class})
public class TestLinuxOsUtility extends MockitoTest {
	
	@Mock
	private Path path;
	
	@Mock
	private File browserFile;
	
	@Mock
	private Path path2;
	
	@Mock
	private File browserFile2;
	
	@BeforeClass(groups={"ut"})
	public void isWindows() {
		
		if (!OSUtility.isLinux()) {
			throw new SkipException("Test only available on Linux platform");
		}
	}
	

	/**
	 * Check no error is raised when no browser is installed (issue #128)
	 */
	@Test(groups={"ut"})
	public void testNoBrowserInstalled() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS);
		

		when(Paths.get("/usr/local/bin/firefox")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);

		when(OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/bin/which: no google-chrome in (/usr/local/sbin)");

		Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
		Assert.assertEquals(browsers.size(), 2);
		Assert.assertFalse(browsers.containsKey(BrowserType.FIREFOX));
	}

	@Test(groups={"ut"})
	public void testFirefoxStandardInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS);
		

		when(Paths.get("/usr/local/bin/firefox")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);

		when(OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/local/bin/firefox");
		when(OSCommand.executeCommandAndWait("firefox --version | more")).thenReturn("Mozilla Firefox 56.0");
		
		when(OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/bin/which: no google-chrome in (/usr/local/sbin)");


		Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
	}
	
	@Test(groups={"ut"})
	public void testChromeStandardInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS);
		
		
		when(Paths.get("/usr/local/bin/google-chrome")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/local/bin/google-chrome");
		when(OSCommand.executeCommandAndWait(new String[] {"google-chrome", "--version"})).thenReturn("Google Chrome 57.0.2987.110");
		
		when(OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
		when(OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");

		
		Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.CHROME));
	}
}
