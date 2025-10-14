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
package com.seleniumtests.ut.reporter.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.seleniumtests.reporter.logger.*;
import com.seleniumtests.util.har.Har;
import com.seleniumtests.util.har.Page;
import com.seleniumtests.util.helper.WaitHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep.StepStatus;


public class TestTestStep extends GenericTest {

	@Test(groups = { "ut" })
	public void testGetFullActionName() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		Assert.assertEquals(step.getFullActionName(), "TestTestStep.step1");
	}
	@Test(groups = { "ut" })
	public void testGetFullActionNameNoOrigin() {
		TestStep step = new TestStep("step1");
		Assert.assertEquals(step.getFullActionName(), "step1");
	}
	
	/**
	 * Checks getStepStatus correctly compute test step status if action is failed
	 * Step is OK
	 */
	@Test(groups = { "ut" })
	public void testGetFailedWithActionKo() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", true, new ArrayList<>()));
		Assert.assertFalse(step.getFailed());
		Assert.assertEquals(step.getStepStatus(), StepStatus.WARNING);
	}

	/**
	 * Checks getStepStatus correctly compute test step status if action is not
	 * failed
	 */
	@Test(groups = { "ut" })
	public void testGetFailedWithActionOk() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		Assert.assertFalse(step.getFailed());
		Assert.assertEquals(step.getStepStatus(), StepStatus.SUCCESS);
	}

	/**
	 * Checks getStepStatus correctly compute test step status if action is not
	 * failed but test step is KO
	 */
	@Test(groups = { "ut" })
	public void testGetFailedWithStepKo() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.setFailed(true);
		step.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		Assert.assertTrue(step.getFailed());
		Assert.assertEquals(step.getStepStatus(), StepStatus.FAILED);
	}

	/**
	 * Checks getStepStatus correctly compute test step status if sub step is failed
	 */
	@Test(groups = { "ut" })
	public void testGetFailedWithActionSubStepKo() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);
		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);
		Assert.assertFalse(step.getFailed());
		Assert.assertEquals(step.getStepStatus(), StepStatus.WARNING);
	}

	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups = { "ut" })
	public void testGetFailedWithActionSubStepOk() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);
		subStep.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		Assert.assertFalse(step.getFailed());
		Assert.assertEquals(step.getStepStatus(), StepStatus.SUCCESS);
	}

	/**
	 * Test that when adding a snapshot to a test step, it's renamed with a name
	 * containing test name, step name and index
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testSnapshotRenaming() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);

		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(),
				"N-A_0-1_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-1_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}
	
	/**
	 * Check case where random part of attachment is removed
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testSnapshotRenamingNoRandom() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setRandomInAttachmentNames(false);
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");
		
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		
		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(),
				"N-A_0-1_step1-.png");
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-1_step1-.html");
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}

	/**
	 * Test that when adding a snapshot to a test step, with a custom name, it's
	 * renamed with this custom name Custom name contains forbidden characters
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testSnapshotRenamingWithCustomName() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "");

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FALSE), 0, "my snapshot <name>");

		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(),
				"my_snapshot_-name--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/my_snapshot_-name--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}

	@Test(groups = { "ut" })
	public void testSnapshotRenamingWithSubFolder() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);

		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(),
				"screenshots/N-A_0-1_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-1_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
	}

	/**
	 * issue #409: Check that renaming takes into account the index of the parent
	 * step
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testSnapshotInSubStep() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);

		TestStep subStep = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		File tmpImgFileSubStep = File.createTempFile("img", ".png");
		File tmpHtmlFileSubStep = File.createTempFile("html", ".html");
		ScreenShot screenshotSubStep = new ScreenShot(tmpImgFileSubStep, tmpHtmlFileSubStep);

		screenshotSubStep.setLocation("http://mysite.com");
		screenshotSubStep.setTitle("mysite");

		step.addStep(subStep);
		subStep.addSnapshot(new Snapshot(screenshotSubStep, "main", SnapshotCheckType.TRUE), 0, null);

		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(),
				"screenshots/N-A_0-1_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-1_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));
		Assert.assertEquals(subStep.getSnapshots().get(0).getScreenshot().getImagePath(),
				"screenshots/N-A_0-11_step1--" + tmpImgFileSubStep.getName().substring(tmpImgFileSubStep.getName().length() - 10));
		Assert.assertEquals(subStep.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-11_step1--" + tmpHtmlFileSubStep.getName().substring(tmpHtmlFileSubStep.getName().length() - 10));

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		tmpImgFileSubStep.deleteOnExit();
		tmpHtmlFileSubStep.deleteOnExit();
	}

	/**
	 * Check we get snapshots from substeps, if requested
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testGetSnapshotSubStep() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);

		TestStep subStep = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		File tmpImgFileSubStep = File.createTempFile("img", ".png");
		File tmpHtmlFileSubStep = File.createTempFile("html", ".html");
		ScreenShot screenshotSubStep = new ScreenShot(tmpImgFileSubStep, tmpHtmlFileSubStep);

		screenshotSubStep.setLocation("http://mysite.com");
		screenshotSubStep.setTitle("mysite");

		step.addStep(subStep);
		subStep.addSnapshot(new Snapshot(screenshotSubStep, "main", SnapshotCheckType.TRUE), 0, null);

		Assert.assertEquals(step.getSnapshots().size(), 1);
		Assert.assertEquals(step.getSnapshots(true).size(), 2);

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		tmpImgFileSubStep.deleteOnExit();
		tmpHtmlFileSubStep.deleteOnExit();
	}

	@Test(groups = { "ut" })
	public void testMultipleSnapshotsInStep() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.TRUE), 0, null);

		File tmpImgFileSubStep = File.createTempFile("img", ".png");
		File tmpHtmlFileSubStep = File.createTempFile("html", ".html");
		ScreenShot screenshotSubStep = new ScreenShot(tmpImgFileSubStep, tmpHtmlFileSubStep);

		screenshotSubStep.setLocation("http://mysite.com");
		screenshotSubStep.setTitle("mysite");

		step.addSnapshot(new Snapshot(screenshotSubStep, "main", SnapshotCheckType.TRUE), 0, null);

		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(),
				"screenshots/N-A_0-1_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-1_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(1).getScreenshot().getImagePath(),
				"screenshots/N-A_0-2_step1--" + tmpImgFileSubStep.getName().substring(tmpImgFileSubStep.getName().length() - 10));
		Assert.assertEquals(step.getSnapshots().get(1).getScreenshot().getHtmlSourcePath(),
				"htmls/N-A_0-2_step1--" + tmpHtmlFileSubStep.getName().substring(tmpHtmlFileSubStep.getName().length() - 10));

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		tmpImgFileSubStep.deleteOnExit();
		tmpHtmlFileSubStep.deleteOnExit();
	}

	/**
	 * Check we get all files from a step and its sub steps
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testGetAllAttachments() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		// create screenshot for main step

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot1 = new ScreenShot(tmpImgFile, tmpHtmlFile);

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();

		screenshot1.setLocation("http://mysite.com");
		screenshot1.setTitle("mysite");
		step.addSnapshot(new Snapshot(screenshot1, "main", SnapshotCheckType.FALSE), 0, null);

		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);

		// create screenshot for sub step

		File tmpImgFile3 = File.createTempFile("img", ".png");
		ScreenShot screenshot2 = new ScreenShot(tmpImgFile3);

		screenshot2.setLocation("http://mysite.com");
		screenshot2.setTitle("mysite");
		subStep.addSnapshot(new Snapshot(screenshot2, "main", SnapshotCheckType.TRUE), 0, null);

		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);

		List<FileContent> attachments = step.getAllAttachments();
		Assert.assertEquals(attachments.size(), 3);
		Assert.assertEquals(attachments.get(0).getFile().getName(), "N-A_0-1_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));
		Assert.assertEquals(attachments.get(1).getFile().getName(), "N-A_0-1_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(attachments.get(2).getFile().getName(), "N-A_0-1_subStep--" + tmpImgFile3.getName().substring(tmpImgFile3.getName().length() - 10));
	}

	/**
	 * Check we get all files from a step and its sub steps from a given SnapshotCheckType
	 *
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testGetAllAttachmentsFilterSnapshotCheckType() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		// create screenshot for main step
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot1 = new ScreenShot(tmpImgFile, tmpHtmlFile);

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();

		screenshot1.setLocation("http://mysite.com");
		screenshot1.setTitle("mysite");
		
		// check that both FALSE & NONE are considered as the same control
		step.addSnapshot(new Snapshot(screenshot1, "main", SnapshotCheckType.FALSE), 0, null);
		step.addSnapshot(new Snapshot(screenshot1, "main", SnapshotCheckType.NONE), 0, null);

		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);

		// create screenshot for sub step
		File tmpImgFile3 = File.createTempFile("img", ".png");
		ScreenShot screenshot2 = new ScreenShot(tmpImgFile3);

		screenshot2.setLocation("http://mysite.com");
		screenshot2.setTitle("mysite");
		subStep.addSnapshot(new Snapshot(screenshot2, "main", SnapshotCheckType.TRUE), 0, null);

		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);

		List<FileContent> attachments = step.getAllAttachments(false, SnapshotCheckType.FALSE);
		// only the HTML and image from first and second added snapshot
		Assert.assertEquals(attachments.size(), 4);
		Assert.assertEquals(attachments.get(0).getFile().getName(), "N-A_0-2_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));
		Assert.assertEquals(attachments.get(1).getFile().getName(), "N-A_0-2_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(attachments.get(2).getFile().getName(), "N-A_0-2_step1--" + tmpHtmlFile.getName().substring(tmpHtmlFile.getName().length() - 10));
		Assert.assertEquals(attachments.get(3).getFile().getName(), "N-A_0-2_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
	}
	
	/**
	 * Check we get all files except HTML when we request it
	 *
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testGetAllAttachmentsFilterHtml() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);

		// create screenshot for main step

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot1 = new ScreenShot(tmpImgFile, tmpHtmlFile);

		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();

		screenshot1.setLocation("http://mysite.com");
		screenshot1.setTitle("mysite");
		step.addSnapshot(new Snapshot(screenshot1, "main", SnapshotCheckType.FALSE), 0, null);

		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);

		// create screenshot for sub step
		File tmpImgFile3 = File.createTempFile("img", ".png");
		ScreenShot screenshot2 = new ScreenShot(tmpImgFile3);

		screenshot2.setLocation("http://mysite.com");
		screenshot2.setTitle("mysite");
		subStep.addSnapshot(new Snapshot(screenshot2, "main", SnapshotCheckType.TRUE), 0, null);

		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);

		List<FileContent> attachments = step.getAllAttachments(true);
		// only the HTML and image from first added snapshot
		Assert.assertEquals(attachments.size(), 2);
		Assert.assertEquals(attachments.get(0).getFile().getName(), "N-A_0-1_step1--" + tmpImgFile.getName().substring(tmpImgFile.getName().length() - 10));
		Assert.assertEquals(attachments.get(1).getFile().getName(), "N-A_0-1_subStep--" + tmpImgFile3.getName().substring(tmpImgFile3.getName().length() - 10));
	}

	@Test(groups = { "ut" }, expectedExceptions = CustomSeleniumTestsException.class)
	public void testTestStepEncodeUnexpected() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.encode("wrongFormat");
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeHtml() {
		TestStep step = new TestStep("step1 \"'<>&\u0192", "step1 \"'<>&\u0192", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION, "\"'<>&\u0192", false);
		step.setFailed(true); // mandatory so that errorCauseDetails is not null
		TestStep encodedTestStep = step.encode("html");
		Assert.assertEquals(encodedTestStep.toString(), "Step step1 &quot;'&lt;&gt;&amp;&fnof;");
		Assert.assertEquals(encodedTestStep.getRootCauseDetails(), "&quot;'&lt;&gt;&amp;&fnof;");
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeJson() {
		TestStep step = new TestStep("step1 \"/\\", "step1 \"/\\", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION, "\"/\\", false);

		step.setFailed(true); // mandatory so that errorCauseDetails is not null
		TestStep encodedTestStep = step.encode("json");
		Assert.assertEquals(encodedTestStep.toString(), "Step step1 \\\"\\/\\\\");
		Assert.assertEquals(encodedTestStep.getAction(), "step1 \\\"\\/\\\\");
		Assert.assertEquals(encodedTestStep.getRootCauseDetails(), "\\\"\\/\\\\");
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeXml() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION,  "\"'<>&", false);
		step.setFailed(true); // mandatory so that errorCauseDetails is not null
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertEquals(encodedTestStep.toString(), "Step step1 &quot;&apos;&lt;&gt;&amp;");
		Assert.assertEquals(encodedTestStep.getRootCauseDetails(), "&quot;&apos;&lt;&gt;&amp;");
	}
	
	@Test(groups = { "ut" })
	public void testDeepCopy() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION, "\"'<>&\u0192", false);
		TestStep encodedTestStep = step.deepCopy();
		Assert.assertEquals(step.getName(), encodedTestStep.getName());
		Assert.assertEquals(step.getStepActions(), encodedTestStep.getStepActions());
		Assert.assertEquals(step.getFailed(), encodedTestStep.getFailed());
		Assert.assertEquals(step.getSnapshots(), encodedTestStep.getSnapshots());
		Assert.assertEquals(step.getFiles(), encodedTestStep.getFiles());
		Assert.assertEquals(step.getDuration(), encodedTestStep.getDuration());
		Assert.assertEquals(step.getStartDate(), encodedTestStep.getStartDate());
		Assert.assertEquals(step.getHarCaptures(), encodedTestStep.getHarCaptures());
		Assert.assertEquals(step.getActionException(), encodedTestStep.getActionException());
		Assert.assertEquals(step.getRootCause(), encodedTestStep.getRootCause());
		Assert.assertEquals(step.getRootCauseDetails(), encodedTestStep.getRootCauseDetails());
		Assert.assertEquals(step.isDisableBugtracker(), encodedTestStep.isDisableBugtracker());
		Assert.assertNull(step.getActionExceptionMessage());
	}

	/**
	 * check failed status is kept
	 */
	@Test(groups = { "ut" })
	public void testTestStepEncodeXmlStatusFailed() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.setFailed(true);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertTrue(encodedTestStep.getFailed());
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeXmlPasswordKept() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, Arrays.asList("myPassword"), true);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertTrue(encodedTestStep.getPwdToReplace().contains("myPassword"));
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeXmlExceptionKept() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.setActionException(new Throwable("foo"));
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertNotNull(encodedTestStep.getActionException());
		Assert.assertEquals(encodedTestStep.getActionExceptionMessage(), "class java.lang.Throwable: foo");
	}
	
	/**
	 * Check that in case of a webdriver exception, platform information and capabilities are not returned 
	 */
	@Test(groups = { "ut" })
	public void testTestStepEncodeXmlWebDriverExceptionKept() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.setActionException(new NoSuchElementException("foo"));
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertNotNull(encodedTestStep.getActionException());
		Assert.assertEquals(encodedTestStep.getActionExceptionMessage(), "class org.openqa.selenium.NoSuchElementException: foo\n");
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeXmlHarKept() throws IOException {
		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		HarCapture cap = new HarCapture(har, "main");

		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.setHarCaptures(Arrays.asList(cap));
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertEquals(encodedTestStep.getHarCaptures().get(0), cap);
	}

	@Test(groups = { "ut" })
	public void testTestStepEncodeXmlFileKept() throws IOException {

		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");

		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.addFile(file);
		TestStep encodedTestStep = step.encode("xml");
		Assert.assertEquals(encodedTestStep.getFiles().get(0).getFile(), file.getFile());
	}

	@Test(groups = { "ut" })
	public void testTestMessageEncodeXml() {
		TestMessage msg = new TestMessage("everything OK \"'<>&", MessageType.INFO);
		TestMessage encodedMsg = msg.encode("xml");
		Assert.assertEquals(encodedMsg.toString(), "everything OK &quot;&apos;&lt;&gt;&amp;");
	}

	@Test(groups = { "ut" })
	public void testTestMessageEncodeXmlErrorMessage() {
		TestMessage msg = new TestMessage("everything OK \"'<>&", MessageType.ERROR);
		TestMessage encodedMsg = msg.encode("xml");
		Assert.assertTrue(encodedMsg.getFailed());
	}

	@Test(groups = { "ut" })
	public void testTestMessageEncodeXmlPasswordKept() {
		TestMessage msg = new TestMessage("everything OK \"'<>&", MessageType.INFO);
		msg.getPwdToReplace().add("myPassword");
		TestMessage encodedMsg = msg.encode("xml");
		Assert.assertTrue(encodedMsg.getPwdToReplace().contains("myPassword"));
	}


	@Test(groups = { "ut" })
	public void testTestValueEncodeXml() {
		TestValue value = new TestValue("id &", "key <>", "value \"'");
		TestValue encodedValue = value.encode("xml");
		Assert.assertEquals(encodedValue.toString(), "id &amp;");
		Assert.assertEquals(encodedValue.getMessage(), "key &lt;&gt;");
		Assert.assertEquals(encodedValue.getValue(), "value &quot;&apos;");
	}

	/**
	 * check we do not re-encode an already encoded message
	 * 
	 */
	@Test(groups = { "ut" })
	public void testTestStepNoreencodeXml() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("xml");
		TestStep encodedTestStep2 = encodedTestStep.encode("xml");
		Assert.assertEquals(encodedTestStep2.toString(), "Step step1 &quot;&apos;&lt;&gt;&amp;");
	}

	@Test(groups = { "ut" })
	public void testTestStepNoReEncodeJson() {
		TestStep step = new TestStep("step1 \"/\\", "step1 \"/\\", this.getClass(), null, new ArrayList<>(), true);
		TestStep encodedTestStep = step.encode("json");
		Assert.assertEquals(encodedTestStep.toJson().getString("name"), "step1 \\\"\\/\\\\");
	}

	/**
	 * Check Step / sub-step encoding with XML
	 */
	@Test(groups = { "ut" })
	public void testEncodeXml() {
		TestStep step = new TestStep("step1 \"'<>&", "step1 \"'<>&", this.getClass(), null, new ArrayList<>(), true);
		step.addMessage(new TestMessage("everything OK \"'<>&", MessageType.INFO));
		step.addAction(new TestAction("action2 \"'<>&", false, new ArrayList<>()));

		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);
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
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testToJson() throws IOException {
		TestStep step = new TestStep("step1 with args: (https://myserver)", "step1", this.getClass(), null, Arrays.asList("foobar"), true);
		step.addMessage(new TestMessage("everything OK", MessageType.INFO));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));

		Har har = new Har();
		har.getLog().addPage(new Page("", "title", "a title"));
		step.addNetworkCapture(new HarCapture(har, "main"));

		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		step.addFile(file);

		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		Snapshot snapshot = new Snapshot(screenshot, "main", SnapshotCheckType.FALSE);
		step.addSnapshot(snapshot, 0, "foo");

		TestStep subStep = new TestStep("subStep with password foobar", "subStep with password foobar", this.getClass(), null, new ArrayList<>(), true);
		subStep.addMessage(new TestMessage("everything in subStep almost OK", MessageType.WARNING));
		subStep.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(subStep);

		JSONObject stepJson = step.toJson();

		Assert.assertEquals(stepJson.getString("type"), "step");
		Assert.assertTrue(stepJson.isNull("exception"));
		Assert.assertEquals(stepJson.getString("name"), "step1 with args: (https://myserver)");
		Assert.assertEquals(stepJson.getString("action"), "step1");
		Assert.assertEquals(stepJson.getString("origin"), "com.seleniumtests.ut.reporter.logger.TestTestStep");
		Assert.assertEquals(stepJson.getString("status"), "SUCCESS");
		Assert.assertEquals(stepJson.getLong("timestamp"), step.getTimestamp().toInstant().toEpochMilli());
		Assert.assertEquals(stepJson.getJSONArray("actions").length(), 3);
		Assert.assertNotNull(stepJson.getLong("timestamp"));

		// check actions order
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(0).getString("type"), "message");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(0).getString("messageType"), "INFO");

		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getString("type"), "action");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getString("name"), "action2");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getBoolean("failed"), false);

		Assert.assertEquals(stepJson.getJSONArray("harCaptures").getJSONObject(0).getString("type"), "networkCapture");
		Assert.assertEquals(stepJson.getJSONArray("harCaptures").getJSONObject(0).getString("name"), "main");

		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("type"), "step");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("name"), "subStep with password ******");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getJSONArray("actions").length(), 2);

		Assert.assertEquals(stepJson.getJSONArray("files").getJSONObject(0).getString("type"), "file");
		Assert.assertEquals(stepJson.getJSONArray("files").getJSONObject(0).getString("name"), "video file");
		
		Assert.assertEquals(stepJson.getJSONArray("snapshots").getJSONObject(0).getString("type"), "snapshot");

	}
	@Test(groups = { "ut" })
	public void testToJsonWithException() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.setActionException(new WebDriverException("KO"));
		step.addMessage(new TestMessage("OK", MessageType.INFO));
		step.addAction(new TestAction("action2", true, new ArrayList<>()));

		JSONObject stepJson = step.toJson();

		Assert.assertEquals(stepJson.getString("type"), "step");
		Assert.assertEquals(stepJson.getString("exception"), "org.openqa.selenium.WebDriverException");
		Assert.assertTrue(stepJson.getString("exceptionMessage").contains("class org.openqa.selenium.WebDriverException: KO"));
		Assert.assertEquals(stepJson.getString("name"), "step1");
		Assert.assertEquals(stepJson.getString("status"), "WARNING");
		Assert.assertEquals(stepJson.getLong("timestamp"), step.getTimestamp().toInstant().toEpochMilli());
		Assert.assertEquals(stepJson.getJSONArray("actions").length(), 2);
		Assert.assertNotNull(stepJson.getLong("timestamp"));


	}

	@Test(groups = { "ut" })
	public void testToString() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.addMessage(new TestMessage("everything OK", MessageType.INFO));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));

//		Har har = new Har(new HarLog());
//		har.getLog().addPage(new HarPage("title", "a title"));
//		step.addNetworkCapture(new HarCapture(har, "main"));

		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		step.addFile(file);

		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);
		subStep.addMessage(new TestMessage("everything in subStep almost OK", MessageType.WARNING));
		subStep.addAction(new TestAction("action1", false, new ArrayList<>()));
		step.addAction(subStep);
		step.addAction(new TestAction("action3", false, new ArrayList<>()));

		Assert.assertEquals(step.toString(), "Step step1\n" + 
				"  - everything OK\n" + 
				"  - action2\n"	+ 
				"  - Step subStep\n" + 
				"    - everything in subStep almost OK\n" + 
				"    - action1\n" + 
				"  - action3");

	}

	/**
	 * Check that if main step contains masking, they are reported in messages /
	 * action / step below
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingMainStep() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, Arrays.asList("passwd"), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		TestStep substep = new TestStep("substep with args: (passwd)", "substep with args: (passwd)", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(action);
		step.addMessage(message);
		step.addStep(substep);

		Assert.assertEquals(step.getName(), "step1 with args: (bar, ******)");
		Assert.assertEquals(action.getName(), "action in step1 with args: (foo, ******)");
		Assert.assertEquals(message.getName(), "everything OK on ******");
		Assert.assertEquals(substep.getName(), "substep with args: (******)");
		Assert.assertEquals(step.toString(),
				"Step step1 with args: (bar, ******)\n" + 
				"  - action in step1 with args: (foo, ******)\n" + 
				"  - everything OK on ******\n" + 
				"  - Step substep with args: (******)");
	}
	
	/**
	 * Check null is refused for passwork masking
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingNull() {
		List<String> pwd = new ArrayList<>();
		pwd.add(null);
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, pwd, true);
		Assert.assertTrue(step.getPwdToReplace().isEmpty());
	}
	
	@Test(groups = { "ut" })
	public void testPasswordMaskingHtmlEncodedMainStep() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, Arrays.asList("passwd"), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		TestStep substep = new TestStep("substep with args: (passwd)", "substep with args: (passwd)", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(action);
		step.addMessage(message);
		step.addStep(substep);
		
		Assert.assertEquals(step.encode("html").getName(), "step1 with args: (bar, ******)");
		Assert.assertEquals(action.encode("html").getName(), "action in step1 with args: (foo, ******)");
		Assert.assertEquals(message.encode("html").getName(), "everything OK on ******");
		Assert.assertEquals(substep.encode("html").getName(), "substep with args: (******)");
		Assert.assertEquals(step.encode("html").toString(),
				"Step step1 with args: (bar, ******)\n" + 
						"  - action in step1 with args: (foo, ******)\n" + 
						"  - everything OK on ******\n" + 
				"  - Step substep with args: (******)");
	}
	
	/**
	 * issue #431: check encoded steps have there password encoded when using special characters
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingHtmlEncodedMainStepWithSpecialCharacters() {
		TestStep step = new TestStep("step1 with args: (bar, passwd§~$µ)", "step1 with args: (bar, passwd§~$µ)", this.getClass(), null, Arrays.asList("passwd§~$µ"), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd§~$µ)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd§~$µ", MessageType.INFO);
		TestStep substep = new TestStep("substep with args: (passwd§~$µ)", "substep with args: (passwd§~$µ)", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(action);
		step.addMessage(message);
		step.addStep(substep);
		
		Assert.assertEquals(step.encode("html").getName(), "step1 with args: (bar, ******)");
		Assert.assertEquals(action.encode("html").getName(), "action in step1 with args: (foo, ******)");
		Assert.assertEquals(message.encode("html").getName(), "everything OK on ******");
		Assert.assertEquals(substep.encode("html").getName(), "substep with args: (******)");
		Assert.assertEquals(step.encode("html").toString(),
				"Step step1 with args: (bar, ******)\n" + 
						"  - action in step1 with args: (foo, ******)\n" + 
						"  - everything OK on ******\n" + 
				"  - Step substep with args: (******)");
	}

	
	@Test(groups = { "ut" })
	public void testPasswordMaskingXmlEncodedMainStepWithSpecialCharacters() {
		TestStep step = new TestStep("step1 with args: (bar, passwd§~$µ)", "step1 with args: (bar, passwd§~$µ)", this.getClass(), null, Arrays.asList("passwd§~$µ"), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd§~$µ)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd§~$µ", MessageType.INFO);
		TestStep substep = new TestStep("substep with args: (passwd§~$µ)", "substep with args: (passwd§~$µ)", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(action);
		step.addMessage(message);
		step.addStep(substep);
		
		Assert.assertEquals(step.encode("xml").getName(), "step1 with args: (bar, ******)");
		Assert.assertEquals(action.encode("xml").getName(), "action in step1 with args: (foo, ******)");
		Assert.assertEquals(message.encode("xml").getName(), "everything OK on ******");
		Assert.assertEquals(substep.encode("xml").getName(), "substep with args: (******)");
		Assert.assertEquals(step.encode("xml").toString(),
				"Step step1 with args: (bar, ******)\n" + 
						"  - action in step1 with args: (foo, ******)\n" + 
						"  - everything OK on ******\n" + 
				"  - Step substep with args: (******)");
	}
	
	/**
	 * Check that if a substep adds password values, parent step is not impacted
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingSubStep() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, new ArrayList<>(), true);
		TestStep substep = new TestStep("substep with args: (passwd)", "substep with args: (passwd)", this.getClass(), null, Arrays.asList("passwd"), true);
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
	@Test(groups = { "ut" })
	public void testNoPasswordMasking() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, new ArrayList<>(), false);
		TestStep substep = new TestStep("substep with args: (passwd)", "substep with args: (passwd)", this.getClass(), null, Arrays.asList("passwd"), false);
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
	@Test(groups = { "ut" })
	public void testPasswordMaskingWithEmptyPassword() {
		List<String> toReplace = new ArrayList<>();
		toReplace.add("");
		TestStep step = new TestStep("step1 with args: (bar, to)", "step1 with args: (bar, to)", this.getClass(), null, toReplace, true);

		Assert.assertEquals(step.getName(), "step1 with args: (bar, to)");
	}

	/**
	 * Check that password masking does not work with strings shorter than 5
	 * characters
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingWithTooShortPassword() {
		List<String> toReplace = new ArrayList<>();
		toReplace.add("passw");
		TestStep step = new TestStep("step1 with args: (bar, passw)", "step1 with args: (bar, passw)", this.getClass(), null, toReplace, true);

		Assert.assertEquals(step.getName(), "step1 with args: (bar, passw)");
	}

	/**
	 * Check that password masking does work with strings longer or equal to 5
	 * characters
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingWithLongerPassword() {
		List<String> toReplace = new ArrayList<>();
		toReplace.add("passwd");
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, toReplace, true);

		Assert.assertEquals(step.getName(), "step1 with args: (bar, ******)");
	}
	
	
	/**
	 * Check we can add a password to list of passwords to mask
	 */
	@Test(groups = { "ut" })
	public void testPasswordMaskingAddPassword() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, new ArrayList<>(), true);
		TestAction action = new TestAction("action in step1 with args: (foo, passwd)", false, new ArrayList<>());
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		TestStep substep = new TestStep("substep with args: (passwd)", "substep with args: (passwd)", this.getClass(), null, new ArrayList<>(), true);
		step.addAction(action);
		step.addMessage(message);
		step.addPasswordToReplace("passwd");
		step.addStep(substep);
		
		// check password is not masked before 'addPasswordToReplace' has been called (only step name will still be replaced)
		Assert.assertEquals(step.getName(), "step1 with args: (bar, ******)");
		Assert.assertEquals(action.getName(), "action in step1 with args: (foo, passwd)");
		Assert.assertEquals(message.getName(), "everything OK on passwd");
		Assert.assertEquals(substep.getName(), "substep with args: (******)");

	}
	
	@Test(groups = { "ut" })
	public void testPasswordMaskingAddPasswordNull() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, new ArrayList<>(), true);
		step.addPasswordToReplace(null);
		
		// check 'null' is refused
		Assert.assertTrue(step.getPwdToReplace().isEmpty());
		Assert.assertEquals(step.getName(), "step1 with args: (bar, passwd)");
	}

	/**
	 * Check duration is correctly handled in simple step with action exclusions
	 */
	@Test(groups = { "ut" })
	public void testDuration() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.setDuration(5000L);
		step.setDurationToExclude(500L);
		TestAction action = new TestAction("action2", false, new ArrayList<>());
		action.setDurationToExclude(600L);
		step.addAction(action);
		Assert.assertEquals(step.getDuration(), (Long) 3900L);
	}

	/**
	 * Check duration is correctly handled with sub steps
	 */
	@Test(groups = { "ut" })
	public void testDurationWithSubStep() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.setDuration(5000L);
		step.setDurationToExclude(500L);

		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);
		TestAction action = new TestAction("action2", false, new ArrayList<>());
		action.setDurationToExclude(600L);
		subStep.addAction(action);
		step.addAction(subStep);
		Assert.assertEquals(step.getDuration(), (Long) 3900L);
	}

	@Test(groups = { "ut" })
	public void testDurationWithSnapshot() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		step.setDuration(5000L);
		step.setDurationToExclude(500L);

		ScreenShot screenshot = new ScreenShot(File.createTempFile("img", ".png"));
		screenshot.setDuration(200);
		Snapshot snapshot = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		step.addSnapshot(snapshot, 0, "name");
		Assert.assertEquals(step.getDuration(), (Long) 4300L);
	}

	@Test(groups = { "ut" })
	public void testUpdateDuration() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		WaitHelper.waitForSeconds(1);
		step.updateDuration();
		step.setStartDate();
		// check duration is computed on timestamp, not startDate
		Assert.assertTrue(step.getDuration() >= 1000);
	}

	@Test(groups = { "ut" })
	public void testTestStepPositionAndParent() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		Assert.assertEquals(step.getPosition(), 0);
		Assert.assertNull(step.getParent());
	}

	@Test(groups = { "ut" })
	public void testTestSubStepPositionAndParent() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", "subStep1", this.getClass(), null, new ArrayList<>(), true);
		step.addStep(subStep);
		TestStep subStep2 = new TestStep("subStep2", "subStep2", this.getClass(), null, new ArrayList<>(), true);
		step.addStep(subStep2);
		Assert.assertEquals(subStep.getPosition(), 0);
		Assert.assertEquals(subStep2.getPosition(), 1);
		Assert.assertEquals(subStep.getParent(), step);
		Assert.assertEquals(subStep2.getParent(), step);
	}

	@Test(groups = { "ut" })
	public void testTestSubActionPositionAndParent() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", "subStep1", this.getClass(), null, new ArrayList<>(), true);
		step.addStep(subStep);
		TestAction action = new TestAction("subStep2", null, new ArrayList<>());
		step.addAction(action);
		Assert.assertEquals(action.getPosition(), 1);
		Assert.assertEquals(action.getParent(), step);
	}

	@Test(groups = { "ut" })
	public void testTestSubMessagePositionAndParent() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", "subStep1", this.getClass(), null, new ArrayList<>(), true);
		step.addStep(subStep);
		TestMessage message = new TestMessage("everything OK on passwd", MessageType.INFO);
		step.addAction(message);
		Assert.assertEquals(message.getPosition(), 1);
		Assert.assertEquals(message.getParent(), step);
	}
//
//	@Test(groups = { "ut" })
//	public void testTestSubCapturePositionAndParent() throws IOException {
//		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
//		TestStep subStep = new TestStep("subStep1", "subStep1", this.getClass(), null, new ArrayList<>(), true);
//		step.addStep(subStep);
//
//		Har har = new Har(new HarLog());
//		har.getLog().addPage(new HarPage("title", "a title"));
//		HarCapture capture = new HarCapture(har, "main");
//		step.addNetworkCapture(capture);
//		Assert.assertEquals(capture.getPosition(), 1);
//		Assert.assertEquals(capture.getParent(), step);
//	}

	@Test(groups = { "ut" })
	public void testTestSubFilePositionAndParent() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", "subStep1", this.getClass(), null, new ArrayList<>(), true);
		step.addStep(subStep);

		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		step.addFile(file);
		Assert.assertEquals(file.getPosition(), 1);
		Assert.assertEquals(file.getParent(), step);
	}

	@Test(groups = { "ut" })
	public void testTestSubSnapshotPositionAndParent() throws IOException {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("subStep1", "subStep1", this.getClass(), null, new ArrayList<>(), true);
		step.addStep(subStep);


		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);

		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");

		Snapshot snapshot = new Snapshot(screenshot, "main", SnapshotCheckType.TRUE);
		step.addSnapshot(snapshot, 0, null);

		Assert.assertEquals(snapshot.getPosition(), 0);
		Assert.assertEquals(snapshot.getParent(), step);
	}
	

	@Test(groups = { "ut" })
	public void testTestStepWithrootCause() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION, "details", false);

		step.setFailed(true); // mandatory so that errorCauseDetails is not null
		Assert.assertEquals(step.getRootCause(), RootCause.REGRESSION);
		Assert.assertEquals(step.getRootCauseDetails(), "details");
	}
	
	/**
	 * When step is successful, root cause is null
	 */
	@Test(groups = { "ut" })
	public void testTestStepSuccessWithrootCause() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION, "details", false);
		
		Assert.assertNull(step.getRootCause());
		Assert.assertNull(step.getRootCauseDetails());
	}
	
	/**
	 * Check error cause of a sub step is transferred to root step
	 */
	@Test(groups = { "ut" })
	public void testTestStepWithrootCauseInSubStep() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		TestStep subStep = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true, RootCause.REGRESSION, "details", false);

		subStep.setFailed(true); // mandatory so that errorCauseDetails is not null
		step.addStep(subStep);
		Assert.assertEquals(step.getRootCause(), RootCause.REGRESSION);
		Assert.assertEquals(step.getRootCauseDetails(), "details");
	}
	
	@Test(groups = { "ut" })
	public void testTestStepWithRootCauseNone() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true, RootCause.NONE, "details", false);
		Assert.assertNull(step.getRootCause());
		Assert.assertNull(step.getRootCauseDetails());
	}
	
	@Test(groups = { "ut" })
	public void testIsTestEndStep() {
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		Assert.assertFalse(step.isTestEndStep());
	}
	
	@Test(groups = { "ut" })
	public void testIsTestEndStep2() {
		TestStep step = new TestStep("Test end", "Test end", this.getClass(), null, new ArrayList<>(), true);
		Assert.assertTrue(step.isTestEndStep());
	}
	
	
	/**
	 * Check we move all attachments that are located in "before-xxx" folders
	 *
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testMoveAttachements() throws IOException {
		try {
			FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().replace("testMoveAttachements", "before-testMoveAttachements")));
		} catch (IOException e) {
		}
		
		TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
		
		// create screenshot for main step
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot1 = new ScreenShot(tmpImgFile, tmpHtmlFile);
		
		// move the capture to a "before-" folde to simulate the case where BeforeMethod executes a driver which takes screenshots
		File imageInBeforeFolder = new File(screenshot1.getImage().getFile().getAbsolutePath().replace("testMoveAttachements", "before-testMoveAttachements"));
		File htmlInBeforeFolder = new File(screenshot1.getHtml().getFile().getAbsolutePath().replace("testMoveAttachements", "before-testMoveAttachements"));
		imageInBeforeFolder.getParentFile().mkdirs();
		FileUtils.moveFile(screenshot1.getImage().getFile(), imageInBeforeFolder);
		FileUtils.moveFile(screenshot1.getHtml().getFile(), htmlInBeforeFolder);
		screenshot1.getImage().setFile(imageInBeforeFolder);
		screenshot1.getHtml().setFile(htmlInBeforeFolder);
		screenshot1.setOutputDirectory(imageInBeforeFolder.getParentFile().getParentFile().getAbsolutePath());
		
		tmpImgFile.deleteOnExit();
		tmpHtmlFile.deleteOnExit();
		
		screenshot1.setLocation("http://mysite.com");
		screenshot1.setTitle("mysite");
		step.addSnapshot(new Snapshot(screenshot1, "main", SnapshotCheckType.FALSE), 0, null);
		
		TestStep subStep = new TestStep("subStep", "subStep", this.getClass(), null, new ArrayList<>(), true);
		
		// create screenshot for sub step
		File tmpImgFile3 = File.createTempFile("img", ".png");
		ScreenShot screenshot2 = new ScreenShot(tmpImgFile3);
		
		screenshot2.setLocation("http://mysite.com");
		screenshot2.setTitle("mysite");
		subStep.addSnapshot(new Snapshot(screenshot2, "main", SnapshotCheckType.TRUE), 0, null);
		
		subStep.addAction(new TestAction("action1", true, new ArrayList<>()));
		step.addAction(new TestAction("action2", false, new ArrayList<>()));
		step.addAction(subStep);
		
		Path outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest");
		step.moveAttachments(outputDir.toString());
		Collection<File> files = FileUtils.listFiles(outputDir.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		Assert.assertEquals(files.size(), 2); // only files in "before-" folder has been moved
	}
	
}
