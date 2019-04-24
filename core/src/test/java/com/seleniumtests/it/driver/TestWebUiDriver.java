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
package com.seleniumtests.it.driver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mock;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.browserfactory.AppiumDriverFactory;
import com.seleniumtests.browserfactory.AppiumLauncherFactory;
import com.seleniumtests.browserfactory.mobile.AdbWrapper;
import com.seleniumtests.browserfactory.mobile.LocalAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.browserfactory.mobile.MobileDeviceSelector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.reporter.ReporterTest;

import io.appium.java_client.android.AndroidDriver;

@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({AdbWrapper.class, AndroidDriver.class, MobileDeviceSelector.class, AppiumDriverFactory.class, AppiumLauncherFactory.class})
public class TestWebUiDriver extends ReporterTest {
	

	@Mock
	private AdbWrapper adbWrapper;
	
	@Mock
	private AndroidDriver<?> androidDriver;
	
	@Mock
	private Options driverOptions;
	
	@Mock
	private Timeouts timeouts;
	

	@Test(groups={"it"})
	public void testLocalAndroidDriver() throws Exception {
		whenNew(AdbWrapper.class).withNoArguments().thenReturn(adbWrapper);
		
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("IPhone 6", "0000", "ios", "10.2", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 5", "1234", "android", "5.0", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 7", "1235", "android", "6.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		whenNew(AndroidDriver.class).withAnyArguments().thenReturn(androidDriver);
		when(androidDriver.manage()).thenReturn(driverOptions);
		when(driverOptions.timeouts()).thenReturn(timeouts);
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("5.0");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		
		PowerMockito.mockStatic(AppiumLauncherFactory.class);
		LocalAppiumLauncher appiumLauncher;
		
		try {
			appiumLauncher = spy(new LocalAppiumLauncher());
			when(AppiumLauncherFactory.getInstance()).thenReturn(appiumLauncher);	
		
			WebUIDriver.getWebDriver(true);
		} catch (ConfigurationException e) {
			throw new SkipException("Test skipped, appium not correctly configured", e);
		}
		
		PowerMockito.verifyNew(AndroidDriver.class).withArguments(any(URL.class), any(DesiredCapabilities.class));
				
		WebUIDriver.cleanUp();
		verify(appiumLauncher).stopAppium();
	}
	
	/**
	 * Check that HAR capture file is present 
	 * Check it contains one page per TestStep
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testHarCaptureExists() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.CAPTURE_NETWORK, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "networkCapture.har").toFile().exists());
			
			JSONObject json = new JSONObject(FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "networkCapture.har").toFile()));
			JSONArray pages = json.getJSONObject("log").getJSONArray("pages");
			
			// 7 steps in HTML 
			// 'getPageUrl' step should be called before driver is created but creating PictureElement starts driver
			Assert.assertTrue(pages.length() >= 6, "content is: " + json.toString());
			List<String> pageNames = new ArrayList<>();
			for (Object page: pages.toList()) {
				pageNames.add(((Map<String, Object>)page).get("id").toString().trim());
			}
			Assert.assertTrue(pageNames.contains("testDriver"));
			Assert.assertTrue(pageNames.contains("_writeSomething"));
			Assert.assertTrue(pageNames.contains("_reset"));
			Assert.assertTrue(pageNames.contains("_sendKeysComposite"));
			Assert.assertTrue(pageNames.contains("_clickPicture"));
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.CAPTURE_NETWORK);
		}
	}
	
	/**
	 * Check that browser logs are written to file (only available for chrome)
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testBrowserLogsExists() throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "driver-log-browser.txt").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "driver-log-client.txt").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "driver-log-driver.txt").toFile().exists());
		
	}
	
	/**
	 * Check that HAR capture file is present in result with manual steps
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsHarCaptureWithManualSteps() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.CAPTURE_NETWORK, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverManualSteps"});
			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverManualSteps", "networkCapture.har").toFile().exists());
			JSONObject json = new JSONObject(FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverManualSteps", "networkCapture.har").toFile()));
			JSONArray pages = json.getJSONObject("log").getJSONArray("pages");
			Assert.assertEquals(pages.length(), 2);
			Assert.assertEquals(pages.getJSONObject(0).getString("id").trim(), "testDriverManualSteps");
			Assert.assertEquals(pages.getJSONObject(1).getString("id").trim(), "Reset");
			
			// step "Write" is not recorded because the driver is not created before the DriverTestPage object is created
		} finally {
			System.clearProperty(SeleniumTestsContext.CAPTURE_NETWORK);
		}
		
	}
	
	@AfterMethod(groups={"it"})
	public void closeBrowser() {
		try {
			WebUIDriver.cleanUp();
		} catch (WebDriverException e) {
			
		}
	}
}
