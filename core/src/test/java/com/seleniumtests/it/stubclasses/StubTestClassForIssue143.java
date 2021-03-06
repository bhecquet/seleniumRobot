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

import java.io.IOException;
import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Stub class for correction of issue #143: [HTML] All tests show the @AfterMethod of all tests
 * @author s047432
 *
 */
public class StubTestClassForIssue143 extends StubParentClass {
	
	
	
	@Test(groups="stub")
	public void testOk1() throws Exception {
		logger.info("test Ok 1");
	}
	
	@Test(groups="stub")
	public void testOk2() throws IOException {
		logger.info("test Ok 2");
	}
	
	@AfterMethod(groups={"stub"})
	public void reset() {
		logger.info("after method");
	}
	
	@AfterMethod(groups={"stub"})
	public void reset2(Method method) {
		logger.info("after method with 'Method parameter'");
	}
}
