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
package com.seleniumtests.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.ITestResult;
import org.testng.Reporter;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import net.lightbody.bmp.core.har.Har;

/**
 * Log methods for test operations.
 * This allow to write some data in reports (message, warn, error, test values, ...). This is accessible to tests
 * 
 * This class is also responsible for managing test steps, actions, snapshots, ...
 * We log like this
 * step1 (TestStep) => a root
 * 	  +--- action1 (TestAction)
 *    +--+ sub-step1 (TestStep)
 *       +--- sub-action1
 *       +--- message (TestMessage)
 *       +--- sub-action2
 *    +--- action2
 * step2 (TestStep) => a root
 * 	  +--- action3 (TestAction)
 * 
 * When logging, we first create a root step (stored in 'currentRootTestStep' variable which will store all sub-steps
 * To know where logging is in the tree (from example above, are we currently in step1 or sub-step1), we record parent step 'parentTestStep'
 * A root step is also a parent step, but inverse is false.
 * 
 * 'currentTestResult' helps storing the test being executed in this thread
 * 'testSteps' records all root steps in the test being executed
 */
public class TestLogging {
	
	private static Map<Thread, ITestResult> currentTestResult = Collections.synchronizedMap(new HashMap<>());
	private static ScenarioLogger logger = ScenarioLogger.getScenarioLogger(TestLogging.class);
	
	private TestLogging() {
		// As a utility class, it is not meant to be instantiated.
	}
    
    /**
     * Write info to logger and current test step
     *
     * @param  message
     * 
     * @deprecated use logger.
     */
	@Deprecated
    public static void info(String message) {
        logger.info(message);
    }
    
    /**
     * Write warning to logger and current test step
     *
     * @param  message
     * @deprecated use logger.
     */
	@Deprecated
    public static void warning(String message) {
        logger.warn(message);
    }
    
    /**
     * Write error to logger and current test step
     *
     * @param  message
     * @deprecated use logger.
     */
	@Deprecated
    public static void error(String message) { 
        logger.error(message);
    } 

    /**
     * Write log message to logger and current test step
     *
     * @deprecated use logger.
     */
	@Deprecated
    public static void log(final String message) {
        logger.log(message);
    }
    
    /**
     * Log a value associated to a message
     * @param id		an id referencing value
     * @param message	a human readable message
     * @param value		value of the message
     * @deprecated use logger.
     */
	@Deprecated
    public static void logTestValue(String id, String message, String value) {
    	logger.logTestValue(id, message, value);
    }
    
    // -------------------- Methods below should not be used directly inside test -----------------
    
    
    /**
     * Store a key / value pair in test, so that it can be added to reports at test level. Contrary to 'logTestValue' which is stored at test step level
     * @param key
     * @param value. A StringInfo object (either StringInfo or HyperlinkInfo)
     * @deprecated use logger.
     */
	@Deprecated
    public static void logTestInfo(String key, StringInfo value) {
    	logger.logTestInfo(key, value);
    }

	/**
	 * 
	 * @param har
	 * @param name
	 * @deprecated use logger.
     */
	@Deprecated
    public static void logNetworkCapture(Har har, String name) {
    	logger.logNetworkCapture(har, name);
    	
    }
    
	/**
	 * 
	 * @param file
	 * @param description
	 * @deprecated use logger.
     */
	@Deprecated
    public static void logFile(File file, String description) {
    	logger.logFile(file, description);
    }
 
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName name of the snapshot, user wants to display
     * @deprecated use logger.
     */
	@Deprecated
    public static void logScreenshot(ScreenShot screenshot, String screenshotName) {
    	logScreenshot(screenshot, screenshotName, WebUIDriver.getCurrentWebUiDriverName());
    }
    
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName 	name of the snapshot, user wants to display
	 * @param driverName		the name of the driver that did the screenshot
     * @deprecated use logger.
     */
	@Deprecated
    public static void logScreenshot(ScreenShot screenshot, String screenshotName, String driverName) {
    	logger.logScreenshot(screenshot, screenshotName, driverName);
    }
    
	/**
	 * 
	 * @param screenshot
	 * @deprecated use logger.
     */
	@Deprecated
    public static void logScreenshot(final ScreenShot screenshot) {
    	logScreenshot(screenshot, null, WebUIDriver.getCurrentWebUiDriverName());
    }
    
	/**
	 * 
	 * @param testStep
	 * @deprecated use logger.
     */
	@Deprecated
    public static void logTestStep(TestStep testStep) {
    	TestStepManager.logTestStep(testStep, true);
    }
    
    
    @Deprecated
    public static Map<ITestResult, List<TestStep>> getTestsSteps() {
    	Map<ITestResult, List<TestStep>> steps = new HashMap<>();

		Map<ITestResult, SeleniumTestsContext> testResultContext = SeleniumTestsContextManager.getTestResultContext();
		synchronized (testResultContext) {
			for (Entry<ITestResult, SeleniumTestsContext> entry: testResultContext.entrySet()) {
				steps.put(entry.getKey(), new ArrayList<TestStep>(entry.getValue().getTestStepManager().getTestSteps())); // copy to avoid problems with concurrent access
			}
		}
    	
    	
    	return steps;
    }
	
}
