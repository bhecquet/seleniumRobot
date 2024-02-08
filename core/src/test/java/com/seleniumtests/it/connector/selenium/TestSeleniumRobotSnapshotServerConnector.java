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
package com.seleniumtests.it.connector.selenium;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.info.ImageLinkInfo;
import com.seleniumtests.reporter.info.Info;
import com.seleniumtests.reporter.info.MultipleInfo;
import com.seleniumtests.reporter.info.StringInfo;
import com.seleniumtests.reporter.logger.FileContent;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector.SnapshotComparisonResult;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class TestSeleniumRobotSnapshotServerConnector extends GenericTest {

	SeleniumRobotSnapshotServerConnector connector;
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);
		
		// pass the token via  -DseleniumRobotServerToken=xxxxxx
		connector = new SeleniumRobotSnapshotServerConnector(true, "http://localhost:8000", SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken());
		if (!connector.getActive()) {
			throw new SkipException("no seleniumrobot server available");
		}
	}
	
	@Test(groups={"it"})
	public void testCreateApplication() {
		connector.createApplication();
		Assert.assertNotNull(connector.getApplicationId());
	}
	
	@Test(groups={"it"})
	public void testCreateEnvironment() {
		connector.createEnvironment();
		Assert.assertNotNull(connector.getEnvironmentId());
	}
	
	@Test(groups={"it"})
	public void testCreateVersion() {
		connector.createVersion();
		Assert.assertNotNull(connector.getApplicationId());
		Assert.assertNotNull(connector.getVersionId());
	}
	
	@Test(groups={"it"})
	public void testCreateSession() {
		connector.createApplication();
		Assert.assertNotNull(connector.getEnvironmentId());
		Assert.assertNotNull(connector.createSession("Session1"));
		Assert.assertNotNull(connector.getVersionId());
	}
	
	/**
	 * create a test case
	 */
	@Test(groups={"it"})
	public void testCreateTestCase() {
		connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Assert.assertNotNull(testCaseId);
		Assert.assertNotNull(connector.getApplicationId());
	}
	
	/**
	 * create a test case and check it's added to session
	 */
	@Test(groups={"it"})
	public void testCreateTestCaseInSession() {
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1", "SUCCESS", "LOCAL", null);
		Assert.assertNotNull(sessionId);
		Assert.assertNotNull(testCaseInSessionId);
		Assert.assertNotNull(connector.getApplicationId());
	}

	@Test(groups={"it"})
	public void testSendTestInfos() {
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1", "SUCCESS", "LOCAL", null);

		Map<String, Info> infos = new HashMap<>();
		MultipleInfo mInfo = new MultipleInfo(TestStepManager.LAST_STATE_NAME);
		File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "image", "imageCapture.png").toFile();
		mInfo.addInfo(new ImageLinkInfo(new FileContent(imageFile)));
		infos.put(TestStepManager.LAST_STATE_NAME, mInfo);
		infos.put("Issue", new StringInfo("ID=12"));

		connector.recordTestInfo(infos, testCaseInSessionId);
	}
	
	/**
	 * create a test step and check it's added to test case
	 */
	@Test(groups={"it"})
	public void testCreateTestStep() {
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1", "SUCCESS", "LOCAL", "some description");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		connector.createTestStep("Step 2", testCaseInSessionId);
		Assert.assertNotNull(testStepId);
		
		List<String> testSteps = connector.getStepListFromTestCase(testCaseInSessionId);
		Assert.assertEquals(testSteps.size(), 2);
		Assert.assertEquals(testSteps.get(0), testStepId.toString());
	}
	
	@Test(groups={"it"})
	public Integer testCreateStepReferenceSnapshot() throws IOException {
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 2");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 2", "SUCCESS", "LOCAL", "some description");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "logs", 1, testCaseInSessionId, testStepId);
		
		File image = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "img.png").toFile();
		image.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), image);
		ScreenShot screenshot = new ScreenShot(image, null, "");
		Snapshot snapshot = new Snapshot(screenshot, "img", SnapshotCheckType.TRUE);
		
		connector.createStepReferenceSnapshot(snapshot, stepResultId);
		
		File referenceImage = connector.getReferenceSnapshot(stepResultId);
		Assert.assertNotNull(referenceImage);
		Assert.assertEquals(FileUtils.sizeOf(referenceImage), 5892);
		
		return stepResultId;
	}
	
	/**
	 * create a snapshot
	 * @throws IOException 
	 */
	@Test(groups={"it"})
	public void testCreateSnapshot() throws IOException {

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 2");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 2", "SUCCESS", "LOCAL", "some description");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "logs", 1, testCaseInSessionId, testStepId);
		File image = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "img.png").toFile();
		image.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), image);
		ScreenShot screenshot = new ScreenShot(image, null, "");
		Snapshot snapshot = new Snapshot(screenshot, "img", SnapshotCheckType.TRUE);
		Integer snapshotId = connector.createSnapshot(snapshot, stepResultId, Arrays.asList(new Rectangle(10, 11, 120, 230),
				new Rectangle(100, 110, 220, 130)));
		
		Assert.assertNotNull(snapshotId);
	}
	
	
	/**
	 * create a snapshot
	 * @throws IOException 
	 */
	@Test(groups={"it"})
	public void testCheckSnapshotHasNoDifferences() throws IOException {

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 2");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 2", "SUCCESS", "LOCAL", "some description");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "logs", 1, testCaseInSessionId, testStepId);
		File image = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "img.png").toFile();
		image.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), image);
		ScreenShot screenshot = new ScreenShot(image, null, "");
		Snapshot snapshot = new Snapshot(screenshot, "img", SnapshotCheckType.TRUE);
		SnapshotComparisonResult comparisonResult = connector.checkSnapshotHasNoDifferences(snapshot, "Test 2", "Step 1");
		
		Assert.assertEquals(comparisonResult, SnapshotComparisonResult.OK);
	}
	
	/**
	 * create a snapshot
	 * @throws IOException 
	 */
	@Test(groups={"it"})
	public void testRecordStepResult() throws IOException {
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 2");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 2", "SUCCESS", "LOCAL", "some description");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "some logs", 1, testCaseInSessionId, testStepId);
		
		Assert.assertNotNull(stepResultId);
	}
	
	@Test(groups={"it"})
	public void testDetectErrorInPicture() throws IOException {
		
		File image = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "img.png").toFile();
		image.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/imageFieldDetection/login-page-error.jpg"), image);
		ScreenShot screenshot = new ScreenShot(image, null, "");
		Snapshot snapshot = new Snapshot(screenshot, "img", SnapshotCheckType.FALSE);
		JSONObject detectionData = connector.detectErrorInPicture(snapshot);
		JSONArray errors = detectionData.getJSONArray("fields");
		
		Assert.assertEquals(errors.length(), 1);
		Assert.assertEquals(errors.getJSONObject(0).getString("text"), "Votre email/mot de passe est incorrect");
	}
	
	@Test(groups={"it"})
	public void testDetectFieldsInPicture() throws IOException {
		
		File image = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "img.png").toFile();
		image.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/imageFieldDetection/browserCapture.png"), image);
		ScreenShot screenshot = new ScreenShot(image, null, "");
		Snapshot snapshot = new Snapshot(screenshot, "img", SnapshotCheckType.FALSE);
		JSONObject detectionData = connector.detectFieldsInPicture(snapshot);
		JSONArray fields = detectionData.getJSONArray("fields");
		
		Assert.assertEquals(fields.length(), 50);
		Assert.assertEquals(fields.getJSONObject(0).getString("text"), "Test delay");
	}
	
	@Test(groups={"it"})
	public void testGetStepReferenceDetectFieldInformation() throws IOException {
		Integer stepResultId = testCreateStepReferenceSnapshot();
		JSONObject detectionData = connector.getStepReferenceDetectFieldInformation(stepResultId, "afcc45");
		
		// no field found because the used picture does not contain any
		// we only check no error occurs calling the service
		Assert.assertNull(detectionData.get("error"));
		
	}

	
	
	
}
