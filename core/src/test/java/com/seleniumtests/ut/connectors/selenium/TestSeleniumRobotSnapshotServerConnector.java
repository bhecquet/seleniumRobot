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
package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.seleniumtests.reporter.logger.FileContent;
import com.seleniumtests.reporter.logger.GenericFile;
import com.seleniumtests.reporter.logger.TestStep;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector.SnapshotComparisonResult;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.reporter.logger.Snapshot;

import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

@PrepareForTest({Unirest.class})
public class TestSeleniumRobotSnapshotServerConnector extends ConnectorsTest {
	
	@Mock
	private Snapshot snapshot;
	
	@Mock
	private ScreenShot screenshot;
	
	@Mock
	private WebElement element;


	@BeforeMethod(groups= {"ut"})
	public void init(final ITestContext testNGCtx) {

		when(element.getRect()).thenReturn(new Rectangle(10,  11, 12, 13));
		SnapshotCheckType snapshotCheckType = SnapshotCheckType.FULL.exclude(element);
		snapshotCheckType.check(SnapshotTarget.PAGE);
		
		when(snapshot.getScreenshot()).thenReturn(screenshot);
		when(snapshot.getName()).thenReturn("snapshot");
		when(snapshot.getCheckSnapshot()).thenReturn(snapshotCheckType);
		when(screenshot.getImagePath()).thenReturn("img.png");
		when(screenshot.getImage()).thenReturn(new FileContent(new File("/home/img.png")));
	}
	
	/**
	 * simulate an inactive server
	 * @throws UnirestException 
	 */
	private SeleniumRobotSnapshotServerConnector configureNotAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenThrow(UnirestException.class);
		when(unirestInstance.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		return new SeleniumRobotSnapshotServerConnector(true, SERVER_URL);
	}
	
	
	@Test(groups= {"ut"})
	public void testServerNotActive() {
		SeleniumRobotSnapshotServerConnector connector = new SeleniumRobotSnapshotServerConnector(false, SERVER_URL);
		Assert.assertFalse(connector.getActive());
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveNotAlive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		Assert.assertFalse(connector.getActive());
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveAndAlive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		Assert.assertTrue(connector.getActive());
	}
	
	// application creation
	@Test(groups= {"ut"})
	public void testCreateApplication() throws UnirestException {		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		connector.setApplicationId(null); // reset to be sure it's recreated
		
		connector.createApplication();
		Assert.assertEquals((int)connector.getApplicationId(), 9);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateApplicationInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		connector.setApplicationId(null); // reset to be sure it's recreated

		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createApplication();
		Assert.assertNull(connector.getApplicationId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateApplicationServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		connector.setApplicationId(null); // reset to be sure it's recreated
		
		connector.createApplication();
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL));
	}
	
	// version creation
	@Test(groups= {"ut"})
	public void testCreateVersionApplicationNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		connector.setApplicationId(null); // reset to be sure it's recreated
		
		connector.createVersion();
		
		// check application has also been created
		Assert.assertNotNull(connector.getApplicationId());
		Assert.assertNotNull(connector.getVersionId());
		Assert.assertEquals((int)connector.getVersionId(), 11);
		verify(connector).createApplication();
	}
	
	@Test(groups= {"ut"})
	public void testCreateVersion() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		connector.setVersionId(null); // reset to be sure it's recreated

		connector.createVersion();
		
		Assert.assertNotNull(connector.getVersionId());
		Assert.assertEquals((int)connector.getVersionId(), 11);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateVersionInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		connector.setVersionId(null); // reset to be sure it's recreated
		
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createVersion();
	}
	
	@Test(groups= {"ut"})
	public void testCreateVersionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		connector.setVersionId(null); // reset to be sure it's recreated
		
		connector.createVersion();
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.VERSION_API_URL));
	}
	
	// environment creation
	@Test(groups= {"ut"})
	public void testCreateEnvironment() throws UnirestException {		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		connector.createEnvironment();
		Assert.assertEquals((int)connector.getEnvironmentId(), 10);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateEnvironmentInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createEnvironment();
		Assert.assertNull(connector.getEnvironmentId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateEnvironmentServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		connector.createEnvironment();
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL));
	}
	
	// session creation => as environment is not defined, error raised
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSessionEnvironmentNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		connector.createSession("Session1");
	}
	
	@Test(groups= {"ut"})
	public void testCreateSession() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.createVersion();
		Integer sessionId = connector.createSession("Session1");
		
		Assert.assertEquals((int)sessionId, 13);
	}
	
	/**
	 * Session name is limited to 100 chars by server, check we strip it
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateSessionLongName() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		MultipartBody request = (MultipartBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 200, "{'id': '14'}", "body");	
		
		connector.createVersion();
		Integer sessionId = connector.createSession("Session1" + StringUtils.repeat("-", 93));
		
		verify(request).field("name", ("Session1" + StringUtils.repeat("-", 92)));
		Assert.assertEquals((int)sessionId, 14);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSessionInError() throws UnirestException {

		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		Integer sessionId = connector.createSession("Session1");
		Assert.assertNull(sessionId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateSessionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createSession("Session1");
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.SESSION_API_URL));
	}
	
	// test case creation
	@Test(groups= {"ut"})
	public void testCreateTestCasePrerequisiteNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		connector.setApplicationId(null);
		Integer testCaseId = connector.createTestCase("Test 1");
		
		Assert.assertEquals((int)testCaseId, 12);
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCase() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.createApplication();
		Integer testCaseId = connector.createTestCase("Test 1");
		
		Assert.assertEquals((int)testCaseId, 12);
	}
	
	/**
	 * Test case name is limited to 150 chars by server, check we strip it
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateTestCaseLongName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '14'}", "body");	
		
		connector.createApplication();
		Integer testCaseId = connector.createTestCase("Test 1" + StringUtils.repeat("-", 145));
		
		verify(request).field("name", ("Test 1" + StringUtils.repeat("-", 144)));
		Assert.assertEquals((int)testCaseId, 14);
	}

	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateTestCaseEmptyName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.createApplication();
		connector.createTestCase("");
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateTestCaseNullName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.createApplication();
		connector.createTestCase(null);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestCaseInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createApplication();
		Integer testCaseId = connector.createTestCase("Test 1");
		Assert.assertNull(testCaseId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCase("Test 1");
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL));
	}
	
	// test case in session creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateTestCaseInSessionNoTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.createTestCaseInSession(connector.createSession("Session1"), null, "Test 1");
	}
	
	// test case in session creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateTestCaseInSessionNoSession() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.createTestCaseInSession(null, connector.createTestCase("Test 1"), "Test 1");
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSession() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");

		Assert.assertEquals((int)testCaseInSessionId, 15);
	}
	
	/**
	 * Test case in session name is limited to 100 chars by server, check we strip it
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSessionLongName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL, 200, "{'id': '14'}", "body");
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1" + StringUtils.repeat("-", 95));
		
		verify(request).field("name", ("Test 1" + StringUtils.repeat("-", 94)));
		Assert.assertEquals((int)testCaseInSessionId, 14);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestCaseInSessionInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Assert.assertNull(testCaseInSessionId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSessionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCaseInSession(1, 1, "Test 1");
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL));
	}
	
	// test step creation
	@Test(groups= {"ut"})
	public void testCreateTestStep() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Assert.assertEquals((int)testStepId, 14);
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestStepLongName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		HttpRequestWithBody request = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL, 200, "{'id': '9'}", "request");	
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1" + StringUtils.repeat("-", 95), testCaseInSessionId);
		
		verify(request).field("name", ("Step 1" + StringUtils.repeat("-", 94)));
		Assert.assertEquals((int)testStepId, 9);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestStepInError() throws UnirestException {	
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Assert.assertNull(testStepId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestStepServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestStep("Step 1", 1);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL));
	}
	
	// getStepList
	@Test(groups= {"ut"})
	public void testGetStepListFromTestCaseNone() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		Assert.assertEquals(connector.getStepListFromTestCase(null).size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void  testGetStepListFromTestCaseWithError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");

		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);

		Assert.assertEquals(connector.getStepListFromTestCase(testCaseInSessionId).size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testGetStepListFromTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': ['1', '2']}");		
		Assert.assertEquals(connector.getStepListFromTestCase(testCaseInSessionId).size(), 2);
		Assert.assertEquals(connector.getStepListFromTestCase(testCaseInSessionId).get(0), "1");
	}
	
	// addTestStepToTestCase
	@Test(groups= {"ut"})
	public void testAddTestStepToTestCase() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': ['1', '2']}");
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.createTestStep("Step 1", testCaseInSessionId);
		
		verify(connector).addTestStepsToTestCases(Arrays.asList("1", "2", "14"), testCaseInSessionId);
	}
	
	@Test(groups= {"ut"})
	public void testAddAlreadyLinkedTestStepToTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': ['1', '14']}");
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.createTestStep("Step 1", testCaseInSessionId);

		verify(connector).addTestStepsToTestCases(Arrays.asList("1", "14"), testCaseInSessionId);
	}
	
	@Test(groups= {"ut"})
	public void testAddNoTestStepToTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.addTestStepsToTestCases(new ArrayList<>(), testCaseInSessionId);
		
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.patch(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL));
	}
	
	// snapshot creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateSnapshotNoStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, null, new ArrayList<>());
	}
	
	
	@Test(groups= {"ut"})
	public void testCreateSnapshot() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}", "body");	
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Integer snapshotId = connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, null);
		
		// check prerequisites has been created
		Assert.assertEquals((int)sessionId, 13);
		Assert.assertEquals((int)snapshotId, 16);
		
		// exclude zones are not sent as not provided
		verify(request, never()).field(eq(SeleniumRobotSnapshotServerConnector.FIELD_EXCLUDE_ZONES), anyString());
		verify(request).field("diffTolerance", "0.0");
	}

	@Test(groups= {"ut"})
	public void testCreateSnapshotWithExcludeZones() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}", "body");	
		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Integer snapshotId = connector.createSnapshot(snapshot, 
				sessionId, 
				testCaseInSessionId, 
				stepResultId, 
				Arrays.asList(new Rectangle(10, 11, 120, 230), 
											new Rectangle(100, 110, 220, 130)));
		
		// check prerequisites has been created
		Assert.assertEquals((int)sessionId, 13);
		Assert.assertEquals((int)snapshotId, 16);
		
		verify(request).field(SeleniumRobotSnapshotServerConnector.FIELD_EXCLUDE_ZONES, "[{\"x\":10,\"y\":11,\"height\":120,\"width\":230},{\"x\":100,\"y\":110,\"height\":220,\"width\":130}]");
	}
	
	/**
	 * 
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testCreateSnapshotNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		connector.createSnapshot(null, sessionId, testCaseInSessionId, stepResultId, new ArrayList<>());
	}
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testCreateSnapshotNull2() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		when(snapshot.getScreenshot()).thenReturn(null);
		connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, null);
	}
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testCreateSnapshotNull3() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		when(screenshot.getImage()).thenReturn(null);
		connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, new ArrayList<>());
	}
	
	/**
	 * snapshot name is limited to 100 chars by server, check we strip it
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateSnapshotLongName() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '14'}", "body");
		when(snapshot.getName()).thenReturn("snapshot" + StringUtils.repeat("-", 93));
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Integer snapshotId = connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, null);
		
		
		verify(request).field("name", ("snapshot" + StringUtils.repeat("-", 92)));
		
		// check prerequisites has been created
		Assert.assertEquals((int)sessionId, 13);
		Assert.assertEquals((int)snapshotId, 14);
	}
	
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSnapshotInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Integer snapshotId = connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, null);
		Assert.assertNull(snapshotId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateSnapshotServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();


		Integer snapshotId = connector.createSnapshot(snapshot, 1, 1, 1, new ArrayList<>());
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL));
		
		Assert.assertNull(snapshotId);
	}
	
	/**
	 * Check the case were seleniumRobot server is not up to date and createSnapshot API only returns id
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCheckSnapshotHasNoDifferencesWithOutdatedServer() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		connector.setVersionId(11); // set it directly has it has been reset on creation
		
		createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16'}");
		
		SnapshotComparisonResult snapshotHasNoDifference = connector.checkSnapshotHasNoDifferences(snapshot, "Test 1", "Step 1");
		
		// In case API is not up to date, checkSnapshotHasNoDifferences returns 'NOT_DONE'
		Assert.assertEquals(snapshotHasNoDifference, SnapshotComparisonResult.NOT_DONE);
	}

	@Test(groups= {"ut"})
	public void testCheckSnapshotHasNoDifferences() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}", "body");
		
		connector.setVersionId(11); // set it directly has it has been reset on creation

		SnapshotComparisonResult snapshotHasNoDifference = connector.checkSnapshotHasNoDifferences(snapshot, "Test 1", "Step 1");

		// When no difference is found, return true
		Assert.assertEquals(snapshotHasNoDifference, SnapshotComparisonResult.OK);
		
		// check exclude zones are sent
		verify(request).field(SeleniumRobotSnapshotServerConnector.FIELD_EXCLUDE_ZONES, "[{\"x\":10,\"y\":11,\"height\":12,\"width\":13}]");
	}
	
	@Test(groups= {"ut"})
	public void testCheckSnapshotHasNoDifferencesNoExcludeZones() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		MultipartBody request = (MultipartBody) createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}", "body");
		
		connector.setVersionId(11); // set it directly has it has been reset on creation
		SnapshotCheckType snapshotCheckType = SnapshotCheckType.FULL;
		snapshotCheckType.check(SnapshotTarget.PAGE);
		when(snapshot.getCheckSnapshot()).thenReturn(snapshotCheckType);
		
		SnapshotComparisonResult snapshotHasNoDifference = connector.checkSnapshotHasNoDifferences(snapshot, "Test 1", "Step 1");
		
		// When no difference is found, return true
		Assert.assertEquals(snapshotHasNoDifference, SnapshotComparisonResult.OK);
		
		// check exclude zones are not sent as they are not present in snapshot
		verify(request, never()).field(eq(SeleniumRobotSnapshotServerConnector.FIELD_EXCLUDE_ZONES), anyString());
	}
	
	@Test(groups= {"ut"})
	public void testCheckSnapshotHasNoDifferencesWithDiff() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		connector.setVersionId(11); // set it directly has it has been reset on creation
		
		createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': null, 'computed': true, 'computingError': '', 'diffPixelPercentage': 10.0, 'tooManyDiffs': true}");
		
		SnapshotComparisonResult snapshotHasNoDifference = connector.checkSnapshotHasNoDifferences(snapshot, "Test 1", "Step 1");
		
		// When there are differences, return false
		Assert.assertEquals(snapshotHasNoDifference, SnapshotComparisonResult.KO);
	}
	
	@Test(groups= {"ut"})
	public void testCheckSnapshotHasNoDifferencesWithComputingError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		connector.setVersionId(11); // set it directly has it has been reset on creation
		createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': null, 'computed': true, 'computingError': 'Error computing', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}");

		SnapshotComparisonResult snapshotHasNoDifference = connector.checkSnapshotHasNoDifferences(snapshot, "Test 1", "Step 1");
		
		// In case of computing errors, it may be due to server problem, so do not retry
		Assert.assertEquals(snapshotHasNoDifference, SnapshotComparisonResult.NOT_DONE);
	}
	
	@Test(groups= {"ut"})
	public void testCheckSnapshotHasNoDifferencesServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		SnapshotComparisonResult snapshotHasNoDifference = connector.checkSnapshotHasNoDifferences(snapshot, "Test 1", "Step 1");
		
		// When server is not there, no error should be raised for this check
		Assert.assertEquals(snapshotHasNoDifference, SnapshotComparisonResult.NOT_DONE);
	}
	
	
	@Test(groups= {"ut"})
	public void testCreateExcludeZone() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Integer snapshotId = connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, null);
		int excludeZoneId = connector.createExcludeZones(new Rectangle(1, 1, 1, 1), snapshotId);
		
		// check prerequisites has been created
		Assert.assertEquals(excludeZoneId, 18);
	}
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateExcludeZoneInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.EXCLUDE_API_URL, 200, "{'id': '18'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Integer snapshotId = connector.createSnapshot(snapshot, sessionId, testCaseInSessionId, stepResultId, null);
		connector.createExcludeZones(new Rectangle(1, 1, 1, 1), snapshotId);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateExcludeZoneNoSnapshotId() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();

		connector.createExcludeZones(new Rectangle(1, 1, 1, 1), null);
	}
	
	@Test(groups= {"ut"})
	public void testCreateExcludeZoneServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		Integer excludeZoneId = connector.createExcludeZones(new Rectangle(1, 1, 1, 1), 0);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL));
		
		Assert.assertNull(excludeZoneId);
	}
	
	// step result creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateStepResultNoStep() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		connector.recordStepResult(true, "", 1, 1, 1, null);
	}

	@Test(groups= {"ut"})
	public void testCreateStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		
		// check prerequisites has been created
		Assert.assertEquals((int)sessionId, 13);
		Assert.assertEquals((int)stepResultId, 17);
	}

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateStepResultInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		Assert.assertNull(stepResultId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateStepResultServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();

		Integer stepResultId = connector.recordStepResult(true, "", 1, 1, 1, 1);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL));
		
		Assert.assertNull(stepResultId);
	}

	@Test(groups= {"ut"})
	public void testCreateStepResultFromTestStep() throws IOException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);

		TestStep testStep = new TestStep("Step 1", null, new ArrayList<>(), true);
		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		testStep.addFile(file);
		testStep.addFile(file);
		Integer stepResultId = connector.recordStepResult(testStep, sessionId, testCaseInSessionId, testStepId);

		// check we record step, then update it and store all files
		verify(connector).recordStepResult(true, "", 0, sessionId, testCaseInSessionId, testStepId);
		String filePath = file.getFile().getAbsolutePath().replace("\\", "/");
	}

	/**
	 * When error occurs uploading file, go to the next file
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testCreateStepResultFromTestStepErrorUploading() throws IOException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		// fail on uploading file
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.FILE_API_URL, 201, "{'id': '18'}", "body");
		when(req.asString()).thenThrow(UnirestException.class);

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);

		TestStep testStep = new TestStep("Step 1", null, new ArrayList<>(), true);
		GenericFile file = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		testStep.addFile(file);
		testStep.addFile(file);

		Integer stepResultId = connector.recordStepResult(testStep, sessionId, testCaseInSessionId, testStepId);

		// check we record step, then update it and store all files
		verify(connector).recordStepResult(true, "", 0, sessionId, testCaseInSessionId, testStepId);
		String filePath = file.getFile().getAbsolutePath().replace("\\", "/");
	}
	@Test(groups= {"ut"})
	public void testCreateStepResultFromTestStepErrorUploading2() throws IOException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);

		TestStep testStep = new TestStep("Step 1", null, new ArrayList<>(), true);
		GenericFile fileOk = new GenericFile(File.createTempFile("video", ".avi"), "video file");
		GenericFile fileKo = new GenericFile(File.createTempFile("video", ".png"), "no file");
		testStep.addFile(fileKo);
		fileKo.getFile().delete(); // delete file so that upload fails
		testStep.addFile(fileOk);
		Integer stepResultId = connector.recordStepResult(testStep, sessionId, testCaseInSessionId, testStepId);

		// check we record step, then update it and store all files
		verify(connector).recordStepResult(true, "", 0, sessionId, testCaseInSessionId, testStepId);
		String filePath = fileOk.getFile().getAbsolutePath().replace("\\", "/");
	}

	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "A test step must be provided")
	public void testCreateStepResultFromTestStepNoStep() {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);

		Integer stepResultId = connector.recordStepResult(null, sessionId, testCaseInSessionId, testStepId);

	}

	@Test(groups= {"ut"})
	public void testUploadFile() throws IOException {
		File tmp = File.createTempFile("video", ".avi");
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		Integer fileId = connector.uploadFile(tmp, 17);
		Assert.assertEquals(fileId, (Integer)18);
	}

	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "stepResultId must be provided")
	public void testUploadFileNoStepResultId() throws IOException {
		File tmp = File.createTempFile("video", ".avi");
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		connector.uploadFile(tmp, null);
	}

	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = ".*does not exist")
	public void testUploadFileWrongFile() throws IOException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		connector.uploadFile(new File(""), 17);
	}

	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testUploadFileInError() throws IOException {
		File tmp = File.createTempFile("video", ".avi");
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.FILE_API_URL, 201, "{'id': '18'}", "body");
		when(req.asString()).thenThrow(UnirestException.class);

		connector.uploadFile(tmp, 17);
	}

	@Test(groups= {"ut"})
	public void testUploadFileInactive() throws IOException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();

		Integer fileId = connector.uploadFile(new File(""), 17);
		Assert.assertNull(fileId);
	}

	@Test(groups= {"ut"})
	public void testUpdateStepResult() {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		Integer stepResultId = connector.updateStepResult("foo", 17);
		Assert.assertEquals(stepResultId, (Integer)17);

	}

	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testUpdateStepResultNoStepResultId() {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		connector.updateStepResult("foo", null);
	}

	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testUpdateStepResultInError() {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		HttpRequest<?> req = createServerMock("PATCH", SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL + "18/", 200, "{'id': '18'}", "body");
		when(req.asString()).thenThrow(UnirestException.class);
		connector.updateStepResult("foo", 18);
	}

	// add logs to test case in session
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testRecordTestLogsNoTestCaseInSession() {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());

		connector.addLogsToTestCaseInSession(null, "some logs");
	}
	
	
	@Test(groups= {"ut"})
	public void testRecordTestLogs() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.addLogsToTestCaseInSession(testCaseInSessionId, "some logs");
	}
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testRecordTestLogsInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("PATCH", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15/", 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.addLogsToTestCaseInSession(testCaseInSessionId, "some logs");
	}
	
	@Test(groups= {"ut"})
	public void testGetComparisonResultOk() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.SUCCESS);
		Assert.assertTrue(errorMessage.toString().isEmpty()); // no computing error message
	}
	
	@Test(groups= {"ut"})
	public void testGetComparisonResultKo() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'completed': true, 'isOkWithSnapshots': false}");		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.FAILURE);
		Assert.assertTrue(errorMessage.toString().isEmpty()); // no computing error message
	}
	

	/**
	 * In case computing errors occur, comparison may return isOkWithSnapshots: null, handle it as "skipped"
	 */
	@Test(groups= {"ut"})
	public void testGetComparisonResultSkipped() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'completed': true, 'isOkWithSnapshots': null, 'computingError': ['img1: computing error', 'img2: computing error']}");		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.SKIP);
		Assert.assertEquals(errorMessage.toString(), "\"img1: computing error\"\n" + 
				"\"img2: computing error\"");
	}
	
	/**
	 * when comparison is not completed, do not interfere with result => returns true
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetComparisonResultNotCompleted() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'completed': false, 'isOkWithSnapshots': true}");		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.SUCCESS);
		Assert.assertTrue(errorMessage.toString().isEmpty()); // no computing error message
	}
	
	/**
	 * when comparison is not available, do not interfere with result => returns true
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetComparisonResultNotAvailable() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'completed': true}");		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.SKIP);
		Assert.assertTrue(errorMessage.toString().isEmpty()); // no computing error message
	}
	
	
	@Test(groups= {"ut"})
	public void testGetComparisonResultTakesTooMuchTime() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'completed': false}");		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.SKIP);
		Assert.assertTrue(errorMessage.toString().isEmpty()); // no computing error message
	}
	
	/**
	 * If error raised during getting comparison, do not interfere with result
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetComparisonResultInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		HttpRequest<?> req = createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'completed': true}");		
		when(req.asString()).thenThrow(UnirestException.class);
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		StringBuilder errorMessage = new StringBuilder();
		int comparisonResult = connector.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
		Assert.assertEquals(comparisonResult, ITestResult.SKIP); // result is 'skip' if we cannot get comparison result
	}
	
	// reference image for step creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateStepReferenceSnapshotNoStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.createStepReferenceSnapshot(snapshot, null);
	}
	
	
	@Test(groups= {"ut"})
	public Integer testCreateStepReferenceSnapshot() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		connector.createStepReferenceSnapshot(snapshot, stepResultId);
		
		// check prerequisites has been created
		Assert.assertEquals((int)sessionId, 13);
		
		return stepResultId;
	}
	
	/**
	 * 
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testCreateStepReferenceSnapshotNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		connector.createStepReferenceSnapshot(null, stepResultId);
	}
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testCreateStepReferenceSnapshotNull2() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		when(snapshot.getScreenshot()).thenReturn(null);
		connector.createStepReferenceSnapshot(snapshot, stepResultId);
	}
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testCreateStepReferenceSnapshotNull3() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		when(screenshot.getImage()).thenReturn(null);
		connector.createStepReferenceSnapshot(snapshot, stepResultId);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateStepReferenceSnapshotInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL, 200, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);

		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		connector.createStepReferenceSnapshot(snapshot, stepResultId);
	}
	
	@Test(groups= {"ut"})
	public void testCreateStepReferenceSnapshotServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();


		connector.createStepReferenceSnapshot(snapshot, 1);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL));
		
	}
	
	// reference image for step => get it
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testGetStepReferenceSnapshotNoStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		connector.getReferenceSnapshot(null);
	}
	
	
	@Test(groups= {"ut"})
	public void testGetStepReferenceSnapshot() throws UnirestException, IOException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		createServerMock("GET", String.format("%s%d/", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL, stepResultId), 200, createImageFromResource("tu/ffLogo1.png"));	
		
		File picture = connector.getReferenceSnapshot(stepResultId);
		
		Assert.assertNotNull(picture);
		Assert.assertTrue(picture.length() > 0);
	}
	
	@Test(groups= {"ut"})
	public void testGetStepReferenceSnapshotNotFound() throws UnirestException, IOException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureMockedSnapshotServerConnection());
		
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		createServerMock("GET", String.format("%s%d/", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL, stepResultId), 404, createImageFromResource("tu/ffLogo1.png"));	
		
		File picture = connector.getReferenceSnapshot(stepResultId);
		
		Assert.assertNull(picture);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testGetStepReferenceSnapshotInError() throws UnirestException, IOException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		
		Integer sessionId = connector.createSession("Session1");
		Integer testCaseId = connector.createTestCase("Test 1");
		Integer testCaseInSessionId = connector.createTestCaseInSession(sessionId, testCaseId, "Test 1");
		Integer testStepId = connector.createTestStep("Step 1", testCaseInSessionId);
		Integer stepResultId = connector.recordStepResult(true, "", 1, sessionId, testCaseInSessionId, testStepId);
		
		HttpRequest<?> req = createServerMock("GET", String.format("%s%d/", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL, stepResultId), 200, createImageFromResource("tu/ffLogo1.png"));	
		when(req.asBytes()).thenThrow(UnirestException.class);
				
		connector.getReferenceSnapshot(stepResultId);
	}
	
	@Test(groups= {"ut"})
	public void testGetStepReferenceSnapshotServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		
		connector.getReferenceSnapshot(1);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL));
		
	}
	

	@Test(groups={"it"})
	public void testDetectFieldsInPicture() {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		JSONObject detectionData = connector.detectFieldsInPicture(snapshot);
		JSONArray fields = detectionData.getJSONArray("fields");
		
		Assert.assertEquals(fields.length(), 2);
		Assert.assertEquals(fields.getJSONObject(0).getString("class_name"), "field_with_label");
		Assert.assertEquals(detectionData.getString("fileName"), "img.png");
	}
	
	/**
	 * Detection fails (e.g: feautre not activated on server, timeout, ...)
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testDetectFieldsInPictureServerInactive() {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();

		connector.detectFieldsInPicture(snapshot);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.DETECT_API_URL));
	}
	
	/**
	 * Detection fails (e.g: feautre not activated on server, timeout, ...)
	 * @throws IOException
	 */
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class, expectedExceptionsMessageRegExp = "field detection failed")
	public void testDetectFieldsInPictureInError() throws IOException {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.DETECT_API_URL, 500, "{'error': 'Field detector disabled'}", "body");	
		
		connector.detectFieldsInPicture(snapshot);
	}
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testDetectFieldsInPictureInError2() throws IOException {

		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("POST", SeleniumRobotSnapshotServerConnector.DETECT_API_URL, 500, "{'id': '9'}", "body");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.detectFieldsInPicture(snapshot);
	}
	
	/**
	 * Provided image does not exist
	 * @throws IOException
	 */
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class, expectedExceptionsMessageRegExp = "Provided snapshot does not exist")
	public void testDetectFieldsInPictureInvalidPicture() {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		connector.detectFieldsInPicture((Snapshot)null);
	}
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class, expectedExceptionsMessageRegExp = "Provided screenshot does not exist")
	public void testDetectFieldsInPictureInvalidPicture2() {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		when(snapshot.getScreenshot()).thenReturn(null);
		connector.detectFieldsInPicture(snapshot);
	}
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class, expectedExceptionsMessageRegExp = "Provided screenshot does not exist")
	public void testDetectFieldsInPictureInvalidPicture3() {
		
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		when(screenshot.getImage()).thenReturn(null);
		connector.detectFieldsInPicture(snapshot);
	}
	
	

	@Test(groups={"it"})
	public void testDetectErrorInPicture() {
		// same as testDetectFieldInPicture()
	}
	
	@Test(groups={"it"})
	public void testGetStepReferenceDetectFieldInformation() {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();

		JSONObject detectionData = connector.getStepReferenceDetectFieldInformation(1, "afcc45");
		JSONArray fields = detectionData.getJSONArray("fields");
		
		Assert.assertEquals(fields.length(), 2);
		Assert.assertEquals(fields.getJSONObject(0).getString("class_name"), "field_with_label");
		
		Assert.assertEquals(detectionData.getString("fileName"), "img.png");
	}
	
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class, expectedExceptionsMessageRegExp = "Cannot get field detector information for reference snapshot")
	public void testGetStepReferenceDetectFieldInformationInError() {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.DETECT_API_URL, 404, "no field detection information", "body");
		
		connector.getStepReferenceDetectFieldInformation(1, "afcc45");
	}
	
	@Test(groups={"it"}, expectedExceptions = SeleniumRobotServerException.class, expectedExceptionsMessageRegExp = "Cannot get field detector information for reference snapshot")
	public void testGetStepReferenceDetectFieldInformationInError2() {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		HttpRequest<?> req = createServerMock("GET", SeleniumRobotSnapshotServerConnector.DETECT_API_URL, 200, "no field detection information", "body");
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.getStepReferenceDetectFieldInformation(1, "afcc45");
	}
	
	@Test(groups={"it"}, expectedExceptions = ConfigurationException.class)
	public void testGetStepReferenceDetectFieldInformationNullVersion() {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();
		
		connector.getStepReferenceDetectFieldInformation(1, null);
	}
	@Test(groups={"it"}, expectedExceptions = ConfigurationException.class)
	public void testGetStepReferenceDetectFieldInformationNullStepResult() {
		SeleniumRobotSnapshotServerConnector connector = configureMockedSnapshotServerConnection();

		connector.getStepReferenceDetectFieldInformation(null, "afcc45");
	}
	
	@Test(groups={"it"})
	public void testGetStepReferenceDetectFieldInformationInactive() {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.getStepReferenceDetectFieldInformation(1, "afcc45");
		
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.get(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.DETECT_API_URL));
	}
	
	
	
	

}
