package com.seleniumtests.browserfactory.mobile;

import org.apache.logging.log4j.Logger;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class ExistingAppiumLauncher implements AppiumLauncher {
	
	private static Logger logger = SeleniumRobotLogger.getLogger(ExistingAppiumLauncher.class);
	
	private String appiumServerUrl;

	public ExistingAppiumLauncher(String appiumServerUrl) {
		if (appiumServerUrl.endsWith("/wd/hub/")) {
			this.appiumServerUrl = appiumServerUrl;
		} else {
			this.appiumServerUrl = appiumServerUrl + "/wd/hub/";
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
			try {
				HttpResponse<String> response = Unirest.get(appiumServerUrl + "sessions").asString();
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
