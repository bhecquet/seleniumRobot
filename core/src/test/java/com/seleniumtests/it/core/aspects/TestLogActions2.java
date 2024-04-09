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

import java.util.Arrays;

import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions;
import com.seleniumtests.it.driver.support.pages.DriverTestPageObjectFatory;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.uipage.htmlelements.SeleniumElement;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.reporter.logger.TestStep;

public class TestLogActions2 extends GenericDriverTest {


	private DriverTestPage testPage;

	@BeforeMethod(groups = {"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {

		GenericTest.resetTestNGREsultAndLogger();
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
		driver = WebUIDriver.getWebDriver(true);
		testPage = new DriverTestPage(true);

	}

	/**
	 * Check that when calling a method with password in it, this is masked
	 * parameter name contains 'password' but not only that to check replacement is done on similar strings
	 */
	@Test(groups = {"ut"})
	public void testPassworkMasking() {

		testPage._setPassword("someText");
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 2);
	}

	/**
	 * Same as above but parameter name is 'pwd'
	 */
	@Test(groups = {"ut"})
	public void testPassworkMasking2() {

		testPage._setPassword2("someText");
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 2);
	}

	/**
	 * Check that if a step accepts a variable number of arguments, they are replaced
	 */
	@Test(groups = {"ut"})
	public void testMultiplePassworkMasking() {

		testPage._setPasswords("someText", "someOtherText");
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertFalse(step.toString().contains("someOtherText"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 3);
	}

	/**
	 * Check that if a step accepts a variable number of arguments, they are replaced
	 */
	@Test(groups = {"ut"})
	public void testMultiplePassworkMaskingWithList() {

		testPage._setPasswords(Arrays.asList("someText", "someOtherText"));
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertFalse(step.toString().contains("someOtherText"));
		Assert.assertTrue(step.toString().contains("sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [******,], )"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 3);
	}

	/**
	 * Test that we log the action and page where the action occurs, when TestAction is created
	 *
	 * @throws Exception
	 */
	@Test(groups = {"ut"})
	public void testLogPageObjectAction() throws Exception {

		testPage._goToNewPage();
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// check page action has a 'page' field filled but no element
		TestAction selectNewWindowAction = step.getStepActions().get(2); // get the "selectNewWindow" action
		Assert.assertEquals(selectNewWindowAction.getAction(), "selectNewWindow");
		Assert.assertEquals(selectNewWindowAction.getPage(), DriverTestPage.class);
		Assert.assertNull(selectNewWindowAction.getElement());

		TestAction openPageAction = ((TestStep) step.getStepActions().get(4)).getStepActions().get(0);
		Assert.assertEquals(openPageAction.getAction(), "openPage");
		Assert.assertEquals(openPageAction.getPage(), DriverSubTestPage.class);
		Assert.assertNull(openPageAction.getElement());
	}

	@Test(groups = {"ut"})
	public void testCreateSteps() throws Exception {

		testPage._goToNewPage();
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// check we have all actions / steps
		Assert.assertEquals(step.getStepActions().size(), 7);
		Assert.assertEquals(step.getStepActions().get(0).getName(), "click on LinkElement My link, by={By.id: link} ");

		// check sub-step is created
		Assert.assertTrue(step.getStepActions().get(4) instanceof TestStep);

		// check messages are recorded
		Assert.assertTrue(step.getStepActions().get(6) instanceof TestMessage);
		Assert.assertTrue(step.getStepActions().get(6).getName().contains("Open web page in"));
	}

	/**
	 * check step and actions are marked as failed when error occurs
	 * @throws Exception
	 */
	@Test(groups = {"ut"})
	public void testCreateFailedSteps() throws Exception {

		try {
			testPage._writeSomethingOnNonExistentElement();
		} catch (NoSuchElementException e) {
			// we expect this fails
		}
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		// check we have all actions / steps
		Assert.assertEquals(step.getStepActions().size(), 2);
		Assert.assertEquals(step.getStepActions().get(0).getName(), "sendKeys on TextFieldElement Text, by={By.id: text___} with args: (true, true, [a text,], )");
		Assert.assertTrue(step.getStepActions().get(0).getFailed());
		Assert.assertTrue(step.getStepActions().get(1).getName().contains("Warning: Searched element [TextFieldElement Text, by={By.id: text___}] from page 'com.seleniumtests.it.driver.support.pages.DriverTestPage' could not be found"));
		Assert.assertEquals(((TestMessage)step.getStepActions().get(1)).getMessageType(), TestMessage.MessageType.WARNING);

		// check step is marked as failed
		Assert.assertTrue(step.getFailed());
	}

	/**
	 * Check that if name of method is annotated with @Step, this name is used
	 *
	 * @throws Exception
	 */
	@Test(groups = {"ut"})
	public void testNameOfMethodAnnotated() throws Exception {

		testPage._resetWithAnnotation();
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
		Assert.assertEquals(step.getName(), "reset page");
	}

	/**
	 * Check that if name of method is annotated with @Step, this name is used
	 *
	 * @throws Exception
	 */
	@Test(groups = {"ut"})
	public void testNameOfCucumberMethod() throws Exception {

		try {
			SeleniumRobotTestPlan.setCucumberTest(true);

			testPage._resetWithCucumber();
			TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
			Assert.assertEquals(step.getName(), "reset page cucumber ");
		} finally {
			SeleniumRobotTestPlan.setCucumberTest(false);
		}
	}

	/**
	 * Check that when Selenium overrideNativeActions is set to false (the default) these actions are logged
	 * @throws Exception
	 */
	@Test(groups = {"ut"})
	public void testLogNativeSeleniumElementActionNoOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		new DriverTestPageNativeActions(true).reset();

		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(4);

		// check page action has a 'page' field filled but no element
		TestAction clickAction = step.getStepActions().get(0); // get the "click" action
		Assert.assertEquals(clickAction.getAction(), "click");
		Assert.assertTrue(clickAction.getName().contains("click on Element located by"));
		Assert.assertEquals(clickAction.getPage(), DriverTestPageNativeActions.class);
		Assert.assertTrue(clickAction.getElement() instanceof SeleniumElement);
		Assert.assertTrue(clickAction.getElement().getCallingPage() instanceof DriverTestPageNativeActions);
		Assert.assertEquals(clickAction.getElement().getOrigin(), "com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions");
		Assert.assertNull(clickAction.getElement().getFieldName()); // null as created inline
		Assert.assertNotNull(clickAction.getElement().getName());
	}

	@Test(groups = {"ut"})
	public void testLogPictureAction() {

		testPage._clickPicture();

		// check an action has been created for this
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
		Assert.assertEquals(step.getStepActions().size(), 3);
		Assert.assertEquals(step.getStepActions().get(0).getName(), "clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )");
		Assert.assertEquals(step.getStepActions().get(0).getAction(), "clickAt");
		Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.picture);
		Assert.assertEquals(step.getStepActions().get(0).getPage(), DriverTestPage.class);
		Assert.assertFalse(step.getStepActions().get(0).getFailed());
	}

	/**
	 * Test composite actions are correctly logged
	 * Each set of composite actions (something that ends with "perform()") must be enclosed in a sub-step "Composite" that holds all actions (click, move, ...)
	 */
	@Test(groups = {"ut"})
	public void testLogCompositeAction() {

		testPage._sendKeysComposite();

		// check an action has been created for this
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		Assert.assertEquals(step.getStepActions().size(), 2);
		TestStep compositeAction1 = (TestStep) step.getStepActions().get(0);
		TestStep compositeAction2 = (TestStep) step.getStepActions().get(1);

		Assert.assertEquals(compositeAction1.getName(), "Composite moveToElement,sendKeys,on element 'TextFieldElement Text, by={By.id: text2}'");
		Assert.assertEquals(compositeAction2.getName(), "Composite moveToElement,click,on element 'ButtonElement Reset, by={By.id: button2}'");

		// 2 sub steps (one for each composite action)
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getName(), "moveToElement with args: (TextFieldElement Text, by={By.id: text2}, )");
		Assert.assertEquals(compositeAction1.getStepActions().get(1).getName(), "sendKeys with args: ([composite,], )");
		Assert.assertEquals(compositeAction2.getStepActions().get(0).getName(), "moveToElement with args: (ButtonElement Reset, by={By.id: button2}, )");
		Assert.assertEquals(compositeAction2.getStepActions().get(1).getName(), "click ");
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getAction(), "moveToElement");
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getElement(), testPage.textElement);
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getPage(), DriverTestPage.class);
		Assert.assertFalse(compositeAction1.getStepActions().get(0).getFailed());
		Assert.assertEquals(compositeAction1.getStepActions().get(1).getAction(), "sendKeys");
		Assert.assertNull(compositeAction1.getStepActions().get(1).getElement());
		Assert.assertNull(compositeAction1.getStepActions().get(1).getPage());
		Assert.assertFalse(compositeAction1.getStepActions().get(1).getFailed());
	}

	/**
	 * Test logging of composite actions using selenium WebElement
	 */
	@Test(groups = {"ut"})
	public void testLogCompositeActionWithSeleniumElement() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		testPage._sendKeysCompositeWebElement();

		// check an action has been created for this
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		Assert.assertEquals(step.getStepActions().size(), 2);
		TestStep compositeAction1 = (TestStep) step.getStepActions().get(0);
		TestStep compositeAction2 = (TestStep) step.getStepActions().get(1);

		Assert.assertEquals(compositeAction1.getName(), "Composite moveToElement,sendKeys,on element 'id: text2'");
		Assert.assertEquals(compositeAction2.getName(), "Composite moveToElement,click,on element 'id: button2'");

		// 2 sub steps (one for each composite action)
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getName(), "moveToElement with args: (id: text2, )");
		Assert.assertEquals(compositeAction1.getStepActions().get(1).getName(), "sendKeys with args: ([composite,], )");
		Assert.assertEquals(compositeAction2.getStepActions().get(0).getName(), "moveToElement with args: (id: button2, )");
		Assert.assertEquals(compositeAction2.getStepActions().get(1).getName(), "click ");
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getAction(), "moveToElement");
		Assert.assertNotNull(compositeAction1.getStepActions().get(0).getElement());
		Assert.assertEquals(compositeAction1.getStepActions().get(0).getPage(), DriverTestPage.class);
		Assert.assertFalse(compositeAction1.getStepActions().get(0).getFailed());
		Assert.assertEquals(compositeAction1.getStepActions().get(1).getAction(), "sendKeys");
		Assert.assertNull(compositeAction1.getStepActions().get(1).getElement());
		Assert.assertNull(compositeAction1.getStepActions().get(1).getPage());
		Assert.assertFalse(compositeAction1.getStepActions().get(1).getFailed());
	}

	/**
	 * Test logging of composite actions when no element is specified
	 */
	@Test(groups = {"ut"})
	public void testLogCompositeActionWithoutElement() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		testPage._sendKeysCompositeNoElement();

		// check an action has been created for this
		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

		Assert.assertEquals(step.getStepActions().size(), 2);
		TestStep compositeAction2 = (TestStep) step.getStepActions().get(1);

		Assert.assertEquals(compositeAction2.getName(), "Composite moveByOffset,");

		// 2 sub steps (one for each composite action)
		Assert.assertEquals(compositeAction2.getStepActions().get(0).getName(), "moveByOffset with args: (10, 0, )");

	}

	/**
	 * Check that when Selenium overrideNativeActions is set to false (the default) these actions in PageFactory pattern are logged
	 *
	 * @throws Exception
	 */
	@Test(groups = {"ut"})
	public void testLogPageFactorySeleniumElementActionNoOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		new DriverTestPageObjectFatory(true).reset();

		TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(4);

		// check page action has a 'page' field filled but no element
		TestAction clickAction = step.getStepActions().get(0); // get the "click" action
		Assert.assertEquals(clickAction.getAction(), "click");
		Assert.assertTrue(clickAction.getName().contains("click on Element located by"));
		Assert.assertEquals(clickAction.getPage(), DriverTestPageObjectFatory.class);
		Assert.assertTrue(clickAction.getElement() instanceof SeleniumElement);
		Assert.assertEquals(clickAction.getElement().getOrigin(), "com.seleniumtests.it.driver.support.pages.DriverTestPageObjectFatory");
		Assert.assertTrue(clickAction.getElement().getCallingPage() instanceof DriverTestPageObjectFatory);
		Assert.assertNull(clickAction.getElement().getFieldName()); // null as we don't support field name for page factory elements
																	// this could be possible as elements are initialized on page creation,
																	// but this is a marginal use case, so we ignore it deliberately
		// name here is the one returned by HtmlUnit driver, which is different from other drivers
		Assert.assertEquals(clickAction.getElement().getName(), "id: button2");
	}
}