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
package com.seleniumtests.ut.util.osutility;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.seleniumtests.util.osutility.*;
import org.apache.commons.lang3.SystemUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class TestWindowsOsUtility extends MockitoTest {
	
	@Mock
	private Path path;
	
	@Mock
	private File browserFile;
	
	@Mock
	private Path path2;
	
	@Mock
	private Path path3;
	
	@Mock
	private Path path4;
	
	@Mock
	private File versionFolder;
	
	@Mock
	private File exeFile;
	
	@Mock
	private File browserFile2;
	
	@BeforeMethod(groups={"ut"})
	public void isWindows() {
		
		if (!OSUtility.isWindows()) {
			throw new SkipException("Test only available on Windows platform");
		}

		OSUtilityFactory.resetInstance();
	}
	@AfterMethod(groups={"ut"}, alwaysRun = true)
	public void resetBrowserList() {

		if (OSUtility.isWindows()) {
			OSUtility.resetInstalledBrowsersWithVersion();
		}
	}

	@Test(groups={"ut"})
	public void testGetBuild() {
		Assert.assertNotEquals(OSUtilityFactory.getInstance().getOSBuild(), "5000");
	}
	
	@Test(groups={"ut"})
	public void testBrowserList() {
		List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
		Assert.assertTrue(browsers.contains(BrowserType.INTERNET_EXPLORER));
		
		if (SystemUtils.IS_OS_WINDOWS) {
			Assert.assertTrue(browsers.contains(BrowserType.EDGE));
		}
	}
	
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"netstat", "-aon"})).thenReturn("""
Proto  Adresse locale         Adresse distante       État
TCP    0.0.0.0:64360          0.0.0.0:0              LISTENING       39320
TCP    0.0.0.0:64362          0.0.0.0:0              LISTENING       26660
TCP    0.0.0.0:51239          0.0.0.0:0              LISTENING       22492
TCP    10.165.131.105:139     0.0.0.0:0              LISTENING       4
TCP    10.165.131.105:49244   192.168.62.10:8080     TIME_WAIT       0
TCP    10.165.131.105:49320   10.204.88.85:443       TIME_WAIT       0""");

			Integer processPid = OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239);
			Assert.assertEquals(processPid, (Integer) 22492);
		}
	}
	
	/**
	 * Check we don't match if port is remote
	 */
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort2() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"netstat", "-aon"})).thenReturn("""
Proto  Adresse locale         Adresse distante       État
TCP    0.0.0.0:64360          0.0.0.0:0              LISTENING       39320
TCP    0.0.0.0:64362          0.0.0.0:0              LISTENING       26660
TCP    0.0.0.0:123          0.0.0.0:51239             LISTENING       22492
TCP    10.165.131.105:139     0.0.0.0:0              LISTENING       4
TCP    10.165.131.105:49244   192.168.62.10:8080     TIME_WAIT       0
TCP    10.165.131.105:49320   10.204.88.85:443       TIME_WAIT       0""");
			
			Assert.assertNull(OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239));
		}
	}
	
	/**
	 * Check we don't match if is not listening
	 */
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort3() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"netstat", "-aon"})).thenReturn("""
Proto  Adresse locale         Adresse distante       État
TCP    0.0.0.0:64360          0.0.0.0:0              LISTENING       39320
TCP    0.0.0.0:64362          0.0.0.0:0              LISTENING       26660
TCP    0.0.0.0:123          0.0.0.0:0             LISTENING       22492
TCP    10.165.131.105:139     0.0.0.0:0              LISTENING       4
TCP    10.165.131.105:51239   192.168.62.10:8080     TIME_WAIT       0
TCP    10.165.131.105:49320   10.204.88.85:443       TIME_WAIT       0""");

			Assert.assertNull(OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239));
		}
	}
	
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPortNotFound() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"netstat", "-aon"})).thenReturn("");

			Assert.assertNull(OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239));
		}
	}
	
	@Test(groups={"ut"})
	public void testGetProcessList() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class)) {
			String command = SystemUtility.getenv("windir") + "\\system32\\" + "tasklist.exe";
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {command, "/NH", "/SVC"})).thenReturn("""
eclipse.exe                   6480 N/A
javaw.exe                     7280 N/A
chromedriver_2.45_chrome-    11252 N/A
svchost.exe                   1784 CryptSvc, Dnscache, LanmanWorkstation,
									NlaSvc, TermService, WinRM""");
			List<ProcessInfo> plist = OSUtilityFactory.getInstance().getRunningProcessList();
			Assert.assertEquals(plist.size(), 4);
			Assert.assertEquals(plist.get(0).getName(), "eclipse");
			Assert.assertEquals(plist.get(0).getPid(), "6480");
			Assert.assertEquals(plist.get(1).getName(), "javaw");
			Assert.assertEquals(plist.get(2).getName(), "chromedriver_2.45_chrome-");
			Assert.assertEquals(plist.get(3).getName(), "svchost");
		}
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML\\shell\\open\\command
	 */
	@Test(groups={"ut"})
	public void testFirefoxStandardWindowsInstallation() throws IOException {
		
		Path profilePath = Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile");
		Stream<Path> profiles = Files.list(Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile"));
		
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			MockedStatic<Paths> mockedPaths = mockStatic(Paths.class);
			MockedStatic<Files> mockedFiles = mockStatic(Files.class)
			) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
			mockedPaths.when(() -> Paths.get(contains("Profiles"))).thenReturn(profilePath);
			mockedFiles.when(() -> Files.list(profilePath)).thenReturn(profiles);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "--version", "|", "more"})).thenReturn("Mozilla Firefox 56.0");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("""
HKEY_CLASSES_ROOT\\FirefoxHTML
Fin de la recherche : 2 correspondance(s) trouvée(s).""");

			OSUtility.refreshBrowserList();
			List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
			Assert.assertTrue(browsers.contains(BrowserType.FIREFOX));
		}
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML-308046\\shell\\open\\command
	 */
	@Test(groups={"ut"})
	public void testFirefoxServerWindowsInstallation() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML-AC250DEAA7389F99\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "--version", "|", "more"})).thenReturn("Mozilla Firefox 56.0");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("""
HKEY_CLASSES_ROOT\\FirefoxHTML-AC250DEAA7389F99
Fin de la recherche : 2 correspondance(s) trouvée(s).""");


			OSUtility.refreshBrowserList();
			List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
			Assert.assertTrue(browsers.contains(BrowserType.FIREFOX));
		}
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML-308046\\shell\\open\\command
	 */
	@Test(groups={"ut"})
	public void testSeveralFirefoxInstallations() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe")).thenReturn(path);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML-AC250DEAA7389F99\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe\" -osint -url \"%1\"");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "--version", "|", "more"})).thenReturn("Mozilla Firefox 56.0");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe", "--version", "|", "more"})).thenReturn("Mozilla Firefox 55.0");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("""
HKEY_CLASSES_ROOT\\FirefoxHTML
HKEY_CLASSES_ROOT\\FirefoxHTML-AC250DEAA7389F99
Fin de la recherche : 2 correspondance(s) trouvée(s).""");

			OSUtility.refreshBrowserList();
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
			Assert.assertEquals(browsers.get(BrowserType.FIREFOX).size(), 2);
			Assert.assertTrue(browsers.get(BrowserType.FIREFOX).get(0).getPath().contains("Mozilla Firefox"));
		}
	}
	
	/**
	 * check that only valid installations are returned
	 */
	@Test(groups={"ut"})
	public void testSeveralFirefoxInstallationsMissingBrowser() {

		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe")).thenReturn(path2);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(path2.toFile()).thenReturn(browserFile2);
			when(browserFile.exists()).thenReturn(true);
			when(browserFile2.exists()).thenReturn(false);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\" -osint -url \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML-AC250DEAA7389F99\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe\" -osint -url \"%1\"");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "--version", "|", "more"})).thenReturn("Mozilla Firefox 56.0");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe", "--version", "|", "more"})).thenReturn("Mozilla Firefox 55.0");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("""
HKEY_CLASSES_ROOT\\FirefoxHTML
HKEY_CLASSES_ROOT\\FirefoxHTML-AC250DEAA7389F99
Fin de la recherche : 2 correspondance(s) trouvée(s).""");

			OSUtility.refreshBrowserList();
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
			Assert.assertEquals(browsers.get(BrowserType.FIREFOX).size(), 1);
			Assert.assertTrue(browsers.get(BrowserType.FIREFOX).get(0).getPath().contains("Mozilla Firefox"));
		}
	}
	
	/**
	 * Search chrome
	 */
	@Test(groups={"ut"})
	public void testChromeStandardWindowsInstallation() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

			OSUtility.refreshBrowserList();
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.CHROME));
			Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 1);
			Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
			Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "57.0");
		}
	}
	
	/**
	 * Issue #308: check we detect chrome beta on its location in registry
	 * Check also we get 2 chrome instances
	 */
	@Test(groups={"ut"})
	public void testChromeBetaAndStandardWindowsInstallation() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe\" -- \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome Beta", "version")).thenReturn("77.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenReturn("76.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

			OSUtility.refreshBrowserList(true); // force detecting beta browser
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.CHROME));
			Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 2);
			Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
			Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "76.0");
			Assert.assertEquals(browsers.get(BrowserType.CHROME).get(1).getVersion(), "77.0");
		}
	}
	/**
	 * issue #458: Check that even with 'discoverBetaBrowsers' set to false, we get all chrome browsers (filtering is done elsewhere)
	 */
	@Test(groups={"ut"})
	public void testChromeBetaNotDiscoveredStandardWindowsInstallation() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe\" -- \"%1\"");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome Beta", "version")).thenReturn("77.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenReturn("76.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

			OSUtility.refreshBrowserList();
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.CHROME));
			Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 2);
			Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
			Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "76.0");
			Assert.assertEquals(browsers.get(BrowserType.CHROME).get(1).getVersion(), "77.0");
		}
	}
	
	/**
	 * Search chrome but does not exist on file system
	 */
	@Test(groups={"ut"})
	public void testChromeNotReallyInstalled() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {
		
		mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(false);
		
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenThrow(Win32Exception.class);
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
		mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.CHROME));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 0);}
	}
	
	/**
	 * Search IE
	 */
	@Test(groups={"ut"})
	public void testIEStandardWindowsInstallation() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files\\Internet_Explorer\\IEXPLORE.EXE")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenReturn("C:\\Program Files\\Internet_Explorer\\IEXPLORE.EXE");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "svcVersion")).thenReturn("11.0.9600.18000");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

			OSUtility.refreshBrowserList();
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.INTERNET_EXPLORER));
			Assert.assertEquals(browsers.get(BrowserType.INTERNET_EXPLORER).size(), 1);
			Assert.assertNull(browsers.get(BrowserType.INTERNET_EXPLORER).get(0).getPath());
			Assert.assertEquals(browsers.get(BrowserType.INTERNET_EXPLORER).get(0).getVersion(), "11");
		}
	}
	
	/**
	 * Search Edge chromium
	 */
	@Test(groups={"ut"})
	public void testEdgeChromiumStandardWindowsInstallation() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {
		
		mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe")).thenReturn(path);
		mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application", "msedge.exe")).thenReturn(path2);
		when(path.toFile()).thenReturn(browserFile);
		when(path.toFile()).thenReturn(browserFile);
		when(path2.toString()).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe");
		when(browserFile.exists()).thenReturn(true);
		
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application");
		mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "Version")).thenReturn("92.0.902.780");
		mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.EDGE));
		Assert.assertEquals(browsers.get(BrowserType.EDGE).size(), 1);
		Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getPath(), "C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe");
		Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getVersion(), "92.0");}
	}
	
	/**
	 * Search Edge chromium in HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\App Paths\msedge.exe
	 * In this case, version will be found in folder structure
	 */
	@Test(groups={"ut"})
	public void testEdgeChromiumStandardWindowsInstallation2() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			// mock search of browser version in folder structure
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);
			when(browserFile.listFiles()).thenReturn(new File[]{exeFile, versionFolder}); // list of files in C:\Program Files (x86)\Microsoft\Edge\Application
			when(versionFolder.isDirectory()).thenReturn(true);
			when(versionFolder.getName()).thenReturn("104.0.1293.70");
			when(exeFile.isDirectory()).thenReturn(false);
			when(exeFile.getName()).thenReturn("msedge.exe");

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe")).thenReturn(path);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application", "msedge.exe")).thenReturn(path2);
			when(path2.toString()).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe");

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\App Paths\\msedge.exe", "Path")).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "Version")).thenThrow(Win32Exception.class);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

			OSUtility.refreshBrowserList();
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.EDGE));
			Assert.assertEquals(browsers.get(BrowserType.EDGE).size(), 1);
			Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getPath(), "C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe");
			Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getVersion(), "104.0");
		}
	}
	
	/**
	 * Search Edge chromium Beta
	 */
	@Test(groups={"ut"})
	public void testEdgeChromiumBetaStandardWindowsInstallation() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<Advapi32Util> mockedAdvapi = mockStatic(Advapi32Util.class);
			 MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)
		) {

			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe")).thenReturn(path);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application", "msedge.exe")).thenReturn(path2);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge Beta\\Application\\msedge.exe")).thenReturn(path3);
			mockedPaths.when(() -> Paths.get("C:\\Program Files (x86)\\Microsoft_\\Edge Beta\\Application", "msedge.exe")).thenReturn(path4);
			when(path.toFile()).thenReturn(browserFile);
			when(path2.toFile()).thenReturn(browserFile);
			when(path3.toFile()).thenReturn(browserFile);
			when(path4.toFile()).thenReturn(browserFile);
			when(path2.toString()).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe");
			when(path4.toString()).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge Beta\\Application\\msedge.exe");
			when(browserFile.exists()).thenReturn(true);

			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation")).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge\\Application");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "Version")).thenReturn("92.0.902.780");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge Beta", "InstallLocation")).thenReturn("C:\\Program Files (x86)\\Microsoft_\\Edge Beta\\Application");
			mockedAdvapi.when(() -> Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge Beta", "Version")).thenReturn("93.0.902.780");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"REG", "QUERY", "HKCR", "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

			OSUtility.refreshBrowserList(true); // search beta browsers
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.EDGE));
			Assert.assertEquals(browsers.get(BrowserType.EDGE).size(), 2);
			Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getPath(), "C:\\Program Files (x86)\\Microsoft_\\Edge\\Application\\msedge.exe");
			Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getVersion(), "92.0");
			Assert.assertEquals(browsers.get(BrowserType.EDGE).get(1).getPath(), "C:\\Program Files (x86)\\Microsoft_\\Edge Beta\\Application\\msedge.exe");
			Assert.assertEquals(browsers.get(BrowserType.EDGE).get(1).getVersion(), "93.0");
		}
	}
	
}
