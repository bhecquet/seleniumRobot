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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.google.gdata.util.common.html.HtmlToText;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;

/**
 * Log methods for test operations.
 */
public class TestLogging {

	private static Map<ITestResult, List<TestStep>> testsSteps = Collections.synchronizedMap(new HashMap<>());
	private static Map<String, String> testLogs = Collections.synchronizedMap(new HashMap<>());
	private static final String LOG_PATTERN = " %-5p %d [%t] %C{1}: %m%n";
	private static final String LOG_FILE_NAME = "seleniumRobot.log";
	private static final Pattern LOG_FILE_PATTERN = Pattern.compile(".*?\\d \\[(.*?)\\](.*)");
	
	public static final String START_TEST_PATTERN = "Start method ";
	public static final String END_TEST_PATTERN = "Finish method ";

    private TestLogging() {
		// As a utility class, it is not meant to be instantiated.
	}
    
    /**
     * error Logger.
     *
     * @param  message
     */
    public static void errorLogger(String message) {
        String formattedMessage = "<li><b><font color='#6600CC'>" + message + "</font></b></li>";
        log(formattedMessage, false, false);
    }
    
    /**
     * Update root logger so that logs are made available in a log file
     * This code is delayed so that SeleniumTestsContext is initialized
     * This is also not called for unit and integration tests
     */
    public static void updateLogger() {
    	Appender fileLoggerAppender = Logger.getRootLogger().getAppender("FileLogger");
    	if (fileLoggerAppender == null) {
    		Logger rootLogger = Logger.getRootLogger();
    		
    		// clean output dir
            String outputDir = SeleniumTestsContextManager.getGlobalContext().getOutputDirectory();
        	try {
				FileUtils.deleteDirectory(new File(outputDir));
				new File(outputDir).mkdirs();
			} catch (IOException e) {
				// do nothing
			}
            
            FileAppender fileAppender = new FileAppender();
            fileAppender.setName("FileLogger");
            fileAppender.setFile(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory() + "/" + LOG_FILE_NAME);
            fileAppender.setLayout(new PatternLayout(LOG_PATTERN));
            fileAppender.setThreshold(Level.INFO);
            fileAppender.activateOptions();
            rootLogger.addAppender(fileAppender);
    	}
    }
    
    public static Logger getLogger(final Class<?> cls) {
        boolean rootIsConfigured = Logger.getRootLogger().getAllAppenders().hasMoreElements();
        if (!rootIsConfigured) {
        	
            BasicConfigurator.configure();
            Logger rootLogger = Logger.getRootLogger();
            rootLogger.setLevel(Level.INFO);

            Appender appender = (Appender) rootLogger.getAllAppenders().nextElement();
            appender.setLayout(new PatternLayout(LOG_PATTERN));
        }

        return Logger.getLogger(cls);
    }
    
    /**
     * Parses log file and returns only lines of the current thread
     * @return
     * @throws IOException 
     */
    public static void parseLogFile() throws IOException {
    	List<String> logLines = FileUtils.readLines(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory() + "/" + LOG_FILE_NAME));
    	Map<String, String> testPerThread = new HashMap<>();
    	
    	for (String line: logLines) {
    		Matcher matcher = LOG_FILE_PATTERN.matcher(line);
    		if (matcher.matches()) {
    			String thread = matcher.group(1);
    			String content = matcher.group(2);
    			
    			if (content.contains(START_TEST_PATTERN)) {
    				String testName = content.split(START_TEST_PATTERN)[1].trim();
    				testPerThread.put(thread, testName);
    				testLogs.put(testName, "");
    			}
    			if (testPerThread.get(thread) != null) {
    				String testName = testPerThread.get(thread);
    				testLogs.put(testName, testLogs.get(testName).concat(content + "\n"));
    			}
    		}
    	}
    }
    

    // TODO: remove this method
    public static Map<String, Map<String, List<String>>> getPageListenerLog(final String pageListenerClassName) {
        return null;
    }

    // TODO: remove this method
    public static List<String> getPageListenerLogByMethodInstance(final ITestResult testResult) {
        return new ArrayList<>();
    }

    /**
     * Log info.
     *
     * @param  message
     */
    public static void logInfo(String message) {
        String formattedMessage = "<li><font color='#00cd00'>" + message + "</font></li>";
        log(formattedMessage, false, false);
    }

    /**
     * Log method.
     *
     * @param  message
     */
    public static void log(final String message) {
        log(message, false, false);
    }

    /**
     * Log.
     *
     * @param  message
     * @param  logToStandardOutput
     */
    public static void log(final String message, final boolean logToStandardOutput) {
        log(message, false, logToStandardOutput);
    }

    /**
     * Log principal method (all others log methods use this one at the end)
     *
     * @param  message
     * @param  failed
     * @param  logToStandardOutput
     */
    public static void log(String message, final boolean failed, final boolean logToStandardOutput) {

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

    public static String escape(final String message) {
        return message.replaceAll("\\n", "<br/>").replaceAll("<", "@@lt@@").replaceAll(">", "^^greaterThan^^");
    }

    public static String unEscape(String message) {
        String formattedMessage = message.replaceAll("<br/>", "\\n").replaceAll("@@lt@@", "<").replaceAll("\\^\\^gt\\^\\^", ">");

        formattedMessage = HtmlToText.htmlToPlainText(formattedMessage);
        return formattedMessage;
    }

    /**
     * Log Web Output (add "Output:" to the message)
     *
     * @param  url
     * @param  message
     * @param  failed
     */
    public static void logWebOutput(final String message, final boolean failed) {
        log("Output: " + message + "<br/>", failed, false);
    }

    /**
     * Log Web Step (add the message in the list of steps (with a number))
     *
     * @param  url
     * @param  message
     * @param  failed
     */
    public static void logWebStep(final String message, final boolean failed) {
        log("<li>" + (failed ? "<b>FailedStep</b>: " : " ") + message + "</li>", failed, false);
    }
    
    public static void logTestStep(TestStep testStep) {
    	log("<li>" + (testStep.getFailed() ? "<b>FailedStep</b>: " : " ") + "<b>" + testStep.getName() + "</b>", testStep.getFailed(), false);
    	List<TestAction> actionList = testStep.getStepActions();
    	
    	if (!actionList.isEmpty()) {
    		log("<ul>");
	    	for (TestAction action: actionList) {
	    		if (action instanceof TestStep) {	
					logTestStep((TestStep)action);	
				} else {
					log("<li>" + (action.getFailed() ? "<b>FailedAction</b>: " : " ") + action.getName() + "</li>", action.getFailed(), false);
				}
			}
    		log("</ul>");
    	}
    	log("</li>");
    	
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
                    + "' class='lightbox'>Application Snapshot</a>");
        }

        return sbMessage.toString();
    }

    /**
     * Log method.
     *
     * @param  message
     */
    public static void warning(String message) {
        String formattedMessage = "<li><font color='#FFFF00'>" + message + "</font></li>";
        log(formattedMessage, false, false);
    }

	public static Map<String, String> getTestLogs() {
		return testLogs;
	}
}
