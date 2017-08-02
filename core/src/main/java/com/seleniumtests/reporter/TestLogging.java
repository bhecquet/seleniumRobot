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
        String formattedMessage = "<li><font color='#00cd00'>" + message + "</font></li>";
        log(formattedMessage, false, false);
        logMessage(message, MessageType.INFO);
        logger.info(message);
    }
    
    /**
     * Write warning to logger and current test step
     *
     * @param  message
     */
    public static void warning(String message) {
        String formattedMessage = "<li><font color='#FFFF00'>" + message + "</font></li>";
        log(formattedMessage, false, false);
        logMessage("Warning: " + message, MessageType.WARNING);
        logger.warn(message);
    }
    
    /**
     * Write error to logger and current test step
     *
     * @param  message
     */
    public static void error(String message) {
        String formattedMessage = "<li><b><font color='#6600CC'>" + message + "</font></b></li>";
        log(formattedMessage, false, false);
        logMessage(message, MessageType.ERROR);
        logger.error(message);
    }

    /**
     * Write log message to logger and current test step
     *
     * @param  message
     */
    public static void log(final String message) {
        log(message, false, false);
        logMessage(message, MessageType.LOG);
        logger.info(message);
    }
    
    
    private static void logMessage(final String message, final MessageType messageType) {
    	if (getParentTestStep() != null) {
    		getParentTestStep().addMessage(new TestMessage(message, messageType));
    	}
    }

    /**
     * @deprecated will be deleted when SeleniumTestReported will be removed
     * Log principal method (all others log methods use this one at the end)
     *
     * @param  message
     * @param  failed
     * @param  logToStandardOutput
     */
    @Deprecated
    private static void log(String message, final boolean failed, final boolean logToStandardOutput) {

    	String formattedMessage = message;
        if (formattedMessage == null) {
        	formattedMessage = "";
        }

        formattedMessage = formattedMessage.replaceAll("\\n", "<br/>");

        if (failed) {
        	formattedMessage = "<span style=\"font-weight:bold;color:#cc0052;\">" + formattedMessage + "</span>";
        }

        Reporter.log(escape(formattedMessage), logToStandardOutput);
    }

    /**
     * @deprecated will be deleted when SeleniumTestReported will be removed
     * @param message
     * @return
     */
    @Deprecated
    private static String escape(final String message) {
        return message.replaceAll("\\n", "<br/>").replaceAll("<", "@@lt@@").replaceAll(">", "^^greaterThan^^");
    }

    /**
     * Log Web Output (add "Output:" to the message)
     *
     * @param  url
     * @param  message
     * @param  failed
     */
    public static void logScreenshot(final ScreenShot screenshot, final boolean failed) {
    	String screenshotString = TestLogging.buildScreenshotLog(screenshot);
    	String message = screenshot.getTitle() + ": " + screenshotString;
        log(OUTPUT_PATTERN + message + "<br/>", failed, false);
        logMessage(OUTPUT_PATTERN + message, failed ? MessageType.ERROR: MessageType.LOG);
        
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
    	log("<li>" + (testStep.getFailed() ? "<b>FailedStep</b>: " : " ") + "<b>" + testStep.getName() + "</b>", testStep.getFailed(), false);
    	List<TestAction> actionList = testStep.getStepActions();
    	
    	if (!actionList.isEmpty()) {
    		log("<ul>", false, false);
	    	for (TestAction action: actionList) {
	    		if (action instanceof TestStep) {	
					logTestStep((TestStep)action, false);	
				} else {
					log("<li>" + (action.getFailed() ? "<b>FailedAction</b>: " : " ") + action.getName() + "</li>", action.getFailed(), false);
				}
			}
    		log("</ul>", false, false);
    	}
    	log("</li>", false, false);
    	
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
