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

import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
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
import org.openqa.selenium.remote.CapabilityType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.FirefoxCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;

@PrepareForTest({OSUtility.class, BrowserInfo.class})
public class TestFirefoxCapabilitiesFactory extends MockitoTest {
	
	Map<BrowserType, List<BrowserInfo>> browserInfos;

	@Mock
	private DriverConfig config;

	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private SeleniumTestsContext context;
	

	@BeforeMethod(groups= {"ut"})
	public void init() {
		browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, "47.0", "/usr/bin/firefox", false)));
		PowerMockito.mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		when(config.getTestContext()).thenReturn(context);
		when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));
		when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(capa.is(CapabilityType.ACCEPT_INSECURE_CERTS));
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
		Assert.assertEquals(capa.getBrowserVersion(), "");
		Assert.assertEquals(capa.getCapability(CapabilityType.PROXY), proxyConfig);
		Assert.assertNull(((Map<?,?>)(((FirefoxOptions)capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("args"));
	}

	/**
	 * Check default behaviour when node tags are defined in grid mode
	 * tags are transferred to driver
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInGridMode() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(Arrays.asList("foo", "bar"));
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.NODE_TAGS), Arrays.asList("foo", "bar"));
	}
	
	/**
	 * Check default behaviour when node tags are defined in local mode
	 * tags are not transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInLocalMode() {
		
		when(config.getNodeTags()).thenReturn(Arrays.asList("foo", "bar"));
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatformName(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithHeadless() {
		
		when(config.isHeadlessBrowser()).thenReturn(true);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((FirefoxOptions)capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("args").toString(), "[-headless, --window-size=1280,1024, --width=1280, --height=1024]");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getBrowserVersion()).thenReturn("60.0");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getBrowserVersion(), "60.0");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultFirefoxCapabilities() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
		when(config.isSetAssumeUntrustedCertificateIssuer()).thenReturn(true);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "firefox");
		Assert.assertEquals(capa.getCapability(FirefoxDriver.Capability.MARIONETTE), false);
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Field fieldAcceptUntrustedCerts = FirefoxProfile.class.getDeclaredField("acceptUntrustedCerts");
		fieldAcceptUntrustedCerts.setAccessible(true);
		Assert.assertTrue((boolean) fieldAcceptUntrustedCerts.get(profile));
		Field fieldUntrustedCertIssuer = FirefoxProfile.class.getDeclaredField("untrustedCertIssuer");
		fieldUntrustedCertIssuer.setAccessible(true);
		Assert.assertTrue((boolean) fieldUntrustedCertIssuer.get(profile));
		
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.Window.QueryInterface", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.Window.frameElement.get", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.HTMLDocument.compatMode.get", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getStringPreference("capability.policy.default.Document.compatMode.get", ""), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(profile.getIntegerPreference("dom.max_chrome_script_run_time", 100), 0);
		Assert.assertEquals(profile.getIntegerPreference("dom.max_script_run_time", 100), 0);
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideUserAgent() {
		
		when(config.getUserAgentOverride()).thenReturn("FIREFOX 55");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("general.useragent.override", ""), "FIREFOX 55");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideBinPath() {
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.getFirefoxBinPath()).thenReturn("/opt/firefox/bin/firefox");
		
		// SeleniumTestsContext class adds a browserInfo when binary path is set
		Map<BrowserType, List<BrowserInfo>> updatedBrowserInfos = new HashMap<>();
		updatedBrowserInfos.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, "47.0", "", false), 
																	new BrowserInfo(BrowserType.FIREFOX, "44.0", "/opt/firefox/bin/firefox", false)));

		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((FirefoxOptions)capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("binary") , "/opt/firefox/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesStandardBinPath() {
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();

		Assert.assertEquals(((Map<?,?>)(((FirefoxOptions)capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("binary"), "/usr/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideNtlmAuth() {
		
		when(config.getNtlmAuthTrustedUris()).thenReturn("uri://uri.ntlm");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("network.automatic-ntlm-auth.trusted-uris", ""), "uri://uri.ntlm");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideDownloadDir() {
		
		when(config.getBrowserDownloadDir()).thenReturn("/home/download");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
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
	public void testCreateFirefoxCapabilitiesNoOverrideDownloadDirRemote() {
		
		when(config.getBrowserDownloadDir()).thenReturn("/home/download");
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		FirefoxProfile profile = (FirefoxProfile)capa.getCapability(FirefoxDriver.Capability.PROFILE);
		
		// check profile
		Assert.assertEquals(profile.getStringPreference("browser.download.dir", ""), "");
		Assert.assertEquals(profile.getIntegerPreference("browser.download.folderList", 0), 0);
		Assert.assertEquals(profile.getStringPreference("browser.helperApps.neverAsk.saveToDisk", ""), "");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithDefaultProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getFirefoxProfilePath()).thenReturn(BrowserInfo.DEFAULT_BROWSER_PRODFILE);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to 'default'
		Assert.assertEquals(capa.getCapability("firefoxProfile"), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithUserProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getFirefoxProfilePath()).thenReturn("/home/user/profile");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to 'default'
		Assert.assertEquals(capa.getCapability("firefoxProfile"), "/home/user/profile");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithoutDefaultProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to 'default'
		Assert.assertNull(capa.getCapability("firefoxProfile"));
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWrongProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getFirefoxProfilePath()).thenReturn("foo");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to 'default'
		Assert.assertNull(capa.getCapability("firefoxProfile"));
	}
	
}
