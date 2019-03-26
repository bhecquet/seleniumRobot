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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

@Listeners({com.seleniumtests.reporter.reporters.ReporterControler.class,
	com.seleniumtests.core.runner.SeleniumRobotTestListener.class
	})
public class SeleniumRobotTestPlan {
	
	private static Map<Thread, Boolean> cucumberTest = Collections.synchronizedMap(new HashMap<>());
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotTestPlan.class);
	
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
	}
	
	/**
	 * According to TestNG doc, this method will be executed after the \@AfterMethod inside test classes
	 * #issue 136: This will close any remaining browser for this thread and forbid user to create a new driver in other \@AfterXXX
	 */
	@AfterMethod(alwaysRun=true)
	public void finishTestMethod(Method method) {
		WebUIDriver.cleanUp();
		SeleniumTestsContextManager.getThreadContext().setDriverCreationBlocked(true);
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
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param newValue				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     * @param timeToLive			if > 0, this variable will be destroyed after some days (defined by variable)
     * @param reservable			if true, this variable will be set as reservable in variable server. This means it can be used by only one test at the same time
     */
    public void createOrUpdateParam(String key, String value, boolean specificToVersion, int timeToLive, boolean reservable) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion, timeToLive, reservable);
    }
    
    /**
     * Kills the named process, locally or remotely
     * @param processName
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
     * returns the robot configuration
     * @return
     */
    public SeleniumTestsContext robotConfig() {
    	return SeleniumTestsContextManager.getThreadContext();
    }
}
