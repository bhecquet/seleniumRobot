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
package com.seleniumtests.util.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumRobotLogger {
	
	private static final String LOG_PATTERN = " %-5p %d [%t] %C{1}: %m%n";

	private static final String FILE_APPENDER_NAME = "FileLogger";
	private static final Pattern LOG_FILE_PATTERN = Pattern.compile(".*?\\d \\[(.*?)\\](.*)");
	private static Map<String, String> testLogs = Collections.synchronizedMap(new HashMap<>());
	private static String outputDirectory;
	private static String defaultOutputDirectory;
	
	public static final String START_TEST_PATTERN = "Start method ";
	public static final String END_TEST_PATTERN = "Finish method ";
	public static final String METHOD_NAME = "methodName";
	public static final String LOG_FILE_NAME = "seleniumRobot.log";
	
	private SeleniumRobotLogger() {
		// As a utility class, it is not meant to be instantiated.
	}

	public static Logger getLogger(final Class<?> cls) {
	    boolean rootIsConfigured = Logger.getRootLogger().getAllAppenders().hasMoreElements();
	    if (!rootIsConfigured) {
	    	
	        BasicConfigurator.configure();
	        Logger rootLogger = Logger.getRootLogger();
	        rootLogger.setLevel(Level.INFO);
	
	        Appender appender = (Appender) rootLogger.getAllAppenders().nextElement();
	        appender.setLayout(new PatternLayout(SeleniumRobotLogger.LOG_PATTERN));
	    }
	
	    return Logger.getLogger(cls);
	}


	/**
	 * Update root logger so that logs are made available in a log file
	 * This code is delayed so that SeleniumTestsContext is initialized
	 * This is also not called for unit and integration tests
	 * 
	 */
	public static void updateLogger(String outputDir, String defaultOutputDir) {
		outputDirectory = outputDir;
		defaultOutputDirectory = defaultOutputDir;
		Appender fileLoggerAppender = Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
		if (fileLoggerAppender == null) {
			Logger rootLogger = Logger.getRootLogger();
			FileAppender fileAppender = new FileAppender();
			
			// clean output dir
			cleanResults();
			
			for (int i=0; i < 4; i++) {
				try {
					if (!new File(outputDir).exists()) {
						new File(outputDir).mkdirs();
					}
			        
			        
			        fileAppender.setName(FILE_APPENDER_NAME);
			        fileAppender.setFile(outputDir + "/" + SeleniumRobotLogger.LOG_FILE_NAME);
			        fileAppender.setLayout(new PatternLayout(LOG_PATTERN));
			        fileAppender.setThreshold(Level.INFO);
			        fileAppender.activateOptions();
			        break;
				} catch (Exception e) {
				}
			}
	        rootLogger.addAppender(fileAppender);
		}
	}
	
	/**
	 * Clean result directories
	 * Delete the directory that will be used to write these test results
	 * Delete also directories in "test-output" which are older than 300 minutes. Especially useful when test is requested to write result
	 * to a sub-directory of test-output with timestamp (for example). Without this mechanism, results would never be cleaned
	 */
	private static void cleanResults() {
		// clean output dir
    	try {
			FileUtils.deleteDirectory(new File(outputDirectory));
			WaitHelper.waitForSeconds(1);
		} catch (IOException e) {
			// do nothing
		}
    	
		new File(outputDirectory).mkdirs();
		WaitHelper.waitForSeconds(1);
    	
    	for (File directory: new File(defaultOutputDirectory).listFiles(file -> file.isDirectory())) {
    		try {
				if (Files.readAttributes(directory.toPath(), BasicFileAttributes.class).lastAccessTime().toInstant().atZone(ZoneOffset.UTC).toLocalTime()
						.isBefore(ZonedDateTime.now().minusMinutes(300).withZoneSameInstant(ZoneOffset.UTC).toLocalTime())) {
					FileUtils.deleteDirectory(directory);
				}
			} catch (IOException e) {
			}
    	
    	}
	}


	/**
	 * Parses log file and returns only lines of the current thread
	 * @return
	 * @throws IOException 
	 */
	public static void parseLogFile() {
		Appender fileLoggerAppender = Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
		if (fileLoggerAppender == null) {
			return;
		} 
		
		// read the file from appender directly
		List<String> logLines;
		try {
			logLines = FileUtils.readLines(new File(((FileAppender)fileLoggerAppender).getFile())); 
		} catch (IOException e) {
			getLogger(SeleniumRobotLogger.class).error("cannot read log file", e);
			return;
		}
			
		Map<String, String> testPerThread = new HashMap<>();
		
		for (String line: logLines) {
			Matcher matcher = SeleniumRobotLogger.LOG_FILE_PATTERN.matcher(line);
			if (matcher.matches()) {
				String thread = matcher.group(1);
				String content = matcher.group(2);
				
				if (content.contains(SeleniumRobotLogger.START_TEST_PATTERN)) {
					String testName = content.split(SeleniumRobotLogger.START_TEST_PATTERN)[1].trim();
					testPerThread.put(thread, testName);
					 
					// do not refresh content of logs in case test is retried
					if (!SeleniumRobotLogger.testLogs.containsKey(testName)) {
						SeleniumRobotLogger.testLogs.put(testName, "");
					}
				}
				if (testPerThread.get(thread) != null) {
					String testName = testPerThread.get(thread);
					SeleniumRobotLogger.testLogs.put(testName, SeleniumRobotLogger.testLogs.get(testName).concat(line + "\n"));
				}
			}
		}
	}
	
	public static void reset() {
		SeleniumRobotLogger.testLogs.clear();
		
		// clear log file
		Appender fileAppender = Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
		if (fileAppender != null) {
			fileAppender.close();
			Logger.getRootLogger().removeAppender(FILE_APPENDER_NAME);
			new File(outputDirectory + "/" + SeleniumRobotLogger.LOG_FILE_NAME).delete();
		}
	}


	public static Map<String, String> getTestLogs() {
		return testLogs;
	}



	public static void setOutputDirectory(String outputDirectory) {
		SeleniumRobotLogger.outputDirectory = outputDirectory;
	}


}
