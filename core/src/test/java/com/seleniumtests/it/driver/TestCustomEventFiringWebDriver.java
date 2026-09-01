package com.seleniumtests.it.driver;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.seleniumtests.it.reporter.ReporterTest;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import org.testng.xml.XmlSuite;

public class TestCustomEventFiringWebDriver extends GenericDriverTest {
	
	protected List<BrowserType> installedBrowsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
	
	@BeforeMethod(groups="it", alwaysRun = true)
	public void init() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit"); // be sure test is not "non GUI"
	}
	
	@Test(groups={"it"})
	public void testHtmlUnitDriverClosed() {
		if (!installedBrowsers.contains(BrowserType.HTMLUNIT)) {
			throw new SkipException("browser not found");
		}
		
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		try {
			Assert.assertFalse(driver.isBrowserOrAppClosed());
			driver.close();
			Assert.assertTrue(driver.isBrowserOrAppClosed());
		} finally {
			driver.quit();
		}
	}
	
	@Test(groups={"it"})
	public void testChromeDriverClosed() {
		if (!installedBrowsers.contains(BrowserType.CHROME)) {
			throw new SkipException("browser not found");
		}
		
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", null);
		try {
			Assert.assertFalse(driver.isBrowserOrAppClosed());
			driver.close();
			Assert.assertTrue(driver.isBrowserOrAppClosed());
		} finally {
			driver.quit();
		}
	}
	
	@Test(groups={"it"})
	public void testFirefoxDriverClosed() {
		if (!installedBrowsers.contains(BrowserType.FIREFOX)) {
			throw new SkipException("browser not found");
		}
		
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true, BrowserType.FIREFOX, "main", null);
		try {
			Assert.assertFalse(driver.isBrowserOrAppClosed());
			driver.close();
			Assert.assertTrue(driver.isBrowserOrAppClosed());
		} finally {
			driver.quit();
		}
	}
	
	@Test(groups={"it"})
	public void testIEDriverClosed() {
		if (!installedBrowsers.contains(BrowserType.INTERNET_EXPLORER)) {
			throw new SkipException("browser not found");
		}
		
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "main", null);
		try {
			Assert.assertFalse(driver.isBrowserOrAppClosed());
			driver.close();
			Assert.assertTrue(driver.isBrowserOrAppClosed());
		} finally {
			driver.quit();
		}
	}
	
	@Test(groups={"it"})
	public void testEdgeDriverClosed() {
		if (!installedBrowsers.contains(BrowserType.EDGE)) {
			throw new SkipException("browser not found");
		}
		
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true, BrowserType.EDGE, "main", null);
		try {
			Assert.assertFalse(driver.isBrowserOrAppClosed());
			driver.close();
			Assert.assertTrue(driver.isBrowserOrAppClosed());
		} finally {
			driver.quit();
		}
	}

	/**
	 * When driver.quit() is called during a test, all subsequent calls to driver, made internally may fail and write tons of error logs
	 * Check it's not the case
	 */
	@Test(groups={"it"})
	public void testDriveQuitDuringTest() throws IOException {
		ReporterTest.executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, XmlSuite.ParallelMode.METHODS, new String[]{"testQuitDriverDuringTest"});

		String logs = ReporterTest.readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("Test is OK"));
		Assert.assertFalse(logs.contains("browser has crashed")); // check setDriverExited does not say driver has crashed whereas quit() was wanted
		Assert.assertFalse(logs.contains("DriverExceptionListener"));

		JSONObject result = ReporterTest.readTestMethodDetailedFile("testQuitDriverDuringTest");
		List<Object> steps = result.getJSONArray("steps").toList();
		Assert.assertEquals(steps.size(), 11);
		Assert.assertEquals(((Map<String, Object>)steps.get(7)).get("name"), "_doSomethingElse");

	}
}
