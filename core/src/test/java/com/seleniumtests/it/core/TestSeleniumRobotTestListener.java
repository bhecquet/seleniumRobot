package com.seleniumtests.it.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class TestSeleniumRobotTestListener extends GenericTest {

	private TestNG executeSubTest(int threadCount, String[] testMethods, String cucumberTests) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.FALSE);
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
		
		return tng;
	}
	
	/**
	 * Test that 2 tests (1 cucumber and 1 TestNG) are correctly executed in parallel
	 * - result is OK
	 * - test names are OK
	 * Check is done indirectly from the report files because there seems to be no way to check listener state
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultiThreadTests(ITestContext testContext) throws Exception {
		
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions"}, "core_3,core_4");
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		Assert.assertTrue(mainReportContent.contains(".html'>core_3</a>"));
		Assert.assertTrue(mainReportContent.contains(".html'>core_4</a>"));
		Assert.assertTrue(mainReportContent.contains(".html'>testAndSubActions</a>"));
		
		// all 3 methods are OK
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "<i class=\"fa fa-circle circleSuccess\">"), 3);
	}
	
	private TestNG executeSubTest2(XmlSuite.ParallelMode parallelMode) throws IOException {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(parallelMode);
		suite.setFileName("/home/test/seleniumRobot/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put("softAssertEnabled", "false");
		suite.setParameters(suiteParameters);
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
	
		suite.setThreadCount(5);
		suite.setParallel(parallelMode);
		
		XmlClass xmlClass1 = new XmlClass("com.seleniumtests.it.stubclasses.StubTestClassForListener1");
		XmlClass xmlClass2 = new XmlClass("com.seleniumtests.it.stubclasses.StubTestClassForListener2");
		XmlClass xmlClass3 = new XmlClass("com.seleniumtests.it.stubclasses.StubTestClassForListener3");
		XmlClass xmlClass4 = new XmlClass("com.seleniumtests.it.stubclasses.StubTestClassForListener4");
		
		// TestNG test1: 2 classes
		XmlTest test1 = new XmlTest(suite);
		test1.setName("test1");
		test1.addParameter(SeleniumTestsContext.BROWSER, "none");
	
		List<XmlClass> classes1 = new ArrayList<XmlClass>();
		classes1.add(xmlClass1);
		classes1.add(xmlClass2);
		test1.setXmlClasses(classes1) ;
		
		// TestNG test2: 1 class
		XmlTest test2 = new XmlTest(suite);
		test2.setName("test2");
		test2.addParameter(SeleniumTestsContext.BROWSER, "none");
		List<XmlClass> classes2 = new ArrayList<XmlClass>();
		classes2.add(xmlClass1);
		classes2.add(xmlClass3);
		classes2.add(xmlClass4);
		test2.setXmlClasses(classes2) ;
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return tng;
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelTests(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.TESTS);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport"), 
							StringUtils.countMatches(mainReportContent, "></i><a href='SeleniumTestReport-") - 1);

		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<i class\\=\"fa fa-circle circleSkipped\"></i><a href\\='SeleniumTestReport-\\d+\\.html'>test1Listener4</a>.*"));
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelClasses(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.CLASSES);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport"), 
				StringUtils.countMatches(mainReportContent, "></i><a href='SeleniumTestReport-") - 1);

		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<i class\\=\"fa fa-circle circleSkipped\"></i><a href\\='SeleniumTestReport-\\d+\\.html'>test1Listener4</a>.*"));
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelMethods(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.METHODS);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "class=\"fa fa-circle circleSuccess\"></i><a href='SeleniumTestReport"), 
				StringUtils.countMatches(mainReportContent, "></i><a href='SeleniumTestReport-") - 1);
		
		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<i class\\=\"fa fa-circle circleSkipped\"></i><a href\\='SeleniumTestReport-\\d+\\.html'>test1Listener4</a>.*"));
	}
	
	/* TODO:
	 * - 2 tests classes with 2 tests each, with @BeforeClass, @BeforeTest, @BeforeMethod, @BeforeGroup
	 * 	- test all parallel modes available
	 *  - check with logs, for example, that something added in test context (before) is kept in test and available after test
	 */
}
