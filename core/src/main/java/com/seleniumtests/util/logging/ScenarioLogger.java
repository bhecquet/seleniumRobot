package com.seleniumtests.util.logging;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.GenericFile;
import com.seleniumtests.reporter.logger.HarCapture;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.StringInfo;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestValue;

import net.lightbody.bmp.core.har.Har;

public class ScenarioLogger extends Logger {

	protected ScenarioLogger(String name) {
		super(name);
	}

	public static ScenarioLogger getScenarioLogger(final Class<?> cls) {
		SeleniumRobotLogger.getLogger(cls);
		return (ScenarioLogger) Logger.getLogger("." + cls.getName(), new ScenarioLoggerFactory());
	}
	
	@Override
	public void info(Object message) {
        logMessage(message.toString(), MessageType.INFO);
        super.info(message);
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
    @Override
    public void warn(Object message) {
    	logMessage("Warning: " + message.toString(), MessageType.WARNING);
    	super.warn(message);
    }
    
    /**
     * Write error to logger and current test step
     *
     * @param  message
     */
    @Override
    public void error(Object message) { 
        logMessage(message.toString(), MessageType.ERROR);
        super.error(message);
    } 

    /**
     * Write log message to logger and current test step
     *
     * @param  message
     */
    public void log(final String message) {
        logMessage(message, MessageType.LOG);
        super.info(message);
    }

    /**
     * Log a value associated to a message
     * @param id		an id referencing value
     * @param message	a human readable message
     * @param value		value of the message
     */
    public void logTestValue(String id, String message, String value) {

    	try {
	    	TestStep runningStep = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
	    	if (runningStep != null) {
	    		runningStep.addValue(new TestValue(id, message, value));
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
    }
    /**
     * Store a key / value pair in test, so that it can be added to reports at test level. Contrary to 'logTestValue' which is stored at test step level
     * @param key
     * @param value. A StringInfo object (either StringInfo or HyperlinkInfo)
     */
    public void logTestInfo(String key, StringInfo value) {
    	TestNGResultUtils.setTestInfo(Reporter.getCurrentTestResult(), key, value);
    	super.info(String.format("Storing into test result %s: %s", key, value.getInfo() ));
    }

    private void logMessage(final String message, final MessageType messageType) {
    	try {
	    	TestStep runningStep = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
	    	if (runningStep != null) {
	    		runningStep.addMessage(new TestMessage(message, messageType));
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
    }

    public void logNetworkCapture(Har har, String name) {
    	
    	try {
	    	TestStep runningStep = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
	    	if (runningStep != null) {
	    		try {
	    			runningStep.addNetworkCapture(new HarCapture(har, name));
				} catch (IOException e) {
					super.error("cannot create network capture file: " + e.getMessage(), e);
				} catch (NullPointerException e) {
					super.error("HAR capture is null");
				}
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
    	
    }
    
    public void logFile(File file, String description) {

    	try {
	    	TestStep runningStep = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
	    	if (runningStep != null) {
	    		runningStep.addFile(new GenericFile(file, description));
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
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
     * Log screenshot. Should not be directly used inside tests
     *
     * @param screenshot		screenshot to log
	 * @param screenshotName 	name of the snapshot, user wants to display
	 * @param driverName		the name of the driver that did the screenshot
     */
    public void logScreenshot(ScreenShot screenshot, String screenshotName, String driverName) {

    	try {
	    	TestStep runningStep = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
	    	if (runningStep != null) {
	    		try {
	    			runningStep.addSnapshot(new Snapshot(screenshot, driverName), 
	    					SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getTestSteps().size(),
	    					screenshotName);
	    		} catch (NullPointerException e) {
	    			super.error("screenshot is null");
	    		}
	    	}
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
    }
    
    public void logScreenshot(final ScreenShot screenshot) {
    	logScreenshot(screenshot, null, WebUIDriver.getCurrentWebUiDriverName());
    }

}
