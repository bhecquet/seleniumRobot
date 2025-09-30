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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Proxy;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;

public class OSUtilityUnix extends OSUtility {

	private static final String WHICH_ERROR = "which:";
	public static final String WHICH_COMMAND = "which";


	/**
     * Ask console for every running process.
     *
     * @return list of output command lines
     */
	@Override
    public List<ProcessInfo> getRunningProcessList(){
    	String[] strProcessList = OSCommand.executeCommandAndWait(new String[] {"ps", "ax"}).split("\n");
    	
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
     * @param pid	pid of process to kill
     * @param force to kill the process
     */
    @Override
    public String killProcess(String pid, boolean force) {
    	
    	if (force) {
    		return OSCommand.executeCommandAndWait(new String[] {"kill", "-SIGKILL", pid});
    	} else {
    		return OSCommand.executeCommandAndWait(new String[] {"kill", "-SIGTERM", pid});
    	}
    }
    
    /**
     * Kill process by name
     */
	@Override
	public String killProcessByName(String programName, boolean force) {
    	return OSCommand.executeCommandAndWait(new String[] {"killall", "-I", programName});
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
		return OSCommand.executeCommandAndWait(new String[] {"uname", "-a"});
	}
	
	@Override
	public Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion(boolean discoverBetaBrowsers) {
		Map<BrowserType, List<BrowserInfo>> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, List.of(new BrowserInfo(BrowserType.HTMLUNIT, BrowserInfo.LATEST_VERSION, null)));

		String firefoxLocation = OSCommand.executeCommandAndWait(new String[] {WHICH_COMMAND, "firefox"}).trim();
		String iceweaselLocation = OSCommand.executeCommandAndWait(new String[] {WHICH_COMMAND, "iceweasel"}).trim();
		String chromeLocation = OSCommand.executeCommandAndWait(new String[] {WHICH_COMMAND, "google-chrome"}).trim();
		String chromiumLocation = OSCommand.executeCommandAndWait(new String[] {WHICH_COMMAND, "chromium-browser"}).trim();
		
		if (!firefoxLocation.isEmpty() && !firefoxLocation.contains(WHICH_ERROR)) {
			String version = getFirefoxVersion("firefox");
            List<BrowserInfo> arrayFirefox = new ArrayList<>();
            arrayFirefox.add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), firefoxLocation));
            browserList.put(BrowserType.FIREFOX, arrayFirefox);
			
		} else if (!iceweaselLocation.isEmpty() && !iceweaselLocation.contains(WHICH_ERROR)) {
			String version = getFirefoxVersion("iceweasel");
            List<BrowserInfo> arrayIceWeasel = new ArrayList<>();
            arrayIceWeasel.add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), iceweaselLocation));
            browserList.put(BrowserType.FIREFOX, arrayIceWeasel);
		}
		if (!chromiumLocation.isEmpty() && !chromiumLocation.contains(WHICH_ERROR)) {
			String version = getChromeVersion("chromium-browser");
            List<BrowserInfo> arrayChromium = new ArrayList<>();
            arrayChromium.add(new BrowserInfo(BrowserType.CHROME, extractChromiumVersion(version), chromiumLocation));
            browserList.put(BrowserType.CHROME, arrayChromium);
			
		} else if (!chromeLocation.isEmpty() && !chromeLocation.contains(WHICH_ERROR)) {
			String version = getChromeVersion("google-chrome");
            List<BrowserInfo> arrayChrome = new ArrayList<>();
            arrayChrome.add(new BrowserInfo(BrowserType.CHROME, extractChromeVersion(version), chromeLocation));
            browserList.put(BrowserType.CHROME, arrayChrome);
		} 
		
		return browserList;
	}
	
	@Override
	public List<Long> getChildProcessPid(Long parentProcess, String processName, List<Long> existingPids) {
		
		List<Long> searchedPids = new ArrayList<>();
		
		String pids = OSCommand.executeCommandAndWait(new String[] {"pgrep", "-P", String.valueOf(parentProcess), "-d" , "-l"}).trim();
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
		return OSCommand.executeCommandAndWait(new String[] {"ps", "-p", String.valueOf(pid), "-o", "comm="});
	}


	@Override
	public Integer getProcessIdByListeningPort(int port) {
		// example: TCP    127.0.0.1:51239        0.0.0.0:0              LISTENING       22492
		String lines = OSCommand.executeCommandAndWait(new String[] {"netstat", "-anp"}).trim();
		Pattern pattern = Pattern.compile(String.format(".*\\:%d\\s+.*\\:.*LISTEN\\s+(\\d+).*", port));
		for (String line: lines.split("\n")) {
			Matcher matcher = pattern.matcher(line.trim());

			if (matcher.matches()) {
				return Integer.parseInt(matcher.group(1));
			}
		} 
		return null;
	}

	@Override
	public Charset getConsoleCharset() {
		try {
			return Charset.forName(OSCommand.executeCommandAndWait(new String[] {"locale", "charmap"}, -1, StandardCharsets.UTF_8).trim());
		} catch (Exception e) {
			return Charset.defaultCharset();
		}
	}
	
	@Override
	public void setSystemProxy(Proxy proxy) {
		logger.warn("setSystemProxy is not available on unix systems");
		
	}

}
