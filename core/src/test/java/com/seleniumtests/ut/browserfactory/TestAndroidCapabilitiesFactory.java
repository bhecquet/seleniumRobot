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
package com.seleniumtests.ut.browserfactory;

import java.io.File;
import java.util.Arrays;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.AndroidCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
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
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.CHROME.toString());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.CHROME.toString().toLowerCase());
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_VERSION), "8.0");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.DEVICE_NAME), "Samsung Galasy S8");
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_PACKAGE), "com.infotel.mobile"); // from exampleConfigGenericParams.xml when tu.xml is executed, else, null
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY), "com.infotel.mobile.StartActivity"); // from exampleConfigGenericParams.xml when tu.xml is executed, else, null
		Assert.assertNull(capa.getCapability(MobileCapabilityType.FULL_RESET));
	}
	
	/**
	 * issue #367: check automationName is set in capabilities when overriden
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithAutomationName() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.CHROME.toString());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setAutomationName("UiAutomator1");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.CHROME.toString().toLowerCase());
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "UiAutomator1");
		Assert.assertNull(capa.getCapability(MobileCapabilityType.FULL_RESET));
	}
	
	/**
	 * Check default behaviour when node tags are defined in grid mode
	 * tags are transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInGridMode() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.CHROME.toString());
		context.setNodeTags("foo,bar");
		context.setRunMode("grid");
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.NODE_TAGS), Arrays.asList("foo", "bar"));
	}
	
	/**
	 * Check default behaviour when node tags are defined in local mode
	 * tags are not transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInLocalMode() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.CHROME.toString());
		context.setNodeTags("foo,bar");
		context.setRunMode("local");
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();

		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultAndroidBrowserCapabilities() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.BROWSER.toString());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.BROWSER.toString().toLowerCase());
		Assert.assertNull(capa.getCapability(ChromeOptions.CAPABILITY));
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_VERSION), "8.0");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.DEVICE_NAME), "Samsung Galasy S8");
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_PACKAGE), "com.infotel.mobile"); // from exampleConfigGenericParams.xml when tu.xml is executed, else, null
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY), "com.infotel.mobile.StartActivity"); // from exampleConfigGenericParams.xml when tu.xml is executed, else, null
		Assert.assertNull(capa.getCapability(MobileCapabilityType.FULL_RESET));
	}
	
	/**
	 * Check mobile test with app
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplication() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setAppPackage("appPackage");
		context.setAppActivity("appActivity");
		context.setApp("com.covea.mobileapp");
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "");
		Assert.assertEquals(capa.getCapability("app"), "com.covea.mobileapp");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Appium");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.PLATFORM_VERSION), "8.0");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.DEVICE_NAME), "Samsung Galasy S8");
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.FULL_RESET), true);
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_PACKAGE), "appPackage");
		Assert.assertEquals(capa.getCapability(AndroidMobileCapabilityType.APP_ACTIVITY), "appActivity");
	}
	
	/**
	 * Check mobile test with app relative path => check absolute path is set in capabilities 
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithRelativeApplicationPath() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setAppPackage("appPackage");
		context.setAppActivity("appActivity");
		context.setApp("data/core/app.apk");
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "");
		logger.info("app path: " + capa.getCapability("app"));
		Assert.assertTrue(capa.getCapability("app").toString().contains("/data/core/app.apk"));
	}
	
	/**
	 * Check mobile test with app relative path => check absolute path is set in capabilities
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithAbsoluteApplicationPath() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setAppPackage("appPackage");
		context.setAppActivity("appActivity");
		String path = new File("data/core/app.apk").getAbsolutePath();
		context.setApp(path);
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "");
		Assert.assertEquals(capa.getCapability("app"), path.replace("\\", "/"));
	}
	
	/**
	 * Check mobile test with app
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplicationOverrideFullReset() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setAppPackage("appPackage");
		context.setAppActivity("appActivity");
		context.setFullReset(false);
		context.setApp("com.covea.mobileapp");
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.FULL_RESET), false);
	}
	
	/**
	 * Check automationName capability with android < 4
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplicationOldAndroid() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setMobilePlatformVersion("2.3");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S1");
		context.setAppPackage("appPackage");
		context.setAppActivity("appActivity");
		context.setFullReset(true);
		context.setApp("com.covea.mobileapp");
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "Selendroid");
	}
	
	/**
	 * Check automationName capability with android < 6
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithApplicationOldAndroid2() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setMobilePlatformVersion("5.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S5");
		context.setAppPackage("appPackage");
		context.setAppActivity("appActivity");
		context.setFullReset(true);
		context.setApp("com.covea.mobileapp");
		DriverConfig config = new DriverConfig(context);
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(MobileCapabilityType.AUTOMATION_NAME), "UiAutomator1");
	}
}
