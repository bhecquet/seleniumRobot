package com.seleniumtests.browserfactory;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverConfig;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

public class WindowsAppCapabilitiesFactory extends ICapabilitiesFactory {

    public WindowsAppCapabilitiesFactory(DriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public MutableCapabilities createCapabilities() {
        MutableCapabilities capabilities = new MutableCapabilities((Map.of("platformName", "windows",
                "appium:automationName", "FlaUI")));

        // path to the app that will be started
        if (webDriverConfig.getApp() != null && !webDriverConfig.getApp().isEmpty()) {
            capabilities.setCapability("appium:app", webDriverConfig.getApp());

        // name of the application window
        } else if (webDriverConfig.getAppActivity() != null && !webDriverConfig.getAppActivity().isEmpty()) {
            capabilities.setCapability("appium:appTopLevelWindowTitleMatch", webDriverConfig.getAppActivity());
        } else {
            throw new ConfigurationException("Either 'app' or 'appActivity' must be provided");
        }

        return capabilities;
    }
}
