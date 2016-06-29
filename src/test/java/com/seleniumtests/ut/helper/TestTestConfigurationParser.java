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

package com.seleniumtests.ut.helper;

import java.io.File;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.util.TestConfigurationParser;

public class TestTestConfigurationParser {
	
	private static TestConfigurationParser configParser;
	
	@BeforeClass(groups={"ut"})
	public void init(ITestContext testNGCtx) {
		File suiteFile = new File(testNGCtx.getSuite().getXmlSuite().getFileName());
        String configFile = suiteFile.getPath().replace(suiteFile.getName(), "") + testNGCtx.getSuite().getParameter("testConfig");
		configParser = new TestConfigurationParser(configFile);
	}

	@Test(groups={"ut"})
	public void testParameterNodes() {
		Assert.assertEquals(configParser.getParameterNodes().size(), 10);
	}
	
	@Test(groups={"ut"})
	public void testServiceNodes() {
		Assert.assertEquals(configParser.getServiceNodes().size(), 4);
	}
	
	@Test(groups={"ut"})
	public void testDeviceNodes() {
		Assert.assertEquals(configParser.getDeviceNodes().size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testDeviceNodesJson() {
		Assert.assertEquals(configParser.getDeviceNodesAsJson(), "{\"Samsung Galaxy Nexus SPH-L700 4.3\":\"Android 4.3\",\"Android Emulator\":\"Android 5.1\"}");
	}
}
