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

public class OSUtilityUnix extends OSCommand {

	/**
     * Ask console for every running process.
     * @return list of output command lines
     */
    public List<ProcessInfo> getRunningProcessList(){
    	String command = "ps";
    	List<String> strProcessList = Arrays.asList(executeCommand(command).split("\n"));
    	
    	List<ProcessInfo> processInfoList = new ArrayList<>();
    	for (String sentence : strProcessList) {
    		String[] words = sentence.split("\\s+");
    		
    		ProcessInfo processInfo = new ProcessInfo();
    		
    		int i = 0;
    		
    		if (words[i] != null && !words[i].isEmpty()) {
    			
    			// PID
    			processInfo.setPid(words[i]);
    			i++;
    			
    			// TTY
    			processInfo.setSessionName(words[i]);
    			i++;
    			
    			// TIME
    			processInfo.setCpuTime(words[i]);
    			i++;
    			
    			// CMD
    			processInfo.setName(words[i]);
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
    		return executeCommand("kill -SIGKILL " + pid);
    	} else {
    		return executeCommand("kill -SIGTERM " + pid);
    	}
    }
}
