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
package com.seleniumtests.util.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumRobotLogger {
	
	private static final String LOG_PATTERN = " %-5p %d [%t] %C{1}: %m%n";

	private static final String FILE_APPENDER_NAME = "FileLogger";
	private static final Pattern LOG_FILE_PATTERN = Pattern.compile(".*?\\d \\[([^\\]]+)\\](.*)");
	private static Map<String, String> testLogs = Collections.synchronizedMap(new HashMap<>());
	private static String outputDirectory;
	private static String defaultOutputDirectory;
	
	public static final String START_TEST_PATTERN = "Start method ";
	public static final String END_TEST_PATTERN = "Finish method ";
	public static final String LOG_FILE_NAME = "seleniumRobot.log";
	public static final String INTERNAL_DEBUG = "internalDebug";
	public static final String MAVEN_EXECUTION = "mavenExecution";
	private static boolean rootIsConfigured = false;
	
	private SeleniumRobotLogger() {
		// As a utility class, it is not meant to be instantiated.
	}
	
	private static void configureLogger() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		AppenderComponentBuilder console = builder.newAppender("stdout", "Console"); 
		builder.add(console);
		
		LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
		layout.addAttribute("pattern", LOG_PATTERN);
		console.add(layout);
		
		RootLoggerComponentBuilder rootLogger;
		// use System property instead of SeleniumTestsContext class as SeleniumrobotLogger class is used for grid extension package and 
        // we do not want to depend on "SeleniumTestsContext" class here
		if (System.getProperty(INTERNAL_DEBUG) != null && System.getProperty(INTERNAL_DEBUG).contains("core")) {
			rootLogger = builder.newRootLogger(Level.DEBUG);
        } else {
        	rootLogger = builder.newRootLogger(Level.INFO);
        }
		rootLogger.add(builder.newAppenderRef("stdout"));

		builder.add(rootLogger);
		
		Configurator.initialize(builder.build());
	}
	
	public static Logger getLogger(final Class<?> cls) {
		
		
	    if (!rootIsConfigured) {
	    	configureLogger();

			if (System.getProperty(MAVEN_EXECUTION) == null || System.getProperty(MAVEN_EXECUTION).equals("false")) {
				System.out.println("streams redirected to logger");
		        // redirect standard output and error to logger so that all logs are written to log file
		        System.setErr(new PrintStream(new Sys.Error(LogManager.getLogger(cls)), true));
		        System.setOut(new PrintStream(new Sys.Out(LogManager.getLogger(cls)), true));
			}
	
	        rootIsConfigured = true;
	    }

	    return LogManager.getLogger(cls);
	}


	/**
	 * Update root logger so that logs are made available in a log file
	 * This code is delayed so that SeleniumTestsContext is initialized
	 * This is also not called for unit and integration tests
	 * 
	 */
	public static void updateLogger(String outputDir, String defaultOutputDir) {
		updateLogger(outputDir, defaultOutputDir, SeleniumRobotLogger.LOG_FILE_NAME);
	}
	
	public static void updateLogger(String outputDir, String defaultOutputDir, String logFileName) {
		updateLogger(outputDir, defaultOutputDir, logFileName, true);
	}
	
	public static void updateLogger(String outputDir, String defaultOutputDir, String logFileName, boolean doCleanResults) {
		outputDirectory = outputDir;
		defaultOutputDirectory = defaultOutputDir;
		Appender fileLoggerAppender = ((LoggerContext)LogManager.getContext()).getConfiguration().getAppender(FILE_APPENDER_NAME);
		if (fileLoggerAppender == null) {

			// clean output dir
			if (doCleanResults) {
				cleanResults();
			}
			
			for (int i=0; i < 4; i++) {
				try {
					if (!new File(outputDir).exists()) {
						new File(outputDir).mkdirs();
					}
					
					PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
					layoutBuilder.withPattern(LOG_PATTERN);
					
					FileAppender.Builder<?> fileAppenderBuilder = FileAppender.newBuilder()
							.withFileName(outputDir + "/" + logFileName);
					fileAppenderBuilder.setLayout(layoutBuilder.build());
					fileAppenderBuilder.setName(FILE_APPENDER_NAME);


			        if (System.getProperty(INTERNAL_DEBUG) != null && System.getProperty(INTERNAL_DEBUG).contains("core")) {
			        	((LoggerContext)LogManager.getContext()).getConfiguration().getRootLogger().addAppender(fileAppenderBuilder.build(), Level.DEBUG, null);
			        } else {
			        	((LoggerContext)LogManager.getContext()).getConfiguration().getRootLogger().addAppender(fileAppenderBuilder.build(), Level.INFO, null);
			        }
			        
			        break;
				} catch (Exception e) {
				}
			}

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
    	
		if (new File(defaultOutputDirectory).exists()) {
	    	for (File directory: new File(defaultOutputDirectory).listFiles(File::isDirectory)) {
	    		try {
					if (Files.readAttributes(directory.toPath(), BasicFileAttributes.class).lastAccessTime().toInstant().atZone(ZoneOffset.UTC).toLocalTime()
							.isBefore(ZonedDateTime.now().minusMinutes(300).withZoneSameInstant(ZoneOffset.UTC).toLocalTime())) {
						FileUtils.deleteDirectory(directory);
					}
				} catch (IOException e) {
				}
	    	
	    	}
		}
	}


	/**
	 * Parses log file and store logs of each test in testLogs variable
	 * @return
	 * @throws IOException 
	 */
	public static synchronized void parseLogFile() {
		
		Appender fileLoggerAppender = ((LoggerContext)LogManager.getContext()).getConfiguration().getAppender(FILE_APPENDER_NAME);
		if (fileLoggerAppender == null) {
			return;
		} 
		
		// read the file from appender directly
		List<String> logLines;
		try {
			logLines = FileUtils.readLines(new File(((FileAppender)fileLoggerAppender).getFileName()), StandardCharsets.UTF_8); 
		} catch (IOException e) {
			getLogger(SeleniumRobotLogger.class).error("cannot read log file", e);
			return;
		}
		
		// clean before reading file. correction of issue #100
		SeleniumRobotLogger.testLogs.clear();
			
		//store the name of the thread for each test
		Map<String, String> testPerThread = new HashMap<>();
		String previousThread = null; // will store the thread associated to the previously read line
		
		for (String line: logLines) {
			Matcher matcher = SeleniumRobotLogger.LOG_FILE_PATTERN.matcher(line);
			if (matcher.matches()) {
				String thread = matcher.group(1);
				String content = matcher.group(2);
				previousThread = thread;
				
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
			} else if (previousThread != null && testPerThread.get(previousThread) != null) {
				String testName = testPerThread.get(previousThread);
				SeleniumRobotLogger.testLogs.put(testName, SeleniumRobotLogger.testLogs.get(testName).concat(line + "\n"));
			}
		}
	}
	
	public static void reset() throws IOException {
		SeleniumRobotLogger.testLogs.clear();
		
		// clear log file
		Appender fileLoggerAppender = ((LoggerContext)LogManager.getContext()).getConfiguration().getAppender(FILE_APPENDER_NAME);
		if (fileLoggerAppender != null) {
			fileLoggerAppender.stop();
			
			// wait for handler to be closed
			WaitHelper.waitForMilliSeconds(200);
			((LoggerContext)LogManager.getContext()).getConfiguration().getRootLogger().removeAppender(FILE_APPENDER_NAME);
			((LoggerContext)LogManager.getContext()).updateLoggers();
		}
		Path logFilePath = Paths.get(outputDirectory, SeleniumRobotLogger.LOG_FILE_NAME).toAbsolutePath();
		if (logFilePath.toFile().exists()) {
			Files.delete(logFilePath);
		}
	}


	public static Map<String, String> getTestLogs() {
		return testLogs;
	}



	public static void setOutputDirectory(String outputDirectory) {
		SeleniumRobotLogger.outputDirectory = outputDirectory;
	}


}
