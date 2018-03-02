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
package com.seleniumtests.ut.reporter;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import com.seleniumtests.GenericTest;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;

public class TestTestLogging extends GenericTest {

	@BeforeMethod(groups={"ut"})
	public void reset() {
		TestLogging.reset();
	}
	
	@Test(groups={"ut"})
	public void testInfo() {
		TestLogging.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>()));
		TestLogging.info("message");
		Assert.assertEquals(TestLogging.getParentTestStep().getStepActions().size(), 1);
		Assert.assertTrue(TestLogging.getParentTestStep().getStepActions().get(0) instanceof TestMessage);
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.INFO);
	}
	
	@Test(groups={"ut"})
	public void testWarning() {
		TestLogging.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>()));
		TestLogging.warning("message");
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.WARNING);
	}
	
	@Test(groups={"ut"})
	public void testError() {
		TestLogging.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>()));
		TestLogging.error("message");
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.ERROR);
	}
	
	@Test(groups={"ut"})
	public void testLog() {
		TestLogging.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>()));
		TestLogging.log("message");
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.LOG);
	}
	
	@Test(groups={"ut"})
	public void testLogScreenshotOk() {
		TestLogging.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>()));
		TestLogging.logScreenshot(new ScreenShot());
		Assert.assertEquals(TestLogging.getParentTestStep().getSnapshots().size(), 1);
	}
	
	@Test(groups={"ut"})
	public void testLogTestStep() {
		TestStep testStep = new TestStep("step", null, new ArrayList<>());
		ITestResult testResult = new TestResult();
		
		TestLogging.setCurrentTestResult(testResult);
		TestLogging.setCurrentRootTestStep(testStep);
		TestLogging.logTestStep(testStep);
		Assert.assertEquals(TestLogging.getTestsSteps().get(testResult).size(), 1);
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotString() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot);
		String screenshotStr = snapshotLogger.buildScreenshotLog();
		Assert.assertEquals(screenshotStr, "Output: title: <a href='http://location' target=url>Application URL</a> | <a href='file.html' target=html>Application HTML Source</a> | <a href='file.png' class='lightbox'>Application Snapshot</a>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutInfo() {
		ScreenShot screenshot = new ScreenShot();
		Snapshot snapshotLogger = new Snapshot(screenshot);
		String screenshotStr = snapshotLogger.buildScreenshotLog();
		Assert.assertEquals(screenshotStr, "Output: null: ");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutImage() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		Snapshot snapshotLogger = new Snapshot(screenshot);
		String screenshotStr = snapshotLogger.buildScreenshotLog();
		Assert.assertEquals(screenshotStr, "Output: title: <a href='http://location' target=url>Application URL</a> | <a href='file.html' target=html>Application HTML Source</a>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutSource() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setLocation("http://location");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot);
		String screenshotStr = snapshotLogger.buildScreenshotLog();
		Assert.assertEquals(screenshotStr, "Output: title: <a href='http://location' target=url>Application URL</a> | <a href='file.png' class='lightbox'>Application Snapshot</a>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutLocation() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setTitle("title");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		Snapshot snapshotLogger = new Snapshot(screenshot);
		String screenshotStr = snapshotLogger.buildScreenshotLog();
		Assert.assertEquals(screenshotStr, "Output: title:  | <a href='file.html' target=html>Application HTML Source</a> | <a href='file.png' class='lightbox'>Application Snapshot</a>");
	}
}
