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
package com.seleniumtests.ut.util.osutility;

import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class TestWindowsOsUtility extends GenericTest {
	
	@BeforeClass(groups={"ut"})
	public void isWindows() {
		if (!OSUtility.isWindows()) {
			throw new SkipException("Test only available on Windows platform");
		}
	}

	@Test(groups={"ut"})
	public void testGetBuild() {
		Assert.assertNotEquals(OSUtilityFactory.getInstance().getOSBuild(), 5000);
	}
	
	@Test(groups={"ut"})
	public void testBrowserList() {
		List<BrowserType> browsers = OSUtilityFactory.getInstance().getInstalledBrowsers();
		Assert.assertTrue(browsers.contains(BrowserType.INTERNET_EXPLORER));
		
		if (SystemUtils.IS_OS_WINDOWS_10) {
			Assert.assertTrue(browsers.contains(BrowserType.EDGE));
		}
	}
}
