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
import java.util.EnumMap;
import java.util.Map;

import com.seleniumtests.driver.BrowserType;

public class OSUtilityMac extends OSUtilityUnix {
	
	@Override
	public Map<BrowserType, String> getInstalledBrowsersWithVersion() {
		Map<BrowserType, String> browserList = new EnumMap<>(BrowserType.class);
		
		// safari is always installed on mac os
		browserList.put(BrowserType.SAFARI, "latest");
		
		if (new File("/Applications/Google Chrome.app").isDirectory()) {
			String version = OSCommand.executeCommandAndWait("\"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome\" --version");
			browserList.put(BrowserType.CHROME, extractChromeVersion(version));
		}
		if (new File("/Applications/Firefox.app").isDirectory()) {
			String version = OSCommand.executeCommandAndWait("/Applications/Firefox.app/Contents/MacOS/firefox --version | more");
			browserList.put(BrowserType.FIREFOX, extractFirefoxVersion(version));
		}
		
		return browserList;
	}
}
