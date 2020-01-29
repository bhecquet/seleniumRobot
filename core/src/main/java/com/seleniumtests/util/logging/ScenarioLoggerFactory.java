package com.seleniumtests.util.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

class ScenarioLoggerFactory implements LoggerFactory {

	ScenarioLoggerFactory() {
	}

	public Logger makeNewLoggerInstance(String name) {
		return new ScenarioLogger(name);
	}
}
