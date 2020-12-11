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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class StubTestClassForVideoTest extends StubParentClass {
	

	@DataProvider(name = "data")
	public Object[][] data(ITestContext testContext) {
		return new String[][] {new String[] {"data1"}, new String[] {"data2"}};
	}
	

	@BeforeMethod(groups="video")
	public void initVideo(Method method) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		SeleniumTestsContextManager.getThreadContext().setTestRetryCount(1);
		new DriverTestPage(true); // starts driver in Before Method
	}

	@Test(groups={"video"}, dataProvider = "data")
	public void testDriverShortWithDataProvider(String data) throws Exception {
		new DriverTestPage(true);
	}
	
	@Test(groups={"video"})
	public void testDriverShortKo() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
			._writeSomethingOnNonExistentElement();
	}
}
