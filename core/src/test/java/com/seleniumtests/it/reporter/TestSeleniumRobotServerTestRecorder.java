package com.seleniumtests.it.reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.app.VelocityEngine;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumRobotServerTestRecorder;
import com.seleniumtests.reporter.SeleniumTestsReporter2;
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
	@SuppressWarnings("unchecked")
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
		verify(serverConnector, times(9)).createTestCaseInSession(); 
		verify(serverConnector, times(3)).createTestStep("step 1");
		verify(serverConnector).createTestStep("step 2");
		verify(serverConnector).recordStepResult(false, "Step step 1\\nclick button\\nsendKeys to text field\\nStep step 1.3: open page\\nclick link\\na message\\nsendKeys to password field");
		
	}
	
	/**
	 * Test when no seleniumrobot server is present
	 */
	
	
	/**
	 * Test when seleniumRobot server raises an error when registring app
	 */
	

	/**
	 * Test when seleniumRobot server raises an error when recording test result
	 */
}
