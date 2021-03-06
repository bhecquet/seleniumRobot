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

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.contains;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;
import com.seleniumtests.util.osutility.ProcessInfo;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

@PrepareForTest({Advapi32Util.class, OSUtilityWindows.class, OSCommand.class, Paths.class, BrowserInfo.class})
public class TestWindowsOsUtility extends MockitoTest {
	
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
	
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort() {

		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("netstat -aon")).thenReturn("Proto  Adresse locale         Adresse distante       État\r\n"
				+ "TCP    0.0.0.0:64360          0.0.0.0:0              LISTENING       39320\r\n" + 
				"  TCP    0.0.0.0:64362          0.0.0.0:0              LISTENING       26660\r\n" + 
				"  TCP    0.0.0.0:51239          0.0.0.0:0              LISTENING       22492\r\n" + 
				"  TCP    10.165.131.105:139     0.0.0.0:0              LISTENING       4\r\n" + 
				"  TCP    10.165.131.105:49244   192.168.62.10:8080     TIME_WAIT       0\r\n" + 
				"  TCP    10.165.131.105:49320   10.204.88.85:443       TIME_WAIT       0");
		
		Integer processPid = OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239);
		Assert.assertEquals((Integer)processPid, (Integer)22492);
	}
	
	/**
	 * Check we don't match if port is remote
	 */
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort2() {
		
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("netstat -aon")).thenReturn("Proto  Adresse locale         Adresse distante       État\r\n"
				+ "TCP    0.0.0.0:64360          0.0.0.0:0              LISTENING       39320\r\n" + 
				"  TCP    0.0.0.0:64362          0.0.0.0:0              LISTENING       26660\r\n" + 
				"  TCP    0.0.0.0:123          0.0.0.0:51239             LISTENING       22492\r\n" + 
				"  TCP    10.165.131.105:139     0.0.0.0:0              LISTENING       4\r\n" + 
				"  TCP    10.165.131.105:49244   192.168.62.10:8080     TIME_WAIT       0\r\n" + 
				"  TCP    10.165.131.105:49320   10.204.88.85:443       TIME_WAIT       0");
		
		Assert.assertNull(OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239));
	}
	
	/**
	 * Check we don't match if is not listening
	 */
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort3() {
		
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("netstat -aon")).thenReturn("Proto  Adresse locale         Adresse distante       État\r\n"
				+ "TCP    0.0.0.0:64360          0.0.0.0:0              LISTENING       39320\r\n" + 
				"  TCP    0.0.0.0:64362          0.0.0.0:0              LISTENING       26660\r\n" + 
				"  TCP    0.0.0.0:123          0.0.0.0:0             LISTENING       22492\r\n" + 
				"  TCP    10.165.131.105:139     0.0.0.0:0              LISTENING       4\r\n" + 
				"  TCP    10.165.131.105:51239   192.168.62.10:8080     TIME_WAIT       0\r\n" + 
				"  TCP    10.165.131.105:49320   10.204.88.85:443       TIME_WAIT       0");
		
		Assert.assertNull(OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239));
	}
	
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPortNotFound() {
		
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("netstat -aon")).thenReturn("");

		Assert.assertNull(OSUtilityFactory.getInstance().getProcessIdByListeningPort(51239));
	}
	
	@Test(groups={"ut"})
	public void testGetProcessList() {
		
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait(contains("system32\\tasklist.exe /NH /SVC"))).thenReturn("eclipse.exe                   6480 N/A\r\n" + 
				"javaw.exe                     7280 N/A\r\n" + 
				"chromedriver_2.45_chrome-    11252 N/A\r\n"
				+ "svchost.exe                   1784 CryptSvc, Dnscache, LanmanWorkstation,\r\n" + 
				"                                   NlaSvc, TermService, WinRM");
		List<ProcessInfo> plist= OSUtilityFactory.getInstance().getRunningProcessList();
		Assert.assertEquals(plist.size(), 4);
		Assert.assertEquals(plist.get(0).getName(), "eclipse");
		Assert.assertEquals(plist.get(0).getPid(), "6480");
		Assert.assertEquals(plist.get(1).getName(), "javaw");
		Assert.assertEquals(plist.get(2).getName(), "chromedriver_2.45_chrome-");
		Assert.assertEquals(plist.get(3).getName(), "svchost");
	}
	
	/**
	 * Search Firefox in registry at HKEY_CLASSES_ROOT\\FirefoxHTML\\shell\\open\\command
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testFirefoxStandardWindowsInstallation() throws IOException {
		
		Path profilePath = Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile");
		Stream<Path> profiles = Files.list(Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile"));
		
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		PowerMockito.mockStatic(Files.class);
		
		when(Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
		when(Paths.get(contains("Profiles"))).thenReturn(profilePath);
		when(Files.list(profilePath)).thenReturn(profiles);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);

		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
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
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testFirefoxServerWindowsInstallation() throws IOException {

		Path profilePath = Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile");
		Stream<Path> profiles = Files.list(Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile"));
		
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		PowerMockito.mockStatic(Files.class);
		
		when(Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
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
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testSeveralFirefoxInstallations() throws IOException {

		Path profilePath = Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile");
		Stream<Path> profiles = Files.list(Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile"));
		
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		PowerMockito.mockStatic(Files.class);
		
		when(Paths.get("C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe")).thenReturn(path);
		when(Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
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
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
		Assert.assertEquals(browsers.get(BrowserType.FIREFOX).size(), 2);
		Assert.assertTrue(browsers.get(BrowserType.FIREFOX).get(0).getPath().contains("Mozilla Firefox"));
	}
	
	/**
	 * check that only valid installations are returned
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testSeveralFirefoxInstallationsMissingBrowser() throws IOException {
		

		Path profilePath = Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile");
		Stream<Path> profiles = Files.list(Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "ffprofile"));
		
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		PowerMockito.mockStatic(Files.class);
		
		when(Paths.get("C:\\Program Files (x86)\\Mozilla2 Firefox\\firefox.exe")).thenReturn(path2);
		when(Paths.get("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(path2.toFile()).thenReturn(browserFile2);
		when(browserFile.exists()).thenReturn(true);
		when(browserFile2.exists()).thenReturn(false);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
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
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
		Assert.assertEquals(browsers.get(BrowserType.FIREFOX).size(), 1);
		Assert.assertTrue(browsers.get(BrowserType.FIREFOX).get(0).getPath().contains("Mozilla Firefox"));
	}
	
	/**
	 * Search chrome
	 */
	@Test(groups={"ut"})
	public void testChromeStandardWindowsInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);

		when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);

		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");

		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.CHROME));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 1);
		Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "57.0");
	}
	
	/**
	 * Check we get chrome version even if the key "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome" is not in registry
	 * Check version is get from parent folder
	 */
	/*@Test(groups={"ut"})
	public void testChromeStandardWindowsInstallationButNotInRegistry() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS);
		
		when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.CHROME));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 1);
		Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "78.0");
	}*/
	
	/**
	 * Issue #308: check we detect chrome beta on its location in registry
	 * Check also we get 2 chrome instances
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testChromeBetaAndStandardWindowsInstallation() throws Exception {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		
		PowerMockito.when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
		PowerMockito.when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome Beta", "version")).thenReturn("77.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenReturn("76.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList(true); // force detecting beta browser
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.CHROME));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 2);
		Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "76.0");
		Assert.assertEquals(browsers.get(BrowserType.CHROME).get(1).getVersion(), "77.0");
	}
	/**
	 * Issue #308: check we do not detect chrome beta on its location in registry when we only request mainstream browsers
	 * Check also we get 1 chrome instances
	 */
	@Test(groups={"ut"})
	public void testChromeBetaNotDiscoveredStandardWindowsInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		
		when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
		when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome Beta\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome Beta", "version")).thenReturn("77.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenReturn("76.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.CHROME));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 1);
		Assert.assertTrue(browsers.get(BrowserType.CHROME).get(0).getPath().contains("Application"));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).get(0).getVersion(), "76.0");
	}
	
	/**
	 * Search chrome but does not exist on file system
	 */
	@Test(groups={"ut"})
	public void testChromeNotReallyInstalled() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		
		when(Paths.get("C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(false);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenReturn("\"C:\\Program Files (x86)\\Google_\\Chrome\\Application\\chrome.exe\" -- \"%1\"");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version")).thenReturn("57.0.2987.110");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.CHROME));
		Assert.assertEquals(browsers.get(BrowserType.CHROME).size(), 0);
	}
	
	/**
	 * Search IE
	 */
	@Test(groups={"ut"})
	public void testIEStandardWindowsInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		
		when(Paths.get("C:\\Program Files\\Internet_Explorer\\IEXPLORE.EXE")).thenReturn(path);
		when(path.toFile()).thenReturn(browserFile);
		when(browserFile.exists()).thenReturn(true);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenReturn("C:\\Program Files\\Internet_Explorer\\IEXPLORE.EXE");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "svcVersion")).thenReturn("11.0.9600.18000");
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.INTERNET_EXPLORER));
		Assert.assertEquals(browsers.get(BrowserType.INTERNET_EXPLORER).size(), 1);
		Assert.assertNull(browsers.get(BrowserType.INTERNET_EXPLORER).get(0).getPath());
		Assert.assertEquals(browsers.get(BrowserType.INTERNET_EXPLORER).get(0).getVersion(), "11");
	}
	
	/**
	 * Search Edge chromium
	 */
	@Test(groups={"ut"})
	public void testEdgeChromiumStandardWindowsInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		when(OSCommand.executeCommandAndWait("powershell.exe \"(Get-AppxPackage Microsoft.MicrosoftEdge).Version\"")).thenReturn("44.18362.449.0");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertTrue(browsers.keySet().contains(BrowserType.EDGE));
		Assert.assertEquals(browsers.get(BrowserType.EDGE).size(), 1);
		Assert.assertNull(browsers.get(BrowserType.EDGE).get(0).getPath());
		Assert.assertEquals(browsers.get(BrowserType.EDGE).get(0).getVersion(), "44");
	}
	
	/**
	 * Error message is thrown on some old windows not supporting Get-AppxPackage.
	 * Moreover, we should not get Edge browser if it's not installed
	 */
	@Test(groups={"ut"})
	public void testEdgeChromiumOnOldWindows() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(Advapi32Util.class);
		PowerMockito.mockStatic(Paths.class);
		
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "")).thenThrow(Win32Exception.class); // chrome beta not installed
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "")).thenThrow(Win32Exception.class);
		when(Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber")).thenThrow(Win32Exception.class);
		when(OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"})).thenReturn("");
		when(OSCommand.executeCommandAndWait("powershell.exe \"(Get-AppxPackage Microsoft.MicrosoftEdge).Version\"")).thenReturn("Get-AppxPackage : The term 'Get-AppxPackage' is not recognized as the name of");
		
		OSUtility.refreshBrowserList();
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		Assert.assertFalse(browsers.keySet().contains(BrowserType.EDGE));
	}
}
