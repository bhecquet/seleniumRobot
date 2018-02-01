package com.seleniumtests.core.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.Listeners;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

@Listeners({com.seleniumtests.reporter.SeleniumTestsReporter2.class, 
	com.seleniumtests.reporter.PerformanceReporter.class,
	com.seleniumtests.reporter.SeleniumRobotServerTestRecorder.class,
	com.seleniumtests.reporter.TestManagerReporter.class,
	com.seleniumtests.reporter.JsonReporter.class,
	com.seleniumtests.core.runner.SeleniumRobotTestListener.class
	})
public class SeleniumRobotTestPlan {
	
	private static Map<Thread, Boolean> cucumberTest = Collections.synchronizedMap(new HashMap<>());
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestPlan.class);
	
	public SeleniumRobotTestPlan() {
		// do nothing
	}
	
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
    	return TestTasks.param(key);
    }
    
    /**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * @param key
     * @param value
     */
    public void createOrUpdateParam(String key, String value) {
    	TestTasks.createOrUpdateParam(key, value);
    }
    
    /**
     * Kills the named process, locally or remotely
     * @param processName
     */
    public void killProcess(String processName) {
    	TestTasks.killProcess(processName);
    }
    
    /**
     * Add step to current test
     * @param stepName
     */
    public void addStep(String stepName) {
    	TestTasks.addStep(stepName);
    }
}
