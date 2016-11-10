/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.testretry.TestRetryListener;
import com.seleniumtests.reporter.SeleniumTestsReporter;
import com.seleniumtests.reporter.SeleniumTestsReporter2;
import com.seleniumtests.reporter.TestListener;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestSeleniumBothTestsReporters extends MockitoTest {

	private SeleniumTestsReporter reporter;
	private SeleniumTestsReporter2 reporter2;
	private TestListener testListener;
	private TestRetryListener retry;

	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 * @throws IOException 
	 */
	private XmlSuite executeSubTest(String[] testClasses) throws IOException {
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
		tng.addListener((IReporter)reporter2);
		tng.addListener((ITestListener)testListener);
		tng.addListener((IInvokedMethodListener)testListener);
		tng.addListener(retry);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		SeleniumRobotLogger.parseLogFile();
		
		return suite;
	}

	/**
	 * Test in pseudo real conditions where 2 reporters are used at the same time, doing the same thing
	 * with a retry analyzer
	 * In that case, each one try to change the result of a failed test with soft assertion resulting in the test not being 
	 * replayed
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportSummaryContentWithSteps(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter();
		reporter2 = new SeleniumTestsReporter2();
		testListener = new TestListener();
		retry = new TestRetryListener();

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass3"});
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		
		// check content of summary report file
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		
		Assert.assertTrue(mainReportContent.contains("circleFailed\"></i><a href='SeleniumTestReport-1.html'>testFailedWithException</a>"));
		Assert.assertTrue(mainReportContent.contains("circleFailed\"></i><a href='SeleniumTestReport-2.html'>testFailedWithSoftAssertDisabled</a>"));
		Assert.assertTrue(mainReportContent.contains("circleFailed\"></i><a href='SeleniumTestReport-3.html'>testFailedWithSoftAssertEnabled</a>"));
		Assert.assertTrue(mainReportContent.contains("circleFailed\"></i><a href='SeleniumTestReport-4.html'>testMultipleFailedWithSoftAssertEnabled</a>"));
		Assert.assertTrue(mainReportContent.contains("circleSuccess\"></i><a href='SeleniumTestReport-5.html'>testOk</a>"));
		
		// 
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-1\">2</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-2\">2</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-2\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-3\">3</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-3\">1</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-4\">3</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-4\">2</td>"));
		
		// test OK
		Assert.assertTrue(mainReportContent.contains("<td name=\"passed-5\">3</td>"));
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-5\">0</td>"));
	}
	
	/**
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnException(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter();
		reporter2 = new SeleniumTestsReporter2();
		testListener = new TestListener();
		retry = new TestRetryListener();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass3"});
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		
		// check content of summary report file
		String detailsReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-1.html"));
		
		// check no action is executed after the exception
		Assert.assertTrue(detailsReportContent.contains("</button> failAction "));
		Assert.assertFalse(detailsReportContent.contains("</button> add with args: (1, )"));
		
		// check logs contain retry
		Assert.assertTrue(detailsReportContent.contains("TestLogging: [RETRYING] class com.seleniumtests.it.reporter.StubTestClass3.testFailedWithException FAILED, Retrying 1 time"));
	}
	
	/**
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnAssertWithoutSoftAssertion(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter();
		reporter2 = new SeleniumTestsReporter2();
		testListener = new TestListener();
		retry = new TestRetryListener();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass3"});
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		
		// check content of summary report file
		String detailsReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		
		// check no action is executed after the exception
		Assert.assertTrue(detailsReportContent.contains("</button> assertAction "));
		Assert.assertFalse(detailsReportContent.contains("</button> add with args: (1, )"));
		
		// check exception display
		Assert.assertTrue(detailsReportContent.contains("<div>class java.lang.AssertionError: false error expected [true] but found [false]</div>"));
	}
	
	/**
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnAssertWithSoftAssertion(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter();
		reporter2 = new SeleniumTestsReporter2();
		testListener = new TestListener();
		retry = new TestRetryListener();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass3"});
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		
		// check content of summary report file
		String detailsReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-3.html"));
		
		// check no action is executed after the exception
		Assert.assertTrue(detailsReportContent.contains("</button> assertAction "));
		Assert.assertTrue(detailsReportContent.contains("</button> add with args: (1, )"));
		
		// check exception display
		Assert.assertTrue(detailsReportContent.contains("<div>class java.lang.AssertionError: false error expected [true] but found [false]</div>"));
		
		// check log display
		Assert.assertTrue(detailsReportContent.contains("<div> TestLogging: !!!FAILURE ALERT!!! - Assertion Failure: false error expected [true] but found [false]</div>"));
	}
	
	/**
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnMultipleAssertWithSoftAssertion(ITestContext testContext) throws Exception {
		
		reporter = new SeleniumTestsReporter();
		reporter2 = new SeleniumTestsReporter2();
		testListener = new TestListener();
		retry = new TestRetryListener();
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass3"});
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		
		// check content of summary report file
		String detailsReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-4.html"));
		
		// check no action is executed after the exception
		Assert.assertTrue(detailsReportContent.contains("</button> assertAction "));
		Assert.assertTrue(detailsReportContent.contains("</button> assertAction2"));
		Assert.assertTrue(detailsReportContent.contains("</button> add with args: (1, )"));
		
		// check exception display
		Assert.assertTrue(detailsReportContent.contains("<div>class java.lang.AssertionError: !!! Many Test Failures (2)</div>"));
		Assert.assertTrue(detailsReportContent.contains("<div class=\"stack-element\">class java.lang.AssertionError: Failure 1 of 2</div>"));
		Assert.assertFalse(detailsReportContent.contains("at org.aspectj.runtime"));
		Assert.assertFalse(detailsReportContent.contains("at sun.reflect."));
		Assert.assertFalse(detailsReportContent.contains("at org.testng.TestNG"));
		
		// check log display
		Assert.assertTrue(detailsReportContent.contains("<div> TestLogging: !!!FAILURE ALERT!!! - Assertion Failure: false error expected [true] but found [false]</div>"));
		Assert.assertTrue(detailsReportContent.contains("<div> TestLogging: !!!FAILURE ALERT!!! - Assertion Failure: false error2 expected [true] but found [false]</div>"));
	}
	
}
