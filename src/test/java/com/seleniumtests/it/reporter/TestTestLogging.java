package com.seleniumtests.it.reporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumTestsReporter2;
import com.seleniumtests.reporter.TestLogging;

public class TestTestLogging extends GenericTest {


	private XmlSuite executeSubTest(int threadCount, String testClassName) {
		
		SeleniumTestsReporter2 reporter = new SeleniumTestsReporter2();
		
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
		tng.addListener((ITestListener)reporter);
		tng.addListener((IInvokedMethodListener)reporter);
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
		Assert.assertTrue(TestLogging.getTestLogs().get("test1").contains("test1 finished"));	
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file
	 * Several tests run at the same time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkLogParsingWithThreads() throws Exception {
		executeSubTest(3, "StubTestClassWithWait");
		Assert.assertTrue(TestLogging.getTestLogs().get("test1").contains("test1 finished"));	
		Assert.assertTrue(TestLogging.getTestLogs().get("test2").contains("test2 finished"));	
		Assert.assertTrue(TestLogging.getTestLogs().get("test3").contains("test3 finished"));	
	}
	
	@Test(groups = { "it" })
	public void checkLogParsingWithSeveralThreadsPerTest() throws Exception {
		executeSubTest(2, "StubTestClassWithWait");
		Assert.assertTrue(TestLogging.getTestLogs().get("test1").contains("test1 finished"));	
		Assert.assertTrue(TestLogging.getTestLogs().get("test2").contains("test2 finished"));	
		Assert.assertTrue(TestLogging.getTestLogs().get("test3").contains("test3 finished"));	
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file when test is executed with pages
	 * One test at a time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkTestStepHandling() throws Exception {
		executeSubTest(1, "StubTestClassForTestSteps");
		Assert.assertTrue(TestLogging.getTestLogs().get("testPage").contains("Start method testPage"));	
		Assert.assertTrue(TestLogging.getTestLogs().get("testPage").contains("TestLogging: tell me why"));	
	}
	
}
