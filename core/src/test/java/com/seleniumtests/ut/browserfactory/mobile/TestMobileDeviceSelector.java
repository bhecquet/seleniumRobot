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
package com.seleniumtests.ut.browserfactory.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.options.BaseOptions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.browserfactory.mobile.AdbWrapper;
import com.seleniumtests.browserfactory.mobile.InstrumentsWrapper;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.browserfactory.mobile.MobileDeviceSelector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.osutility.OSCommand;

import static org.mockito.Mockito.*;

public class TestMobileDeviceSelector extends MockitoTest {
	
	@Mock
	private AdbWrapper adbWrapper;
	
	@Mock
	private InstrumentsWrapper instrumentsWrapper;
	
	@InjectMocks
	private MobileDeviceSelector deviceSelector;
	
	/**
	 * Test that an error is raised when no matching device is found
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectNonExistingAndroidDevice() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		MutableCapabilities requestedCaps = new BaseOptions();
		((BaseOptions)requestedCaps).setPlatformName("android");

		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	/**
	 * Test that an error is raised when no mobile capability is found (DEVICE_NAME, PLATFORM_NAME, PLATFORM_VERSION)
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectWithoutMobileCapabilities() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		BaseOptions requestedCaps = new BaseOptions();
		requestedCaps.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectAndroidNotReady() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("Nexus 5", "1234", "android", "5.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		MutableCapabilities requestedCaps = new BaseOptions();
		((BaseOptions)requestedCaps).setPlatformName("android");
		
		deviceSelector.setAndroidReady(false);
		deviceSelector.setIosReady(false);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectiOSNotReady() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("IPhone 6", "0000", "iOS", "10.2", new ArrayList<>()));
		when(instrumentsWrapper.parseIosDevices()).thenReturn(deviceList);
		
		// requested caps
		MutableCapabilities requestedCaps = new BaseOptions();
		((BaseOptions)requestedCaps).setPlatformName("iOS");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	/**
	 * Test that when several devices exists with the same capabilities, the first one is selected
	 * Test platform name with different cases
	 */
	@Test(groups={"ut"})
	public void testSelectFirstMatchingAndroidDevice() {
		// available devices
		List<MobileDevice> deviceListAndroid = new ArrayList<>();
		deviceListAndroid.add(new MobileDevice("Nexus 5", "1234", "Android", "5.0", new ArrayList<>()));
		deviceListAndroid.add(new MobileDevice("Nexus 7", "1235", "Android", "6.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceListAndroid);
		
		List<MobileDevice> deviceListIos = new ArrayList<>();
		deviceListIos.add(new MobileDevice("IPhone 6", "0000", "iOS", "10.2", new ArrayList<>()));
		when(instrumentsWrapper.parseIosDevices()).thenReturn(deviceListIos);
		
		// requested caps
		MutableCapabilities requestedCaps = new BaseOptions();
		((BaseOptions)requestedCaps).setPlatformName("android");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		Assert.assertEquals(deviceSelector.getRelevantMobileDevice(requestedCaps), deviceListAndroid.get(0));
	}
	
	/**
	 * Test that when several devices exists with the same capabilities, the first one is selected
	 * Test platform name with different cases
	 */
	@Test(groups={"ut"})
	public void testSelectFirstMatchingiOSDevice() {
		// available devices
		List<MobileDevice> deviceListAndroid = new ArrayList<>();
		deviceListAndroid.add(new MobileDevice("Nexus 5", "1234", "Android", "5.0", new ArrayList<>()));
		deviceListAndroid.add(new MobileDevice("Nexus 7", "1235", "Android", "6.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceListAndroid);
		
		List<MobileDevice> deviceListIos = new ArrayList<>();
		deviceListIos.add(new MobileDevice("IPhone 6", "0000", "iOS", "10.2", new ArrayList<>()));
		deviceListIos.add(new MobileDevice("IPhone 7", "0000", "iOS", "10.3", new ArrayList<>()));
		when(instrumentsWrapper.parseIosDevices()).thenReturn(deviceListIos);
		
		// requested caps
		MutableCapabilities requestedCaps = new BaseOptions<>();
		((BaseOptions)requestedCaps).setPlatformName("iOS");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		Assert.assertEquals(deviceSelector.getRelevantMobileDevice(requestedCaps), deviceListIos.get(0));
	}
	
	/**
	 * Test that only the device that matches all capabilities is selected
	 */
	@Test(groups={"ut"})
	public void testSelectMostMatchingAndroidDevice() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("IPhone 6", "0000", "iOS", "10.2", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 5", "1234", "Android", "5.0", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 7", "1235", "android", "6.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		MutableCapabilities requestedCaps = new BaseOptions();
		((BaseOptions)requestedCaps).setPlatformName("android")
				.setPlatformVersion("6.0");

		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);
		
		Assert.assertEquals(deviceSelector.getRelevantMobileDevice(requestedCaps), deviceList.get(2));
	}
	
	/**
	 * Test the update of capabilities with device properties
	 * input capabilities only know the name, and from that data, we expect to get the platform and version
	 * plus an updated version of device name with the id instead as it's the property expected by ADB to 
	 * communicate with any device
	 */
	@Test(groups={"ut"})
	public void testCapabilitiesUpdate() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("nexus 5", "1234", "android", "5.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		MutableCapabilities requestedCaps = new UiAutomator2Options();
		((UiAutomator2Options)requestedCaps).setDeviceName("Nexus 5");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);

		// pass a MutableCapabilities to be sure castings are correct. In core, it's this type which is transmitted
		UiAutomator2Options updatedCaps = new UiAutomator2Options(deviceSelector.updateCapabilitiesWithSelectedDevice(new MutableCapabilities(requestedCaps), DriverMode.LOCAL));
		Assert.assertEquals(updatedCaps.getPlatformName(), Platform.ANDROID);
		Assert.assertEquals(updatedCaps.getDeviceName().orElse(null), "nexus 5");
		Assert.assertEquals(updatedCaps.getUdid().orElse(null), "1234");
		Assert.assertEquals(updatedCaps.getPlatformVersion().orElse(null), "5.0");
		Assert.assertTrue(updatedCaps.getChromedriverExecutable().isEmpty());
	}
	
	/**
	 * Test the update of driver capabilities when using browsers in local mode
	 */
	@Test(groups={"ut"})
	public void testCapabilitiesUpdateWithDriver() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		BrowserInfo chromeInfo = new BrowserInfo(BrowserType.CHROME, "47.0", null);
		chromeInfo.setDriverFileName("chromedriver.exe");
		deviceList.add(new MobileDevice("nexus 5", "1234", "android", "5.0", Arrays.asList(chromeInfo)));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);

		MutableCapabilities requestedCaps = new UiAutomator2Options();
		((UiAutomator2Options)requestedCaps).setDeviceName("Nexus 5").withBrowserName("chrome");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);

		UiAutomator2Options updatedCaps = new UiAutomator2Options(deviceSelector.updateCapabilitiesWithSelectedDevice(new MutableCapabilities(requestedCaps), DriverMode.LOCAL));
		Assert.assertEquals(updatedCaps.getChromedriverExecutable().orElse(null), "chromedriver.exe");
		
	}

	/**
	 * Check that chrome driver is set even if we test an application, to be able to access webview
	 */
	@Test(groups={"ut"})
	public void testCapabilitiesUpdateWithDriverInAppTest() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		BrowserInfo chromeInfo = new BrowserInfo(BrowserType.CHROME, "47.0", null);
		chromeInfo.setDriverFileName("chromedriver.exe");
		deviceList.add(new MobileDevice("nexus 5", "1234", "android", "5.0", Arrays.asList(chromeInfo)));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);

		MutableCapabilities requestedCaps = new UiAutomator2Options();
		((UiAutomator2Options)requestedCaps).setDeviceName("Nexus 5").setApp("myApp.apk");

		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);

		UiAutomator2Options updatedCaps = new UiAutomator2Options(deviceSelector.updateCapabilitiesWithSelectedDevice(new MutableCapabilities(requestedCaps), DriverMode.LOCAL));
		Assert.assertEquals(updatedCaps.getChromedriverExecutable().orElse(null), "chromedriver.exe");
	}

	/**
	 * Test the case where no driver is available for the embedded chrome
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Chrome version 47 does not have any associated driver, update seleniumRobot version")
	public void testCapabilitiesUpdateWithDriverInAppTestNoDriver() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		BrowserInfo chromeInfo = spy(new BrowserInfo(BrowserType.CHROME, "47.0", null));

		deviceList.add(new MobileDevice("nexus 5", "1234", "android", "5.0", Arrays.asList(chromeInfo)));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		doThrow(new ConfigurationException("Chrome version 47 does not have any associated driver, update seleniumRobot version")).when(chromeInfo).getDriverFileName();

		MutableCapabilities requestedCaps = new UiAutomator2Options();
		((UiAutomator2Options)requestedCaps).setDeviceName("Nexus 5").setApp("myApp.apk");

		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);

		deviceSelector.updateCapabilitiesWithSelectedDevice(new MutableCapabilities(requestedCaps), DriverMode.LOCAL);
	}
	
	/**
	 * Test the driver capabilities are not updated with driver executable when using browsers in non-local mode
	 */
	@Test(groups={"ut"})
	public void testCapabilitiesUpdateWithDriverNonLocal() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		BrowserInfo chromeInfo = new BrowserInfo(BrowserType.BROWSER, "47.0", null);
		chromeInfo.setDriverFileName("chromedriver2.exe");
		deviceList.add(new MobileDevice("nexus 5", "1234", "android", "5.0", Arrays.asList(chromeInfo)));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);

		MutableCapabilities requestedCaps = new UiAutomator2Options();
		((UiAutomator2Options)requestedCaps).setDeviceName("Nexus 5").withBrowserName("browser");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);

		UiAutomator2Options updatedCaps = new UiAutomator2Options(deviceSelector.updateCapabilitiesWithSelectedDevice(new MutableCapabilities(requestedCaps), DriverMode.GRID));
		Assert.assertTrue(updatedCaps.getChromedriverExecutable().isEmpty());
		
	}
}
