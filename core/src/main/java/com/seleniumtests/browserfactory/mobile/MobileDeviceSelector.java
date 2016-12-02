package com.seleniumtests.browserfactory.mobile;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.customexception.ConfigurationException;

import io.appium.java_client.remote.MobileCapabilityType;

public class MobileDeviceSelector {
	
	private AdbWrapper adbWrapper;
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
			if (deviceName != null && device.getName() != null && !device.getName().equalsIgnoreCase(deviceName.toString())) {
				keep = false;
			}
			if (platformName != null && device.getPlatform() != null && !device.getPlatform().equalsIgnoreCase(platformName.toString())) {
				keep = false;
			}
			if (platformVersion != null && device.getVersion() != null && !device.getVersion().equalsIgnoreCase(platformVersion.toString())) {
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
			// TODO: handle iOS devices
		}
		
		List<MobileDevice> filteredDeviceList = filterDevices(deviceList, 
																deviceName == null ? null: deviceName.toString(), 
																platformName == null ? null: platformName.toString(), 
																platformVersion == null ? null: platformVersion.toString()
		);
		
		if (filteredDeviceList.isEmpty()) {
			throw new ConfigurationException("no matching device found");
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
		
		if (selectedDevice.getPlatform().equals("android")) {
			capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, selectedDevice.getId());
		}
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, selectedDevice.getPlatform());
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, selectedDevice.getVersion());
		
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
