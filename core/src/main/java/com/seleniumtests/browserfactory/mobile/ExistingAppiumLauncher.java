package com.seleniumtests.browserfactory.mobile;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

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
			try (CloseableHttpClient client = HttpClients.createDefault();) {
				HttpGet request = new HttpGet(appiumServerUrl + "sessions"); 
		        CloseableHttpResponse response = client.execute(request);

		        if (response.getStatusLine().getStatusCode() == 200) {
		        	logger.info("appium has started");
		        	break;
		        }
			} catch (IOException e) {
				logger.info("appium not started");
			}
			WaitHelper.waitForSeconds(1);
		}
	}

}
