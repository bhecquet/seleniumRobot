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
package com.seleniumtests;

import java.lang.reflect.Method;

import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.reporter.logger.TestLogging;

/**
 * Redefine calls to PowerMockTestCase methods as they are not called when using TestNG groups
 * we MUST mark them as "alwaysRun"
 * @author behe
 *
 */

public class MockitoTest  extends PowerMockTestCase {

	protected static final String SERVER_URL = "http://localhost:4321";

	@BeforeMethod(groups={"ut", "it"})  
	public void beforeMethod(final Method method, final ITestContext testNGCtx, final ITestResult testResult) throws Exception {
		doBeforeMethod(method);
		beforePowerMockTestMethod();
		initThreadContext(testNGCtx, null, testResult);
		MockitoAnnotations.initMocks(this); 
	}
	
	/**
	 * Something to do at the beginning of \@BerforeMethod in case this MUST be done before context initialization
	 */
	protected void doBeforeMethod(final Method method) {
		// do nothing
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx, null, null);
	}
	
	public void initThreadContext(final ITestContext testNGCtx,  final String testName, final ITestResult testResult) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, testName, null, testResult);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
	}
	
	@BeforeClass(groups={"ut", "it"})  
	public void beforeClass() throws Exception {
		beforePowerMockTestClass();
	}
	
	@AfterMethod(groups={"ut", "it"})
	public void afterMethod() throws Exception {
		afterPowerMockTestMethod();

		TestLogging.reset();
	}
	
	@AfterClass(groups={"ut", "it"})
	public void afterClass() throws Exception {
		afterPowerMockTestClass();
	}
	
	public void myTest() {
		
	}
	
}
