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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.neotys.selenium.proxies.NLWebDriverFactory;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.HtmlUnitCapabilitiesFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

public class TestDesktopCommonCapabilityFactory extends MockitoTest {

	@Mock
	private DriverConfig config;
	
	@Mock
	private Proxy proxyConfig;
	
	@BeforeMethod(groups= {"ut"})
	public void init() {

		when(config.getBrowserType()).thenReturn(BrowserType.HTMLUNIT);
		when(config.isSetAcceptUntrustedCertificates()).thenReturn(true);
	}
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
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
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.NODE_TAGS), Arrays.asList("foo", "bar"));
	}
	
	/**
	 * Check default behaviour when beta browser is defined in grid mode
	 * tags are transferred to driver
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithBetaInGridMode() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getNodeTags()).thenReturn(Arrays.asList("foo", "bar"));
		when(config.getMode()).thenReturn(DriverMode.GRID);
		when(config.getBetaBrowser()).thenReturn(true);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getCapability(SeleniumRobotCapabilityType.BETA_BROWSER), true);
	}
	
	/**
	 * Check default behaviour when node tags are defined in local mode
	 * tags are not transferred to driver 
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithNodeTagsInLocalMode() {
		
		when(config.getNodeTags()).thenReturn(Arrays.asList("foo", "bar"));
		when(config.getMode()).thenReturn(DriverMode.LOCAL);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertFalse(capa.is(SeleniumRobotCapabilityType.NODE_TAGS));
	}
	
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilitiesWithPlatform() {
		
		when(config.getProxy()).thenReturn(proxyConfig);
		when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
		
		Assert.assertEquals(capa.getPlatformName(), Platform.WINDOWS);
		
	}
	
	/**
	 * Check that with neoload in design mode, proxy is set
	 */
	@Test(groups={"ut"})
	public void testCreateHtmlUnitCapabilitiesWithNeoloadDesign() {
		Proxy nlproxy = new Proxy();
		nlproxy.setHttpProxy("localhost:8090");

		DesiredCapabilities nlCaps = new DesiredCapabilities();
		nlCaps.setCapability(CapabilityType.PROXY, nlproxy);

		try (MockedStatic mockedNLDriverFactory = mockStatic(NLWebDriverFactory.class)){

			mockedNLDriverFactory.when(() -> NLWebDriverFactory.addProxyCapabilitiesIfNecessary(any(DesiredCapabilities.class))).thenReturn(nlCaps);
			when(config.isNeoloadActive()).thenReturn(true);
			System.setProperty("nl.selenium.proxy.mode", "Design");

			MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();

			// we check that we have called the neoload method
			Assert.assertNotNull(capa.getCapability(CapabilityType.PROXY));
			Assert.assertEquals(((Proxy)capa.getCapability(CapabilityType.PROXY)).getHttpProxy(), "localhost:8090");



		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	
	/**
	 * Check that with neoload in design mode, proxy is overriden by neoload parameters
	 */
	@Test(groups={"ut"})
	public void testCreateHtmlUnitCapabilitiesWithNeoloadRecording() {

		Proxy proxy = new Proxy();
		proxy.setHttpProxy("localhost:1234");
		Proxy nlproxy = new Proxy();
		nlproxy.setHttpProxy("localhost:8090");

		DesiredCapabilities nlCaps = new DesiredCapabilities();
		nlCaps.setCapability(CapabilityType.PROXY, nlproxy);

		try (MockedStatic mockedNLDriverFactory = mockStatic(NLWebDriverFactory.class)){

			mockedNLDriverFactory.when(() -> NLWebDriverFactory.addProxyCapabilitiesIfNecessary(any(DesiredCapabilities.class))).thenReturn(nlCaps);
			when(config.isNeoloadActive()).thenReturn(true);
			when(config.getProxy()).thenReturn(proxy);
			System.setProperty("nl.selenium.proxy.mode", "Design");

			MutableCapabilities capa = new HtmlUnitCapabilitiesFactory(config).createCapabilities();
			Assert.assertNotNull(capa.getCapability(CapabilityType.PROXY));
			Assert.assertEquals(((Proxy)capa.getCapability(CapabilityType.PROXY)).getHttpProxy(), "localhost:8090");

		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	
	/**
	 * Check that if an error occurs during Neoload connection, we raise a human readable exception
	 * Design API not available: ExceptionInInitializerError
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateHtmlUnitCapabilitiesWithNeoloadNotAccessible() {
		try (MockedStatic mockedNLDriverFactory = mockStatic(NLWebDriverFactory.class)){
			mockedNLDriverFactory.when(() -> NLWebDriverFactory.addProxyCapabilitiesIfNecessary(any(DesiredCapabilities.class))).thenThrow(ExceptionInInitializerError.class);
			when(config.isNeoloadActive()).thenReturn(true);
			System.setProperty("nl.selenium.proxy.mode", "Design");

			new HtmlUnitCapabilitiesFactory(config).createCapabilities();

		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	
	/**
	 * Check that if an error occurs during Neoload connection, we raise a human readable exception
	 * No project started: RuntimeException
	 * No license: RuntimeException
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateHtmlUnitCapabilitiesWithNeoloadNotLicensed() {
		try (MockedStatic mockedNLDriverFactory = mockStatic(NLWebDriverFactory.class)){
			mockedNLDriverFactory.when(() -> NLWebDriverFactory.addProxyCapabilitiesIfNecessary(any(DesiredCapabilities.class))).thenThrow(RuntimeException.class);
			when(config.isNeoloadActive()).thenReturn(true);
			System.setProperty("nl.selenium.proxy.mode", "Design");

			new HtmlUnitCapabilitiesFactory(config).createCapabilities();

		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	
}
