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
package com.seleniumtests.ut.core;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

/**
 * The first 4 tests will fail when executed directly because they use the tu.xml specific configurations
 * @author behe
 *
 */
public class TestSeleniumTestContextManagerWithoutInit {
	
	/**
	 * Check that global context gets parameters from XML file
	 * @param testContext
	 */
	@Test(groups= {"ut context"}) 
	public void testGlobalContextGetsParametersFromXmlFile(ITestContext testContext) {
		try {
			SeleniumTestsContextManager.initGlobalContext(testContext.getSuite()); 
			SeleniumTestsContext context = SeleniumTestsContextManager.getGlobalContext();
			context.setSoftAssertEnabled(false); 
			Assert.assertEquals(context.getApp(), "myMobileApp.apk");
		} finally {
			SeleniumTestsContextManager.setGlobalContext(null);
		}
	}
	
	@Test(groups= {"ut context"}, expectedExceptions=ConfigurationException.class)
	public void testGlobalContextMustBeInit() {
		SeleniumTestsContextManager.setGlobalContext(null);
		SeleniumTestsContextManager.getGlobalContext();
	}
	
	@Test(groups= {"ut context"}, expectedExceptions=ConfigurationException.class)
	public void testThreadContextMustBeInit() {
		SeleniumTestsContextManager.setThreadContext(null);
		SeleniumTestsContextManager.getThreadContext();
	}
}
