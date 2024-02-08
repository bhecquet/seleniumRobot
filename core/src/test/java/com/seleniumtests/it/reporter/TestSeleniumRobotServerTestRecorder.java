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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.info.Info;
import com.seleniumtests.reporter.info.MultipleInfo;
import com.seleniumtests.reporter.logger.TestStep;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Rectangle;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.reporter.reporters.SeleniumRobotServerTestRecorder;

import kong.unirest.json.JSONObject;

@PrepareForTest({SeleniumRobotSnapshotServerConnector.class, CommonReporter.class, SeleniumRobotVariableServerConnector.class, SeleniumRobotServerContext.class, SeleniumTestsContext.class})
public class TestSeleniumRobotServerTestRecorder extends ReporterTest {
	
	private SeleniumRobotServerTestRecorder reporter;
	
	@Mock
	SeleniumRobotSnapshotServerConnector serverConnector;
	
	@Mock
	SeleniumRobotVariableServerConnector variableServer;
	
	/**
	 * In this test, everything is fine with seleniumrobot server
	 * @throws Exception
	 */
	// TODO: check that record is not done when compareSnapshot & resultRecord are false
	// TODO: check that record is not done when seleniumRobotServer is not active (by params)
	@Test(groups={"it"})
	public void testReportGeneration() throws Exception {
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testSkipped", "testOkWithTestName"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector, atLeastOnce()).createSession(anyString(), eq("BROWSER:NONE"));
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testAndSubActions");
			verify(serverConnector).createTestCase("testInError");
			verify(serverConnector).createTestCase("testWithException");
			verify(serverConnector).createTestCase("testSkipped");
			verify(serverConnector, times(5)).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testAndSubActions"), eq("SUCCESS"), eq("LOCAL"), eq("a test with steps"));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testInError"), eq("FAILURE"), eq("LOCAL"), eq(""));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testWithException"), eq("FAILURE"), eq("LOCAL"), eq(""));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testSkipped"), eq("SKIP"), eq("LOCAL"), eq(""));
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("A test which is <OK> é&"), eq("SUCCESS"), eq("LOCAL"), eq("")); // a test with custom name
			verify(serverConnector, times(4)).createTestStep(eq("step 1"), anyInt());
			verify(serverConnector).createTestStep(eq("step 2"), anyInt());
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), eq(new ArrayList<>())); // two snapshots but only once is sent because the other has no name
			verify(serverConnector, never()).createExcludeZones(any(Rectangle.class), anyInt());
			
			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Snapshot hasn't any name, it won't be sent to server")); // one snapshot has no name, error message is displayed

			verify(serverConnector, times(30)).recordStepResult(any(TestStep.class), anyInt(), anyInt()); // all steps are recorded
			// files are uploaded. Only 'testWithException' holds a Snapshot with snapshotCheckType.NONE and so, only its 2 files are uploaded
			// other snapshots, which are used for image comparison / regression are uploaded through an other service 'createSnapshot'
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "htmls", "testWithException_0-1_step_1--tened.html").toFile()), anyInt());
			verify(serverConnector).uploadFile(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "screenshots", "testWithException_0-1_step_1--rtened.png").toFile()), anyInt());
			// check image id and html id has been updated once files has been uploaded, for tests where snapshots has been uploaded
			verify(serverConnector).updateStepResult(contains("\"snapshots\":[{\"idHtml\":0,\"displayInReport\":true,\"name\":\"a name\",\"idImage\":0,\"failed\":false,\"position\":0,\"type\":\"snapshot\",\"snapshotCheckType\":\"NONE\""), eq(0));
			verify(serverConnector).updateStepResult(contains("\"snapshots\":[{\"idHtml\":null,\"displayInReport\":true,\"name\":\"main\""), eq(0));
			
			// check logs has been uploaded (one upload for each test)
			verify(serverConnector, times(5)).uploadLogs(any(File.class), eq(0));
			verify(serverConnector).uploadLogs(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "execution.log").toFile()), eq(0));
			verify(serverConnector).uploadLogs(eq(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "execution.log").toFile()), eq(0));

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
		}
	}
	
	/**
	 * When selenium server is not active, do not try to send information to it
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationServerInactive() throws Exception {
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			initMocks();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testSkipped", "testOkWithTestName"});
			
			// check server has NOT been called for all aspects of test (app, version, ...)
			verify(serverConnector, never()).createSession(anyString(), anyString());
			verify(serverConnector, never()).createTestCase(anyString());
			verify(serverConnector, never()).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector, never()).createTestCaseInSession(anyInt(), anyInt(), anyString(), anyString(), anyString(), eq(""));
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

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks();
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

		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks();
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
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			
			ArgumentCaptor<List<Rectangle>> listArgument = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);

			initMocks();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector, atLeastOnce()).createSession(anyString(), eq("BROWSER:CHROME"));
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testDriverCustomSnapshot"), eq("SUCCESS"), eq("LOCAL"), eq(""));
			verify(serverConnector).createTestStep(eq("_captureSnapshot with args: (my snapshot, )"), anyInt());
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), listArgument.capture()); // 1 custom snapshot taken with name

			verify(serverConnector, times(4)).uploadFile(fileCapture.capture(), eq(0));
			Assert.assertTrue(fileCapture.getAllValues().stream().noneMatch(f -> f.getName().contains("my_snapshot")));

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
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");

			initMocks();
			ArgumentCaptor<File> fileCapture = ArgumentCaptor.forClass(File.class);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector, atLeastOnce()).createSession(anyString(), eq("BROWSER:CHROME"));
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testDriverCustomSnapshot"), eq("SUCCESS"), eq("LOCAL"), eq(""));
			verify(serverConnector).createTestStep(eq("_captureSnapshot with args: (my snapshot, )"), anyInt());

			// check capture recorded for comparison is sent to server as attachment
			verify(serverConnector, times(6)).uploadFile(fileCapture.capture(), eq(0));
			Assert.assertEquals(fileCapture.getAllValues().stream().filter(f -> f.getName().contains("my_snapshot")).count(), 2);
			verify(serverConnector, never()).createSnapshot(any(Snapshot.class), anyInt(), eq(new ArrayList<>())); // 1 custom snapshot taken with name
			verify(serverConnector, never()).createExcludeZones(any(Rectangle.class), anyInt()); // one exclude zone created with that snapshot
			
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
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(false);

			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);

			PowerMockito.mockStatic(SeleniumRobotSnapshotServerConnector.class);
			PowerMockito.doReturn(serverConnector).when(SeleniumRobotSnapshotServerConnector.class, "getInstance");

			doReturn(serverConnector).when(reporter).getServerConnector();
			when(serverConnector.getActive()).thenReturn(false);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
	
			// check server has been called for all aspects of test (app, version, ...)
			verify(serverConnector, never()).createSession(anyString(), eq("BROWSER:NONE"));
			
			// check all test cases are created, in both test classes
			verify(serverConnector, never()).createTestCase(anyString());
			verify(serverConnector, never()).createTestCaseInSession(anyInt(), anyInt(), anyString(), anyString(), anyString(), eq(""));
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
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks();
			doThrow(SeleniumRobotServerException.class).when(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong(), anyInt(), anyInt());
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError"});
			
			// check server has been called for session
			verify(serverConnector).createSession(anyString(), eq("BROWSER:NONE")); // once per TestNG context (so 1 time here)
			verify(serverConnector, times(13)).recordStepResult(any(TestStep.class), anyInt(), anyInt());
			
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
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");

			initMocks();
			
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), eq(120));

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});

			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
			verify(serverConnector, times(2)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps
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
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "false");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			

			initMocks();
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), eq(120));

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
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
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			
			initMocks();
			// check failed step is recorded
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(any(TestStep.class), anyInt(), eq(120));
			
			
			doThrow(SeleniumRobotServerException.class).doNothing().when(serverConnector).createStepReferenceSnapshot(any(Snapshot.class), anyInt());
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(2)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps 
			
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
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			
			initMocks();
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(any(TestStep.class), anyInt(), eq(120));
			
			
			doThrow(SeleniumRobotServerException.class).when(serverConnector).createStepReferenceSnapshot(any(Snapshot.class), anyInt());
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(2)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps 
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
		}
	}

	/**
	 * @throws Exception
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void initMocks() throws Exception, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
		when(variableServer.isAlive()).thenReturn(true);
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
		PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);

		PowerMockito.mockStatic(SeleniumRobotSnapshotServerConnector.class);
		reset(serverConnector); // reset call count
		PowerMockito.doReturn(serverConnector).when(SeleniumRobotSnapshotServerConnector.class, "getInstance");
		when(serverConnector.getReferenceSnapshot(anyInt())).thenReturn(File.createTempFile("img", ".png"));
		
		when(serverConnector.detectFieldsInPicture(any(ScreenShot.class))).thenReturn(new JSONObject("{'fields': [], 'labels': [], 'version': 'aaa', 'error': null}"));
		when(serverConnector.detectErrorInPicture(any(ScreenShot.class))).thenReturn(new JSONObject("{'fields': [], 'labels': [], 'version': 'aaa', 'error': null}"));

		doReturn(serverConnector).when(reporter).getServerConnector();
		when(serverConnector.getActive()).thenReturn(true);
		when(serverConnector.getUrl()).thenReturn("http://localhost:1234");
	}
}
