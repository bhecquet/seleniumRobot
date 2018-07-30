/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.ut.browserfactory;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.driver.DriverConfig;

public class TestFirefoxDriverFactory extends GenericTest {

	@Test(groups={"ut"})
	public void testVersionChoice_v48() {
		String versionString = "48.0";
		Assert.assertEquals(BrowserInfo.useLegacyFirefoxVersion(versionString), false);
	}
	
	@Test(groups={"ut"})
	public void testVersionChoice_v47() {
		String versionString = "47.0";
		Assert.assertEquals(BrowserInfo.useLegacyFirefoxVersion(versionString), true);
	}
	
}
