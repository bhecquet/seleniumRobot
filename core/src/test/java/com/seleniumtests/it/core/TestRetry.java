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
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.reporter.ReporterTest;

public class TestRetry extends ReporterTest {

	@Test(groups={"it"})
	public void testRetryOnException() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});

		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testInError/TestReport\\.html'.*?>testInError</a>.*"));
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='testWithException/TestReport\\.html'.*?>testWithException</a>.*"));
	
		// check failed test is not retried (AssertionError) based on log. No more direct way found
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "TestReport.html").toFile());
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent2.contains("Failed in 1 times"));
		Assert.assertFalse(detailedReportContent2.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testInError"));
		
		// check test with exception is retried based on log. No more direct way found
		String detailedReportContent3 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "TestReport.html").toFile());
		detailedReportContent3 = detailedReportContent3.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent3.contains("Failed in 3 times"));
		Assert.assertTrue(detailedReportContent3.contains("[RETRYING] class com.seleniumtests.it.stubclasses.StubTestClass.testWithException"));
		
		// check that in case of retry, steps are not logged twice
		Assert.assertTrue(detailedReportContent3.contains("step 1"));
		Assert.assertTrue(detailedReportContent3.contains("<li>played 3 times")); // only the last step is retained
		Assert.assertFalse(detailedReportContent3.contains("<li>played 2 times")); // only the last step is retained
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent3, "step 1"), 1); 

	}
	
	@Test(groups={"it"})
	public void testCucumberRetryOnException() throws Exception {
		
		executeSubCucumberTests("error_scenario", 1);
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		Assert.assertTrue(mainReportContent.matches(".*<a href\\='error_scenario/TestReport\\.html'.*?>error_scenario</a>.*"));
		
		// check failed test is not retried (AssertionError) based on log. No more direct way found
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "error_scenario", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent.contains("Failed in 3 times"));
		Assert.assertTrue(detailedReportContent.contains("[RETRYING] class com.seleniumtests.core.runner.CucumberTestPlan.feature"));

		// check that in case of retry, steps are not logged twice and step name is the cucumber step name (defined by annotation
		Assert.assertTrue(detailedReportContent.contains("write_error"));
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "Start method error_scenario"), 3); 
		Assert.assertEquals(StringUtils.countOccurrencesOf(detailedReportContent, "Finish method error_scenario"), 3); 
		
	}
}
