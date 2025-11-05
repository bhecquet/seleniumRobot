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
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.util.helper.WaitHelper;

public class StubTestClass2 extends StubParentClass {

	@BeforeMethod(groups={"stub", "stub2"})
	public void set(Method method) {
		WaitHelper.waitForMilliSeconds(100);
	}
	
	@Test(groups="stub")
	public void test1() {
	}
	
	/**
	 * Skipped as test5 failed
	 */
	@Test(groups="stub", dependsOnGroups={"stub2"})
	public void test2() {
	}
	
	/**
	 * Skipped as test4 failed
	 */
	@Test(groups="stub", dependsOnMethods={"test4"})
	public void test3() {
	}
	
	@Test(groups="stub")
	public void test4() {
		Assert.fail("fail");
	}
	
	@Test(groups="stub2")
	public void test5() {
		Assert.assertTrue(false);
	}
	
	@Test(groups="stub", dependsOnMethods={"test1"})
	public void test6() {
	}

	@Test(groups="stub")
	public void test7() {
		throw new SkipException("test skipped");
	}
}
