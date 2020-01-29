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

import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContext.TestStepManager;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
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
	private static ScenarioLogger logger = SeleniumRobotLogger.getScenarioLogger(TestLogging.class);
	
	private TestLogging() {
		// As a utility class, it is not meant to be instantiated.
	}
    
    /**
     * Write info to logger and current test step
     *
     * @param  message
     */
    public static void info(String message) {
        logger.info(message);
    }
    
    /**
     * Write warning to logger and current test step
     *
     * @param  message
     */
    public static void warning(String message) {
        logger.warn(message);
    }
    
    /**
     * Write error to logger and current test step
     *
     * @param  message
     */
    public static void error(String message) { 
        logger.error(message);
    } 

    /**
     * Write log message to logger and current test step
     *
     * @param  message
     */
    public static void log(final String message) {
        logger.log(message);
    }
    
    /**
     * Log a value associated to a message
     * @param id		an id referencing value
     * @param message	a human readable message
     * @param value		value of the message
     */
    public static void logTestValue(String id, String message, String value) {
    	logger.logTestValue(id, message, value);
    }
    
    // -------------------- Methods below should not be used directly inside test -----------------
    
    
    /**
     * Store a key / value pair in test, so that it can be added to reports at test level. Contrary to 'logTestValue' which is stored at test step level
     * @param key
     * @param value. A StringInfo object (either StringInfo or HyperlinkInfo)
     */
    public static void logTestInfo(String key, StringInfo value) {
    	logger.logTestInfo(key, value);
    }

    public static void logNetworkCapture(Har har, String name) {
    	logger.logNetworkCapture(har, name);
    	
    }
    
    public static void logFile(File file, String description) {
    	logger.logFile(file, description);
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
    	logger.logScreenshot(screenshot, screenshotName, driverName);
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
    		
    		// notify each TestStepManager about the new test step (useful for AfterClass / AfterTest configuration methods)
    		for (SeleniumTestsContext testContext: SeleniumTestsContextManager.getContextForCurrentTestState()) {
    			TestStepManager stepManager = testContext.getTestStepManager();
    	    	stepManager.getTestSteps().add(testStep);
    	    	stepManager.setRootTestStep(null);
    	    	stepManager.setRunningTestStep(null);
    		}
	    	
    	}
    	
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

	public static void setCurrentRootTestStep(TestStep testStep) {
		try {
			SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().setRootTestStep(testStep);
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
	}
	
	public static TestStep getCurrentRootTestStep() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRootTestStep();
    	} catch (IndexOutOfBoundsException e) {
    		// null, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}
	
	public static void setParentTestStep(TestStep testStep) {
		try {
			SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().setRunningTestStep(testStep);
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
	}
	
	public static TestStep getParentTestStep() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}

	public static void setCurrentTestResult(ITestResult testResult) {
		TestLogging.currentTestResult.put(Thread.currentThread(), testResult);
	}
	
	/**
	 * Returns the previous TestStep in the list or null if no step exists for this test
	 * @return
	 */
	public static TestStep getPreviousStep() {

		try {
			List<TestStep> allSteps = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getTestSteps();
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
		resetCurrentTestResult();
		
		try {
			SeleniumRobotLogger.reset();
		} catch (IOException e) {
			logger.error("Cannot delete log file", e);
		}
	}
	
}
