/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.util.osutility;

import static com.seleniumtests.core.SeleniumTestsContext.CHROME_BINARY_PATH;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Platform;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.Reporter;
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
import com.sun.jna.platform.win32.Advapi32Util;

@PrepareForTest({Advapi32Util.class, OSUtilityUnix.class, OSUtilityFactory.class, OSCommand.class, Paths.class, BrowserInfo.class, OSUtility.class})
public class TestLinuxOsUtility extends MockitoTest {

    @Mock
    private Path path;

    @Mock
    private File browserFile;

    @Mock
    private Path path2;

    @Mock
    private File browserFile2;


    @BeforeClass(groups = {"ut"})
    public void isWindows() throws Exception {
        PowerMockito.mockStatic(OSUtility.class);
        when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.LINUX);
        PowerMockito.doCallRealMethod().when(OSUtility.class, "refreshBrowserList", false);
        PowerMockito.doCallRealMethod().when(OSUtility.class, "resetInstalledBrowsersWithVersion");
        when(OSUtility.getInstalledBrowsersWithVersion(false)).thenCallRealMethod();
    }

    @Test(groups = {"ut"})
    public void testGetProcessPidByListenPort() {
        PowerMockito.mockStatic(OSCommand.class);
        when(OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("tcp        0      0 0.0.0.0:48000           0.0.0.0:*               LISTEN      1421/nimbus(control\r\n" +
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

    /**
     * Check we don't match if is not listening
     */
    @Test(groups = {"ut"})
    public void testGetProcessPidByListenPort2() {
        PowerMockito.mockStatic(OSCommand.class);
        when(OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("tcp        0      0 0.0.0.0:48000           0.0.0.0:*               LISTEN      1421/nimbus(control\r\n" +
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

    /**
     * Check we don't match if port is remote
     */
    @Test(groups = {"ut"})
    public void testGetProcessPidByListenPort3() {
        PowerMockito.mockStatic(OSCommand.class);
        when(OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("tcp        0      0 0.0.0.0:48000           0.0.0.0:*               LISTEN      1421/nimbus(control\r\n" +
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

    @Test(groups = {"ut"})
    public void testGetProcessPidByListenPortNotFound() {

        PowerMockito.mockStatic(OSCommand.class);
        when(OSCommand.executeCommandAndWait("netstat -anp")).thenReturn("");

        Assert.assertNull(new OSUtilityUnix().getProcessIdByListeningPort(51239));
    }

    /**
     * Check no error is raised when no browser is installed (issue #128)
     */
    @Test(groups = {"ut"})
    public void testNoBrowserInstalled() {
        PowerMockito.mockStatic(OSCommand.class);
        PowerMockito.mockStatic(Paths.class);


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

    @Test(groups = {"ut"})
    public void testFirefoxStandardInstallation() {
        PowerMockito.mockStatic(OSCommand.class);
        PowerMockito.mockStatic(Paths.class);


        when(Paths.get("/usr/local/bin/firefox")).thenReturn(path);
        when(path.toFile()).thenReturn(browserFile);
        when(browserFile.exists()).thenReturn(true);

        when(OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/local/bin/firefox");
        when(OSCommand.executeCommandAndWait("firefox --version | more")).thenReturn("Mozilla Firefox 56.0");

        when(OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
        when(OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");
        when(OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/bin/which: no google-chrome in (/usr/local/sbin)");


        Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
        assertTrue(browsers.containsKey(BrowserType.FIREFOX));
    }

    @Test(groups = {"ut"})
    public void testChromeStandardInstallation() {
        PowerMockito.mockStatic(OSCommand.class);
        PowerMockito.mockStatic(Paths.class);

        when(Paths.get("/usr/local/bin/google-chrome")).thenReturn(path);
        when(path.toFile()).thenReturn(browserFile);
        when(browserFile.exists()).thenReturn(true);

        when(OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/local/bin/google-chrome");
        when(OSCommand.executeCommandAndWait(new String[]{"google-chrome", "--version"})).thenReturn("Google Chrome 57.0.2987.110");

        when(OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
        when(OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
        when(OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");

        Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().discoverInstalledBrowsersWithVersion();
        assertTrue(browsers.containsKey(BrowserType.CHROME));
    }

    @Test(groups = {"ut"})
    public void testChromeSpecialBinaryInstallation() {
        PowerMockito.mockStatic(OSCommand.class);
        PowerMockito.mockStatic(Paths.class);

        when(Paths.get("/usr/local/bin/google-chrome")).thenReturn(path);
        when(path.toFile()).thenReturn(browserFile);
        when(browserFile.exists()).thenReturn(true);

        when(OSUtility.getChromeVersion(anyString())).thenReturn("Google Chrome 57.0.2987.110");
        when(OSUtility.extractChromeOrChromiumVersion(anyString())).thenReturn("57.0");

        when(OSCommand.executeCommandAndWait("which google-chrome")).thenReturn("/usr/local/bin/google-chrome");
        when(OSCommand.executeCommandAndWait(new String[]{"/usr/local/bin/google-chrome", "--version"})).thenReturn("Google Chrome 57.0.2987.110");

        when(OSCommand.executeCommandAndWait("which firefox")).thenReturn("/usr/bin/which: no firefox in (/usr/local/sbin)");
        when(OSCommand.executeCommandAndWait("which iceweasel")).thenReturn("/usr/bin/which: no iceweasel in (/usr/local/sbin)");
        when(OSCommand.executeCommandAndWait("which chromium-browser")).thenReturn("/usr/bin/which: no chromium-browser in (/usr/local/sbin)");

        SeleniumTestsContextManager.getThreadContext().setAttribute(CHROME_BINARY_PATH, "/usr/local/bin/google-chrome");

        OSUtility.resetInstalledBrowsersWithVersion();
        SeleniumTestsContextManager.getThreadContext().configureContext(Reporter.getCurrentTestResult());
        Map<BrowserType, List<BrowserInfo>> browsers = new OSUtilityUnix().getInstalledBrowsersWithVersion();

        assertEquals(browsers.size(), 3);
    }
}
