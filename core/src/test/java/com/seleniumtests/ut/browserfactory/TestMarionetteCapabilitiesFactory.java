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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.ChromeCapabilitiesFactory;
import com.seleniumtests.browserfactory.FirefoxCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;

@PrepareForTest({OSUtility.class})
public class TestMarionetteCapabilitiesFactory extends MockitoTest {

	@Mock
	private DriverConfig config;

	@Mock
	private Proxy proxyConfig;

	@Mock
	private SeleniumTestsContext context;

	@BeforeMethod(groups= {"ut"})
	public void init() {
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, "58.0", "/usr/bin/firefox", false)));
		PowerMockito.mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		Mockito.when(config.getTestContext()).thenReturn(context);
		Mockito.when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));
		Mockito.when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		Assert.assertTrue(capa.is(CapabilityType.TAKES_SCREENSHOT));
		Assert.assertTrue(capa.is(CapabilityType.ACCEPT_SSL_CERTS));
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
		Assert.assertEquals(capa.getBrowserVersion(), "");
		Assert.assertEquals(capa.getCapability(CapabilityType.PROXY), proxyConfig);
	}

	/**
	 * Check default behaviour when node tags are defined in grid mode
	 * tags are transferred to driver
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInGridMode() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getNodeTags()).thenReturn(Arrays.asList("foo", "bar"));
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.NODE_TAGS), Arrays.asList("foo", "bar"));
	}
	
	/**
	 * Check default behaviour when node tags are defined in local mode
	 * tags are not transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInLocalMode() {
		
		Mockito.when(config.getNodeTags()).thenReturn(Arrays.asList("foo", "bar"));
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatformName(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithJavascriptDisabled() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(false);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getBrowserVersion()).thenReturn("60.0");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getBrowserVersion(), "60.0");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultMarionetteCapabilities() {

		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Mockito.when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
		Mockito.when(config.isSetAssumeUntrustedCertificateIssuer()).thenReturn(true);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "firefox");
		Assert.assertEquals(capa.getCapability(FirefoxDriver.Capability.MARIONETTE), true);
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertTrue(profile.getBooleanPreference("webdriver_accept_untrusted_certs", false));
		Assert.assertTrue(profile.getBooleanPreference("webdriver_assume_untrusted_issuer", false));
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.Window.QueryInterface", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.Window.frameElement.get", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.HTMLDocument.compatMode.get", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.Document.compatMode.get", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getIntegerPreference("dom.max_chrome_script_run_time", 100), 0);
		Assert.assertEquals(profile.getIntegerPreference("dom.max_script_run_time", 100), 0);
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideUserAgent() {
		
		Mockito.when(config.getUserAgentOverride()).thenReturn("FIREFOX 55");
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("general.useragent.override", ""), "FIREFOX 55");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideBinPath() {
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Mockito.when(config.getFirefoxBinPath()).thenReturn("/opt/firefox/bin/firefox");
		
		// SeleniumTestsContext class adds a browserInfo when binary path is set
		Map<BrowserType, List<BrowserInfo>> updatedBrowserInfos = new HashMap<>();
		updatedBrowserInfos.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, "57.0", "", false), 
																	new BrowserInfo(BrowserType.FIREFOX, "58.0", "/opt/firefox/bin/firefox", false)));

		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((FirefoxOptions)capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("binary") , "/opt/firefox/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesStandardBinPath() {
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();

		Assert.assertEquals(((Map<?,?>)(((FirefoxOptions)capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("binary") , "/usr/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideNtlmAuth() {
		
		Mockito.when(config.getNtlmAuthTrustedUris()).thenReturn("uri://uri.ntlm");
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("network.automatic-ntlm-auth.trusted-uris", ""), "uri://uri.ntlm");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideDownloadDir() {
		
		Mockito.when(config.getBrowserDownloadDir()).thenReturn("/home/download");
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("browser.download.dir", ""), "/home/download");
		Assert.assertEquals(profile.getIntegerPreference("browser.download.folderList", 0), 2);
		Assert.assertEquals(profile.getBooleanPreference("browser.download.manager.showWhenStarting", true), false);
		Assert.assertEquals(profile.getStringPreference("browser.helperApps.neverAsk.saveToDisk", ""), "application/octet-stream,text/plain,application/pdf,application/zip,text/csv,text/html");
	}
	
	/**
	 * issue #365: Check DownloadDir is not set in remote
	 */
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesNoOverrideDownloadDirRemote() {
		
		Mockito.when(config.getBrowserDownloadDir()).thenReturn("/home/download");
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("browser.download.dir", ""), "");
		Assert.assertEquals(profile.getIntegerPreference("browser.download.folderList", 0), 0);
		Assert.assertEquals(profile.getStringPreference("browser.helperApps.neverAsk.saveToDisk", ""), "");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesStandardDriverPathLocal() {
		System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		try {
			Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
			
			new FirefoxCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertTrue(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY).replace(File.separator, "/").contains("/drivers/geckodriver"));
		} finally {
			System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideDriverPathLocal() {
		System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		try {
			Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
			Mockito.when(config.getGeckoDriverPath()).thenReturn("/opt/firefox/driver/geckodriver");
			
			new FirefoxCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY).replace(File.separator, "/"), "/opt/firefox/driver/geckodriver");
		} finally {
			System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesStandardDriverPathGrid() {
		System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertNull(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY));
	}

	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithDefaultProfile() {
		
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		Mockito.when(config.getFirefoxProfilePath()).thenReturn(BrowserInfo.DEFAULT_BROWSER_PRODFILE);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to 'default'
		Assert.assertEquals(capa.getCapability("firefoxProfile"), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithUserProfile() {
		
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		Mockito.when(config.getFirefoxProfilePath()).thenReturn("/home/user/profile");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to user profile
		Assert.assertEquals(capa.getCapability("firefoxProfile"), "/home/user/profile");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithoutDefaultProfile() {
		
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is no set when not requested
		Assert.assertNull(capa.getCapability("firefoxProfile"));
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWrongProfile() {
		
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		Mockito.when(config.getFirefoxProfilePath()).thenReturn("foo");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is not set if name is not valid
		Assert.assertNull(capa.getCapability("firefoxProfile"));
	}
	
}
