/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.reporter.reporters.SeleniumRobotServerTestRecorder;

@PrepareForTest({SeleniumRobotSnapshotServerConnector.class, SeleniumRobotServerTestRecorder.class})
public class TestSeleniumRobotServerTestRecorder extends ReporterTest {
	
	private SeleniumRobotServerTestRecorder reporter;
	
	@Mock
	SeleniumRobotSnapshotServerConnector serverConnector;
	
	/**
	 * In this test, everything is fine with seleniumrobot server
	 * @throws Exception
	 */
	// TODO: re-enable these tests. For now, powermockito does not mock "new" call
	// TODO: check that record is not done when compareSnapshot & resultRecord are false
	// TODO: check that record is not done when seleniumRobotServer is not active (by params)
	@Test(groups={"it"}, enabled=false)
	public void testReportGeneration() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		PowerMockito.whenNew(SeleniumRobotSnapshotServerConnector.class).withNoArguments().thenReturn(serverConnector);
//		PowerMockito.mockStatic(SeleniumRobotServerTestRecorder.class);
//		when(SeleniumRobotServerTestRecorder.class.newInstance()).thenReturn(reporter);
//		
//		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(true);

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClass2"});
		

		// check server has been called for all aspects of test (app, version, ...)
		verify(serverConnector).createApplication();
		verify(serverConnector).createVersion();
		verify(serverConnector).createEnvironment();
		verify(serverConnector).createSession();
		
		// check all test cases are created, in both test classes
		verify(serverConnector).createTestCase("testAndSubActions");
		verify(serverConnector).createTestCase("testInError");
		verify(serverConnector).createTestCase("testWithException");
		verify(serverConnector).createTestCase("test1");
		verify(serverConnector).createTestCase("test2");
		verify(serverConnector).createTestCase("test3");
		verify(serverConnector).createTestCase("test4");
		verify(serverConnector).createTestCase("test5");
		verify(serverConnector).createTestCase("test6");
		verify(serverConnector, times(10)).addLogsToTestCaseInSession(anyString());
		verify(serverConnector, times(10)).createTestCaseInSession(); 
		verify(serverConnector, times(3)).createTestStep("step 1");
		verify(serverConnector).createTestStep("step 2");
		verify(serverConnector).createSnapshot(any(File.class));
		
		// check that screenshot information are removed from logs (the pattern "Output: ...")
		verify(serverConnector).recordStepResult(eq(false), eq("Step step 1\nclick button\nsendKeys to text field\nStep step 1.3: open page\nclick link\na message\nsendKeys to password field"), anyLong());
	}
	
	/**
	 * Test when no seleniumrobot server is present
	 */
	@Test(groups={"it"}, enabled=false)
	public void testNoReportWhenServerIsOffline() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(false);

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClass2"});
		

		// check server has been called for all aspects of test (app, version, ...)
		verify(serverConnector, never()).createApplication();
		verify(serverConnector, never()).createVersion();
		verify(serverConnector, never()).createEnvironment();
		verify(serverConnector, never()).createSession();
		
		// check all test cases are created, in both test classes
		verify(serverConnector, never()).createTestCase(anyString());
		verify(serverConnector, never()).createTestCaseInSession(); 
		verify(serverConnector, never()).createTestStep(anyString());
	}
	
	
	/**
	 * Test when seleniumRobot server raises an error when registring app
	 */
	@Test(groups={"it"}, enabled=false)
	public void testErrorHandlingWhenRecordingApp() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(true);
		doThrow(SeleniumRobotServerException.class).when(serverConnector).createApplication();

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClass2"});
		

		// check that process is interrupted
		verify(serverConnector).createApplication();
		verify(serverConnector, never()).createVersion();
		verify(serverConnector, never()).createEnvironment();
		verify(serverConnector, never()).createSession();
		
		// check no recording has been performed
		verify(serverConnector, never()).createTestCase(anyString());
		verify(serverConnector, never()).createTestCaseInSession(); 
		verify(serverConnector, never()).createTestStep(anyString());
	}
	

	/**
	 * Test when seleniumRobot server raises an error when recording test result
	 * Recording stops as soon as an error is raised to avoid inconsistencies in data
	 */
	@Test(groups={"it"}, enabled=false)
	public void testErrorHandlingWhenRecordingTestResult() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(true);
		doThrow(SeleniumRobotServerException.class).when(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong());

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClass2"});
		

		// check server has been called for all aspects of test (app, version, ...)
		verify(serverConnector).createApplication();
		verify(serverConnector).createVersion();
		verify(serverConnector).createEnvironment();
		verify(serverConnector).createSession();
		
		// only one test should be created because process is interrupted
		verify(serverConnector).createTestCase(anyString());
		verify(serverConnector).createTestCaseInSession(); 
		verify(serverConnector).createTestStep(anyString());
		verify(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong());
	}
}
