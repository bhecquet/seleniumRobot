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
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestRetry extends ReporterTest {

	@Test(groups={"it"})
	public void testNotRetriedOnAssertion() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testInError"});

		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
	
		// check failed test is not retried (AssertionError) based on log. No more direct way found
		String detailedReportContent = readTestMethodResultFile("testInError");
		Assert.assertTrue(detailedReportContent.contains("Failed in 1 times"));
		Assert.assertFalse(detailedReportContent.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testInError"));
		
	}
	
	@Test(groups={"it"})
	public void testRetryOnException() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithException"});
		
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testWithException/TestReport\\.html'.*?>testWithException</a>.*"));
		
		// check test with exception is retried based on log. No more direct way found
		String detailedReportContent = readTestMethodResultFile("testWithException");
		Assert.assertTrue(detailedReportContent.contains("Failed in 3 times"));
		Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testWithException"));
		
		// check that in case of retry, steps are not logged twice
		Assert.assertTrue(detailedReportContent.contains("step 1"));
		Assert.assertTrue(detailedReportContent.contains("<li>played 3 times")); // only the last step is retained
		Assert.assertFalse(detailedReportContent.contains("<li>played 2 times")); // only the last step is retained
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "step 1"), 1); 
		
	}
	
	/**
	 * Check that with DataProvider, number of retries is correctly taken into account
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnExceptionWithDataProvider() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "1");
		
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithExceptionAndDataProvider"});
			
			String mainReportContent = readSummaryFile();
			Assert.assertTrue(mainReportContent.matches(".*<a href\\='testWithExceptionAndDataProvider/TestReport\\.html'.*?>testWithExceptionAndDataProvider</a>.*"));
			
			// check test with exception is retried based on log. No more direct way found
			String detailedReportContent = readTestMethodResultFile("testWithExceptionAndDataProvider");
			Assert.assertTrue(detailedReportContent.contains("FAILED, Retrying 1 time"));
			Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testWithExceptionAndDataProvider"));
			
			// check that in case of retry, steps are not logged twice
			Assert.assertTrue(detailedReportContent.contains("step 1"));
			Assert.assertTrue(detailedReportContent.contains("<li>played 2 times")); // only the last step is retained
			Assert.assertFalse(detailedReportContent.contains("<li>played 1 times")); // only the last step is retained
			Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "step 1"), 1); 
		} finally {
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
		}
		
	}
	
	@Test(groups={"it"})
	public void testRetryOnExceptionWithParameter() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithExceptionAndDataProvider"});
		
		String mainReportContent = readSummaryFile();
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testWithExceptionAndDataProvider/TestReport\\.html'.*?>testWithExceptionAndDataProvider</a>.*"));
		
		// check test with exception is retried based on log. No more direct way found
		String detailedReportContent = readTestMethodResultFile("testWithExceptionAndDataProvider");
		Assert.assertTrue(detailedReportContent.contains("FAILED, Retrying 2 time"));
		Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testWithExceptionAndDataProvider"));
		
		// check that in case of retry, steps are not logged twice
		Assert.assertTrue(detailedReportContent.contains("step 1"));
		Assert.assertTrue(detailedReportContent.contains("<li>played 3 times")); // only the last step is retained
		Assert.assertFalse(detailedReportContent.contains("<li>played 2 times")); // only the last step is retained
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "step 1"), 1); 
		
	}
	
	/**
	 * issue #282: check it's possible to increase dynamically the max retry count
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnExceptionWithDynamicMaxRetry() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithExceptionAndMaxRetryIncreased"});

		// check test with exception is retried based on log. No more direct way found
		String detailedReportContent = readTestMethodResultFile("testWithExceptionAndMaxRetryIncreased");
		Assert.assertTrue(detailedReportContent.contains("Failed in 5 times"));
		Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testWithExceptionAndMaxRetryIncreased"));
		
		// check that in case of retry, steps are not logged twice
		Assert.assertTrue(detailedReportContent.contains("step 1"));
		Assert.assertTrue(detailedReportContent.contains("<li>played 5 times")); // only the last step is retained
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "step 1"), 1); 	
	}
	
	/**
	 * issue #282: check it's possible to increase dynamically the max retry count, but not above limit (2* max retry)
	 * Default is 3, so we should not execute the test more than (2 * 2 + 1) times
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testRetryOnExceptionWithDynamicMaxRetryAboveLimit() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testWithExceptionAndMaxRetryIncreasedWithLimit"});
		
		// check test with exception is retried based on log. No more direct way found
		String detailedReportContent = readTestMethodResultFile("testWithExceptionAndMaxRetryIncreasedWithLimit");
		Assert.assertTrue(detailedReportContent.contains("Failed in 5 times"));
		Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testWithExceptionAndMaxRetryIncreasedWithLimit"));
		
		// check that in case of retry, steps are not logged twice
		Assert.assertTrue(detailedReportContent.contains("step 1"));
		Assert.assertTrue(detailedReportContent.contains("<li>played 5 times")); // only the last step is retained
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "step 1"), 1); 	
	}
	
	@Test(groups={"it"})
	public void testCucumberRetryOnException() throws Exception {
		
		executeSubCucumberTests("error_scenario", 1);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"), StandardCharsets.UTF_8);
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='error_scenario/TestReport\\.html'.*?>error_scenario</a>.*"));
		Assert.assertFalse(mainReportContent.matches(".*<a href\\='error_scenario/TestReport\\.html'.*?>error_scenario-1</a>.*")); // check all executions are put in the same test
		
		// check failed test is not retried (AssertionError) based on log. No more direct way found
		String detailedReportContent = readTestMethodResultFile("error_scenario");
		Assert.assertTrue(detailedReportContent.contains("Failed in 3 times"));
		Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.core.runner.CucumberTestPlan.runScenario"));

		// check that in case of retry, steps are not logged twice and step name is the cucumber step name (defined by annotation
		Assert.assertTrue(detailedReportContent.contains("write_error"));
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "Start method error_scenario"), 3); 
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "Finish method error_scenario"), 3); 
		
	}
}
