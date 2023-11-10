package com.seleniumtests.ut.connectors.extools;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.HashMap;

import org.mockito.Mock;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.Lighthouse;
import com.seleniumtests.connectors.extools.LighthouseFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;

//@PrepareForTest({WebUIDriver.class})
public class TestLighthouseFactory extends MockitoTest {
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@BeforeMethod(groups="ut", alwaysRun = true)
	public void init() {
//		PowerMockito.mockStatic(WebUIDriver.class);
//		PowerMockito.when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
	}
	
	@Test(groups="ut")
	public void testGetInstanceChrome() {
		
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability("se:cdp", "http://localhost:54321/bla");
		caps.setCapability(CapabilityType.BROWSER_NAME, "chrome");
		caps.setCapability(ChromeOptions.CAPABILITY, new HashMap<Object, Object>() {{put("debuggerAddress", "localhost:12345");}});
		when(driver.getCapabilities()).thenReturn(caps);
		
		Lighthouse lighthouse = LighthouseFactory.getInstance();
		Assert.assertNotNull(lighthouse);
		Assert.assertEquals(lighthouse.getPort(), 12345); // port taken from debuggerAddress
		Assert.assertEquals(lighthouse.getOutputPath(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "lighthouseOut").toString()); // store locally
	}
	
	@Test(groups="ut")
	public void testGetInstanceEdge() {
		
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability("se:cdp", "http://localhost:54321/bla");
		caps.setCapability(CapabilityType.BROWSER_NAME, "MicrosoftEdge");
		caps.setCapability(EdgeOptions.CAPABILITY, new HashMap<Object, Object>() {{put("debuggerAddress", "localhost:12345");}});
		when(driver.getCapabilities()).thenReturn(caps);
		
		Lighthouse lighthouse = LighthouseFactory.getInstance();
		Assert.assertNotNull(lighthouse);
		Assert.assertEquals(lighthouse.getPort(), 12345); // port taken from debuggerAddress
		Assert.assertEquals(lighthouse.getOutputPath(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "lighthouseOut").toString()); // store locally
	}
	
	/**
	 * We detect that a browser is supported, using the "se:cdp" capability
	 */
	@Test(groups="ut", expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Lighthouse can only be used on chromium browsers")
	public void testGetInstanceBrowserNotSupported() {
		
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		caps.setCapability(EdgeOptions.CAPABILITY, new HashMap<Object, Object>() {{put("debuggerAddress", "localhost:12345");}});
		when(driver.getCapabilities()).thenReturn(caps);
		
		Lighthouse lighthouse = LighthouseFactory.getInstance();
		Assert.assertNotNull(lighthouse);
		Assert.assertEquals(lighthouse.getPort(), 12345); // port taken from debuggerAddress
		Assert.assertEquals(lighthouse.getOutputPath(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "lighthouseOut").toString()); // store locally
	}
	
	@Test(groups="ut")
	public void testGetInstanceWithGrid() {
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability("se:cdp", "http://localhost:54321/bla");
		caps.setCapability(CapabilityType.BROWSER_NAME, "chrome");
		caps.setCapability(ChromeOptions.CAPABILITY, new HashMap<Object, Object>() {{put("debuggerAddress", "localhost:12345");}});
		when(driver.getCapabilities()).thenReturn(caps);
		
		Lighthouse lighthouse = LighthouseFactory.getInstance();
		Assert.assertNotNull(lighthouse);
		Assert.assertEquals(lighthouse.getPort(), 12345); // port taken from debuggerAddress
		Assert.assertEquals(lighthouse.getOutputPath(), "upload/lighthouseOut"); // store on grid
	}

}
