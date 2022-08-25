/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.core;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;

import com.neotys.selenium.proxies.NLWebDriver;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.osutility.ProcessInfo;

import net.lightbody.bmp.BrowserMobProxy;

/**
 * Class for storing tasks that can be used in test and / or in webpages
 * @author s047432
 *
 */
public class TestTasks {
	
	private static final String ERROR_NO_GRID_CONNECTOR_ACTIVE = "No grid connector active";
	private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(TestTasks.class); 
	
	private TestTasks() {
		// nothing to do
	}

	 /**
     * Kills the named process, locally or remotely
     * @param processName		name of the process (do not provide .exe extension on windows)
     */
	public static void killProcess(String processName) {
    	if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
    		OSUtilityFactory.getInstance().killProcessByName(processName, true);
    		
    	} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
    		SeleniumGridConnector gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
    		if (gridConnector != null) {
    			gridConnector.killProcess(processName);
    		} else {
				throw new ScenarioException(ERROR_NO_GRID_CONNECTOR_ACTIVE);
			}
    		
    	} else {
    		logger.error("killing a process is only supported in local and grid mode");
    	}
    }
	
	/**
	 * returns the list of processes, on the node, whose name without extension is the requested one
	 * e.g: getProcessList("WINWORD")
	 * Case will be ignored
	 * @param processName
	 * @return
	 */
	public static List<Integer> getProcessList(String processName) {
		if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
			return OSUtilityFactory.getInstance().getRunningProcesses(processName).stream()
				.map(ProcessInfo::getPid)
				.map(Integer::valueOf)
				.collect(Collectors.toList());
			
		} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
			SeleniumGridConnector gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
			if (gridConnector != null) {
				return gridConnector.getProcessList(processName);
			} else {
				throw new ScenarioException(ERROR_NO_GRID_CONNECTOR_ACTIVE);
			}
		} else {
			throw new ScenarioException("killing a process is only supported in local and grid mode");
		}
	}

    /**
     * Execute a command
     * If test is run locally, you can execute any command, limit is the rights given to the user
     * If test is run through seleniumRobot grid, only commands allowed by grid will be allowed
     * @param program
     * @param args
     */
    public static String executeCommand(String program, String ... args) {
    	return executeCommand(program, -1, null, args);
    }
    
    /**
     * Execute a command
     * If test is run locally, you can execute any command, limit is the rights given to the user
     * If test is run through seleniumRobot grid, only commands allowed by grid will be allowed
     * @param program	program to execute. If program is to be searched in system path, then, program should be "OSCommand.USE_PATH" + "myprogram", and first arg will be the program name
     * @param charset	the charset used to read program output
     * @param timeout	timeout in seconds. After this duration, program will be terminated
     * @param args		arguments to give to program
     */
    public static String executeCommand(String program, int timeout, Charset charset, String ... args) {
    	if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
    		String[] cmd = new String[] {program};
    		cmd = ArrayUtils.addAll(cmd, args);
    		return OSCommand.executeCommandAndWait(cmd, timeout, charset);
    		
    	} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
    		SeleniumGridConnector gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
    		if (gridConnector != null) {
    			return gridConnector.executeCommand(program, timeout, args);
    		} else {
    			throw new ScenarioException(ERROR_NO_GRID_CONNECTOR_ACTIVE);
    		}
    		
    	} else {
    		throw new ScenarioException("command execution only supported in local and grid mode");
    	}
    }
	
	/**
	 * Method for creating or updating a variable on the seleniumRobot server (or locally if server is not used)
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key
     * @param newValue
     */
	public static void createOrUpdateParam(String key, String newValue) {
		createOrUpdateParam(key, newValue, true);
    }
	
	/**
	 * Method for creating or updating a variable locally. If selenium server is not used, there is no difference with 'createOrUpdateParam'. 
	 * If seleniumRobot server is used, then, this method will only change variable value locally, not updating the remote one
	 * @param key
	 * @param newValue
	 */
	public static void createOrUpdateLocalParam(String key, String newValue) {
		createOrUpdateParam(key, newValue, false, TestVariable.TIME_TO_LIVE_INFINITE, false, true);
	}
	
	/**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param newValue				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     */
	public static void createOrUpdateParam(String key, String newValue, boolean specificToVersion) {
		createOrUpdateParam(key, newValue, specificToVersion, TestVariable.TIME_TO_LIVE_INFINITE, false, false);
	}
		
	/**
     * Method for creating or updating a variable. If variables are get from seleniumRobot server, this method will update the value on the server
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param newValue				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     * @param timeToLive			if > 0, this variable will be destroyed after some days (defined by variable). A positive value is mandatory if reservable is set to true 
     * 								because multiple variable can be created
     * @param reservable			if true, this variable will be set as reservable in variable server. This means it can be used by only one test at the same time
     * 								True value also means that multiple variables of the same name can be created and a timeToLive > 0 MUST be provided so that server database is regularly purged
     */
	public static void createOrUpdateParam(String key, String newValue, boolean specificToVersion, int timeToLive, boolean reservable) {
		createOrUpdateParam(key, newValue, specificToVersion, timeToLive, reservable, false);
		
	}
	
	/**
     * Method for creating or updating a variable. If variables are get from seleniumRobot server, this method will update the value on the server
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param newValue				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     * @param timeToLive			if > 0, this variable will be destroyed after some days (defined by variable). A positive value is mandatory if reservable is set to true 
     * 								because multiple variable can be created
     * @param reservable			if true, this variable will be set as reservable in variable server. This means it can be used by only one test at the same time
     * 								True value also means that multiple variables of the same name can be created and a timeToLive > 0 MUST be provided so that server database is regularly purged
     * @param localUpdateOnly		it true, value won't be set on remote server
     */
	public static void createOrUpdateParam(String key, String newValue, boolean specificToVersion, int timeToLive, boolean reservable, boolean localUpdateOnly) {
		
		SeleniumRobotVariableServerConnector variableServer = SeleniumTestsContextManager.getThreadContext().getVariableServer();
		
		if (reservable && timeToLive <= 0) {
			throw new ScenarioException("When creating a variable as reservable, a positive timeToLive value MUST be provided");
		}
		
		// check if we update an existing variable
		TestVariable variable = SeleniumTestsContextManager.getThreadContext().getConfiguration().get(key);
		if (variable == null || reservable) {
			variable = new TestVariable(key, newValue);	
		} else {
			variable.setValue(newValue);
		}
		variable.setReservable(reservable);
		variable.setTimeToLive(timeToLive);
		if (variableServer != null && !localUpdateOnly) {
			variable = variableServer.upsertVariable(variable, specificToVersion);
		}
		
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(variable.getName(), variable);
	}
	
	 /**
     * Get parameter from configuration
     * If configuration can be get from threadContext, it's done, else, we look at global context
     * 
     * @param key
     * 
     * @return String
     */
    public static String param(String key) {
    	try {
    		return getParam(SeleniumTestsContextManager.getThreadContext(), key);
    	} catch (ConfigurationException e) {
    		return getParam(SeleniumTestsContextManager.getGlobalContext(), key);
    	}
    }
    
    /**
     * Get parameter from configuration
     * If multiple variables match the pattern, only one is returned
     * @param keyPattern	Pattern for searching key. If null, no filtering will be done on key
     * @return
     */
    public static String param(Pattern keyPattern) {
    	try {
    		return getParamWithPattern(SeleniumTestsContextManager.getThreadContext(), keyPattern, null);
    	} catch (ConfigurationException e) {
    		return getParamWithPattern(SeleniumTestsContextManager.getGlobalContext(), keyPattern, null);
    	}
    }
    

    /**
     * Get parameter from configuration
     * If multiple variables match the pattern, only one is returned
     * @param keyPattern	Pattern for searching key. If null, no filtering will be done on key
     * @param valuePattern	Pattern for searching value. If null, no filtering will be done on value
     * @return
     */
    public static String param(Pattern keyPattern, Pattern valuePattern) {
    	try {
    		return getParamWithPattern(SeleniumTestsContextManager.getThreadContext(), keyPattern, valuePattern);
    	} catch (ConfigurationException e) {
    		return getParamWithPattern(SeleniumTestsContextManager.getGlobalContext(), keyPattern, valuePattern);
    	}
    }
  
    
    private static String getParam(SeleniumTestsContext context, String key) {
    	TestVariable value = context.getConfiguration().get(key);
    	if (value == null) {
    		logger.warning(String.format("Variable %s is not defined", key));
    		return "";
    	}
    	return value.getValue();
    }
    
    private static String getParamWithPattern(SeleniumTestsContext context, Pattern keyPattern, Pattern valuePattern) {
    	List<TestVariable> matchingVariables = context.getConfiguration()
				.values()
				.stream()
				.filter(v -> keyPattern == null || keyPattern.matcher(v.getName()).find())
				.filter(v -> valuePattern == null || valuePattern.matcher(v.getValue()).find())
				.collect(Collectors.toList());
    	Collections.shuffle(matchingVariables);
    	
    	if (matchingVariables.isEmpty()) {
    		logger.warning(String.format("Variable %s [%s] is not defined", 
    				keyPattern == null ? "null": keyPattern.pattern(), 
    				valuePattern == null ? "null": valuePattern.pattern()));
    		return "";
    	}
    	return matchingVariables.get(0).getValue();
    }
   
    public static void terminateCurrentStep() {
    	NLWebDriver neoloadDriver = WebUIDriver.getNeoloadDriver();
    	
    	// log the previous step if it exists and create the new one
    	TestStep previousStep = TestStepManager.getCurrentRootTestStep();
    	if (previousStep != null) {
    		previousStep.updateDuration();
    		TestStepManager.logTestStep(previousStep);
    		
    		if (neoloadDriver != null) {
				neoloadDriver.stopTransaction();
			}
    	}
    }
    
    /**
     * Add step to test and add snapshot to it
     * If a previous step exists, store it
     * @param stepName			name of the step
     * 							If name is null, only previous step is stored, no new step is created
     * @param passwordsToMask	array of strings that must be replaced by '*****' in reports
     */
    public static void addStep(String stepName, String ... passwordsToMask ) {
    	if (!SeleniumTestsContextManager.getThreadContext().isManualTestSteps()) {
    		throw new ConfigurationException("manual steps can only be used when automatic steps are disabled ('manualTestSteps' option set to true)");
    	}
    	
    	NLWebDriver neoloadDriver = WebUIDriver.getNeoloadDriver();
    	
    	// log the previous step if it exists and create the new one
    	terminateCurrentStep();
    	
    	// stepName is null when test is terminating. We don't create a new step
    	if (stepName != null) {
	    	TestStep step = new TestStep(stepName, Reporter.getCurrentTestResult(), Arrays.asList(passwordsToMask), SeleniumTestsContextManager.getThreadContext().getMaskedPassword());
	    	TestStepManager.setCurrentRootTestStep(step);
	    	capturePageSnapshot();
	    	
	    	// start a new page when using BrowserMobProxy (network capture)
	    	BrowserMobProxy mobProxy = WebUIDriver.getBrowserMobProxy();
	    	if (mobProxy != null) {
	    		mobProxy.newPage(stepName);
	    	}
	    	
	    	// start a new transaction when using Neoload
	    	if (neoloadDriver != null) {
				neoloadDriver.startTransaction(stepName);
			}
    	}
    }
    
    public static void capturePageSnapshot() {
    	if (WebUIDriver.getWebDriver(false) != null) {
    		ScreenShot screenshot = new ScreenshotUtil().capture(SnapshotTarget.PAGE, ScreenShot.class);
    		
    		if (screenshot != null) {
    			logger.logScreenshot(screenshot);
    		}
    	}
    }
    
    /**
     * In case the scenario uses several drivers, switch to one or another using this method, so that any new calls will go through this driver
     * @param driverName
     */
    public static WebDriver switchToDriver(String driverName) {
    	WebUIDriver.switchToDriver(driverName);
    	return WebUIDriver.getWebDriver(false);
    }
}
