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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OSUtilityWindows extends OSUtility {
	
	public static int getIEVersion() {

        String output = executeCommand("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v svcVersion");
        if (output.split("\n").length < 3) {
            output = executeCommand("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v Version");
        }

        String internet_explorer_value = output.split("\n")[2];
        String version = internet_explorer_value.trim().split("   ")[2];
        version = version.trim().split("\\.")[0];
        return Integer.parseInt(version);
    }

    /**
     * Ask console for every running process.
     * @return list of output command lines
     */
    protected static List<String> getWRunningProcessList(){
    	String command = System.getenv("windir") + "\\system32\\" + "tasklist.exe /NH"; // Windows 7, windir = C:\Windows\ 
	    return Arrays.asList(executeCommand(command).split("\n"));
    }
    
    /**
     * Terminate process from command line terminal.
     * @param process
     * @param force to kill the process
     * @return
     * @throws IOException
     */
    protected static String wkillProcess(String process, boolean force) throws IOException {

    	if (force) {
    		return executeCommand("taskkill /F /IM " + process + ".exe");
    	} else {
    		return executeCommand("taskkill /IM " + process + ".exe");
    	}
    	
    }
    
    /**
     * Terminate Internet explorer.
     * @param force to kill the process
     * @return output command lines
     * @throws IOException
     */
    public static String killIEProcess(boolean force) throws IOException {
		return wkillProcess("iexplore", true);	
    }
}
