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

package com.seleniumtests.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.seleniumtests.reporter.TestLogging;

public class OSUtility {
	
	private static final Logger logger = TestLogging.getLogger(OSUtility.class);
	
	/**
	 * 
	 */
	static String[] webBrowserProcessList = 
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
    
    /**
     * Execute a command in command line terminal
     * @param cmd
     * @param wait for the end of the command execution
     * @return 
     */
    protected static String executeCommand(final String cmd) {
        String output = "";
        Process proc;
        try {
			proc = Runtime.getRuntime().exec(cmd);
			
	        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	
	    	BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	        
	        String s = null;
	        
        	// read result output from command
            while ((s = stdInput.readLine()) != null) {
                output += s + "\n";
            }
            // read error output from command
            while ((s = stdError.readLine()) != null) {
                output += s + "\n";
            }
        } catch (IOException e1) {
        	logger.error(e1);
        }
        
        return output;
    }
    
    /**
     * Kill named process
     * @param process
     * @param force
     * @throws IOException
     */
    public static void killProcess(String process, boolean force) throws IOException {
    	if (isWindows()) {
			OSUtilityWindows.wkillProcess(process, force);
		} else {
			OSUtilityUnix.ukillProcess(process, false);
		}
    }

    /**
     * Ask system to terminate all the known web browser processes.
     * @param force
     */
    public static void killWebBrowserProcesses(boolean force){
    	
    	int stopWhile=0;
    	int maxCount=15;
    	//TODO: set this to false after test phase
    	while (isWebBrowserRunning(true) && stopWhile<maxCount) {
    		stopWhile++;
    		for (String process : webBrowserProcessList) {
    			if (isProcessRunning(process)){
			    	try {
			    		killProcess(process, force);
					} catch (IOException e) {
						logger.error(e);
					}
    			}
    		}
    	} // end while
    	if (stopWhile >= maxCount) {
    		logger.info("could not stop every running process.");
    	}
    }
    
    /**
     * Returns list of all running processes
     * @return
     */
    public static List<String> getRunningProcessList() {
    	if (isWindows()) {
    		return OSUtilityWindows.getWRunningProcessList();
    	} else {
    		return OSUtilityUnix.getURunningProcessList();
    	}	
    }
    
    /**
     * @param showAll running process found
     * @return true if one of the running process is a known web browser.
     */
    public static boolean isWebBrowserRunning(boolean showAll) {
    	
    	boolean isRunning = false;
    	for (String line : getRunningProcessList()) {
    		for (String process : webBrowserProcessList){
	    		if (line.toLowerCase().contains(process)){
					isRunning = true;
					if (!showAll) break;
					logger.info("process is still running : " + line);
	    		}
    		}
    	}
    	return isRunning;
    }
    
    /**
     * @return the list of the running known web browser processes.
     */
    public static List<String> whichWebBrowserRunning() {
    	
    	List<String> webBrowserRunningList = new ArrayList<>();
    	for (String line : getRunningProcessList()) {
    		for (String process : webBrowserProcessList){
	    		if (line.toLowerCase().contains(process)){
	    			webBrowserRunningList.add(line);
	    		}
    		}
    	}
    	return webBrowserRunningList;
    }
    
    /**
     * @param key : process name
     * @return true if the key is found in the running process list.
     */
    public static boolean isProcessRunning(String key) {
    	
    	for (String line : getRunningProcessList()) {
    		if (line.contains(key)){
				return true;
    		}
    	}
    	return false;
    }
    
}
