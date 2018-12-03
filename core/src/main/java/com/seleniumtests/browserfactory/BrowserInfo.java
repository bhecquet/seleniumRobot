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
package com.seleniumtests.browserfactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class BrowserInfo {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(BrowserInfo.class);

	private static final Pattern REG_CHROME_VERSION = Pattern.compile(".*chrome-(\\d+)-(\\d+).*");
	private static final Pattern REG_ANDROID_VERSION = Pattern.compile(".*android-(\\d+\\.\\d+).*");
	public static final String LATEST_VERSION = "999.9";
	private static List<String> driverList;
	
	private String version;
	private String path;
	private String driverFileName;
	private String os;
	private BrowserType browser;
	private boolean driverFileSearched = false;
	
	/**
	 * Create information about the 
	 * @param browser
	 * @param version
	 * @param path				path to browser executable
	 * @param driverFileName
	 */
	public BrowserInfo(BrowserType browser, String version, String path) {
		this(browser, version, path, true);
	}
	
	/**
	 * 
	 * @param browser
	 * @param version
	 * @param path				path to browser executable
	 * @param check				do we check if browser path exists or not. Should not be used outside of tests
	 */
	public BrowserInfo(BrowserType browser, String version, String path, boolean check) {
		this.browser = browser;
		this.path = path;
		
		if (path != null && check && !Paths.get(path).toFile().exists()) {
			throw new ConfigurationException(String.format("browser file %s does not exists", path));
		}
		
		try {
			Float.parseFloat(version);
			this.version = version;
		} catch (NumberFormatException e) {
			logger.warn(String.format("Cannot parse browser version %s for browser", version, browser));
			this.version = "0.0";
		}
		
		os = OSUtility.getCurrentPlatorm().toString().toLowerCase();
	}
	
	public BrowserInfo getEmptyBrowserInfo() {
		BrowserInfo info = new BrowserInfo(BrowserType.NONE, "", null);
		info.driverFileSearched = true;
		info.driverFileName = null;
		return info;
	}

	private void addDriverFile() throws IOException {
		switch (browser) {
			case CHROME:
				addChromeDriverFile();
				break;
			case BROWSER:
				addAndroidDriverFile();
				break;
			case FIREFOX:
				addFirefoxDriverFile();
				break;
			case EDGE:
				addEdgeDriverFile();
				break;
			case INTERNET_EXPLORER:
				addInternetExplorerDriverFile();
				break;
			default:
				driverFileName = null;
		}
		checkResourceExists();
	}
	
	private void checkResourceExists() {
		if (driverFileName != null && getClass().getClassLoader()
				.getResourceAsStream(String.format("drivers/%s/%s%s", os, driverFileName, OSUtilityFactory.getInstance().getProgramExtension())) == null) {
			throw new ConfigurationException(String.format("Driver file %s does not exist in resources", driverFileName));
		}
	}
	
	/**
	 * SeleniumRobot uses external jar files to hold drivers (seleniumRobot-windows-driver, seleniumRobot-linux-driver,seleniumRobot-mac-driver)
	 * Each one also contains a file (driver-list-<windows/linux/mac>.txt) which references all driver files in the jar.
	 * This method returns the content of this text file
	 * @return
	 * @throws IOException 
	 */
	public String[] getDriverListFromJarResources(String driverListFileName) throws IOException {
		return IOUtils.readLines(BrowserInfo.class.getClassLoader().getResourceAsStream(driverListFileName), Charset.forName("UTF-8")).get(0).split(",");
	}
	
	public List<String> getDriverFiles() throws IOException {
		if (driverList != null) {
			return driverList;
		} else {
			// try to get file names from list generated by maven. In IDE mode, this should not work and we fall back to resource reading
			String driverListFileName = String.format("driver-list-%s.txt", OSUtility.getCurrentPlatorm().toString().toLowerCase());
			try {
				List<String> drivers = new ArrayList<>();
				
				String[] driverListFromFile = getDriverListFromJarResources(driverListFileName);
	
				logger.info(String.format("getting drivers from %s", driverListFileName));
		    	for (String driverNameWithPf: driverListFromFile) {
		    		if (!driverNameWithPf.startsWith(os)) {
		    			continue;
		    		}
		    		String driverName = driverNameWithPf.replace(os + "/", "");
		    		drivers.add(driverName);
		        }
		    	return drivers;
			} catch (NullPointerException | IOException e) {
				// issue #179: due to driver externalization in jars, it's not possible to read them from resources anymore
				throw new ConfigurationException(String.format("Could not read driver from resource file %s, make sure that the dependency seleniumRobot-%s-driver.jar is accessible to robot (in maven repo for developpers and in lib/ folder with classpath option for testers)", 
						driverListFileName,
						OSUtility.getCurrentPlatorm().toString().toLowerCase()));
			}
		}
	}
	
	/**
	 * Find the most suitable driver when using chrome browser
	 * @throws IOException
	 */
	private void addChromeDriverFile() throws IOException {
		List<String> driverFiles = getDriverFiles();
		driverFiles = driverFiles.stream()
				.filter(s -> s.contains("chrome-") && s.startsWith("chromedriver_"))
				.map(s -> s.replace(".exe", ""))
				.sorted()
				.collect(Collectors.toList());

		if (driverFiles.isEmpty()) {
			throw new ConfigurationException("no chromedriver in resources");
		}
		
		Map<Integer, String> driverVersion = new HashMap<>();
		
		for (String fileName: driverFiles) {
			Matcher matcher = REG_CHROME_VERSION.matcher(fileName);
			if (matcher.matches()) {
				int minVersion = Integer.parseInt(matcher.group(1));
				int maxVersion = Integer.parseInt(matcher.group(2));
				if (maxVersion < minVersion) {
					throw new ConfigurationException(String.format("Chrome driver file %s version is incorrect, max version should be > to min version", fileName));
				} else {
					for (int i = minVersion; i <= maxVersion; i++) {
						driverVersion.put(i, fileName);
					}
				}
			} else {
				throw new ConfigurationException(String.format("Driver %s is excluded as it does not match the pattern 'chromedriver_<version>_chrome-<minVersion>-<maxVersion>'", fileName));
			}
		}
		
		int chromeVersion = Integer.parseInt(version.split("\\.")[0]);
		driverFileName = driverVersion.get(chromeVersion);
		
		// when chrome version is greater than driver version, take the last driver and display warning as something may go wrong
		if (driverFileName == null && chromeVersion > Collections.max(driverVersion.keySet())) {
			driverFileName = driverFiles.get(driverFiles.size() - 1);
			
			logger.warn("--------------------------------------------------------------------");
			logger.warn(String.format("Chrome version %d does not have any associated driver, update seleniumRobot version, the latest driver has been used", chromeVersion));
			logger.warn("--------------------------------------------------------------------");
		} else if (driverFileName == null) {
			throw new ConfigurationException(String.format("Chrome version %d does not have any associated driver, update seleniumRobot version", chromeVersion));
		}
	}
	
	/**
	 * Add the most suitable chrome driver for android driver version
	 * @throws IOException 
	 */
	private void addAndroidDriverFile() throws IOException {
		List<String> driverFiles = getDriverFiles();
		driverFiles = driverFiles.stream()
								.filter(s -> s.contains("android-") && s.startsWith("chromedriver_"))
								.map(s -> s.replace(".exe", ""))
								.sorted()
								.collect(Collectors.toList());
		
		if (driverFiles.isEmpty()) {
			throw new ConfigurationException("no chromedriver in resources");
		}
		
		Map<String, String> driverVersion = new HashMap<>();
		
		for (String fileName: driverFiles) {
			Matcher matcher = REG_ANDROID_VERSION.matcher(fileName);
			if (matcher.matches()) {
				driverVersion.put(matcher.group(1), fileName);

			} else {
				throw new ConfigurationException(String.format("Driver %s is excluded as it does not match the pattern 'chromedriver_<version>_android-<x.y>'", fileName));
			}
		}
		
		driverFileName = driverVersion.get(version);
		
		if (driverFileName == null) {
			throw new ConfigurationException(String.format("Chrome version %s does not have any associated driver, update seleniumRobot version", version));
		}
	}
	
	/**
     * use firefox (return true) if version is below 48
     * @param versionString
     * @return
     */
    public static boolean useLegacyFirefoxVersion(String versionString) {
    	Pattern regMozilla = Pattern.compile(".*?(\\d+)\\..*");
    	Matcher versionMatcher = regMozilla.matcher(versionString);
		if (versionMatcher.matches()) {
			String version = versionMatcher.group(1);
			if (Integer.parseInt(version) < 48) {
				return true;
			}
		} 
		return false;
    }
	
	private void addFirefoxDriverFile() {
		if (!useLegacyFirefoxVersion(version)) {
			driverFileName = "geckodriver";
		}
	}
	
	/**
	 * Edge driver depends on windows build
	 */
	private void addEdgeDriverFile() {
		driverFileName = "MicrosoftWebDriver_" + version;
	}
	
	/**
	 * IE driver version depends on IE version itself
	 */
	private void addInternetExplorerDriverFile() {
		// assume windows is installed on C:
		
		
		if (Integer.parseInt(version) < 10) {
			driverFileName = "IEDriverServer_x64";
    	} else {
    		
    		// https://stackoverflow.com/questions/21458588/ie-tabs-are-not-running-in-64-bit-mode-even-after-enabling-enhanced-protected-mo
    		// https://blogs.msdn.microsoft.com/askie/2009/03/09/opening-a-new-tab-may-launch-a-new-process-with-internet-explorer-8-0/
    		// issue #147: key HKEY_CURRENT_USER\\Software\\Microsoft\\Internet Explorer\\Main\\TabProcGrowth controls the starting of processes in IE
    		// if not existing or if value is 1, a 32 bits process is started
    		// if value is 0, 64 bit process is started
    		String tabProcGrowth;
    		try {
    			tabProcGrowth = Advapi32Util.registryGetValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Internet Explorer\\Main", "TabProcGrowth").toString();
    		} catch (Win32Exception | NullPointerException e) {
    			tabProcGrowth = "1";
    		}
    		
    		if ("0".equals(tabProcGrowth)) {
    			driverFileName = "IEDriverServer_x64";
    			
    		// in all other cases, IE starts with mix 64 and 32 bits (2 processes) which needs 32 bit driver (https://github.com/SeleniumHQ/selenium-google-code-issue-archive/issues/5116#issuecomment-192106556)
    		} else {
    			driverFileName = "IEDriverServer_Win32";
    		}
    		
    	}
		
	}
	
	public String getVersion() {
		return version;
	}
	public String getPath() {
		return path;
	}

	public BrowserType getBrowser() {
		return browser;
	}

	public String getDriverFileName() {
		if (!driverFileSearched) {
			try {
				addDriverFile();
			} catch (IOException e) {
				logger.error("Cannot get driver file", e);
			} finally {
				driverFileSearched = true;
			}
		}
		return driverFileName;
	}
	
	/**
	 * @param driverFileName
	 */
	public void setDriverFileName(String driverFileName) {
		this.driverFileName = driverFileName;
		driverFileSearched = true;
	}

	public static List<String> getDriverList() {
		return driverList;
	}

	public static void setDriverList(List<String> driverList) {
		BrowserInfo.driverList = driverList;
	}
	
	/**
	 * get 
	 * @return
	 */
    public List<Long> getProgramPid(String programName, List<Long> existingPids) {
    	final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        
        OSUtility osUtility = OSUtilityFactory.getInstance();
        
        if(index == 0) {
        	return new ArrayList<>();
        }
        try {
            String processId = Long.toString(Long.parseLong(jvmName.substring(0, index)));
            List<Long> driverProcesses = osUtility.getChildProcessPid(Long.parseLong(processId), programName + osUtility.getProgramExtension(), existingPids);
            return driverProcesses;
        } catch (Exception e) {
        	logger.warn("could not get driver pid", e);
        	return new ArrayList<>();
        }
    }
    
    /**
     * Returns the list of pids for existing drivers and browsers. These are direct programes launched by seleniumRobot
     * @param existingPids
     * @return
     */
    public List<Long> getDriverAndBrowserPid(List<Long> existingPids) {
    	
    	List<Long> pids = new ArrayList<>();
    	
    	// no driver used to connect to browser
    	if ((browser == BrowserType.FIREFOX && driverFileName == null) || browser == BrowserType.SAFARI) {
    		pids.addAll(getBrowserPid(existingPids));
    	} else {
    		pids.addAll(getDriverPid(existingPids));
    	}
    	
    	return pids;
    }
    
    /**
     * Get the driver process created by selenium. 
     * @param existingPids
     * @return
     */
    public List<Long> getDriverPid(List<Long> existingPids) {
    	return getProgramPid(driverFileName, existingPids);
    }
    
    public List<Long> getBrowserPid(List<Long> existingPids) {
    	return getProgramPid(browser.getBrowserType().substring(1) + OSUtilityFactory.getInstance().getProgramExtension(), existingPids);
    }
    
    /**
     * Get the list of pids for the browser launched by driver and all subprocess created by browser
     * @param driverPid
     * @return
     */
    public List<Long> getAllBrowserSubprocessPids(List<Long> driverPids) {
    	OSUtility osUtility = OSUtilityFactory.getInstance();
    	List<Long> allPids = new ArrayList<>(driverPids);
    	
    	try {
    		for (Long driverPid: driverPids) {
    			List<Long> browserPids = osUtility.getChildProcessPid(driverPid, null, new ArrayList<>());
    			allPids.addAll(browserPids);
    			for (Long browserPid: browserPids) {
    				List<Long> subBrowserPids = osUtility.getChildProcessPid(browserPid, null, new ArrayList<>());
    				allPids.addAll(subBrowserPids);
    			}
    		}
    		return allPids;
    		
    		
		} catch (IOException e) {
			return new ArrayList<>();
		}
    }

	public boolean isDriverFileSearched() {
		return driverFileSearched;
	}
	
	/**
	 * Returns the BrowserInfo with highest version
	 * @return
	 */
	public static BrowserInfo getHighestDriverVersion(List<BrowserInfo> browserInfos) {
		
		BrowserInfo highest = null;
		
		for (BrowserInfo browserInfo: browserInfos) {
			if (browserInfo == null) {
				continue;
			}
			
			if (highest == null || Float.parseFloat(browserInfo.getVersion()) > Float.parseFloat(highest.getVersion())) {
				highest = browserInfo;
			}
		}
		return highest;
	}
	
	/**
	 * returns the accurate BrowserInfo according to the expected version. If no version matches, raise a ConfigurationException
	 * @param version
	 * @return
	 * @throws ConfigurationException  if no browserinfo matches
	 */
	public static BrowserInfo getInfoFromVersion(String version, List<BrowserInfo> browserInfos) {
		for (BrowserInfo browserInfo: browserInfos) {
			if (browserInfo != null && browserInfo.getVersion() != null && browserInfo.getVersion().equals(version)) {
				return browserInfo;
			}
		}
		throw new ConfigurationException(String.format("Browser is not installed in version %s", version));
	}
	
	/**
	 * returns the accurate BrowserInfo according to the expected binary. If no matches, raise a ConfigurationException
	 * Also check that file exists
	 * @param version
	 * @return
	 * @throws ConfigurationException  if no browserinfo matches
	 */
	public static BrowserInfo getInfoFromBinary(String binPath, List<BrowserInfo> browserInfos) {
		for (BrowserInfo browserInfo: browserInfos) {
			if (browserInfo != null && browserInfo.getPath() != null && browserInfo.getPath().equals(binPath)) {
				return browserInfo;
			}
		}
		throw new ConfigurationException(String.format("Browser is not present at %s", binPath));
	}
}
