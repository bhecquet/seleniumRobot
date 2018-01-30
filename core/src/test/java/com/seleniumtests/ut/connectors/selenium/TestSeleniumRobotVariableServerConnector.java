package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;

@PrepareForTest({Unirest.class})
public class TestSeleniumRobotVariableServerConnector extends MockitoTest {
	
	
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
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 200, "{'id': 1}");		
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 200, "{'id': 2}");		
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_TESTCASE_API_URL, 200, "{'id': 3}");		
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_VERSION_API_URL, 200, "{'id': 4}");	
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '4'}");	
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '3'}");
		createServerMock("POST", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "{'id': 13, 'name': 'custom.test.variable.key', 'value': 'value', 'reservable': false}");
		createServerMock("PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 12), 200, "{'id': 12, 'name': 'custom.test.variable.key', 'value': 'value', 'reservable': false}");
		createServerMock("PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2), 200, "{}");
		createServerMock("GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "[{'id': 1, 'name': 'key1', 'value': 'value1', 'reservable': false}, {'id': 2, 'name': 'key2', 'value': 'value2', 'reservable': true}]");	

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
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 404, "");		
		new SeleniumRobotVariableServerConnector("Test1");
	}
	
	/**
	 * Force variable server to hold the environment
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"}, expectedExceptions=ConfigurationException.class)
	public void testEnvironmentDoesNotExist() throws UnirestException {
		
		configureAliveConnection();
		createServerMock("GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 404, "");		
		new SeleniumRobotVariableServerConnector("Test1");
	}
	
	@Test(groups= {"ut"})
	public void testFetchVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		Map<String, TestVariable> variables = connector.getVariables();
		Assert.assertEquals(variables.get("key1").getValue(), "value1");
		Assert.assertEquals(variables.get("key2").getValue(), "value2");
	}
	

	@Test(groups= {"ut"})
	public void testVariableUpdateExistingVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, TestVariable.TEST_VARIABLE_PREFIX + "key");
		TestVariable variable = connector.upsertVariable(existingVariable);
		
		PowerMockito.verifyStatic();
		Unirest.patch(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL));
		
		Assert.assertEquals(variable.getValue(), "value");
	}
	
	/**
	 * Variable is re-created when user has changed the value of a non-custom variable
	 * @throws UnirestException
	 */
	@Test(groups= {"ut"})
	public void testVariableRecreateExistingVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		TestVariable existingVariable = new TestVariable(12, "key", "value", false, "key");
		TestVariable variable = connector.upsertVariable(existingVariable);
		
		PowerMockito.verifyStatic();
		Unirest.post(ArgumentMatchers.contains(SeleniumRobotVariableServerConnector.VARIABLE_API_URL));
		
		Assert.assertEquals(variable.getValue(), "value");
	}
	
	@Test(groups= {"ut"})
	public void testVariableCreateNewVariable() throws UnirestException {
		
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		TestVariable existingVariable = new TestVariable("key", "value");
		TestVariable variable = connector.upsertVariable(existingVariable);
		
		PowerMockito.verifyStatic();
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
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		List<TestVariable> variables = new ArrayList(connector.getVariables().values());
		
		connector.unreserveVariables(variables);
		
		// only one dereservation should be called
		PowerMockito.verifyStatic();
		Unirest.patch(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2)));
	}
	
	@Test(groups= {"ut"})
	public void testVariableDereservationNullId() throws UnirestException {
		configureAliveConnection();
		SeleniumRobotVariableServerConnector connector= new SeleniumRobotVariableServerConnector("Test1");
		List<TestVariable> variables = Arrays.asList(new TestVariable("key", "value"));
		
		connector.unreserveVariables(variables);
		
		// only one dereservation should be called
		PowerMockito.verifyStatic(times(0));
		Unirest.patch(ArgumentMatchers.contains(String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2)));
	}

}
