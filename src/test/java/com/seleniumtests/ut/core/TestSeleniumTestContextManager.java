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

package com.seleniumtests.ut.core;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class TestSeleniumTestContextManager {

	/**
	 * Test reading of extended configuration (referenced by testConfig parameter in suite file)
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void readExtendedConfiguration(ITestContext iTestContext) {
		iTestContext = SeleniumTestsContextManager.getContextFromConfigFile(iTestContext);
		
		Assert.assertEquals(iTestContext.getSuite().getXmlSuite().getParameter(SeleniumTestsContext.DEVICE_LIST), "{\"Samsung Galaxy Nexus SPH-L700 4.3\":\"Android 4.3\",\"Android Emulator\":\"Android 5.1\"}");
		Assert.assertEquals(iTestContext.getSuite().getXmlSuite().getParameter(SeleniumTestsContext.APPIUM_SERVER_URL), "http://localhost:4723/wd/hub");
		Assert.assertEquals(iTestContext.getSuite().getXmlSuite().getParameter(SeleniumTestsContext.APP_PACKAGE), "com.infotel.mobile");
	}
	
	/**
	 * A value defined in suite and in extended configuration will be get from extended configuration
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void extendedConfigurationOverridesSuiveValue(ITestContext iTestContext) {
		iTestContext = SeleniumTestsContextManager.getContextFromConfigFile(iTestContext);
		
		Assert.assertEquals(iTestContext.getSuite().getXmlSuite().getParameter(SeleniumTestsContext.BROWSER), "chrome");
	}
	
	
	
}
