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

import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.TestAction;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.reporter.TestMessage;
import com.seleniumtests.reporter.TestMessage.MessageType;
import com.seleniumtests.reporter.TestStep;

public class StubTestClass extends StubParentClass {
	
	private static int count = 0;

	@Test(groups="stub", description="a test with steps")
	public void testAndSubActions() {
		TestStep step1 = new TestStep("step 1");
		step1.addAction(new TestAction("click button", false));
		step1.addAction(new TestAction("sendKeys to text field", true));
		
		ScreenShot screenshot = new ScreenShot();
		screenshot.setImagePath("screenshots/image.png");
		step1.setSnapshot("screenshots/image.png");
		step1.addMessage(new TestMessage(TestLogging.OUTPUT_PATTERN + TestLogging.buildScreenshotLog(screenshot), MessageType.INFO));
		step1.setActionException(new WebDriverException("driver exception"));
		TestStep subStep1 = new TestStep("step 1.3: open page");
		subStep1.addAction(new TestAction("click link", false));
		subStep1.addMessage(new TestMessage("a message", MessageType.LOG));
		subStep1.addAction(new TestAction("sendKeys to password field", false));
		step1.addAction(subStep1);
		
		step1.setDuration(1230L);
		TestStep step2 = new TestStep("step 2");
		step2.setDuration(14030L);
		TestLogging.logTestStep(step1);
		TestLogging.logTestStep(step2);
	}
	
	@Test(groups="stub")
	public void testInError() {
		TestStep step1 = new TestStep("step 1");
		TestLogging.setCurrentRootTestStep(step1);
		TestLogging.getParentTestStep().addAction(new TestAction("click button", false));
		TestLogging.getParentTestStep().addMessage(new TestMessage("click ok", MessageType.INFO));
		TestLogging.warning("Some warning message");
		TestLogging.info("Some Info message");
		TestLogging.error("Some Error message");
		TestLogging.log("Some log message");
		TestLogging.getParentTestStep().addAction(new TestAction("send keyboard action", false));
		TestLogging.logTestStep(TestLogging.getCurrentRootTestStep());
		Assert.fail("error");
	}
	
	@Test(groups="stub")
	public void testWithException() {
		count++;
		TestStep step1 = new TestStep("step 1");
		step1.addAction(new TestAction(String.format("played %d times", count), false));
		step1.addAction(new TestAction("click button", false));
		TestLogging.logTestStep(step1);
		throw new DriverExceptions("some exception");
	}
}
