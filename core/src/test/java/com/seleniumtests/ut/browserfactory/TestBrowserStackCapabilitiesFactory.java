package com.seleniumtests.ut.browserfactory;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserStackCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.DebugMode;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mock;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

public class TestBrowserStackCapabilitiesFactory extends MockitoTest {


    @Mock
    private DriverConfig config;

    @Mock
    private Proxy proxyConfig;

    @Mock
    private SeleniumTestsContext context;


    @BeforeMethod(groups= {"ut"})
    public void init() {
        when(config.getHubUrl()).thenReturn(List.of("https://user:token@hub.browserstack.com/wd/hub"));
        when(config.getTestContext()).thenReturn(context);
        when(config.getMode()).thenReturn(DriverMode.BROWSERSTACK);
        when(config.getDebug()).thenReturn(List.of(DebugMode.NONE));
        when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
        when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
        when(config.getAttachExistingDriverPort()).thenReturn(null);
        when(config.getOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
        when(config.getDownloadOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getDownloadOutputDirectory());
    }


    private static String getBrowserstackOption(MutableCapabilities capa, String optionName) {
        return ((Map<String, String>) capa.getCapability("bstack:options")).get(optionName);
    }

    /**
     * Check default chrome on Windows behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultChromeWindowsCapabilities() {
        when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
        MutableCapabilities capa = testCreateDefaultWebDesktopCapabilities();
        Assert.assertEquals(capa.getBrowserName(), "chrome");

        // check chrome specific capabilities are present
        Assert.assertTrue(((Map<?,?>)((capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*"));
    }

    /**
     * Check default chrome on Mac behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultChromeMacCapabilities() {
        when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        when(config.getPlatform()).thenReturn("OS X Sequoia");
        when(config.getBrowserVersion()).thenReturn("140.0");
        when(config.getProxy()).thenReturn(proxyConfig);
        when(config.getNodeTags()).thenReturn(new ArrayList<>());
        when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);

        MutableCapabilities capa = new BrowserStackCapabilitiesFactory(config).createCapabilities();

        Assert.assertTrue(capa.is(CapabilityType.ACCEPT_INSECURE_CERTS));
        Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));

        Assert.assertNotNull(capa.getCapability("bstack:options"));
        Assert.assertEquals(getBrowserstackOption(capa, "os"), "OS X");
        Assert.assertEquals(getBrowserstackOption(capa, "osVersion"), "sequoia");
        Assert.assertEquals(getBrowserstackOption(capa, "browserVersion"), "140.0");
        Assert.assertEquals(capa.getBrowserName(), "chrome");
    }


    /**
     * Check default chrome on Mac behaviour
     */
    @Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = ".*Only Windows and Mac are supported desktop platforms \\('Windows xxx' or 'OS X xxx'\\).*")
    public void testCreateDefaultChromeLinuxCapabilities() {
        when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        when(config.getPlatform()).thenReturn("Linux Ubuntu 24.04");
        when(config.getProxy()).thenReturn(proxyConfig);
        when(config.getNodeTags()).thenReturn(new ArrayList<>());
        when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);

        new BrowserStackCapabilitiesFactory(config).createCapabilities();
    }


    /**
     * Check default firefox behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultFirefoxCapabilities() {
        when(config.getBrowserType()).thenReturn(BrowserType.FIREFOX);
        MutableCapabilities capa = testCreateDefaultWebDesktopCapabilities();
        Assert.assertEquals(capa.getBrowserName(), "firefox");

        // check chrome specific capabilities are present
        Assert.assertTrue(((Map<?,?>)((capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("prefs").toString().contains("{remote.active-protocols=1}"));
    }

    /**
     * Check default edge behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultEdgeCapabilities() {
        when(config.getBrowserType()).thenReturn(BrowserType.EDGE);
        MutableCapabilities capa = testCreateDefaultWebDesktopCapabilities();
        Assert.assertEquals(capa.getBrowserName(), "MicrosoftEdge");

        // check chrome specific capabilities are present
        Assert.assertTrue(((Map<?,?>)((capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().contains("--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*"));
    }

    /**
     * Check default safari behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultSafariCapabilities() {
        when(config.getBrowserType()).thenReturn(BrowserType.SAFARI);
        MutableCapabilities capa = testCreateDefaultWebDesktopCapabilities();
        Assert.assertEquals(capa.getBrowserName(), "safari");
    }

    /**
     * Check IE is refused
     */
    @Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = ".*Browser INTERNET_EXPLORER is not supported for desktop tests.*")
    public void testCreateDefaultIECapabilities() {

        when(config.getBrowserType()).thenReturn(BrowserType.INTERNET_EXPLORER);
        testCreateDefaultWebDesktopCapabilities();
    }



    @NotNull
    private MutableCapabilities testCreateDefaultWebDesktopCapabilities() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
        when(config.getPlatform()).thenReturn("Windows 11");
        when(config.getProxy()).thenReturn(proxyConfig);
        when(config.getNodeTags()).thenReturn(new ArrayList<>());
        when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);

        MutableCapabilities capa = new BrowserStackCapabilitiesFactory(config).createCapabilities();

        Assert.assertTrue(capa.is(CapabilityType.ACCEPT_INSECURE_CERTS));
        Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
        Assert.assertEquals(capa.getBrowserVersion(), "");
        Assert.assertEquals(capa.getCapability(CapabilityType.PROXY), proxyConfig);
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID), SeleniumTestsContext.getContextId().toString());
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.TEST_ID), "no-test");

        Assert.assertNotNull(capa.getCapability("bstack:options"));
        Assert.assertEquals(getBrowserstackOption(capa, "os"), "Windows");
        Assert.assertEquals(getBrowserstackOption(capa, "osVersion"), "11");
        Assert.assertEquals(getBrowserstackOption(capa, "sessionName"), "no-test");
        Assert.assertEquals(getBrowserstackOption(capa, "projectName"), "core");
        Assert.assertEquals(getBrowserstackOption(capa, "consoleLogs"), "info");
        Assert.assertEquals(getBrowserstackOption(capa, "userName"), "user");
        Assert.assertEquals(getBrowserstackOption(capa, "accessKey"), "token");
        Assert.assertEquals(getBrowserstackOption(capa, "buildName"), SeleniumTestsContext.getContextId().toString());
        return capa;
    }

    /**
     * Check default android behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultAndroidCapabilities() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        when(config.getPlatform()).thenReturn("Android");
        when(config.getDeviceName()).thenReturn("Samsung Galaxy");
        when(config.getMobilePlatformVersion()).thenReturn("14");
        when(config.getApp()).thenReturn("myApp.apk");
        when(config.getAppiumCapabilities()).thenReturn(new MutableCapabilities());
        when(config.getNodeTags()).thenReturn(new ArrayList<>());

        MutableCapabilities capa = new BrowserStackCapabilitiesFactory(config).createCapabilities();

        Assert.assertEquals(capa.getCapability("appium:app"), "myApp.apk");
        Assert.assertEquals(capa.getCapability("appium:automationName"), "UIAutomator2");
        Assert.assertEquals(capa.getCapability("appium:deviceName"), "Samsung Galaxy");
        Assert.assertEquals(capa.getCapability("appium:fullReset"), false);
        Assert.assertEquals(capa.getCapability("appium:platformVersion"), "14");
        Assert.assertEquals(capa.getCapability("platformName"), Platform.ANDROID);
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID), SeleniumTestsContext.getContextId().toString());
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.TEST_ID), "no-test");

        Assert.assertNotNull(capa.getCapability("bstack:options"));
        Assert.assertEquals(getBrowserstackOption(capa, "os"), "Android");
        Assert.assertEquals(getBrowserstackOption(capa, "osVersion"), "14");
        Assert.assertEquals(getBrowserstackOption(capa, "sessionName"), "no-test");
        Assert.assertEquals(getBrowserstackOption(capa, "projectName"), "core");
        Assert.assertEquals(getBrowserstackOption(capa, "consoleLogs"), "info");
        Assert.assertEquals(getBrowserstackOption(capa, "buildName"), SeleniumTestsContext.getContextId().toString());
        Assert.assertEquals(getBrowserstackOption(capa, "userName"), "user");
        Assert.assertEquals(getBrowserstackOption(capa, "accessKey"), "token");
    }

    /**
     * Check default iOS behaviour
     */
    @Test(groups={"ut"})
    public void testCreateDefaultIOSCapabilities() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        when(config.getPlatform()).thenReturn("iOS");
        when(config.getDeviceName()).thenReturn("iPhone 17");
        when(config.getMobilePlatformVersion()).thenReturn("26.0");
        when(config.getApp()).thenReturn("myApp.zip");
        when(config.getAppiumCapabilities()).thenReturn(new MutableCapabilities());
        when(config.getNodeTags()).thenReturn(new ArrayList<>());

        MutableCapabilities capa = new BrowserStackCapabilitiesFactory(config).createCapabilities();

        Assert.assertEquals(capa.getCapability("appium:app"), "myApp.zip");
        Assert.assertEquals(capa.getCapability("appium:automationName"), "XCuiTest");
        Assert.assertEquals(capa.getCapability("appium:deviceName"), "iPhone 17");
        Assert.assertEquals(capa.getCapability("appium:fullReset"), false);
        Assert.assertEquals(capa.getCapability("appium:platformVersion"), "26.0");
        Assert.assertEquals(capa.getCapability("platformName"), Platform.IOS);
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID), SeleniumTestsContext.getContextId().toString());
        Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.TEST_ID), "no-test");

        Assert.assertNotNull(capa.getCapability("bstack:options"));
        Assert.assertEquals(getBrowserstackOption(capa, "os"), "iOS");
        Assert.assertEquals(getBrowserstackOption(capa, "osVersion"), "26.0");
        Assert.assertEquals(getBrowserstackOption(capa, "sessionName"), "no-test");
        Assert.assertEquals(getBrowserstackOption(capa, "projectName"), "core");
        Assert.assertEquals(getBrowserstackOption(capa, "consoleLogs"), "info");
        Assert.assertEquals(getBrowserstackOption(capa, "buildName"), SeleniumTestsContext.getContextId().toString());
        Assert.assertEquals(getBrowserstackOption(capa, "userName"), "user");
        Assert.assertEquals(getBrowserstackOption(capa, "accessKey"), "token");
    }

    /**
     * Check default other OS behaviour
     */
    @Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = ".*Platform WindowsPhone is unknown for mobile tests.*")
    public void testCreateDefaultOtherPlatformCapabilities() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
        when(config.getPlatform()).thenReturn("WindowsPhone");
        when(config.getDeviceName()).thenReturn("Phone 17");
        when(config.getMobilePlatformVersion()).thenReturn("26.0");
        when(config.getApp()).thenReturn("myApp.zip");
        when(config.getAppiumCapabilities()).thenReturn(new MutableCapabilities());
        when(config.getNodeTags()).thenReturn(new ArrayList<>());

        new BrowserStackCapabilitiesFactory(config).createCapabilities();

    }

    /**
     * Check default android behaviour
     */
    @Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = ".*Wrong test format detected. Should be either mobile or desktop.*")
    public void testCreateCapabilitiesWrontTestType() {
        SeleniumTestsContextManager.getThreadContext().setTestType(TestType.NON_GUI);
        when(config.getPlatform()).thenReturn("Android");
        when(config.getAppiumCapabilities()).thenReturn(new MutableCapabilities());
        when(config.getProxy()).thenReturn(proxyConfig);

        new BrowserStackCapabilitiesFactory(config).createCapabilities();

    }

}
