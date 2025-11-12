package com.seleniumtests.browserfactory.mobile;

import org.apache.logging.log4j.Logger;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;

public class ExistingAppiumLauncher implements AppiumLauncher {
	
	private static Logger logger = SeleniumRobotLogger.getLogger(ExistingAppiumLauncher.class);
	
	private String appiumServerUrl;

	public ExistingAppiumLauncher(String appiumServerUrl) {
		if (appiumServerUrl.endsWith("/wd/hub/") || appiumServerUrl.endsWith("/")) {
			this.appiumServerUrl = appiumServerUrl;
		} else {
			this.appiumServerUrl = appiumServerUrl + "/";
		}
	}
	
	@Override
	public void startAppium() {
		waitAppiumAlive();

	}

	@Override
	public void stopAppium() {
		// nothing to do
	}
	

	/**
	 * Returns the local appium URL
	 * @return
	 */
	public String getAppiumServerUrl() {
		return appiumServerUrl;
	}
	

	/**
	 * Call /wd/hub/sessions to see if appium is started
	 */
	private void waitAppiumAlive() {

		for (int i=0; i< 60; i++) {
			String endPoint = i % 2 == 0 ? "appium/sessions": "sessions"; // appium 3 / appium 2
			try {
				HttpResponse<String> response = Unirest.get(appiumServerUrl + endPoint).asString();
				if (response.getStatus() == 200) {
					logger.info("appium has started");
		        	break;
				}
			} catch (UnirestException e) {
				logger.info("appium not started");
			}
			
			
			WaitHelper.waitForSeconds(1);
		}
	}

}
