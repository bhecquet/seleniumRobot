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
package com.seleniumtests.browserfactory.mobile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSCommand;

public class AdbWrapper {

	private String adbVersion;
	private String adbCommand;
	private static Pattern devicePattern = Pattern.compile("^(.*?)\\s+device$");
	private static Pattern versionPattern = Pattern.compile(".*\\[ro\\.build\\.version\\.release\\]: \\[(.*?)\\].*");
	private static Pattern namePattern = Pattern.compile(".*\\[ro\\.product\\.model\\]: \\[(.*?)\\].*");
	
	private static Logger logger = SeleniumRobotLogger.getLogger(AdbWrapper.class);
	
	public AdbWrapper() {
		checkInstallation();
	}

	/**
	 * Checks that ADB is installed
	 * Search for an environment variable ANDROID_HOME
	 * If found, use the adb inside that directory
	 * Else, try to run ADB directly
	 * @throws ConfigurationException when ADB is not found
	 * @return
	 */
	private void checkInstallation() {
		String androidHome = System.getenv("ANDROID_HOME");
		if (androidHome != null) {
			adbCommand = Paths.get(androidHome, "platform-tools", "adb").toString();
		} else {
			adbCommand = "adb";
		}
		
		// check command is OK
		String reply = OSCommand.executeCommandAndWait(adbCommand + " version");
		adbVersion = reply.split("\n")[0].trim().replace("Android Debug Bridge version", "").trim();
		
		if (!adbVersion.matches(".*\\d\\.\\d.*")) {
			throw new ConfigurationException("ADB does not seem to be installed, is environment variable ANDROID_HOME set ?");
		}
	}
	
	/**
	 * Returns a device list from ADB using the commands
	 * adb devices -l => get list of connected devices
	 * adb -s <id> shell getprop  => get specific properties
	 * @return
	 */
	public List<MobileDevice> getDeviceList() {
		List<MobileDevice> devices = new ArrayList<>();
		for (String deviceId: getDeviceIds()) {
			
			String deviceName = "";
			String osVersion = "";
			
			String reply = OSCommand.executeCommandAndWait(String.format("%s -s %s shell getprop", adbCommand, deviceId)).replace("\n", "").replace("\r", "");
			Matcher matcherName = namePattern.matcher(reply);
			if (matcherName.matches()) {
				deviceName = matcherName.group(1);
			}
			Matcher matcherVersion = versionPattern.matcher(reply);
			if (matcherVersion.matches()) {
				osVersion = matcherVersion.group(1);
			}

			if (deviceName.isEmpty() || osVersion.isEmpty()) {
				logger.warn(String.format("device with id %s could not be parsed, device or version not found", deviceId));
			} else {
				devices.add(new MobileDevice(deviceName, deviceId, "android", osVersion, getInstalledBrowsers(deviceId)));
			}					
		}
		return devices;
	}
	
	private List<String> getInstalledBrowsers(String deviceId) {
		List<String> browsers = new ArrayList<>();
		String reply = OSCommand.executeCommandAndWait(String.format("%s -s %s shell \"pm list packages\"", adbCommand, deviceId));
		
		for (String line: reply.split("\n")) {
			if (line.contains("package:com.android.chrome")) {
				browsers.add("chrome");
			}
			if (line.contains("package:com.android.browser")) {
				browsers.add("browser");
			}
		}
		return browsers;
	}
	
	/**
	 * Returns the list of device ids using command 'adb devices'
	 * @return
	 */
	private List<String> getDeviceIds() {
		List<String> deviceIds = new ArrayList<>();
		String reply = OSCommand.executeCommandAndWait(adbCommand + " devices");
		
		for (String line: reply.split("\n")) {
			Matcher matcher = devicePattern.matcher(line);
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
