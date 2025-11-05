package com.seleniumtests.browserfactory;

import com.seleniumtests.driver.DriverConfig;

public class BrowserstackDriverFactory extends SeleniumGridDriverFactory {
    public BrowserstackDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }

    @Override
    protected ICapabilitiesFactory getCapabilitiesFactory() {
        return new BrowserStackCapabilitiesFactory(webDriverConfig);
    }
}
