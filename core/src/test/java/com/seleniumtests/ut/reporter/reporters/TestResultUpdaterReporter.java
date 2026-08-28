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
package com.seleniumtests.ut.reporter.reporters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mockito.MockedStatic;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
import com.seleniumtests.reporter.reporters.ResultUpdaterReporter;

import org.testng.Assert;

/**
 * Unit tests for {@link ResultUpdaterReporter}
 */
public class TestResultUpdaterReporter extends GenericTest {

	/**
	 * Helper class to give test access to the protected 'generateReport' method
	 */
	public static class TestableResultUpdaterReporter extends ResultUpdaterReporter {
		public void callGenerateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport, boolean finalGeneration) {
			generateReport(resultSet, outdir, optimizeReport, finalGeneration);
		}
	}

	private static final String SERVER_URL = "http://localhost:4321";

	@BeforeMethod(groups = { "ut" })
	public void reset() {
		resetTestNGResultAndLogger();
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(true);
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour(SnapshotComparisonBehaviour.CHANGE_TEST_RESULT.toString());
	}

	/**
	 * Creates a standalone ITestResult (not the currently running test result) so that changing its status
	 * or throwable does not impact the outcome of the unit test itself
	 */
	private ITestResult getTestResult() {
		ITestResult testResult = TestResult.newEmptyTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		return testResult;
	}

	private Map<ITestContext, Set<ITestResult>> buildResultSet(ITestResult testResult) {
		Map<ITestContext, Set<ITestResult>> resultSet = new HashMap<>();
		Set<ITestResult> results = new HashSet<>();
		results.add(testResult);
		resultSet.put(mock(ITestContext.class), results);
		return resultSet;
	}

	/**
	 * If server is not active, nothing should be done: connector is not even requested
	 */
	@Test(groups = { "ut" })
	public void testServerNotActive() {
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerActive(false);

		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);
			mockedConnector.verify(SeleniumRobotSnapshotServerConnector::getInstance, never());
		}

		Assert.assertFalse(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}

	/**
	 * if snapshot comparison is disabled on server, nothing should be done
	 */
	@Test(groups = { "ut" })
	public void testSnapshotComparisonNotActive() {
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshot(false);

		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);
			mockedConnector.verify(SeleniumRobotSnapshotServerConnector::getInstance, never());
		}

		Assert.assertFalse(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}

	/**
	 * If test is already in FAILURE state, comparison should not be requested and test result kept as is
	 */
	@Test(groups = { "ut" })
	public void testAlreadyFailedTestIsNotUpdated() {
		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.FAILURE);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);

		SeleniumRobotSnapshotServerConnector connector = mock(SeleniumRobotSnapshotServerConnector.class);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedConnector.when(SeleniumRobotSnapshotServerConnector::getInstance).thenReturn(connector);
			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);
			verify(connector, never()).getTestCaseInSessionComparisonResult(anyInt(), any());
		}

		Assert.assertEquals(testResult.getStatus(), ITestResult.FAILURE);
		Assert.assertFalse(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}

	/**
	 * If reporter has already been executed for this test result, it should not be executed again
	 */
	@Test(groups = { "ut" })
	public void testAlreadyExecutedIsSkipped() {
		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);
		TestNGResultUtils.setUpdateResultReportExecuted(testResult, true);

		SeleniumRobotSnapshotServerConnector connector = mock(SeleniumRobotSnapshotServerConnector.class);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedConnector.when(SeleniumRobotSnapshotServerConnector::getInstance).thenReturn(connector);
			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);
			verify(connector, never()).getTestCaseInSessionComparisonResult(anyInt(), any());
		}

		Assert.assertEquals(testResult.getStatus(), ITestResult.SUCCESS);
	}

	/**
	 * When no snapshot comparison has been requested for this test (no id recorded), nothing should be done
	 */
	@Test(groups = { "ut" })
	public void testNoSnapshotSessionId() {
		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);

		SeleniumRobotSnapshotServerConnector connector = mock(SeleniumRobotSnapshotServerConnector.class);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class)) {
			mockedConnector.when(SeleniumRobotSnapshotServerConnector::getInstance).thenReturn(connector);
			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);
			verify(connector, never()).getTestCaseInSessionComparisonResult(anyInt(), any());
		}

		Assert.assertFalse(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}

	/**
	 * Test is OK, snapshot comparison is OK => test result must remain unchanged
	 */
	@Test(groups = { "ut" })
	public void testSnapshotComparisonOk() {
		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);

		SeleniumRobotSnapshotServerConnector connector = mock(SeleniumRobotSnapshotServerConnector.class);
		when(connector.getTestCaseInSessionComparisonResult(anyInt(), any())).thenReturn(ITestResult.SUCCESS);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
				MockedStatic<TestStepManager> mockedStepManager = mockStatic(TestStepManager.class)) {
			mockedConnector.when(SeleniumRobotSnapshotServerConnector::getInstance).thenReturn(connector);

			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);

			mockedStepManager.verify(() -> TestStepManager.logThrowableToTestEndStep(any()), never());
		}

		Assert.assertEquals(testResult.getStatus(), ITestResult.SUCCESS);
		Assert.assertNull(testResult.getThrowable());
		Assert.assertEquals(TestNGResultUtils.getSnapshotComparisonResult(testResult), (Integer) ITestResult.SUCCESS);
		Assert.assertTrue(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}

	/**
	 * Test is OK, snapshot comparison is KO and behaviour is 'CHANGE_TEST_RESULT' => test result must be changed to FAILURE
	 */
	@Test(groups = { "ut" })
	public void testSnapshotComparisonKoChangesTestResult() {
		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);

		SeleniumRobotSnapshotServerConnector connector = mock(SeleniumRobotSnapshotServerConnector.class);
		when(connector.getTestCaseInSessionComparisonResult(anyInt(), any())).thenReturn(ITestResult.FAILURE);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
				MockedStatic<TestStepManager> mockedStepManager = mockStatic(TestStepManager.class)) {
			mockedConnector.when(SeleniumRobotSnapshotServerConnector::getInstance).thenReturn(connector);

			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);

			mockedStepManager.verify(() -> TestStepManager.logThrowableToTestEndStep(testResult), times(1));
		}

		Assert.assertEquals(testResult.getStatus(), ITestResult.FAILURE);
		Assert.assertTrue(testResult.getThrowable() instanceof ScenarioException);
		Assert.assertEquals(TestNGResultUtils.getSnapshotComparisonResult(testResult), (Integer) ITestResult.FAILURE);
		Assert.assertTrue(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}

	/**
	 * Test is OK, snapshot comparison is KO but behaviour is 'DISPLAY_ONLY' => test result must remain unchanged
	 */
	@Test(groups = { "ut" })
	public void testSnapshotComparisonKoDisplayOnlyDoesNotChangeResult() {
		SeleniumTestsContextManager.getGlobalContext().seleniumServer().setSeleniumRobotServerCompareSnapshotBehaviour(SnapshotComparisonBehaviour.DISPLAY_ONLY.toString());

		ITestResult testResult = getTestResult();
		testResult.setStatus(ITestResult.SUCCESS);
		TestNGResultUtils.setSnapshotTestCaseInSessionId(testResult, 10);

		SeleniumRobotSnapshotServerConnector connector = mock(SeleniumRobotSnapshotServerConnector.class);
		when(connector.getTestCaseInSessionComparisonResult(anyInt(), any())).thenReturn(ITestResult.FAILURE);

		try (MockedStatic<SeleniumRobotSnapshotServerConnector> mockedConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
				MockedStatic<TestStepManager> mockedStepManager = mockStatic(TestStepManager.class)) {
			mockedConnector.when(SeleniumRobotSnapshotServerConnector::getInstance).thenReturn(connector);

			new TestableResultUpdaterReporter().callGenerateReport(buildResultSet(testResult), "outDir", false, true);

			mockedStepManager.verify(() -> TestStepManager.logThrowableToTestEndStep(any()), never());
		}

		Assert.assertEquals(testResult.getStatus(), ITestResult.SUCCESS);
		Assert.assertNull(testResult.getThrowable());
		Assert.assertEquals(TestNGResultUtils.getSnapshotComparisonResult(testResult), (Integer) ITestResult.FAILURE);
		Assert.assertTrue(TestNGResultUtils.isUpdateResultReportExecuted(testResult));
	}
}
