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
package com.seleniumtests.it.stubclasses;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.helper.WaitHelper;

public class StubTestClass extends StubParentClass {
	
	private static int count = 0;
	
	@BeforeClass(groups={"stub"})
	public void setCount() {
		count = 0;
	}

	@BeforeMethod(groups={"stub"})
	public void set(Method method) {
		WaitHelper.waitForMilliSeconds(100);
		TestLogging.info("before count: " + count);
	}
	
	@AfterMethod(groups={"stub"})
	public void reset(Method method) {
		TestLogging.info("after count: " + count);
	}
	
	@Test(groups="stub", description="a test with steps")
	public void testAndSubActions() throws IOException {
		System.out.println(SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports());
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		
		ScreenShot screenshot = new ScreenShot();
		File tmpImg = File.createTempFile("img_with_very_very_very_long_name_to_be_shortened", ".png");

		screenshot.setImagePath("screenshot/" + tmpImg.getName());
		FileUtils.moveFile(tmpImg, new File(screenshot.getFullImagePath()));
		
		step1.addSnapshot(new Snapshot(screenshot), 1);
		step1.setActionException(new WebDriverException("driver exception"));
		TestStep subStep1 = new TestStep("step 1.3: open page", TestLogging.getCurrentTestResult(), new ArrayList<>());
		subStep1.addAction(new TestAction("click link", false, new ArrayList<>()));
		subStep1.addMessage(new TestMessage("a message", MessageType.LOG));
		subStep1.addAction(new TestAction("sendKeys to password field", false, new ArrayList<>()));
		step1.addAction(subStep1);
		WaitHelper.waitForSeconds(3);
		step1.setDuration(1230L);
		TestStep step2 = new TestStep("step 2", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step2.setDuration(14030L);
		TestLogging.logTestStep(step1);
		TestLogging.logTestStep(step2);
	}
	
	@Test(groups="stub")
	public void testInError() {
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		TestLogging.setCurrentRootTestStep(step1);
		TestLogging.getParentTestStep().addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.getParentTestStep().addMessage(new TestMessage("click ok", MessageType.INFO));
		TestLogging.warning("Some warning message");
		TestLogging.info("Some Info message");
		TestLogging.error("Some Error message");
		TestLogging.log("Some log message");
		TestLogging.logTestValue("key", "we found a value of", "10");
		
		TestLogging.getParentTestStep().addAction(new TestAction("send keyboard action", false, new ArrayList<>()));
		TestLogging.logTestStep(TestLogging.getCurrentRootTestStep());
		Assert.fail("error");
	}
	
	@Test(groups="stub")
	public void testWithException() {
		count++;
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.logTestStep(step1);
		throw new DriverExceptions("some exception");
	}
}
