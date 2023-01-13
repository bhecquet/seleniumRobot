package com.seleniumtests.ut.core.context;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;


@PrepareForTest({ SeleniumRobotVariableServerConnector.class})
public class TestSeleniumRobotServerContext extends ConnectorsTest {
	

	@Mock
	private SeleniumRobotVariableServerConnector variableServer;


	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testSeleniumRobotServerActive(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerActiveWithoutUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl("http://localhost:8000");
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerActiveNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(null);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerActive());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotServerUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl("http://localhost:8000");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerUrl(), "http://localhost:8000");
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerUrlNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerUrl());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotServerToken(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerToken("123");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken(), "123");
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerTokenNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerToken(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotVariablesOlderThan(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableOlderThan(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableOlderThan(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSeleniumRobotVariablesOlderThanNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableOlderThan(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableOlderThan(), (Integer)SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotSnapshotTtl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotTtl(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotTtl(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSeleniumRobotSnapshotTtlNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotTtl(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotTtl(), (Integer)SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL);
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotVariableReservationDuration(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableReservationDuration(300);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableReservationDuration(), (Integer)300);
	}
	@Test(groups="ut context")
	public void testSeleniumRobotVariableReservationDurationNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerVariableReservationDuration(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerVariableReservationDuration(), (Integer)SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
	}
	
	@Test(groups="ut context")
	public void testCompareSnapshotBehaviour(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour("addTestResult");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour(), SnapshotComparisonBehaviour.ADD_TEST_RESULT);
	}
	@Test(groups="ut context")
	public void testCompareSnapshotBehaviourNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour(), SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
	}
	
	@Test(groups="ut context")
	public void testCompareSnapshot(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshot(), true);
	}
	@Test(groups="ut context")
	public void testCompareSnapshotNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerCompareSnapshot(), SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
	}
	
	@Test(groups="ut context")
	public void testRecordResults(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerRecordResults(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerRecordResults(), true);
	}
	@Test(groups="ut context")
	public void testRecordResultsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerRecordResults(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerRecordResults(), SeleniumRobotServerContext.DEFAULT_SELENIUMROBOTSERVER_RECORD_RESULTS);
	}
	

	/**
	 * If parameter is defined in variable server and as JVM parameter (user
	 * defined), the user defined parameter must be used
	 * 
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testUserDefinedParamOverridesVariableServer(final ITestContext testNGCtx, final XmlTest xmlTest)
			throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);
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
	 * 
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testVariablesFromVariableServer(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {

		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
					.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
					.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, -1)).thenReturn(variables);

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
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testVariablesFromVariableServerWithReservationDuration(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		
		Map<String, TestVariable> variables = new HashMap<>();
		variables.put("key1", new TestVariable("key1", "val1"));
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_RESERVATION, "300");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class)
			.withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null))
			.thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			when(variableServer.getVariables(0, 300)).thenReturn(variables);
			
			ITestResult testResult = GenericTest.generateResult(testNGCtx, getClass());
			initThreadContext(testNGCtx, "myTest", testResult);
			
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			seleniumTestsCtx.configureContext(testResult);
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "val1");
			verify(variableServer).getVariables(0, 300);
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
		}
	}
}
