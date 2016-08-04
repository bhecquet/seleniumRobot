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

package com.seleniumtests.util.osutility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.seleniumtests.reporter.TestLogging;

/**
 * Facade of the OS Utility Windows or Unix
 */
public class OSUtility {

	private static final Logger logger = TestLogging.getLogger(OSUtility.class);
	
	private String[] webBrowserProcessList= 
		{ 
		 //Android browser, 
		 "chrome", "chromedriver", 
		 "firefox", 
		 //htmlunit, 
		 "iexplore", //"IEDriverServer",
		 //marionette,
		 //opera, 
		 //phntomjs, 
		 //safari, 
		 };
	
	private OSUtilityWindows osw;
	private OSUtilityUnix osu;
	
	public OSUtility() {
		if (isWindows()){
			osw = new OSUtilityWindows();
		} else {
			osu = new OSUtilityUnix();
		}
	}
	
	/******************************************
	 *********** OS information ***************
	 ******************************************/
	
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
    public List<ProcessInfo> getRunningProcessList() {
    	if (isWindows()) {
    		return osw.getRunningProcessList();
    	} else {
    		return osu.getRunningProcessList();
    	}	
    }
    
    /**
     * @param name of the process
     * @return ProcessInfo
     */
    public ProcessInfo getRunningProcess(String processName) {
    	
    	ProcessInfo output = new ProcessInfo();
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		if (processInfo.getName().toLowerCase().equals(processName)){
    			output = processInfo;
				break;
    		}
    	}
    	return output;
    }
    
    /**
     * @param processName : process name
     * @return true if the key is found in the running process list.
     */
    public boolean isProcessRunning(String processName) {
    	
    	ProcessInfo processInfo = getRunningProcess(processName);
		if (processInfo.getName() != null){
			return true;
		}
    	return false;
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
	    		if (processInfo.getName().toLowerCase().equals(processName)){
					isRunning = true;
					if (!showAll) break;
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
	    		if (processInfo.getName().toLowerCase().equals(processName)){
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
     * Kill named process
     * @param process
     * @param force
     * @throws IOException
     */
    public String killProcess(String pid, boolean force) throws IOException {
    	if (isWindows()) {
			return osw.killProcess(pid, force);
		} else {
			return osu.killProcess(pid, false);
		}
    }

    /**
     * Ask system to terminate all the known web browser processes.
     * @param force
     */
    public void killAllWebBrowserProcess(boolean force){
    	
    	for (ProcessInfo processInfo : getRunningProcessList()) {
    		for (String processName : webBrowserProcessList) {
    			if (processInfo.getName().toLowerCase().equals(processName)){
    				try {
    					logger.info("Asked system to terminate : " + processInfo.getName());
			    		killProcess(processInfo.getPid(), force);
					} catch (IOException e) {
						logger.error(e);
					}
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
    public String killIEProcess(boolean force) throws IOException {
    	ProcessInfo processInfo = getRunningProcess("iexplore");
		if (processInfo.getName() != null){
			logger.info("Asked system to terminate : " + processInfo.getName());
			return killProcess(processInfo.getPid(), true);	
		}
		return "Internet Explorer has not been found.";
    }
    
    public int getIEVersion() {
    	if (isWindows()) {
			return osw.getIEVersion();
		}
    	logger.error("Internet Explorer is supported by Windows only.");
    	return 0;
    }
    
}
