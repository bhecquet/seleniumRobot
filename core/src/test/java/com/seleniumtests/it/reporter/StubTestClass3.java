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

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotRunner;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.core.aspects.CalcPage;

public class StubTestClass3 extends StubParentClass {

	@BeforeClass(groups={"stub"})
	public void init() {
		// thread context manager is already initialized by StubParentClass
		SeleniumTestsContextManager.getThreadContext().setBrowser("none");
	}
	
	@AfterClass(groups={"stub"})
	public void teardown() {
		WebUIDriver.cleanUp();
	}
	
	@BeforeMethod(groups={"stub"})
	public void set() {
		SeleniumRobotRunner.setCucumberTest(false);
	}

	@Test(groups="stub")
	public void testOk() throws IOException {
		new CalcPage()
			.add(1, 1);
	}
	
	/**
	 * Test KO with a runtime exception
	 * @throws IOException 
	 */
	@Test(groups="stub")
	public void testFailedWithException() throws IOException {
		new CalcPage()
			.failAction()
			.add(1);
	}
	
	/**
	 * Test KO with assertion, soft assertion disabled
	 * @throws IOException 
	 */
	@Test(groups="stub")
	public void testFailedWithSoftAssertDisabled() throws IOException {
		new CalcPage()
			.assertAction()
			.add(1);
	}
	
	/**
	 * Test KO with assertion, soft assertion enabled
	 * @throws IOException 
	 */
	@Test(groups="stub")
	public void testFailedWithSoftAssertEnabled() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(true);
		new CalcPage()
			.assertAction()
			.add(1);
	}
	
	/**
	 * Test KO with multiple assertions, soft assertion enabled
	 * @throws IOException 
	 */
	@Test(groups="stub")
	public void testMultipleFailedWithSoftAssertEnabled() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(true);
		new CalcPage()
			.assertAction()
			.assertAction2()
			.add(1);
	}

}
