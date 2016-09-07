/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.driver.DriverConfig;

public class TestFirefoxDriverFactory extends GenericTest {

	@Test(groups={"ut"})
	public void testVersionChoice_v47() {
		StringBuilder versionString = new StringBuilder("Mozilla Firefox 47.0");
		Assert.assertEquals(new FirefoxDriverFactory(new DriverConfig()).useFirefoxVersion(versionString), false);
	}
	
	@Test(groups={"ut"})
	public void testVersionChoice_v46() {
		StringBuilder versionString = new StringBuilder("Mozilla Firefox 46.0");
		Assert.assertEquals(new FirefoxDriverFactory(new DriverConfig()).useFirefoxVersion(versionString), true);
	}
	
	@Test(groups={"ut"})
	public void testVersionChoice_debian() {
		StringBuilder versionString = new StringBuilder("Mozilla Iceweasel 38.8.0");
		Assert.assertEquals(new FirefoxDriverFactory(new DriverConfig()).useFirefoxVersion(versionString), true);
	}
	
}
