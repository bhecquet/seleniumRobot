/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;

public class OSUtilityUnix extends OSUtility {
	
	/**
     * Ask console for every running process.
     * @return list of output command lines
     */
	@Override
    public List<ProcessInfo> getRunningProcessList(){
    	String command = "ps ax";
    	List<String> strProcessList = Arrays.asList(OSCommand.executeCommandAndWait(command).split("\n"));
    	
    	List<ProcessInfo> processInfoList = new ArrayList<>();
    	for (String sentence : strProcessList) {
    		String[] words = sentence.trim().split("\\s+");
    		
    		ProcessInfo processInfo = new ProcessInfo();
    		
    		int i = 0;
    		
    		if (words[i] != null && !words[i].isEmpty()) {
    			
    			// PID
    			processInfo.setPid(words[i]);
    			i++;
    			
    			// TTY
    			processInfo.setSessionName(words[i]);
    			i += 2;
    			
    			// TIME
    			processInfo.setCpuTime(words[i]);
    			i++;
    			
    			// CMD
    			processInfo.setName(words[i]);
    		}
    		processInfoList.add(processInfo);
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
    @Override
    public String killProcess(String pid, boolean force) {
    	
    	if (force) {
    		return OSCommand.executeCommandAndWait("kill -SIGKILL " + pid);
    	} else {
    		return OSCommand.executeCommandAndWait("kill -SIGTERM " + pid);
    	}
    }

	@Override
	public String getProgramExtension() {
		return "";
	}

	@Override
	public int getIEVersion() {
		return 0;
	}
	

	@Override
	public String getOSBuild() {
		return OSCommand.executeCommandAndWait("uname -a");
	}
	
	@Override
	public Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion() {
		Map<BrowserType, List<BrowserInfo>> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, Arrays.asList(new BrowserInfo(BrowserType.HTMLUNIT, BrowserInfo.LATEST_VERSION, null)));
		browserList.put(BrowserType.PHANTOMJS, Arrays.asList(new BrowserInfo(BrowserType.PHANTOMJS, BrowserInfo.LATEST_VERSION, null)));
		
		// TODO: handle multiple installation of firefox and Chrome
		if (!OSCommand.executeCommandAndWait("which firefox").trim().isEmpty()) {
			String version = getFirefoxVersion("firefox");
			browserList.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), OSCommand.executeCommandAndWait("which firefox").trim())));
		} else if (!OSCommand.executeCommandAndWait("which iceweasel").trim().isEmpty()) {
			String version = getFirefoxVersion("iceweasel");
			browserList.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), OSCommand.executeCommandAndWait("which iceweasel").trim())));
		}
		if (!OSCommand.executeCommandAndWait("which chromium-browser").trim().isEmpty()) {
			String version = getChromeVersion("chromium-browser");
			browserList.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, extractChromiumVersion(version), OSCommand.executeCommandAndWait("which chromium-browser").trim())));
		} else if (!OSCommand.executeCommandAndWait("which google-chrome").trim().isEmpty()) {
			String version = getChromeVersion("google-chrome");
			browserList.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, extractChromeVersion(version), OSCommand.executeCommandAndWait("which google-chrome").trim())));
		} 
		
		return browserList;
	}
	
	@Override
	public List<Long> getChildProcessPid(Long parentProcess, String processName, List<Long> existingPids) throws IOException {
		
		List<Long> searchedPids = new ArrayList<>();
		
		String pids = OSCommand.executeCommandAndWait(String.format("pgrep -P %d -d , -l", parentProcess)).trim();
        for(String process: pids.split(",")) {
        	String[] processSplit = process.split(" ");
        	Long pid;
        	try {
        		pid = Long.parseLong(processSplit[0]);
        	} catch (NumberFormatException e) {
        		continue;
        	}
        	
        	// pgrep limits process name to 15 chars so do not compare with entire name
            if((processName == null || processName.startsWith(processSplit[1])) && !existingPids.contains(pid)) {
            	searchedPids.add(pid);
            }
        }
       
        
        return searchedPids;
	}
	
	@Override
	public String getProgramNameFromPid(Long pid) {
		return OSCommand.executeCommandAndWait(String.format("ps -p %d -o comm=", pid));
	}

}
