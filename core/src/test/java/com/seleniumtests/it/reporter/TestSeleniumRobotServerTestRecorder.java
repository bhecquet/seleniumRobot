package com.seleniumtests.it.reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.reporter.SeleniumRobotServerTestRecorder;
import com.seleniumtests.reporter.TestListener;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestSeleniumRobotServerTestRecorder extends MockitoTest {
	
	private SeleniumRobotServerTestRecorder reporter;
	
	@Mock
	SeleniumRobotSnapshotServerConnector serverConnector;

	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 * @throws IOException 
	 */
	private XmlSuite executeSubTest(String[] testClasses) throws IOException {
		TestListener testListener = new TestListener();
		
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		for (String testClass: testClasses) {
			XmlTest test = new XmlTest(suite);
			test.setName(testClass.substring(testClass.lastIndexOf(".") + 1));
			List<XmlClass> classes = new ArrayList<XmlClass>();
			classes.add(new XmlClass(testClass));
			test.setXmlClasses(classes) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.addListener((IReporter)reporter);
		tng.addListener((ITestListener)testListener);
		tng.addListener((IInvokedMethodListener)testListener);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		SeleniumRobotLogger.parseLogFile();
		
		return suite;
	}
	

	/**
	 * In this test, everything is fine with seleniumrobot server
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportGeneration() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(true);

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass", "com.seleniumtests.it.reporter.StubTestClass2"});
		

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
		verify(serverConnector, times(9)).addLogsToTestCaseInSession(anyString());
		verify(serverConnector, times(9)).createTestCaseInSession(); 
		verify(serverConnector, times(3)).createTestStep("step 1");
		verify(serverConnector).createTestStep("step 2");
		verify(serverConnector).createSnapshot(any(File.class));
		
		// check that screenshot information are removed from logs (the pattern "Output: ...")
		verify(serverConnector).recordStepResult(eq(false), eq("Step step 1\nclick button\nsendKeys to text field\nStep step 1.3: open page\nclick link\na message\nsendKeys to password field"), anyLong());
	}
	
	/**
	 * Test when no seleniumrobot server is present
	 */
	@Test(groups={"it"})
	public void testNoReportWhenServerIsOffline() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(false);

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass", "com.seleniumtests.it.reporter.StubTestClass2"});
		

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
	@Test(groups={"it"})
	public void testErrorHandlingWhenRecordingApp() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(true);
		doThrow(SeleniumRobotServerException.class).when(serverConnector).createApplication();

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass", "com.seleniumtests.it.reporter.StubTestClass2"});
		

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
	@Test(groups={"it"})
	public void testErrorHandlingWhenRecordingTestResult() throws Exception {
		
		reporter = spy(new SeleniumRobotServerTestRecorder());
		when(reporter.getServerConnector()).thenReturn(serverConnector);
		when(serverConnector.getActive()).thenReturn(true);
		doThrow(SeleniumRobotServerException.class).when(serverConnector).recordStepResult(anyBoolean(), anyString(), anyLong());

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass", "com.seleniumtests.it.reporter.StubTestClass2"});
		

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
