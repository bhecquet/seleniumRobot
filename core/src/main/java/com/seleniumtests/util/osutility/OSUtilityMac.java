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

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;

public class OSUtilityMac extends OSUtilityUnix {
	
	@Override
	public Map<BrowserType, BrowserInfo> discoverInstalledBrowsersWithVersion() {
		Map<BrowserType, BrowserInfo> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, new BrowserInfo(BrowserType.HTMLUNIT, "latest", null));
		browserList.put(BrowserType.PHANTOMJS, new BrowserInfo(BrowserType.PHANTOMJS, "latest", null));
		
		// safari is always installed on mac os
		browserList.put(BrowserType.SAFARI, new BrowserInfo(BrowserType.SAFARI, "latest", null));
		
		if (new File("/Applications/Google Chrome.app").isDirectory()) {
//			String version = OSCommand.executeCommandAndWait(new String[] {"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--version"});
			String version = getChromeVersion("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
			browserList.put(BrowserType.CHROME, new BrowserInfo(BrowserType.CHROME, extractChromeVersion(version), "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"));
		}
		if (new File("/Applications/Firefox.app").isDirectory()) {
//			String version = OSCommand.executeCommandAndWait("/Applications/Firefox.app/Contents/MacOS/firefox --version | more");
			String version = getFirefoxVersion("/Applications/Firefox.app/Contents/MacOS/firefox");
			browserList.put(BrowserType.FIREFOX, new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), "/Applications/Firefox.app/Contents/MacOS/firefox"));
		}
		
		return browserList;
	}
}
