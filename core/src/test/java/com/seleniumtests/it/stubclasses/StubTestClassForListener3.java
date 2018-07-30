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
package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;


public class StubTestClassForListener3 extends StubParentClass {

	@Test(groups="stub3", dataProvider = "data")
	public void test1Listener3(String data) {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method exec", "test1Listener3");
		SeleniumTestsContextManager.getThreadContext().setAttribute(data, "data");

	}
	
	@DataProvider
	public Object[][] data(ITestContext testContext) {
		return new String[][] {new String[] {"data1"}, new String[] {"data2"}};
	}

	@AfterMethod(groups={"stub3"})
	public void afterMethod(Method method) {
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method exec"), method.getName());
		
		// check that when test start, it takes a fresh value of the context when using data provider
		Assert.assertTrue((SeleniumTestsContextManager.getThreadContext().getAttribute("data1") == null 
							&& SeleniumTestsContextManager.getThreadContext().getAttribute("data2") != null)
						  || (SeleniumTestsContextManager.getThreadContext().getAttribute("data1") != null 
							&& SeleniumTestsContextManager.getThreadContext().getAttribute("data2") == null));
	}
	
	@AfterTest
	public void afterTest(XmlTest xmlTest) {
		// check we get a context (no exception raised)
		Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext());
	}
	
	@AfterClass
	public void afterClass() {
		// check we get a context (no exception raised)
		Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext());
	}

	@AfterGroups(groups={"stub3"})
	public void afterGroup() {
		// check we get a context (no exception raised)
		Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext());
	}
}
