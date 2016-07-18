/*
 * Copyright 2015 www.seleniumtests.com
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OSUtilityWindows extends OSUtility {
	
	public static int getIEVersion() {
        List<String> output;
        output = executeCommand("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v svcVersion");
        if (output.size() < 3) {
            output = executeCommand("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v Version");
        }

        String internet_explorer_value = (output.get(2));
        String version = internet_explorer_value.trim().split("   ")[2];
        version = version.trim().split("\\.")[0];
        return Integer.parseInt(version);
    }

    public static String getSlash() {
        return "\\";
    }

    /**
     * Ask console for every running process.
     * @return list of output command lines
     */
    protected static List<String> getRunningProcessList(){
    	List<String> processList = new ArrayList<>();
    	String command;
    	command = System.getenv("windir") +"\\system32\\"+"tasklist.exe"; // Windows 7, windir = C:\Windows\ 
	    processList = executeCommand(command);
    	return processList;
    }
    
    /**
     * Terminate process from command line terminal.
     * @param process
     * @param force to kill the process
     * @return
     * @throws IOException
     */
    protected static String killProcess(String process, boolean force) throws IOException {
    	List<String> outputLines;
    	
    	if (force) {
    		outputLines = executeCommand("taskkill /F /IM "+process+".exe");
    	} else {
    		outputLines = executeCommand("taskkill /IM "+process+".exe");
    	}
    	
        return StringUtility.fromListToString(outputLines);
    }
    
    /**
     * Terminate Internet explorer.
     * @param force to kill the process
     * @return output command lines
     * @throws IOException
     */
    public static String killIEProcess(boolean force) throws IOException {
    	String output;
    	
		output = killProcess("iexplore", true);
    	
        return output;
    }
}
