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
package com.seleniumtests.core.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class ConfigReader {
	
	private static final String GLOBAL_SECTION_NAME = "General";
	private static final Logger logger = SeleniumRobotLogger.getLogger(ConfigReader.class);
	private static File configFile;
	
	/**
	 * Get config file path
	 * In case SeleniumTestsContextManager.generateApplicationPath has not already been called, return null as configFile won't be 
	 * valid
	 * 
	 * @return
	 */
	private static File getCurrentConfigFile() {
		String configPath = SeleniumTestsContextManager.getConfigPath();
		if (configPath == null) {
			return null;
		}
		if (new File(configPath + File.separator + "env.ini").isFile()) {
			return new File(configPath + File.separator + "env.ini");
		} else {
			return new File(configPath + File.separator + "config.ini");
		}
	}

	public static File getConfigFile() {
		if (configFile == null) {
			configFile = getCurrentConfigFile();
		}
		return configFile;
	}

	public Map<String, String> readConfig(InputStream iniFileStream) {
		return readConfig(iniFileStream, SeleniumTestsContextManager.getThreadContext().getTestEnv());
	}
	
	/**
	 * read configuration from default config file
	 * @return
	 */
	public Map<String, String> readConfig() {
		try (InputStream iniFileStream = FileUtils.openInputStream(getConfigFile());){
			return readConfig(iniFileStream, SeleniumTestsContextManager.getThreadContext().getTestEnv());
		} catch (NullPointerException e) {
			logger.warn("config file is null, check config path has been set using 'SeleniumTestsContextManager.generateApplicationPath()'");
			return new HashMap<>();
		} catch (IOException e1) {
			logger.warn("no valid config.ini file for this application");
			return new HashMap<>();
		}
	}
	
	public Map<String, String> readConfig(InputStream iniFileStream, String environment) {
		
		Map<String, String> testConfig = new HashMap<>();
	
		try {
			Ini ini = new Ini();
			Config conf = ini.getConfig();
			conf.setGlobalSection(true);
			conf.setGlobalSectionName(GLOBAL_SECTION_NAME);
			conf.setFileEncoding(Charset.forName("UTF-8"));
			ini.setConfig(conf);
			ini.load(iniFileStream);
			
			Set<Ini.Entry<String, Ini.Section>> sections = ini.entrySet();
			
			// first get global options
			testConfig = getGlobalOptions(testConfig, sections);
			
			// then overwrite global options with local ones
			testConfig = getLocalOptions(testConfig, sections, environment);
			
			return testConfig;
			
		} catch (InvalidFileFormatException e) {
			throw new ConfigurationException("Invalid file: " + iniFileStream);
		} catch (IOException e) {
			throw new ConfigurationException(String.format("File does not exist %s: %s", iniFileStream, e.getMessage()));
		}
	}
	
	/**
	 * 
	 * @param testConfig
	 * @param sections
	 * @return configuration with global options
	 */
	private Map<String, String> getGlobalOptions(Map<String, String> testConfig, 
													Set<Ini.Entry<String, Ini.Section>> sections){
		
		for (Ini.Entry<String, Ini.Section> section: sections) {
			
			if (section.getKey().equals(GLOBAL_SECTION_NAME)) {
				for (Ini.Entry<String, String> sectionOption: section.getValue().entrySet()) {
					testConfig.put(sectionOption.getKey(), sectionOption.getValue());
				}
			}
		}
		return testConfig;
	}
	
	/**
	 * 
	 * @param testConfig
	 * @param sections
	 * @param environment
	 * @return configuration with local options overriding previous options
	 */
	private Map<String, String> getLocalOptions(Map<String, String> testConfig, 
												Set<Ini.Entry<String, Ini.Section>> sections, String environment) {
		boolean envFound = false;
		for (Ini.Entry<String, Ini.Section> section: sections) {
			
			if (section.getKey().equals(environment)) {
				envFound = true;
				for (Ini.Entry<String, String> sectionOption: section.getValue().entrySet()) {
					testConfig.put(sectionOption.getKey(), sectionOption.getValue());
				}
			}
		}
		if (!envFound) {
			logger.warn(String.format("No section in env.ini/config.ini file matches the environment '%s'", environment));
		}
		return testConfig;
	}
}
