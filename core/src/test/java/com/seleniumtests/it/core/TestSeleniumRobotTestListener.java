/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
package com.seleniumtests.it.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestSeleniumRobotTestListener extends ReporterTest {

	private static final String DRIVER_BLOCKED_MSG = "Driver creation forbidden before @BeforeMethod and after @AfterMethod execution";
	
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
		
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions"}, "core_3,core_4", "");
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		Assert.assertTrue(mainReportContent.contains(">core_3</a>"));
		Assert.assertTrue(mainReportContent.contains(">core_4</a>"));
		Assert.assertTrue(mainReportContent.contains(">testAndSubActions</a>"));
		
		// all 3 methods are OK
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "<i class=\"fa fa-circle circleSuccess\">"), 3);
	}
	
	/**
	 * Checks that with a data provider, test context does not overlap between test methods and that displayed logs correspond to the method execution and not all method executions
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testContextWithDataProvider(ITestContext testContext) throws Exception {
		
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDataProvider.testMethod"}, "", "");
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		// check that all tests are OK and present into summary file. If test is KO (issue #115), the same context is taken for subsequent test method calls
		Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='testMethod/TestReport.html' .*?>testMethod</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='testMethod-1/TestReport.html' .*?>testMethod-1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='testMethod-2/TestReport.html' .*?>testMethod-2</a>.*"));

		// check each result file to see if it exists and if it only contains information about this method context (log of this method only)
		String detailedReportContent1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMethod", "TestReport.html").toFile());
		detailedReportContent1 = detailedReportContent1.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "data written"), 1);
		Assert.assertTrue(detailedReportContent1.contains("data written: data1"));
		Assert.assertTrue(detailedReportContent1.contains("Test Details - testMethod with params: (data1)"));
		
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMethod-1", "TestReport.html").toFile());
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "data written"), 1);
		Assert.assertTrue(detailedReportContent2.contains("data written: data2"));
		Assert.assertTrue(detailedReportContent2.contains("Test Details - testMethod-1 with params: (data2)"));
		
		String detailedReportContent3 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testMethod-2", "TestReport.html").toFile());
		detailedReportContent3 = detailedReportContent3.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "data written"), 1);
		Assert.assertTrue(detailedReportContent3.contains("data written: data3"));
		Assert.assertTrue(detailedReportContent3.contains("Test Details - testMethod-2 with params: (data3)"));
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
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "class=\"fa fa-circle circleSuccess\">"), 
							StringUtils.countMatches(mainReportContent, "TestReport.html") - 1);
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "TestReport.html"), 9);

		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<i class\\=\"fa fa-circle circleSkipped\"></i><a href\\='test1Listener4/TestReport\\.html'.*?>test1Listener4</a>.*"));
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelClasses(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.CLASSES);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "class=\"fa fa-circle circleSuccess\">"), 
				StringUtils.countMatches(mainReportContent, "TestReport.html") - 1);
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "TestReport.html"), 9);

		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<i class\\=\"fa fa-circle circleSkipped\"></i><a href\\='test1Listener4/TestReport\\.html'.*?>test1Listener4</a>.*"));
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelMethods(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.METHODS);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "class=\"fa fa-circle circleSuccess\">"), 
				StringUtils.countMatches(mainReportContent, "TestReport.html") - 1);
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "TestReport.html"), 9);
		
		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<i class\\=\"fa fa-circle circleSkipped\"></i><a href\\='test1Listener4/TestReport\\.html'.*?>test1Listener4</a>.*"));
	}
	
	@Test(groups={"it"})
	public void testContextDriverBlockingBeforeSuite(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "beforeSuite");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
											"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains(DRIVER_BLOCKED_MSG));
		Assert.assertFalse(logs.contains("start suite"));
		Assert.assertFalse(logs.contains("start test"));
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}
	
	@Test(groups={"it"})
	public void testContextDriverBlockingBeforeTest(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "beforeTest");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertTrue(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));
		
		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertFalse(logs.contains("start test"));
		

		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}
	
	@Test(groups={"it"})
	public void testContextDriverBlockingBeforeClass(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "beforeClass");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertTrue(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertFalse(logs.contains("start class"));
		
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}
	
	@Test(groups={"it"})
	public void testContextDriverBlockingBeforeMethod(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "beforeMethod");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertTrue(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertTrue(logs.contains("start class"));
		Assert.assertFalse(logs.contains("start method"));
		
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}
	
	@Test(groups={"it"})
	public void testContextDriverNotBlockingInTest(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "test");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertFalse(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("Finished creating *htmlunit driver"));
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertTrue(logs.contains("start class"));
		Assert.assertTrue(logs.contains("start method"));
		Assert.assertTrue(logs.contains("test 1"));
		Assert.assertTrue(logs.contains("test 2"));
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}
	
	@Test(groups={"it"})
	public void testContextDriverNotBlockingInAfterMethod(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "afterMethod");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertFalse(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("Finished creating *htmlunit driver"));
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertTrue(logs.contains("start class"));
		Assert.assertTrue(logs.contains("start method"));
		Assert.assertTrue(logs.contains("test 1"));
		Assert.assertTrue(logs.contains("end method"));
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}

	@Test(groups={"it"})
	public void testContextDriverBlockingAfterClass(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "afterClass");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertTrue(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertTrue(logs.contains("start class"));
		Assert.assertTrue(logs.contains("start method"));
		Assert.assertTrue(logs.contains("test 1"));
		Assert.assertTrue(logs.contains("end method"));
		Assert.assertFalse(logs.contains("end class"));
		
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}
	
	@Test(groups={"it"})
	public void testContextDriverBlockingAfterTest(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "afterTest");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertTrue(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertTrue(logs.contains("start class"));
		Assert.assertTrue(logs.contains("start method"));
		Assert.assertTrue(logs.contains("test 1"));
		Assert.assertTrue(logs.contains("end method"));
		Assert.assertTrue(logs.contains("end class"));
		Assert.assertFalse(logs.contains("end test"));
		
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}
}
