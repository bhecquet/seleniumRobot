package com.seleniumtests.connectors.selenium;

import org.apache.log4j.Logger;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class SeleniumRobotServerConnector {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotServerConnector.class);
	public static final String SELENIUM_SERVER_URL = "seleniumServerUrl";
	public static final String SELENIUM_SERVER_LOGIN = "seleniumServerLogin";
	public static final String SELENIUM_SERVER_PASWORD = "seleniumServerPassword";

	protected String url;
	protected boolean active = false;
	
	public SeleniumRobotServerConnector() {
		active = isActive();
	}
	
	protected abstract boolean isAlive();
	
	protected boolean isAlive(String testUrl) {
		Unirest.setTimeouts(1500, 1500);
		try {
			return Unirest.get(url + testUrl).asString().getStatus() == 200;
		} catch (UnirestException e) {
			return false;
		} finally {
			Unirest.setTimeouts(10000, 60000);
		}
	}
	
	protected boolean isActive() {
		if (!SeleniumTestsContextManager.getThreadContext().getConfiguration().containsKey(SELENIUM_SERVER_URL)) {
			logger.warn(String.format("selenium server won't be used, key %s is not available in configuration. It must be the root URL of server 'http://<host>:<port>'", SELENIUM_SERVER_URL));
			return false;
		} else {
			url = SeleniumTestsContextManager.getThreadContext().getConfiguration().get(SELENIUM_SERVER_URL);
			return true;
		}
	}
	
	public boolean getActive() {
		return active;
	}
}
