package com.seleniumtests.ut.browserfactory;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.browserfactory.AndroidCapabilitiesFactory;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.MobileCapabilityType;

public class TestAndroidCapabilitiesFactory {

	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilities() {
		DriverConfig config = new DriverConfig();
		config.setBrowser(BrowserType.FIREFOX);
		config.setMobilePlatformVersion("4.4");
		config.setApp("");
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory();
		DesiredCapabilities capa = capaFactory.createCapabilities(config);
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.FIREFOX);
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
	}
	
	/**
	 * Check mobile test with app
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilities_app() {
		DriverConfig config = new DriverConfig();
		config.setMobilePlatformVersion("4.4");
		config.setApp("com.infotel.infolidays");
		
		DesiredCapabilities tmpCap = new DesiredCapabilities();
		tmpCap.setCapability("app", "com.infotel.infolidays");
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory();
		DesiredCapabilities capa = capaFactory.createCapabilities(config);
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), null);
		Assert.assertEquals(capa.getCapability("app"), "com.infotel.infolidays");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
	}
}
