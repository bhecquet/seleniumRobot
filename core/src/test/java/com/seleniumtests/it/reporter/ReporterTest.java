package com.seleniumtests.it.reporter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.TestLogging;

public class ReporterTest extends MockitoTest {
	

	@BeforeMethod(groups={"it"})
	public void setLogs(Method method, ITestContext context) throws IOException {
		TestLogging.reset();
		SeleniumTestsContext.resetOutputFolderNames();
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()));
	}
	
	protected TestNG executeSubTest(String[] testClasses) throws IOException {
		return executeSubTest(1, testClasses);
	}
	
	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 * @throws IOException 
	 */
	protected TestNG executeSubTest(int threadCount, String[] testClasses) throws IOException {
		return executeSubTest(threadCount, testClasses, XmlSuite.ParallelMode.METHODS, new String[] {});
	}
	
	/**
	 * Execute SeleniumTestPlan tests
	 * @param threadCount
	 * @param testClasses
	 * @param parallelMode
	 * @param methods
	 * @return
	 * @throws IOException
	 */
	protected TestNG executeSubTest(int threadCount, String[] testClasses, XmlSuite.ParallelMode parallelMode, String[] methods) throws IOException {
//		TestListener testListener = new TestListener();
		
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
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
			test.setName(String.format("%s_%d", testClass.substring(testClass.lastIndexOf(".") + 1), new Random().nextInt()));
			test.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes = new ArrayList<XmlClass>();
			XmlClass xmlClass = new XmlClass(testClass);
			if (methods.length > 0) {
				List<XmlInclude> includes = new ArrayList<>();
				for (String method: methods) {
					includes.add(new XmlInclude(method));
				}
				xmlClass.setIncludedMethods(includes);
			}
			classes.add(xmlClass);
			test.setXmlClasses(classes) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return tng;
	}
	
	/**
	 * Execute SeleniumTestPlan and cucumber tests
	 * Each test method is put in its own TestNG test
	 * @param threadCount
	 * @param testMethods
	 * @param cucumberTests
	 * @return
	 * @throws IOException
	 */
	protected TestNG executeSubTest(int threadCount, String[] testMethods, String cucumberTests, String group) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("softAssertEnabled", "false");
		suiteParameters.put("cucumberPackage", "com.seleniumtests");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		
		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.TESTS);
		}
		
		// TestNG tests
		for (String testMethod: testMethods) {
			String className = testMethod.substring(0, testMethod.lastIndexOf("."));
			String methodName = testMethod.substring(testMethod.lastIndexOf(".") + 1);

			XmlTest test = new XmlTest(suite);
			test.setName(String.format("%s_%d", methodName, new Random().nextInt()));
			
			if (group != null) {
				test.addIncludedGroup(group);
			}
			
			test.addParameter(SeleniumTestsContext.BROWSER, "none");
			List<XmlClass> classes = new ArrayList<XmlClass>();
			XmlClass xmlClass = new XmlClass(className);
			
			List<XmlInclude> include = new ArrayList<>();
			include.add(new XmlInclude(methodName));
			xmlClass.setIncludedMethods(include);
			classes.add(xmlClass);
			test.setXmlClasses(classes) ;
		}	
		
		// cucumber tests
		if (!cucumberTests.isEmpty()) {
			XmlTest test = new XmlTest(suite);
			test.setName(String.format("cucumberTest_%d", new Random().nextInt()));
			XmlPackage xmlPackage = new XmlPackage("com.seleniumtests.core.runner.*");
			test.setXmlPackages(Arrays.asList(xmlPackage));
			Map<String, String> parameters = new HashMap<>();
			parameters.put("cucumberTests", cucumberTests);
			parameters.put("cucumberTags", "");
			test.setParameters(parameters);
		}
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return tng;
	}
	
	/**
	 * 
	 * @param cucumberTests 	cucumber test param as it would be passed in XML file
	 * @return
	 * @throws IOException 
	 */
	protected XmlSuite executeSubCucumberTests(String cucumberTests, int threadCount) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("cucumberPackage", "com.seleniumtests");
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		

		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.METHODS);
		}
		
		XmlTest test = new XmlTest(suite);
		test.setName(String.format("cucumberTest_%d", new Random().nextInt()));
		XmlPackage xmlPackage = new XmlPackage("com.seleniumtests.core.runner.*");
		test.setXmlPackages(Arrays.asList(xmlPackage));
		Map<String, String> parameters = new HashMap<>();
		parameters.put("cucumberTests", cucumberTests);
		parameters.put("cucumberTags", "");
		test.setParameters(parameters);
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
	
	/**
	 * Reads the TestReport.html file for the given test name
	 * line breaks are removed
	 * @param testName
	 * @return
	 * @throws IOException 
	 */
	protected String readTestMethodResultFile(String testName) throws IOException {
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), testName, "TestReport.html").toFile());
		return detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
	}
	
	protected String readSeleniumRobotLogFile() throws IOException {
		String logContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "seleniumRobot.log").toFile());
		return logContent.replace("\n", "").replace("\r",  "");
	}
}
