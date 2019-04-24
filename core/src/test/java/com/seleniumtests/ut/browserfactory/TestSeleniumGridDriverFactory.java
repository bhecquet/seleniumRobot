package com.seleniumtests.ut.browserfactory;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.awt.AWTException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.DebugMode;

@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({SeleniumGridDriverFactory.class})
public class TestSeleniumGridDriverFactory extends MockitoTest {

	@Mock
	private DriverConfig config;

	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private SeleniumGridConnector gridConnector1;
	
	@Mock
	private SeleniumGridConnector gridConnector2;

	@Mock
	private SeleniumTestsContext context;
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private RemoteWebDriver driver2;
	
	@Mock
	private Options options;
	
	@Mock
	private Timeouts timeouts;
	

	@BeforeMethod(groups= {"ut"})
	public void init() throws Exception {
		when(config.getTestContext()).thenReturn(context);
		Mockito.when(config.getDebug()).thenReturn(Arrays.asList(DebugMode.NONE));

		when(config.getBrowserType()).thenReturn(BrowserType.HTMLUNIT);
		when(config.getPlatform()).thenReturn("windows");
		
		// configure driver
		when(driver.manage()).thenReturn(options);
		when(driver2.manage()).thenReturn(options);
		when(options.timeouts()).thenReturn(timeouts);
		
		when(gridConnector1.getHubUrl()).thenReturn(new URL("http://localhost:1111/wd/hub"));
		when(gridConnector2.getHubUrl()).thenReturn(new URL("http://localhost:2222/wd/hub"));
	}
	
	/**
	 * Check we create a driver from grid
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDriverCreation() throws Exception {
		
		SeleniumTestsContextManager.setThreadContext(context);
		when(context.getTestType()).thenReturn(TestType.WEB);
		
		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(Arrays.asList(gridConnector1));
		
		// connect to grid
		PowerMockito.whenNew(RemoteWebDriver.class).withAnyArguments().thenReturn(driver);
		WebDriver newDriver = new SeleniumGridDriverFactory(config).createWebDriver();
		Assert.assertNotNull(newDriver);
		Assert.assertEquals(newDriver, driver);
	}
	
	/**
	 * If grid is not active, driver is not created and exception is raised
	 * @throws Exception
	 */
	@Test(groups={"ut"}, expectedExceptions=SeleniumGridException.class)
	public void testDriverNotCreatedIfGridNotActive() throws Exception {
		
		try {
			SeleniumGridDriverFactory.setRetryTimeout(1);
			
			SeleniumTestsContextManager.setThreadContext(context);
			when(context.getTestType()).thenReturn(TestType.WEB);
			
			when(gridConnector1.isGridActive()).thenReturn(false);
			when(context.getSeleniumGridConnectors()).thenReturn(Arrays.asList(gridConnector1));
			
			// connect to grid
			PowerMockito.whenNew(RemoteWebDriver.class).withAnyArguments().thenReturn(driver);
			new SeleniumGridDriverFactory(config).createWebDriver();
			
		} finally {
			SeleniumGridDriverFactory.setRetryTimeout(SeleniumGridDriverFactory.DEFAULT_RETRY_TIMEOUT);
		}
		
	}
	
	/**
	 * If exception is raised during driver creation, exception is raised
	 * @throws Exception
	 */
	@Test(groups={"ut"}, expectedExceptions=SeleniumGridException.class)
	public void testDriverNotCreatedIfError() throws Exception {
		
		try {
			SeleniumGridDriverFactory.setRetryTimeout(1);
			
			SeleniumTestsContextManager.setThreadContext(context);
			when(context.getTestType()).thenReturn(TestType.WEB);
			
			when(gridConnector1.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(Arrays.asList(gridConnector1));
			
			// connect to grid;
			PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:1111/wd/hub")), any(DesiredCapabilities.class)).thenThrow(new WebDriverException(""));
			new SeleniumGridDriverFactory(config).createWebDriver();
			
		} finally {
			SeleniumGridDriverFactory.setRetryTimeout(SeleniumGridDriverFactory.DEFAULT_RETRY_TIMEOUT);
		}	
	}
	
	/**
	 * Check we use the first grid connector if it's available
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithSeveralGridConnectors() throws Exception {
		
		SeleniumTestsContextManager.setThreadContext(context);
		when(context.getTestType()).thenReturn(TestType.WEB);
		
		when(gridConnector1.isGridActive()).thenReturn(true);
		when(gridConnector2.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(Arrays.asList(gridConnector1, gridConnector2));
		
		// connect to grid. One driver for each connector so that it's easy to distinguish them
		PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:1111/wd/hub")), any(DesiredCapabilities.class)).thenReturn(driver);
		PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:2222/wd/hub")), any(DesiredCapabilities.class)).thenReturn(driver2);
		WebDriver newDriver = new SeleniumGridDriverFactory(config).createWebDriver();
		Assert.assertNotNull(newDriver);
	}
	
	/**
	 * Check we use the first grid connector if it's available
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithSeveralGridConnectorsOneUnavailable() throws Exception {
		
		SeleniumTestsContextManager.setThreadContext(context);
		when(context.getTestType()).thenReturn(TestType.WEB);
		
		when(gridConnector1.isGridActive()).thenReturn(false);
		when(gridConnector2.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(Arrays.asList(gridConnector1, gridConnector2));
		
		// connect to grid. One driver for each connector so that it's easy to distinguish them
		PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:1111/wd/hub")), any(DesiredCapabilities.class)).thenReturn(driver);
		PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:2222/wd/hub")), any(DesiredCapabilities.class)).thenReturn(driver2);
		WebDriver newDriver = new SeleniumGridDriverFactory(config).createWebDriver();
		Assert.assertNotNull(newDriver);
		
		Assert.assertEquals(newDriver, driver2);
	}
	
	/**
	 * Check we use the connector which is not in error
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithSeveralGridConnectorsOneInError() throws Exception {
		
		try {
			SeleniumGridDriverFactory.setRetryTimeout(1);
			
			SeleniumTestsContextManager.setThreadContext(context);
			when(context.getTestType()).thenReturn(TestType.WEB);
			
			when(gridConnector1.isGridActive()).thenReturn(true);
			when(gridConnector2.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(Arrays.asList(gridConnector1, gridConnector2));
			
			// first driver will create an exception
			PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:1111/wd/hub")), any(DesiredCapabilities.class)).thenThrow(new WebDriverException(""));
			PowerMockito.whenNew(RemoteWebDriver.class).withParameterTypes(URL.class, Capabilities.class).withArguments(eq(new URL("http://localhost:2222/wd/hub")), any(DesiredCapabilities.class)).thenReturn(driver2);
			WebDriver newDriver = new SeleniumGridDriverFactory(config).createWebDriver();
			Assert.assertNotNull(newDriver);
			
			Assert.assertEquals(newDriver, driver2);
			
		} finally {
			SeleniumGridDriverFactory.setRetryTimeout(SeleniumGridDriverFactory.DEFAULT_RETRY_TIMEOUT);
		}	
	}
}
















