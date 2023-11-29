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
package com.seleniumtests.browserfactory.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.options.BaseOptions;
import io.appium.java_client.remote.options.SupportsDeviceNameOption;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;

import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;

public class MobileDeviceSelector {
	
	private AdbWrapper adbWrapper;
	private InstrumentsWrapper instrumentsWrapper;
	private Boolean androidReady;
	private Boolean iosReady;
	
	public MobileDeviceSelector initialize() {
		try {
			adbWrapper = new AdbWrapper();
			androidReady = true;
		} catch (ConfigurationException e) {
			adbWrapper = null;
			androidReady = false;
		}
		
		try {
			instrumentsWrapper = new InstrumentsWrapper();
			iosReady = true;
		} catch (ConfigurationException e) {
			instrumentsWrapper = null;
			iosReady = false;
		}
		return this;
	}
	
	private void isInitialized() {
		if (androidReady == null || iosReady == null) {
			throw new ConfigurationException("You must call MobileDeviceSelector.initialize() before using it");
		}
	}
	
	private List<MobileDevice> filterDevices(List<MobileDevice> deviceList, String deviceName, String platformName, String platformVersion) {
		List<MobileDevice> filteredDeviceList = new ArrayList<>();
		
		for (MobileDevice device: deviceList) {
			boolean keep = true;
			if (deviceName != null && device.getName() != null && !device.getName().equalsIgnoreCase(deviceName)) {
				keep = false;
			}
			if (platformName != null && device.getPlatform() != null && !device.getPlatform().equalsIgnoreCase(platformName)) {
				keep = false;
			}
			if (platformVersion != null && device.getVersion() != null && !device.getVersion().equalsIgnoreCase(platformVersion)) {
				keep = false;
			}
			if (keep) {
				filteredDeviceList.add(device);
			}
		}
		return filteredDeviceList;
	}

	/**
	 * Returns the mobile device corresponding to the device name and/or OS version specified in test properties
	 * @throws ConfigurationException if no relevant device is found
	 * @return
	 */
	public MobileDevice getRelevantMobileDevice(MutableCapabilities capabilities) {
		isInitialized();
		Object deviceName = capabilities.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX +  SupportsDeviceNameOption.DEVICE_NAME_OPTION);
		Object platformName = capabilities.getCapability(CapabilityType.PLATFORM_NAME);
		Optional<String> platformVersion = new BaseOptions<>(capabilities).getPlatformVersion();
		
		if (deviceName == null
				&& platformName == null
				&& (!platformVersion.isPresent() || platformVersion.get() == null)
				) {
			throw new ConfigurationException("at least one mobile capaiblity must be provided: DEVICE_NAME, PLATFORM_NAME, PLATFORM_VERSION");
		}
		
		List<MobileDevice> deviceList = new ArrayList<>();
		
		if (Boolean.TRUE.equals(androidReady)) {
			deviceList.addAll(adbWrapper.getDeviceList());
		}
		
		if (Boolean.TRUE.equals(iosReady)) {
			deviceList.addAll(instrumentsWrapper.parseIosDevices());
		}
		
		if (deviceList.isEmpty()) {
			throw new ConfigurationException("No device found, check at least one is connected");
		}
		
		List<MobileDevice> filteredDeviceList = filterDevices(deviceList, 
																deviceName == null ? null: deviceName.toString(), 
																platformName == null ? null: platformName.toString(), 
																platformVersion == null ? null: platformVersion.toString()
		);
		
		if (filteredDeviceList.isEmpty()) {
			throw new ConfigurationException("no matching device found among: " + deviceList);
		}
		
		// returns the first matching device
		return filteredDeviceList.get(0);
	}
	
	/**
	 * From input capabilities, (e.g: platform, version or device real name), update capabilities 
	 * with deviceName, platform, version, or other useful data
	 * @param capabilities
	 * @return
	 */
	public MutableCapabilities updateCapabilitiesWithSelectedDevice(MutableCapabilities capabilities, DriverMode driverMode) {
		MobileDevice selectedDevice = getRelevantMobileDevice(capabilities);
		
		if ("android".equals(selectedDevice.getPlatform())) {
			((UiAutomator2Options)capabilities).setDeviceName(selectedDevice.getName())
					.setUdid(selectedDevice.getId());
			
			// set the right chromedriver executable according to android browser / chromeversion
			// it's only the file name, not it's path
			if (driverMode == DriverMode.LOCAL && !capabilities.getBrowserName().isEmpty()) {
				String chromeDriverFile = null;
				if (BrowserType.CHROME.toString().equalsIgnoreCase(capabilities.getBrowserName())) {
					chromeDriverFile = selectedDevice.getBrowserInfo(BrowserType.CHROME).getDriverFileName();
	        	} else if (BrowserType.BROWSER.toString().equalsIgnoreCase(capabilities.getBrowserName())) {
	        		chromeDriverFile = selectedDevice.getBrowserInfo(BrowserType.BROWSER).getDriverFileName();
	        	}
				if (chromeDriverFile != null) {
					// driver extraction will be done later. For example in AppiumDriverFactory
					((UiAutomator2Options)capabilities).setChromedriverExecutable(chromeDriverFile);
				}
			}
			
		} else if ("ios".equalsIgnoreCase(selectedDevice.getPlatform())) {
			((XCUITestOptions)capabilities).setUdid(selectedDevice.getId())
							.setDeviceName(selectedDevice.getName());
//			capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + IOSMobileCapabilityType.XCODE_CONFIG_FILE, System.getenv("APPIUM_HOME") + "/node_modules/appium/node_modules/appium-xcuitest-driver/WebDriverAgent/xcodeConfigFile.xcconfig");
		}
		capabilities.setCapability(CapabilityType.PLATFORM_NAME, selectedDevice.getPlatform());
		((BaseOptions)capabilities).setPlatformVersion(selectedDevice.getVersion());
		
		return capabilities;
	}
	
	
	
	public boolean isAndroidReady() {
		return androidReady;
	}

	public void setAndroidReady(boolean androidReady) {
		this.androidReady = androidReady;
	}

	public boolean isIosReady() {
		return iosReady;
	}

	public void setIosReady(boolean iosReady) {
		this.iosReady = iosReady;
	}
}
