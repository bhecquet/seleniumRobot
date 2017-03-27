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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seleniumtests.driver.BrowserType;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class OSUtilityWindows extends OSUtility {
	
	Pattern versionPattern = Pattern.compile(".*?(\\d+\\.\\d+\\.\\d+).*?");
		
	@Override
	public int getIEVersion() {

        String output = OSCommand.executeCommandAndWait("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v svcVersion");
        if (output.split("\n").length < 3) {
            output = OSCommand.executeCommandAndWait("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v Version");
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
	@Override
    public List<ProcessInfo> getRunningProcessList(){
    	/*
    	 * Output command : Image name ;  PID ;  Session name ;  Session# ;  Mem Usage .
    	 * In Windows 7, windir = C:\Windows\ 
    	 * /NH will not display column headers
    	 * and /V displays also : Status ;  Username ;  CPU time ;  Windows title .
    	 * or /SVC displays only : Image name ;  PID ;  Services .
    	 */
    	String command = System.getenv("windir") + "\\system32\\" + "tasklist.exe /NH /SVC";
    	List<String> strProcessList = Arrays.asList(OSCommand.executeCommandAndWait(command).split("\n"));
    	
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
    @Override
    public String killProcess(String pid, boolean force) {

    	if (force) {
    		return OSCommand.executeCommandAndWait("taskkill /F /PID " + pid);
    	} else {
    		return OSCommand.executeCommandAndWait("taskkill /PID " + pid);
    	}
    	
    }

	@Override
	public String getProgramExtension() {
		return ".exe";
	}

	@Override
	public String getOSBuild() {
		String version = OSCommand.executeCommandAndWait("cmd /C ver").replace("\r", "").replace("\n", "").trim();
		Matcher versionMatcher = versionPattern.matcher(version);
		if (versionMatcher.matches()) {
			return versionMatcher.group(1);
		} else {
			logger.error("could not get Windows version");
			return "5000";
		}
	}

	@Override
	public Map<BrowserType, String> getInstalledBrowsersWithVersion() {
			
		Map<BrowserType, String> browserList = new EnumMap<>(BrowserType.class);
		
		// look for Firefox
		try {
			String firefoxPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "FirefoxHTML\\shell\\open\\command", "");
			String version = OSCommand.executeCommandAndWait(firefoxPath + File.separator + "firefox --version | more");
			browserList.put(BrowserType.FIREFOX, extractFirefoxVersion(version));
		} catch (Win32Exception e) {}
		
		
		// look for chrome
		try {
			Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "");
			String version;
			try {
				version = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version");
			} catch (Win32Exception e) {
				version = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version");
			}
			browserList.put(BrowserType.CHROME, extractChromeVersion("Google Chrome " + version));
		} catch (Win32Exception e) {}
		
		// look for ie
		try {
			Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "");
			String version;
			try {
				version = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "svcVersion");
			} catch (Win32Exception e) {
				version = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "version");
			}
			browserList.put(BrowserType.INTERNET_EXPLORER, extractIEVersion(version));
		} catch (Win32Exception e) {}
		
		// look for edge
		try {
			String version = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber");
			browserList.put(BrowserType.EDGE, extractEdgeVersion(version));
		} catch (Win32Exception e) {}
		
		return browserList;
	}
}
