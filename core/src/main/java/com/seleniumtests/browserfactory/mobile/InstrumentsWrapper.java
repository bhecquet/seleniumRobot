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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Platform;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

/**
 * Class for managing iOS devices
 * @author behe
 *
 */
public class InstrumentsWrapper {
	
	// xcrun xctrace list devices
	
	// examples
	// Apple TV 1080p (10.2) [6444F65D-DA15-4505-8307-4520FD346ACE] (Simulator)
	// Mac mini [CBFA063D-2535-5FD8-BA05-CE8D3683D6BA]
	private static final Pattern REG_DEVICE = Pattern.compile("([a-zA-Z0-9 ]+).*?\\((\\d+\\.\\d+\\.?\\d?)\\).*?\\[(.*?)\\].*");
		

	public InstrumentsWrapper() {
		checkInstallation();
	}
	
	private void checkInstallation() {
		if (OSUtility.getCurrentPlatorm() != Platform.MAC) {
			throw new ConfigurationException("InstrumentsWrapper can only be used on Mac");
		}
		
		String out = OSCommand.executeCommandAndWait("xcrun");
		if (!out.contains("xcrun [options]")) {
			throw new ConfigurationException("xcrun is not installed");
		}
	}
	
	/**
	 * Use 'instruments -s devices' to know which devices are available for test
	 */
	public List<MobileDevice> parseIosDevices() {
		String output = OSCommand.executeCommandAndWait("xcrun simctl list devices available --json");
		List<MobileDevice> devList = new ArrayList<>();
		
		JSONObject devicesJson = new JSONObject(output);
		
		for (String family: devicesJson.getJSONObject("devices").keySet()) {
			if (!family.contains("iOS")) {
				continue;
			}
			String version = family.replace("com.apple.CoreSimulator.SimRuntime.iOS-", "").replace("-", ".");
			JSONArray familyDevices = devicesJson.getJSONObject("devices").getJSONArray(family);
			for (Object device: familyDevices.toList()) {
				MobileDevice dev = new MobileDevice(((Map<String, String>)device).get("name").split("\\(")[0].trim(), 
						((Map<String, String>)device).get("udid"), 
						"iOS", 
						version, 
						Arrays.asList(new BrowserInfo(BrowserType.SAFARI, "latest", null)));
				devList.add(dev);
			}
		}
		
		/*for (String line: output.split("\n")) {
			line = line.trim();
			Matcher matcher = REG_DEVICE.matcher(line);

			if (matcher.matches()) {
				MobileDevice dev = new MobileDevice(matcher.group(1).trim(), 
						matcher.group(3), 
						"iOS", 
						matcher.group(2), 
						Arrays.asList(new BrowserInfo(BrowserType.SAFARI, "latest", null)));
				devList.add(dev);
			}
		}*/
		return devList;
	}
}
