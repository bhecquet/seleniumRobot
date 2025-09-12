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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Hex;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;

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

        String output = OSCommand.executeCommandAndWait("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v svcVersion", true);
        if (output.split("\n").length < 3) {
            output = OSCommand.executeCommandAndWait("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v Version", true);
        }

        String internetExplorerValue = output.split("\n")[2];
        String version = internetExplorerValue.trim().split("   ")[2];
        version = version.trim().split("\\.")[0];
        return Integer.parseInt(version);
    }

	/**
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
    	String command = SystemUtility.getenv("windir") + "\\system32\\" + "tasklist.exe /NH /SVC";
    	List<String> strProcessList = Arrays.asList(OSCommand.executeCommandAndWait(command, true).split("\n"));
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
     * @param pid
     * @param force to kill the process
     * @return
     * @throws IOException
     */
    @Override
    public String killProcess(String pid, boolean force) {

    	if (force) {
    		try {
    			OSCommand.executeCommand(String.format("wmic process where \"processid='%s'\" delete", pid));
    			return "Done";
    		} catch (Exception e) {
    			// use an other mean if wmic fails
    		}
    		OSCommand.executeCommand("taskkill /F /PID " + pid);
    	} else {
    		OSCommand.executeCommand("taskkill /PID " + pid);
    	}
    	return "Done";
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
    			OSCommand.executeCommand(String.format("wmic process where \"name='%s'\" delete", programName + getProgramExtension()));
    			return "Done";
    		} catch (Exception e) {
    			// use an other mean if wmic fails
    		}
			OSCommand.executeCommand("taskkill /F /IM " + programName + getProgramExtension());
    	} else {
    		OSCommand.executeCommand("taskkill /IM " + programName + getProgramExtension());
    	}
		return "Done";
	}

	@Override
	public String getProgramExtension() {
		return ".exe";
	}

	@Override
	public String getOSBuild() {
		String version = OSCommand.executeCommandAndWait("cmd /C ver", true).replace("\r", "").replace("\n", "").trim();
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
	 * @param edgePath
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
	 * @param edgeBetaPath
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

		String fullFirefoxVersion = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Mozilla\\Mozilla Firefox", "CurrentVersion");
		String firefoxVersion = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Mozilla\\Mozilla Firefox", "");
		String firefoxPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, String.format("SOFTWARE\\Mozilla\\Mozilla Firefox\\%s\\Main", fullFirefoxVersion), "PathToExe");

		return List.of(firefoxPath, "Mozilla  " + firefoxVersion);
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
		
		// look for Firefox
		try {
			browserList.put(BrowserType.FIREFOX, new ArrayList<>());
			
			List<String> ffInfos = searchFirefoxVersions();

			browserList.get(BrowserType.FIREFOX).add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(ffInfos.get(1)), ffInfos.get(0)));
		} catch (Exception e) {
			logger.error("Error searching Firefox, it may not be installed: " + e.getMessage());
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
		String[] processNames = OSCommand.executeCommandAndWait(String.format("wmic process where processId=%d get name", pid), true).trim().split("\n");
		String processName = processNames[processNames.length - 1];
		return processName.endsWith(".exe") ? processName: "";
	}
	
	@Override
	public Integer getProcessIdByListeningPort(int port) {
		// example: TCP    127.0.0.1:51239        0.0.0.0:0              LISTENING       22492
		String lines = OSCommand.executeCommandAndWait("netstat -aon", true).trim();
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
	
	/**
	 * Set the proxy at system level
	 * This may be used when proxy configuration is blocked at browser level
	 *
	 * For information: https://stackoverflow.com/questions/1564627/how-to-set-automatic-configuration-script-for-a-dial-up-connection-programmati
	 * HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Connections registry key has values for all connections that are defined in 'Internet Options' and for LAN settings too (DefaultConnectionSettings is for LAN). The values are byte arrays and here is the description of every byte:
		1) Byte number zero always has a 3C or 46 - I couldnt find more information about this byte.The next three bytes are zeros.
		2) Byte number 4 is a counter used by the 'Internet Options' property sheet (Internet explorer->Tools->Internet Options...). 
		As you manually change the internet setting (such as LAN settings in the Connections tab), this counter increments.Its not very useful byte.But it MUST have a value.I keep it zero always.The next three bytes are zeros (Bytes 5 to 7).
		3) Byte number 8 can take different values as per your settings. 
		The value is : 
			09 when only 'Automatically detect settings' is enabled 
			03 when only 'Use a proxy server for your LAN' is enabled 
			0B when both are enabled 
			05 when only 'Use automatic configuration script' is enabled 
			0D when 'Automatically detect settings' and 'Use automatic configuration script' are enabled 
			07 when 'Use a proxy server for your LAN' and 'Use automatic configuration script' are enabled 
			0F when all the three are enabled. 
			01 when none of them are enabled. 
		The next three bytes are zeros (Bytes 9 to B).
		4) Byte number C (12 in decimal) contains the length of the proxy server address.For example a proxy server '127.0.0.1:80' has length 12 (length includes the dots and the colon).The next three bytes are zeros (Bytes D to F).
		5) Byte 10 (or 16 in decimal) contains the proxy server address - like '127.0.0.1:80' (where 80 is obviously the port number)
		6) the byte immediatley after the address contians the length of additional information.The next three bytes are zeros. 
		For example if the 'Bypass proxy server for local addresses' is ticked, then this byte is 07,
		the next three bytes are zeros and then comes a string i.e. '' ( indicates that you are bypassing the proxy server.Now since has 7 characters, the length is 07!). You will have to experiment on your own for finding more about this. If you dont have any additional info then the length is 0 and no information is added.
		7) The byte immediately after the additional info, is the length of the automatic configuration script address
		 (If you dont have a script address then you dont need to add anything,skip this step and goto step 8).
		 The next three bytes are zeros,then comes the address.
		8) Finally, 32 zeros are appended.(I dont know why!) 
	 * 
	 * ////////////////////////// Example of code
	        String autoConfigUrl = Configuration.get("proxyAutoUrl");
	        String proxyUrl = "http=" + Configuration.get("proxyAddress") + ":" + Configuration.get("proxyPort") + ";";
	        proxyUrl += "https=" + Configuration.get("proxyAddress") + ":" + Configuration.get("proxyPort") + ";";
	        proxyUrl += "ftp=" + Configuration.get("proxyAddress") + ":" + Configuration.get("proxyPort");
	
	        String proxyExclusion = Configuration.get("proxyExclude");
	
	        // Build string
	        // http://stackoverflow.com/questions/1564627/how-to-set-automatic-configuration-script-for-a-dial-up-connection-programmati
	        String hexString = "460000001800000003000000";
	
	        try {
	            // proxy
	            hexString += String.format("%02X000000", proxyUrl.length());
	            hexString += Hex.encodeHexString(proxyUrl.getBytes("UTF-8"));
	
	            // proxy exclusion
	            hexString += String.format("%02X000000", proxyExclusion.length());
	            hexString += Hex.encodeHexString(proxyExclusion.getBytes("UTF-8"));
	            hexString += "00000000";
	
	            // unknown
	            hexString += "01000000";
	
	            // auto configuration script
	            hexString += String.format("%02X000000", autoConfigUrl.length());
	            hexString += Hex.encodeHexString(autoConfigUrl.getBytes("UTF-8"));
	
	            // finalisation of string
	            hexString += "41ee29087b79cf010000000000000000000000000100" +
	                              "0000020000000ac82a86000000000000000000000000000000" +
	                              "00000000000000000000000000000000000000000000000000" +
	                              "00000000000000000000000000000000000000000000000000" +
	                              "00000000000000000000000000000000000000000000000000" +
	                              "00000000000000000000000000000000000000000000000000" +
	                              "0000000000";
	        } catch (UnsupportedEncodingException e) {
	        }
	
	        byte[] data = javax.xml.bind.DatatypeConverter.parseHexBinary(hexString);
	        Advapi32Util.registrySetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "DefaultConnectionSettings", data);
	        Advapi32Util.registrySetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "SavedLegacySettings", data);
	
	        Tools.waitMs(5000);
	    ///////////////////////////////

	 */
	public void setSystemProxy(Proxy proxy) {
		String autoConfigUrl = proxy.getProxyAutoconfigUrl() == null ? "": proxy.getProxyAutoconfigUrl();
        String proxyUrl = proxy.getHttpProxy() == null ? "": proxy.getHttpProxy();
        String proxyExclusion = proxy.getNoProxy() == null ? "": proxy.getNoProxy();
		
		String hexString = "46000000"; // init
		hexString += Integer.toString(ThreadLocalRandom.current().nextInt(10, 100)) + "000000"; // counter
		if (proxy.getProxyType() == ProxyType.MANUAL) {
			hexString += "03000000";
		} else if (proxy.getProxyType() == ProxyType.DIRECT) {
			hexString += "01000000";
		} else if (proxy.getProxyType() == ProxyType.PAC) {
			hexString += "05000000";
		} else if (proxy.getProxyType() == ProxyType.AUTODETECT) {
			hexString += "09000000";
		} else {
			return;
		}
		
        try {
            // proxy
            hexString += String.format("%02X000000", proxyUrl.length());
            hexString += Hex.encodeHexString(proxyUrl.getBytes("UTF-8"));

            // proxy exclusion
            hexString += String.format("%02X000000", proxyExclusion.length());
            hexString += Hex.encodeHexString(proxyExclusion.getBytes("UTF-8"));

            // auto configuration script
            hexString += String.format("%02X000000", autoConfigUrl.length());
            hexString += Hex.encodeHexString(autoConfigUrl.getBytes("UTF-8"));
            if (proxy.getProxyType() == ProxyType.PAC) {
            	 // finalisation of string
	            hexString +=  "0100000000000000" +
	                          "0000000000000000" +
	                          "0000000000000000" +
	                          "0000000000000000";
            } else {
	
	            // finalisation of string
	            hexString +=  "0000000000000000" +
	                          "0000000000000000" +
	                          "0000000000000000" +
	                          "0000000000000000";
            }
        } catch (UnsupportedEncodingException e) {
        }

        byte[] data = DatatypeConverter.parseHexBinary(hexString);
        Advapi32Util.registrySetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "DefaultConnectionSettings", data);
//        Advapi32Util.registrySetBinaryValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\Connections", "SavedLegacySettings", data);

	}	
}
