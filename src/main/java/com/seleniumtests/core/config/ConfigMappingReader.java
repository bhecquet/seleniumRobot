/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.core.config;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.helper.IniHelper;


public class ConfigMappingReader {
	
	private static final Logger logger = TestLogging.getLogger(ConfigMappingReader.class);

	private static final String OBJECT_MAPPING_FILE_NAME = "objectMapping.ini";
	
	/**
	 * @author  Sophie
	 * @param page name of the caller page : will read the part [page] in the .ini
	 * @return the HashMap with all properties corresponding with the mobile using and the page
	 */
	public Map<String, String> readConfig(String page) {
		
		//Recup values in context, lowerCase because of the name of directories but can be changed...
		String mobile = SeleniumTestsContextManager.getThreadContext().getPlatform().toLowerCase();
		String version = SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion();
		
		return readConfig(mobile, version, page);
	}
	
	/**
	 * @author  Sophie
	 * @return the HashMap with all properties corresponding with the mobile using 
	 */
	public Map<String, HashMap<String, String>> readConfig() {
		
		//Recup values in context, lowerCase because of the name of directories but can be changed...
		String mobile = SeleniumTestsContextManager.getThreadContext().getPlatform().toLowerCase();
		String version = SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion();
		
		return readConfig(mobile, version); 
	}
	
	/**
	 * @author  Sophie
	 * @param type name of the directory representing the mobile type (android, ios, etc), can be empty
	 * @param version name of the directory representing the version (4.4, ios_6, etc), can be empty
	 * @param page name of the caller page : will read the part [page] in the .ini
	 * @return the HashMap with all properties corresponding with the mobile using and the page caller
	 */
	public Map<String, String> readConfig(String type, String version, String page){
		return readConfig(type, version).get(page);
	}
	
	/**
	 * @author  Sophie
	 * @param typeDir : name of the directory representing the mobile type (android, ios, etc), can be empty
	 * @param versionDir : name of the directory representing the version (4.4, ios_6, etc), can be empty
	 * @return the Map with all properties corresponding with the mobile using 
	 */
	public Map<String, HashMap<String,String>> readConfig(String systemType, String versionDir){
		
		//create HashMap for result
		Map<String, HashMap<String,String>> testConfig = new HashMap<>();

		// load generic configuration
		File globalConfigFile =  Paths.get(SeleniumTestsContextManager.getConfigPath(), OBJECT_MAPPING_FILE_NAME).toFile();
		testConfig = extractConfigValues(testConfig, globalConfigFile);
		
		// load system specific configuration if any (web / ios / android)
		if(systemType != null && !systemType.isEmpty()) {
			
			String typeDir;
			if (!"ios".equalsIgnoreCase(systemType) && !"android".equalsIgnoreCase(systemType)) {
				typeDir = "web";
			} else {
				typeDir = systemType.toLowerCase();
			}
			File systemConfigFile =  Paths.get(SeleniumTestsContextManager.getConfigPath(), typeDir, OBJECT_MAPPING_FILE_NAME).toFile();
			testConfig = extractConfigValues(testConfig, systemConfigFile);
			
			if(versionDir != null && !versionDir.isEmpty()){
				
				File versionConfigFile =  Paths.get(SeleniumTestsContextManager.getConfigPath(), typeDir, versionDir, OBJECT_MAPPING_FILE_NAME).toFile();
				testConfig = extractConfigValues(testConfig, versionConfigFile);
			}
		}
		
		return testConfig;
	}
	
	/**
	 * 
	 * @param testConfig
	 * @param file
	 * @return
	 */
	private Map<String, HashMap<String,String>> extractConfigValues(Map<String, HashMap<String,String>> testConfig, File file){
		Map<String, HashMap<String,String>> newTestConfig = new HashMap<>();
		try{
			newTestConfig = IniHelper.readIniFile(file, testConfig);
		}
		catch (ConfigurationException e){
			logger.debug("No such file : " + file);
			return testConfig;
		}
		return newTestConfig;
	}
	
		
}
