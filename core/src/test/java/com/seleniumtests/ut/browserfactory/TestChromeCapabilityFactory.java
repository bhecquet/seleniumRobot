package com.seleniumtests.ut.browserfactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.ChromeCapabilitiesFactory;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.osutility.OSUtility;

@PrepareForTest({OSUtility.class, BrowserInfo.class})
public class TestChromeCapabilityFactory extends MockitoTest {
	
	Map<BrowserType, List<BrowserInfo>> browserInfos;

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {
		browserInfos = new HashMap<>();
		browserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, "63.0", "", false)));
		PowerMockito.mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion()).thenReturn(browserInfos);
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatform(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithJavascriptDisabled() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(false);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithVersion() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getBrowserVersion()).thenReturn("60.0");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getVersion(), "60.0");
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultChromeCapabilities() {
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString(), "[--disable-translate]");
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "chrome");
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesOverrideUserAgent() {
		
		Mockito.when(config.getUserAgentOverride()).thenReturn("CHROME 55");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString(), "[--user-agent=CHROME 55, --disable-translate]");
	}
	
	/**
	 * 
	 */
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesOverrideBinPath() {
		
		Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
		Mockito.when(config.getChromeBinPath()).thenReturn("/opt/chrome/bin/chrome");

		// SeleniumTestsContext class adds a browserInfo when binary path is set
		Map<BrowserType, List<BrowserInfo>> updatedBrowserInfos = new HashMap<>();
		updatedBrowserInfos.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, "63.0", "", false), 
																	new BrowserInfo(BrowserType.CHROME, "64.0", "/opt/chrome/bin/chrome", false)));

		PowerMockito.when(OSUtility.getInstalledBrowsersWithVersion()).thenReturn(updatedBrowserInfos);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("binary").toString(), "/opt/chrome/bin/chrome");
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesStandardDriverPathLocal() {
		try {
			Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
			
			new ChromeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertTrue(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY).replace(File.separator, "/").contains("/drivers/chromedriver_"));
		} finally {
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesOverrideDriverPathLocal() {
		try {
			Mockito.when(config.getMode()).thenReturn(DriverMode.LOCAL);
			Mockito.when(config.getChromeDriverPath()).thenReturn("/opt/chrome/driver/chromedriver");
			
			new ChromeCapabilitiesFactory(config).createCapabilities();
			
			Assert.assertEquals(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY).replace(File.separator, "/"), "/opt/chrome/driver/chromedriver");
		} finally {
			System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
		}
	}
	
	@Test(groups={"ut"})
	public void testCreateChromeCapabilitiesStandardDriverPathGrid() {
		Mockito.when(config.getMode()).thenReturn(DriverMode.GRID);
		
		new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertNull(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
	}
	
	/**
	 * Check mobile capabilities does not share the desktop capabilities
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultMobileCapabilities() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createMobileCapabilities(config);
		
		Assert.assertNull(capa.getCapability(CapabilityType.SUPPORTS_JAVASCRIPT));
		Assert.assertNull(capa.getCapability(CapabilityType.TAKES_SCREENSHOT));
		Assert.assertNull(capa.getCapability(CapabilityType.ACCEPT_SSL_CERTS));
		Assert.assertEquals(((Map<?,?>)capa.getCapability(ChromeOptions.CAPABILITY)).get("args").toString(), "[--disable-translate]");
	}
	
	@Test(groups={"ut"})
	public void testCreateMobileCapabilitiesOverrideUserAgent() {
		Mockito.when(config.getUserAgentOverride()).thenReturn("CHROME 55");
		
		MutableCapabilities capa = new ChromeCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(((Map<?,?>)(((ChromeOptions)capa).asMap().get(ChromeOptions.CAPABILITY))).get("args").toString(), "[--user-agent=CHROME 55, --disable-translate]");
	}
}
