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
import org.apache.log4j.Logger;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.helper.IniHelper;


public class ConfigMappingReader {
	
	private static final Logger logger = TestLogging.getLogger(ConfigMappingReader.class);
	
	/**
	 * @author  Sophie
	 * @param page name of the caller page : will read the part [page] in the .ini
	 * @return the HashMap with all properties corresponding with the mobile using and the page
	 */
	public Map<String, String> readConfig(String page) {
		
		//Recup values in context, lowerCase because of the name of directories but can be to change...
		String mobile = SeleniumTestsContextManager.getThreadContext().getPlatform().toLowerCase();
		String version = SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion();
		
		return readConfig(mobile, version, page);
	}
	
	/**
	 * @author  Sophie
	 * @return the HashMap with all properties corresponding with the mobile using 
	 */
	public Map<String, HashMap<String, String>> readConfig() {
		
		//Recup values in context, lowerCase because of the name of directories but can be to change...
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
	public Map<String, HashMap<String,String>> readConfig(String typeDir, String versionDir){
		
		//create HashMap for result
		Map<String, HashMap<String,String>> testConfig = new HashMap<>();
		
		//create String for Paths
		String iniFilePath = "";		
		String typeFilePath =  "";
		String versionFilePath = "";
		
		if(typeDir != null && !"".equals(typeDir)){
			
			typeFilePath =  Paths.get(SeleniumTestsContext.getConfigPath(), "objectMapping.ini").toString();
			iniFilePath = Paths.get(iniFilePath, typeDir).toString();
			
			//load targeted files from parent
			File fileForIniToHerite = new File(typeFilePath);
			
			//load values from the common file for all types of mobile
			testConfig = extractConfigValues(testConfig, fileForIniToHerite);
			
			
			if(versionDir != null && !"".equals(versionDir)){
				
				versionFilePath =  Paths.get(SeleniumTestsContext.getConfigPath(), typeDir, "objectMapping.ini").toString();
				iniFilePath = Paths.get(iniFilePath, versionDir).toString();
				
				//load targeted files from son
				File secondFileForIniToHerite = new File(versionFilePath);
				
				//load values from the common file for this type of mobile (android or ios)
				testConfig = extractConfigValues(testConfig, secondFileForIniToHerite);
			}
		}
		
		//Path of the target file
		iniFilePath = Paths.get(SeleniumTestsContext.getConfigPath() , iniFilePath , "objectMapping.ini").toString();
		
		//load targeted files from older parent
		File fileForIni = new File(iniFilePath);
		
		//extract values of past file
		testConfig = extractConfigValues(testConfig, fileForIni);
		
		return testConfig;
	}
	
	/**
	 * 
	 * @param testConfig
	 * @param file
	 * @return
	 */
	private Map<String, HashMap<String,String>> extractConfigValues(
									Map<String, HashMap<String,String>> testConfig, File file){
		try{
			testConfig = IniHelper.readIniFile(file, testConfig);
		}
		catch (ConfigurationException e){
			logger.debug("No such file : " + file);
		}
		return testConfig;
	}
	
		
}
