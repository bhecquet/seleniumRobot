package com.seleniumtests.ut.browserfactory;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.BrowserStackCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

public class TestBrowserStackCapabilitiesFactory extends MockitoTest {

    public static final String CURRENT_CHROME_VERSION = "140.0";
    public static final String NEXT_CHROME_VERSION = "141.0";
    Map<BrowserType, List<BrowserInfo>> browserInfos;

    @Mock
    private DriverConfig config;

    @Mock
    private Proxy proxyConfig;

    @Mock
    private SeleniumTestsContext context;


    @BeforeMethod(groups= {"ut"})
    public void init() {

        when(config.getTestContext()).thenReturn(context);
        when(config.getMode()).thenReturn(DriverMode.BROWSERSTACK);
        when(config.getDebug()).thenReturn(List.of(DebugMode.NONE));
        when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
        when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(config.getAttachExistingDriverPort()).thenReturn(null);
        when(config.getOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
        when(config.getDownloadOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getDownloadOutputDirectory());
    }


    /**
     * Check default behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultCapabilities() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(config.getPlatform()).thenReturn("Windows 11");
        when(config.getProxy()).thenReturn(proxyConfig);
        when(config.getNodeTags()).thenReturn(new ArrayList<>());
        when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);

        MutableCapabilities capa = new BrowserStackCapabilitiesFactory(config).createCapabilities();

        Assert.assertTrue(capa.is(CapabilityType.ACCEPT_INSECURE_CERTS));
        Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
        Assert.assertEquals(capa.getBrowserVersion(), "");
        Assert.assertEquals(capa.getCapability(CapabilityType.PROXY), proxyConfig);
        Assert.assertEquals(capa.getBrowserName(), "CHROME");
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID), SeleniumTestsContext.getContextId().toString());
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.TEST_ID), "no-test");
        Assert.assertNotNull(capa.getCapability("bstack:options"));
        Assert.assertEquals(((Map<String, String>)capa.getCapability("bstack:options")).get("os"), "Windows");
        Assert.assertEquals(((Map<String, String>)capa.getCapability("bstack:options")).get("osVersion"), "11");
        Assert.assertEquals(((Map<String, String>)capa.getCapability("bstack:options")).get("sessionName"), "no-test");
        Assert.assertEquals(((Map<String, String>)capa.getCapability("bstack:options")).get("projectName"), "core");
        Assert.assertEquals(((Map<String, String>)capa.getCapability("bstack:options")).get("consoleLogs"), "info");
        Assert.assertEquals(((Map<String, String>)capa.getCapability("bstack:options")).get("buildName"), SeleniumTestsContext.getContextId().toString());

        // check chrome specific capabilities are present
        Assert.assertTrue(((Map<?,?>)((capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*"));

    }


}
