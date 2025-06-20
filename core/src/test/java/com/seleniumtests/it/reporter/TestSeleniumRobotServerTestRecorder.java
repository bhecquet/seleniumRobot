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
package com.seleniumtests.it.reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.info.Info;
import com.seleniumtests.reporter.info.MultipleInfo;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.CommonReporter;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.reporters.SeleniumRobotServerTestRecorder;

import kong.unirest.json.JSONObject;

public class TestSeleniumRobotServerTestRecorder extends ReporterTest {

	@Mock
	private SeleniumRobotSnapshotServerConnector serverConnector;

	private SeleniumRobotServerTestRecorder reporter;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		// clear sessionID for session recording so that each test is not polluted by others
		SeleniumRobotServerTestRecorder.resetSessionId();
	}

	/**
	 * In this test, everything is fine with seleniumrobot server
	 * @throws Exception
	 */
	// TODO: check that record is not done when compareSnapshot & resultRecord are false
	// TODO: check that record is not done when seleniumRobotServer is not active (by params)
	@Test(groups={"it"})
	public void testReportGeneration() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.STARTED_BY, "http://mylauncher/test");

			initMocks(mockedCommonReporter, mockedServerConnector);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testSkipped", "testOkWithTestName"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector).createSession(anyString(), eq("BROWSER:NONE"), eq("http://mylauncher/test"), any(OffsetDateTime.class));
			verify(serverConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), anyString(), anyString(), anyString()); // with snapshot behaviour set to "display_only", this method should not be called

			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testAndSubActions");
			verify(serverConnector).createTestCase("testInError");
			verify(serverConnector).createTestCase("testWithException");
			verify(serverConnector).createTestCase("testSkipped");
			verify(serverConnector, times(5)).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testAndSubActions"), eq("SUCCESS"), eq("LOCAL"), eq("a test with steps"), any(OffsetDateTime.class));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testInError"), eq("FAILURE"), eq("LOCAL"), eq(""), any(OffsetDateTime.class));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testWithException"), eq("FAILURE"), eq("LOCAL"), eq(""), any(OffsetDateTime.class));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testSkipped"), eq("SKIP"), eq("LOCAL"), eq(""), any(OffsetDateTime.class));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("A test which is <OK> é&"), eq("SUCCESS"), eq("LOCAL"), eq(""), any(OffsetDateTime.class)); // a test with custom name
			verify(serverConnector, times(4)).createTestStep(eq("step 1"), anyInt());
			verify(serverConnector, times(5)).createTestStep(eq("Test end"), anyInt());
			verify(serverConnector).createTestStep(eq("step 2"), anyInt());
			verify(serverConnector, times(5)).createTestStep(eq("No previous execution results, you can enable it via parameter '-DkeepAllResults=true'"), anyInt());
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), eq(new ArrayList<>())); // two snapshots but only once is sent because the other has no name
			
			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Snapshot hasn't any name, it won't be sent to server")); // one snapshot has no name, error message is displayed

			verify(serverConnector, times(34)).recordStepResult(any(TestStep.class), anyInt(), anyInt()); // all steps are recorded
			// files are uploaded. Only 'testWithException' holds a Snapshot with snapshotCheckType.NONE and so, only its 2 files are uploaded
			// other snapshots, which are used for image comparison / regression are uploaded through an other service 'createSnapshot'
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "htmls", "testWithException_0-1_step_1--tened.html").toFile()), anyInt());
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "screenshots", "testWithException_0-1_step_1--rtened.png").toFile()), anyInt());
			// check image id and html id has been updated once files has been uploaded, for tests where snapshots has been uploaded
			verify(serverConnector).updateStepResult(contains("\"snapshots\":[{\"idHtml\":0,\"displayInReport\":true,\"name\":\"a name\",\"idImage\":0,\"failed\":false,\"position\":0,\"type\":\"snapshot\",\"snapshotCheckType\":\"NONE\""), eq(0));
			verify(serverConnector).updateStepResult(contains("\"snapshots\":[{\"exception\":\"org.openqa.selenium.WebDriverException\",\"idHtml\":null,\"displayInReport\":true,\"name\":\"main\""), eq(0));
			
			// check logs has been uploaded (one upload for each test)
			verify(serverConnector, times(5)).uploadLogs(any(File.class), eq(0));
			verify(serverConnector).uploadLogs(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "execution.log").toFile()), eq(0));
			verify(serverConnector).uploadLogs(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "execution.log").toFile()), eq(0));

			verify(serverConnector, times(5)).getTestCaseInSessionComparisonResult(anyInt(), any(StringBuilder.class)); // called once per test

			// check test infos has been sent
			ArgumentCaptor<Map<String, Info>> infosArgument = ArgumentCaptor.forClass(Map.class);
			verify(serverConnector, times(5)).recordTestInfo(infosArgument.capture(), eq(0));
			Map<String, Info> infos = infosArgument.getValue();
			Assert.assertEquals(infos.size(), 1);
			Assert.assertTrue(infos.get(TestStepManager.LAST_STATE_NAME) instanceof MultipleInfo);

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.STARTED_BY);
		}
	}

	/**
	 * Check all calls are done when using a real driver
	 * Use the behaviour "changeTestResult" as more calls are done
	 * #677: check calls on snapshot comparison are homogeneous with browser information
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationWithChangeTestResult() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.STARTED_BY, "http://mylauncher/test");

			initMocks(mockedCommonReporter, mockedServerConnector);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});

			// check browser has the same valeurs for all calls
			verify(serverConnector).createSession(anyString(), eq("BROWSER:CHROME"), eq("http://mylauncher/test"), any(OffsetDateTime.class));
			// one snapshot is compared with reference during test run to check if test must be replayed
			verify(serverConnector).checkSnapshotHasNoDifferences(any(Snapshot.class), eq("testDriverCustomSnapshot"), eq("_captureSnapshot"), eq("BROWSER:CHROME"));

			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector, times(1)).addLogsToTestCaseInSession(anyInt(), anyString());
			ArgumentCaptor<List<Rectangle>> rectangleArgument = ArgumentCaptor.forClass(List.class);
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), rectangleArgument.capture()); // 1 snapshot sent for comparison
			List<Rectangle> rectangles = rectangleArgument.getValue();
			Assert.assertEquals(rectangles.size(), 1); // 1 exclude zone sent with snapshot

			// verify that the final check for comparison result is done
			verify(serverConnector).getTestCaseInSessionComparisonResult(eq(0), any(StringBuilder.class));

			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Snapshots has been recorded with TestCaseSessionId: 0")); // one snapshot has no name, error message is displayed

			verify(serverConnector, times(10)).recordStepResult(any(TestStep.class), anyInt(), anyInt()); // all steps are recorded

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.STARTED_BY);
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
	}

	/**
	 * Check all calls are done when using a real driver
	 * Use the behaviour "addTestResult" as more calls are done
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationWithAddTestResult() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.STARTED_BY, "http://mylauncher/test");

			initMocks(mockedCommonReporter, mockedServerConnector);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});

			// check browser has the same valeurs for all calls
			verify(serverConnector).createSession(anyString(), eq("BROWSER:CHROME"), eq("http://mylauncher/test"), any(OffsetDateTime.class));
			// one snapshot is compared with reference during test run to check if test must be replayed
			verify(serverConnector, never()).checkSnapshotHasNoDifferences(any(Snapshot.class), eq("testDriverCustomSnapshot"), eq("DriverTestPage._captureSnapshot"), eq("BROWSER:CHROME"));

			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector, times(1)).addLogsToTestCaseInSession(anyInt(), anyString());
			ArgumentCaptor<List<Rectangle>> rectangleArgument = ArgumentCaptor.forClass(List.class);
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), rectangleArgument.capture()); // 1 snapshot sent for comparison
			List<Rectangle> rectangles = rectangleArgument.getValue();
			Assert.assertEquals(rectangles.size(), 1); // 1 exclude zone sent with snapshot

			// verify that the final check for comparison result is done
			verify(serverConnector).getTestCaseInSessionComparisonResult(eq(0), any(StringBuilder.class));

			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Snapshots has been recorded with TestCaseSessionId: 0")); // one snapshot has no name, error message is displayed

			verify(serverConnector, times(10)).recordStepResult(any(TestStep.class), anyInt(), anyInt()); // all steps are recorded

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.STARTED_BY);
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
	}

	/**
	 * Check test session is created only once even when multiple suites are executed
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationMultiSuites() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.STARTED_BY, "http://mylauncher/test");

			initMocks(mockedCommonReporter, mockedServerConnector);

			executeMultiSuites(new String[]{"com.seleniumtests.it.stubclasses.StubTestClass"}, new String[]{"testAndSubActions"});

			// check session is created only once, even with multiple suites
			verify(serverConnector).createSession(anyString(), eq("BROWSER:NONE"), eq("http://mylauncher/test"), any(OffsetDateTime.class));
			verify(serverConnector, times(2)).createTestCase("testAndSubActions");
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testAndSubActions"), eq("SUCCESS"), eq("LOCAL"), eq("a test with steps"), any(OffsetDateTime.class));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testAndSubActions-1"), eq("SUCCESS"), eq("LOCAL"), eq("a test with steps"), any(OffsetDateTime.class));


		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.STARTED_BY);
		}
	}

	@Test(groups={"it"})
	public void testReportGenerationWithPreviousReport() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.STARTED_BY, "http://mylauncher/test");

			initMocks(mockedCommonReporter, mockedServerConnector);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithException"});

			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector).createSession(anyString(), eq("BROWSER:NONE"), eq("http://mylauncher/test"), any(OffsetDateTime.class));

			verify(serverConnector).createTestCase("testWithException");
			verify(serverConnector).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testWithException"), eq("FAILURE"), eq("LOCAL"), eq(""), any(OffsetDateTime.class));
			verify(serverConnector).createTestStep(eq("Previous execution results"), anyInt());

			verify(serverConnector, times(7)).recordStepResult(any(TestStep.class), anyInt(), anyInt()); // all steps are recorded

			// check images and previous execution results are recorded
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "htmls", "testWithException_0-1_step_1--tened.html").toFile()), anyInt());
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "screenshots", "testWithException_0-1_step_1--rtened.png").toFile()), anyInt());
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "retry-testWithException-1.zip").toFile()), anyInt());
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "retry-testWithException-2.zip").toFile()), anyInt());

			// check logs has been uploaded (one upload for each test)
			verify(serverConnector).uploadLogs(any(File.class), eq(0));
			verify(serverConnector).uploadLogs(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "execution.log").toFile()), eq(0));


		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
			System.clearProperty(SeleniumTestsContext.STARTED_BY);
		}
	}
	
	/**
	 * When selenium server is not active, do not try to send information to it
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationServerInactive() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testSkipped", "testOkWithTestName"});
			
			// check server has NOT been called for all aspects of test (app, version, ...)
			verify(serverConnector, never()).createSession(anyString(), anyString(), isNull(), any(OffsetDateTime.class));
			verify(serverConnector, never()).createTestCase(anyString());
			verify(serverConnector, never()).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector, never()).createTestCaseInSession(anyInt(), anyInt(), anyString(), anyString(), anyString(), eq(""), any(OffsetDateTime.class));
			verify(serverConnector, never()).createTestStep(anyString(), anyInt());
			verify(serverConnector, never()).createSnapshot(any(Snapshot.class), anyInt(), eq(new ArrayList<>())); // two snapshots but only once is sent because the other has no name
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * When creation of test steps fails, logs and test infos are not sent
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationErrorCreatingTestStep() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			doThrow(SeleniumRobotServerException.class).when(serverConnector).createTestStep(anyString(), anyInt());
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Error recording result on selenium robot server")); // one snapshot has no name, error message is displayed

			// check logs has NOT been uploaded
			verify(serverConnector, never()).uploadLogs(any(File.class), eq(0));

			// check test infos has NOT been sent
			verify(serverConnector, never()).recordTestInfo(any(), eq(0));

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * When creation of test case fails, steps, logs and test infos are not sent
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationErrorCreatingTestCase() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			doThrow(SeleniumRobotServerException.class).when(serverConnector).createTestCase(anyString());
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Error recording result on selenium robot server")); // one snapshot has no name, error message is displayed

			// check steps are NOT sent
			verify(serverConnector, never()).createTestStep(anyString(), anyInt());

			// check logs has NOT been uploaded
			verify(serverConnector, never()).uploadLogs(any(File.class), eq(0));

			// check test infos has NOT been sent
			verify(serverConnector, never()).recordTestInfo(any(), eq(0));

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	/**
	 * Test that focuses on snapshots: it's sent to server, exclusion zones are also sent
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationWithSnapshots() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");

			ArgumentCaptor<List<Rectangle>> listArgument = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);

			initMocks(mockedCommonReporter, mockedServerConnector);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector).createSession(anyString(), eq("BROWSER:CHROME"), isNull(), any(OffsetDateTime.class));
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testDriverCustomSnapshot"), eq("SUCCESS"), eq("LOCAL"), eq(""), any(OffsetDateTime.class));
			verify(serverConnector).createTestStep(eq("_captureSnapshot"), anyInt());
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), listArgument.capture()); // 1 custom snapshot taken with name

			verify(serverConnector, times(12)).uploadFile(fileCapture.capture(), eq(0)); // 5 pictures, 5 HTML, 1 HAR, 1 driver logs
			List<File> allFiles = fileCapture.getAllValues();

			Assert.assertTrue(allFiles.stream().noneMatch(f -> f.getName().contains("my_snapshot")));
			Assert.assertTrue(allFiles.stream().anyMatch(f -> f.getName().contains("driver-log-browser.txt")));
			Assert.assertTrue(allFiles.stream().anyMatch(f -> f.getName().contains("main-networkCapture.har")));
			Assert.assertEquals(allFiles.stream().filter(f -> f.getName().endsWith(".html")).count(), 5);
			Assert.assertEquals(allFiles.stream().filter(f -> f.getName().endsWith(".png")).count(), 5);

			// check exclude zones have been sent
			Assert.assertEquals(listArgument.getValue().size(), 1);
			Assert.assertTrue(listArgument.getValue().get(0).getWidth() > 150); // don't be precise as it may depend on screen / computer
			Assert.assertTrue(listArgument.getValue().get(0).getHeight() > 15);
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
	}
	
	/**
	 * Test that focuses on snapshots: if flag SELENIUMROBOTSERVER_COMPARE_SNAPSHOT is set to false, snapshots are sent as attachments
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationWithoutSnapshots() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");

			initMocks(mockedCommonReporter, mockedServerConnector);
			ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector).createSession(anyString(), eq("BROWSER:CHROME"), isNull(), any(OffsetDateTime.class));
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testDriverCustomSnapshot"), eq("SUCCESS"), eq("LOCAL"), eq(""), any(OffsetDateTime.class));
			verify(serverConnector).createTestStep(eq("_captureSnapshot"), anyInt());

			// check capture recorded for comparison is sent to server as attachment
			verify(serverConnector, times(14)).uploadFile(fileCapture.capture(), eq(0)); // 1 HAR, 5 picture + HTML + 2 pictures for comparison + driver logs
			Assert.assertEquals(fileCapture.getAllValues().stream().filter(f -> f.getName().contains("my_snapshot")).count(), 2);
			verify(serverConnector, never()).createSnapshot(any(Snapshot.class), anyInt(), eq(new ArrayList<>())); // 1 custom snapshot taken with name
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
	}
	
	/**
	 * Test when no seleniumrobot server is present
	 */
	@Test(groups={"it"})
	public void testNoReportWhenServerIsOffline() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
				when(variableServer.isAlive()).thenReturn(false);
			});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			reporter = spy(new SeleniumRobotServerTestRecorder());
			mockedCommonReporter.when(() -> CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);
			mockedServerConnector.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(serverConnector);

			doReturn(serverConnector).when(reporter).getServerConnector();
			when(serverConnector.getActive()).thenReturn(false);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
	
			// check server has been called for all aspects of test (app, version, ...)
			verify(serverConnector, never()).createSession(anyString(), eq("BROWSER:NONE"), isNull(), any(OffsetDateTime.class));
			
			// check all test cases are created, in both test classes
			verify(serverConnector, never()).createTestCase(anyString());
			verify(serverConnector, never()).createTestCaseInSession(anyInt(), anyInt(), anyString(), anyString(), anyString(), eq(""), any(OffsetDateTime.class));
			verify(serverConnector, never()).createTestStep(anyString(), anyInt());
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	

	/**
	 * Test when seleniumRobot server raises an error when recording test result
	 * Recording stops as soon as an error is raised to avoid inconsistencies in data
	 */
	@Test(groups={"it"})
	public void testErrorHandlingWhenRecordingTestResult() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			doThrow(SeleniumRobotServerException.class).when(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong(), anyInt(), anyInt());
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError"});
			
			// check server has been called for session
			verify(serverConnector).createSession(anyString(), eq("BROWSER:NONE"), isNull(), any(OffsetDateTime.class)); // once per TestNG context (so 1 time here)
			verify(serverConnector, times(15)).recordStepResult(any(TestStep.class), anyInt(), anyInt());
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check reference snapshot is retrieved when step fails
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsStepReferenceForFailedStep() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), eq(120));

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});

			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement", 0);
			verify(serverConnector, times(1)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps OK
			verify(serverConnector, never()).createStepReferenceSnapshot(any(Snapshot.class), eq(123)); // no reference recording for failed step
			verify(serverConnector).getReferenceSnapshot(anyInt()); // check we get reference snapshot for failed step
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * If SELENIUMROBOTSERVER_COMPARE_SNAPSHOT is true but not SELENIUMROBOTSERVER_RECORD_RESULTS, references should not be recorded
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDoesNotContainsStepReferenceForFailedStep() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), eq(120));

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement", 0);
			verify(serverConnector, never()).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // no reference recording at all
			verify(serverConnector, never()).createStepReferenceSnapshot(any(Snapshot.class), eq(123)); // no reference recording for failed step
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * We will check that all calls to server will be done even if an error occurs when calling 'createStepReferenceSnapshot' on the first time
	 * Further call should continue
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsStepReferenceForFailedStepWithError() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			initMocks(mockedCommonReporter, mockedServerConnector);
			// check failed step is recorded
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(any(TestStep.class), anyInt(), eq(120));
			
			
			doThrow(SeleniumRobotServerException.class).doNothing().when(serverConnector).createStepReferenceSnapshot(any(Snapshot.class), anyInt());
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(1)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for steps OK
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * Check that when server is not up to date, trying to record reference snapshot will fail but should not prevent test to continue
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDoesNotContainReferenceIfServerNotUpToDate() throws Exception {
		
		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks(mockedCommonReporter, mockedServerConnector);
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(any(TestStep.class), anyInt(), eq(120));
			
			
			doThrow(SeleniumRobotServerException.class).when(serverConnector).createStepReferenceSnapshot(any(Snapshot.class), anyInt());
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(1)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	@Test(groups={"it"})
	public void testReportContainsDownloadedFile() throws Exception {

		try (MockedConstruction mockedVariableServer = mockConstruction(SeleniumRobotVariableServerConnector.class, (variableServer, context) -> {
			when(variableServer.isAlive()).thenReturn(true);
		});
			 MockedStatic mockedServerConnector = mockStatic(SeleniumRobotSnapshotServerConnector.class);
			 MockedStatic mockedCommonReporter = mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		) {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.STARTED_BY, "http://mylauncher/test");

			initMocks(mockedCommonReporter, mockedServerConnector);
			ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDownloadFile"});

			// check browser has the same valeurs for all calls
			verify(serverConnector).createSession(anyString(), eq("BROWSER:CHROME"), eq("http://mylauncher/test"), any(OffsetDateTime.class));

			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDownloadFile");
			verify(serverConnector, times(9)).uploadFile(fileCapture.capture(), eq(0)); // 1 PDF + 3 HTML + 3 PNG + 1 HAR + 1 driver logs
			Assert.assertTrue(fileCapture.getAllValues().stream().filter(f -> f.getName().equals("nom-du-fichier.pdf")).findFirst().isPresent());

		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.STARTED_BY);
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
	}

	/**
	 * @throws Exception
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void initMocks(MockedStatic mockedCommonReporter, MockedStatic mockedServerConnector) throws Exception {

		reporter = spy(new SeleniumRobotServerTestRecorder());
		mockedCommonReporter.when(() -> CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);
		mockedServerConnector.when(() -> SeleniumRobotSnapshotServerConnector.getInstance()).thenReturn(serverConnector);

		reset(serverConnector); // reset call count
		when(serverConnector.getReferenceSnapshot(anyInt())).thenReturn(File.createTempFile("img", ".png"));
		
		when(serverConnector.detectFieldsInPicture(any(ScreenShot.class))).thenReturn(new JSONObject("{'fields': [], 'labels': [], 'version': 'aaa', 'error': null}"));
		when(serverConnector.detectErrorInPicture(any(ScreenShot.class))).thenReturn(new JSONObject("{'fields': [], 'labels': [], 'version': 'aaa', 'error': null}"));

		doReturn(serverConnector).when(reporter).getServerConnector();
		when(serverConnector.getActive()).thenReturn(true);
		when(serverConnector.getUrl()).thenReturn("http://localhost:1234");
	}
}
