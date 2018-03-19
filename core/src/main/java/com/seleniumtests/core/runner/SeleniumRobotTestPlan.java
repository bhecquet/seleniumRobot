package com.seleniumtests.core.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.Listeners;

import com.seleniumtests.core.TestTasks;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

@Listeners({com.seleniumtests.reporter.reporters.ReporterControler.class,
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
     * env.ini file. Variable will be stored as a variable of the current tested application
     * @param key				name of the param
     * @param value				value of the parameter (or new value if we update it)
     */
    public void createOrUpdateParam(String key, String value) {
    	TestTasks.createOrUpdateParam(key, value);
    }
    
    /**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * @param key					name of the param
     * @param value					value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     */
    public void createOrUpdateParam(String key, String value, boolean specificToVersion) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion);
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
