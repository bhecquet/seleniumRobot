package com.seleniumtests.it.reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class ReporterTest extends MockitoTest {

	protected TestNG executeSubTest(String[] testClasses) throws IOException {
		return executeSubTest(1, testClasses);
	}
	
	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 * @throws IOException 
	 */
	protected TestNG executeSubTest(int threadCount, String[] testClasses) throws IOException {
//		TestListener testListener = new TestListener();
		
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.FALSE);
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.METHODS);
		}
		
		for (String testClass: testClasses) {
			XmlTest test = new XmlTest(suite);
			test.setName(testClass.substring(testClass.lastIndexOf(".") + 1));
			test.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes = new ArrayList<XmlClass>();
			classes.add(new XmlClass(testClass));
			test.setXmlClasses(classes) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
//		tng.addListener((IReporter)reporter);
//		tng.addListener((ITestListener)testListener);
//		tng.addListener((IInvokedMethodListener)testListener);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
//		SeleniumRobotLogger.parseLogFile();
		
		return tng;
	}
	
	/**
	 * 
	 * @param cucumberTests 	cucumber test param as it would be passed in XML file
	 * @return
	 * @throws IOException 
	 */
	protected XmlSuite executeSubCucumberTests(String cucumberTests) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("cucumberPackage", "com.seleniumtests");
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		XmlTest test = new XmlTest(suite);
		test.setName("cucumberTest");
		XmlPackage xmlPackage = new XmlPackage("com.seleniumtests.core.runner.*");
		test.setXmlPackages(Arrays.asList(xmlPackage));
		Map<String, String> parameters = new HashMap<>();
		parameters.put("cucumberTests", cucumberTests);
		parameters.put("cucumberTagss", "");
		test.setParameters(parameters);
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
}
