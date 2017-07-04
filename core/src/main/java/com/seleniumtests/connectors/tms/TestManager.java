package com.seleniumtests.connectors.tms;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.seleniumtests.connectors.tms.hpalm.HpAlmConnector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class TestManager {
	

	protected static final Logger logger = SeleniumRobotLogger.getLogger(TestManager.class);
	protected Boolean initialized;
	
	public TestManager() {
		initialized = false;
	}
	
	public abstract void recordResult();
	
	public abstract void recordResultFiles();
	
	public abstract void login();

	public abstract void init();

	public abstract void logout();
	
	public static TestManager getInstance(JSONObject configString) {
		String type;
		try {
			type = configString.getString("type");
		} catch (JSONException e) {
			throw new ConfigurationException("Test manager type must be provided. ex: {'type': 'hp', 'run': '3'}");
		}
		
		if ("hp".equals(type)) {
			return new HpAlmConnector(configString);
		} else {
			throw new ConfigurationException(String.format("TestManager type [%s] is unknown, valid values are: ['hp']", type));
		}
	}

	public Boolean getInitialized() {
		return initialized;
	}

}
