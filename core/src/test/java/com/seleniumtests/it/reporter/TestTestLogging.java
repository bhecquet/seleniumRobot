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
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumTestsReporter2;
import com.seleniumtests.reporter.TestListener;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestTestLogging extends GenericTest {

	@BeforeMethod(groups={"ut"})
	public void reset() {
		TestLogging.reset();
	}

	private XmlSuite executeSubTest(int threadCount, String testClassName) {
		
		SeleniumTestsReporter2 reporter = new SeleniumTestsReporter2();
		TestListener testListener = new TestListener();
		
		XmlSuite suite = new XmlSuite();
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		suite.setName("TmpSuite");
		
		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.METHODS);
		}
		 
		XmlTest test = new XmlTest(suite);
		test.setName("FirstTest");
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass("com.seleniumtests.it.reporter." + testClassName));
		test.setXmlClasses(classes) ;
		test.addParameter(SeleniumTestsContext.BROWSER, "none");
		
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.addListener((IReporter)reporter);
		tng.addListener((ITestListener)testListener);
		tng.addListener((IInvokedMethodListener)testListener);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
	
	/**
	 * Check SeleniumRobot creates log file
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkFileLogger() throws Exception {
		executeSubTest(1, "StubTestClassWithWait");
		Assert.assertTrue(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory() + "/seleniumRobot.log").isFile());
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file
	 * One test at a time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkLogParsing() throws Exception {
		executeSubTest(1, "StubTestClassWithWait");
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test1").contains("test1 finished"));	
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file
	 * Several tests run at the same time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkLogParsingWithThreads() throws Exception {
		executeSubTest(3, "StubTestClassWithWait");
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test1").contains("test1 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test2").contains("test2 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test3").contains("test3 finished"));	
	}
	
	@Test(groups = { "it" })
	public void checkLogParsingWithSeveralThreadsPerTest() throws Exception {
		executeSubTest(2, "StubTestClassWithWait");
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test1").contains("test1 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test2").contains("test2 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test3").contains("test3 finished"));	
	}
	
	@Test(groups = { "it" })
	public void checkLogParsingWithRetry() throws Exception {
		executeSubTest(2, "StubTestClassWithWait");	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testSimulatingRetry").contains("TestLogging: [RETRYING] class com.seleniumtests.it.reporter.StubTestClassWithWait FAILED, Retrying 1 time"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testSimulatingRetry").contains("testSimulatingRetry starting"));	
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file when test is executed with pages
	 * One test at a time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkTestStepHandling() throws Exception {
		executeSubTest(1, "StubTestClassForTestSteps");
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testPage").contains("Start method testPage"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testPage").contains("TestLogging: tell me why"));
		
		// check log level is present
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testPage").contains("INFO "));	
	}
	
}
