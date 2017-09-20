package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
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
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;

@PrepareForTest({Unirest.class})
public class TestSeleniumRobotSnapshotServerConnector extends MockitoTest {
	
	private static final String SERVER_URL = "http://localhost:4321";
	
	@Mock
	private GetRequest getAliveRequest;
	
	@Mock
	private HttpRequestWithBody postRequest;
	
	@Mock
	private HttpResponse<String> responseAliveString;

	@BeforeMethod(groups= {"ut"})
	public void init(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		PowerMockito.mockStatic(Unirest.class);
	}
	
	/**
	 * simulate an alive sever responding to all requests
	 * @throws UnirestException 
	 */
	private SeleniumRobotSnapshotServerConnector configureAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenReturn(responseAliveString);
		when(responseAliveString.getStatus()).thenReturn(200);
		when(Unirest.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(SeleniumRobotServerConnector.SELENIUM_SERVER_URL, SERVER_URL);
		
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
		
		return new SeleniumRobotSnapshotServerConnector();
	}
	
	/**
	 * simulate an inactive server
	 * @throws UnirestException 
	 */
	private SeleniumRobotSnapshotServerConnector configureNotAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenThrow(UnirestException.class);
		when(Unirest.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(SeleniumRobotServerConnector.SELENIUM_SERVER_URL, SERVER_URL);
		return new SeleniumRobotSnapshotServerConnector();
	}
	
	/**
	 * Method for creating snapshot server reply mock
	 * @throws UnirestException 
	 */
	private void createSnapshotServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		
		HttpResponse<String> response = mock(HttpResponse.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
		
		switch(requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class); 
				when(Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);
				when(getRequest.asString()).thenReturn(response);
				when(response.getStatus()).thenReturn(statusCode);
				when(response.getBody()).thenReturn(replyData);
				break;
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
				break;
			
		}
	}
	
	@Test(groups= {"ut"})
	public void testServerNotActive() {
		SeleniumRobotSnapshotServerConnector connector = new SeleniumRobotSnapshotServerConnector();
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
		
		connector.createApplication();
		Assert.assertEquals((int)connector.getApplicationId(), 9);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateApplicationInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL)).thenThrow(UnirestException.class);
		
		connector.createApplication();
		Assert.assertNull(connector.getApplicationId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateApplicationServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createApplication();
		PowerMockito.verifyStatic(never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL));
	}
	
	// version creation
	@Test(groups= {"ut"})
	public void testCreateVersionApplicationNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
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
		
		connector.createApplication();
		connector.createVersion();
		
		Assert.assertNotNull(connector.getVersionId());
		Assert.assertEquals((int)connector.getVersionId(), 11);
		verify(connector).createApplication();
	}
	
	@Test(groups= {"ut"})
	public void testCreateVersionInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.VERSION_API_URL)).thenThrow(UnirestException.class);
		
		connector.createApplication();
		Assert.assertNull(connector.getVersionId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateVersionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createVersion();
		PowerMockito.verifyStatic(never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.VERSION_API_URL));
	}
	
	// environment creation
	@Test(groups= {"ut"})
	public void testCreateEnvironment() throws UnirestException {		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		
		connector.createEnvironment();
		Assert.assertEquals((int)connector.getEnvironmentId(), 10);
	}
	

	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateEnvironmentInError() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL)).thenThrow(UnirestException.class);
		
		connector.createEnvironment();
		Assert.assertNull(connector.getEnvironmentId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateEnvironmentServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createEnvironment();
		PowerMockito.verifyStatic(never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL));
	}
	
	// session creation
	@Test(groups= {"ut"})
	public void testCreateSessionEnvironmentNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createSession();
		
		Assert.assertEquals((int)connector.getSessionId(), 13);
		
		// check application, environment and version have also been created
		Assert.assertEquals((int)connector.getApplicationId(), 9);
		Assert.assertEquals((int)connector.getEnvironmentId(), 10);
		Assert.assertEquals((int)connector.getVersionId(), 11);
		verify(connector).createEnvironment();
		verify(connector).createVersion();
		verify(connector).createApplication();
	}
	
	@Test(groups= {"ut"})
	public void testCreateSession() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createApplication();
		connector.createVersion();
		connector.createEnvironment();
		connector.createSession();
		
		Assert.assertEquals((int)connector.getSessionId(), 13);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSessionInError() throws UnirestException {

		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.SESSION_API_URL)).thenThrow(UnirestException.class);
		
		connector.createSession();
		Assert.assertNull(connector.getSessionId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateSessionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createSession();
		PowerMockito.verifyStatic(never());
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.SESSION_API_URL));
	}
	
	// test case creation
	@Test(groups= {"ut"})
	public void testCreateTestCasePrerequisiteNull() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createSession();
		connector.createTestCase("Test 1");
		
		Assert.assertEquals((int)connector.getTestCaseId("Test 1"), 12);
		
		// check application has also been created
		Assert.assertEquals((int)connector.getApplicationId(), 9);
		verify(connector).createApplication();
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCase() throws UnirestException {	
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createApplication();
		connector.createTestCase("Test 1");
		
		Assert.assertEquals((int)connector.getTestCaseId("Test 1"), 12);
	}
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateTestCaseInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL)).thenThrow(UnirestException.class);
		
		connector.createApplication();
		connector.createTestCase("Test 1");
		Assert.assertNull(connector.getTestCaseId("Test 1"));
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCase("Test 1");
		PowerMockito.verifyStatic(never());
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
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL)).thenThrow(UnirestException.class);
		
		connector.createSession();
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		Assert.assertNull(connector.getTestCaseInSessionId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateTestCaseInSessionServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCaseInSession();
		PowerMockito.verifyStatic(never());
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
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL)).thenThrow(UnirestException.class);
		
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
		PowerMockito.verifyStatic(never());
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
		
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15")).thenThrow(UnirestException.class);

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
		
		PowerMockito.verifyStatic(never());
		Unirest.patch(ArgumentMatchers.contains(SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL));
	}
	
	// snapshot creation
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testCreateSnapshotNoStepResult() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		
		connector.createSnapshot(new File(""));
	}
	
	
	@Test(groups= {"ut"})
	public void testCreateSnapshot() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = spy(configureAliveConnection());
		

		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		connector.createSnapshot(new File(""));
		
		// check prerequisites has been created
		Assert.assertEquals((int)connector.getSessionId(), 13);
		Assert.assertEquals((int)connector.getSnapshotId(), 0);
	}
	
	
	@Test(groups= {"ut"}, expectedExceptions=SeleniumRobotServerException.class)
	public void testCreateSnapshotInError() throws UnirestException {
		
		SeleniumRobotSnapshotServerConnector connector = configureAliveConnection();
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL)).thenThrow(UnirestException.class);
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.recordStepResult(true, "", 1);
		connector.createSnapshot(new File(""));
		Assert.assertNull(connector.getSnapshotId());
	}
	
	@Test(groups= {"ut"})
	public void testCreateSnapshotServerInactive() throws UnirestException {
		SeleniumRobotSnapshotServerConnector connector = configureNotAliveConnection();
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.createTestStep("Step 1");
		connector.createSnapshot(new File(""));
		PowerMockito.verifyStatic(never());
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
		when(Unirest.post(SERVER_URL + SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL)).thenThrow(UnirestException.class);
		
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
		PowerMockito.verifyStatic(never());
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
		when(Unirest.patch(SERVER_URL + SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15/")).thenThrow(UnirestException.class);
		
		connector.createTestCase("Test 1");
		connector.createTestCaseInSession();
		connector.addLogsToTestCaseInSession("some logs");
	}

}
