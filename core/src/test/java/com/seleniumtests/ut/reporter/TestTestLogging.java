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
package com.seleniumtests.ut.reporter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.HarCapture;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarPage;

public class TestTestLogging extends GenericTest {

	@BeforeMethod(groups={"ut"})
	public void reset() {

		resetTestNGREsultAndLogger();
	}
	
	@Test(groups={"ut"})
	public void testInfo() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		logger.info("message");
		Assert.assertEquals(TestStepManager.getParentTestStep().getStepActions().size(), 1);
		Assert.assertTrue(TestStepManager.getParentTestStep().getStepActions().get(0) instanceof TestMessage);
		Assert.assertEquals(((TestMessage)(TestStepManager.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.INFO);
	}
	
	@Test(groups={"ut"})
	public void testWarning() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		logger.warning("message");
		Assert.assertEquals(((TestMessage)(TestStepManager.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.WARNING);
	}
	
	@Test(groups={"ut"})
	public void testError() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		logger.error("message");
		Assert.assertEquals(((TestMessage)(TestStepManager.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.ERROR);
	}
	
	@Test(groups={"ut"})
	public void testLog() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		logger.log("message");
		Assert.assertEquals(((TestMessage)(TestStepManager.getParentTestStep().getStepActions().get(0))).getMessageType(), MessageType.LOG);
	}
	
	@Test(groups={"ut"})
	public void testLogScreenshotOk() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		logger.logScreenshot(new ScreenShot());
		Assert.assertEquals(TestStepManager.getParentTestStep().getSnapshots().size(), 1);
	}
	
	/**
	 * Check HTML page is moved
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testRelocateScreenshotHtmlOnly() throws IOException {

		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			
			ScreenShot screenshot = new ScreenShot();
			String htmlSourcePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "capture.html").toString();
			FileUtils.write(new File(htmlSourcePath), "<html>", StandardCharsets.UTF_8);
			screenshot.setHtmlSourcePath(String.format("../%s/%s/%s.html", "testRelocateScreenshotHtmlOnly", ScreenshotUtil.HTML_DIR, "capture")); // copied from ScreeshotUtils class
			
			logger.logScreenshot(screenshot);
			File initialFile = new File(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getFullHtmlPath());
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			Assert.assertNull(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getImagePath());
			
			// relocate
			TestStepManager.getParentTestStep().getSnapshots().get(0).relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved");
			File movedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved", ScreenshotUtil.HTML_DIR, "N-A_0-1_step--pture.html").toFile();
	
			Assert.assertTrue(movedFile.exists());
			Assert.assertFalse(initialFile.exists());
			Assert.assertEquals(new File(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getFullHtmlPath()), movedFile);
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved"));
		}
	}
	
	/**
	 * Check image file is moved
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testRelocateScreenshotImageOnly() throws IOException {
		
		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			
			ScreenShot screenshot = new ScreenShot();
			String imgSourcePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "capture.png").toString();
			FileUtils.write(new File(imgSourcePath), "<img>", StandardCharsets.UTF_8);
			screenshot.setImagePath(String.format("%s/%s.png", ScreenshotUtil.SCREENSHOT_DIR, "capture")); // copied from ScreeshotUtils class
			
			logger.logScreenshot(screenshot);
			File initialFile = new File(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getFullImagePath());
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			Assert.assertEquals(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getHtmlSource(), "");
			
			// relocate
			TestStepManager.getParentTestStep().getSnapshots().get(0).relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved");
			File movedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved", ScreenshotUtil.SCREENSHOT_DIR, "N-A_0-1_step--apture.png").toFile();

			Assert.assertTrue(movedFile.exists());
			Assert.assertFalse(initialFile.exists());
			Assert.assertEquals(new File(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getFullImagePath()), movedFile);
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved"));
		}
	}
	
	@Test(groups={"ut"})
	public void testRelocateScreenshotSamePath() throws IOException {
		
		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			
			ScreenShot screenshot = new ScreenShot();
			String imgSourcePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "capture.png").toString();
			FileUtils.write(new File(imgSourcePath), "<img>", StandardCharsets.UTF_8);
			screenshot.setImagePath(String.format("%s/%s.png", ScreenshotUtil.SCREENSHOT_DIR, "capture")); // copied from ScreeshotUtils class
			
			logger.logScreenshot(screenshot);
			File initialFile = new File(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getFullImagePath());
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			Assert.assertEquals(TestStepManager.getParentTestStep().getSnapshots().get(0).getScreenshot().getHtmlSource(), "");
			
			// check no error is raised if we ask the files to be moved at the same place
			TestStepManager.getParentTestStep().getSnapshots().get(0).relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory());
			
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved"));
		}
	}
	
	@Test(groups={"ut"})
	public void testLogHarOk() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		Har har = new Har(new HarLog());
		har.getLog().addPage(new HarPage("title", "a title"));
		logger.logNetworkCapture(har, "main");
		Assert.assertFalse(TestStepManager.getParentTestStep().getHarCaptures().isEmpty());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "main-networkCapture.har").toFile().exists());
	}

	@Test(groups={"ut"})
	public void testRelocateHar() throws IOException {

		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			Har har = new Har(new HarLog());
			har.getLog().addPage(new HarPage("title", "a title"));
			logger.logNetworkCapture(har, "main");
			File initialFile = TestStepManager.getParentTestStep().getHarCaptures().get(0).getFile();
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			
			// relocate
			TestStepManager.getParentTestStep().getHarCaptures().get(0).relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved");
			File movedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved", "main-networkCapture.har").toFile();
			
			Assert.assertTrue(movedFile.exists());
			Assert.assertFalse(initialFile.exists());
			Assert.assertEquals(TestStepManager.getParentTestStep().getHarCaptures().get(0).getFile(), movedFile);
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved"));
		}
	}
	
	/**
	 * Test no error is raised is outputDirectory is null
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testRelocateHarNull() throws IOException {
		
		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			Har har = new Har(new HarLog());
			har.getLog().addPage(new HarPage("title", "a title"));
			logger.logNetworkCapture(har, "main");
			File initialFile = TestStepManager.getParentTestStep().getHarCaptures().get(0).getFile();
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			
			// relocate
			TestStepManager.getParentTestStep().getHarCaptures().get(0).relocate(null);
			
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		}
	}
	
	// GenericFile
	@Test(groups={"ut"})
	public void testLogFile() throws IOException {

		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		logger.logFile(videoFile, "video");

		// check file has been moved to log folder
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile().exists());

	}
	
	/**
	 * Check no error is raised if file is already at the right place
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testLogFileSamePlace() throws IOException {
		
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		File videoFile = File.createTempFile("video", ".avi");
		FileUtils.moveFile(videoFile, Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
		
		logger.logFile(videoFile, "video");
	}
	
	@Test(groups={"ut"})
	public void testLogFileExists() throws IOException {
		
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		File videoFile = File.createTempFile("video", ".avi");
		FileUtils.copyFile(videoFile, Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
		
		logger.logFile(videoFile, "video");
	}
	
	@Test(groups={"ut"})
	public void testRelocateFile() throws IOException {
		
		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			File videoFile = File.createTempFile("video", ".avi");
			videoFile.deleteOnExit();
			logger.logFile(videoFile, "video");
			File initialFile = TestStepManager.getParentTestStep().getFiles().get(0).getFile();
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			
			// relocate
			TestStepManager.getParentTestStep().getFiles().get(0).relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved");
			File movedFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved", videoFile.getName()).toFile();
			
			Assert.assertTrue(movedFile.exists());
			Assert.assertFalse(initialFile.exists());
			Assert.assertEquals(TestStepManager.getParentTestStep().getFiles().get(0).getFile(), movedFile);
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "_moved"));
			
		}
	}
	
	/**
	 * Test no error is raised is outputDirectory is null
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testRelocateFileNull() throws IOException {
		
		try {
			TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
			File videoFile = File.createTempFile("video", ".avi");
			videoFile.deleteOnExit();
			logger.logFile(videoFile, "video");
			File initialFile = TestStepManager.getParentTestStep().getFiles().get(0).getFile();
			Assert.assertTrue(initialFile.exists()); // file exists before moving
			
			// relocate
			TestStepManager.getParentTestStep().getFiles().get(0).relocate(null);
		} finally {
			FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
			
		}
	}
	
	@Test(groups={"ut"})
	public void testLogHarNull() {
		TestStepManager.setCurrentRootTestStep(new TestStep("step", null, new ArrayList<>(), true));
		logger.logNetworkCapture(null, "main");
		Assert.assertTrue(TestStepManager.getParentTestStep().getHarCaptures().isEmpty());
	}
	
	@Test(groups={"ut"})
	public void testLogTestStep(ITestContext context) {
		TestStep testStep = new TestStep("step", null, new ArrayList<>(), true);
		ITestResult testResult = Reporter.getCurrentTestResult();
		
		Reporter.setCurrentTestResult(testResult);
		TestStepManager.setCurrentRootTestStep(testStep);
		TestStepManager.logTestStep(testStep);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().size(), 1);
	}
}
