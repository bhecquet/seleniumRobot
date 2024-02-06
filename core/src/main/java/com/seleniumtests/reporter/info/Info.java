package com.seleniumtests.reporter.info;

import com.seleniumtests.util.logging.SeleniumRobotLogger;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.ITestResult;

public abstract class Info {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(Info.class);

	public String getInfo() {
		return description;
	}
	
	protected String description;
	
	protected Info(String info) {
		this.description = info;
	}
	
	public abstract String encode(String format);

	public abstract JSONObject toJson();
}
