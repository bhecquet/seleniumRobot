/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.core.aspects;

import java.io.IOException;
import java.util.List;

import com.seleniumtests.it.stubclasses.StubTestClassForDriverTest;
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
	public void testVideoStartDateSetWhenVideoRecordingEnabled() {
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
	public void testVideoStartDateSetWhenVideoRecordingDisabled() {
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
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLogging() {
		new CalcPage()
				.add(1, 1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(0).getOrigin(), CalcPage.class);
		Assert.assertEquals(steps.get(0).getAction(), "openPage");
		Assert.assertEquals(steps.get(1).getName(), "add with args: (1, 1, )");
		Assert.assertEquals(steps.get(1).getOrigin(), CalcPage.class);
		Assert.assertEquals(steps.get(1).getAction(), "add");
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
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescription() {
		new CalcPage()
		.addWithName(1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add something to total");
		Assert.assertEquals(steps.get(1).getAction(), "add something to total");
	}
	
	/**
	 * Check that if step definition contains argument name, this one is replaced
	 * - page opening
	 * - 'add 1 to total' (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionAndArgs() {
		new CalcPage()
		.addWithName2(1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 to total");
		Assert.assertEquals(steps.get(1).getAction(), "add ${a} to total");
	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - add something to total (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 * Here, we check the "@Step" annotation
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescription2() {
		new CalcPage()
		.addWithNameBis(1);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "add something to total");
		Assert.assertEquals(steps.get(1).getAction(), "add something to total");
	}
	
	/**
	 * Check that if step definition contains argument name, this one is replaced
	 * - page opening
	 * - 'add 1 to total' (this step has the @StepName description
	 * Checks that root steps are correctly intercepted
	 * Here, we check the "@Step" annotation
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionAndArgs2() {
		new CalcPage()
		.addWithName2Bis(1);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 to total");
		Assert.assertEquals(steps.get(1).getAction(), "add ${a} to total");
	}
	
	/**
	 * Check that if step definition contains argument name and one of argument is password, it's masked
	 * - Connect to calc with login/******
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionPassword() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
			.connectWithName("login", "somePassToConnect");

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "Connect to calc with login/******");
		Assert.assertEquals(steps.get(1).getAction(), "Connect to calc with ${login}/${password}");
	}
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionPassword2() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.connectWithName("login", "$§AbCdE$DeF£GhIjKl*:?");
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "Connect to calc with login/******");
	}
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionPassword3() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.connect("login", "$§AbCdE$DeF£GhIjKl*:?");
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "connect with args: (login, ******, )");
	}
	
	/**
	 * Check that if step definition contains argument name with array, all values are visible
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionArray() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		new CalcPage()
		.addWithName3(1, 2, 3);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getName(), "add 1 and [2,3,] to total");
	}
	
	/**
	 * Check that if step definition contains argument name with array, all values are visible
	 * Here, we check the "@Step" annotation
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLoggingWithStepDescriptionArray2() {
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
	 */
	@Test(groups={"it"})
	public void testSimpleCucumberStepLogging() {
		SeleniumRobotTestPlan.setCucumberTest(true);
		new CalcPage()
			.addC(1, 1);

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "^add '(\\d+)' to '(\\d+)'$ with args: (1, 1, )");
		Assert.assertEquals(steps.get(1).getAction(), "^add '(\\d+)' to '(\\d+)'$");
		Assert.assertEquals(steps.get(1).getOrigin(), CalcPage.class);
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertFalse(steps.get(1).getFailed());
		Assert.assertEquals(steps.get(0).getStepActions().size(), 2);
		Assert.assertEquals(steps.get(0).getStepActions().get(0).getName(), "Opening page CalcPage");
		Assert.assertTrue(steps.get(0).getStepActions().get(1).getName().contains("Open web page in"));
		Assert.assertEquals(steps.get(1).getStepActions().size(), 1);
		Assert.assertEquals(steps.get(1).getStepActions().get(0).getName(), "^add '(\\d+)' to '(\\d+)'$ with args: (1, 1, )");

	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - failAction
	 * Checks that root steps are correctly intercepted with an action in error
	 * Also check this action is marked as failed and exception is present in step
	 */
	@Test(groups={"it"})
	public void testFailedStepOnException() {
		try {
			new CalcPage()
				.failAction();
		} catch (DriverExceptions e) {
			// continue
		}

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "failAction");
		Assert.assertFalse(steps.get(0).getFailed());
		Assert.assertTrue(steps.get(1).getFailed());
		Assert.assertNotNull(steps.get(1).getActionException());
		Assert.assertEquals(steps.get(1).getActionException().getMessage(), "fail");
	}

	/**
	 * Check that if an action fails, it holds the exception, and also the calling step
	 */
	@Test(groups="it")
	public void testFailedAction() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);
		try {
			new StubTestClassForDriverTest().testDriverFailed();
		} catch (Exception e) {
			// ignore error
		}

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 4);

		Assert.assertFalse(steps.get(2).getFailed());
		Assert.assertNull(steps.get(2).getActionException());
		Assert.assertEquals(steps.get(3).getName(), "_writeSomethingOnNonExistentElement");
		Assert.assertTrue(steps.get(3).getFailed());

		// exception is forwarded to step
		Assert.assertTrue(steps.get(3).getActionException().getMessage().startsWith("Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found"));
		Assert.assertEquals(steps.get(3).getStepActions().size(), 2);

		// the failed action has logged exception
		Assert.assertEquals(steps.get(3).getStepActions().get(0).getName(), "sendKeys on TextFieldElement Text, by={By.id: text___} with args: (true, true, [a text,], )");
		Assert.assertTrue(steps.get(3).getStepActions().get(0).getActionException().getMessage().startsWith("Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found"));
		Assert.assertTrue(steps.get(3).getStepActions().get(0).getFailed());

		// a test message is logged for that action in error
		Assert.assertTrue(steps.get(3).getStepActions().get(1).getName().startsWith("""
				Warning: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found
				For documentation on this error, please visit:"""));

	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - failAction
	 * Checks that root steps are correctly intercepted with an action in error (AssertionError)
	 * Also check this action is marked as failed and exception is present in step
	 */
	@Test(groups={"it"})
	public void testFailedStepOnAssertion() {
		try {
			new CalcPage()
			.assertAction();
		} catch (AssertionError e) {
			// continue
		}

		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "assertAction");
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
	 */
	@Test(groups={"it"})
	public void testSubStepsNonCucumberStepLogging() {
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
		Assert.assertEquals(subStep.getName(), "doNothing");
		TestAction subSubAction = subStep.getStepActions().get(0);
		Assert.assertEquals(subSubAction.getName(), "doNothing on HtmlElement none, by={By.id: none} ");
		
		Assert.assertEquals(steps.get(2).getStepActions().size(), 1);
		subStep = (TestStep)steps.get(2).getStepActions().get(0);
		Assert.assertEquals(subStep.getStepActions().size(), 1);
		Assert.assertEquals(subStep.getName(), "add with args: (2, 2, )");
		TestStep subSubStep = (TestStep)subStep.getStepActions().get(0);
		Assert.assertEquals(subSubStep.getName(), "doNothing");
	}
	

	private void testPassword(boolean maskPassword, String password, String expectedPass) {
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
	 */
	@Test(groups={"it"})
	public void testPasswordNoReplacement() {
		testPassword(false, "somePassToConnect", "somePassToConnect");
	}
	
	
	/**
	 * Check password replacement when {@code @Mask} annotation is used on parameter
	 */
	@Test(groups={"it"})
	public void testPasswordMasking() {
		new CalcPage()
			.addAndMask(1, 234567);
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(0).getName(), "openPage with args: (null, )");
		Assert.assertEquals(steps.get(1).getName(), "addAndMask with args: (1, ******, )");
	}
	
	/**
	 * If password is null do not replace
	 */
	@Test(groups={"it"})
	public void testPasswordReplacementNull() {
		testPassword(true, null, "null");
	}
	
	/**
	 * Check password replacement done when requested by start option
	 */
	@Test(groups={"it"})
	public void testPasswordReplacement() {
		testPassword(true, "somePassToConnect", "******");
	}
	
	/**
	 * Check password replacement is not done if password is null
	 */
	@Test(groups={"it"})
	public void testNullPasswordReplacement() {
		testPassword(true, null, "null");
	}
	
	/**
	 * A step is defined with error cause
	 */
	@Test(groups={"it"})
	public void testStepWithErrorCause() {
		try {
			new CalcPage()
			.addWithErrorCauseErrorAndDetails(1);
		} catch (DriverExceptions e) {
			// continue
		}
		
		List<TestStep> steps = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps();
		Assert.assertEquals(steps.size(), 2);
		Assert.assertEquals(steps.get(1).getRootCause(), RootCause.REGRESSION);
		Assert.assertEquals(steps.get(1).getRootCauseDetails(), "Check your scripts");
	}
}
