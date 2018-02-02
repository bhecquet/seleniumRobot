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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.reporter.logger.TestStep;

public class TestTestStep extends GenericTest {

	/**
	 * Checks getFailed correctly compute test step status if action is failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionKo() {
		TestStep step = new TestStep("step1", null);
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", true));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionOk() {
		TestStep step = new TestStep("step1", null);
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed but test step is KO
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithStepKo() {
		TestStep step = new TestStep("step1", null);
		step.setFailed(true);
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is  failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepKo() {
		TestStep step = new TestStep("step1", null);
		TestStep subStep = new TestStep("subStep", null);
		subStep.addAction(new TestAction("action1", true));
		step.addAction(new TestAction("action2", false));
		step.addAction(subStep);
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepOk() {
		TestStep step = new TestStep("step1", null);
		TestStep subStep = new TestStep("subStep", null);
		subStep.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Test that when adding a snapshot to a test step, it's renamed with a name containing test name, step name and index
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testSnapshotRenaming() throws IOException {
		TestStep step = new TestStep("step1", null);
		ScreenShot screenshot = new ScreenShot();
		
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		
		screenshot.setOutputDirectory(tmpImgFile.getParent());
		screenshot.setLocation("http://mysite.com");
		screenshot.setTitle("mysite");
		screenshot.setImagePath(tmpImgFile.getName());
		screenshot.setHtmlSourcePath(tmpHtmlFile.getName());
		
		step.addSnapshot(new Snapshot(screenshot), 0);
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "N-A_0-1_step1-" + tmpImgFile.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "N-A_0-1_step1-" + tmpHtmlFile.getName());
	}
	@Test(groups={"ut"})
	public void testSnapshotRenamingWithSubFolder() throws IOException {
		TestStep step = new TestStep("step1", null);
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
		
		step.addSnapshot(new Snapshot(screenshot), 0);
		
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getImagePath(), "screenshots/N-A_0-1_step1-" + tmpImgFile2.getName());
		Assert.assertEquals(step.getSnapshots().get(0).getScreenshot().getHtmlSourcePath(), "htmls/N-A_0-1_step1-" + tmpHtmlFile2.getName());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups={"ut"})
	public void testToJson() {
		TestStep step = new TestStep("step1", null);
		step.addMessage(new TestMessage("everything OK", MessageType.INFO));
		step.addAction(new TestAction("action2", false));
		
		TestStep subStep = new TestStep("subStep", null);
		subStep.addMessage(new TestMessage("everything in subStep almost OK", MessageType.WARNING));
		subStep.addAction(new TestAction("action1", false));
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
		
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("type"), "step");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("name"), "subStep");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getJSONArray("actions").length(), 2);
		
	}
}
