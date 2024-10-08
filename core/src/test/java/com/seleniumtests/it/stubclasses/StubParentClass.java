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
package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumTestPlan;
import com.seleniumtests.util.helper.WaitHelper;

public class StubParentClass extends SeleniumTestPlan {
	
	/**
	 * Generate context to have logger correctly initialized
	 */
	@BeforeSuite(groups="stub")
	public void initSuite() {
		if (System.getProperty(SeleniumTestsContext.VIDEO_CAPTURE) == null) {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "false");
		}
	}
	
	@AfterSuite(groups="stub")
	public void resetSuite() {
		System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
	}
	
	/**
	 * Slow down test so that they are ordered correctly in SeleniumRobotTestListener.onFinish()
	 */
	@BeforeMethod(groups="stub")
	public void slow(Method method) {
		WaitHelper.waitForMilliSeconds(10);
	}
}
