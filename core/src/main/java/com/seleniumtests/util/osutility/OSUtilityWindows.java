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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DisabledConnector;
import com.seleniumtests.driver.BrowserType;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class OSUtilityWindows extends OSUtility {
	
	private static final String MSEDGE_EXE = "msedge.exe";
	private static final String EXE_EXT_QUOTE = ".exe\"";
	private static final String KEY_VERSION = "version";
	Pattern versionPattern = Pattern.compile(".*?(\\d++\\.\\d++\\.\\d++).*?");
		
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
    	Pattern pTasklist = Pattern.compile("([^\\s]+)\\s++(\\d++)\\s++.*");
    	
    	List<ProcessInfo> processInfoList = new ArrayList<>();
    	for (String sentence : strProcessList) {
    		sentence = sentence.toLowerCase().trim();
    		
    		ProcessInfo processInfo = new ProcessInfo();
    		
			Matcher m = pTasklist.matcher(sentence);
			
			if (m.matches()) {
				processInfo.setName(m.group(1).replaceAll(".exe", "").toLowerCase());
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
    		try {
    			OSCommand.executeCommandAndWait(String.format("wmic process where \"processid='%s'\" delete", pid));
    		} catch (Exception e) {
    			// use an other mean if wmic fails
    		}
    		return OSCommand.executeCommandAndWait("taskkill /F /PID " + pid);
    	} else {
    		return OSCommand.executeCommandAndWait("taskkill /PID " + pid);
    	}
    	
    }
    
    /**
     * Kill process by name, extension must not be given as it's added automatically
     * @param 	programName	name of the process without extension
     * @param	force		do we force program exit
     */
	@Override
	public String killProcessByName(String programName, boolean force) {
		if (force) {
			try {
    			OSCommand.executeCommandAndWait(String.format("wmic process where \"name='%s'\" delete", programName + getProgramExtension()));
    		} catch (Exception e) {
    			// use an other mean if wmic fails
    		}
			return OSCommand.executeCommandAndWait("taskkill /F /IM " + programName + getProgramExtension());
    	} else {
    		return OSCommand.executeCommandAndWait("taskkill /IM " + programName + getProgramExtension());
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
	
	private String getChromeVersionFromRegistry() {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", KEY_VERSION);
		} catch (Win32Exception e) {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", KEY_VERSION);
		}
	}
	
	private String getChromeBetaVersionFromRegistry() {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome Beta", KEY_VERSION);
		} catch (Win32Exception e) {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome Beta\\BLBeacon", KEY_VERSION);
		}
	}
	
	/**
	 * Search for a folder with version name where chrome.exe is located (e.g: 58.0.3029.81)
	 * @param chromePath
	 * @return
	 */
	public String getChromeVersionFromFolder(String chromePath) {
		if (!new File(chromePath).exists()) {
			throw new ConfigurationException("Chrome version could not be get from folder, chrome path does not exist");
		}
		for (File file: new File(chromePath.replace("chrome.exe", "")).listFiles()) {
			if (file.isDirectory() && file.getName().matches("^\\d++.*")) {
				return file.getName();
			}
		}
		throw new ConfigurationException("Chrome version could not be get from folder");
	}
	
	/**
	 * Search for a folder with version name where msedge.exe is located (e.g: 58.0.3029.81)
	 * @param chromePath
	 * @return
	 */
	public String getEdgeVersionFromFolder(String edgePath) {
		File parentFolder = Paths.get(edgePath).toFile();
		if (!parentFolder.exists()) {
			throw new ConfigurationException("Edge version could not be get from folder, edge path does not exist");
		}
		for (File file: parentFolder.listFiles()) {
			if (file.isDirectory() && file.getName().matches("^\\d++.*")) {
				return file.getName();
			}
		}
		throw new ConfigurationException("Edge version could not be get from folder");
	}

	private String getEdgeVersionFromRegistry() {
		return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "Version");
	}
	
	private String getEdgeBetaVersionFromRegistry() {
		return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge Beta", "Version");
	}
	
	private String getWindowsBetaEdgeVersion(String edgeBetaPath) {
		String versionBeta;
		try {
			versionBeta = getEdgeBetaVersionFromRegistry();
		} catch (Win32Exception e) {
			versionBeta = getEdgeVersionFromFolder(edgeBetaPath);
		}
		return versionBeta;
	}

	/**
	 * @param chromePath
	 * @return
	 */
	private String getWindowsEdgeVersion(String edgeBetaPath) {
		String version;
		try {
			version = getEdgeVersionFromRegistry();
		} catch (Win32Exception e) {
			version = getEdgeVersionFromFolder(edgeBetaPath);
		}
		return version;
	}
	
	private String getIeVersionFromRegistry() {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "svcVersion");
		} catch (Win32Exception e) {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", KEY_VERSION);
		}
	}
	
	private List<String> searchFirefoxVersions() {
		List<String> firefoxInstallations = new ArrayList<>();
		String out = OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"});
		for (String line: out.split("\n")) {
			if (line.startsWith("HKEY_CLASSES_ROOT")) {
				String keyPath = line.replace("HKEY_CLASSES_ROOT\\", "").trim();
				try {
					String firefoxPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, keyPath + "\\shell\\open\\command", "");
					firefoxPath = firefoxPath.split(EXE_EXT_QUOTE)[0].replace("\"", "") + ".exe";
					firefoxInstallations.add(firefoxPath);
				} catch (Win32Exception e) {
					// do not crash
				}
			}
		}
		return firefoxInstallations;
	}
	
	private String getEdgeChromiumPath() {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge", "InstallLocation");
		} catch (Win32Exception e) {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\App Paths\\msedge.exe", "Path");
		}
	}

	@Override
	public Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion(boolean discoverBetaBrowsers) {
			
		Map<BrowserType, List<BrowserInfo>> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, Arrays.asList(new BrowserInfo(BrowserType.HTMLUNIT, BrowserInfo.LATEST_VERSION, null)));
		browserList.put(BrowserType.PHANTOMJS, Arrays.asList(new BrowserInfo(BrowserType.PHANTOMJS, BrowserInfo.LATEST_VERSION, null)));
		
		// look for Firefox
		try {
			browserList.put(BrowserType.FIREFOX, new ArrayList<>());
			
			for (String firefoxPath: searchFirefoxVersions()) {
				String version = getFirefoxVersion(firefoxPath);
				try {
					browserList.get(BrowserType.FIREFOX).add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), firefoxPath));
				} catch (ConfigurationException e) {
					// ignore
				}
			}
		} catch (IndexOutOfBoundsException e) {
			// ignore
		}


		// look for chrome
		try {
			browserList.put(BrowserType.CHROME, new ArrayList<>());

			// main chrome version
			String chromePath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "");
			chromePath = chromePath.split(EXE_EXT_QUOTE)[0].replace("\"", "") + ".exe";
			String version = getWindowsChromeVersion(chromePath);
			browserList.get(BrowserType.CHROME).add(new BrowserInfo(BrowserType.CHROME, extractChromeVersion("Google Chrome " + version), false, chromePath));
		} catch (Win32Exception | ConfigurationException e) {
			logger.warn("Error searching chrome installations: " + e.getMessage());
		}

		try {
			// beta chrome version
			String chromeBetaPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeBHTML\\shell\\open\\command", "");
			chromeBetaPath = chromeBetaPath.split(EXE_EXT_QUOTE)[0].replace("\"", "") + ".exe";
			String versionBeta;
			versionBeta = getWindowsBetaChromeVersion(chromeBetaPath);
			browserList.get(BrowserType.CHROME).add(new BrowserInfo(BrowserType.CHROME, extractChromeVersion("Google Chrome " + versionBeta), true, chromeBetaPath));

		} catch (Win32Exception | ConfigurationException e) {
			logger.warn("Error searching Beta chrome installations: " + e.getMessage());
		}




		
		// look for ie
		try {
			Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "");
			String version = getIeVersionFromRegistry();
			
			browserList.put(BrowserType.INTERNET_EXPLORER, Arrays.asList(new BrowserInfo(BrowserType.INTERNET_EXPLORER, extractIEVersion(version), null)));
		} catch (Win32Exception | ConfigurationException e) {
			logger.warn("Error searching Internet explorer installations: " + e.getMessage());
		}
		
		
		// look for edge chromium
		try {
			browserList.put(BrowserType.EDGE, new ArrayList<>());
			String edgePath = getEdgeChromiumPath();
			String version = getWindowsEdgeVersion(edgePath);
			
			if (version != null && !version.isEmpty()) {
				browserList.get(BrowserType.EDGE).add(new BrowserInfo(BrowserType.EDGE, extractEdgeVersion(version), false, Paths.get(edgePath, MSEDGE_EXE).toString()));
			}
		} catch (Win32Exception | ConfigurationException e) {
			logger.warn("Error searching Edge chromium installations: " + e.getMessage());
		}
		

		try {
			// beta edge version
			String edgePathBeta = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge Beta", "InstallLocation");
			String versionBeta = getWindowsBetaEdgeVersion(edgePathBeta);

			if (versionBeta != null && !versionBeta.isEmpty()) {
				browserList.get(BrowserType.EDGE).add(new BrowserInfo(BrowserType.EDGE, extractEdgeVersion(versionBeta), true, Paths.get(edgePathBeta, MSEDGE_EXE).toString()));
			}

		} catch (Win32Exception | ConfigurationException e) {
			logger.warn("Error searching Beta Edge chromium installations: " + e.getMessage());
		}

		return browserList;
	}

	/**
	 * @param chromeBetaPath
	 * @return
	 */
	private String getWindowsBetaChromeVersion(String chromeBetaPath) {
		String versionBeta;
		try {
			versionBeta = getChromeBetaVersionFromRegistry();
		} catch (Win32Exception e) {
			versionBeta = getChromeVersionFromFolder(chromeBetaPath);
		}
		return versionBeta;
	}

	/**
	 * @param chromePath
	 * @return
	 */
	private String getWindowsChromeVersion(String chromePath) {
		String version;
		try {
			version = getChromeVersionFromRegistry();
		} catch (Win32Exception e) {
			version = getChromeVersionFromFolder(chromePath);
		}
		return version;
	}
	
	public String getSoapUiPath() {
		try {
			String[] jvms = Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, "Software\\ej-technologies\\exe4j\\jvms");
			
			for (String jvm: jvms) {
				if (jvm.toLowerCase().contains("soapui")) {
					return jvm.split("/jre/bin")[0];
				}
			}
		} catch (Exception e) {
			// ignore
		}
		throw new DisabledConnector("SOAP UI is not installed (not found in registry). Install it and run it once (MANDATORY)");
	
	}

	@Override
	public List<Long> getChildProcessPid(Long parentProcess, String processName, List<Long> existingPids) throws IOException {
		Scanner scan = new Scanner(Runtime.getRuntime().exec(String.format("wmic process where (ParentProcessId=%d) get Caption,ProcessId", parentProcess)).getInputStream());
        scan.useDelimiter("\\A");
        String childProcessIds =  scan.hasNext() ? scan.next() : "";
        List<Long> namedSubprocesses = new ArrayList<>();
        String[] splited = childProcessIds.split("\\s+");
        for(int i =0 ; i<splited.length; i = i+2){
        	Long pid;
        	try {
        		pid = Long.parseLong(splited[i+1]);
        	} catch (NumberFormatException e) {
        		continue;
        	}
            if((processName == null || processName.equalsIgnoreCase(splited[i])) && !existingPids.contains(pid)) {
            	namedSubprocesses.add(pid);
            }
        }
       
        scan.close();
        
        return namedSubprocesses;
	}

	@Override
	public String getProgramNameFromPid(Long pid) {
		String[] processNames = OSCommand.executeCommandAndWait(String.format("wmic process where processId=%d get name", pid)).trim().split("\n");
		String processName = processNames[processNames.length - 1];
		return processName.endsWith(".exe") ? processName: "";
	}
	
	@Override
	public Integer getProcessIdByListeningPort(int port) {
		// example: TCP    127.0.0.1:51239        0.0.0.0:0              LISTENING       22492
		String lines = OSCommand.executeCommandAndWait("netstat -aon").trim();
		Pattern pattern = Pattern.compile(String.format(".*\\:%d\\s+.*\\:.*LISTENING\\s+(\\d+).*", port));
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
			return Charset.forName("cp" + OSCommand.executeCommandAndWait(new String[] {"cmd.exe", "/C", "chcp"}, -1, StandardCharsets.UTF_8).split(": ")[1].trim());
		} catch (Exception e) {
			return Charset.defaultCharset();
		}

	}
}
