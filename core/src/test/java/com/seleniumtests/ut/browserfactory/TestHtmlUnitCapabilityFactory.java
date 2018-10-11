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

import java.util.ArrayList;
import java.util.Arrays;

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
import com.seleniumtests.browserfactory.ChromeCapabilitiesFactory;
import com.seleniumtests.browserfactory.HtmlUnitCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
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
		Mockito.when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();

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
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();

		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.TOOLS));
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		Mockito.when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatform(), Platform.WINDOWS);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithJavascriptDisabled() {
		
		Mockito.when(config.isEnableJavascript()).thenReturn(false);
		Mockito.when(config.getProxy()).thenReturn(proxyConfig);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(CapabilityType.SUPPORTS_JAVASCRIPT));
		
	}

	@Test(groups={"ut"})
	public void testCreateDefaultHtmlUnitCapabilities() {
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(CapabilityType.BROWSER_NAME), "htmlunit");
	}
}
