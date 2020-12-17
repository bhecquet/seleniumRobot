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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.GenericFile;
import com.seleniumtests.reporter.logger.HarCapture;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestValue;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarPage;

public class TestTestStep extends GenericTest {

	/**
	 * Checks getFailed correctly compute test step status if action is failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionKo() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", true, new ArrayList<>()));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionOk() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed but test step is KO
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithStepKo() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.setFailed(true);
		step.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is  failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepKo() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep", null, new ArrayList<>(), true);
		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepOk() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep", null, new ArrayList<>(), true);
		subStep.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Test that when adding a snapshot to a test step, it's renamed with a name containing test name, step name and index
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testSnapshotRenaming() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath(tmpImgFile.getName());
		screenshot.setHtmlSourcePath(tmpHtmlFile.getName());
		
		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "N-A_0-1_step1-" + tmpImgFile.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "N-A_0-1_step1-" + tmpHtmlFile.getName());
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}
	
	/**
	 * Test that when adding a snapshot to a test step, with a custom name, it's renamed with this custom name
	 * Custom name contains forbidden characters
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testSnapshotRenamingWithCustomName() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath(tmpImgFile.getName());
		screenshot.setHtmlSourcePath(tmpHtmlFile.getName());
		
		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FALSE), 0, "my snapshot <name>");
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "my_snapshot_-name-" + tmpImgFile.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "my_snapshot_-name-" + tmpHtmlFile.getName());
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}
	
	@Test(groups={"ut"})
	public void testSnapshotRenamingWithSubFolder() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpImgFile2 = Paths.get(tmpImgFile.getParent(), "screenshots", tmpImgFile.getName()).toFile();
		FileUtils.moveFile(tmpImgFile, tmpImgFile2);
		File tmpHtmlFile = File.createTempFile("html", ".html");
		File tmpHtmlFile2 = Paths.get(tmpHtmlFile.getParent(), "htmls", tmpHtmlFile.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFile, tmpHtmlFile2);
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath("screenshots/" + tmpImgFile2.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtmlFile2.getName());
		
		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "screenshots/N-A_0-1_step1-" + tmpImgFile2.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "htmls/N-A_0-1_step1-" + tmpHtmlFile2.getName());
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}
	
	/**
	 * issue #409: Check that renaming takes into account the index of the parent step
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testSnapshotInSubStep() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpImgFile2 = Paths.get(tmpImgFile.getParent(), "screenshots", tmpImgFile.getName()).toFile();
		FileUtils.moveFile(tmpImgFile, tmpImgFile2);
		File tmpHtmlFile = File.createTempFile("html", ".html");
		File tmpHtmlFile2 = Paths.get(tmpHtmlFile.getParent(), "htmls", tmpHtmlFile.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFile, tmpHtmlFile2);
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath("screenshots/" + tmpImgFile2.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtmlFile2.getName());

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);
		
		TestStep subStep = new TestStep("step1", null, new ArrayList<>(), true);
		ScreenShot screenshotSubStep = new ScreenShot();
		File tmpImgFileSubStep = File.createTempFile("img", ".png");
		File tmpImgFileSubStep2 = Paths.get(tmpImgFileSubStep.getParent(), "screenshots", tmpImgFileSubStep.getName()).toFile();
		FileUtils.moveFile(tmpImgFileSubStep, tmpImgFileSubStep2);
		File tmpHtmlFileSubStep = File.createTempFile("html", ".html");
		File tmpHtmlFileSubStep2 = Paths.get(tmpHtmlFileSubStep.getParent(), "htmls", tmpHtmlFileSubStep.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFileSubStep, tmpHtmlFileSubStep2);
		
		screenshotSubStep.setOutputDirectory(tmpImgFileSubStep.getParent());
		screenshotSubStep.setLocation("http://mysite.com");
		screenshotSubStep.setTitle("mysite");
		screenshotSubStep.setImagePath("screenshots/" + tmpImgFileSubStep2.getName());
		screenshotSubStep.setHtmlSourcePath("htmls/" + tmpHtmlFileSubStep2.getName());
		
		step.addStep(subStep);
		subStep.addSnapshot(new Snapshot(screenshotSubStep, "main", SnapshotCheckType.TRUE), 0, null);
		
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "screenshots/N-A_0-1_step1-" + tmpImgFile2.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "htmls/N-A_0-1_step1-" + tmpHtmlFile2.getName());		
		Assert.assertEquals(subStep.getSnapshots().get(0).getScreenshot().getImagePath(), "screenshots/N-A_0-11_step1-" + tmpImgFileSubStep2.getName());
		Assert.assertEquals(subStep.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "htmls/N-A_0-11_step1-" + tmpHtmlFileSubStep2.getName());
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		tmpImgFileSubStep.deleteOnExit();
		tmpHtmlFileSubStep.deleteOnExit();
	}
	
	@Test(groups={"ut"})
	public void testMultipleSnapshotsInStep() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpImgFile2 = Paths.get(tmpImgFile.getParent(), "screenshots", tmpImgFile.getName()).toFile();
		FileUtils.moveFile(tmpImgFile, tmpImgFile2);
		File tmpHtmlFile = File.createTempFile("html", ".html");
		File tmpHtmlFile2 = Paths.get(tmpHtmlFile.getParent(), "htmls", tmpHtmlFile.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFile, tmpHtmlFile2);
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath("screenshots/" + tmpImgFile2.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtmlFile2.getName());
		
		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);
		
		ScreenShot screenshotSubStep = new ScreenShot();
		File tmpImgFileSubStep = File.createTempFile("img", ".png");
		File tmpImgFileSubStep2 = Paths.get(tmpImgFileSubStep.getParent(), "screenshots", tmpImgFileSubStep.getName()).toFile();
		FileUtils.moveFile(tmpImgFileSubStep, tmpImgFileSubStep2);
		File tmpHtmlFileSubStep = File.createTempFile("html", ".html");
		File tmpHtmlFileSubStep2 = Paths.get(tmpHtmlFileSubStep.getParent(), "htmls", tmpHtmlFileSubStep.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFileSubStep, tmpHtmlFileSubStep2);
		
		screenshotSubStep.setOutputDirectory(tmpImgFileSubStep.getParent());
		screenshotSubStep.setLocation("http://mysite.com");
		screenshotSubStep.setTitle("mysite");
		screenshotSubStep.setImagePath("screenshots/" + tmpImgFileSubStep2.getName());
		screenshotSubStep.setHtmlSourcePath("htmls/" + tmpHtmlFileSubStep2.getName());
		
		step.addSnapshot(new Snapshot(screenshotSubStep, "main", SnapshotCheckType.TRUE), 0, null);
		
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "screenshots/N-A_0-1_step1-" + tmpImgFile2.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "htmls/N-A_0-1_step1-" + tmpHtmlFile2.getName());		
		Assert.assertEquals(step.getSnapshots().get(1).getScreenshot().getImagePath(), "screenshots/N-A_0-2_step1-" + tmpImgFileSubStep2.getName());
		Assert.assertEquals(step.getSnapshots().get(1).getScreenshot().getHtmlSourcePath(), "htmls/N-A_0-2_step1-" + tmpHtmlFileSubStep2.getName());		
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		tmpImgFileSubStep.deleteOnExit();
		tmpHtmlFileSubStep.deleteOnExit();
	}
	
	/**
	 * Check we get all files from a step and its sub steps
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testListAllAttachments() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		
		// create screenshot for main step
		ScreenShot screenshot1 = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpImgFile2 = Paths.get(tmpImgFile.getParent(), "screenshots", tmpImgFile.getName()).toFile();
		FileUtils.moveFile(tmpImgFile, tmpImgFile2);
		File tmpHtmlFile = File.createTempFile("html", ".html");
		File tmpHtmlFile2 = Paths.get(tmpHtmlFile.getParent(), "htmls", tmpHtmlFile.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFile, tmpHtmlFile2);
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		
		screenshot1.setOutputDirectory(tmpImgFile.getParent());
		screenshot1.setLocation("http://mysite.com");
		screenshot1.setTitle("mysite");
		screenshot1.setImagePath("screenshots/" + tmpImgFile2.getName());
		screenshot1.setHtmlSourcePath("htmls/" + tmpHtmlFile2.getName());
		step.addSnapshot(new Snapshot(screenshot1, "main", SnapshotCheckType.FALSE), 0, null);
		
		TestStep subStep = new TestStep("subStep", null, new ArrayList<>(), true);
		
		// create screenshot for sub step
		ScreenShot screenshot2 = new ScreenShot();
		
		File tmpImgFile3 = File.createTempFile("img", ".png");
		File tmpImgFile4 = Paths.get(tmpImgFile3.getParent(), "screenshots", tmpImgFile3.getName()).toFile();
		FileUtils.moveFile(tmpImgFile3, tmpImgFile4);
		
		screenshot2.setOutputDirectory(tmpImgFile3.getParent());
		screenshot2.setLocation("http://mysite.com");
		screenshot2.setTitle("mysite");
		screenshot2.setImagePath("screenshots/" + tmpImgFile4.getName());
		subStep.addSnapshot(new Snapshot(screenshot2, "main", SnapshotCheckType.TRUE), 0, null);
		 
		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);
		
		List<File> attachments = step.getAllAttachments();
		Assert.assertEquals(attachments.size(), 3);
		Assert.assertEquals(attachments.get(0).getName(), "N-A_0-1_step1-" + tmpHtmlFile2.getName());
		Assert.assertEquals(attachments.get(1).getName(), "N-A_0-1_step1-" + tmpImgFile2.getName());
		Assert.assertEquals(attachments.get(2).getName(), "N-A_0-1_subStep-" + tmpImgFile4.getName());
	}
	
	@Test(groups={"ut"}, expectedExceptions=CustomSeleniumTestsException.class)
	public void testTestStepEncodeUnexpected() {
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		step.encode("wrongFormat");
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeHtml() {
		TestStep step = new TestStep("step1 \"'<>&\u0192", null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("html");
		Assert.assertEquals("Step step1 &quot;'&lt;&gt;&amp;&fnof;", encodedTestStep.toString());
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeJson() {
		TestStep step = new TestStep("step1 \"/\\", null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("json");
		Assert.assertEquals("Step step1 \\\"\\/\\\\", encodedTestStep.toString());
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeXml() {
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertEquals("Step step1 &quot;&apos;&lt;&gt;&amp;", encodedTestStep.toString());
	}
	
	/**
	 * check failed status is kept
	 */
	@Test(groups={"ut"})
	public void testTestStepEncodeXmlStatusFailed() {
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		step.setFailed(true);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertTrue(encodedTestStep.getFailed());
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeXmlPasswordKept() {
		TestStep step = new TestStep("step1 \"'<>&", null, Arrays.asList("myPassword"), true);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertTrue(encodedTestStep.getPwdToReplace().contains("myPassword"));
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeXmlExceptionKept() {
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		step.setActionException(new Throwable());
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertNotNull(encodedTestStep.getActionException());
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeXmlHarKept() throws IOException {
		Har har = new Har(new HarLog());
		har.getLog().addPage(new HarPage("title", "a title"));
		HarCapture cap = new HarCapture(har, "main");
		
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		step.setHarCaptures(Arrays.asList(cap));
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertEquals(encodedTestStep.getHarCaptures().get(0), cap);
	}
	
	@Test(groups={"ut"})
	public void testTestStepEncodeXmlFileKept() throws IOException {

		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		
		
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		step.addFile(file);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertEquals(encodedTestStep.getFiles().get(0).getFile(), file.getFile());
	}
	
	@Test(groups={"ut"})
	public void testTestMessageEncodeXml() {
		TestMessage msg = new TestMessage("everything OK \"'<>&", MessageType.INFO);
		TestMessage encodedMsg = msg.encode("xml");
		Assert.assertEquals("everything OK &quot;&apos;&lt;&gt;&amp;", encodedMsg.toString());
	}
	
	@Test(groups={"ut"})
	public void testTestMessageEncodeXmlErrorMessage() {
		TestMessage msg = new TestMessage("everything OK \"'<>&", MessageType.ERROR);
		TestMessage encodedMsg = msg.encode("xml");
		Assert.assertTrue(encodedMsg.getFailed());
	}

	@Test(groups={"ut"})
	public void testTestMessageEncodeXmlPasswordKept() {
		TestMessage msg = new TestMessage("everything OK \"'<>&", MessageType.INFO);
		msg.getPwdToReplace().add("myPassword");
		TestMessage encodedMsg = msg.encode("xml");
		Assert.assertTrue(encodedMsg.getPwdToReplace().contains("myPassword"));
	}
	
	@Test(groups={"ut"})
	public void testTestActionEncodeXml() {
		TestAction action = new TestAction("action2 \"'<>&", false, new ArrayList<>());
		TestAction encodedAction = action.encode("xml");
		Assert.assertEquals("action2 &quot;&apos;&lt;&gt;&amp;", encodedAction.toString());
	}
	
	@Test(groups={"ut"})
	public void testTestActionEncodeXmlFailedStatus() {
		TestAction action = new TestAction("action2 \"'<>&", true, new ArrayList<>());
		TestAction encodedAction = action.encode("xml");
		Assert.assertTrue(encodedAction.getFailed());
	}
	
	@Test(groups={"ut"})
	public void testTestActionEncodeXmlPasswordKept() {
		TestAction action = new TestAction("action2 \"'<>&", false, Arrays.asList("myPassword"));
		TestAction encodedAction = action.encode("xml");
		Assert.assertTrue(encodedAction.getPwdToReplace().contains("myPassword"));
	}
	
	@Test(groups={"ut"})
	public void testTestActionEncodeXmlExceptionKept() {
		TestAction action = new TestAction("action2 \"'<>&", false, new ArrayList<>());
		action.setActionException(new Throwable());
		TestAction encodedAction = action.encode("xml");
		Assert.assertNotNull(encodedAction.getActionException());
	}
	
	@Test(groups={"ut"})
	public void testTestValueEncodeXml() {
		TestValue value = new TestValue("id &", "key <>", "value \"'");
		TestValue encodedValue = value.encode("xml");
		Assert.assertEquals("id &amp;", encodedValue.toString());
		Assert.assertEquals("key &lt;&gt;", encodedValue.getMessage());
		Assert.assertEquals("value &quot;&apos;", encodedValue.getValue());
	}
	
	/** 
	 * check we do not re-encode an already encoded message
	 * 
	 */
	@Test(groups={"ut"})
	public void testTestStepNoreencodeXml() {
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("xml");
		TestStep encodedTestStep2 = encodedTestStep.encode("xml");
		Assert.assertEquals("Step step1 &quot;&apos;&lt;&gt;&amp;", encodedTestStep2.toString());
	}
	

	@Test(groups={"ut"})
	public void testTestStepNoReEncodeJson() {
		TestStep step = new TestStep("step1 \"/\\", null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("json");
		Assert.assertEquals("step1 \\\"\\/\\\\", encodedTestStep.toJson().getString("name"));
	}
	
	/**
	 * Check Step / sub-step encoding with XML
	 */
	@Test(groups={"ut"})
	public void testEncodeXml() {
		TestStep step = new TestStep("step1 \"'<>&", null, new ArrayList<>(), true);
		step.addMessage(new TestMessage("everything OK \"'<>&", MessageType.INFO));
		step.addAction(new TestAction("action2 \"'<>&", false, new ArrayList<>()));
		
		TestStep subStep = new TestStep("subStep", null, new ArrayList<>(), true);
		subStep.addMessage(new TestMessage("everything in subStep almost OK", MessageType.WARNING));
		subStep.addAction(new TestAction("action1 \"'<>&", false, new ArrayList<>()));
		step.addAction(subStep);
		
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertTrue(encodedTestStep.toString().contains("Step step1 &quot;&apos;&lt;&gt;&amp;"));
		Assert.assertTrue(encodedTestStep.toString().contains("everything OK &quot;&apos;&lt;&gt;&amp;"));
		Assert.assertTrue(encodedTestStep.toString().contains("action2 &quot;&apos;&lt;&gt;&amp;"));
		Assert.assertTrue(encodedTestStep.toString().contains("action1 &quot;&apos;&lt;&gt;&amp;"));
		
	}
	
	
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testToJson() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.addMessage(new TestMessage("everything OK", MessageType.INFO));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		
		Har har = new Har(new HarLog());
		har.getLog().addPage(new HarPage("title", "a title"));
		step.addNetworkCapture(new HarCapture(har, "main"));
		
		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		step.addFile(file);
		
		TestStep subStep = new TestStep("subStep", null, new ArrayList<>(), true);
		subStep.addMessage(new TestMessage("everything in subStep almost OK", MessageType.WARNING));
		subStep.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(subStep);
		
		JSONObject stepJson = step.toJson();
		
		Assert.assertEquals(stepJson.getString("type"), "step");
		Assert.assertEquals(stepJson.getString("name"), "step1");
		Assert.assertEquals(stepJson.getJSONArray("actions").length(), 3);
		
		// check actions order
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(0).getString("type"), "message");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(0).getString("messageType"), "INFO");
		
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getString("type"), "action");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getString("name"), "action2");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getBoolean("failed"), false);
		
		Assert.assertEquals(stepJson.getJSONArray("harCaptures").getJSONObject(0).getString("type"), "networkCapture");
		Assert.assertEquals(stepJson.getJSONArray("harCaptures").getJSONObject(0).getString("name"), "main");
		
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("type"), "step");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("name"), "subStep");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getJSONArray("actions").length(), 2);
		
		Assert.assertEquals(stepJson.getJSONArray("files").getJSONObject(0).getString("type"), "file");
		Assert.assertEquals(stepJson.getJSONArray("files").getJSONObject(0).getString("name"), "video file");
		Assert.assertTrue(stepJson.getJSONArray("files").getJSONObject(0).getString("file").contains(".avi"));
		
		
	}
	
	/**
	 * Check that if main step contains masking, they are reported in messages / action / step below
	 */
	@Test(groups={"ut"})
	public void testPasswordMaskingMainStep() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", null, Arrays.asList("passwd"), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		TestStep substep = new TestStep("substep with args: (passwd)", null, new ArrayList<>(), true);
		step.addAction(action);
		step.addMessage(message);
		step.addStep(substep);
		
		Assert.assertEquals(step.getName(), "step1 with args: (bar, ******)");
		Assert.assertEquals(action.getName(), "action in step1 with args: (foo, ******)");
		Assert.assertEquals(message.getName(), "everything OK on ******");
		Assert.assertEquals(substep.getName(), "substep with args: (******)");
	}
	
	/**
	 * Check that if a substep adds password values, parent step is not impacted
	 */
	@Test(groups={"ut"})
	public void testPasswordMaskingSubStep() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", null, new ArrayList<>(), true);
		TestStep substep = new TestStep("substep with args: (passwd)", null, Arrays.asList("passwd"), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		step.addAction(substep);
		substep.addAction(action);
		substep.addMessage(message);
		
		Assert.assertEquals(step.getName(), "step1 with args: (bar, passwd)");
		Assert.assertEquals(action.getName(), "action in step1 with args: (foo, ******)");
		Assert.assertEquals(message.getName(), "everything OK on ******");
		Assert.assertEquals(substep.getName(), "substep with args: (******)");
	}
	
	/**
	 * check that when disabled, password masking does not change test step
	 */
	@Test(groups={"ut"})
	public void testNoPasswordMasking() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", null, new ArrayList<>(), false);
		TestStep substep = new TestStep("substep with args: (passwd)", null, Arrays.asList("passwd"), false);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		step.addAction(substep);
		substep.addAction(action);
		substep.addMessage(message);
		
		Assert.assertEquals(step.getName(), "step1 with args: (bar, passwd)");
		Assert.assertEquals(action.getName(), "action in step1 with args: (foo, passwd)");
		Assert.assertEquals(message.getName(), "everything OK on passwd");
		Assert.assertEquals(substep.getName(), "substep with args: (passwd)");
	}
	

	/**
	 * Check that password masking does not work with empty strings
	 */
	@Test(groups={"ut"})
	public void testPasswordMaskingWithEmptyPassword() {
		List<String> toReplace = new ArrayList<>();
		toReplace.add("");
		TestStep step = new TestStep("step1 with args: (bar, to)", null, toReplace, true);

		Assert.assertEquals(step.getName(), "step1 with args: (bar, to)");
	}
	
	/**
	 * Check that password masking does not work with strings shorter than 5 characters
	 */
	@Test(groups={"ut"})
	public void testPasswordMaskingWithTooShortPassword() {
		List<String> toReplace = new ArrayList<>();
		toReplace.add("passw");
		TestStep step = new TestStep("step1 with args: (bar, passw)", null, toReplace, true);
		
		Assert.assertEquals(step.getName(), "step1 with args: (bar, passw)");
	}
	
	/**
	 * Check that password masking does work with strings longer or equal to 5 characters
	 */
	@Test(groups={"ut"})
	public void testPasswordMaskingWithLongerPassword() {
		List<String> toReplace = new ArrayList<>();
		toReplace.add("passwd");
		TestStep step = new TestStep("step1 with args: (bar, passwd)", null, toReplace, true);
		
		Assert.assertEquals(step.getName(), "step1 with args: (bar, ******)");
	}
	

	/**
	 * Check duration is correctly handled in simple step with action exclusions
	 */
	@Test(groups={"ut"})
	public void testDuration() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.setDuration(5000L);
		step.setDurationToExclude(500L);
		TestAction action = new TestAction("action2", false, new ArrayList<>());
		action.setDurationToExclude(600L);
		step.addAction(action);
		Assert.assertEquals(step.getDuration(), (Long)3900L);
	}
	
	/**
	 * Check duration is correctly handled with sub steps
	 */
	@Test(groups={"ut"})
	public void testDurationWithSubStep() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.setDuration(5000L);
		step.setDurationToExclude(500L);
		
		TestStep subStep = new TestStep("subStep", null, new ArrayList<>(), true);
		TestAction action = new TestAction("action2", false, new ArrayList<>());
		action.setDurationToExclude(600L);
		subStep.addAction(action);
		step.addAction(subStep);
		Assert.assertEquals(step.getDuration(), (Long)3900L);
	}
	
	@Test(groups={"ut"})
	public void testDurationWithSnapshot() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		step.setDuration(5000L);
		step.setDurationToExclude(500L);
		
		ScreenShot screenshot = new ScreenShot();
		screenshot.setDuration(200);
		Snapshot snapshot = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		step.addSnapshot(snapshot, 0, "name");
		Assert.assertEquals(step.getDuration(), (Long)4300L);
	}
	
	@Test(groups={"ut"})
	public void testTestStepPositionAndParent() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		Assert.assertEquals(step.getPosition(), 0);
		Assert.assertNull(step.getParent());
	}
	
	@Test(groups={"ut"})
	public void testTestSubStepPositionAndParent() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", null, new ArrayList<>(), true);
		step.addStep(subStep);
		TestStep subStep2 = new TestStep("subStep2", null, new ArrayList<>(), true);
		step.addStep(subStep2);
		Assert.assertEquals(subStep.getPosition(), 0);
		Assert.assertEquals(subStep2.getPosition(), 1);
		Assert.assertEquals(subStep.getParent(), step);
		Assert.assertEquals(subStep2.getParent(), step);
	}
	
	@Test(groups={"ut"})
	public void testTestSubActionPositionAndParent() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", null, new ArrayList<>(), true);
		step.addStep(subStep);
		TestAction action = new TestAction("subStep2", null, new ArrayList<>());
		step.addAction(action);
		Assert.assertEquals(action.getPosition(), 1);
		Assert.assertEquals(action.getParent(), step);
	}

	@Test(groups={"ut"})
	public void testTestSubMessagePositionAndParent() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", null, new ArrayList<>(), true);
		step.addStep(subStep);
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		step.addAction(message);
		Assert.assertEquals(message.getPosition(), 1);
		Assert.assertEquals(message.getParent(), step);
	}
	
	@Test(groups={"ut"})
	public void testTestSubCapturePositionAndParent() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", null, new ArrayList<>(), true);
		step.addStep(subStep);

		Har har = new Har(new HarLog());
		har.getLog().addPage(new HarPage("title", "a title"));
		HarCapture capture = new HarCapture(har, "main");
		step.addNetworkCapture(capture);
		Assert.assertEquals(capture.getPosition(), 1);
		Assert.assertEquals(capture.getParent(), step);
	}
	
	@Test(groups={"ut"})
	public void testTestSubFilePositionAndParent() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", null, new ArrayList<>(), true);
		step.addStep(subStep);

		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		step.addFile(file);
		Assert.assertEquals(file.getPosition(), 1);
		Assert.assertEquals(file.getParent(), step);
	}
	
	@Test(groups={"ut"})
	public void testTestSubSnapshotPositionAndParent() throws IOException {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", null, new ArrayList<>(), true);
		step.addStep(subStep);
		
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath(tmpImgFile.getName());
		screenshot.setHtmlSourcePath(tmpHtmlFile.getName());
		
		Snapshot snapshot = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		step.addSnapshot(snapshot, 0, null);
		
		Assert.assertEquals(snapshot.getPosition(), 0);
		Assert.assertEquals(snapshot.getParent(), step);
	}
}
