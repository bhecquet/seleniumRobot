package com.seleniumtests.util.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.seleniumtests.reporter.logger.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.info.Info;
import com.seleniumtests.reporter.logger.GenericFile;
import com.seleniumtests.reporter.logger.HarCapture;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestValue;
import com.seleniumtests.uipage.PageObject;

//import net.lightbody.bmp.core.har.Har;

public class ScenarioLogger {
	
	private Logger logger;
	
	private ScenarioLogger(Logger logger) {
		this.logger = logger;
	}

	public static ScenarioLogger getScenarioLogger(final Class<?> cls) {
		Logger logger = SeleniumRobotLogger.getLogger(cls);
		return new ScenarioLogger(logger);
	}
	
	private String cleanMessage(Object message) {
		if (message == null) {
			return "null";
		} else {
			return message.toString();
		}
	}

	public void info(Object message) {
        logMessage(cleanMessage(message), MessageType.INFO);
        logger.info(message);
    }
    
    /**
     * Kept for compatibility with TestLogging
     * @param message
     */
    public void warning(String message) {
        warn(message);
    }
    
    /**
     * Write warning to logger and current test step
     *
     * @param  message
     */
    public void warn(Object message) {
    	logMessage("Warning: " + cleanMessage(message), MessageType.WARNING);
    	logger.warn(message);
    }
    
    /**
     * Write error to logger and current test step
     *
     * @param  message
     */
    public void error(Object message) { 
        logMessage(cleanMessage(message), MessageType.ERROR);
        logger.error(message);
    } 
    public void error(Object message, Exception e) { 
    	logMessage(cleanMessage(message), MessageType.ERROR);
    	logger.error(message, e);
    } 

    /**
     * Write log message to logger and current test step
     *
     * @param  message
     */
    public void log(final String message) {
        logMessage(message, MessageType.LOG);
        logger.info(message);
    }

    /**
     * Log a value associated to a message
     * @param id		an id referencing value
     * @param message	a human readable message
     * @param value		value of the message
     */
    public void logTestValue(String id, String message, String value) {


    	TestStep runningStep = TestStepManager.getParentTestStep();
    	if (runningStep == null) {
    		runningStep = TestStepManager.getCurrentOrPreviousStep();
    	}
    	
    	if (runningStep != null) {
    		runningStep.addValue(new TestValue(id, message, value));
    	}

    }
    /**
     * Store a key / value pair in test, so that it can be added to reports at test level. Contrary to 'logTestValue' which is stored at test step level
     * @param key
     * @param value. A StringInfo object (either StringInfo or HyperlinkInfo)
     */
    public void logTestInfo(String key, Info value) {
    	logTestInfo(key, value, Reporter.getCurrentTestResult());
    }
    public void logTestInfo(String key, Info value, ITestResult testResult) {
    	TestNGResultUtils.setTestInfo(testResult, key, value);
    	logger.info(String.format("Storing into test result %s: %s", key, value.getInfo()));
    }

    /**
     * Log to current step (root or sub-step)
     * if none found, get the current root step or the previous root step
     * @param message
     * @param messageType
     */
    private void logMessage(final String message, final MessageType messageType) {

    	TestStep runningStep = TestStepManager.getParentTestStep();
    	if (runningStep == null) {
    		runningStep = TestStepManager.getCurrentOrPreviousStep();
    	}
    	
    	if (runningStep != null) {
    		runningStep.addMessage(new TestMessage(message, messageType));
    	} 

    }


    public void logNetworkCapture(Har har, String name) {
    	

    	TestStep runningStep = TestStepManager.getParentTestStep();
    	if (runningStep == null) {
    		runningStep = TestStepManager.getCurrentOrPreviousStep();
    	}
    	if (runningStep != null) {
    		try {
    			runningStep.addNetworkCapture(new HarCapture(har, name));
			} catch (IOException e) {
				logger.error("cannot create network capture file: " + e.getMessage(), e);
			} catch (NullPointerException e) {
				logger.error("HAR capture is null");
			}
    	}
    	
    }
    

    /**
     * Log the given file to HTML result, file will be moved to test-output root folder
     * @param file			file to log
     * @param description	a comment for the file
     */
    public void logFile(File file, String description) {
    	logFile(file, description, true);
    }
     
    /**
     * Log the given file to HTML result
     * @param file			file to log
     * @param description	a comment for the file
     * @param move			if true, the file will be moved to log folder. Useful when the input file is outside the selenium result tree
     */
    public void logFile(File file, String description, boolean move) {

    	try {
	    	TestStep runningStep = TestStepManager.getParentTestStep();
	    	if (runningStep == null) {
	    		runningStep = TestStepManager.getCurrentOrPreviousStep();
	    	}
	    	if (runningStep != null) {
	    		runningStep.addFile(new GenericFile(file, description, move));
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	} catch (IOException e) {
			error(e.getMessage());
    	}
    }
    
    /**
     * Log the file into the "Test end" step
     * @param file
     * @param description
     */
    public FileContent logFileToTestEnd(File file, String description) {
    	
    	try {
    		TestStep runningStep = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getLastTestStep();
    		if (runningStep != null) {
				GenericFile genericFile = new GenericFile(file, description);
    			runningStep.addFile(genericFile);
				return genericFile.getFileContent();
    		}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	} catch (IOException e) {
			error(e.getMessage());
		}
		return null;
    }
 
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName name of the snapshot, user wants to display
     */
    public void logScreenshot(ScreenShot screenshot, String screenshotName) {
    	logScreenshot(screenshot, screenshotName, WebUIDriver.getCurrentWebUiDriverName());
    }
    
    /**
     * Log screenshot by requesting check
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName name of the snapshot, user wants to display
	 * @param checkSnapshot		If true, check if we should compare snapshot on selenium server
     */
    public void logScreenshot(ScreenShot screenshot, String screenshotName, SnapshotCheckType checkSnapshot) {
    	logScreenshot(screenshot, screenshotName, WebUIDriver.getCurrentWebUiDriverName(), checkSnapshot);
    }
    
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName 	name of the snapshot, user wants to display
	 * @param driverName		the name of the driver that did the screenshot
     */
    public void logScreenshot(ScreenShot screenshot, String screenshotName, String driverName) {
    	logScreenshot(screenshot, screenshotName, driverName, SnapshotCheckType.FALSE);
    }
    
    /**
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName 	name of the snapshot, user wants to display
	 * @param driverName		the name of the driver that did the screenshot
	 * @param checkSnapshot		If true, check if we should compare snapshot on selenium server
     */
    public void logScreenshot(ScreenShot screenshot, String screenshotName, String driverName, SnapshotCheckType checkSnapshot) {

    	try {
	    	TestStep runningStep = TestStepManager.getParentTestStep();
	    	if (runningStep == null) {
	    		runningStep = TestStepManager.getCurrentOrPreviousStep();
	    	}
	    	
	    	if (runningStep != null) {
	    		try {
	    			String displayedScreenshotName = screenshotName == null ? "": "-" + screenshotName;
	    			runningStep.addSnapshot(new Snapshot(screenshot, String.format("drv:%s%s", driverName, displayedScreenshotName), checkSnapshot), 
	    					SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getTestSteps().size(),
	    					screenshotName);
	    		} catch (FileNotFoundException e) {
	    			logger.error("screenshot is null");
	    		}
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
    }
    
    public void logScreenshot(final ScreenShot screenshot) {
    	logScreenshot(screenshot, null, WebUIDriver.getCurrentWebUiDriverName());
    }
    
    /**
     * 
     */

	/**
	 * Method for logging error on actions (e.g: a click fails)
	 * It logs some lines of the stack to know where problem occured more precisely
	 * even if the error is then catched. 
	 * @param throwable
	 */
	public void logActionError(Throwable throwable) {
		if (throwable != null) {
			StackTraceElement[] s1 = throwable.getStackTrace();
			
			StringBuilder string = new StringBuilder(throwable.getMessage() == null ? "null": throwable.getMessage());
			for (int x = 0; x < s1.length; x++) {
				try {
					if (PageObject.class.isAssignableFrom(Class.forName(s1[x].getClassName()))) {
						string.append("\nat " + s1[x].toString());
					}
				} catch (ClassNotFoundException e) {
				}
			}
			warn(string);
		}
	}
	

}
