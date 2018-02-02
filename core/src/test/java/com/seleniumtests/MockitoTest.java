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
package com.seleniumtests;

import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Redefine calls to PowerMockTestCase methods as they are not called when using TestNG groups
 * we MUST mark them as "alwaysRun"
 * @author behe
 *
 */

public class MockitoTest  extends PowerMockTestCase {

	protected static final String SERVER_URL = "http://localhost:4321";

	@BeforeMethod(groups={"ut", "it"})  
	public void beforeMethod(final ITestContext testNGCtx) throws Exception {
		beforePowerMockTestMethod();
		initThreadContext(testNGCtx);
		MockitoAnnotations.initMocks(this); 
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx, null);
	}
	
	public void initThreadContext(final ITestContext testNGCtx,  final String testName) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, testName);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
	}
	
	@BeforeClass(groups={"ut", "it"})  
	public void beforeClass() throws Exception {
		beforePowerMockTestClass();
	}
	
	@AfterMethod(groups={"ut", "it"})
	public void afterMethod() throws Exception {
		afterPowerMockTestMethod();
	}
	
	@AfterClass(groups={"ut", "it"})
	public void afterClass() throws Exception {
		afterPowerMockTestClass();
	}
	
}
