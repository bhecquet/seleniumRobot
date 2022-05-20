package com.seleniumtests.browserfactory;

import org.openqa.selenium.MutableCapabilities;

import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.MobileCapabilityType;

public abstract class ICloudCapabilityFactory extends ICapabilitiesFactory {

	protected static final String ANDROID_PLATFORM = "android";
	protected static final String IOS_PLATFORM = "ios";
	protected static final String NO_APP_UPLOAD = "NO_UPLOAD:";

	protected ICloudCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	/**
	 * Do we upload application ?
	 * It depends on "NO_UPLOAD:" prefix in path
	 * Application path is changed accordingly
	 * @param capabilities
	 * @return
	 */
	protected boolean isUploadApp(MutableCapabilities capabilities) {
		boolean uploadApp = !((String) capabilities.getCapability(MobileCapabilityType.APP)).startsWith(NO_APP_UPLOAD);
		if (!uploadApp) {
			capabilities.setCapability(MobileCapabilityType.APP, ((String) capabilities.getCapability(MobileCapabilityType.APP)).replace(NO_APP_UPLOAD, ""));
		}
		
		return uploadApp;
	}

}
