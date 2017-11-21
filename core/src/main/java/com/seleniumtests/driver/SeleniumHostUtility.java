package com.seleniumtests.driver;

import java.awt.image.BufferedImage;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.customexception.ConfigurationException;

public abstract class SeleniumHostUtility {

	public abstract void uploadFile(String filePath);
	
	public abstract BufferedImage captureDesktopToBuffer();
	
	public static SeleniumHostUtility getInstance(DriverMode driverMode) {
		if (driverMode == DriverMode.LOCAL) {
			return new LocalSeleniumHostUtility();
		} else if (driverMode == DriverMode.GRID) {
			SeleniumGridConnector gridConnector = SeleniumGridConnectorFactory.getInstance();
			if (gridConnector instanceof SeleniumRobotGridConnector) {
				return new GridSeleniumHostUtility(gridConnector);
			} else {
				throw new ConfigurationException("Only specific seleniumRobot grid is supported for remote host actions");
			}
		} else {
			throw new ConfigurationException("Only GRID and LOCAL mode are supported for SeleniumHostUtility actions");
		}
	}
}
