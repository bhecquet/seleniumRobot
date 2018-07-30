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

import java.io.IOException;
import java.lang.reflect.Method;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.core.aspects.CalcPage;

public class StubTestClassManualSteps extends StubParentClass {

	@AfterClass(groups={"stub"})
	public void teardown() {
		WebUIDriver.cleanUp();
		WebUIDriver.cleanUpWebUIDriver();
	}
	
	@BeforeMethod(groups={"stub"})
	public void set(Method method) {
		SeleniumRobotTestPlan.setCucumberTest(false);
	}

	@Test(groups="stub")
	public void testOk() throws IOException {

		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
		
		addStep("Test start");
		CalcPage cPage = new CalcPage();
		
		addStep("add some values");
		cPage.add(1, 1);
		cPage.add(1, 2);
		cPage.minus(2);
		
		addStep("do nothing");
		cPage.doNothing();
	}
	
	/**
	 * Test that if we specify a password to mask, it's not shown in report
	 * @throws IOException
	 */
	@Test(groups="stub")
	public void testOkPassword() throws IOException {
		
		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
		
		addStep("Test start");
		CalcPage cPage = new CalcPage();
		
		addStep("Connect to calc", "aPassPhrase");
		cPage.connect("login", "aPassPhrase");
		
		addStep("Reconnect to calc");
		cPage.connect("login", "anOtherPassPhrase");

	}
	
	/**
	 * Test where we throw an assertion to show if report handles it
	 * @throws IOException
	 */
	@Test(groups="stub")
	public void testWithAssert() throws IOException {
		
		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
		
		addStep("Test start");
		CalcPage cPage = new CalcPage();
		
		addStep("assert exception");
		cPage.assertAction();
		
		addStep("do nothing");
		cPage.doNothing();
	}


}
