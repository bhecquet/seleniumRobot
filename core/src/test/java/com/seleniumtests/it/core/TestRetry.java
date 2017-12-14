package com.seleniumtests.it.core;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestRetry extends ReporterTest {

	@Test(groups={"it"})
	public void testRetryOnException() throws Exception {
		
		TestNG tng = executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});

		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		Assert.assertTrue(mainReportContent.contains("<a href='SeleniumTestReport-2.html'>testInError</a>"));
		Assert.assertTrue(mainReportContent.contains("<a href='SeleniumTestReport-3.html'>testWithException</a>"));
	
		// check failed test is not retried (AssertionError) based on log. No more direct way found
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-2.html"));
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent2.contains("Failed in 1 times"));
		Assert.assertFalse(detailedReportContent2.contains("[RETRYING] class com.seleniumtests.it.reporter.StubTestClass.testInError"));
		
		// check test with exception is retried based on log. No more direct way found
		String detailedReportContent3 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport-3.html"));
		detailedReportContent3 = detailedReportContent3.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent3.contains("Failed in 3 times"));
		Assert.assertTrue(detailedReportContent3.contains("[RETRYING] class com.seleniumtests.it.reporter.StubTestClass.testWithException"));
		
		// check that in case of retry, steps are not logged twice
		Assert.assertTrue(detailedReportContent3.contains("step 1"));
		Assert.assertTrue(detailedReportContent3.contains("<li>played 3 times")); // only the last step is retained
		Assert.assertFalse(detailedReportContent3.contains("<li>played 2 times")); // only the last step is retained
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent3, "step 1"), 1); 

	}
}
