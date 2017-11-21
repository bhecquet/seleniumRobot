package com.seleniumtests.driver;

import java.awt.image.BufferedImage;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;

/*
 * Use SeleniumRobotGridConnector
 */
public class GridSeleniumHostUtility extends SeleniumHostUtility {
	
	private SeleniumGridConnector gridConnector;

	public GridSeleniumHostUtility(SeleniumGridConnector gridConnector) {
		this.gridConnector = gridConnector;
	}

	@Override
	public void uploadFile(String filePath) {
		gridConnector.uploadFile(filePath);

	}

	@Override
	public BufferedImage captureDesktopToBuffer() {
		return gridConnector.captureDesktopToBuffer();
	}

}
