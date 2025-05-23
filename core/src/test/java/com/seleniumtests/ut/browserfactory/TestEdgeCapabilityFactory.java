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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.EdgeCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;

public class TestEdgeCapabilityFactory extends MockitoTest {

	private static final String BETA_VERSION = "131.0";

	private static final String RELEASE_VERSION = "130.0";

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private SeleniumTestsContext context;
	
	private Map<BrowserType, List<BrowserInfo>> browserInfos;


	private MockedStatic mockedOsUtility;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {
		browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, RELEASE_VERSION, "", false)));
		mockedOsUtility = mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		when(config.getTestContext()).thenReturn(context);
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));
		when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
		when(config.getBrowserType()).thenReturn(BrowserType.EDGE);
		when(config.getAttachExistingDriverPort()).thenReturn(null);
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
		when(config.getOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
		when(config.getDownloadOutputDirectory()).thenReturn(SeleniumTestsContextManager.getThreadContext().getDownloadOutputDirectory());
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedOsUtility.close();
		System.clearProperty(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY);
	}


	/**
	 * If beta is not requested, get the non beta version even if both are present
	 */
	@Test(groups= {"ut"})
	public void testNonBetaVersionBrowserChoosen() {
	
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, RELEASE_VERSION, "", false, false), 
				new BrowserInfo(BrowserType.EDGE, BETA_VERSION, "", false, true)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		
		EdgeCapabilitiesFactory capaFactory = new EdgeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
		Assert.assertFalse(capaFactory.getSelectedBrowserInfo().getBeta());
		Assert.assertEquals(capaFactory.getSelectedBrowserInfo().getVersion(), RELEASE_VERSION);
	}
	
	/**
	 * If beta is not requested, and non beta browser not installed, return null
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser EDGE  is not available")
	public void testNonBetaVersionBrowserAbsent() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, BETA_VERSION, "", false, true)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		
		EdgeCapabilitiesFactory capaFactory = new EdgeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
	}
	
	/**
	 * If beta is requested, get the beta version even if both are present
	 */
	@Test(groups= {"ut"})
	public void testBetaVersionBrowserChoosen() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, RELEASE_VERSION, "", false, false), 
				new BrowserInfo(BrowserType.EDGE, BETA_VERSION, "", false, true)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(true)).thenReturn(browserInfos);
		when(config.getBetaBrowser()).thenReturn(true);
		
		EdgeCapabilitiesFactory capaFactory = new EdgeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
		Assert.assertTrue(capaFactory.getSelectedBrowserInfo().getBeta());
		Assert.assertEquals(capaFactory.getSelectedBrowserInfo().getVersion(), BETA_VERSION);
	}

	/**
	 * If beta is not requested, and non beta browser not installed, return null
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser EDGE beta is not available")
	public void testBetaVersionBrowserAbsent() {

		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, RELEASE_VERSION, "", false, false)));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(true)).thenReturn(browserInfos);
		when(config.getBetaBrowser()).thenReturn(true);
		
		EdgeCapabilitiesFactory capaFactory = new EdgeCapabilitiesFactory(config);
		capaFactory.createCapabilities();
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}

	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {

		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatformName(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {

		browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, List.of(new BrowserInfo(BrowserType.CHROME, RELEASE_VERSION, "", false),
				new BrowserInfo(BrowserType.EDGE, BETA_VERSION, "", false)
		));
		mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getBrowserVersion()).thenReturn(BETA_VERSION);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getBrowserVersion(), BETA_VERSION);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultEdgeCapabilities() {
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*]");
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "MicrosoftEdge");
		Map<?,?> prefs = (Map<?,?>)(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("prefs"));
		Assert.assertEquals(prefs.get("profile.exit_type"), "Normal");
		Assert.assertTrue(prefs.get("savefile.default_directory").toString().contains("downloads"));
		Assert.assertEquals(prefs.get("download.prompt_for_download"), false);
		Assert.assertTrue(prefs.get("download.default_directory").toString().contains("downloads"));
		Assert.assertNull(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("debuggerAddress")); // no debuger address set as we do not attach an existing browser
	}
		
	/**
	 * Check we set debugger address
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultEdgeCapabilitiesAttach() {
			
		when(config.getAttachExistingDriverPort()).thenReturn(10);
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*]");
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "MicrosoftEdge");
		Assert.assertNull(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("prefs")); // no preference set when attaching to existing browser
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("debuggerAddress"), "127.0.0.1:10");
		}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesOverrideUserAgent() {
		
		when(config.getUserAgentOverride()).thenReturn("EDGE 55");
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--user-agent=EDGE 55, --disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*]");
	}
	
	@Test(groups = {"ut"})
	public void testCreateEdgeCapabilitiesOverrideUserAgentWithVariables() {
		
		when(config.getUserAgentOverride()).thenReturn("EDGE 55 and variable ${browser}");
		SeleniumTestsContext stc = new SeleniumTestsContext();
		stc.setBrowser("edge");
		when(config.getTestContext()).thenReturn(stc);
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?, ?>) (((EdgeOptions) capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().contains("--user-agent=EDGE 55 and variable EDGE"));
	}
	
	@Test(groups = {"ut"})
	public void testCreateEdgeCapabilitiesOverrideUserAgentWithWrongVariables() {
		
		when(config.getUserAgentOverride()).thenReturn("EDGE 55 and variable ${bowser}");
		SeleniumTestsContext stc = new SeleniumTestsContext();
		stc.setBrowser("edge");
		when(config.getTestContext()).thenReturn(stc);
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(((Map<?, ?>) (((EdgeOptions) capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().contains("--user-agent=EDGE 55 and variable ${bowser}"));
	}

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWithRemoveOptions() {

		when(config.getEdgeOptions()).thenReturn("++no-sandbox");

		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();

		Assert.assertFalse(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().contains("--no-sandbox"));
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesHeadless() {
		
		when(config.isHeadlessBrowser()).thenReturn(true);
			
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();

		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*, --headless, --window-size=1280,1024, --disable-gpu]");
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesUserDefinedProfile() {
		
		when(config.getEdgeProfilePath()).thenReturn("/home/foo/edge");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*, --user-data-dir=/home/foo/edge]");
	}

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesDefaultProfile() {
		
		when(config.getEdgeProfilePath()).thenReturn("default");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);

		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		// a user data dir is configured
		Assert.assertNotEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*, --user-data-dir=/home/foo/edge]");
		Assert.assertTrue(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().startsWith("[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*, --user-data-dir="));
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWrongProfile() {

		when(config.getEdgeProfilePath()).thenReturn("wrongName");
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		// a user data dir is configured
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*]");
	}
	

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesStandardDriverPathLocal() {
		try {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			
			new EdgeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertTrue(System.getProperty(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY).replace(File.separator, "/").contains("/drivers/edgedriver_"));
		} finally {
			System.clearProperty(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY);
		}
	}

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesOverrideDriverPathLocal() {
		try {
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			when(config.getEdgeDriverPath()).thenReturn("/opt/edge/driver/edgedriver");
			
			new EdgeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY).replace(File.separator, "/"), "/opt/edge/driver/edgedriver");
		} finally {
			System.clearProperty(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY);
		}
	}

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesStandardDriverPathGrid() {
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertNull(System.getProperty(EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY));
	}

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWithDefaultProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getEdgeProfilePath()).thenReturn(BrowserInfo.DEFAULT_BROWSER_PRODFILE);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		// check 'edgeProfile' is set to 'default'
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.EDGE_PROFILE), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWithUserProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getEdgeProfilePath()).thenReturn("/home/user/profile");
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		// check option is added with user profile
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.EDGE_PROFILE));
		Assert.assertTrue(((Map<String, List<String>>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").contains("--user-data-dir=/home/user/profile"));
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWithoutDefaultProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		// check 'edgeProfile' is not set as not requested, and no option added
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.EDGE_PROFILE));
		Assert.assertFalse(((Map<String, List<String>>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir=/home/user/profile"));
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWrongProfileGrid() {
		
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getEdgeProfilePath()).thenReturn("foo");
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();

		// check 'edgeProfile' is not set as it's wrong profile path, and no option added
		Assert.assertNull(capa.getCapability(SeleniumRobotCapabilityType.EDGE_PROFILE));
		Assert.assertFalse(((Map<String, List<String>>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString().contains("--user-data-dir=/home/user/profile"));
	}

	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWithOptions() {
		
		when(config.getEdgeOptions()).thenReturn("--key1=value1 --key2=value2");
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate, --disable-web-security, --no-sandbox, --disable-site-isolation-trials, --disable-features=IsolateOrigins,site-per-process, --remote-allow-origins=*, --key1=value1, --key2=value2]");
	}
	
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesWithLogging() {
		
		try {
			when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.DRIVER));
			when(config.getMode()).thenReturn(DriverMode.LOCAL);
			new EdgeCapabilitiesFactory(config).createCapabilities();
		
			Assert.assertEquals(System.getProperty(EdgeDriverService.EDGE_DRIVER_VERBOSE_LOG_PROPERTY), "true");
			Assert.assertTrue(System.getProperty(EdgeDriverService.EDGE_DRIVER_LOG_PROPERTY).endsWith("edgedriver.log"));
		} finally {
			System.clearProperty(EdgeDriverService.EDGE_DRIVER_VERBOSE_LOG_PROPERTY);
			System.clearProperty(EdgeDriverService.EDGE_DRIVER_LOG_PROPERTY);
		}
	}
}
