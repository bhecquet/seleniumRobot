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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSUtilityWindows extends OSCommand {
		
	
	public int getIEVersion() {

        String output = executeCommand("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v svcVersion");
        if (output.split("\n").length < 3) {
            output = executeCommand("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v Version");
        }

        String internetExplorerValue = output.split("\n")[2];
        String version = internetExplorerValue.trim().split("   ")[2];
        version = version.trim().split("\\.")[0];
        return Integer.parseInt(version);
    }

	/**
     * @param fast : true gets only Image Name and pId of the process
     * @return list of ProcessInfo
     */
    protected List<ProcessInfo> getRunningProcessList(){
    	/*
    	 * Output command : Image name ;  PID ;  Session name ;  Session# ;  Mem Usage .
    	 * In Windows 7, windir = C:\Windows\ 
    	 * /NH will not display column headers
    	 * and /V displays also : Status ;  Username ;  CPU time ;  Windows title .
    	 * or /SVC displays only : Image name ;  PID ;  Services .
    	 */
    	String command = System.getenv("windir") + "\\system32\\" + "tasklist.exe /NH /SVC";
    	List<String> strProcessList = Arrays.asList(executeCommand(command).split("\n"));
    	
    	List<ProcessInfo> processInfoList = new ArrayList<>();
    	for (String sentence : strProcessList) {
    		sentence = sentence.toLowerCase();
    		
    		ProcessInfo processInfo = new ProcessInfo();
    		
			Pattern pTasklist = Pattern.compile("(system|.*\\.exe)\\s+(\\d+).*");
			Matcher m = pTasklist.matcher(sentence);
			
			if (m.matches()) {
				processInfo.setName(m.group(1).replaceAll(".exe", ""));
				processInfo.setPid(m.group(2));
				
				processInfoList.add(processInfo);
			}
    	}
    	return processInfoList;
    }
    
    /**
     * Terminate process from command line terminal.
     * @param process
     * @param force to kill the process
     * @return
     * @throws IOException
     */
    protected String killProcess(String pid, boolean force) throws IOException {

    	if (force) {
    		return executeCommand("taskkill /F /PID " + pid);
    	} else {
    		return executeCommand("taskkill /PID " + pid);
    	}
    	
    }
}
