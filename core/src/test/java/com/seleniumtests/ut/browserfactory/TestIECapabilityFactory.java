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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.ChromeCapabilitiesFactory;
import com.seleniumtests.browserfactory.IECapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.OSUtilityWindows;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg.HKEY;

// TODO: enable test on linux platform using mocks
@PrepareForTest({OSUtility.class, OSUtilityFactory.class, Advapi32Util.class})
public class TestIECapabilityFactory extends MockitoTest {

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private SeleniumTestsContext context;
	
	@Mock
	private OSUtilityWindows osUtility;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {
		PowerMockito.mockStatic(System.class);
		PowerMockito.when(System.getProperty(anyString())).thenCallRealMethod();
		PowerMockito.when(System.setProperty(anyString(), anyString())).thenCallRealMethod();
		PowerMockito.when(System.clearProperty(anyString())).thenCallRealMethod();
		PowerMockito.when(System.getProperty("os.name")).thenReturn("Windows 10");	
		
		Map<BrowserType, List<BrowserInfo>> browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.INTERNET_EXPLORER, Arrays.asList(new BrowserInfo(BrowserType.INTERNET_EXPLORER, "11", "", false)));

		PowerMockito.mockStatic(OSUtility.class);
		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion()).thenReturn(browserInfos);
		PowerMockito.when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		PowerMockito.mockStatic(OSUtilityFactory.class);
		PowerMockito.when(OSUtilityFactory.getInstance()).thenReturn(osUtility);
		
		when(osUtility.getProgramExtension()).thenReturn(".exe");
		
		when(config.getTestContext()).thenReturn(context);
		Mockito.when(config.isDevMode()).thenReturn(false);
		
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertTrue(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		Assert.assertTrue(capa.is(CapabilityType.TAKES_SCREENSHOT));
		Assert.assertTrue(capa.is(CapabilityType.ACCEPT_SSL_CERTS));
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.TOOLS));
		Assert.assertEquals(capa.getVersion(), "");
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
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}

	/**
	 * Check default behaviour when external programs are defined in grid mode
	 * tools are transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithToolsInGridMode() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getExternalPrograms()).thenReturn(Arrays.asList("foo"));
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();

		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.TOOLS), Arrays.asList("foo"));
	}

	/**
	 * Check default behaviour when external programs are defined in local mode
	 * tags are not transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithToolsInLocalMode() {
		
		Mockito.when(config.getExternalPrograms()).thenReturn(Arrays.asList("foo"));
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();

		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.TOOLS));
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatform(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithJavascriptDisabled() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(false);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getBrowserVersion()).thenReturn("10.0");
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getVersion(), "10.0");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultIECapabilities() {
		
		MutableCapabilities capa = new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "internet explorer");
		Assert.assertTrue((Boolean)capa.getCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING));
		Assert.assertTrue((Boolean)capa.getCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS));
		Assert.assertTrue((Boolean)capa.getCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION));
		Assert.assertEquals((String)capa.getCapability(InternetExplorerDriver.INITIAL_BROWSER_URL), "about:blank");
	}
	
	@Test(groups={"ut"})
	public void testCreateIECapabilitiesStandardDriverPathLocal() {
		try {
			PowerMockito.mockStatic(Advapi32Util.class);
			PowerMockito.when(Advapi32Util.registryGetValue(any(HKEY.class), anyString(), anyString())).thenReturn("1");
			Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
			
			new IECapabilitiesFactory(config).createCapabilities();
			
			Assert.assertTrue(System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY).replace(File.separator, "/").contains("/drivers/IEDriverServer_"));
		} finally {
			System.clearProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateIECapabilitiesOverrideDriverPathLocal() {
		try {
			PowerMockito.mockStatic(Advapi32Util.class);
			PowerMockito.when(Advapi32Util.registryGetValue(any(HKEY.class), anyString(), anyString())).thenReturn("1");
			Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
			Mockito.when(config.getIeDriverPath()).thenReturn("/opt/ie/driver/ie");
			
			new IECapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY).replace(File.separator, "/"), "/opt/ie/driver/ie");
		} finally {
			System.clearProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateIECapabilitiesStandardDriverPathGrid() {
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		new IECapabilitiesFactory(config).createCapabilities();
		
		Assert.assertNull(System.getProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY));
	}
	
}
