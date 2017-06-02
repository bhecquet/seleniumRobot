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
package com.seleniumtests.browserfactory.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.osutility.OSCommand;

import io.appium.java_client.remote.MobileCapabilityType;

public class MobileDeviceSelector {
	
	private AdbWrapper adbWrapper;
	private Boolean androidReady;
	private Boolean iosReady;
	
	// examples
	// Apple TV 1080p (10.2) [6444F65D-DA15-4505-8307-4520FD346ACE] (Simulator)
	// Mac mini [CBFA063D-2535-5FD8-BA05-CE8D3683D6BA]
	private static final Pattern REG_DEVICE = Pattern.compile("([a-zA-Z0-9 ]+).*?\\((\\d+\\.\\d+\\.?\\d?)\\).*?\\[(.*?)\\].*");
	
	public MobileDeviceSelector initialize() {
		try {
			adbWrapper = new AdbWrapper();
			androidReady = true;
		} catch (ConfigurationException e) {
			adbWrapper = null;
			androidReady = false;
		}
		iosReady = true;
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
	public MobileDevice getRelevantMobileDevice(DesiredCapabilities capabilities) {
		isInitialized();
		Object deviceName = capabilities.getCapability(MobileCapabilityType.DEVICE_NAME);
		Object platformName = capabilities.getCapability(MobileCapabilityType.PLATFORM_NAME);
		Object platformVersion = capabilities.getCapability(MobileCapabilityType.PLATFORM_VERSION);
		
		if (deviceName == null
				&& platformName == null
				&& platformVersion == null
				) {
			throw new ConfigurationException("at least one mobile capaiblity must be provided: DEVICE_NAME, PLATFORM_NAME, PLATFORM_VERSION");
		}
		
		List<MobileDevice> deviceList = new ArrayList<>();
		
		if (androidReady) {
			deviceList.addAll(adbWrapper.getDeviceList());
		}
		
		if (iosReady) {
			deviceList.addAll(parseIosDevices());
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
	public DesiredCapabilities updateCapabilitiesWithSelectedDevice(DesiredCapabilities capabilities) {
		MobileDevice selectedDevice = getRelevantMobileDevice(capabilities);
		
		if ("android".equals(selectedDevice.getPlatform())) {
			capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, selectedDevice.getId());
		}
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, selectedDevice.getPlatform());
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, selectedDevice.getVersion());
		
		return capabilities;
	}
	
	/**
	 * Use 'instruments -s devices' to know which devices are available for test
	 */
	public List<MobileDevice> parseIosDevices() {
		String output = OSCommand.executeCommandAndWait("instruments -s devices");
		List<MobileDevice> devList = new ArrayList<>();
		
		for (String line: output.split("\n")) {
			line = line.trim();
			Matcher matcher = REG_DEVICE.matcher(line);

			if (matcher.matches()) {
				MobileDevice dev = new MobileDevice(matcher.group(1).trim(), matcher.group(3), "iOS", matcher.group(2), Arrays.asList(new String[] {"safari"}));
				devList.add(dev);
			}
		}
		return devList;
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
