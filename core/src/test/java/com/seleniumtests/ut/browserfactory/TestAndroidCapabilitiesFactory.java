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

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.options.BaseOptions;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
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
		context.setDeviceName("Samsung Galaxy S8");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertEquals(capa.getBrowserName(), BrowserType.CHROME.toString().toLowerCase());
		Assert.assertEquals(capa.getAutomationName().get(), "UIAutomator2");
		Assert.assertEquals(capa.getPlatformName(), Platform.ANDROID);
		Assert.assertEquals(capa.getPlatformVersion().get(), "8.0");
		Assert.assertEquals(new UiAutomator2Options(capa).getDeviceName().orElse(null), "Samsung Galaxy S8");
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
		Assert.assertFalse(capa.doesFullReset().isPresent());
	}
	
	/**
	 * issue #367: check automationName is NOT set in capabilities when overriden
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
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.CHROME.toString().toLowerCase());
		Assert.assertEquals(capa.getAutomationName().get(), "UIAutomator2");
		Assert.assertFalse(capa.doesFullReset().isPresent());
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
		context.setBrowser(BrowserType.CHROME.toString());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galaxy S8");
		context.setApp("");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), BrowserType.CHROME.toString().toLowerCase());
		Assert.assertEquals(capa.getAutomationName().get(), "UIAutomator2");
		Assert.assertEquals(capa.getPlatformName(), Platform.ANDROID);
		Assert.assertEquals(capa.getPlatformVersion().get(), "8.0");
		Assert.assertEquals(new UiAutomator2Options(capa).getDeviceName().get(), "Samsung Galaxy S8");
		Assert.assertFalse(capa.doesFullReset().isPresent());
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
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertNull(capa.getCapability(CapabilityType.BROWSER_NAME));
		Assert.assertEquals(new UiAutomator2Options(capa).getApp().get(), "com.covea.mobileapp");
		Assert.assertEquals(capa.getAutomationName().get(), "UIAutomator2");
		Assert.assertEquals(capa.getPlatformName(), Platform.ANDROID);
		Assert.assertEquals(capa.getPlatformVersion().get(), "8.0");
		Assert.assertEquals(new UiAutomator2Options(capa).getDeviceName().get(), "Samsung Galasy S8");
		Assert.assertTrue(new UiAutomator2Options(capa).doesFullReset().get());
		Assert.assertEquals(new UiAutomator2Options(capa).getAppPackage().get(), "appPackage");
		Assert.assertEquals(new UiAutomator2Options(capa).getAppActivity().get(), "appActivity");
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
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertNull(capa.getCapability(CapabilityType.BROWSER_NAME));
		Assert.assertTrue(new UiAutomator2Options(capa).getApp().orElse("").contains("/data/core/app.apk"));
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
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertNull(capa.getCapability(CapabilityType.BROWSER_NAME));
		Assert.assertEquals(new UiAutomator2Options(capa).getApp().orElse(""), path.replace("\\", "/"));
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
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertEquals(capa.doesFullReset().get(), false);
	}

	/**
	 * Check that when user sets a capability with 'appiumCaps' option, it's forwarded to appium
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithUserDefined() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.CHROME.toString());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setApp("");
		context.setAppiumCapabilities("key1=value1;key2=value2");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		BaseOptions capa = new BaseOptions(capaFactory.createCapabilities());
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + "key1"), "value1");
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + "key2"), "value2");

	}
	
	/**
	 * Check that when user sets a capability with 'appiumCaps' option which do not override an other option
	 */
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithUserDefinedOverride() {
		SeleniumTestsContext context = new SeleniumTestsContext(SeleniumTestsContextManager.getThreadContext());
		context.setBrowser(BrowserType.CHROME.toString());
		context.setMobilePlatformVersion("8.0");
		context.setPlatform("android");
		context.setDeviceName("Samsung Galasy S8");
		context.setApp("");
		context.setAppiumCapabilities("browserName=firefox;key2=value2");
		
		DriverConfig config = new DriverConfig(context);
		
		
		AndroidCapabilitiesFactory capaFactory = new AndroidCapabilitiesFactory(config);
		MutableCapabilities capa = capaFactory.createCapabilities();
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + CapabilityType.BROWSER_NAME), "firefox");
		
	}
}
