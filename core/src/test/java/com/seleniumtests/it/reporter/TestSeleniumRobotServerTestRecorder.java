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

import java.io.File;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.reporter.reporters.SeleniumRobotServerTestRecorder;

@PrepareForTest({SeleniumRobotSnapshotServerConnector.class, CommonReporter.class, SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class})
@PowerMockIgnore({"javax.net.ssl.*", "com.google.inject.*"})
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
			verify(serverConnector, atLeastOnce()).createSession();
			
			// check all test cases are created, call MUST be done only once to avoid result to be recorded several times
			verify(serverConnector).createTestCase("testAndSubActions");
			verify(serverConnector).createTestCase("testInError");
			verify(serverConnector).createTestCase("testWithException");
			verify(serverConnector).createTestCase("testSkipped");
			verify(serverConnector, times(4)).addLogsToTestCaseInSession(anyString());
			verify(serverConnector, times(4)).createTestCaseInSession(); 
			verify(serverConnector, times(3)).createTestStep("step 1");
			verify(serverConnector).createTestStep("step 2");
			verify(serverConnector).createSnapshot(any(File.class)); // one snapshot
			
			// check that screenshot information are removed from logs (the pattern "Output: ...")
			verify(serverConnector).recordStepResult(eq(false), contains("step 1.3: open page"), eq(1230L));
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
			verify(serverConnector, never()).createSession();
			
			// check all test cases are created, in both test classes
			verify(serverConnector, never()).createTestCase(anyString());
			verify(serverConnector, never()).createTestCaseInSession(); 
			verify(serverConnector, never()).createTestStep(anyString());
			
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
			
			doThrow(SeleniumRobotServerException.class).when(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong());
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError"});
			
			// check server has been called for session
			verify(serverConnector, times(3)).createSession(); // once for each test, and once after execution finishes
			verify(serverConnector, times(3)).recordStepResult(anyBoolean(), anyString(), anyLong()); // once for each test execution + the final logging which is done only once as it fails
																							// it shows that when result has not been recorded, it's retried
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
}
