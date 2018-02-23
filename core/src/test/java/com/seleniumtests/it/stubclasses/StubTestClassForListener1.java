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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;


public class StubTestClassForListener1 extends StubTestClassForListenerParent {
	

	@BeforeTest
	public void beforeTest(XmlTest xmlTest) {
		SeleniumTestsContextManager.getThreadContext().setAttribute("test", xmlTest.getName());
	}

	@BeforeClass
	public void beforeClass() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("class", "listener1");
	}

	@BeforeMethod(groups={"stub1"})
	public void beforeMethod(Method method) {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method", method.getName());
	}
	
	@Test(groups="stub1")
	public void test1Listener1(XmlTest xmlTest) {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method exec", "test1Listener1");

		// test that all Before method settings are kept in test method
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method"), "test1Listener1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("test"), xmlTest.getName());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("class"), "listener1");
		SeleniumTestsContextManager.getMethodContext();
	}
	
	@Test(groups="stub1")
	public void test2Listener1() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("method exec", "test2Listener1");
	}
	
	@AfterMethod(groups={"stub1"})
	public void afterMethod(Method method, XmlTest xmlTest) {
		
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("test"), xmlTest.getName());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("class"), "listener1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method"), method.getName());
		
		// check setting added by test method is kept  		
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("method exec"), method.getName());
	}

	@AfterClass
	public void afterClass(XmlTest xmlTest) {
		// class context should not contain method settings
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getAttribute("method"));
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getAttribute("method exec"));
		
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("class"), "listener1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("test"), xmlTest.getName());
	}
	
	@AfterTest
	public void afterTest(XmlTest xmlTest) {
		// test context should not contain method / class settings
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getAttribute("method"));
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getAttribute("method exec"));
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getAttribute("class"));
		
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("test"), xmlTest.getName());
	}

}
