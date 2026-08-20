/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.browserfactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
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

import static org.mockito.Mockito.*;

public class TestFirefoxCapabilitiesFactory extends MockitoTest {

	@Mock
	private DriverConfig config;

	@Mock
	private Proxy proxyConfig;

	@Mock
	private SeleniumTestsContext context;

	private MockedStatic<OSUtility> mockedOsUtility;

	@BeforeMethod(groups= {"ut"})
	public void init() {
		mockedOsUtility = mockStatic(OSUtility.class, CALLS_REAL_METHODS);

		Map<BrowserType, List<BrowserInfo>> browserInfos = new EnumMap<>(BrowserType.class);
		browserInfos.put(BrowserType.FIREFOX, List.of(new BrowserInfo(BrowserType.FIREFOX, "58.0", "/usr/bin/firefox", false)));

		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		when(config.getTestContext()).thenReturn(context);
		when(config.getDebug()).thenReturn(List.of(DebugMode.NONE));
		when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
		when(config.getBrowserType()).thenReturn(BrowserType.FIREFOX);
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedOsUtility.close();
		System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
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
	public void testCreateDefaultCapabilitiesWithVersion() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getBrowserVersion()).thenReturn("60.0");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getBrowserVersion(), "60.0");
		
	}

	private Map<String, Object> getPreferences(FirefoxOptions options) {
		return (Map<String, Object>) ((Map<String, Object>) (options.asMap().get("moz:firefoxOptions"))).get("prefs");
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultFirefoxCapabilities() throws SecurityException, IllegalArgumentException {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
		when(config.isSetAssumeUntrustedCertificateIssuer()).thenReturn(true);
		
		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "firefox");

		Map<String, Object> prefs = getPreferences(capa);

		Assert.assertEquals(prefs.get("capability.policy.default.Window.QueryInterface"), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(prefs.get("capability.policy.default.Window.frameElement.get"), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(prefs.get("capability.policy.default.HTMLDocument.compatMode.get"), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(prefs.get("capability.policy.default.Document.compatMode.get"), FirefoxCapabilitiesFactory.ALL_ACCESS);
		Assert.assertEquals(prefs.get("dom.max_chrome_script_run_time"), 0);
		Assert.assertEquals(prefs.get("dom.max_script_run_time"), 0);
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideUserAgent() {
		
		when(config.getUserAgentOverride()).thenReturn("FIREFOX 55");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		Map<String, Object> prefs = getPreferences(capa);

		// check profile
		Assert.assertEquals(prefs.get("general.useragent.override"), "FIREFOX 55");
	}
	

	@Test(groups = {"ut"})
	public void testCreateFirefoxCapabilitiesOverrideUserAgentWithVariables() {
		
		when(config.getUserAgentOverride()).thenReturn("FIREFOX 55 and variable ${browser}");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		SeleniumTestsContext stc = new SeleniumTestsContext();
		stc.setBrowser("firefox");
		when(config.getTestContext()).thenReturn(stc);

		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		Map<String, Object> prefs = getPreferences(capa);
		
		// check profile
		Assert.assertEquals(prefs.get("general.useragent.override"), "FIREFOX 55 and variable FIREFOX");
	}
	
	@Test(groups = {"ut"})
	public void testCreateFirefoxCapabilitiesOverrideUserAgentWithWrongVariables() {
		
		when(config.getUserAgentOverride()).thenReturn("FIREFOX 55 and variable ${bowser}");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		SeleniumTestsContext stc = new SeleniumTestsContext();
		stc.setBrowser("firefox");
		when(config.getTestContext()).thenReturn(stc);

		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		Map<String, Object> prefs = getPreferences(capa);
		
		// check profile
		Assert.assertEquals(prefs.get("general.useragent.override"), "FIREFOX 55 and variable ${bowser}");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideBinPath() {
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.getFirefoxBinPath()).thenReturn("/opt/firefox/bin/firefox");
		
		// SeleniumTestsContext class adds a browserInfo when binary path is set
		Map<BrowserType, List<BrowserInfo>> updatedBrowserInfos = new EnumMap<>(BrowserType.class);
		updatedBrowserInfos.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, "57.0", "", false), 
																	new BrowserInfo(BrowserType.FIREFOX, "58.0", "/opt/firefox/bin/firefox", false)));

		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)((capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("binary") , "/opt/firefox/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesStandardBinPath() {
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();

		Assert.assertEquals(((Map<?,?>)((capa).asMap().get(FirefoxOptions.FIREFOX_OPTIONS))).get("binary") , "/usr/bin/firefox");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideNtlmAuth() {
		
		when(config.getNtlmAuthTrustedUris()).thenReturn("uri://uri.ntlm");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);

		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		Map<String, Object> prefs = getPreferences(capa);
		
		// check profile
		Assert.assertEquals(prefs.get("network.automatic-ntlm-auth.trusted-uris"), "uri://uri.ntlm");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideDownloadDir() {
		
		when(config.getDownloadOutputDirectory()).thenReturn("/home/download");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);

		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		Map<String, Object> prefs = getPreferences(capa);
		
		// check profile
		Assert.assertEquals(prefs.get("browser.download.dir"), "/home/download");
		Assert.assertEquals(prefs.get("browser.download.folderList"), 2);
        Assert.assertFalse((Boolean) prefs.get("browser.download.manager.showWhenStarting"));
		Assert.assertEquals(prefs.get("browser.helperApps.neverAsk.saveToDisk"), "application/octet-stream,text/plain,application/pdf,application/zip,text/csv,text/html");
	}
	
	/**
	 * issue #365: Check DownloadDir is not set in remote
	 */
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesNoOverrideDownloadDirRemote() {
		
		when(config.getDownloadOutputDirectory()).thenReturn("/home/download");
		when(config.getMode()).thenReturn(DriverMode.GRID);

		FirefoxOptions capa = (FirefoxOptions) new FirefoxCapabilitiesFactory(config).createCapabilities();
		Map<String, Object> prefs = getPreferences(capa);
		
		// check profile
		Assert.assertNull(prefs.get("browser.download.dir"));
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesStandardDriverPathLocal() {
		try {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			
			new FirefoxCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertTrue(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY).replace(File.separator, "/").contains("/drivers/geckodriver"));
		} finally {
			System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		}
	}



	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesDownloadDriverPathLocal() {
		try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			when(config.getDownloadDrivers()).thenReturn(true);

			new FirefoxCapabilitiesFactory(config).createCapabilities();

			Assert.assertNull(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY));
			ArgumentCaptor<Path> pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
			mockedFiles.verify(() -> Files.createDirectories(pathArgumentCaptor.capture()));
			Assert.assertTrue(pathArgumentCaptor.getValue().toString().replace("\\", "/").contains(".cache/selenium"));
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesOverrideDriverPathLocal() {
		try {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			when(config.getGeckoDriverPath()).thenReturn("/opt/firefox/driver/geckodriver");
			
			new FirefoxCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY).replace(File.separator, "/"), "/opt/firefox/driver/geckodriver");
		} finally {
			System.clearProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesStandardDriverPathGrid() {
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertNull(System.getProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY));
	}

	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithDefaultProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getFirefoxProfilePath()).thenReturn(BrowserInfo.DEFAULT_BROWSER_PRODFILE);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to 'default'
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.FIREFOX_PROFILE), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithUserProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getFirefoxProfilePath()).thenReturn("/home/user/profile");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is set to user profile
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.FIREFOX_PROFILE), "/home/user/profile");
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWithoutDefaultProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is no set when not requested
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.FIREFOX_PROFILE));
	}
	
	@Test(groups={"ut"})
	public void testCreateFirefoxCapabilitiesWrongProfile() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getFirefoxProfilePath()).thenReturn("foo");
		
		MutableCapabilities capa = new FirefoxCapabilitiesFactory(config).createCapabilities();
		
		// check 'firefoxProfile' is not set if name is not valid
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.FIREFOX_PROFILE));
	}
	
}
