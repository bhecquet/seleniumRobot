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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
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
import com.seleniumtests.util.osutility.OSUtilityWindows;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

@PrepareForTest({Advapi32Util.class, OSUtilityWindows.class, OSCommand.class})
public class TestWindowsOsUtility extends MockitoTest {
	
	@BeforeClass(groups={"ut"})
	public void isWindows() {
		
		if (!OSUtility.isWindows()) {
			throw new SkipException("Test only available on Windows platform");
		}
	}

	@Test(groups={"ut"})
	public void testGetBuild() {
		Assert.assertNotEquals(OSUtilityFactory.getInstance().getOSBuild(), 5000);
	}
	
	@Test(groups={"ut"})
	public void testBrowserList() {
		List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
		Assert.assertTrue(browsers.contains(BrowserType.INTERNET_EXPLORER));
		
		if (SystemUtils.IS_OS_WINDOWS_10) { 
			Assert.assertTrue(browsers.contains(BrowserType.EDGE));
		}
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML\\shell\\open\\command
	 */
	@Test(groups={"ut"})
	public void testFirefoxStandardWindowsInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);

		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");
		when(OSCommand.executeCommandAndWait("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe --version | more")).thenReturn("Mozilla Firefox 56.0");
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("\r\n" +
				"HKEY_CLASSES_ROOT\\FirefoxHTML\r\n" +
				"Fin de la recherche : 2 correspondance(s) trouvée(s).");

		OSUtility.refreshBrowserList();
		List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
		Assert.assertTrue(browsers.contains(BrowserType.FIREFOX));
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML-308046\\shell\\open\\command
	 */
	@Test(groups={"ut"})
	public void testFirefoxServerWindowsInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML-AC250DEAA7389F99\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");
																			
		when(OSCommand.executeCommandAndWait("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe --version | more")).thenReturn("Mozilla Firefox 56.0");
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("\r\n" +
				"HKEY_CLASSES_ROOT\\FirefoxHTML-AC250DEAA7389F99\r\n" +
				"Fin de la recherche : 2 correspondance(s) trouvée(s).");

		
		OSUtility.refreshBrowserList();
		List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
		Assert.assertTrue(browsers.contains(BrowserType.FIREFOX));
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML-308046\\shell\\open\\command
	 */
	@Test(groups={"ut"})
	public void testSeveralFirefoxInstallations() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML-AC250DEAA7389F99\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe\" -osint -url \"%1\"");
		when(OSCommand.executeCommandAndWait("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe --version | more")).thenReturn("Mozilla Firefox 56.0");
		when(OSCommand.executeCommandAndWait("C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe --version | more")).thenReturn("Mozilla Firefox 55.0");
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("\r\n" +
				"HKEY_CLASSES_ROOT\\FirefoxHTML\r\n" +
				"HKEY_CLASSES_ROOT\\FirefoxHTML-AC250DEAA7389F99\r\n" +
				"Fin de la recherche : 2 correspondance(s) trouvée(s).");

		
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, BrowserInfo> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
		Assert.assertTrue(browsers.get(BrowserType.FIREFOX).getPath().contains("Mozilla Firefox"));
	}
}
