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
package com.seleniumtests.it.core.aspects;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.runner.SeleniumRobotRunner;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestAction;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.reporter.TestStep;

public class TestLogActions extends GenericTest {
	
	@BeforeClass(groups={"it"})
	public void init() {
		System.setProperty("browser", "none");
	}
	
	@AfterClass(groups={"it"})
	public void teardown() {
		System.clearProperty("browser");
		WebUIDriver.cleanUp();
	}
	
	@BeforeMethod(groups={"it"})
	public void set() {
		SeleniumRobotRunner.setCucumberTest(false);
	}
	
	@AfterMethod(groups={"it"})
	public void reset() {
		TestLogging.resetCurrentTestResult();
	}

	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - add
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLogging() throws IOException {
		new CalcPage()
				.add(1, 1);

		List<TestStep> steps = TestLogging.getTestsSteps().get(Reporter.getCurrentTestResult());
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add with args: (1, 1, )");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertFalse(steps.get(1).getFailed());
		Assert.assertEquals(steps.get(0).getStepActions().size(), 0);
		Assert.assertEquals(steps.get(1).getStepActions().size(), 1);
	}
	
	/**
	 * Only test presence of steps with cucumber annotations
	 * - page opening
	 * 		- addC		=> first interception by calling addC: never happens in real cucumber test
	 * 			- addC  => cucumber annotation interception
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleCucumberStepLogging() throws IOException {
		SeleniumRobotRunner.setCucumberTest(true);
		new CalcPage()
			.addC(1, 1);
		
		List<TestStep> steps = TestLogging.getTestsSteps().get(Reporter.getCurrentTestResult());
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add '(\\d+)' to '(\\d+)' with args: (1, 1, )");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertFalse(steps.get(1).getFailed());
		Assert.assertEquals(steps.get(0).getStepActions().size(), 0);
		Assert.assertEquals(steps.get(1).getStepActions().size(), 1);

	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - failAction
	 * Checks that root steps are correctly intercepted with an action in error
	 * Also check this action is marked as failed and exception is present in step
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFailedStepOnException() throws IOException {
		try {
			new CalcPage()
				.failAction();
		} catch (DriverExceptions e) {
			// continue;
		}
		
		List<TestStep> steps = TestLogging.getTestsSteps().get(Reporter.getCurrentTestResult());
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "failAction ");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertTrue(steps.get(1).getFailed());
		Assert.assertNotNull(steps.get(1).getActionException());
		Assert.assertEquals(steps.get(1).getActionException().getMessage(), "fail");
	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - failAction
	 * Checks that root steps are correctly intercepted with an action in error (AssertionError)
	 * Also check this action is marked as failed and exception is present in step
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFailedStepOnAssertion() throws IOException {
		try {
			new CalcPage()
			.assertAction();
		} catch (AssertionError e) {
			// continue;
		}
		
		List<TestStep> steps = TestLogging.getTestsSteps().get(Reporter.getCurrentTestResult());
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "assertAction ");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertTrue(steps.get(1).getFailed());
		Assert.assertNotNull(steps.get(1).getActionException());
		Assert.assertEquals(steps.get(1).getActionException().getMessage(), "false error expected [true] but found [false]");
	}
	
	/**
	 * Check presence of sub steps. These are methods defined in Page object but not directly called from main test. We should get
	 * - page opening
	 * - add(1, 1)
	 * 		- nothing
	 * 			- doNothing on HtmlElement none
	 * - add(2)											=> step
	 * 		- add (2, 2)								=> sub-step
	 * 			- donothing								=> sub-step
	 * 				- doNothing on HtmlElement none		=> action on element
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSubStepsNonCucumberStepLogging() throws IOException {
		new CalcPage()
					.add(1, 1)
					.add(2);
		
		List<TestStep> steps = TestLogging.getTestsSteps().get(Reporter.getCurrentTestResult());
		Assert.assertEquals(steps.size(), 3);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add with args: (1, 1, )");
		Assert.assertEquals(steps.get(2).getName(), "add with args: (2, )");

		Assert.assertEquals(steps.get(1).getStepActions().size(), 1);
		TestStep subStep = (TestStep)steps.get(1).getStepActions().get(0);
		Assert.assertEquals(subStep.getStepActions().size(), 1);
		Assert.assertEquals(subStep.getName(), "doNothing ");
		TestAction subSubAction = subStep.getStepActions().get(0);
		Assert.assertEquals(subSubAction.getName(), "doNothing on HtmlElement none, by={By.id: none} ");
		
		Assert.assertEquals(steps.get(2).getStepActions().size(), 1);
		subStep = (TestStep)steps.get(2).getStepActions().get(0);
		Assert.assertEquals(subStep.getStepActions().size(), 1);
		Assert.assertEquals(subStep.getName(), "add with args: (2, 2, )");
		TestStep subSubStep = (TestStep)subStep.getStepActions().get(0);
		Assert.assertEquals(subSubStep.getName(), "doNothing ");
	}
}
