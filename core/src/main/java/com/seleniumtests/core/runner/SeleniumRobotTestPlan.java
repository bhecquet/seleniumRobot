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
package com.seleniumtests.core.runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.reporter.logger.StringInfo;
import com.seleniumtests.util.logging.ScenarioLogger;

@Listeners({com.seleniumtests.reporter.reporters.ReporterControler.class,
	com.seleniumtests.core.runner.SeleniumRobotTestListener.class//,
	//ReportPortalTestListener.class
	})
public class SeleniumRobotTestPlan {
	
	private static Map<Thread, Boolean> cucumberTest = Collections.synchronizedMap(new HashMap<>());
	protected static final Logger logger = ScenarioLogger.getScenarioLogger(SeleniumRobotTestPlan.class);
	
	public SeleniumRobotTestPlan() {
		System.setProperty( "file.encoding", "UTF-8" );
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
	 * issue #150: set driver to null in case it was not cleaned before
	 * This method will be called before any other before method
	 * @param method
	 */
	@BeforeMethod(alwaysRun=true) 
	public void startTestMethod(Method method) {
		WebUIDriver.setWebDriver(null);

		// issue #297: be sure we reset the driver name before the test starts
		WebUIDriver.resetCurrentWebUiDriverName();
	}
	
	/**
	 * According to TestNG doc, this method will be executed after the \@AfterMethod inside test classes
	 * #issue 136: This will close any remaining browser for this thread and forbid user to create a new driver in other \@AfterXXX
	 */
	@AfterMethod(alwaysRun=true)
	public void finishTestMethod(Method method, ITestResult testResult) {
		
		// stop video capture and log file
		File videoFile = WebUIDriver.stopVideoCapture();
		if (videoFile != null) {

	        if (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.TRUE
	        		|| (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.ON_SUCCESS && testResult.isSuccess())
	        		|| (SeleniumTestsContextManager.getThreadContext().getVideoCapture() == VideoCaptureMode.ON_ERROR && !testResult.isSuccess())) {

	        	((ScenarioLogger)logger).logFileToTestEnd(videoFile.getAbsoluteFile(), "Video capture");
	        	logger.info("Video file copied to " + videoFile.getAbsolutePath());
	        	
			} else {
				try {
					Files.delete(Paths.get(videoFile.getAbsolutePath()));
				} catch (IOException e) {
					logger.warn(String.format("Video file %s not deleted: %s", videoFile.getAbsoluteFile(), e.getMessage()));
				}
			}
		}

		WebUIDriver.cleanUp();
		SeleniumTestsContextManager.getThreadContext().setDriverCreationBlocked(true);
		
		SeleniumRobotTestListener.getCurrentListener().onTestFullyFinished(testResult);
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
     * Get parameter from configuration using pattern
     * If multiple variables match the pattern, only one is returned
     * @param keyPattern	Pattern for searching key. If null, no filtering will be done on key
     * @return
     */
    public static String param(Pattern keyPattern) {
    	return TestTasks.param(keyPattern);
    }
    
    /**
     * Get parameter from configuration using pattern
     * If multiple variables match the pattern, only one is returned
     * @param keyPattern	Pattern for searching key. If null, no filtering will be done on key
     * @param valuePattern	Pattern for searching value. If null, no filtering will be done on value
     * @return
     */
    public static String param(Pattern keyPattern, Pattern valuePattern) {
    	return TestTasks.param(keyPattern, valuePattern);
    }
    
    /**
	 * Method for creating or updating a variable on the seleniumRobot server (or locally if server is not used)
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * Variable will be stored as a variable of the current tested application
     * @param key				name of the param
     * @param value				value of the parameter (or new value if we update it)
     */
    public void createOrUpdateParam(String key, String value) {
    	TestTasks.createOrUpdateParam(key, value);
    }
    
    /**
	 * Method for creating or updating a variable locally. If selenium server is not used, there is no difference with 'createOrUpdateParam'. 
	 * If seleniumRobot server is used, then, this method will only change variable value locally, not updating the remote one
	 * @param key
	 * @param newValue
	 */
	public void createOrUpdateLocalParam(String key, String newValue) {
		TestTasks.createOrUpdateLocalParam(key, newValue);
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
    public void createOrUpdateParam(String key, String value, boolean specificToVersion) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion);
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
    public void createOrUpdateParam(String key, String value, boolean specificToVersion, int timeToLive, boolean reservable) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion, timeToLive, reservable);
    }
    
    /**
     * In case the scenario uses several drivers, switch to one or another using this method, so that any new calls will go through this driver
     * @param driverName
     */
    public WebDriver switchToDriver(String driverName) {
    	return TestTasks.switchToDriver(driverName);
    }
    
    /**
     * Kills the named process, locally or remotely
     * @param processName		name of the process (do not provide .exe extension on windows)
     */
    public void killProcess(String processName) {
    	TestTasks.killProcess(processName);
    }
    
    /**
     * get list of the named process, locally or remotely
     * @param processName
     */
    public List<Integer> getProcessList(String processName) {
    	return TestTasks.getProcessList(processName);
    }
    
    /**
     * Add step to current test
     * @param stepName
     * @param passwordsToMask	array of strings that must be replaced by '*****' in reports
     */
    public void addStep(String stepName, String ... passwordToMask) {
    	TestTasks.addStep(stepName, passwordToMask);
    }
    
    /**
     * execute command
     * @param program			name of the program
     * @param args				array of arguments to give to program
     */
    public String executeCommand(String program, String ... args) {
    	return TestTasks.executeCommand(program, args);
    }
    
    /**
     * returns the robot configuration
     * @return
     */
    public SeleniumTestsContext robotConfig() {
    	return SeleniumTestsContextManager.getThreadContext();
    }
    
    /**
     * Allow to increment the maxRetry in case an event occurs
     * Increment will be allowed up to 2 times the total defined by configuration (default is 2)
     */
    public void increaseMaxRetry() {
    	SeleniumRobotTestListener.increaseMaxRetry();
    }
    
    /**
     * Add a information to the test, that will be available in report files at test level
     * @param key	name of the information to display
     * @param value	A StringInfo or HyperlinkInfo instance to display the value 
     */
    public void addTestInfo(String key, StringInfo value) {
    	((ScenarioLogger)logger).logTestInfo(key, value);
    }
}
