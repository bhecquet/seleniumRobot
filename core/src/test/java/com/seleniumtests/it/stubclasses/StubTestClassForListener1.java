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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;


public class StubTestClassForListener1 extends StubParentClass {
	
	private static int count = 0;
	
	@BeforeGroups(groups={"stub1"})
	public void beforeGroup() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("group", "stub1");
	}
	
	@BeforeClass
	public void beforeClass() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("class", "listener1");
	}
	
	@BeforeTest
	public void beforeTest(XmlTest xmlTest) {
		SeleniumTestsContextManager.getThreadContext().setAttribute("test", xmlTest.getName());
	}

	@BeforeMethod(groups={"stub1"})
	public void beforeMethod(Method method) {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method", method.getName());
	}
	
	@Test(groups="stub1")
	public void test1Listener1() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method exec", "test1Listener1");

		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method"), "test1Listener1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("test"), "test1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("class"), "listener1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("group"), "stub1");
	}
	
	@Test(groups="stub1")
	public void test2Listener1() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method exec", "test2Listener1");
	}
	
	@AfterMethod(groups={"stub1"})
	public void afterMethod(Method method) {
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method"), method.getName());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method exec"), method.getName());
	}
	
	@AfterTest
	public void afterTest(XmlTest xmlTest) {
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("test"), xmlTest.getName());
	}
	
	@AfterClass
	public void afterClass() {
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("class"), "listener1");
	}

	@AfterGroups(groups={"stub1"})
	public void afterGroup() {
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("group"), "stub1");
	}
}
