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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContextManager;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.ChromeCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;

public class TestChromeCapabilityFactory extends MockitoTest {

	public static final String CURRENT_CHROME_VERSION = "132.0";
	public static final String NEXT_CHROME_VERSION = "133.0";
	Map<BrowserType, List<BrowserInfo>> browserInfos;

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private SeleniumTestsContext context;

	private MockedStatic mockedOsUtility;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {
		browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, CURRENT_CHROME_VERSION, "", false)));
		mockedOsUtility = mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		when(config.getTestContext()).thenReturn(context);
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));
		when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
		when(config.getBrowserType()).thenReturn(BrowserType.CHROME);
		when(config.getAttachExistingDriverPort()).thenReturn(null);
		when(config.getOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
		when(config.getDownloadOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getDownloadOutputDirectory());
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedOsUtility.close();
		System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
	}
	

	/**
	 * If beta is not requested, get the non beta version even if both are present
	 */
	@Test(groups= {"ut"})
	public void testNonBetaVersionBrowserChoosen() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, CURRENT_CHROME_VERSION, "", false, false),
				new BrowserInfo(BrowserType.CHROME, NEXT_CHROME_VERSION, "", false, true)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		
		ChromeCapabilitiesFactory capaFactory = new ChromeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
		Assert.assertFalse(capaFactory.getSelectedBrowserInfo().getBeta());
		Assert.assertEquals(capaFactory.getSelectedBrowserInfo().getVersion(), CURRENT_CHROME_VERSION);

	}
	
	/**
	 * If beta is not requested, and non beta browser not installed, return null
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser CHROME  is not available")
	public void testNonBetaVersionBrowserAbsent() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, NEXT_CHROME_VERSION, "", false, true)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);

		ChromeCapabilitiesFactory capaFactory = new ChromeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
	}
	
	/**
	 * If beta is requested, get the beta version even if both are present
	 */
	@Test(groups= {"ut"})
	public void testBetaVersionBrowserChoosen() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, CURRENT_CHROME_VERSION, "", false, false),
				new BrowserInfo(BrowserType.CHROME, NEXT_CHROME_VERSION, "", false, true)));

		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(true)).thenReturn(browserInfos);
		when(config.getBetaBrowser()).thenReturn(true);

		ChromeCapabilitiesFactory capaFactory = new ChromeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
		Assert.assertTrue(capaFactory.getSelectedBrowserInfo().getBeta());
		Assert.assertEquals(capaFactory.getSelectedBrowserInfo().getVersion(), NEXT_CHROME_VERSION);
	}

	/**
	 * If beta is not requested, and non beta browser not installed, return null
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser CHROME beta is not available")
	public void testBetaVersionBrowserAbsent() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, CURRENT_CHROME_VERSION, "", false, false)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(true)).thenReturn(browserInfos);
		when(config.getBetaBrowser()).thenReturn(true);

		ChromeCapabilitiesFactory capaFactory = new ChromeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();

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
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatformName(), Platform.WINDOWS);
		
	}

	/**
	 * When version is specified, check it's choosen
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {

		browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, List.of(new BrowserInfo(BrowserType.CHROME, CURRENT_CHROME_VERSION, "", false),
				new BrowserInfo(BrowserType.CHROME, NEXT_CHROME_VERSION, "", false)
				));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getBrowserVersion()).thenReturn(NEXT_CHROME_VERSION);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getBrowserVersion(), NEXT_CHROME_VERSION);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultChromeCapabilities() {

		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();

		Assert.assertTrue(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*"));
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "chrome");
		Map<?,?> prefs = (Map<?,?>)(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("prefs"));
		Assert.assertEquals(prefs.get("profile.exit_type"), "Normal");
		Assert.assertTrue(prefs.get("savefile.default_directory").toString().contains("downloads"));
		Assert.assertEquals(prefs.get("download.prompt_for_download"), false);
		Assert.assertTrue(prefs.get("download.default_directory").toString().contains("downloads"));
		Assert.assertNull(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("debuggerAddress")); // no debuger address set as we do not attach an existing browser
	}
	
	/**
	 * Check we set debugger address
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultChromeCapabilitiesAttach() {
		
		when(config.getAttachExistingDriverPort()).thenReturn(10);
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*"));
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "chrome");
		Assert.assertNull(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("prefs")); // no preference set when attaching to existing browser
		Assert.assertEquals(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("debuggerAddress"), "127.0.0.1:10");
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesOverrideUserAgent() {
		
		when(config.getUserAgentOverride()).thenReturn("CHROME 55");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-agent=CHROME 55"));
	}
	
	@Test(groups = {"ut"})
	public void testCreateChromeCapabilitiesOverrideUserAgentWithVariables() {
		
		when(config.getUserAgentOverride()).thenReturn("CHROME 55 and variable ${browser}");
		SeleniumTestsContext stc = new SeleniumTestsContext();
		stc.setBrowser("chrome");
		when(config.getTestContext()).thenReturn(stc);
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?, ?>) (((ChromeOptions) capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-agent=CHROME 55 and variable CHROME"));
	}
	
	@Test(groups = {"ut"})
	public void testCreateChromeCapabilitiesOverrideUserAgentWithWrongVariables() {
		
		when(config.getUserAgentOverride()).thenReturn("CHROME 55 and variable ${bowser}");
		SeleniumTestsContext stc = new SeleniumTestsContext();
		stc.setBrowser("chrome");
		when(config.getTestContext()).thenReturn(stc);
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?, ?>) (((ChromeOptions) capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-agent=CHROME 55 and variable ${bowser}"));
	}	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesHeadless() {
		
		when(config.isHeadlessBrowser()).thenReturn(true);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		// check headless is configured
		Assert.assertTrue(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--headless, --window-size=1280,1024, --disable-gpu"));
	}

	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesUserDefinedProfile() {
		
		when(config.getChromeProfilePath()).thenReturn("/home/foo/chrome");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir=/home/foo/chrome"));
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesDefaultProfile() {
		
		when(config.getChromeProfilePath()).thenReturn("default");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		// a user data dir is configured
		Assert.assertFalse(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir=/home/foo/chrome"));
		String chromeArgs = ((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString();
		Assert.assertTrue(chromeArgs.contains("--disable-translate"));
		Assert.assertTrue(chromeArgs.contains("--disable-web-security"));
		Assert.assertTrue(chromeArgs.contains("--no-sandbox"));
		Assert.assertTrue(chromeArgs.contains("--remote-allow-origins=*"));
		Assert.assertTrue(chromeArgs.contains("--disable-site-isolation-trials"));
		Assert.assertTrue(chromeArgs.contains("--disable-search-engine-choice-screen"));
		Assert.assertTrue(chromeArgs.contains("--disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades"));
		Assert.assertTrue(chromeArgs.contains("--user-data-dir="));
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWrongProfile() {
		
		when(config.getChromeProfilePath()).thenReturn("wrongName");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		

		Assert.assertFalse(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir"));
	}
	
	/**
	 * 
	 */
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesOverrideBinPath() {
		
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.getChromeBinPath()).thenReturn("/opt/chrome/bin/chrome");

		// SeleniumTestsContext class adds a browserInfo when binary path is set
		Map<BrowserType, List<BrowserInfo>> updatedBrowserInfos = new HashMap<>();
		updatedBrowserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, CURRENT_CHROME_VERSION, "", false),
																	new BrowserInfo(BrowserType.CHROME, NEXT_CHROME_VERSION, "/opt/chrome/bin/chrome", false)));

		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("binary").toString(), "/opt/chrome/bin/chrome");
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesStandardDriverPathLocal() {
		try {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			
			new ChromeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertTrue(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY).replace(File.separator, "/").contains("/drivers/chromedriver_"));
		} finally {
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesOverrideDriverPathLocal() {
		try {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			when(config.getChromeDriverPath()).thenReturn("/opt/chrome/driver/chromedriver");
			
			new ChromeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY).replace(File.separator, "/"), "/opt/chrome/driver/chromedriver");
		} finally {
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesStandardDriverPathGrid() {
		try {
			when(config.getMode()).thenReturn(DriverMode.GRID);

			new ChromeCapabilitiesFactory(config).createCapabilities();

			Assert.assertNull(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
		} finally {
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
		}
	}
	
	/**
	 * Check mobile capabilities does not share the desktop capabilities
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultMobileCapabilities() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createMobileCapabilities(config);

		Assert.assertNull(capa.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS));
		Assert.assertEquals(((Map<?,?>)capa.getCapability(ChromeOptions.CAPABILITY)).get("args").toString(), "[--disable-translate, --disable-web-security, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*]");
	}

	@Test(groups={"ut"})
	public void testCreateDefaultMobileCapabilitiesRemoveOption() {

		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getChromeOptions()).thenReturn("++disable-web-security");

		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createMobileCapabilities(config);

		Assert.assertNull(capa.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS));
		Assert.assertEquals(((Map<?,?>)capa.getCapability(ChromeOptions.CAPABILITY)).get("args").toString(), "[--disable-translate, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*]");
	}
	
	@Test(groups={"ut"})
	public void testCreateMobileCapabilitiesOverrideUserAgent() {
		when(config.getUserAgentOverride()).thenReturn("CHROME 55");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createMobileCapabilities(config);
		
		Assert.assertEquals(((Map<?,?>)(capa.asMap().get(ChromeOptions.CAPABILITY))).get("args").toString(), "[--user-agent=CHROME 55, --disable-translate, --disable-web-security, --disable-site-isolation-trials, --disable-search-engine-choice-screen, --disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades, --remote-allow-origins=*]");
	}
	

	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithDefaultProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getChromeProfilePath()).thenReturn(BrowserInfo.DEFAULT_BROWSER_PRODFILE);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		// check 'chromeProfile' is set to 'default'
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.CHROME_PROFILE), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithUserProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getChromeProfilePath()).thenReturn("/home/user/profile");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		// check option is added with user profile
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.CHROME_PROFILE));
		Assert.assertTrue(((Map<String, List<String>>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").contains("--user-data-dir=/home/user/profile"));
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithoutDefaultProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		// check 'chromeProfile' is not set as not requested, and no option added
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.CHROME_PROFILE));
		Assert.assertFalse(((Map<String, List<String>>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir=/home/user/profile"));
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWrongProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getChromeProfilePath()).thenReturn("foo");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();

		// check 'chromeProfile' is not set as it's wrong profile path, and no option added
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.CHROME_PROFILE));
		Assert.assertFalse(((Map<String, List<String>>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir=/home/user/profile"));
	}
	

	@Test(groups={"ut"})
	public void testCreateMobileCapabilitiesWithOptions() {
		when(config.getChromeOptions()).thenReturn("--key1=value1 --key2=value2");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createMobileCapabilities(config);
		
		Assert.assertTrue(((Map<?,?>)(capa.asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--key1=value1, --key2=value2"));
	}
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithOptions() {

		when(config.getChromeOptions()).thenReturn("--key1=value1 --key2=value2");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--key1=value1, --key2=value2"));
	}

	/**
	 * Test it's possible to remove a default chrome option at startup
	 */
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithRemoveOptions() {

		when(config.getChromeOptions()).thenReturn("++no-sandbox");

		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();

		Assert.assertFalse(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString().contains("--no-sandbox"));
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithLogging() {

		try {
			when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.DRIVER));
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			new ChromeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY), "true");
			Assert.assertTrue(System.getProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY).endsWith("chromedriver.log"));
		} finally {
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY);
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY);
		}
	}
}
