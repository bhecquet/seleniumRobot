package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

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
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

@PrepareForTest({Unirest.class})
public class TestSeleniumRobotVariableServerConnector extends MockitoTest {
	
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
	private void configureAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenReturn(responseAliveString);
		when(responseAliveString.getStatus()).thenReturn(200);
		when(Unirest.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		
		// set default reply from server. To override this behaviour, redefine some steps in test after connector creation
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 200, "{'id': 1}");		
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 200, "{'id': 2}");		
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_TESTCASE_API_URL, 200, "{'id': 3}");		
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_VERSION_API_URL, 200, "{'id': 4}");	
		createVariableServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '4'}");	
		createVariableServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '3'}");
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "[{'name': 'key1', 'value': 'value1'}, {'name': 'key2', 'value': 'value2'}]");	

	}
	
	/**
	 * simulate an inactive server
	 * @throws UnirestException 
	 */
	private SeleniumRobotVariableServerConnector configureNotAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenThrow(UnirestException.class);
		when(Unirest.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		return new SeleniumRobotVariableServerConnector("Test1");
	}
	
	/**
	 * Method for creating snapshot server reply mock
	 * @throws UnirestException 
	 */
	private void createVariableServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		
		HttpResponse<String> response = mock(HttpResponse.class);
		HttpRequest request = mock(HttpRequest.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
		
		when(request.getUrl()).thenReturn(SERVER_URL);
		
		switch(requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class); 
				when(Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);
				when(getRequest.asString()).thenReturn(response);
				when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyInt())).thenReturn(getRequest);
				when(response.getStatus()).thenReturn(statusCode);
				when(getRequest.getHttpRequest()).thenReturn(request);
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
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector("Test1");
		Assert.assertFalse(connector.getActive());
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveNotAlive() throws UnirestException {
		SeleniumRobotVariableServerConnector connector = configureNotAliveConnection();
		Assert.assertFalse(connector.getActive());
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveAndAlive() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector("Test1");
		Assert.assertTrue(connector.getActive());
		Assert.assertEquals(connector.getApplicationId(), 1);
		Assert.assertEquals(connector.getEnvironmentId(), 2);
		Assert.assertEquals(connector.getTestCaseId("Test1"), 3);
		Assert.assertEquals(connector.getVersionId(), 4);
	}

	/**
	 * Force variable server to hold the application
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testApplicationDoesNotExist() throws UnirestException {

		configureAliveConnection();
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 404, "");		
		new SeleniumRobotVariableServerConnector("Test1");
	}
	
	/**
	 * Force variable server to hold the environment
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testEnvironmentDoesNotExist() throws UnirestException {
		
		configureAliveConnection();
		createVariableServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 404, "");		
		new SeleniumRobotVariableServerConnector("Test1");
	}
	
	@Test(groups= {"ut"})
	public void testFetchVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		Map<String, String> variables = connector.getVariables();
		Assert.assertEquals(variables.get("key1"), "value1");
		Assert.assertEquals(variables.get("key2"), "value2");
	}

	
	
	// get variables
	// get information form server

	


}
