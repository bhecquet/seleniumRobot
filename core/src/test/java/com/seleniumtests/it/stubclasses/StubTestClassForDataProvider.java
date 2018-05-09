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
package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.TestLogging;

/* test class for showing the issue #115
 * TODO: how to create a map with parameters, which is accessible from BeforeMethod / AfterMethod
 */
public class StubTestClassForDataProvider extends StubParentClass {

	@Test(groups={"stub"}, dataProvider = "data")
	public void testMethod(String data) {
		
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getAttribute("data"));
		SeleniumTestsContextManager.getThreadContext().setAttribute("data", data);
		TestLogging.log("data written: " + SeleniumTestsContextManager.getThreadContext().getAttribute("data"));
	}
	
	@BeforeMethod(groups={"stub"})
	public void before(Method method, ITestContext ctx, ITestResult res) {
		ctx.getAttributeNames();
		System.out.println("before: " + res.getMethod().getConstructorOrMethod().getMethod().getParameters().hashCode());
	}
	
	@DataProvider
	public Object[][] data(ITestContext testContext) {
		return new String[][] {new String[] {"data1"}, new String[] {"data2"}, new String[] {"data3"}};
	}
}
