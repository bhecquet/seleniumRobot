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
package com.seleniumtests.it.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StubTestClass2 {

	@Test(groups="stub")
	public void test1() {
	}
	
	@Test(groups="stub", dependsOnGroups={"stub2"})
	public void test2() {
	}
	
	@Test(groups="stub", dependsOnMethods={"test4"})
	public void test3() {
	}
	
	@Test(groups="stub")
	public void test4() {
		Assert.fail("fail");
	}
	
	@Test(groups="stub2")
	public void test5() {
		Assert.fail("fail");
	}
	
	@Test(groups="stub", dependsOnMethods={"test3"})
	public void test6() {
	}
}
