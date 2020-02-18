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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.ut.connectors.ConnectorsTest;

@PrepareForTest({Unirest.class})
public class TestSeleniumRobotVariableServerConnector extends ConnectorsTest {
	
	
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
	
	private GetRequest namedApplicationRequest;
	private GetRequest namedEnvironmentRequest;
	private GetRequest namedTestCaseRequest;
	private GetRequest namedVersionRequest;
	private GetRequest variablesRequest;
	private HttpRequestWithBody createApplicationRequest;
	private HttpRequestWithBody createEnvironmentRequest;
	private HttpRequestWithBody createVersionRequest;
	private HttpRequestWithBody createTestCaseRequest;
	private HttpRequestWithBody createVariableRequest;
	private HttpRequestWithBody updateVariableRequest;
	private HttpRequestWithBody updateVariableRequest2;
	
	
	/**
	 * simulate an alive sever responding to all requests
	 * @throws UnirestException 
	 */
	private void configureAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenReturn(responseAliveString);
		when(getAliveRequest.header(anyString(), anyString())).thenReturn(getAliveRequest);
		when(responseAliveString.getStatus()).thenReturn(200);
		when(Unirest.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		
		// set default reply from server. To override this behaviour, redefine some steps in test after connector creation
		namedApplicationRequest = (GetRequest) createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 200, "{'id': 1}");		
		namedEnvironmentRequest = (GetRequest) createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 200, "{'id': 2}");		
		namedTestCaseRequest = (GetRequest) createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_TESTCASE_API_URL, 200, "{'id': 3}");		
		namedVersionRequest = (GetRequest) createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_VERSION_API_URL, 200, "{'id': 4}");	
		createApplicationRequest = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL, 200, "{'id': '1'}");	
		createEnvironmentRequest = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL, 200, "{'id': '2'}");	
		createVersionRequest = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '4'}");	
		createTestCaseRequest = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '3'}");
		createVariableRequest = (HttpRequestWithBody) createServerMock("POST", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "{'id': 13, 'name': 'custom.test.variable.key', 'value': 'value', 'reservable': false}");
		updateVariableRequest = (HttpRequestWithBody) createServerMock("PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 12), 200, "{'id': 12, 'name': 'custom.test.variable.key', 'value': 'value', 'reservable': false}");
		updateVariableRequest2 = (HttpRequestWithBody) createServerMock("PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2), 200, "{}");
		variablesRequest = (GetRequest) createServerMock("GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "[{'id': 1, 'name': 'key1', 'value': 'value1', 'reservable': false}, {'id': 2, 'name': 'key2', 'value': 'value2', 'reservable': true}]");	

	}
	
	/**
	 * simulate an inactive server
	 * @throws UnirestException 
	 */
	private SeleniumRobotVariableServerConnector configureNotAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenThrow(UnirestException.class);
		when(Unirest.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		
		return new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	@Test(groups= {"ut"})
	public void testServerNotActive() {
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(false, SERVER_URL, "Test1", null);
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
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertTrue(connector.getActive());
		Assert.assertEquals(connector.getApplicationId(), 1);
		Assert.assertEquals(connector.getEnvironmentId(), 2);
		Assert.assertEquals(connector.getTestCaseId("Test1"), 3);
		Assert.assertEquals(connector.getVersionId(), 4);
	}
	
	/**
	 * test exception is raised if no token is provided whereas server is secured
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testServerActiveAliveAndSecured() throws UnirestException {
		configureAliveConnection();
		when(responseAliveString.getStatus()).thenReturn(401);
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	/**
	 * test connection is done if token provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testServerActiveAliveAndSecuredWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		verify(getAliveRequest).header("Authorization", "Token 123");
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveAliveAndSecuredWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		verify(getAliveRequest, never()).header("Authorization", "Token 123");
	}

	/**
	 * Force variable server to hold the application
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testApplicationDoesNotExist() throws UnirestException {

		configureAliveConnection();
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 404, "");		
		new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	/**
	 * Force variable server to hold the environment
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testEnvironmentDoesNotExist() throws UnirestException {
		
		configureAliveConnection();
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 404, "");		
		new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	@Test(groups= {"ut"})
	public void testFetchVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables();
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
	}
	

	@Test(groups= {"ut"})
	public void testVariableUpdateExistingVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		TestVariable variable = connector.upsertVariable(existingVariable, true);
		
		PowerMockito.verifyStatic(Unirest.class);
		Unirest.patch(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL));
		
		Assert.assertEquals(variable.getValue(), "value");
	}
	
	@Test(groups= {"ut"})
	public void testVariableUpdateExistingVariableWithToken() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		TestVariable variable = connector.upsertVariable(existingVariable, true);
		

		verify(variablesRequest, never()).header(eq("Authorization"), eq("Token 123"));
	}
	
	/**
	 * Variable is re-created when user has changed the value of a non-custom variable
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testVariableRecreateExistingVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, "key");
		TestVariable variable = connector.upsertVariable(existingVariable, true);
		
		PowerMockito.verifyStatic(Unirest.class);
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL));
		
		Assert.assertEquals(variable.getValue(), "value");
	}
	
	@Test(groups= {"ut"})
	public void testVariableCreateNewVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable("key", "value");
		TestVariable variable = connector.upsertVariable(existingVariable, true);
		
		PowerMockito.verifyStatic(Unirest.class);
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL));
		
		Assert.assertEquals(variable.getValue(), "value");
		Assert.assertEquals(variable.getName(), "key");
		Assert.assertEquals(variable.getInternalName(), TestVariable.TEST_VARIABLE_PREFIX + "key");
	}

	@Test(groups= {"ut"})
	public void testRawVariablesConversion() {
		Map<String, TestVariable> rawVariables = new HashMap<>();
		rawVariables.put("key1", new TestVariable(1, "key1", "value1", false, "key1"));
		rawVariables.put("key2", new TestVariable(1, "key2", "value2", false, "key2"));
		
		Map<String, String> variables = SeleniumRobotVariableServerConnector.convertRawTestVariableMapToKeyValuePairs(rawVariables);
		Assert.assertEquals(variables.get("key1"), "value1");
		Assert.assertEquals(variables.get("key2"), "value2");
	}
	
	@Test(groups= {"ut"})
	public void testVariableDereservation() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		List<TestVariable> variables = new ArrayList(connector.getVariables().values());
		
		connector.unreserveVariables(variables);
		
		// only one dereservation should be called
		PowerMockito.verifyStatic(Unirest.class);
		Unirest.patch(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2)));
	}
	
	@Test(groups= {"ut"})
	public void testVariableDereservationNullId() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		List<TestVariable> variables = Arrays.asList(new TestVariable("key", "value"));
		
		connector.unreserveVariables(variables);
		
		// only one dereservation should be called
		PowerMockito.verifyStatic(Unirest.class, times(0));
		Unirest.patch(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2)));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetApplicationIdWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals(connector.getApplicationId(), 1);
		verify(namedApplicationRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetEnvironmentIdWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals(connector.getEnvironmentId(), 2);
		verify(namedEnvironmentRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetVersionIdWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals(connector.getVersionId(), 4);
		verify(namedVersionRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetTestCaseIdWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals(connector.getTestCaseId("foo"), 3);
		verify(namedTestCaseRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateTestCaseWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		connector.createTestCase("foo");
		verify(createTestCaseRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateVersionWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		connector.createVersion();
		verify(createVersionRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateEnvironmentWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		connector.createEnvironment();
		verify(createEnvironmentRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateApplicationWithoutToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		connector.createApplication();
		verify(createApplicationRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetApplicationIdWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		Assert.assertEquals(connector.getApplicationId(), 1);
		verify(namedApplicationRequest, times(1)).header(eq("Authorization"), eq("Token 123"));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetEnvironmentIdWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		Assert.assertEquals(connector.getEnvironmentId(), 2);
		verify(namedEnvironmentRequest, times(1)).header(eq("Authorization"), eq("Token 123"));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateTestCaseWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createTestCase("foo");
		verify(createTestCaseRequest, times(2)).header(eq("Authorization"), eq("Token 123"));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateVersionWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createVersion();
		verify(createVersionRequest, times(2)).header(eq("Authorization"), eq("Token 123"));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateEnvironmentWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createEnvironment();
		verify(createEnvironmentRequest, times(1)).header(eq("Authorization"), eq("Token 123"));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateApplicationWithToken() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createApplication();
		verify(createApplicationRequest, times(1)).header(eq("Authorization"), eq("Token 123"));
	}
}
