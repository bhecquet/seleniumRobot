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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.edge.seleniumtools.EdgeOptions;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.EdgeCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;

@PrepareForTest({OSUtility.class, OSUtilityFactory.class, EdgeCapabilitiesFactory.class})
public class TestEdgeCapabilityFactory extends MockitoTest {

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private OSUtilityWindows osUtility;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {
		
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, "14393", "", false)));

		PowerMockito.mockStatic(OSUtility.class);
		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(browserInfos);
		PowerMockito.when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		PowerMockito.when(OSUtility.isWindows10()).thenReturn(true);
		
		PowerMockito.mockStatic(OSUtilityFactory.class);
		PowerMockito.when(OSUtilityFactory.getInstance()).thenReturn(osUtility);

		when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));
		when(config.getPageLoadStrategy()).thenReturn(PageLoadStrategy.NORMAL);
		
		when(osUtility.getProgramExtension()).thenReturn(".exe");
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		when(config.isEnableJavascript()).thenReturn(true);
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		Assert.assertTrue(capa.is(CapabilityType.TAKES_SCREENSHOT));
		Assert.assertTrue(capa.is(CapabilityType.ACCEPT_SSL_CERTS));
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
		Assert.assertEquals(capa.getVersion(), "");
		Assert.assertEquals(capa.getCapability(CapabilityType.PROXY), proxyConfig);
	}

	/**
	 * Check default behaviour when node tags are defined in grid mode
	 * tags are transferred to driver
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInGridMode() {
		
		when(config.isEnableJavascript()).thenReturn(true);
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
		
		when(config.isEnableJavascript()).thenReturn(true);
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatform(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithJavascriptDisabled() {
		
		when(config.isEnableJavascript()).thenReturn(false);
		when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {
		
		when(config.isEnableJavascript()).thenReturn(true);
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getBrowserVersion()).thenReturn("10240");
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getVersion(), "10240");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultEdgeCapabilities() {
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "MicrosoftEdge");
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

	/**
	 * check the case where we have a beta browser. The last one is choosen
	 */
	@Test(groups={"ut"})
	public void testCreateEdgeCapabilitiesOverrideBinPath() {
		
		when(config.getMode()).thenReturn(DriverMode.LOCAL);

		// SeleniumTestsContext class adds a browserInfo when binary path is set
		Map<BrowserType, List<BrowserInfo>> updatedBrowserInfos = new HashMap<>();
		updatedBrowserInfos.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, "92.0", "", false), 
																	new BrowserInfo(BrowserType.EDGE, "93.0", "/opt/edge/bin/edge", false)));

		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new EdgeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((EdgeOptions)capa).asMap().get(EdgeOptions.CAPABILITY))).get("binary").toString(), "/opt/edge/bin/edge");
	}
	

	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesWithLogging() {

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
