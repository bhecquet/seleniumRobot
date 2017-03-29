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

import com.seleniumtests.driver.BrowserType;

public class OSUtilityUnix extends OSUtility {
	
	/**
     * Ask console for every running process.
     * @return list of output command lines
     */
	@Override
    public List<ProcessInfo> getRunningProcessList(){
    	String command = "ps";
    	List<String> strProcessList = Arrays.asList(OSCommand.executeCommandAndWait(command).split("\n"));
    	
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
	public Map<BrowserType, String> getInstalledBrowsersWithVersion() {
		Map<BrowserType, String> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, "latest");
		browserList.put(BrowserType.PHANTOMJS, "latest");
		
		if (!OSCommand.executeCommandAndWait("which firefox").trim().isEmpty()) {
			String version = OSCommand.executeCommandAndWait("firefox --version | more");
			browserList.put(BrowserType.FIREFOX, extractFirefoxVersion(version));
		} else if (!OSCommand.executeCommandAndWait("which iceweasel").trim().isEmpty()) {
			String version = OSCommand.executeCommandAndWait("iceweasel --version | more");
			browserList.put(BrowserType.FIREFOX, extractFirefoxVersion(version));
		}
		if (!OSCommand.executeCommandAndWait("which google-chrome").trim().isEmpty()) {
			String version = OSCommand.executeCommandAndWait("google-chrome --version");
			browserList.put(BrowserType.CHROME, extractChromeVersion(version));
		} else if (!OSCommand.executeCommandAndWait("which chromium-browser").trim().isEmpty()) {
			String version = OSCommand.executeCommandAndWait("chromium-browser --version");
			browserList.put(BrowserType.CHROME, extractChromiumVersion(version));
		}
		
		return browserList;
	}
}
