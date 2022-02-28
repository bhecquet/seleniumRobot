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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;

public class OSUtilityMac extends OSUtilityUnix {
	
	@Override
	public Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion(boolean discoverBetaBrowsers) {
		Map<BrowserType, List<BrowserInfo>> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, Arrays.asList(new BrowserInfo(BrowserType.HTMLUNIT, BrowserInfo.LATEST_VERSION, null)));
		
		// safari is always installed on mac os
		browserList.put(BrowserType.SAFARI, Arrays.asList(new BrowserInfo(BrowserType.SAFARI, BrowserInfo.LATEST_VERSION, null)));
		
		// TODO: handle multiple installation of Chrome and Firefox. But how to do that without specifying the location
		if (new File("/Applications/Google Chrome.app").isDirectory()) {
			String version = getChromeVersion("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
			browserList.put(BrowserType.CHROME, Arrays.asList(new BrowserInfo(BrowserType.CHROME, extractChromeVersion(version), "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")));
		}
		if (new File("/Applications/Firefox.app").isDirectory()) {
			String version = getFirefoxVersion("/Applications/Firefox.app/Contents/MacOS/firefox");
			browserList.put(BrowserType.FIREFOX, Arrays.asList(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), "/Applications/Firefox.app/Contents/MacOS/firefox")));
		}
		
		return browserList;
	}
}
