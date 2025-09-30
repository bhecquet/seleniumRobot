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
package com.seleniumtests.browserfactory.mobile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seleniumtests.util.osutility.SystemUtility;
import org.apache.logging.log4j.Logger;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSCommand;

public class AdbWrapper {

	public static final String SHELL = "shell";
	private String adbVersion;
	private String adbCommand;
	private static final Pattern DEVICE_PATTERN = Pattern.compile("^(.+?)\\s+device$");
	private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\[ro\\.build\\.version\\.release\\]: \\[(.+?)\\].*");
	private static final Pattern NAME_PATTERN = Pattern.compile(".*\\[ro\\.product\\.model\\]: \\[(.+?)\\].*");
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(AdbWrapper.class);
	
	private static final Pattern REG_BROWSER_VERSION_NAME = Pattern.compile("versionName=(.+?)-.*");
	private static final Pattern REG_CHROME_VERSION_NAME = Pattern.compile("versionName=(\\d++\\.\\d++).*");
	
	public AdbWrapper() {
		checkInstallation();
	}

	/**
	 * Checks that ADB is installed
	 * Search for an environment variable ANDROID_HOME / ANDROID_SDK_ROOT
	 * If found, use the adb inside that directory
	 * Else, try to run ADB directly
	 * @throws ConfigurationException when ADB is not found
	 */
	private void checkInstallation() {
		
		try {
			checkInstallation("ANDROID_HOME");
		} catch (ConfigurationException e) {
			logger.warn(e.getMessage());
			checkInstallation("ANDROID_SDK_ROOT");
		}
		logger.info("ADB found");
		
	}
	private void checkInstallation(String androidEnvVarName) {
		String androidHome = SystemUtility.getenv(androidEnvVarName);
		if (androidHome != null) {
			adbCommand = Paths.get(androidHome, "platform-tools", "adb").toString();
		} else {
			adbCommand = "adb";
		}
		
		// check command is OK
		try {
			String reply = OSCommand.executeCommandAndWait(new String[] {adbCommand, "version"});
			adbVersion = reply.split("\n")[0].trim().replace("Android Debug Bridge version", "").trim();
		} catch (CustomSeleniumTestsException e) {
			adbVersion = "";
		}
		
		if (!adbVersion.matches(".*\\d\\.\\d.*")) {
			throw new ConfigurationException(String.format("ADB does not seem to be installed, is environment variable %s set ?", androidEnvVarName));
		}
	}
	
	/**
	 * Returns a device list from ADB using the commands
	 * adb devices -l => get list of connected devices
	 * adb -s <id> shell getprop  => get specific properties
	 * @return	device list
	 */
	public List<MobileDevice> getDeviceList() {
		List<MobileDevice> devices = new ArrayList<>();
		for (String deviceId: getDeviceIds()) {
			
			String deviceName = "";
			String osVersion = "";
			
			String reply = OSCommand.executeCommandAndWait(new String[] {adbCommand, "-s", deviceId, SHELL, "getprop"}).replace("\n", "").replace("\r", "");
			Matcher matcherName = NAME_PATTERN.matcher(reply);
			if (matcherName.matches()) {
				deviceName = matcherName.group(1);
			}
			Matcher matcherVersion = VERSION_PATTERN.matcher(reply);
			if (matcherVersion.matches()) {
				osVersion = matcherVersion.group(1);
			}

			if (deviceName.isEmpty() || osVersion.isEmpty()) {
				logger.warn("device with id {} could not be parsed, device or version not found", deviceId);
			} else {
				devices.add(new MobileDevice(deviceName, deviceId, "android", osVersion, getInstalledBrowsers(deviceId)));
			}					
		}
		return devices;
	}
	
	private List<BrowserInfo> getInstalledBrowsers(String deviceId) {
		List<BrowserInfo> browsers = new ArrayList<>();
		String reply = OSCommand.executeCommandAndWait(new String[] {adbCommand, "-s", deviceId, SHELL, "\"pm list packages\""});
		
		for (String line: reply.split("\n")) {
			if (line.contains("package:com.android.chrome")) {
				String chromeVersion = OSCommand.executeCommandAndWait(new String[] {adbCommand, "-s", deviceId, SHELL, "\"dumpsys package com.android.chrome | grep versionName\""});
				Matcher versionMatcher = REG_CHROME_VERSION_NAME.matcher(chromeVersion.split("\\n")[0].trim());
				if (versionMatcher.matches()) {
					browsers.add(new BrowserInfo(BrowserType.CHROME, versionMatcher.group(1), null));
				} else {
					logger.error("Cannot parse chrome version {}", chromeVersion);
					browsers.add(new BrowserInfo(BrowserType.CHROME, chromeVersion, null));
				}
			}
			if (line.contains("package:com.android.browser")) {
				String androidVersion = OSCommand.executeCommandAndWait(new String[] {adbCommand, "-s", deviceId, SHELL, "\"dumpsys package com.android.browser | grep versionName\""});
				Matcher versionMatcher = REG_BROWSER_VERSION_NAME.matcher(androidVersion.split("\\n")[0].trim());
				if (versionMatcher.matches()) {
					browsers.add(new BrowserInfo(BrowserType.BROWSER, versionMatcher.group(1), null));
				} else {
					logger.error("Cannot parse android browser version {}", androidVersion);
					browsers.add(new BrowserInfo(BrowserType.BROWSER, androidVersion, null));
				}
			}
		}
		return browsers;
	}
	
	/**
	 * Returns the list of device ids using command 'adb devices'
	 * @return list of device ids
	 */
	private List<String> getDeviceIds() {
		List<String> deviceIds = new ArrayList<>();
		String reply = OSCommand.executeCommandAndWait(new String[] {adbCommand, "devices"});
		
		for (String line: reply.split("\n")) {
			Matcher matcher = DEVICE_PATTERN.matcher(line.trim());
			if (matcher.matches()) {
				deviceIds.add(matcher.group(1));
			}
		}
		return deviceIds;
	}
	

	public String getAdbVersion() {
		return adbVersion;
	}
}
