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
	 * @param type name of the directory representing the mobile type (android, ios, etc), can be empty
	 * @param version name of the directory representing the version (4.4, ios_6, etc), can be empty
	 * @return the HashMap with all properties corresponding with the mobile using 
	 */
	public Map<String, HashMap<String,String>> readConfig(String type, String version){
		
		//create HashMap for result
		Map<String, HashMap<String,String>> testConfig = new HashMap<>();
		
		//create String for Paths
		String pathToLoad = "";
		String pathToHerite =  "";
		String secondPathToHerite = "";
	
		//modify path of Files with the arguments 
		if(type != null && !"".equals(type)){
			pathToHerite =  Paths.get(SeleniumTestsContext.getConfigPath(), "objectMapping.ini").toString();
			pathToLoad = Paths.get(pathToLoad, type).toString();
			
			if(version != null && !"".equals(version)){
				secondPathToHerite =  Paths.get(SeleniumTestsContext.getConfigPath(), type, "objectMapping.ini").toString();
				pathToLoad = Paths.get(pathToLoad, version).toString();
			}
		}
		
			//Path of the target file
		pathToLoad = Paths.get(SeleniumTestsContext.getConfigPath() , pathToLoad , "objectMapping.ini").toString();
		
		//load targeted files from son to older parent
		File fileForIni = new File(pathToLoad);
		File fileForIniToHerite = new File(pathToHerite);
		File secondFileForIniToHerite = new File(secondPathToHerite);
	
			
		//load parents data
		if(type != null &&  !"".equals(type)){
			//extract values from the common file for all types of mobile
			try{
				testConfig = IniHelper.readIniFile(fileForIniToHerite, testConfig);	
			}
			catch (ConfigurationException e){
				logger.debug("No such file : " + fileForIniToHerite);
			}

			if(version != null && !"".equals(version)){
				//extract values from the common file for this type of mobile (android or ios)
				try{
					testConfig = IniHelper.readIniFile(secondFileForIniToHerite, testConfig);
				}
				catch (ConfigurationException e){
					logger.debug("No such file : " + secondFileForIniToHerite);
				}
			}
		}
			
		//extract values of past file
		try{
			testConfig = IniHelper.readIniFile(fileForIni, testConfig);
		}
		catch (ConfigurationException e){
			logger.debug("No such file : " + fileForIni);
		}
		
		return testConfig;
	}
	
		
}
