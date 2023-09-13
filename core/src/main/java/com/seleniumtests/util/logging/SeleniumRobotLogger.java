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

import com.seleniumtests.util.helper.WaitHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

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
import java.util.Map;
import java.util.regex.Pattern;

public class SeleniumRobotLogger {
	
	private static final String LOG_PATTERN = " %-5p %d [%t] %C{1}: %m%n";

	private static final String FILE_APPENDER_NAME = "FileLogger";
	private static final Pattern LOG_FILE_PATTERN = Pattern.compile(".*?\\d \\[([^\\]]+)\\](.*)");
	private static Map<String, File> testLogs = Collections.synchronizedMap(new HashMap<>());
	private static ThreadLocal<String> loggerNames = new ThreadLocal();
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
	
	public static void createLoggerForTest(String outputDir, String loggerName) {

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		Level level = (System.getProperty(INTERNAL_DEBUG) != null && System.getProperty(INTERNAL_DEBUG).contains("core")) ? Level.DEBUG: Level.INFO;
		
		if (loggerNames.get() != null) {
			String previousName = loggerNames.get();
			Appender appender = ((BuiltConfiguration) config).getLogger(previousName).getAppenders().get(FILE_APPENDER_NAME + "-" + previousName);
			if (appender != null) {
				appender.stop();
				((BuiltConfiguration) config).getLogger(previousName).removeAppender(FILE_APPENDER_NAME + "-" + previousName);
			}
		}
		
		LoggerConfig loggerConfig = new LoggerConfig(loggerName, level, false);
		PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
		layoutBuilder.withPattern(LOG_PATTERN);
		PatternLayout patternLayout = layoutBuilder.build();
		
		File logFile = new File(outputDir + "/execution.log");
		FileAppender.Builder<?> fileAppenderBuilder = FileAppender.newBuilder()
				.withFileName(logFile.getAbsolutePath());
		fileAppenderBuilder.setLayout(patternLayout);
		fileAppenderBuilder.setName(FILE_APPENDER_NAME + "-" + loggerName);
		
		Appender fileAppender = fileAppenderBuilder.build();
		fileAppender.start();

		loggerConfig.addAppender(fileAppender, level, null);
		loggerConfig.addFilter(RepeatFilter.createFilter(Level.ERROR, true, Filter.Result.ACCEPT, Filter.Result.DENY));
		
		config.addLogger(loggerName, loggerConfig);
		
		ctx.updateLoggers();
		loggerNames.set(loggerName);
		testLogs.put(loggerName, logFile);
	}
	
	public static Logger getLoggerForTest() {
		if (loggerNames.get() == null) {
			return null;
		}
		return LogManager.getLogger(loggerNames.get());
	}
	
	private static void configureLogger() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder()
				.setPackages("com.seleniumtests.util.logging"); // to be able to load custom filters
		AppenderComponentBuilder consoleAppenderBuilder  = builder.newAppender("stdout", "CONSOLE"); 
		
		LayoutComponentBuilder layout = builder
				.newLayout("PatternLayout")
				.addAttribute("pattern", LOG_PATTERN);
		consoleAppenderBuilder.add(layout);
		
		consoleAppenderBuilder.add(builder
				.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
			    .addAttribute("marker", "FLOW"));

		
		builder.add(consoleAppenderBuilder);
		

		
		RootLoggerComponentBuilder rootLogger;
		// use System property instead of SeleniumTestsContext class as SeleniumrobotLogger class is used for grid extension package and 
        // we do not want to depend on "SeleniumTestsContext" class here
		if (System.getProperty(INTERNAL_DEBUG) != null && System.getProperty(INTERNAL_DEBUG).contains("core")) {
			rootLogger = builder.newRootLogger(Level.DEBUG);
        } else {
        	rootLogger = builder.newRootLogger(Level.INFO);
        }
		rootLogger.add(builder.newAppenderRef("stdout"));
		rootLogger.add(builder.newFilter("RepeatFilter", Filter.Result.ACCEPT, Filter.Result.DENY));

		
		builder.add(rootLogger);
//		Uncomment to debug configuration
//		try {
//			builder.writeXmlConfiguration(System.out);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Configurator.reconfigure(builder.build());
		
		
	}
	
	public static Logger getLogger(final Class<?> cls) {
		
		
	    if (!rootIsConfigured) {
	    	configureLogger();

			if (System.getProperty(MAVEN_EXECUTION) == null || System.getProperty(MAVEN_EXECUTION).equals("false")) {
				System.out.println("streams redirected to logger");
		        // redirect standard output and error to logger so that all logs are written to log file
		        System.setErr(new PrintStream(new Sys.Error(LogManager.getRootLogger()), true));
		        System.setOut(new PrintStream(new Sys.Out(LogManager.getRootLogger()), true));
			}
	
	        rootIsConfigured = true;
	    }

	    return new LoggerWrapper(LogManager.getLogger(cls));
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
		Appender fileLoggerAppender = isFileAppenderPresent();
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
					 
			        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			        final Configuration config = ctx.getConfiguration();

					PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
					layoutBuilder.withPattern(LOG_PATTERN);
					PatternLayout patternLayout = layoutBuilder.build();
					
					FileAppender.Builder<?> fileAppenderBuilder = FileAppender.newBuilder()
							.withFileName(outputDir + "/" + logFileName);
					fileAppenderBuilder.setLayout(patternLayout);
					fileAppenderBuilder.setName(FILE_APPENDER_NAME);
					
					Appender fileAppender = fileAppenderBuilder.build();
					fileAppender.start();

			        ctx.getRootLogger().addAppender(fileAppender);

			        if (System.getProperty(INTERNAL_DEBUG) != null && System.getProperty(INTERNAL_DEBUG).contains("core")) {
			        	config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
			        } else {
			        	config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.INFO);
			        }
			        ctx.updateLoggers();
			        
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
	 * Remove all execution files as they are not needed anymore, data written to log files
	 */
	public static void cleanTestLogs() {
		
		// stop all file loggers
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		for (LoggerConfig loggerConfig: config.getLoggers().values()) {
			for (Appender appender: loggerConfig.getAppenders().values()) {
				appender.stop();
			}
		}

		for (File file: FileUtils.listFiles(new File(defaultOutputDirectory),
				FileFilterUtils.nameFileFilter("execution.log"),
				TrueFileFilter.INSTANCE )) {
			file.delete();
		}
		
	}
	
	public static void reset() throws IOException {
		SeleniumRobotLogger.testLogs.clear();
		
		// clear log file
		Appender fileLoggerAppender = isFileAppenderPresent();
		if (fileLoggerAppender != null) {
			fileLoggerAppender.stop();
			
			// wait for handler to be closed
			WaitHelper.waitForMilliSeconds(200);
			((LoggerContext)LogManager.getContext(false)).getConfiguration().getRootLogger().removeAppender(FILE_APPENDER_NAME);
			((LoggerContext)LogManager.getContext(false)).updateLoggers();

		}
		Path logFilePath = Paths.get(outputDirectory, SeleniumRobotLogger.LOG_FILE_NAME).toAbsolutePath();
		if (logFilePath.toFile().exists()) {
			Files.delete(logFilePath);
		}
	}
	
	private static  Appender isFileAppenderPresent() {
		return ((LoggerContext)LogManager.getContext(false)).getConfiguration().getRootLogger().getAppenders().get(FILE_APPENDER_NAME);
	}
	
	/**
	 * Returns the file containing logs for requested test name (unique test name)
	 * or else, null
	 * @param testName
	 * @return
	 */
	public static String getTestLogs(String testName) {
		File logFile = testLogs.get(testName);
		if (logFile == null) {
			return "";
		}
		
		try {
			return FileUtils.readFileToString(logFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return "";
		}
	}



	public static void setOutputDirectory(String outputDirectory) {
		SeleniumRobotLogger.outputDirectory = outputDirectory;
	}


}
