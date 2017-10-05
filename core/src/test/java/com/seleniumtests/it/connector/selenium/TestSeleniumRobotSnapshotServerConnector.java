package com.seleniumtests.it.connector.selenium;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class TestSeleniumRobotSnapshotServerConnector extends GenericTest {

	SeleniumRobotSnapshotServerConnector connector;
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl("http://localhost:8002");
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		connector = new SeleniumRobotSnapshotServerConnector();
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
		connector.createSession();
		Assert.assertNotNull(connector.getEnvironmentId());
		Assert.assertNotNull(connector.getSessionId());
		Assert.assertNotNull(connector.getVersionId());
	}
	
	/**
	 * create a test case
	 */
	@Test(groups={"it"})
	public void testCreateTestCase() {
		connector.createSession();
		connector.createTestCase("Test 1");
		Assert.assertNotNull(connector.getTestCaseId("Test 1"));
		Assert.assertNotNull(connector.getApplicationId());
	}
	
	/**
	 * create a test case and check it's added to session
	 */
	@Test(groups={"it"})
	public void testCreateTestCaseInSession() {
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		Assert.assertNotNull(connector.getSessionId());
		Assert.assertNotNull(connector.getTestCaseInSessionId());
		Assert.assertNotNull(connector.getApplicationId());
	}
	
	/**
	 * create a test step and check it's added to test case
	 */
	@Test(groups={"it"})
	public void testCreateTestStep() {
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		Assert.assertNotNull(connector.getTestStepId());
		
		List<String> testSteps = connector.getStepListFromTestCase();
		Assert.assertEquals(testSteps.size(), 1);
		Assert.assertEquals(testSteps.get(0), connector.getTestStepId().toString());
	}
	
	/**
	 * create a snapshot
	 * @throws IOException 
	 */
	@Test(groups={"it"})
	public void testCreateSnapshot() throws IOException {
		connector.createSession();
		connector.createTestCase("Test 2");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "logs", 1);
		File image = File.createTempFile("image-", ".png");
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), image);
		connector.createSnapshot(image);
		
		Assert.assertNotNull(connector.getSnapshotId());
	}
	
	/**
	 * create a snapshot
	 * @throws IOException 
	 */
	@Test(groups={"it"})
	public void testRecordTestResult() throws IOException {
		connector.createSession();
		connector.createTestCase("Test 2");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "some logs", 1);
		
		Assert.assertNotNull(connector.getStepResultId());
	}
	
	
}
