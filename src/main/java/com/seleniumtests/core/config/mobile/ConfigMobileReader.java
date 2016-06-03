package com.seleniumtests.core.config.mobile;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.IniHelper;


public class ConfigMobileReader {
	
	/**
	 * @author  Sophie
	 * @param page name of the caller page : will read the part [page] in the .ini
	 * @return the HashMap with all properties corresponding with the mobile using and the page
	 */
	public HashMap<String, String> readConfig(String page) {
		
		//Recup values in context, lowerCase because of the name of directories but can be to change...
		String mobile = SeleniumTestsContextManager.getThreadContext().getPlatform().toLowerCase();
		String version = SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion();
		
		return readConfig(mobile, version, page);
	}
	
	/**
	 * @author  Sophie
	 * @return the HashMap with all properties corresponding with the mobile using 
	 */
	public HashMap<String, HashMap<String, String>> readConfig() {
		
		//Recup values in context, lowerCase because of the name of directories but can be to change...
		String mobile = SeleniumTestsContextManager.getThreadContext().getPlatform().toLowerCase();
		String version = SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion();
		
		return readConfig(mobile, version); 
	}
	
	/**
	 * @author  Sophie
	 * @param mobileType name of the directory representing the mobile type (android, ios, etc), can be empty
	 * @param version name of the directory representing the version (4.4, ios_6, etc), can be empty
	 * @param page name of the caller page : will read the part [page] in the .ini
	 * @return the HashMap with all properties corresponding with the mobile using and the page caller
	 */
	public HashMap<String, String> readConfig(String mobileType, String version, String page){
		HashMap<String, String> res = new HashMap<String, String>();
		res = readConfig(mobileType, version).get(page);
		return res;
	}
	
	/**
	 * @author  Sophie
	 * @param mobileType name of the directory representing the mobile type (android, ios, etc), can be empty
	 * @param version name of the directory representing the version (4.4, ios_6, etc), can be empty
	 * @return the HashMap with all properties corresponding with the mobile using 
	 */
	public HashMap<String, HashMap<String,String>> readConfig(String mobileType, String version){
		//create HashMap for result
		HashMap<String, HashMap<String,String>> testConfig = new HashMap<String, HashMap<String,String>>();
		
		//create String for Paths
		String pathToLoad = Paths.get("mobile").toString();
		String pathToHerite =  "";
		String secondPathToHerite = "";
	
		//modify path of Files with the arguments 
		if(mobileType != null && !mobileType.equals("")){
			pathToHerite =  Paths.get(SeleniumTestsContext.CONFIG_PATH, "mobile", "objectMapping.ini").toString();
			pathToLoad = Paths.get(pathToLoad, mobileType).toString();
			
			if(version != null && !version.equals("")){
				secondPathToHerite =  Paths.get(SeleniumTestsContext.CONFIG_PATH, "mobile", mobileType, "objectMapping.ini").toString();
				pathToLoad = Paths.get(pathToLoad, version).toString();
			}
		}
		
			//Path of the target file
		pathToLoad = Paths.get(SeleniumTestsContext.CONFIG_PATH , pathToLoad , "objectMapping.ini").toString();
		
		//load targeted files from son to older parent
		File fileForIni = new File(pathToLoad);
		File fileForIniToHerite = new File(pathToHerite);
		File secondFileForIniToHerite = new File(secondPathToHerite);
	
			
		//load parents data
		if(mobileType != null &&  !mobileType.equals("")){
			//extract values from the common file for all types of mobile
			testConfig = IniHelper.readIniFile(fileForIniToHerite, testConfig);
			if(version != null && !version.equals("")){
				//extract values from the common file for this type of mobile (android or ios)
				 testConfig = IniHelper.readIniFile(secondFileForIniToHerite, testConfig);
			}
		}
			
		//extract values of past file
		testConfig = IniHelper.readIniFile(fileForIni, testConfig);
		return testConfig;
		
		}
	
		
}
