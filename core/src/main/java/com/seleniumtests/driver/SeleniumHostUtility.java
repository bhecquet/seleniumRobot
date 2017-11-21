package com.seleniumtests.driver;

import java.awt.image.BufferedImage;

import com.seleniumtests.customexception.ConfigurationException;

public abstract class SeleniumHostUtility {

	public abstract void uploadFile(String filePath);
	
	public abstract BufferedImage captureDesktopToBuffer();
	
	public static SeleniumHostUtility getInstance(DriverMode driverMode) {
		if (driverMode == DriverMode.LOCAL) {
			return new LocalSeleniumHostUtility();
		} else if (driverMode == DriverMode.GRID) {
			return new GridSeleniumHostUtility();
		} else {
			throw new ConfigurationException("Only GRID and LOCAL mode are supported for SeleniumHostUtility actions");
		}
	}
}
