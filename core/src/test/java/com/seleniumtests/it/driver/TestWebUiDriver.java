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
package com.seleniumtests.it.driver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.AppiumDriverFactory;
import com.seleniumtests.browserfactory.AppiumLauncherFactory;
import com.seleniumtests.browserfactory.mobile.AdbWrapper;
import com.seleniumtests.browserfactory.mobile.LocalAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.browserfactory.mobile.MobileDeviceSelector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;

import io.appium.java_client.android.AndroidDriver;

@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({AdbWrapper.class, AndroidDriver.class, MobileDeviceSelector.class, AppiumDriverFactory.class, AppiumLauncherFactory.class})
public class TestWebUiDriver extends MockitoTest {
	

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
		deviceList.add(new MobileDevice("IPhone 6", "0000", "ios", "10.2"));
		deviceList.add(new MobileDevice("Nexus 5", "1234", "android", "5.0"));
		deviceList.add(new MobileDevice("Nexus 7", "1235", "android", "6.0"));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		whenNew(AndroidDriver.class).withAnyArguments().thenReturn(androidDriver);
		when(androidDriver.manage()).thenReturn(driverOptions);
		when(driverOptions.timeouts()).thenReturn(timeouts);
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setPlatform("android");
		SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("5.0");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		
		PowerMockito.mockStatic(AppiumLauncherFactory.class);
		WebDriver driver;
		LocalAppiumLauncher appiumLauncher;
		
		try {
			appiumLauncher = spy(new LocalAppiumLauncher());
			when(AppiumLauncherFactory.getInstance()).thenReturn(appiumLauncher);	
		
			driver = WebUIDriver.getWebDriver();
		} catch (ConfigurationException e) {
			throw new SkipException("Test skipped, appium not correctly configured", e);
		}
		
		PowerMockito.verifyNew(AndroidDriver.class).withArguments(any(URL.class), any(DesiredCapabilities.class));
				
		WebUIDriver.cleanUp();
		verify(appiumLauncher).stopAppium();
	}
	
	@AfterMethod(alwaysRun=true)
	public void closeBrowser() {
		try {
			WebUIDriver.cleanUp();
		} catch (WebDriverException e) {
			
		}
	}
}
