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


/**
 * Test class to check the behaviour of increaseMaxRetry in various situations
 * @author S047432
 *
 */
public class StubTestClassForListener6 extends StubTestClassForListenerParent {
	


	@Test(groups="stub1")
	public void testIncreaseMaxRetryInTestMethod() {
		increaseMaxRetry();
		throw new IndexOutOfBoundsException("error in count");
	}
	
	@Test(groups="stub1")
	public void testIncreaseMaxRetryInAfterTestMethod() {
		SeleniumTestsContextManager.getThreadContext().setAttribute("increase after method", true);
		
		throw new IndexOutOfBoundsException("error in count");
	}
	
	@AfterMethod(groups={"stub1"})
	public void afterMethod(Method method, XmlTest xmlTest) {
		
		if (Boolean.TRUE.equals(SeleniumTestsContextManager.getThreadContext().getAttribute("increase after method"))) {
			increaseMaxRetry();
		}
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
