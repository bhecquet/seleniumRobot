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

package com.seleniumtests.ut.core.config;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.config.ConfigReader;

public class TestConfigReader extends GenericTest {

	@Test(groups={"ut"})
	public void readConfigurationWithValueOverride() {
		HashMap<String, String> config = new ConfigReader().readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/config.ini"), "Dev");
		Assert.assertEquals(config.get("key1"), "value4", "Key override does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationWithoutValueOverride() {
		HashMap<String, String> config = new ConfigReader().readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/config.ini"), "VNR");
		Assert.assertEquals(config.get("key1"), "value1", "Key should not be overriden");
	}
	
	@Test(groups={"ut context"})
	public void mergeIniAndXmlConfiguration(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx, xmlTest);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		
<<<<<<< HEAD
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key6"), "value6", "Value has not been get from xml file");
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key2"), "value20", "Value has not been get from xml file");
	}	
=======
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("variable1"), "value1", "Value has not been get from xml file");
	}
	
>>>>>>> 8ed2adcc6ea7badd7ea92afe80d0a67e8cfed073
}
