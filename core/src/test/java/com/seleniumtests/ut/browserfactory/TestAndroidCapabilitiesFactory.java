/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.ut.browserfactory;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.AndroidCapabilitiesFactory;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

public class TestAndroidCapabilitiesFactory extends GenericTest {

	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultChromeCapabilities() {
		DriverConfig config = new DriverConfig();
		config.setBrowser(BrowserType.FIREFOX);
		config.setMobilePlatformVersion("8.0");
		config.setPlatform("android");
		config.setDeviceName("Samsung Galasy S8");
		config.setApp("");
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.FIREFOX.toString().toLowerCase());
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_VERSION), "8.0");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.DEVICE_NAME), "Samsung Galasy S8");
		Assert.assertNull(capa.getCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT));
		Assert.assertNull(capa.getCapability(AndroidMobileCapabilityType.APP_PACKAGE));
		Assert.assertNull(capa.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY));
		Assert.assertNull(capa.getCapability(MobileCapabilityType.FULL_RESET));
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultAndroidBrowserCapabilities() {
		DriverConfig config = new DriverConfig();
		config.setBrowser(BrowserType.BROWSER);
		config.setMobilePlatformVersion("8.0");
		config.setPlatform("android");
		config.setDeviceName("Samsung Galasy S8");
		config.setApp("");
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.BROWSER.toString().toLowerCase());
		Assert.assertNull(capa.getCapability(ChromeOptions.CAPABILITY));
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_VERSION), "8.0");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.DEVICE_NAME), "Samsung Galasy S8");
		Assert.assertNull(capa.getCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT));
		Assert.assertNull(capa.getCapability(AndroidMobileCapabilityType.APP_PACKAGE));
		Assert.assertNull(capa.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY));
		Assert.assertNull(capa.getCapability(MobileCapabilityType.FULL_RESET));
	}
	
	/**
	 * Check mobile test with app
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplication() {
		DriverConfig config = new DriverConfig();
		config.setMobilePlatformVersion("8.0");
		config.setPlatform("android");
		config.setDeviceName("Samsung Galasy S8");
		config.setAppPackage("appPackage");
		config.setAppActivity("appActivity");
		config.setApp("com.covea.mobileapp");
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "");
		Assert.assertEquals(capa.getCapability("app"), "com.covea.mobileapp");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_VERSION), "8.0");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.DEVICE_NAME), "Samsung Galasy S8");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.FULL_RESET), "false");
		Assert.assertNull(capa.getCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT));
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_PACKAGE), "appPackage");
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY), "appActivity");
	}
	
	/**
	 * Check mobile test with app
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplicationOverrideFullReset() {
		DriverConfig config = new DriverConfig();
		config.setMobilePlatformVersion("8.0");
		config.setPlatform("android");
		config.setDeviceName("Samsung Galasy S8");
		config.setAppPackage("appPackage");
		config.setAppActivity("appActivity");
		config.setFullReset(true);
		config.setApp("com.covea.mobileapp");
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.FULL_RESET), "true");
	}
	
	/**
	 * Check mobile test with app
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplicationOldAndroid() {
		DriverConfig config = new DriverConfig();
		config.setMobilePlatformVersion("2.3");
		config.setPlatform("android");
		config.setDeviceName("Samsung Galasy S1");
		config.setAppPackage("appPackage");
		config.setAppActivity("appActivity");
		config.setFullReset(true);
		config.setApp("com.covea.mobileapp");
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Selendroid");
	}
}
