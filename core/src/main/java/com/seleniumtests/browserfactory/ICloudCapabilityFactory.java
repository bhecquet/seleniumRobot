package com.seleniumtests.browserfactory;

import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.remote.options.SupportsAppOption;
import org.openqa.selenium.MutableCapabilities;

import com.seleniumtests.driver.DriverConfig;

import java.util.Optional;

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
		Optional<String> applicationOption = getApp(capabilities);
		boolean uploadApp = true;
		if (applicationOption.isPresent() && applicationOption.get().startsWith(NO_APP_UPLOAD)) {
			uploadApp = false;
			capabilities.setCapability(CapabilityHelpers.APPIUM_PREFIX + SupportsAppOption.APP_OPTION, applicationOption.get().replace(NO_APP_UPLOAD, ""));
		}
		
		return uploadApp;
	}

}
