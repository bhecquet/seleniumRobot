package com.seleniumtests.reporter.info;

import com.seleniumtests.util.logging.SeleniumRobotLogger;
import org.apache.log4j.Logger;

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
}
