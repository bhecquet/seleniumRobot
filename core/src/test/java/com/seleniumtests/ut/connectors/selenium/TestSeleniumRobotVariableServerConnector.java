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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kong.unirest.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;

public class TestSeleniumRobotVariableServerConnector extends ConnectorsTest {
	
	@Mock
	private Headers headers;

	@Mock
	public GetRequest getAliveRequestHttps;
	
	@Mock
	public HttpResponse responseHttps;

	@BeforeMethod(groups= {"ut"})
	public void init(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
	}
	
	
	/**
	 * simulate an inactive server
	 * @throws UnirestException 
	 */
	private SeleniumRobotVariableServerConnector configureNotAliveConnection() throws UnirestException {
		when(getAliveRequest.asString()).thenThrow(UnirestException.class);
		when(unirestInstance.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		
		return new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	@Test(groups= {"ut"})
	public void testServerNotActive() {
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(false, SERVER_URL, "Test1", null);
		Assert.assertFalse(connector.getActive());
	}
	
	/**
	 * Issue #356: check we change URL if redirect 308 is encountered
	 */
	@Test(groups= {"ut"})
	public void testServerWithRedirect308() {
		configureMockedVariableServerConnection(); // connection for HTTP
		configureMockedVariableServerConnection("https://localhost:2345"); // connection for HTTPS
		HttpResponse<String> response = mock(HttpResponse.class);
		
		when(response.getBody()).thenReturn("Moved Permanently");
		when(response.getHeaders()).thenReturn(headers);
		when(response.getStatus()).thenReturn(308);
		
		when(responseHttps.getBody()).thenReturn("OK");
		when(responseHttps.getStatus()).thenReturn(200);
		
		when(headers.getFirst("Location")).thenReturn("https://localhost:2345");
		when(getAliveRequest.asString()).thenReturn(response);
		when(getAliveRequestHttps.asString()).thenReturn(responseHttps);
		when(unirestInstance.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		when(Unirest.get("https://localhost:2345/variable/api/")).thenReturn(getAliveRequestHttps);
		
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals(connector.getUrl(), "https://localhost:2345");
	}
	
	/**
	 * Issue #356: check we change URL if redirect 301 is encountered
	 */
	@Test(groups= {"ut"})
	public void testServerWithRedirect301() {
		configureMockedVariableServerConnection(); // connection for HTTP
		configureMockedVariableServerConnection("https://localhost:2345"); // connection for HTTPS
		HttpResponse<String> response = mock(HttpResponse.class);
		
		when(response.getBody()).thenReturn("Moved Permanently");
		when(response.getHeaders()).thenReturn(headers);
		when(response.getStatus()).thenReturn(301);
		
		when(responseHttps.getBody()).thenReturn("OK");
		when(responseHttps.getStatus()).thenReturn(200);
		
		when(headers.getFirst("Location")).thenReturn("https://localhost:2345");
		when(getAliveRequest.asString()).thenReturn(response);
		when(getAliveRequestHttps.asString()).thenReturn(responseHttps);
		when(unirestInstance.get(SERVER_URL + "/variable/api/")).thenReturn(getAliveRequest);
		when(Unirest.get("https://localhost:2345/variable/api/")).thenReturn(getAliveRequestHttps);
		
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals(connector.getUrl(), "https://localhost:2345");
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveNotAlive() throws UnirestException {
		SeleniumRobotVariableServerConnector connector = configureNotAliveConnection();
		Assert.assertFalse(connector.getActive());
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveAndAlive() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertTrue(connector.getActive());
		Assert.assertEquals(connector.getApplicationId(), 1);
		Assert.assertEquals(connector.getEnvironmentId(), 2);
		Assert.assertEquals((int)connector.createTestCase("Test1"), 3);
		Assert.assertEquals(connector.getVersionId(), 4);
	}
	
	/**
	 * test exception is raised if no token is provided whereas server is secured
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testServerActiveAliveAndSecured() throws UnirestException {
		configureMockedVariableServerConnection();
		when(responseAliveString.getStatus()).thenReturn(401);
		new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	/**
	 * test connection is done if token provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testServerActiveAliveAndSecuredWithToken() throws UnirestException {
		configureMockedVariableServerConnection();
		new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		verify(getAliveRequest).header("Authorization", "Token 123");
	}
	
	@Test(groups= {"ut"})
	public void testServerActiveAliveAndSecuredWithoutToken() throws UnirestException {
		configureMockedVariableServerConnection();
		 new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		verify(getAliveRequest, never()).header("Authorization", "Token 123");
	}

	/**
	 * Force variable server to hold the application
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testApplicationDoesNotExist() throws UnirestException {

		configureMockedVariableServerConnection();
		createServerMock("GET", SeleniumRobotVariableServerConnector.APPLICATION_API_URL, 404, "");
		new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	/**
	 * Force variable server to hold the environment
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testEnvironmentDoesNotExist() throws UnirestException {
		
		configureMockedVariableServerConnection();
		createServerMock("GET", SeleniumRobotVariableServerConnector.ENVIRONMENT_API_URL, 404, "");
		new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
	}
	
	/**
	 * Check default behaviour with query parameters
	 * 
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetVariables() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables();
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
		
		verify(variablesRequest).queryString("reserve", true);
		verify(variablesRequest).queryString("version", 4);
		verify(variablesRequest).queryString("environment", 2);
		verify(variablesRequest).queryString("test", 3);
		verify(variablesRequest).queryString("olderThan", 0);
		verify(variablesRequest).queryString("format", "json");
		verify(variablesRequest, never()).queryString(eq("reservationDuration"), anyInt());
		verify(variablesRequest, never()).queryString(eq("name"), anyString());
		verify(variablesRequest, never()).queryString(eq("value"), anyString());
	}

	/**
	 * Check custom.test.variable has precedence over regular variable of the same name
	 *
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetVariablesWithDuplicate() throws UnirestException {

		configureMockedVariableServerConnection();
		variablesRequest = (GetRequest) createServerMock(SERVER_URL, "GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "[{'id': 1, 'name': 'key1', 'value': 'value1', 'reservable': false}, {'id': 2, 'name': 'custom.test.variable.key1', 'value': 'value2', 'reservable': true}]");
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables();

		// check the custom variable takes precedence
		Assert.assertEquals(variables.get("key1").getValue(), "value2");
	}
	@Test(groups= {"ut"})
	public void testGetVariablesWithDuplicate2() throws UnirestException {

		configureMockedVariableServerConnection();
		variablesRequest = (GetRequest) createServerMock(SERVER_URL, "GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "[{'id': 2, 'name': 'custom.test.variable.key1', 'value': 'value2', 'reservable': true}, {'id': 1, 'name': 'key1', 'value': 'value1', 'reservable': false}]");
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables();

		// check the custom variable takes precedence
		Assert.assertEquals(variables.get("key1").getValue(), "value2");
	}
	
	/**
	 * Check reservation delay is sent to server in seconds (whereas we provide it in minutes)
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetVariablesWithReservationDelay() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables(0, 30);
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
		
		verify(variablesRequest).queryString("reservationDuration", 1800);
	}
	
	@Test(groups= {"ut"})
	public void testGetVariablesByName() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables("foo", null);
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");

		verify(variablesRequest).queryString("name", "foo");
		verify(variablesRequest, never()).queryString(eq("value"), anyString());
	}
	
	@Test(groups= {"ut"})
	public void testGetVariablesByValue() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables(null, "bar");
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
		
		verify(variablesRequest, never()).queryString(eq("name"), anyString());
		verify(variablesRequest).queryString("value", "bar");
	}
	
	/**
	 * get variables failes with error on server side
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testGetVariablesFailed() throws UnirestException {

		configureMockedVariableServerConnection();
		createServerMock(SERVER_URL, "GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 404, "{}");

		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables();
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
	}
	
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testGetVariablesWithNetworkError() throws UnirestException {
		
		configureMockedVariableServerConnection();
		HttpRequest<?> req =  createServerMock(SERVER_URL, "GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "{}");
		when(req.asString()).thenThrow(UnirestException.class);
		
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Map<String, TestVariable> variables = connector.getVariables();
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
	}
	

	@Test(groups= {"ut"})
	public void testVariableUpdateExistingVariable() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		TestVariable variable = connector.upsertVariable(existingVariable, true);

		mockedUnirest.get().verify(() -> Unirest.patch(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));
		
		Assert.assertEquals(variable.getValue(), "value");
	}

	@Test(groups= {"ut"})
	public void testUpsertVariableWithoutPrefix() throws Exception {
		try {
			configureMockedVariableServerConnection();
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
	
			SeleniumTestsContext selenium = SeleniumTestsContextManager.getThreadContext();
	
			TestVariable existingVariable = new TestVariable(12, "foo", "value", false, "foo");
	
			Map<String, TestVariable> configuration = SeleniumTestsContextManager.getThreadContext().getConfiguration();
			configuration.put("foo", existingVariable);
	
			SeleniumRobotVariableServerConnector  variableServer = new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
			selenium.setVariableServer(variableServer);
	
			createServerMock(SERVER_URL, "POST", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "{'id': 13, 'name': 'custom.test.variable.foo', 'value': 'value', 'reservable': false}");
			createServerMock(SERVER_URL, "PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 13), 200, "{'id': 13, 'name': 'custom.test.variable.foo', 'value': 'value', 'reservable': false}");
	
			TestTasks.createOrUpdateParam("foo", "value");
			TestTasks.createOrUpdateParam("foo", "value");

			mockedUnirest.get().verify(() -> Unirest.patch(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));
			mockedUnirest.get().verify(() -> Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}

	}
	
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testVariableUpdateExistingVariableWithServerError() throws UnirestException {
		
		configureMockedVariableServerConnection();
		createServerMock(SERVER_URL, "PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 12), 500, "{}");
		
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		connector.upsertVariable(existingVariable, true);
	}
	
	@Test(groups= {"ut"}, expectedExceptions = SeleniumRobotServerException.class)
	public void testVariableUpdateExistingVariableWithNetworkError() throws UnirestException {
		
		configureMockedVariableServerConnection();
		HttpRequest<?> req =  createServerMock(SERVER_URL, "PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 12), 200, "{}");
		when(req.asString()).thenThrow(UnirestException.class);
		
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		connector.upsertVariable(existingVariable, true);
	}
	
	/**
	 * issue #429: if variable has been deleted, recreate one
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testVariableUpdateExistingVariableThatDisappeard() throws UnirestException {
		
		configureMockedVariableServerConnection();
		createServerMock(SERVER_URL, "PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 12), 404, "{}");
		
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		TestVariable variable = connector.upsertVariable(existingVariable, true);
		
		// check we tried a PATCH and then create the variable with a POST
		mockedUnirest.get().verify(() -> Unirest.patch(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));
		mockedUnirest.get().verify(() -> Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));
		
		Assert.assertEquals(variable.getValue(), "value");
	}
	
	@Test(groups= {"ut"})
	public void testVariableUpdateExistingVariableWithToken() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		connector.upsertVariable(existingVariable, true);
		

		verify(variablesRequest, never()).header("Authorization", "Token 123");
	}
	
	/**
	 * Variable is re-created when user has changed the value of a non-custom variable
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testVariableRecreateExistingVariable() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, "key");
		TestVariable variable = connector.upsertVariable(existingVariable, true);

		mockedUnirest.get().verify(() -> Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));
		
		Assert.assertEquals(variable.getValue(), "value");
	}
	
	@Test(groups= {"ut"})
	public void testVariableCreateNewVariable() throws UnirestException {
		
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable existingVariable = new TestVariable("key", "value");
		TestVariable variable = connector.upsertVariable(existingVariable, true);

		mockedUnirest.get().verify(() -> Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL)));
		
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
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		List<TestVariable> variables = new ArrayList<>(connector.getVariables().values());
		
		connector.unreserveVariables(variables);
		
		// only one dereservation should be called
		mockedUnirest.get().verify(() -> Unirest.patch(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2))));
	}
	
	@Test(groups= {"ut"})
	public void testVariableDereservationNullId() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		List<TestVariable> variables = Arrays.asList(new TestVariable("key", "value"));
		
		connector.unreserveVariables(variables);

		mockedUnirest.get().verify(() -> Unirest.patch(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2))), never());
	}

	@Test(groups= {"ut"})
	public void testVariableDeletion() {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable variable = new TestVariable(1, "key1", "value1", false, "key1");
		connector.deleteVariable(variable);

		mockedUnirest.get().verify(() -> Unirest.delete(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 1))));
	}


	@Test(groups= {"ut"})
	public void testVariableDeletionNoVariableId() {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable variable = new TestVariable(null, "key1", "value1", false, "key1");
		connector.deleteVariable(variable);

		mockedUnirest.get().verify(() -> Unirest.delete(ArgumentMatchers.contains("/variable/api/variable")), never());
	}

	@Test(groups= {"ut"})
	public void testVariableDeletionWithToken() {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		TestVariable variable = new TestVariable(1, "key1", "value1", false, "key1");
		connector.deleteVariable(variable);

		mockedUnirest.get().verify(() -> Unirest.delete(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 1))));
		verify(deleteVariableRequest, times(1)).header("Authorization", "Token 123");
	}

	/**
	 * Check no error is raised if deletion fails
	 */
	@Test(groups= {"ut"})
	public void testVariableDeletionInError() {
		configureMockedVariableServerConnection();
		createServerMock(SERVER_URL, "DELETE", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 1), 404, "");
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable variable = new TestVariable(1, "key1", "value1", false, "key1");
		connector.deleteVariable(variable);

		mockedUnirest.get().verify(() -> Unirest.delete(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 1))));
	}
	@Test(groups= {"ut"})
	public void testVariableDeletionInError2() {
		configureMockedVariableServerConnection();
		HttpRequest<?> req =  createServerMock(SERVER_URL, "DELETE", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 1), 404, "");
		when(req.asString()).thenThrow(new UnirestException("error"));
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		TestVariable variable = new TestVariable(1, "key1", "value1", false, "key1");
		connector.deleteVariable(variable);

		mockedUnirest.get().verify(() -> Unirest.delete(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 1))));
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetApplicationIdWithoutToken() throws UnirestException {
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", null);
		Assert.assertEquals((int)connector.createTestCase("foo"), 3);
		verify(namedTestCaseRequest, never()).header(eq("Authorization"), anyString());
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateTestCaseWithoutToken() throws UnirestException {
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
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
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		Assert.assertEquals(connector.getApplicationId(), 1);
		verify(namedApplicationRequest, times(1)).header("Authorization", "Token 123");
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testGetEnvironmentIdWithToken() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		Assert.assertEquals(connector.getEnvironmentId(), 2);
		verify(namedEnvironmentRequest, times(1)).header("Authorization", "Token 123");
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateTestCaseWithToken() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createTestCase("foo");
		verify(createTestCaseRequest, times(2)).header("Authorization", "Token 123");
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateVersionWithToken() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createVersion();
		verify(createVersionRequest, times(2)).header("Authorization", "Token 123");
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateEnvironmentWithToken() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createEnvironment();
		verify(createEnvironmentRequest, times(1)).header("Authorization", "Token 123");
	}
	
	/**
	 * Check tokens are not added to request when not provided
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testCreateApplicationWithToken() throws UnirestException {
		configureMockedVariableServerConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector(true, SERVER_URL, "Test1", "123");
		connector.createApplication();
		verify(createApplicationRequest, times(1)).header("Authorization", "Token 123");
	}
}
