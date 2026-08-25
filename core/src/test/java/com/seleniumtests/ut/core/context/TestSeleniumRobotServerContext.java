package com.seleniumtests.ut.core.context;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import com.seleniumtests.util.osutility.SystemUtility;
import org.mockito.MockedConstruction;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;

public class TestSeleniumRobotServerContext extends ConnectorsTest {
	


	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testSeleniumRobotServerActive(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerActiveWithoutUrl(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl("http://localhost:8000");
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerActiveNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(null);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotServerUrl(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl("http://localhost:8000");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerUrl(), "http://localhost:8000");
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerUrlNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerUrl());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotServerToken(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerToken("123");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken(), "123");
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerTokenFromEnvVar(final ITestContext testNGCtx) {
		try {
			SystemUtility.setenv(SeleniumRobotServerContext.SELENIUMROBOTSERVER_TOKEN_ENV_VAR, "456");
			initThreadContext(testNGCtx);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken(), "456");
		} finally {
			SystemUtility.clear(SeleniumRobotServerContext.SELENIUMROBOTSERVER_TOKEN_ENV_VAR);
		}
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerTokenFromEnvVarPriority(final ITestContext testNGCtx) {

		try {
			System.setProperty("seleniumRobotServerToken", "123");
			SystemUtility.setenv(SeleniumRobotServerContext.SELENIUMROBOTSERVER_TOKEN_ENV_VAR, "456");
			initThreadContext(testNGCtx);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken(), "123");
		} finally {
			System.clearProperty("seleniumRobotServerToken");
			SystemUtility.clear(SeleniumRobotServerContext.SELENIUMROBOTSERVER_TOKEN_ENV_VAR);
		}
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerTokenNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerToken(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotVariablesOlderThan(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableOlderThan(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableOlderThan(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSeleniumRobotVariablesOlderThanNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableOlderThan(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableOlderThan(), (Integer)SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotSnapshotTtl(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotTtl(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotTtl(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSeleniumRobotSnapshotTtlNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotTtl(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotTtl(), (Integer)SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL);
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotVariableReservationDuration(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableReservationDuration(300);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableReservationDuration(), (Integer)300);
	}
	@Test(groups="ut context")
	public void testSeleniumRobotVariableReservationDurationNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableReservationDuration(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableReservationDuration(), (Integer)SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
	}
	
	@Test(groups="ut context")
	public void testCompareSnapshotBehaviourNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour(), SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
	}
	
	@Test(groups="ut context")
	public void testCompareSnapshot(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);
        Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshot());
	}
	@Test(groups="ut context")
	public void testCompareSnapshotNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshot(), SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
	}
	
	@Test(groups="ut context")
	public void testRecordResults(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerRecordResults(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerRecordResults());
	}
	@Test(groups="ut context")
	public void testRecordResultsNull(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerRecordResults(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerRecordResults(), SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_RECORD_RESULTS);
	}
	

	/**
	 * If parameter is defined in variable server and as JVM parameter (user
	 * defined), the user defined parameter must be used
	 */
	@Test(groups = { "ut" })
	public void testUserDefinedParamOverridesVariableServer(final ITestContext testNGCtx)
			throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServerConnector = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			System.setProperty("key1", "userValue");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "userValue");

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty("key1");
		}
	}

	/**
	 * Check variables can be get from variable server
	 */
	@Test(groups = { "ut" })
	public void testVariablesFromVariableServer(final ITestContext testNGCtx) throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));

		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServerConnector = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			seleniumTestsCtx.configureContext(testResult);
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "val1");

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check we use the parameter "seleniumRobotServerVariablesReservation" provided by user
	 */
	@Test(groups = { "ut" })
	public void testVariablesFromVariableServerWithReservationDuration(final ITestContext testNGCtx) throws Exception {
		
		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));
		
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServerConnector = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, 300)).thenReturn(variables);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_RESERVATION, "300");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			seleniumTestsCtx.configureContext(testResult);
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "val1");
			verify(mockedVariableServerConnector.constructed().getFirst()).getVariables(0, 300);
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
		}
	}
	
	/**
	 * Check we create a variable server if all connection params are present
	 */
	@Test(groups = "ut")
	public void testVariableServerConnection(final ITestContext testNGCtx) throws Exception {
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServerConnector = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			// check upsert has been called
			verify(mockedVariableServerConnector.constructed().getFirst()).isAlive();

			Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check we create a no variable server if all it's not active
	 */
	@Test(groups = "ut")
	public void testNoVariableServerIfNotRequested(final ITestContext testNGCtx)
			throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check we create a no variable server if no URL
	 */
	@Test(groups = "ut", expectedExceptions = ConfigurationException.class)
	public void testNoVariableServerIfNoURL(final ITestContext testNGCtx) throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}

	/**
	 * Check we create a no variable server if not active
	 */
	@Test(groups = "ut", expectedExceptions = ConfigurationException.class)
	public void testNoVariableServerIfNotAlive(final ITestContext testNGCtx) throws Exception {
		try (MockedConstruction<SeleniumRobotVariableServerConnector> mockedVariableServerConnector = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(false);
		})) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);

			// check upsert has been called
			verify(mockedVariableServerConnector.constructed().getFirst()).isAlive();
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	@Test(groups = "ut")
	public void testInitParams(final ITestContext testNGCtx) {

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL, "25");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_RESERVATION, "10");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, "7");
			initThreadContext(testNGCtx);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerUrl(), "http://localhost:1234");
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshot());
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour(), SnapshotComparisonBehaviour.CHANGE_TEST_RESULT);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotTtl(), 25);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableReservationDuration(), 10);
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerRecordResults());
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableOlderThan(), 7);
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
		}
	}

	/**
	 * Check that if compare snapshot is set to true, recording is activated
	 */
	@Test(groups = "ut")
	public void testInitCompareSnapshot(final ITestContext testNGCtx) {

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			initThreadContext(testNGCtx);
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshot());
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerRecordResults());
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
		}
	}
}
