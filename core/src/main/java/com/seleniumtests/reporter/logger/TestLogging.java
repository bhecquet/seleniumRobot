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

import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
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

	private static Map<ITestResult, List<TestStep>> testsSteps = Collections.synchronizedMap(new HashMap<>());
	private static Map<Thread, TestStep> currentRootTestStep = Collections.synchronizedMap(new HashMap<>());
	private static Map<Thread, TestStep> parentTestStep = Collections.synchronizedMap(new HashMap<>());
	private static Map<Thread, ITestResult> currentTestResult = Collections.synchronizedMap(new HashMap<>());
	private static Logger logger = SeleniumRobotLogger.getLogger(TestLogging.class);
	
	private TestLogging() {
		// As a utility class, it is not meant to be instantiated.
	}
    
    /**
     * Write info to logger and current test step
     *
     * @param  message
     */
    public static void info(String message) {
        logMessage(message, MessageType.INFO);
        logger.info(message);
    }
    
    /**
     * Write warning to logger and current test step
     *
     * @param  message
     */
    public static void warning(String message) {
        logMessage("Warning: " + message, MessageType.WARNING);
        logger.warn(message);
    }
    
    /**
     * Write error to logger and current test step
     *
     * @param  message
     */
    public static void error(String message) { 
        logMessage(message, MessageType.ERROR);
        logger.error(message);
    } 

    /**
     * Write log message to logger and current test step
     *
     * @param  message
     */
    public static void log(final String message) {
        logMessage(message, MessageType.LOG);
        logger.info(message);
    }
    
    /**
     * Log a value associated to a message
     * @param id		an id referencing value
     * @param message	a human readable message
     * @param value		value of the message
     */
    public static void logTestValue(String id, String message, String value) {
    	if (getParentTestStep() != null) {
    		getParentTestStep().addValue(new TestValue(id, message, value));
    	}
    }
    
    // -------------------- Methods below should not be used directly inside test -----------------
    
    
    /**
     * Store a key / value pair in test, so that it can be added to reports at test level. Contrary to 'logTestValue' which is stored at test step level
     * @param key
     * @param value. A StringInfo object (either StringInfo or HyperlinkInfo)
     */
    public static void logTestInfo(String key, StringInfo value) {
    	TestNGResultUtils.setTestInfo(getCurrentTestResult(), key, value);
    	logger.info(String.format("Storing into test result %s: %s", key, value.getInfo() ));
    }
    
    /**
     * /!\ When iterating over test steps, it MUST be put in a synchronized block!! 
     * @return
     */
    public static Map<ITestResult, List<TestStep>> getTestsSteps() {
		return testsSteps;
	}
    
    private static void logMessage(final String message, final MessageType messageType) {
    	if (getParentTestStep() != null) {
    		getParentTestStep().addMessage(new TestMessage(message, messageType));
    	}
    }

    public static void logNetworkCapture(Har har, String name) {
    	if (getParentTestStep() != null) {
    		try {
				getParentTestStep().addNetworkCapture(new HarCapture(har, name));
			} catch (IOException e) {
				logger.error("cannot create network capture file: " + e.getMessage(), e);
			} catch (NullPointerException e) {
				logger.error("HAR capture is null");
			}
    	}
    	
    }
    
    public static void logFile(File file, String description) {
    	if (getParentTestStep() != null) {
    		getParentTestStep().addFile(new GenericFile(file, description));
    	}
    	
    }
 
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName name of the snapshot, user wants to display
     */
    public static void logScreenshot(ScreenShot screenshot, String screenshotName) {
    	logScreenshot(screenshot, screenshotName, WebUIDriver.getCurrentWebUiDriverName());
    }
    
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName 	name of the snapshot, user wants to display
	 * @param driverName		the name of the driver that did the screenshot
     */
    public static void logScreenshot(ScreenShot screenshot, String screenshotName, String driverName) {
    	if (getParentTestStep() != null) {
    		try {
    			getParentTestStep().addSnapshot(new Snapshot(screenshot, driverName), testsSteps.get(getCurrentTestResult()).size(), screenshotName);
    		} catch (NullPointerException e) {
    			logger.error("screenshot is null");
    		}
    	}
    }
    
    public static void logScreenshot(final ScreenShot screenshot) {
    	logScreenshot(screenshot, null, WebUIDriver.getCurrentWebUiDriverName());
    }
    
    public static void logTestStep(TestStep testStep) {
    	logTestStep(testStep, true);
    }
    
    
    
    /**
     * Logs the testStep for this test
     * Once logging is done, parentTestStep and currentRootTestStep are reset to avoid storing new data in them
     * @param testStep
     * @param storeStep
     */
    public static void logTestStep(TestStep testStep, boolean storeStep) {
    	List<TestAction> actionList = testStep.getStepActions();
    	
    	if (!actionList.isEmpty()) {
    		for (TestAction action: actionList) {
	    		if (action instanceof TestStep) {	
					logTestStep((TestStep)action, false);	
				} 
			}
    	}
    	
    	if (storeStep) {
    		TestLogging.testsSteps.get(getCurrentTestResult()).add(testStep);
    		TestLogging.setCurrentRootTestStep(null);
			TestLogging.setParentTestStep(null);
    	}
    }

	public static void setCurrentRootTestStep(TestStep testStep) {
		TestLogging.currentRootTestStep.put(Thread.currentThread(), testStep);
		TestLogging.setParentTestStep(TestLogging.getCurrentRootTestStep());
	}
	
	public static TestStep getCurrentRootTestStep() {
		return TestLogging.currentRootTestStep.get(Thread.currentThread());
	}
	
	public static void setParentTestStep(TestStep testStep) {
		TestLogging.parentTestStep.put(Thread.currentThread(), testStep);
	}
	
	public static TestStep getParentTestStep() {
		return TestLogging.parentTestStep.get(Thread.currentThread());
	}

	public static void setCurrentTestResult(ITestResult testResult) {
		TestLogging.currentTestResult.put(Thread.currentThread(), testResult);
		TestLogging.testsSteps.put(testResult, new ArrayList<>());
	}
	
	/**
	 * Returns the previous TestStep in the list or null if no step exists for this test
	 * @return
	 */
	public static TestStep getPreviousStep() {
		try {
			List<TestStep> allSteps = TestLogging.testsSteps.get(getCurrentTestResult());
			return allSteps.get(allSteps.size() - 1);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * For Integration tests only
	 */
	public static void resetCurrentTestResult() {
		TestLogging.currentTestResult.remove(Thread.currentThread());
	}
	
	public static ITestResult getCurrentTestResult() {
		if (TestLogging.currentTestResult.get(Thread.currentThread()) == null) {
			logger.warn("Reporter did not inform about the current test result, creating one");
			setCurrentTestResult(Reporter.getCurrentTestResult());
		} 
		
		return TestLogging.currentTestResult.get(Thread.currentThread());
	}
	
	/**
	 * For Integration tests only
	 */
	public static void reset() {
		TestLogging.currentRootTestStep.clear();
		TestLogging.testsSteps.clear();
		TestLogging.parentTestStep.clear();
		resetCurrentTestResult();
		
		try {
			SeleniumRobotLogger.reset();
		} catch (IOException e) {
			logger.error("Cannot delete log file", e);
		}
	}
	
}
