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
package com.seleniumtests.util.osutility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Facade of the OS Utility Windows or Unix
 */
public abstract class OSUtility {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(OSUtility.class);
	
	protected static final String LATEST_VERSION = "latest";
	
	private String[] webBrowserProcessList = {"chrome", "firefox", "iexplore", "safari"};

	private String[] webDriverProcessList = {"chromedriver", "geckodriver", "iedriverserver", "microsoftwebdriver"};
		
	private static Map<BrowserType, List<BrowserInfo>> installedBrowsersWithVersion;
	
	/******************************************
	 *********** OS information ***************
	 ******************************************/
	
	public static Platform getCurrentPlatorm() {
		if (isWindows()) {
			return Platform.WINDOWS;
		} else if (isLinux()) {
			return Platform.LINUX;
		} else if (isMac()) {
			return Platform.MAC;
		} else {
			throw new ConfigurationException(getOSName() + " is not recognized as a valid platform");
		}
	}
	
	/**
	 * @return the name of the Operating System
	 */
    public static String getOSName() {
        return System.getProperty("os.name");
    }
    
    /**
     * @return true if the OS is Windows
     */
    public static boolean isWindows() {
        return getOSName().startsWith("Win");
    }
    

    /**
     * @return true if the OS is Windows
     */
    public static boolean isLinux() {
        return getOSName().startsWith("Linux");
    }
    
    /**
     * @return true if the Operating System is MAC OS X
     */
    public static boolean isMac() {
        return getOSName().startsWith("Mac");
    }
    
    /**
     * @return operating system JVM architecture
     */
    public static String getArchitecture() {
        return System.getProperty("os.arch");
    }
    
    /**
     * 
     * @return the quantity of bits of the processor
     */
    public static String getOSBits() {
        return System.getProperty("sun.arch.data.model");
    }
    
	/******************************************
	 ********** Process information ***********
	 ******************************************/
    
    /**
     * Returns list of all running processes
     * @return
     */
    public abstract List<ProcessInfo> getRunningProcessList();
    
    /**
     * get sub process of parent one
     * @param parentProcess		the parent process
     * @param processName		the process name to search. May be null if any child should be matched
     * @param existingPids		reply will not contain these PIDs
     * @return
     * @throws IOException
     */
    public abstract List<Long> getChildProcessPid(Long parentProcess, String processName, List<Long> existingPids) throws IOException;
    
    /**
     * @param name of the process
     * @return ProcessInfo
     */
    public ProcessInfo getRunningProcess(String processName) {
    	try {
    		return getRunningProcesses(processName).get(0);	
    	} catch (IndexOutOfBoundsException e) {
    		return null;
    	}
    }
    
    /**
     * @param name of the process
     * @return ProcessInfo
     */
    public List<ProcessInfo> getRunningProcesses(String processName) {
    	
    	List<ProcessInfo> processes = new ArrayList<>();
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		if (processInfo.getName().equalsIgnoreCase(processName)){
    			processes.add(processInfo);
    		}
    	}
    	return processes;
    }
    
    /**
     * @param processName : process name
     * @return true if the key is found in the running process list.
     */
    public boolean isProcessRunning(String processName) {
    	
    	ProcessInfo processInfo = getRunningProcess(processName);
		return (processInfo != null && processInfo.getName() != null);
    }
    
    /**
     * @param pid : pId of the process
     * @return true if the key is found in the running process list.
     */
    public boolean isProcessRunningByPid(String pid) {
    	
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		if (pid.equals(processInfo.getPid())){
				return true;
    		}
    	}
    	return false;
    }
    
    /**
     * @param showAll running process found
     * @return true if one of the running process is a known web browser.
     */
    public boolean isWebBrowserRunning(boolean showAll) {
    	
    	boolean isRunning = false;
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		for (String processName : webBrowserProcessList){
	    		if (processInfo.getName().equalsIgnoreCase(processName)){
					isRunning = true;
					if (!showAll) {
						break;
					}
					logger.info("Web browser process is still running : " + processInfo.getName());
	    		}
    		}
    	}
    	return isRunning;
    }
    
    /**
     * @return the list of the running known web browser processes.
     */
    public List<ProcessInfo> whichWebBrowserRunning() {
    	
    	List<ProcessInfo> webBrowserRunningList = new ArrayList<>();
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		for (String processName : webBrowserProcessList){
	    		if (processInfo.getName().equalsIgnoreCase(processName)){
	    			logger.info("Web browser process still running : " + processInfo.getName());
	    			webBrowserRunningList.add(processInfo);
	    		}
    		}
    	}
    	return webBrowserRunningList;
    }
    
	/******************************************
	 ************* KILL process ***************
	 ******************************************/
    
    /**
     * Kill process by PID
     * @param process
     * @param force
     * @throws IOException
     */
    public abstract String killProcess(String pid, boolean force);
    
    
    /**
     * Kill process by name
     * @param process
     * @param force
     * @throws IOException
     */
    public abstract String killProcessByName(String programName, boolean force);
    
	
    /**
     * Get extension of the program
     * @return
     */
	public abstract String getProgramExtension();
	

    /**
     * Ask system to terminate all the known web browser processes.
     * @param force
     */
    public void killAllWebBrowserProcess(boolean force){
    	
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		for (String processName : webBrowserProcessList) {
    			if (processInfo.getName().equalsIgnoreCase(processName)){
					logger.info("Asked system to terminate browser: " + processInfo.getName());
		    		killProcess(processInfo.getPid(), force);
    			}
    		}
    	}
    }
    
    /**
     * Ask system to terminate all the drivers processes
     * @param force
     */
    public void killAllWebDriverProcess(){
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		for (String processName : webDriverProcessList) {
    			if (processInfo.getName().contains(processName)){
    				logger.info("Asked system to terminate webdriver: " + processInfo.getName());
    				killProcess(processInfo.getPid(), true);
    			}
    		}
    	}
    }
    
    /**
     * Terminate Internet explorer.
     * @param force to kill the process
     * @return output command lines
     * @throws IOException
     */
    public String killIEProcess() {
    	ProcessInfo processInfo = getRunningProcess("iexplore");
		if (processInfo.getName() != null){
			logger.info("Asked system to terminate : " + processInfo.getName());
			return killProcess(processInfo.getPid(), true);	
		}
		return "Internet Explorer has not been found.";
    }
    
    public abstract int getIEVersion();
    
    public abstract String getOSBuild();
    
    public abstract String getProgramNameFromPid(Long pid);
    
    public static String getChromeVersion(String chromePath) {
    	return OSCommand.executeCommandAndWait(new String[] {chromePath, "--version"});
    }
    
    public static String getFirefoxVersion(String firefoxPath) {
    	return OSCommand.executeCommandAndWait(firefoxPath + " --version | more");
    }
    
    public List<BrowserType> getInstalledBrowsers() {
    	List<BrowserType> browsers = new ArrayList<>();
    	Map<BrowserType, List<BrowserInfo>> installedBrowsers = getInstalledBrowsersWithVersion();
    	for (BrowserType bType: installedBrowsers.keySet()) {
    		if (!installedBrowsers.get(bType).isEmpty()) {
    			browsers.add(bType);
    		}
    	}
    	return browsers;
    }
    
    /**
     * Returns the list of browsers for each type. For selenium robot local, this will help selecting the right binary
     * For grid, we will be able to provide each installed browser to the runner
     * @return
     */
    public abstract Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion();
    
    /**
     * example: Mozilla Firefox 52.0
     * @param versionString
     * @return
     */
    public static String extractFirefoxVersion(String versionString) {
    	Pattern regMozilla = Pattern.compile("^Mozilla .* (\\d+\\.\\d+).*");
    	Matcher versionMatcher = regMozilla.matcher(versionString.trim());
		if (versionMatcher.matches()) {
			return versionMatcher.group(1);
		} else {
			return "";
		}
    }
    
    /**
     * example: Google Chrome 57.0.2987.110
     * @param versionString
     * @return
     */
    public static String extractChromeVersion(String versionString) {
    	Pattern regChrome = Pattern.compile("^Google Chrome (\\d+\\.\\d+).*");
    	Matcher versionMatcher = regChrome.matcher(versionString.trim());
    	if (versionMatcher.matches()) {
    		return versionMatcher.group(1);
    	} else {
    		return "";
    	}
    }
    
    /**
     * example: Chromium 56.0.2924.76 Built on Ubuntu , running on Ubuntu 16.04 
     * @param versionString
     * @return
     */
    public static String extractChromiumVersion(String versionString) {
    	Pattern regChrome = Pattern.compile("^Chromium (\\d+\\.\\d+).*");
    	Matcher versionMatcher = regChrome.matcher(versionString.trim());
    	if (versionMatcher.matches()) {
    		return versionMatcher.group(1);
    	} else {
    		return "";
    	}
    }
    
    /**
     * example: 11.0.9600.18499
     * @param versionString
     * @return
     */
    public static String extractIEVersion(String versionString) {
    	return versionString.split("\\.")[0];
    }
    
    /**
     * example: 10240.th1.160802-1852
     * @param versionString
     * @return
     */
    public static String extractEdgeVersion(String versionString) {
    	return versionString.split("\\.")[0];
    }

	public static Map<BrowserType, List<BrowserInfo>> getInstalledBrowsersWithVersion() {
		if (installedBrowsersWithVersion == null) {
			refreshBrowserList();
		}
		return installedBrowsersWithVersion;
	}
	
	/**
	 * search browsers
	 */
	public static void refreshBrowserList() {
		installedBrowsersWithVersion = OSUtilityFactory.getInstance().discoverInstalledBrowsersWithVersion();
	}
    
}
