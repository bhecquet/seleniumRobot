/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.TestMessage.MessageType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Log methods for test operations.
 */
public class TestLogging {

	private static Map<ITestResult, List<TestStep>> testsSteps = Collections.synchronizedMap(new HashMap<>());
	private static Map<Thread, TestStep> currentRootTestStep = Collections.synchronizedMap(new HashMap<>());
	private static Map<Thread, TestStep> parentTestStep = Collections.synchronizedMap(new HashMap<>());
	private static Map<Thread, ITestResult> currentTestResult = Collections.synchronizedMap(new HashMap<>());
	private static Logger logger = SeleniumRobotLogger.getLogger(TestLogging.class);
	
	public static final String OUTPUT_PATTERN = "Output: ";
	public static final String SNAPSHOT_PATTERN = "Application Snapshot";
	
	private TestLogging() {
		// As a utility class, it is not meant to be instantiated.
	}
    
    public static Map<ITestResult, List<TestStep>> getTestsSteps() {
		return testsSteps;
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
    
    
    private static void logMessage(final String message, final MessageType messageType) {
    	if (getParentTestStep() != null) {
    		getParentTestStep().addMessage(new TestMessage(message, messageType));
    	}
    }

 
    /**
     * Log Web Output (add "Output:" to the message)
     *
     * @param  url
     * @param  message
     * @param  failed
     */
    public static void logScreenshot(final ScreenShot screenshot) {
    	String screenshotString = TestLogging.buildScreenshotLog(screenshot);
    	String message = screenshot.getTitle() + ": " + screenshotString;
        logMessage(OUTPUT_PATTERN + message, MessageType.SNAPSHOT);
        
        // store snapshot path to step
        if (getParentTestStep() != null) {
    		getParentTestStep().setSnapshot(screenshot.getImagePath());
    	}
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

    /**
     * Log Screenshot method
     * Return: screenshot message with links
     *
     * @param  screenShot
     * 
     * @return String
     */
    public static String buildScreenshotLog(final ScreenShot screenShot) {
        StringBuilder sbMessage = new StringBuilder("");
        if (screenShot.getLocation() != null) {
            sbMessage.append("<a href='" + screenShot.getLocation() + "' target=url>Application URL</a>");
        }

        if (screenShot.getHtmlSourcePath() != null) {
            sbMessage.append(" | <a href='" + screenShot.getHtmlSourcePath()
                    + "' target=html>Application HTML Source</a>");
        }

        if (screenShot.getImagePath() != null) {
            sbMessage.append(" | <a href='" + screenShot.getImagePath()
                    + "' class='lightbox'>" + SNAPSHOT_PATTERN + "</a>");
        }

        return sbMessage.toString();
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
	
	public static void reset() {
		TestLogging.currentRootTestStep.clear();
		TestLogging.testsSteps.clear();
		TestLogging.parentTestStep.clear();
		
		SeleniumRobotLogger.reset();
	}
	
}
