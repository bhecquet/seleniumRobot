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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import org.apache.commons.lang3.StringUtils;
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
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.reporter.reporters.SeleniumRobotServerTestRecorder;

@PrepareForTest({SeleniumRobotSnapshotServerConnector.class, CommonReporter.class, SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class})
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
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);

			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);

			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(true);
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testSkipped"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector, atLeastOnce()).createSession(anyString());
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testAndSubActions");
			verify(serverConnector).createTestCase("testInError");
			verify(serverConnector).createTestCase("testWithException");
			verify(serverConnector).createTestCase("testSkipped");
			verify(serverConnector, times(4)).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testAndSubActions")); 
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testInError")); 
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testWithException")); 
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testSkipped")); 
			verify(serverConnector, times(3)).createTestStep(eq("step 1"), anyInt());
			verify(serverConnector).createTestStep(eq("step 2"), anyInt());
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), anyInt(), anyInt()); // two snapshots but only once is sent because the other has no name
			verify(serverConnector, never()).createExcludeZones(any(Rectangle.class), anyInt());
			
			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("Snapshot hasn't any name, it won't be sent to server")); // one snapshot has no name, error message is displayed
			
			// check that screenshot information are removed from logs (the pattern "Output: ...")
			verify(serverConnector).recordStepResult(eq(true), contains("step 1.3: open page"), eq(1230L), anyInt(), anyInt(), anyInt());
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Test that focuses on snapshots: it's sent to server, exclusion zones are also sent
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGenerationWithSnapshots() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			
			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);
			
			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(true);
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
			// check server has been called for all aspects of test (app, version, ...)
			// they may be called for each test but server is responsible for uniqueness of the value
			verify(serverConnector, atLeastOnce()).createSession(anyString());
			
			// issue #331: check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testDriverCustomSnapshot");
			verify(serverConnector).addLogsToTestCaseInSession(anyInt(), anyString());
			verify(serverConnector).createTestCaseInSession(anyInt(), anyInt(), eq("testDriverCustomSnapshot")); 
			verify(serverConnector).createTestStep(eq("_captureSnapshot with args: (my snapshot, )"), anyInt());
			verify(serverConnector).createSnapshot(any(Snapshot.class), anyInt(), anyInt(), anyInt()); // 1 custom snapshot taken with name
			verify(serverConnector).createExcludeZones(any(Rectangle.class), anyInt()); // one exclude zone created with that snapshot
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Test when no seleniumrobot server is present
	 */
	@Test(groups={"it"})
	public void testNoReportWhenServerIsOffline() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(false);

			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);

			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(false);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
	
			// check server has been called for all aspects of test (app, version, ...)
			verify(serverConnector, never()).createSession(anyString());
			
			// check all test cases are created, in both test classes
			verify(serverConnector, never()).createTestCase(anyString());
			verify(serverConnector, never()).createTestCaseInSession(anyInt(), anyInt(), anyString()); 
			verify(serverConnector, never()).createTestStep(anyString(), anyInt());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	

	/**
	 * Test when seleniumRobot server raises an error when recording test result
	 * Recording stops as soon as an error is raised to avoid inconsistencies in data
	 */
	@Test(groups={"it"})
	public void testErrorHandlingWhenRecordingTestResult() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);

			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);

			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(true);
			
			doThrow(SeleniumRobotServerException.class).when(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong(), anyInt(), anyInt(), anyInt());
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError"});
			
			// check server has been called for session
			verify(serverConnector).createSession(anyString()); // once per TestNG context (so 1 time here)
			verify(serverConnector, times(3)).recordStepResult(anyBoolean(), anyString(), anyLong(), anyInt(), anyInt(), anyInt()); // once for each test execution + the final logging which is done only once as it fails
																							// it shows that when result has not been recorded, it's retried
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	@Test(groups={"it"})
	public void testReportContainsStepReferenceForFailedStep() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			

			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);

			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);

			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(true);
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), anyInt(), eq(120));
//			when(serverConnector.recordStepResult(eq(false), anyString(), anyInt(), anyInt(), anyInt(), eq(120))).thenReturn(123);
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});

			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(2)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps
			verify(serverConnector, never()).createStepReferenceSnapshot(any(Snapshot.class), eq(123)); // no reference recording for failed step
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * We will check that at all calls to server will be done even if an error occurs when calling 'createStepReferenceSnapshot' on the first time
	 * Further call should continue
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsStepReferenceForFailedStepWithError() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			
			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);
			
			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(true);
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), anyInt(), eq(120));
			
			
			doThrow(SeleniumRobotServerException.class).doNothing().when(serverConnector).createStepReferenceSnapshot(any(Snapshot.class), anyInt());
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(2)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps 
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
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
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			
			reporter = spy(new SeleniumRobotServerTestRecorder());
			PowerMockito.mockStatic(CommonReporter.class, Mockito.CALLS_REAL_METHODS);
			PowerMockito.when(CommonReporter.getInstance(SeleniumRobotServerTestRecorder.class)).thenReturn(reporter);
			
			when(reporter.getServerConnector()).thenReturn(serverConnector);
			when(serverConnector.getActive()).thenReturn(true);
			when(serverConnector.createTestStep("_writeSomethingOnNonExistentElement ", 0)).thenReturn(120);
			doReturn(123).when(serverConnector).recordStepResult(eq(false), anyString(), anyLong(), anyInt(), anyInt(), eq(120));
			
			
			doThrow(SeleniumRobotServerException.class).when(serverConnector).createStepReferenceSnapshot(any(Snapshot.class), anyInt());
			
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			verify(serverConnector).createTestStep("_writeSomethingOnNonExistentElement ", 0);
			verify(serverConnector).getReferenceSnapshot(123); // check id of failed "stepResult" is used and we try to get the reference image for this failed step
			verify(serverConnector, times(2)).createStepReferenceSnapshot(any(Snapshot.class), eq(0)); // reference recording for other steps 
			
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
}
