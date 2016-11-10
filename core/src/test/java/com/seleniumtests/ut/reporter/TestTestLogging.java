package com.seleniumtests.ut.reporter;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import com.seleniumtests.GenericTest;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.reporter.TestMessage;
import com.seleniumtests.reporter.TestMessage.MessageType;
import com.seleniumtests.reporter.TestStep;

public class TestTestLogging extends GenericTest {

	@BeforeMethod(groups={"ut"})
	public void reset() {
		TestLogging.reset();
	}
	
	@Test(groups={"ut"})
	public void testInfo() {
		TestLogging.setCurrentRootTestStep(new TestStep("step"));
		TestLogging.info("message");
		Assert.assertEquals(TestLogging.getParentTestStep().getStepActions().size(), 1);
		Assert.assertTrue(TestLogging.getParentTestStep().getStepActions().get(0) instanceof TestMessage);
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.INFO);
	}
	
	@Test(groups={"ut"})
	public void testWarning() {
		TestLogging.setCurrentRootTestStep(new TestStep("step"));
		TestLogging.warning("message");
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.WARNING);
	}
	
	@Test(groups={"ut"})
	public void testError() {
		TestLogging.setCurrentRootTestStep(new TestStep("step"));
		TestLogging.error("message");
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.ERROR);
	}
	
	@Test(groups={"ut"})
	public void testLog() {
		TestLogging.setCurrentRootTestStep(new TestStep("step"));
		TestLogging.log("message");
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.LOG);
	}
	
	@Test(groups={"ut"})
	public void testLogScreenshotOk() {
		TestLogging.setCurrentRootTestStep(new TestStep("step"));
		TestLogging.logScreenshot(new ScreenShot(), false);
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.LOG);
	}
	
	@Test(groups={"ut"})
	public void testLogScreenshotKo() {
		TestLogging.setCurrentRootTestStep(new TestStep("step"));
		TestLogging.logScreenshot(new ScreenShot(), true);
		Assert.assertEquals(((TestMessage)(TestLogging.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.ERROR);
	}
	
	@Test(groups={"ut"})
	public void testLogTestStep() {
		TestStep testStep = new TestStep("step");
		ITestResult testResult = new TestResult();
		
		TestLogging.setCurrentTestResult(testResult);
		TestLogging.setCurrentRootTestStep(testStep);
		TestLogging.logTestStep(testStep);
		Assert.assertEquals(TestLogging.getTestsSteps().get(testResult).size(), 1);
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotString() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		String screenshotStr = TestLogging.buildScreenshotLog(screenshot);
		Assert.assertEquals(screenshotStr, "<a href='http://location' target=url>Application URL</a> | <a href='file.html' target=html>Application HTML Source</a> | <a href='file.png' class='lightbox'>Application Snapshot</a>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutInfo() {
		ScreenShot screenshot = new ScreenShot();
		String screenshotStr = TestLogging.buildScreenshotLog(screenshot);
		Assert.assertEquals(screenshotStr, "");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutImage() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setLocation("http://location");
		screenshot.setHtmlSourcePath("file.html");
		String screenshotStr = TestLogging.buildScreenshotLog(screenshot);
		Assert.assertEquals(screenshotStr, "<a href='http://location' target=url>Application URL</a> | <a href='file.html' target=html>Application HTML Source</a>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutSource() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setLocation("http://location");
		screenshot.setImagePath("file.png");
		String screenshotStr = TestLogging.buildScreenshotLog(screenshot);
		Assert.assertEquals(screenshotStr, "<a href='http://location' target=url>Application URL</a> | <a href='file.png' class='lightbox'>Application Snapshot</a>");
	}
	
	@Test(groups={"ut"})
	public void testBuildScreenshotStringWithoutLocation() {
		ScreenShot screenshot = new ScreenShot();
		screenshot.setHtmlSourcePath("file.html");
		screenshot.setImagePath("file.png");
		String screenshotStr = TestLogging.buildScreenshotLog(screenshot);
		Assert.assertEquals(screenshotStr, " | <a href='file.html' target=html>Application HTML Source</a> | <a href='file.png' class='lightbox'>Application Snapshot</a>");
	}
}
