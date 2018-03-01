package com.seleniumtests.core;

import org.apache.log4j.Logger;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

/**
 * Class for storing tasks that can be used in test and / or in webpages
 * @author s047432
 *
 */
public class TestTasks {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestPlan.class);
	
	private TestTasks() {
		// nothing to do
	}

	public static void killProcess(String processName) {
    	if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
    		OSUtilityFactory.getInstance().killProcessByName(processName, true);
    	} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
    		SeleniumGridConnector gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
    		gridConnector.killProcess(processName);
    	} else {
    		logger.error("killing a process is only supported in local and grid mode");
    	}
    }
	
	/**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key
     * @param value
     */
	public static void createOrUpdateParam(String key, String newValue) {
		
		SeleniumRobotVariableServerConnector variableServer = SeleniumTestsContextManager.getThreadContext().getVariableServer();
		
		if (variableServer == null) {
    		throw new ScenarioException("Cannot create or update variable if seleniumRobot server is not connected");
    	}
    	
    	// check if we update an existing variable
    	TestVariable variable = SeleniumTestsContextManager.getThreadContext().getConfiguration().get(key);
    	if (variable == null) {
    		variable = new TestVariable(key, newValue);
    	} else {
    		variable.setValue(newValue);
    	}
    	
    	TestVariable newVariable = variableServer.upsertVariable(variable);
    	SeleniumTestsContextManager.getThreadContext().getConfiguration().put(newVariable.getName(), newVariable);
    }
	
	 /**
     * Get parameter from configuration
     * 
     * @param key
     * 
     * @return String
     */
    public static String param(String key) {
    	TestVariable value = SeleniumTestsContextManager.getThreadContext().getConfiguration().get(key);
    	if (value == null) {
    		TestLogging.error(String.format("Variable %s is not defined", key));
    		return "";
    	}
    	return value.getValue();
    }
    
    /**
     * Add step to test and add snapshot to it
     * If a previous step exists, store it
     * @param stepName	name of the step
     * 					If name is null, only previous step is stored, no new step is created
     */
    public static void addStep(String stepName) {
    	if (!SeleniumTestsContextManager.getThreadContext().isManualTestSteps()) {
    		throw new ConfigurationException("manual steps can only be used when automatic steps are disabled ('manualTestSteps' option set to true)");
    	}
    	
    	// log the previous step if it exists and create the new one
    	TestStep previousStep = TestLogging.getCurrentRootTestStep();
    	if (previousStep != null) {
    		previousStep.updateDuration();
    		TestLogging.logTestStep(previousStep);
    	}
    	
    	if (stepName != null) {
	    	TestStep step = new TestStep(stepName, TestLogging.getCurrentTestResult());
	    	TestLogging.setCurrentRootTestStep(step);
	    	capturePageSnapshot();
    	}
    }
    
    public static void capturePageSnapshot() {
    	if (WebUIDriver.getWebDriver() != null) {
    		TestLogging.logScreenshot(new ScreenshotUtil().captureWebPageSnapshot());
    	}

    }
}
