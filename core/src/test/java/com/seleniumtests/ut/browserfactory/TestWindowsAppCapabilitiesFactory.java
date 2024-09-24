package com.seleniumtests.ut.browserfactory;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.WindowsAppCapabilitiesFactory;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class TestWindowsAppCapabilitiesFactory extends MockitoTest {

    @Mock
    private DriverConfig config;

    @Mock
    private SeleniumTestsContext context;

    @Mock
    private OSUtilityWindows osUtility;

    private MockedStatic mockedOsUtility;
    private MockedStatic mockedOsUtilityFactory;

    @BeforeMethod(groups= {"ut"})
    public void init() {

        mockedOsUtility = mockStatic(OSUtility.class);
        mockedOsUtility.when(() -> OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);

        mockedOsUtilityFactory = mockStatic(OSUtilityFactory.class);
        mockedOsUtilityFactory.when(() -> OSUtilityFactory.getInstance()).thenReturn(osUtility);

        when(osUtility.getProgramExtension()).thenReturn(".exe");

        when(config.getTestContext()).thenReturn(context);
        when(config.getMode()).thenReturn(DriverMode.LOCAL);
        when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));

    }

    @AfterMethod(groups = "ut", alwaysRun = true)
    private void closeMocks() {
        mockedOsUtilityFactory.close();
        mockedOsUtility.close();
    }

    @Test(groups = {"ut"})
    public void testCreateCapabilitiesWithApp() {
        when(config.getApp()).thenReturn("notepad");

        Capabilities capabilities = new WindowsAppCapabilitiesFactory(config).createCapabilities();
        Assert.assertEquals(capabilities.getCapability("platformName").toString(), "windows");
        Assert.assertEquals(capabilities.getCapability("appium:automationName"), "FlaUI");
        Assert.assertEquals(capabilities.getCapability("appium:app"), "notepad");
    }

    @Test(groups = {"ut"})
    public void testCreateCapabilitiesWithLaunchedApp() {
        when(config.getAppActivity()).thenReturn("notepad");

        Capabilities capabilities = new WindowsAppCapabilitiesFactory(config).createCapabilities();
        Assert.assertEquals(capabilities.getCapability("platformName").toString(), "windows");
        Assert.assertEquals(capabilities.getCapability("appium:automationName"), "FlaUI");
        Assert.assertEquals(capabilities.getCapability("appium:appTopLevelWindowTitleMatch"), "notepad");
    }

    @Test(groups = {"ut"}, expectedExceptions = ConfigurationException.class)
    public void testCreateCapabilitiesNoApp() {
        Capabilities capabilities = new WindowsAppCapabilitiesFactory(config).createCapabilities();
    }
}
