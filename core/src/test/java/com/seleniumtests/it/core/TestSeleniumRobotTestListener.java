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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.it.stubclasses.StubTestClassForDriverParallelTest;

@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumGridConnectorFactory.class, SeleniumTestsContext.class, WebUIDriver.class})
public class TestSeleniumRobotTestListener extends ReporterTest {

	private static final String DRIVER_BLOCKED_MSG = "Driver creation forbidden before @BeforeMethod and after @AfterMethod execution";
	
	@Mock
	private SeleniumRobotVariableServerConnector variableServer;
	
	@Mock
	private SeleniumGridConnector gridConnector;
	
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
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"), StandardCharsets.UTF_8);
		Assert.assertTrue(mainReportContent.contains(">core_3</a>"));
		Assert.assertTrue(mainReportContent.contains(">core_4</a>"));
		Assert.assertTrue(mainReportContent.contains(">testAndSubActions</a>"));
		
		// all 3 methods are OK
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "info=\"ok\""), 3);
	}
	
	/**
	 * Test the case where several browsers access the same HtmlElement at the same time (as HtmlElements are almost always declared static)
	 * Be sure each search in a thread adress the driver of this thread
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultiThreadTests2(ITestContext testContext) throws Exception {
		
		executeSubTest(StubTestClassForDriverParallelTest.maxThreads, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverParallelTest"}, ParallelMode.METHODS, new String[] {});
		String logs = readSeleniumRobotLogFile();
		
		Assert.assertEquals(StringUtils.countMatches(logs, "Test is OK"), 4);
		
	}
	
	/**
	 * issue #254: Check we get variable for each test execution (and each retry)
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testWithRetry(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "2");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), anyString(), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testFailedWithException"});
			
			// check get variables has been called once for each retry
			verify(variableServer, times(3)).getVariables(0, -1);
			verify(variableServer, times(3)).unreserveVariables(anyList());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
	}
	
	/**
	 * Check that logs of a failed attempt are kept in the result directory (KEEP_ALL_RESULTS=true)
	 */
	@Test(groups={"it"})
	public void testKeepAllResults(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// check that zip file is created of first execution, but not for the second one
			File resultZip = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "retry-testDriverShortKo-1.zip").toFile();
			Assert.assertTrue(resultZip.exists());
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "retry-testDriverShortKo-2.zip").toFile().exists());
			
			List<String> entries = new ArrayList<>();
	
			ZipUtil.iterate(resultZip, new ZipEntryCallback() {
		      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
		        String entryName = zipEntry.getName();
		        entries.add(entryName.split("\\(file")[0]);
		        
		      }
		    });
			
			// check the content of the zip file
			Assert.assertTrue(entries.contains("testDriverShortKo/TestReport.html"));
			Assert.assertTrue(entries.contains("testDriverShortKo/videoCapture.avi"));
			Assert.assertTrue(entries.contains("testDriverShortKo/resources/app.min.js"));
			Assert.assertTrue(entries.contains("testDriverShortKo/resources/seleniumRobot_solo.css"));
			Assert.assertTrue(entries.contains("testDriverShortKo/screenshots/testDriverShortKo_3-1_openPage_with_args._"));
			Assert.assertTrue(entries.contains("testDriverShortKo/htmls/testDriverShortKo_3-1_openPage_with_args._"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	
	/**
	 * Check that logs of a failed attempt are not kept in the result directory (KEEP_ALL_RESULTS=false)
	 */
	@Test(groups={"it"})
	public void testDoNotKeepAllResults(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			System.setProperty(SeleniumTestsContext.KEEP_ALL_RESULTS, "false");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			// check that zip file is created of first execution, but not for the second one
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "retry-testDriverShortKo-1.zip").toFile().exists());
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "retry-testDriverShortKo-2.zip").toFile().exists());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.KEEP_ALL_RESULTS);
		}
	}
	
	/**
	 * Check variables are get only once when testing
	 * issue #255: also check that seleniumRobot server is called with the right test name
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testWithoutRetry(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withArguments(eq(true), eq("http://localhost:1234"), eq("testFailedWithException"), eq(null)).thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testFailedWithException"});
			
			// check get variables has been called once for each retry
			verify(variableServer).getVariables(0, -1);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
	}
	

	/**
	 * issue #287: check that test is retried even if a configuration error occurs
	 * Also check test is KO and not skipped (won't be possible since: https://github.com/cbeust/testng/issues/2148)
	 * , because error occured in AfterMethod
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetriedWithConfigurationErrorAndTestFailure() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForConfigurationError2"}, ParallelMode.NONE, new String[] {"testInErrorWithAfterMethodError"});
			
			String mainReportContent = readSummaryFile();
			
			// check that test is marked as KO because it executed (https://github.com/cbeust/testng/issues/2148)
			Assert.assertTrue(mainReportContent.matches(".*<a href='testInErrorWithAfterMethodError/TestReport.html' info=\"ko\" .*?>testInErrorWithAfterMethodError</a>.*"));
			
			// check test is retried
			String logs = readSeleniumRobotLogFile();
			Assert.assertEquals(StringUtils.countMatches(logs, "Start method testInErrorWithAfterMethodError"), 2);
			Assert.assertEquals(StringUtils.countMatches(logs, "info before error"), 2);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
		 

	}
	
	/**
	 * Checks that with a data provider, test context does not overlap between test methods and that displayed logs correspond to the method execution and not all method executions
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testContextWithDataProvider(ITestContext testContext) throws Exception {
		
		executeSubTest(5, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDataProvider.testMethod"}, "", "");

		String mainReportContent = readSummaryFile();
		
		// check that all tests are OK and present into summary file. If test is KO (issue #115), the same context is taken for subsequent test method calls
		Assert.assertTrue(mainReportContent.matches(".*<a href='testMethod/TestReport.html' info=\"ok\" .*?>testMethod</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testMethod-1/TestReport.html' info=\"ok\" .*?>testMethod-1</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href='testMethod-2/TestReport.html' info=\"ok\" .*?>testMethod-2</a>.*"));

		// check each result file to see if it exists and if it only contains information about this method context (log of this method only)
		String detailedReportContent1 = readTestMethodResultFile("testMethod");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent1, "data written"), 1);
		Assert.assertTrue(detailedReportContent1.contains("data written: data1"));
		Assert.assertTrue(detailedReportContent1.contains("Test Details - testMethod with params: (data1)"));
		
		String detailedReportContent2 = readTestMethodResultFile("testMethod-1");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "data written"), 1);
		Assert.assertTrue(detailedReportContent2.contains("data written: data2"));
		Assert.assertTrue(detailedReportContent2.contains("Test Details - testMethod-1 with params: (data2)"));
		
		String detailedReportContent3 = readTestMethodResultFile("testMethod-2");
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
		
		String mainReportContent = readSummaryFile();
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "info=\"ok\""), 
							StringUtils.countMatches(mainReportContent, "TestReport.html") - 1);
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "TestReport.html"), 9);

		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test1Listener4/TestReport\\.html' info=\"skipped\".*?>test1Listener4</a>.*"));

		// issue #312: check that result files have been generated at least twice (one during test run and one at the end)
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener2/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1-2/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener3/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener3-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener4/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener1-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "SeleniumTestReport.html"), 10); // 1 per executed test + 1 for final generation
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelClasses(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.CLASSES);
		
		String mainReportContent = readSummaryFile();
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "info=\"ok\""), 
				StringUtils.countMatches(mainReportContent, "TestReport.html") - 1);
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "TestReport.html"), 9);

		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test1Listener4/TestReport\\.html' info=\"skipped\".*?>test1Listener4</a>.*"));
		
		// issue #312: check that result files have been generated once
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener2/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1-2/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener3/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener3-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener4/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener1-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "SeleniumTestReport.html"), 10); // 1 per executed test + 1 for final generation
	}
	
	@Test(groups={"it"})
	public void testContextStorageParallelMethods(ITestContext testContext) throws Exception {
		
		executeSubTest2(ParallelMode.METHODS);
		
		String mainReportContent = readSummaryFile();
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "info=\"ok\""), 
				StringUtils.countMatches(mainReportContent, "TestReport.html") - 1);
		Assert.assertEquals(StringUtils.countMatches(mainReportContent, "TestReport.html"), 9);
		
		// test1Listener4 fails as expected
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='test1Listener4/TestReport\\.html' info=\"skipped\".*?>test1Listener4</a>.*"));

		// issue #312: check that result files have been generated at least twice (one during test run and one at the end)
		String logs = readSeleniumRobotLogFile().replace("\\", "/");
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener2/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test2Listener1-2/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener3/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener3-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener4/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "test1Listener1-1/PERF-result.xml"), 1);
		Assert.assertEquals(StringUtils.countMatches(logs, "SeleniumTestReport.html"), 10); // 1 per executed test + 1 for final generation
	}
	
	/**
	 * Test we cannot create a driver in '@BeforeSuite' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}
	
	/**
	 * Test we cannot create a driver in '@BeforeTest' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}

	/**
	 * Test we cannot create a driver in '@BeforeClass' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
	}
	

	/**
	 * Test we can create a driver in '@BeforeMethod' methods
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testContextDriverNotBlockingBeforeMethod(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "htmlunit");
			System.setProperty("startLocation", "beforeMethod");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5",
			"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test2Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String detailedReportContent1 = readTestMethodResultFile("test1Listener5");
		Assert.assertFalse(detailedReportContent1.contains(DRIVER_BLOCKED_MSG));

		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("start suite"));
		Assert.assertTrue(logs.contains("start test"));
		Assert.assertTrue(logs.contains("start class"));
		Assert.assertTrue(logs.contains("start method"));
		
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should not be skipped
		Assert.assertEquals(jsonResult.getInt("skip"), 0);
	}
	

	/**
	 * Test we can create a driver in '@Test' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}

	/**
	 * Test we can create a driver in '@AfterMethod' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}

	/**
	 * Test we cannot create a driver in '@AfterClass' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}

	/**
	 * Test we cannot create a driver in '@AfterTest' methods
	 * @param testContext
	 * @throws Exception
	 */
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
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// All tests should be skipped because configuration method is skipped
		Assert.assertEquals(jsonResult.getInt("pass"), 2);
	}
	
	/**
	 * issue #289: Check we increase retry count when SO_TIMEOUT is raised in non-local mode
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetriedWithSocketTimeoutError() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/wd/hub");
			
			PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
			PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/wd/hub"))).thenReturn(Arrays.asList(gridConnector));
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[] {"testWithSocketTimeoutOnFirstExec"});
			
			String logs = readSeleniumRobotLogFile();
			
			// check test is retried
			Assert.assertEquals(StringUtils.countMatches(logs, "Start method testWithSocketTimeoutOnFirstExec"), 2);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	/**
	 * issue #289: Check do not increase retry count in case error in not a WebDriverException
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNotRetriedWithAnyError() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/wd/hub");
			
			PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
			PowerMockito.when(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://localhost:4444/wd/hub"))).thenReturn(Arrays.asList(gridConnector));
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[] {"testWithExceptionOnFirstExec"});
			
			String logs = readSeleniumRobotLogFile();
			
			// check test is not retried because exception do not match
			Assert.assertEquals(StringUtils.countMatches(logs, "Start method testWithExceptionOnFirstExec"), 1);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * issue #289: Check we allow a retry when SO_TIMEOUT is raised in local mode because we only want to avoid problem of communication in grid mode
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNotRetriedWithSocketTimeoutErrorInLocalMode() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[] {"testWithSocketTimeoutOnFirstExec"});
			
			String logs = readSeleniumRobotLogFile();
			
			// check test is not retried in local mode
			Assert.assertEquals(StringUtils.countMatches(logs, "Start method testWithSocketTimeoutOnFirstExec"), 1);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	/**
	 * issue #297: be sure we reset the driver name before the test starts
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDriverNameResetAtStart(ITestContext testContext) throws Exception {

		PowerMockito.mockStatic(WebUIDriver.class);
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[] {"testAndSubActions"});

		PowerMockito.verifyStatic(WebUIDriver.class, times(1));
		WebUIDriver.resetCurrentWebUiDriverName();
		
	}
	
	/**
	 * issue #389: check that error raised on startup is the configurationException, and not ScenarioException  "ScenarioException: When using @BeforeMethod / @AfterMethod in tests, this method MUST have a 'java.lang.reflect.Method' "
	 * This issue can only happen if ConfigFailurePolicy is set to "skip" which is the default behaviour
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testConfigurationExceptionIsRendered() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForIssue389"});
		
		String logs = readSeleniumRobotLogFile();
		Assert.assertFalse(logs.contains("When using @BeforeMethod / @AfterMethod in tests")); // check error message is shown when parameter is not given to Before / AfterMethod
	}
	
	/**
	 * Check we get an error message when configuration method do not have a java.lang.reflect.Method as their first parameter
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorRaisedIfConfigurationMethodHasNotMethodReference() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForIssue382e2"});
		
		String logs = readSeleniumRobotLogFile();
		Assert.assertTrue(logs.contains("When using @BeforeMethod / @AfterMethod in tests")); // check error message is shown when parameter is not given to Before / AfterMethod
	}
	
	/**
	 * issue #414: when we capture the last step whereas we have entered a frame, we should capture the whole browser, not only the frame
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCaptureTakenOnLastStep(ITestContext testContext) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.REPLAY_TIME_OUT, "1");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.NONE, new String[] {"testDriverWithFailureAfterSwitchToFrame"});
			
			// check image dimensions are high enough to know if all the page has been captured
			File[] images = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithFailureAfterSwitchToFrame", "screenshots").toFile().listFiles();
			Assert.assertEquals(images.length, 2);

			BufferedImage image = ImageIO.read(images[1]);
			Assert.assertTrue(image.getHeight() > 2500);
			Assert.assertTrue(image.getWidth() > 700);
			
			
		} finally {

			System.clearProperty(SeleniumTestsContext.REPLAY_TIME_OUT);
		}
	}
	
	/**
	 * Check increaseMaxRetry is performed when called from test method
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIncreaseMaxRetryInTest(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener6.testIncreaseMaxRetryInTestMethod"}, "", "stub1");

		String logs = readSeleniumRobotLogFile();
		
		// assert 5 execution (nominal + 4 retries) has been performed (2* default retry count (2))
		Assert.assertEquals(StringUtils.countMatches(logs, "SeleniumRobotTestListener: Start method testIncreaseMaxRetryInTestMethod"), 5);
		
	}
	
	/**
	 * Check we cannot increase max retry outside a test method
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIncreaseMaxRetryInAfterConfig(ITestContext testContext) throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener6.testIncreaseMaxRetryInAfterTestMethod"}, "", "stub1");
		
		String logs = readSeleniumRobotLogFile();
		
		// assert 5 execution (nominal + 4 retries) has been performed (2* default retry count (2))
		Assert.assertEquals(StringUtils.countMatches(logs, "SeleniumRobotTestListener: Start method testIncreaseMaxRetryInAfterTestMethod"), 3);
		
		// check "increaseMaxRetry" cannot be called outside test method
		Assert.assertTrue(logs.contains("SeleniumRobotTestListener: RetryAnalyzer is null, 'increaseMaxRetry' can be called only inside test methods"));
	}
	

	/**
	 * Check that when snapshot server is used for comparison, if comparison fails, test is retried
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoChangeTestResultAndRetried() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			// say that snapshot comparison if failed when checking individual snapshots
			createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 1.0, 'tooManyDiffs': true}");
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			// check test has been retried
			String logs = readSeleniumRobotLogFile();
			Assert.assertTrue(logs.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions FAILED, Retrying 1 time"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used for comparison, if comparison fails, test is not retried as we are in "displayOnly"
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoChangeTestResultNotRetried() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "displayOnly");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			// say that snapshot comparison if failed when checking individual snapshots
			createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 1.0, 'tooManyDiffs': true}");
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check test has NOT been retried
			String logs = readSeleniumRobotLogFile();
			Assert.assertFalse(logs.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions FAILED, Retrying 1 time"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

}
