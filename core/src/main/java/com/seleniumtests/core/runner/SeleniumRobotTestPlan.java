package com.seleniumtests.core.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.Listeners;

import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

@Listeners({com.seleniumtests.reporter.SeleniumTestsReporter2.class, 
//	com.seleniumtests.reporter.TestListener.class, 
	com.seleniumtests.reporter.PerformanceReporter.class,
	com.seleniumtests.reporter.SeleniumRobotServerTestRecorder.class,
	com.seleniumtests.reporter.TestManagerReporter.class,
	com.seleniumtests.reporter.JsonReporter.class,
	com.seleniumtests.core.runner.SeleniumRobotTestListener.class})
public class SeleniumRobotTestPlan {
	
	private static Map<Thread, Boolean> cucumberTest = Collections.synchronizedMap(new HashMap<>());
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestPlan.class);
	
	public static void setCucumberTest(boolean cucumberTestIn) {
		SeleniumRobotTestPlan.cucumberTest.put(Thread.currentThread(), cucumberTestIn);
	}
	

	public static boolean isCucumberTest() {
		Boolean isCucumberT = SeleniumRobotTestPlan.cucumberTest.get(Thread.currentThread());
		if (isCucumberT == null) {
			return false;
		}
		return isCucumberT;
	}

	/**
     * Get parameter from configuration
     * 
     * @param key
     * 
     * @return String
     */
    public static String param(String key) {
    	return PageObject.param(key);
    }
}
