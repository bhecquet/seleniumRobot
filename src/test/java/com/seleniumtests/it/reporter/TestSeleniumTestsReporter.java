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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.app.VelocityEngine;
import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumTestsReporter;

public class TestSeleniumTestsReporter extends MockitoTest {
	
	private SeleniumTestsReporter reporter;

	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 */
	private XmlSuite executeSubTest() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		 
		XmlTest test = new XmlTest(suite);
		test.setName("FirstTest");
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass("com.seleniumtests.it.reporter.StubTestClass"));
		test.setXmlClasses(classes) ;
		
		XmlTest test2 = new XmlTest(suite);
		test2.setName("SecondTest");
		List<XmlClass> classes2 = new ArrayList<XmlClass>();
		classes2.add(new XmlClass("com.seleniumtests.it.reporter.StubTestClass2"));
		test2.setXmlClasses(classes2) ;
		
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.addListener((IReporter)reporter);
		tng.addListener((ITestListener)reporter);
		tng.addListener((IInvokedMethodListener)reporter);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
	
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
		
		reporter = spy(new SeleniumTestsReporter());

		XmlSuite suite = executeSubTest();
		
		// check data stored in reporter
		Assert.assertEquals(reporter.getFailedTests().get("FirstTest").size(), 2);
		Assert.assertEquals(reporter.getFailedTests().get("SecondTest").size(), 2);
		Assert.assertEquals(reporter.getSkippedTests().get("FirstTest").size(), 0);
		Assert.assertEquals(reporter.getSkippedTests().get("SecondTest").size(), 2);
		Assert.assertEquals(reporter.getPassedTests().get("FirstTest").size(), 1);
		Assert.assertEquals(reporter.getPassedTests().get("SecondTest").size(), 2);
		
		int testClassNb = suite.getTests().size();
		int errorNb = reporter.getFailedTests().get("FirstTest").size() 
					+ reporter.getFailedTests().get("SecondTest").size()
					+ reporter.getSkippedTests().get("FirstTest").size()
					+ reporter.getSkippedTests().get("SecondTest").size();
		
		// check at least one generation occured for each part of the report
		verify(reporter).generateReport(anyList(), anyList(), anyString()); // 1 time only
		verify(reporter).generateSuiteSummaryReport(anyList(), anyString());		// 1 suite => 1 call
		verify(reporter).generateReportsSection(anyList());									// 1 suite => 1 call
		verify(reporter, times(2 * testClassNb)).generateHTML(any(ITestContext.class), 			// 2 * 2 test classes => 4 calls
																							anyBoolean(), 
																							any(ISuite.class), 
																							any(ITestContext.class));
		verify(reporter, times(3 * 2 * testClassNb)).generatePanel(any(VelocityEngine.class), 	// 3 * generateHTML calls (skipped/passed/failed) => 12 calls
																							any(IResultMap.class), 
																							any(StringBuilder.class), 
																							anyString(), 
																							any(ISuite.class), 
																							any(ITestContext.class), 
																							anyBoolean());
		verify(reporter, times(errorNb)).generateExceptionReport(any(Throwable.class), 		// number of tests in error
																							any(ITestNGMethod.class), 
																							any(StringBuilder.class));
		verify(reporter, times(errorNb)).generateTheStackTrace(any(Throwable.class), 		// number of tests in error
																							any(ITestNGMethod.class), 
																							anyString(), 
																							any(StringBuilder.class));
		verify(reporter).copyResources();
		
		// check report is complete without error
		Assert.assertEquals(reporter.getGenerationErrorMessage(), null, "error during generation: " + reporter.getGenerationErrorMessage());
		
	}
}
