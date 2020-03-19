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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;

@PrepareForTest({Unirest.class})
public class TestSeleniumRobotSnapshotServerConnector extends MockitoTest {
	
	private static final String SERVER_URL = "http://localhost:4321";
	
	@Mock
	private GetRequest getAliveRequest;
	
	@Mock
	private HttpRequestWithBody postRequest;
	
	@Mock
	private HttpResponse<String> responseAliveString;
	
	@Mock
	private Snapshot snapshot;
	
	@Mock
	private ScreenShot screenshot;

	@BeforeMethod(groups= {"ut"})
	public void init(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		PowerMockito.mockStatic(Unirest.class);
		
		when(snapshot.getScreenshot()).thenReturn(screenshot);
		when(screenshot.getImagePath()).thenReturn("img.png");
	}
	
	/**
	 * simulate an alive sever responding to all requests
	 * @throws UnirestException 
	 */
	private SeleniumRobotSnapshotServerConnector configureAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenReturn(responseAliveString);
		when(responseAliveString.getStatus()).thenReturn(200);
		when(Unirest.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		
		// set default reply from server. To override this behaviour, redefine some steps in test after connector creation
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL, 200, "{'id': '9'}");	
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL, 200, "{'id': '10'}");	
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '11'}");	
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '12'}");
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL, 200, "{'id': '15'}");
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16'}");
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL, 200, "{'id': '17'}");
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 200, "{'id': '13'}");	
		createSnapshotServerMock("PATCH", SeleniumRobotSnapshotServerConnector.SESSION_API_URL + "13/", 200, "{\"id\":13,\"sessionId\":\"4b2e32f4-69dc-4f05-9644-4287acc2c9ac\",\"date\":\"2017-07-24\",\"browser\":\"*none\",\"environment\":\"DEV\",\"version\":2}");		
		createSnapshotServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': []}");		
		createSnapshotServerMock("PATCH", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15/", 200, "{\"id\":12,\"name\":\"Test 1\",\"version\":11,\"testSteps\":[14]}");		
		createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL, 200, "{'id': '14'}");
		createSnapshotServerMock("GET", SeleniumRobotServerConnector.NAMED_APPLICATION_API_URL, 200, "{'id': 9}");		
		createSnapshotServerMock("GET", SeleniumRobotServerConnector.NAMED_ENVIRONMENT_API_URL, 200, "{'id': 10}");		
		createSnapshotServerMock("GET", SeleniumRobotServerConnector.NAMED_TESTCASE_API_URL, 200, "{'id': 12}");		
		createSnapshotServerMock("GET", SeleniumRobotServerConnector.NAMED_VERSION_API_URL, 200, "{'id': 11}");		
		
		SeleniumRobotSnapshotServerConnector connector = new SeleniumRobotSnapshotServerConnector(true, SERVER_URL);
		
		// reset default value to force creation
		connector.setVersionId(null);
		connector.setTestCaseId(null);
		return connector;
	}
	
	/**
	 * simulate an inactive server
	 * @throws UnirestException 
	 */
	private SeleniumRobotSnapshotServerConnector configureNotAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenThrow(UnirestException.class);
		when(Unirest.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		return new SeleniumRobotSnapshotServerConnector(true, SERVER_URL);
	}
	
	/**
	 * Method for creating snapshot server reply mock
	 * @throws UnirestException 
	 */
	private BaseRequest createSnapshotServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		
		@SuppressWarnings("unchecked")
		HttpResponse<String> response = mock(HttpResponse.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
		
		switch(requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class); 
				when(Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);
				when(getRequest.asString()).thenReturn(response);
				when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
				when(response.getStatus()).thenReturn(statusCode);
				when(response.getBody()).thenReturn(replyData);
				return getRequest;
			case "POST":
				when(Unirest.post(SERVER_URL + apiPath)).thenReturn(postRequest);
			case "PATCH":
				when(Unirest.patch(SERVER_URL + apiPath)).thenReturn(postRequest);
				when(postRequest.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyBoolean())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				when(requestMultipartBody.asString()).thenReturn(response);
				when(response.getStatus()).thenReturn(statusCode);
				when(response.getBody()).thenReturn(replyData);
				return requestMultipartBody;
			
		}
		return null;
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
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		Assert.assertTrue(connector.getActive());
	}
	
	// application creation
	@Test(groups= {"ut"})
	public void testCreateApplication() throws UnirestException {		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.setApplicationId(null); // reset to be sure it's recreated
		
		connector.createApplication();
		Assert.assertEquals((int)connector.getApplicationId(), 9);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateApplicationInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.setApplicationId(null); // reset to be sure it's recreated

		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL, 200, "{'id': '9'}");	
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
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
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
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		connector.setVersionId(null); // reset to be sure it's recreated

		connector.createVersion();
		
		Assert.assertNotNull(connector.getVersionId());
		Assert.assertEquals((int)connector.getVersionId(), 11);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateVersionInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.setVersionId(null); // reset to be sure it's recreated
		
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '9'}");	
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
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		connector.createEnvironment();
		Assert.assertEquals((int)connector.getEnvironmentId(), 10);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateEnvironmentInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL, 200, "{'id': '9'}");	
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
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		connector.setEnvironmentId(null); // reset to be sure it's recreated
		
		connector.createSession();
	}
	
	@Test(groups= {"ut"})
	public void testCreateSession() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createVersion();
		connector.createSession();
		
		Assert.assertEquals((int)connector.getSessionId(), 13);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSessionInError() throws UnirestException {

		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createSession();
		Assert.assertNull(connector.getSessionId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateSessionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createSession();
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.SESSION_API_URL));
	}
	
	// test case creation
	@Test(groups= {"ut"})
	public void testCreateTestCasePrerequisiteNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		connector.setTestCaseId(null);
		
		connector.createSession();
		connector.createTestCase("Test 1");
		
		Assert.assertEquals((int)connector.getTestCaseId("Test 1"), 12);
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCase() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createApplication();
		connector.createTestCase("Test 1");
		
		Assert.assertEquals((int)connector.getTestCaseId("Test 1"), 12);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateTestCaseEmptyName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createApplication();
		connector.createTestCase("");
	}
	
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateTestCaseNullName() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createApplication();
		connector.createTestCase(null);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestCaseInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createApplication();
		connector.createTestCase("Test 1");
		Assert.assertNull(connector.getTestCaseId("Test 1"));
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
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createTestCaseInSession();
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSessionPrerequisiteNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		
		Assert.assertEquals((int)connector.getTestCaseInSessionId(), 15);
		
		// check session has also been created
		Assert.assertEquals((int)connector.getSessionId(), 13);
		verify(connector).createSession();
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSession() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();

		Assert.assertEquals((int)connector.getTestCaseInSessionId(), 15);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestCaseInSessionInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL, 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		Assert.assertNull(connector.getTestCaseInSessionId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSessionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCaseInSession();
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL));
	}
	
	// test step creation
	@Test(groups= {"ut"})
	public void testCreateTestStep() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		Assert.assertEquals((int)connector.getTestStepId(), 14);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestStepInError() throws UnirestException {	
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL, 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		Assert.assertNull(connector.getTestStepId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestStepServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestStep("Step 1");
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL));
	}
	
	// getStepList
	@Test(groups= {"ut"})
	public void testGetStepListFromTestCaseNone() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		Assert.assertEquals(connector.getStepListFromTestCase().size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void  testGetStepListFromTestCaseWithError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();

		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);

		Assert.assertEquals(connector.getStepListFromTestCase().size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testGetStepListFromTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		
		createSnapshotServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': ['1', '2']}");		
		Assert.assertEquals(connector.getStepListFromTestCase().size(), 2);
		Assert.assertEquals(connector.getStepListFromTestCase().get(0), "1");
	}
	
	// addTestStepToTestCase
	@Test(groups= {"ut"})
	public void testAddTestStepToTestCase() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		createSnapshotServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': ['1', '2']}");
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		
		verify(connector).addTestStepsToTestCases(Arrays.asList("1", "2", "14"));
	}
	
	@Test(groups= {"ut"})
	public void testAddAlreadyLinkedTestStepToTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		createSnapshotServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': ['1', '14']}");
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");

		verify(connector).addTestStepsToTestCases(Arrays.asList("1", "14"));
	}
	
	@Test(groups= {"ut"})
	public void testAddNoTestStepToTestCase() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		connector.addTestStepsToTestCases(new ArrayList<>());
		
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.patch(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL));
	}
	
	// snapshot creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateSnapshotNoStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createSnapshot(snapshot);
	}
	
	
	@Test(groups= {"ut"})
	public void testCreateSnapshot() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		

		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		connector.createSnapshot(snapshot);
		
		// check prerequisites has been created
		Assert.assertEquals((int)connector.getSessionId(), 13);
		Assert.assertEquals((int)connector.getSnapshotId(), 0);
	}
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSnapshotInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		connector.createSnapshot(snapshot);
		Assert.assertNull(connector.getSnapshotId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateSnapshotServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.createSnapshot(snapshot);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL));
	}
	
	// step result creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateStepResultNoStep() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.recordStepResult(true, "", 1);
	}
	
	
	@Test(groups= {"ut"})
	public void testCreateStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		
		// check prerequisites has been created
		Assert.assertEquals((int)connector.getSessionId(), 13);
		Assert.assertEquals((int)connector.getStepResultId(), 17);
	}
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateStepResultInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("POST", SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL, 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		Assert.assertNull(connector.getStepResultId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateStepResultServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		PowerMockito.verifyStatic(Unirest.class, never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL));
	}
	
	// add logs to test case in session
	@Test(groups= {"ut"})
	public void testRecordTestLogsNoTestCaseInSession() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		connector.createTestCase("Test 1");
		connector.addLogsToTestCaseInSession("some logs");

		verify(connector).createTestCaseInSession();
	}
	
	
	@Test(groups= {"ut"})
	public void testRecordTestLogs() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.addLogsToTestCaseInSession("some logs");
	}
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testRecordTestLogsInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		BaseRequest req = createSnapshotServerMock("PATCH", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15/", 200, "{'id': '9'}");	
		when(req.asString()).thenThrow(UnirestException.class);
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.addLogsToTestCaseInSession("some logs");
	}

}
