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

import org.testng.annotations.Test;

public class StubTestClassWithWait extends StubParentClass {

	@Test(groups="stub")
	public void test1() {
		logger.info("test1 starting");
		logger.info("test1 finished");
	}
	
	@Test(groups="stub")
	public void test2() {
		logger.info("test2 starting");
		logger.info("test2 finished");
	}
	
	@Test(groups="stub")
	public void test3() {
		logger.info("test3 starting");
		logger.info("test3 finished");
	}
	
}
