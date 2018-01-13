package com.seleniumtests.ut.browserfactory;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.HtmlUnitCapabilitiesFactory;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.osutility.OSUtility;

@PrepareForTest({OSUtility.class})
public class TestHtmlUnitCapabilityFactory extends MockitoTest {

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory().createCapabilities(config);
		
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
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory().createCapabilities(config);
		
		Assert.assertEquals(capa.getPlatform(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithJavascriptDisabled() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(false);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory().createCapabilities(config);
		
		Assert.assertFalse(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		
	}

	@Test(groups={"ut"})
	public void testCreateDefaultHtmlUnitCapabilities() {
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory().createCapabilities(config);
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "htmlunit");
	}
}
