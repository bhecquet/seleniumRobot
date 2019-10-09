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
	public static boolean failed = false;
	
	@BeforeClass(groups={"stub"})
	public void setCount() {
		WaitHelper.waitForMilliSeconds(100);
		count = 0;
		failed = false;
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
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		
		ScreenShot screenshot = new ScreenShot();
		File tmpImg = File.createTempFile("img_with_very_very_very_long_name_to_be_shortened", ".png");
		File tmpHtml = File.createTempFile("html_with_very_very_very_long_name_to_be_shortened", ".html");

		screenshot.setImagePath("screenshot/" + tmpImg.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtils.moveFile(tmpImg, new File(screenshot.getFullImagePath()));
		FileUtils.moveFile(tmpHtml, new File(screenshot.getFullHtmlPath()));
		
		step1.addSnapshot(new Snapshot(screenshot, "main"), 1, null);
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
	
	/**
	 * Increase max retry
	 */
	@Test(groups="stub")
	public void testWithExceptionAndMaxRetryIncreased() {
		count++;
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.logTestStep(step1);
		
		try {
			throw new DriverExceptions("some exception");
		} finally {
			if (count < 3) {
				increaseMaxRetry();
			}
		}
	}
	
	/**
	 * Increase max retry above limit
	 */
	@Test(groups="stub")
	public void testWithExceptionAndMaxRetryIncreasedWithLimit() {
		count++;
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.logTestStep(step1);
		
		try {
			throw new DriverExceptions("some exception");
		} finally {
			if (count < 4) {
				increaseMaxRetry();
			}
		}
	}
	
	
	/**
	 * Issue #229: test with a step in error but test OK
	 */
	@Test(groups="stub")
	public void testOkWithOneStepFailed() {
		TestStep step1 = new TestStep("step 1", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction("first action failed", true, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.logTestStep(step1);
	}
	
	/**
	 * Test which fails only on first execution
	 */
	@Test(groups="stub")
	public void testWithExceptionOnFirstExec() {

		TestStep step1 = new TestStep("step 10", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.logTestStep(step1);
		
		if (!failed) {
			failed = true;
			throw new DriverExceptions("some exception");
		}
	}
	
	/**
	 * Test which fails only on first execution
	 */
	@Test(groups="stub")
	public void testWithSocketTimeoutOnFirstExec() {
		
		TestStep step1 = new TestStep("step 10", TestLogging.getCurrentTestResult(), new ArrayList<>());
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestLogging.logTestStep(step1);
		
		if (!failed) {
			failed = true;
			throw new WebDriverException("Session [6919ba25-53b6-4615-bd59-e97399bf1e12] was terminated due to SO_TIMEOUT");
		}
	}
}
