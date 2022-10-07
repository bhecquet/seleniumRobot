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
import java.nio.file.Paths;
import java.util.List;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Test that default reporting contains results.json file (CustomReporter.java) with default summary reports defined in SeleniumTestsContext.DEFAULT_CUSTOM_SUMMARY_REPORTS
 * @author s047432
 *
 */
public class TestJUnitReporter extends ReporterTest {
	

	@AfterMethod(groups={"it"})
	private void deleteGeneratedFiles() {
		File outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		for (File file: outDir.listFiles()) {
			if ("results.json".equals(file.getName())) {
				file.delete();
			}
		}
		
	}
	

	/**
	 * Check single test report format when tests have steps
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultithreadTestReport() throws Exception {
		
		SeleniumTestsContextManager.removeThreadContext();
		List<String> testList = executeSubTest(3, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClassForDataProvider", "com.seleniumtests.it.stubclasses.StubTestClass2", "com.seleniumtests.it.stubclasses.StubTestClass3"}, ParallelMode.TESTS, new String[] {});
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		for (String testName: testList) {
			Assert.assertTrue(Paths.get(outDir, "junitreports", String.format("TEST-%s.xml", testName)).toFile().exists());
		}
		Assert.assertEquals(testList.size(), 4);
		
	}
	
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {

		List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();

		// check all files are generated with the right name
		for (String testName: testList) {
			Assert.assertTrue(Paths.get(outDir, "junitreports", String.format("TEST-%s.xml", testName)).toFile().exists());
		}

	}
	
	@Test(groups={"it"})
	public void testReportContent(ITestContext testContext) throws Exception {

		List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException", "testSkipped"});

		String result = readJUnitFile(testList.get(0));
		Assert.assertTrue(result.contains("<failure type=\"java.lang.AssertionError\" message=\"error\">"));
		Assert.assertTrue(result.contains("<error type=\"com.seleniumtests.customexception.DriverExceptions\" message=\"some exception\">")); // errors
		Assert.assertTrue(result.contains("[main] SeleniumRobotTestListener: Finish method testSkipped")); // some logs
		
		// issue #397: test we get only the executed tests 
		Assert.assertTrue(result.contains("<testcase name=\"testInError\""));
		Assert.assertTrue(result.contains("<testcase name=\"testAndSubActions\""));
		Assert.assertTrue(result.contains("<testcase name=\"testWithException\""));
		Assert.assertTrue(result.contains("<testcase name=\"testSkipped\""));
		Assert.assertFalse(result.contains("<testcase name=\"testWithExceptionAndDataProvider\""));
		Assert.assertFalse(result.contains("<testcase name=\"testWithExceptionAndMaxRetryIncreased\""));
		Assert.assertFalse(result.contains("<testcase name=\"testWithExceptionAndMaxRetryIncreasedWithLimit\""));
	
	}
	
	/**
	 * Check custom names are set in JUnit report
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContentCustomTestName(ITestContext testContext) throws Exception {
		
		List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testOkWithTestName", "testOkWithTestNameAndDataProvider"});
		
		String result = readJUnitFile(testList.get(0));
		Assert.assertTrue(result.contains("<testcase name=\"A test which is &lt;OK&gt; é&amp;\""));
		Assert.assertTrue(result.contains("<testcase name=\"A test which is OK (data2, data3)\""));
		
	}
	

	/**
	 * Check that when snapshot server is used with behavior "addTestResult" 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoAddTestResult() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String result = readJUnitFile(testList.get(0));
			
			Assert.assertTrue(result.contains("tests=\"2\""));
			Assert.assertTrue(result.contains("errors=\"1\""));
			Assert.assertTrue(result.contains("<error type=\"com.seleniumtests.customexception.ScenarioException\" message=\"Snapshot comparison failed\">"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	@Test(groups={"it"})
	public void testSnapshotComparisonSkipAddTestResult() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "addTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error1']}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String result = readJUnitFile(testList.get(0));
			Assert.assertTrue(result.contains("tests=\"2\""));
			Assert.assertTrue(result.contains("errors=\"0\""));
			Assert.assertTrue(result.contains("skipped=\"1\""));
			
			Assert.assertTrue(result.matches(".*<testcase name=\"snapshots-testAndSubActions\" time=\"\\d+\\.\\d+\" classname=\"com.seleniumtests.it.stubclasses.StubTestClass\"><skipped/>.*"));

			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * Check that when snapshot server is used with behavior "addTestResult" 2 results should be presented: one with the result of selenium test, a second one with the result of snapshot comparison.
	 * Both are the same but second test is there for integration with junit parser so that we can differentiate navigation result from GUI result.
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonKoChangeTestResult() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': false}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check there are 2 results. first one is the selenium test (OK) and second one is the snapshot comparison (KO)
			String result = readJUnitFile(testList.get(0));
			Assert.assertTrue(result.contains("tests=\"1\""));
			Assert.assertTrue(result.contains("errors=\"1\""));
			Assert.assertTrue(result.contains("<error type=\"com.seleniumtests.customexception.ScenarioException\" message=\"Snapshot comparison failed\">"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}
	
	/**
	 * When snapshot comparison is skipped, this does not change the test result
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotComparisonSkipChangeTestResult() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, "changeTestResult");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:4321");
			
			SeleniumRobotSnapshotServerConnector server = configureMockedSnapshotServerConnection();
			createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': null, 'computingError': ['error1']}");		
			
			SeleniumTestsContextManager.removeThreadContext();
			List<String> testList = executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check there is 1 results
			String result = readJUnitFile(testList.get(0));
			Assert.assertTrue(result.contains("tests=\"1\""));
			Assert.assertTrue(result.contains("errors=\"0\"")); // as comparison is skipped, test result is not changed
			Assert.assertFalse(result.contains("<error type=\"com.seleniumtests.customexception.ScenarioException\" message=\"Snapshot comparison failed\">"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
	}

}
