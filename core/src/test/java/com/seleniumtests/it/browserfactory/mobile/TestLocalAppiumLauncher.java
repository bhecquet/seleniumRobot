package com.seleniumtests.it.browserfactory.mobile;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.mobile.LocalAppiumLauncher;
import com.seleniumtests.customexception.ConfigurationException;

public class TestLocalAppiumLauncher extends GenericTest {

	@Test(groups={"it"})
	public void testAppiumStartup() {
		try {
			LocalAppiumLauncher appium = new LocalAppiumLauncher();
			appium.startAppium();
			appium.stopAppium();
		} catch (ConfigurationException e) {
			throw new SkipException("Test skipped, appium not correctly configured", e);
		}
	}
}
