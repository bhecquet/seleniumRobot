/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
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
import com.seleniumtests.browserfactory.FirefoxCapabilitiesFactory;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
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
		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion()).thenReturn(browserInfos);
		Mockito.when(config.getTestContext()).thenReturn(context);
		Mockito.when(context.isDevMode()).thenReturn(false);
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		Assert.assertTrue(capa.is(CapabilityType.TAKES_SCREENSHOT));
		Assert.assertTrue(capa.is(CapabilityType.ACCEPT_SSL_CERTS));
		Assert.assertEquals(capa.getVersion(), "");
		Assert.assertEquals(capa.getCapability(CapabilityType.PROXY), proxyConfig);
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatform(), Platform.WINDOWS);
		
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
		
		Assert.assertEquals(capa.getVersion(), "60.0");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultMarionetteCapabilities() {

		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Mockito.when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
		Mockito.when(config.isSetAssumeUntrustedCertificateIssuer()).thenReturn(true);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "firefox");
		Assert.assertEquals(capa.getCapability(FirefoxDriver.MARIONETTE), true);
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE);
		
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
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE);
		
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

		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion()).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(FirefoxDriver.BINARY), "/opt/firefox/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesStandardBinPath() {
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();

		Assert.assertEquals(capa.getCapability(FirefoxDriver.BINARY), "/usr/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideNtlmAuth() {
		
		Mockito.when(config.getNtlmAuthTrustedUris()).thenReturn("uri://uri.ntlm");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("network.automatic-ntlm-auth.trusted-uris", ""), "uri://uri.ntlm");
	}
	
	@Test(groups={"ut"})
	public void testCreateMarionetteCapabilitiesOverrideDownloadDir() {
		
		Mockito.when(config.getBrowserDownloadDir()).thenReturn("/home/download");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("browser.download.dir", ""), "/home/download");
		Assert.assertEquals(profile.getIntegerPreference("browser.download.folderList", 0), 2);
		Assert.assertEquals(profile.getBooleanPreference("browser.download.manager.showWhenStarting", true), false);
		Assert.assertEquals(profile.getStringPreference("browser.helperApps.neverAsk.saveToDisk", ""), "application/octet-stream,text/plain,application/pdf,application/zip,text/csv,text/html");
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
	
}
