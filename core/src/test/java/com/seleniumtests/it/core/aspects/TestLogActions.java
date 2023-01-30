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
package com.seleniumtests.it.core.aspects;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.helper.WaitHelper;

public class TestLogActions extends GenericTest {
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext testContext) {
		System.setProperty("browser", "none");
		SeleniumTestsContextManager.initGlobalContext(testContext);
		initThreadContext(testContext);
	}
	
	@AfterClass(groups={"it"})
	public void teardown() {
		System.clearProperty("browser");
		WebUIDriver.cleanUp();
	}
	
	@BeforeMethod(groups={"it"})
	public void set() {
		SeleniumRobotTestPlan.setCucumberTest(false);
	}
	
	@AfterMethod(groups={"it"})
	public void reset() {
		resetCurrentTestResult();
	}

	@Test(groups={"it"})
	public void testVideoStartDateSetWhenVideoRecordingEnabled() throws Exception {
		WebDriver driver = null;
		try {
			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
			driver = WebUIDriver.getWebDriver(true);
			DriverTestPage testPage = new DriverTestPage(true);
			WaitHelper.waitForSeconds(1);
			testPage._writeSomething();
			
			TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
			TestStep step = stepManager.getTestSteps().get(2);
			
			Assert.assertTrue(step.getVideoTimeStamp() > 0);
			Assert.assertNotNull(stepManager.getVideoStartDate());
		} finally {
			if (driver != null) {
				WebUIDriver.cleanUp();
			}
		}
	}
	
	@Test(groups={"it"})
	public void testVideoStartDateSetWhenVideoRecordingDisabled() throws Exception {
		WebDriver driver = null;
		try {
			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			SeleniumTestsContextManager.getThreadContext().setVideoCapture("false");
			driver = WebUIDriver.getWebDriver(true);
			DriverTestPage testPage = new DriverTestPage(true);
			WaitHelper.waitForSeconds(1);
			testPage._writeSomething();
			
			TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
			TestStep step = stepManager.getTestSteps().get(2);
			
			Assert.assertEquals(step.getVideoTimeStamp(), 0);
			Assert.assertNull(stepManager.getVideoStartDate());
		} finally {
			if (driver != null) {
				WebUIDriver.cleanUp();
			}
		}
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

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add with args: (1, 1, )");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertFalse(steps.get(1).getFailed());
		Assert.assertEquals(steps.get(0).getStepActions().size(), 2); // Opening page + timing
		Assert.assertEquals(steps.get(1).getStepActions().size(), 1);
	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - add something to total (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescription() throws IOException {
		new CalcPage()
		.addWithName(1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add something to total");
	}
	
	/**
	 * Check that if step definition contains argument name, this one is replaced
	 * - page opening
	 * - 'add 1 to total' (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionAndArgs() throws IOException {
		new CalcPage()
		.addWithName2(1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 to total");
	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - add something to total (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 * 
	 * Here, we check the "@Step" annotation
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescription2() throws IOException {
		new CalcPage()
		.addWithNameBis(1);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add something to total");
	}
	
	/**
	 * Check that if step definition contains argument name, this one is replaced
	 * - page opening
	 * - 'add 1 to total' (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 * 
	 * Here, we check the "@Step" annotation
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionAndArgs2() throws IOException {
		new CalcPage()
		.addWithName2Bis(1);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 to total");
	}
	
	/**
	 * Check that if step definition contains argument name and one of argument is password, it's masked
	 * - Connect to calc with login/******
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionPassword() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
			.connectWithName("login", "somePassToConnect");

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "Connect to calc with login/******");
	}
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionPassword2() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.connectWithName("login", "$§AbCdE$DeF£GhIjKl*:?");
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "Connect to calc with login/******");
	}
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionPassword3() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.connect("login", "$§AbCdE$DeF£GhIjKl*:?");
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "connect with args: (login, ******, )");
	}
	
	/**
	 * Check that if step definition contains argument name with array, all values are visible
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionArray() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.addWithName3(1, 2, 3);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 and [2,3,] to total");
	}
	
	/**
	 * Check that if step definition contains argument name with array, all values are visible
	 * 
	 * Here, we check the "@Step" annotation
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionArray2() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.addWithName3Bis(1, 2, 3);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 and [2,3,] to total");
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
		SeleniumRobotTestPlan.setCucumberTest(true);
		new CalcPage()
			.addC(1, 1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add '(\\d+)' to '(\\d+)' with args: (1, 1, )");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertFalse(steps.get(1).getFailed());
		Assert.assertEquals(steps.get(0).getStepActions().size(), 2);
		Assert.assertEquals(steps.get(0).getStepActions().get(0).getName(), "Opening page CalcPage");
		Assert.assertTrue(steps.get(0).getStepActions().get(1).getName().contains("Open web page in"));
		Assert.assertEquals(steps.get(1).getStepActions().size(), 1);
		Assert.assertEquals(steps.get(1).getStepActions().get(0).getName(), "add '(\\d+)' to '(\\d+)' with args: (1, 1, )");

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

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
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

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
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

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
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
	

	private void testPassword(boolean maskPassword, String password, String expectedPass) throws IOException {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(maskPassword);
		new CalcPage()
		.connect("login", password);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), String.format("connect with args: (login, %s, )", expectedPass));
	}
	
	/**
	 * Check password replacement not done when not requested
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testPasswordNoReplacement() throws IOException {
		testPassword(false, "somePassToConnect", "somePassToConnect");
	}
	
	
	/**
	 * Check password replacement when {@code @Mask} annotation is used on parameter
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testPasswordMasking() throws IOException {
		new CalcPage()
			.addAndMask(1, 234567);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), String.format("addAndMask with args: (1, ******, )"));
	}
	
	/**
	 * If password is null do not replace
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testPasswordReplacementNull() throws IOException {
		testPassword(true, null, "null");
	}
	
	/**
	 * Check password replacement done when requested by start option
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testPasswordReplacement() throws IOException {
		testPassword(true, "somePassToConnect", "******");
	}
	
	/**
	 * Check password replacement is not done if password is null
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testNullPasswordReplacement() throws IOException {
		testPassword(true, null, "null");
	}
	
	/**
	 * A step is defined with error cause
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testStepWithErrorCause() throws IOException {
		try {
			new CalcPage()
			.addWithErrorCauseErrorAndDetails(1);
		} catch (DriverExceptions e) {
			// continue;
		}
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getRootCause(), RootCause.REGRESSION);
		Assert.assertEquals(steps.get(1).getRootCauseDetails(), "Check your scripts");
	}
}
