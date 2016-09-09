/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.FirefoxCapabilitiesFactory;
import com.seleniumtests.core.proxy.ProxyConfig;
import com.seleniumtests.driver.DriverConfig;

public class TestFirefoxCapabilitiesFactory extends MockitoTest {

	@Mock
	private DriverConfig config;
	
	@Mock
	private ProxyConfig proxyConfig;
	
	/**
	 * Check default behaviour
	 */
	@Test(groups={"ut"})
	public void testCreateDefaultCapabilities() {
		
		Mockito.when(config.isUseFirefoxDefaultProfile()).thenReturn(false);
		Mockito.when(config.isEnableJavascript()).thenReturn(true);
		Mockito.when(config.getProxyConfig()).thenReturn(proxyConfig);
		
		DesiredCapabilities capa = new FirefoxCapabilitiesFactory().createCapabilities(config);
		
		Assert.assertTrue(capa.isJavascriptEnabled());
		Assert.assertEquals(capa.getVersion(), "");
	}
	
	@Test(groups={"ut"})
	public void testCreateCapabilitiesWithProfileInfo() {
		
		Mockito.when(config.isUseFirefoxDefaultProfile()).thenReturn(false);
		Mockito.when(config.isEnableJavascript()).thenReturn(false);
		Mockito.when(config.getProxyConfig()).thenReturn(proxyConfig);
		Mockito.when(config.getBrowserDownloadDir()).thenReturn("/home");
		Mockito.when(config.getNtlmAuthTrustedUris()).thenReturn("http://home.com");
		Mockito.when(config.getUserAgentOverride()).thenReturn("");
		Mockito.when(config.getFirefoxBinPath()).thenReturn("/home/bin");
		
		DesiredCapabilities capa = new FirefoxCapabilitiesFactory().createCapabilities(config);
		
		Assert.assertFalse(capa.isJavascriptEnabled());
		Assert.assertEquals(((FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE)).getIntegerPreference("browser.download.folderList", 0), 2);
		Assert.assertEquals(System.getProperty("webdriver.firefox.bin"), "/home/bin");
		Assert.assertEquals(((FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE)).getStringPreference("general.useragent.override", "_"), "");
		Assert.assertEquals(((FirefoxProfile)capa.getCapability(FirefoxDriver.PROFILE)).getStringPreference("network.automatic-ntlm-auth.trusted-uris", ""), "http://home.com");
	}
	
}
