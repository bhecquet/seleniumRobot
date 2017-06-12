package com.seleniumtests.browserfactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class BrowserInfo {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(BrowserInfo.class);

	private static final Pattern REG_CHROME_VERSION = Pattern.compile(".*chrome-(\\d+)-(\\d+).*");
	private static final Pattern REG_ANDROID_VERSION = Pattern.compile(".*android-(\\d+\\.\\d+).*");
	
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
		this.browser = browser;
		this.path = path;
		this.version = version;
		os = OSUtility.getCurrentPlatorm().toString().toLowerCase();		
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
	
	private List<String> getDriverFiles() throws IOException {
		return IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(String.format("drivers/%s/", os)), Charsets.UTF_8);
	}
	
	/**
	 * Find the most suitable driver when using chrome browser
	 * @throws IOException
	 */
	private void addChromeDriverFile() throws IOException {
		List<String> driverFiles = getDriverFiles();
		driverFiles = driverFiles.stream()
				.filter(s -> s.contains("chrome-") && s.startsWith("chromedriver_"))
				.map(FilenameUtils::removeExtension)
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
								.map(FilenameUtils::removeExtension)
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
	
	private void addFirefoxDriverFile() {
		driverFileName = "geckodriver";
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
		if (Integer.parseInt(version) < 10) {
			driverFileName = "IEDriverServer_x64";
    	} else {
    		driverFileName = "IEDriverServer_Win32";
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
	 * For test only
	 * @param driverFileName
	 */
	public void setDriverFileName(String driverFileName) {
		this.driverFileName = driverFileName;
		driverFileSearched = true;
	}

}
