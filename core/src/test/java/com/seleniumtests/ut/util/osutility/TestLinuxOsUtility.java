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

import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.Platform;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityUnix;

public class TestLinuxOsUtility extends MockitoTest {
	
	@Mock
	private Path path;
	
	@Mock
	private File browserFile;
	
	@Mock
	private Path path2;
	
	@Mock
	private File browserFile2;

	private MockedStatic mockedOsUtility;
	
	@BeforeClass(groups = {"ut"})
    public void isWindows() throws Exception {
		mockedOsUtility = mockStatic(OSUtility.class);

		mockedOsUtility.when(() -> OSUtility.getCharset()).thenCallRealMethod();
		mockedOsUtility.when(() -> OSUtility.getCurrentPlatorm()).thenReturn(Platform.LINUX);
		mockedOsUtility.when(() -> OSUtility.refreshBrowserList(false)).thenCallRealMethod();
		mockedOsUtility.when(() -> OSUtility.resetInstalledBrowsersWithVersion()).thenCallRealMethod();
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(new HashMap<>());

		mockedOsUtility.when(() -> OSUtility.isWindows()).thenReturn(false);
		mockedOsUtility.when(() -> OSUtility.isMac()).thenReturn(false);
		mockedOsUtility.when(() -> OSUtility.extractChromeVersion(anyString())).thenCallRealMethod();
		mockedOsUtility.when(() -> OSUtility.extractChromeOrChromiumVersion(anyString())).thenCallRealMethod();
    }

	@AfterClass(groups = {"ut"})
	public void closeMocks() {
		mockedOsUtility.close();
	}
	
	

	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort() {
		

		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("tcp        0      0 0.0.0.0:48000           0.0.0.0:*               LISTEN      1421/nimbus(control\r\n" +
					"tcp        0      0 0.0.0.0:51239           0.0.0.0:*               LISTEN      22492/nimbus(spooler\r\n" +
					"tcp        0      0 0.0.0.0:10050           0.0.0.0:*               LISTEN      1382/zabbix_agentd\r\n" +
					"tcp      112      0 10.204.84.149:48624     10.204.90.112:5647      CLOSE_WAIT  1362/python\r\n" +
					"tcp      112      0 10.204.84.149:48836     10.204.90.112:5647      CLOSE_WAIT  1362/python\r\n" +
					"tcp6       0      0 10.204.84.149:39436     10.200.42.177:5555      TIME_WAIT   -\r\n" +
					"tcp6       0      0 10.204.84.149:52030     10.200.42.184:5555      TIME_WAIT   -\r\n" +
					"tcp6       0      0 10.204.84.149:60048     10.200.41.38:5555       TIME_WAIT   -\r\n" +
					"udp        0      0 0.0.0.0:68              0.0.0.0:*                           1301/dhclient\r\n" +
					"udp        0      0 0.0.0.0:111             0.0.0.0:*                           939/rpcbind\r\n"
			);

			Integer processPid = new OSUtilityUnix().getProcessIdByListeningPort(51239);

			Assert.assertEquals((Integer) processPid, (Integer) 22492);
		}
	}
	
	/**
	 * Check we don't match if is not listening
	 */
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort2() {


		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("tcp        0      0 0.0.0.0:48000           0.0.0.0:*               LISTEN      1421/nimbus(control\r\n" +
					"tcp        0      0 0.0.0.0:1234           0.0.0.0:*               LISTEN      22492/nimbus(spooler\r\n" +
					"tcp        0      0 0.0.0.0:10050           0.0.0.0:*               LISTEN      1382/zabbix_agentd\r\n" +
					"tcp      112      0 10.204.84.149:48624     10.204.90.112:5647      CLOSE_WAIT  1362/python\r\n" +
					"tcp      112      0 10.204.84.149:51239     10.204.90.112:5647      CLOSE_WAIT  1362/python\r\n" +
					"tcp6       0      0 10.204.84.149:39436     10.200.42.177:5555      TIME_WAIT   -\r\n" +
					"tcp6       0      0 10.204.84.149:52030     10.200.42.184:5555      TIME_WAIT   -\r\n" +
					"tcp6       0      0 10.204.84.149:60048     10.200.41.38:5555       TIME_WAIT   -\r\n" +
					"udp        0      0 0.0.0.0:48000           0.0.0.0:*                           1421/nimbus(control\r\n" +
					"udp        0      0 0.0.0.0:68              0.0.0.0:*                           1301/dhclient\r\n" +
					"udp        0      0 0.0.0.0:111             0.0.0.0:*                           939/rpcbind\r\n"
			);

			Assert.assertNull(new OSUtilityUnix().getProcessIdByListeningPort(51239));
		}
	}
	
	/**
	 * Check we don't match if port is remote
	 */
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPort3() {
		
		
		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("tcp        0      0 0.0.0.0:48000           0.0.0.0:*               LISTEN      1421/nimbus(control\r\n" +
					"tcp        0      0 0.0.0.0:1234           0.0.0.0:*               LISTEN      22492/nimbus(spooler\r\n" +
					"tcp        0      0 0.0.0.0:10050           0.0.0.0:51239          LISTEN      1382/zabbix_agentd\r\n" +
					"tcp      112      0 10.204.84.149:48624     10.204.90.112:5647      CLOSE_WAIT  1362/python\r\n" +
					"tcp      112      0 10.204.84.149:12345     10.204.90.112:5647      CLOSE_WAIT  1362/python\r\n" +
					"tcp6       0      0 10.204.84.149:39436     10.200.42.177:5555      TIME_WAIT   -\r\n" +
					"tcp6       0      0 10.204.84.149:52030     10.200.42.184:5555      TIME_WAIT   -\r\n" +
					"tcp6       0      0 10.204.84.149:60048     10.200.41.38:5555       TIME_WAIT   -\r\n" +
					"udp        0      0 0.0.0.0:48000           0.0.0.0:*                           1421/nimbus(control\r\n" +
					"udp        0      0 0.0.0.0:68              0.0.0.0:*                           1301/dhclient\r\n" +
					"udp        0      0 0.0.0.0:111             0.0.0.0:*                           939/rpcbind\r\n"
			);

			Assert.assertNull(new OSUtilityUnix().getProcessIdByListeningPort(51239));
		}
	}
	
	@Test(groups={"ut"})
	public void testGetProcessPidByListenPortNotFound() {
		
		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class)) {
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("");

			Assert.assertNull(new OSUtilityUnix().getProcessIdByListeningPort(51239));
		}
	}

	/**
	 * Check no error is raised when no browser is installed (issue #128)
	 */
	@Test(groups={"ut"})
	public void testNoBrowserInstalled() {
		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic mockedPaths = mockStatic(Paths.class);) {

			mockedPaths.when(() -> Paths.get("/usr/local/bin/firefox")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/bin/which: no google-chrome in (/usr/local/sbin)");

			Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
			Assert.assertEquals(browsers.size(), 1); // only HTMLUNIT
			Assert.assertFalse(browsers.containsKey(BrowserType.FIREFOX));
		}
	}

	@Test(groups={"ut"})
	public void testFirefoxStandardInstallation() {
		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic mockedPaths = mockStatic(Paths.class);) {
			
			mockedPaths.when(() -> Paths.get("/usr/local/bin/firefox")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/local/bin/firefox");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("firefox --version | more")).thenReturn("Mozilla Firefox 56.0");

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/bin/which: no google-chrome in (/usr/local/sbin)");

			Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.FIREFOX));
		}
	}
	
	@Test(groups={"ut"})
	public void testChromeStandardInstallation() {
		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic mockedPaths = mockStatic(Paths.class);) {
			
			mockedPaths.when(() -> Paths.get("/usr/local/bin/google-chrome")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedOsUtility.when(() -> OSUtility.getChromeVersion("google-chrome")).thenReturn("Google Chrome 103.0.2987.110");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/local/bin/google-chrome");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"google-chrome", "--version"})).thenReturn("Google Chrome 57.0.2987.110");

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");

			Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
			Assert.assertTrue(browsers.containsKey(BrowserType.CHROME));
		}
	}
	
	@Test(groups = {"ut"})
	public void testChromeSpecialBinaryInstallation() {
		try (MockedStatic mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic mockedPaths = mockStatic(Paths.class);) {

			mockedPaths.when(() -> Paths.get(anyString(), anyString())).thenCallRealMethod();
			mockedPaths.when(() -> Paths.get("/usr/local/bin/google-chrome")).thenReturn(path);
			mockedPaths.when(() -> Paths.get("/usr/local/bin/google-chrome-binary")).thenReturn(path);
			when(path.toFile()).thenReturn(browserFile);
			when(browserFile.exists()).thenReturn(true);

			mockedOsUtility.when(() -> OSUtility.getChromeVersion("google-chrome")).thenReturn("Google Chrome 57.0.2987.110");
			mockedOsUtility.when(() -> OSUtility.getChromeVersion("/usr/local/bin/google-chrome-binary")).thenReturn("Google Chrome 66.6.6666.666");

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/local/bin/google-chrome");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[]{"/usr/local/bin/google-chrome", "--version"})).thenReturn("Google Chrome 57.0.2987.110");

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");

			SeleniumTestsContextManager.getThreadContext().setAttribute(SeleniumTestsContext.CHROME_BINARY_PATH, "/usr/local/bin/google-chrome-binary");

			OSUtility.resetInstalledBrowsersWithVersion();
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenCallRealMethod(); // we want the real behaviour so that configureContext does its job
			SeleniumTestsContextManager.getThreadContext().configureContext(Reporter.getCurrentTestResult());
			Map<BrowserType, List<BrowserInfo>> browsersBinary = new OSUtilityUnix().getInstalledBrowsersWithVersion(false);

			assertEquals(browsersBinary.size(), 2); // chrome
			assertEquals(browsersBinary.get(BrowserType.CHROME).size(), 2);
			Assert.assertEquals(browsersBinary.get(BrowserType.CHROME).get(0).getVersion(), "57.0");
			Assert.assertEquals(browsersBinary.get(BrowserType.CHROME).get(1).getVersion(), "66.6");
		}
	}
}
