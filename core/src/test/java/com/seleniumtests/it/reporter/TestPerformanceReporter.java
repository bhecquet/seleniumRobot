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
package com.seleniumtests.it.reporter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.contexts.SeleniumRobotServerContext;
import com.seleniumtests.util.osutility.OSUtility;

/**
 * Test that default reporting contains an XML file per test (CustomReporter.java) with default test reports defined in SeleniumTestsContext.DEFAULT_CUSTOM_TEST_REPORTS
 * /!\ this could also test presence and content of detailed-result.json, but it's only a matter of formatting
 */
public class TestPerformanceReporter extends ReporterTest {

	@BeforeMethod(groups={"it"})
	private void deleteGeneratedFiles() {
		FileUtils.deleteQuietly(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()));
	}
	
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});

		// check all files are generated with the right name
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "PERF-result.xml").toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "PERF-result.xml").toFile().exists());
	}


	/**
	 * Check if param gridnode is ok in xml report
	 */
	@Test(groups={"it"})
	public void testReportGenerationGrid(ITestContext testContext) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4321/wd/hub");
	
			createGridHubMockWithNodeOK();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			String jmeterReport = readTestMethodPerfFile("testDriverShort");
	
			// check file is generated with gridnode param
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShort", "PERF-result.xml").toFile().exists());
			Assert.assertTrue(jmeterReport.contains("gridnode=\"localhost\""));
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Check if param gridnode is correctly handled when test has not been executed
	 */
	@Test(groups={"it"})
	public void testReportGenerationGridTestSkipped(ITestContext testContext) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4321/wd/hub");
			
			createGridHubMockWithNodeOK();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo", "testDriverShortSkipped"});
			String jmeterReport = readTestMethodPerfFile("testDriverShortSkipped");
			
			// check file is generated with gridnode param
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortSkipped", "PERF-result.xml").toFile().exists());
			Assert.assertTrue(jmeterReport.contains("gridnode=\"N/A\"")); // test skipped, grid not not available
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}

	@Test(groups={"it"})
	public void testReportGenerationLocal(ITestContext testContext) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
	
			createGridHubMockWithNodeOK();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			String jmeterReport = readTestMethodPerfFile("testDriverShort");
	
			// check file is generated with gridnode param
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShort", "PERF-result.xml").toFile().exists());
			Assert.assertTrue(jmeterReport.contains("gridnode=\"LOCAL\""));
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}

	/**
	 * Check all steps of test case are available
	 */
	@Test(groups={"it"})
	public void testReportWithSteps(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithException"});
		
		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testWithException");
		
		Assert.assertTrue(jmeterReport.contains("hostname=\"\" name=\"testWithException\" tests=\"6\" time=\""));
		Assert.assertTrue(jmeterReport.contains("browser=\"NONE\""));
		Assert.assertTrue(jmeterReport.contains("appVersion=\"" + SeleniumTestsContextManager.getApplicationVersion()));
		Assert.assertTrue(jmeterReport.contains("coreVersion=\"" + SeleniumTestsContextManager.getCoreVersion()));
		Assert.assertTrue(jmeterReport.contains("retries=\"2\"")); 
		Assert.assertTrue(jmeterReport.contains("mobileApp=\"\""));
		Assert.assertTrue(jmeterReport.contains("device=\"\"")); 
		Assert.assertTrue(jmeterReport.contains("platform=\"" + OSUtility.getCurrentPlatorm().toString()));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 4: step 1\" time=\""));
		
		// check report contains configuration steps and not internal configuration steps (call to configure() method should not be the first step)
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 3: Pre test step: set\" time="));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 5: Test end\" time="));
		Assert.assertTrue(jmeterReport.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 6: Post test step: reset\" time="));
	}
	
	@Test(groups={"it"})
	public void testReportWithCustomTestNames(ITestContext testContext) throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testOkWithTestName", "testOkWithTestNameAndDataProvider"});
		
		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testOkWithTestName");
		Assert.assertTrue(jmeterReport.contains("hostname=\"\" name=\"A test which is &lt;OK&gt; é&amp;\" tests=\"6\" time=\""));
		String jmeterReport1 = readTestMethodPerfFile("testOkWithTestNameAndDataProvider");
		Assert.assertTrue(jmeterReport1.contains("hostname=\"\" name=\"A test which is OK (data2, data3)\" tests=\"5\" time=\""));

	}
	
	/**
	 * Check number of retries is correctly logged
	 * This test is failed in "test" part, not in steps. Check it's correctly logged as failed
	 */
	@Test(groups={"it"})
	public void testReportWithRetry(ITestContext testContext) throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithException"});
		
		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testWithException");

		// issue #418: now it's Test end which is the failed step because in this case execption is raised directly in test scenario
		Assert.assertTrue(jmeterReport.contains("retries=\"2\"")); 
		Assert.assertTrue(jmeterReport.contains("errors=\"1\"")); // be sure an error is reported
		Assert.assertTrue(jmeterReport.contains("failures=\"0\""));
		Assert.assertTrue(jmeterReport.contains("failedStep=\"\"")); // failed step is not logged as there are no step logged
	}
	
	/**
	 * Check all steps of test case are available
	 */
	@Test(groups={"it"})
	public void testWithStepOkAndStepInError(ITestContext testContext) throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testOkWithOneStepFailed"});
		
		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testOkWithOneStepFailed");
		
		Assert.assertTrue(jmeterReport.contains("<testsuite gridnode=\"LOCAL\" errors=\"0\" failures=\"0\" hostname=\"\" name=\"testOkWithOneStepFailed\" tests=\"6\""));
		Assert.assertTrue(jmeterReport.contains("name=\"Step 1: Pre test step: setCount\""));
		Assert.assertTrue(jmeterReport.contains("name=\"Step 2: Pre test step: slow\""));
		Assert.assertTrue(jmeterReport.contains("name=\"Step 3: Pre test step: set\""));
		Assert.assertTrue(jmeterReport.contains("name=\"Step 4: step 1\""));
		Assert.assertTrue(jmeterReport.contains("name=\"Step 5: Test end\""));
		Assert.assertTrue(jmeterReport.contains("name=\"Step 6: Post test step: reset\""));
	}
	
	/**
	 * Check that when a step contains an exception and is failed, this one is written in file
	 */
	@Test(groups={"it"})
	public void testErrorWithException(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testFailedWithException"});
		
		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testFailedWithException");
		
		Assert.assertTrue(jmeterReport.contains("<error message=\"class com.seleniumtests.customexception.DriverExceptions: fail\""));
		Assert.assertTrue(jmeterReport.contains("<![CDATA[class com.seleniumtests.customexception.DriverExceptions: fail"));
		Assert.assertTrue(jmeterReport.contains("at com.seleniumtests.it.core.aspects.CalcPage.failAction_aroundBody"));

		// check the step marked as failed is the last failed step (not 'Test end')
		Assert.assertTrue(jmeterReport.contains("failedStep=\"failAction\""));
		Assert.assertTrue(jmeterReport.contains("errors=\"1\"")); // only one error
	}	

	/**
	 * Chack that if several custom reports are specified through custom reports, they are all available
	 */
	@Test(groups={"it"})
	public void testMultipleReportsWithSteps(ITestContext testContext) throws Exception {

		try {
			System.setProperty("customTestReports", "PERF::xml::reporter/templates/report.perf.vm,PERF2::json::ti/report.test.vm");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			String jmeterReport1 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF-result.xml").toFile(), StandardCharsets.UTF_8);
			String jmeterReport2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "PERF2-result.json").toFile(), StandardCharsets.UTF_8);
			
			Assert.assertTrue(jmeterReport1.contains("<testsuite gridnode=\"LOCAL\" errors=\"0\" failures=\"0\" hostname=\"\" name=\"testAndSubActions\" tests=\"7\" time=\""));
			Assert.assertTrue(jmeterReport2.contains("\"suiteName\": \"testAndSubActions\""));
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	/**
	 * Test that performance reporter is correctly encoded
	 */
	@Test(groups={"it"})
	public void testXmlCharacterEscape(ITestContext testContext) throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		String detailedReportContent = readTestMethodPerfFile("testAndSubActions");
		
		// check step 1 has been encoded
		Assert.assertTrue(detailedReportContent.contains("name=\"Step 2: step 1 &lt;&gt;&quot;&apos;&amp;/\""));
	}
	
	/**
	 * issue #205
	 * Test that performance reporter correctly encode error messages
	 */
	@Test(groups={"it"})
	public void testXmlErrorMessageEscape(ITestContext testContext) throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"}, ParallelMode.METHODS, new String[] {"testWithException"});

		String detailedReportContent = readTestMethodPerfFile("testWithException");
		
		// check error message is correctly XML encoded
		Assert.assertTrue(detailedReportContent.contains("<error message=\"class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href=&apos;http://someurl/link&apos; style=&apos;background-color: red;&apos;&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;\" type=\"\">"));
		
		// check CDATA error text is not encoded
		Assert.assertTrue(detailedReportContent.contains("<![CDATA[class com.seleniumtests.customexception.DriverExceptions: & some exception \"with \" <strong><a href='http://someurl/link' style='background-color: red;'>HTML to encode</a></strong>"));
	}
	
	/**
	 * Test all exceptions are there. The root one is only present in text
	 */
	@Test(groups={"it"})
	public void testXmlErrorMessageEscapeDoubleException(ITestContext testContext) throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});

		String detailedReportContent = readTestMethodPerfFile("testWithChainedException");
		
		// check error message is correctly XML encoded
		Assert.assertTrue(detailedReportContent.contains("<error message=\"class com.seleniumtests.customexception.DriverExceptions: &amp; some exception &quot;with &quot; &lt;strong&gt;&lt;a href=&apos;http://someurl/link&apos; style=&apos;background-color: red;&apos;&gt;HTML to encode&lt;/a&gt;&lt;/strong&gt;\" type=\"\">"));
		
		// check CDATA error text is not encoded
		Assert.assertTrue(detailedReportContent.contains("<![CDATA[class com.seleniumtests.customexception.DriverExceptions: & some exception \"with \" <strong><a href='http://someurl/link' style='background-color: red;'>HTML to encode</a></strong>"));
		Assert.assertTrue(detailedReportContent.contains("class com.seleniumtests.customexception.DriverExceptions: Caused by root <error>"));
	}
	
	/**
	 * Check that if a test is skipped, a performance report is still generated
	 */
	@Test(groups={"it"})
	public void testSkippedTestGeneration(ITestContext testContext) throws Exception {

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass2"});

		// check that files are present and that they contain no step
		String detailedReportContent = readTestMethodPerfFile("test2");
		logger.info("content: ");
		logger.info(detailedReportContent);
		Assert.assertTrue(detailedReportContent.contains("Test has not started or has been skipped"));
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "<testcase classname"), 1); // only Test end step
		
		// check other file contains steps
		String detailedReportContent2 = readTestMethodPerfFile("test1");
		Assert.assertTrue(detailedReportContent2.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass2\" name=\"Step 1: Pre test step: slow\""));
		Assert.assertTrue(detailedReportContent2.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass2\" name=\"Step 2: Pre test step: set\""));
	}
	
	/**
	 * Check that if a step is skipped, a performance report is still generated
	 */
	@Test(groups={"it"})
	public void testSkippedStepInTest(ITestContext testContext) throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestSteps"});

		String detailedReportContent = readTestMethodPerfFile("testSkippedStep");
		
		Assert.assertTrue(detailedReportContent.matches(".*<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClassForTestSteps\" name=\"Step 4: skipStep\" time=\"\\d+\\.\\d+\"><skipped/>.*"));
		Assert.assertTrue(detailedReportContent.contains("errors=\"-1\""));
	}
	

	/**
	 * Check that information recorded during test, by calling 'SeleniumRobotTestPlan.addTestInfo(key, value)' are added to summary and test report
	 */
	@Test(groups={"it"})
	public void testWithTestInfo() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithInfo1", "testWithInfo2", "testAndSubActions"});
		
		// check info is present in PERF-result.xml
		String detailedReportContent = readTestMethodPerfFile("testWithInfo2");
		Assert.assertTrue(detailedReportContent.contains("<infos>" + 
				"<info key=\"user ID\" value=\"link http://foo/bar/12345;info 12345\"></info>"
				+ "<info key=\"Last State\" value=\"\"></info>" + 
				"</infos>"));

		String detailedReportContent2 = readTestMethodPerfFile("testAndSubActions");
		Assert.assertTrue(detailedReportContent2.contains("<infos><info key=\"Last State\" value=\"\"></info>" +  
				"</infos>"));
	}
	

	/**
	 * Check that when snapshot server is used with behavior "addTestResult" 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoAddTestResult() throws Exception {
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// test both files are available
			String detailedReportContent1 = readTestMethodPerfFile("snapshots-testAndSubActions");
			Assert.assertTrue(detailedReportContent1.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step"));
			
			// this file is not re-generated with "snapshot comparison" step, but this not a problem. Important fact is that both files are present
			String detailedReportContent2 = readTestMethodPerfFile("testAndSubActions");
			Assert.assertTrue(detailedReportContent2.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 6: Test end\""));
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	@Test(groups={"it"})
	public void testSnapshotComparisonSkipAddTestResult() throws Exception {
		
		try {
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': 'error'}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// test both files are available
			String detailedReportContent1 = readTestMethodPerfFile("snapshots-testAndSubActions");
			Assert.assertTrue(detailedReportContent1.contains("<system-out><![CDATA[Test skipped]]></system-out>"));
			Assert.assertTrue(detailedReportContent1.contains("errors=\"-1\"")); // -1 means 'skipped' with our reporter
			
			// this file is not re-generated with "snapshot comparison" step, but this not a problem. Important fact is that both files are present
			String detailedReportContent2 = readTestMethodPerfFile("testAndSubActions");
			Assert.assertTrue(detailedReportContent2.contains("<testcase classname=\"com.seleniumtests.it.stubclasses.StubTestClass\" name=\"Step 6: Test end\""));
			
		} finally {
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumRobotServerContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

	@Test(groups={"it"})
	public void testReportErrors(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testFailedWithException"});

		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testFailedWithException");

		// issue #418: now it's Test end which is the failed step because in this case execption is raised directly in test scenario
		Assert.assertTrue(jmeterReport.contains("retries=\"2\""));
		Assert.assertTrue(jmeterReport.contains("failures=\"0\"")); // no failure as it's not assertion exception
		Assert.assertTrue(jmeterReport.contains("errors=\"1\"")); // be sure an error is reported
		Assert.assertTrue(jmeterReport.contains("failedStep=\"failAction\""));
	}

	@Test(groups={"it"})
	public void testReportFailures(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.METHODS, new String[] {"testMultipleFailedWithSoftAssertEnabled"});

		// check content of summary report file
		String jmeterReport = readTestMethodPerfFile("testMultipleFailedWithSoftAssertEnabled");

		// issue #418: now it's Test end which is the failed step because in this case execption is raised directly in test scenario
		Assert.assertTrue(jmeterReport.contains("retries=\"0\""));
		Assert.assertTrue(jmeterReport.contains("failures=\"2\"")); // 2 steps failed
		Assert.assertTrue(jmeterReport.contains("errors=\"0\"")); // no error, only failure
		Assert.assertTrue(jmeterReport.contains("failedStep=\"assertAction2\""));
	}
}
