/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class TestReporterControler extends ReporterTest {

	
	/**
	 * Check that files created by robot but not integrated to tests are deleted
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testUnusedCaptureAreDeleted() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"});
		
		// if a file belongs to a step, it's renamed
		for (File htmlFile: Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverNativeActions", "htmls").toFile().listFiles()) {
			Assert.assertTrue(htmlFile.getName().startsWith("testDriver"));
		}
		for (File imgFile: Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverNativeActions", "screenshots").toFile().listFiles()) {
			Assert.assertTrue(imgFile.getName().startsWith("testDriver"));
		}
	}
	

	/**
	 * Check BeforeXXX configuration error is recorded in detailed file
	 * - in execution logs
	 * - a configuration step is displayed
	 * Check that overall step is skipped
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithBeforeConfigurationError() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForConfigurationError1"}); 
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		// check main result is skipped with step failed in red
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\" class=\"failedSteps\">1</td>"));
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithABeforeMethodError", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");

		// check test is skipped as before method failed
		Assert.assertTrue(detailedReportContent.contains("<header class='main-header header-skipped'>"));
		
		// Check details of the configuration error is displayed in report (this behaviour is controled by TestNG which adds exception from BeforeXXX to test method throwable)
		Assert.assertTrue(detailedReportContent.contains("<div>class com.seleniumtests.customexception.ConfigurationException: Some error before method</div>"));
		
		// check we have a step for BeforeMethod and it's marked as failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Pre test step: beforeMethod"));
		
		// Check details of the configuration error is displayed in report
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">				com.seleniumtests.customexception.ConfigurationException: Some error before method"));

		// check that when test is skipped, a message on test status is displayed
		Assert.assertTrue(detailedReportContent.contains("[main] TestLogging: Test has not started or has been skipped</div>"));
				
		
	}
	
	/**
	 * Check AfterXXX configuration error is recorded in detailed file
	 * - a specific step is displayed
	 * - logs of this specific step is present in execution logs
	 * Check that overall test is OK
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsWithAfterConfigurationError() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForConfigurationError2"}); 
		
		String mainReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SeleniumTestReport.html"));
		mainReportContent = mainReportContent.replace("\n", "").replace("\r",  "");
		
		// check main result is skipped with step failed in red
		Assert.assertTrue(mainReportContent.contains("<td name=\"failed-1\" class=\"failedSteps\">1</td>"));
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithAfterMethodError", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		// Check details of the configuration error is displayed in report
		Assert.assertTrue(detailedReportContent.contains("<div class=\"message-error\">				com.seleniumtests.customexception.ConfigurationException: Some error after method"));
		
		// check test is still OK as only after method failed
		Assert.assertTrue(detailedReportContent.contains("<header class='main-header header-success'>"));
		
		// check execution log does not contain our post configuration step
		Assert.assertFalse(detailedReportContent.contains("<div>class com.seleniumtests.customexception.ConfigurationException: Some error after method</div>"));
		
		// check we have a step for AfterMethod and it's marked as failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fa fa-plus\"></i></button> Post test step: afterMethod"));
		
		// check logs written in @AfterXXX are present in execution logs
		Assert.assertTrue(detailedReportContent.contains("[main] TestLogging: some warning</div>"));
		
		// check that when test is OK, a message on test status is displayed
		Assert.assertTrue(detailedReportContent.contains("[main] TestLogging: Test is OK"));
		
	}
	
	/**
	 * Check that all configuration steps are logged in detailed report as pre / post test actions
	 * Also check that configuration step name does not contain method arguments
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsAllConfigurationSteps() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener1"}); 
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "test1Listener1", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent.contains("</i></button> Pre test step: beforeMethod -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Pre test step: beforeTest -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Pre test step: beforeClass -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Post test step: afterMethod -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Post test step: afterClass -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button> Post test step: afterTest -"));
		
		// check reference to configuration methods for class / test / method are in both results (some are common)
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "test2Listener1", "TestReport.html").toFile());
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Pre test step: beforeMethod -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Pre test step: beforeTest -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Pre test step: beforeClass -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Post test step: afterMethod -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Post test step: afterClass -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button> Post test step: afterTest -"));
	}
	
	/**
	 * detailed report should contain only configuration steps corresponding to the test method / test class / test (TestNG test)
	 * By default, test context contains all configuration methods. Check we filter them and we have only one configuration step even if it's retried
	 * (case where test method fails and is retried, \@BeforeMethod is then called several times
	 */
	@Test(groups={"it"})
	public void testReportDetailsOnlyTestConfigurationSteps() throws Exception {
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "TestReport.html").toFile());
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</i></button> Pre test step: set -"), 1);
		
		String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "TestReport.html").toFile());
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "</i></button> Pre test step: set -"), 1);
		
		// check that when test is KO, error cause is displayed
		Assert.assertTrue(detailedReportContent2.contains("[main] TestLogging: Test is KO with error: error"));
		
		String detailedReportContent3 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testWithException", "TestReport.html").toFile());
		detailedReportContent3 = detailedReportContent3.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "</i></button> Pre test step: set -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "</i></button> Post test step: reset -"), 1);
		
		// in case of test method error, it is retried so each Before/After method is also replayed. Check it's the last one we have
		Assert.assertTrue(detailedReportContent3.contains("<div class=\"message-info\">before count: 2</div>"));
		Assert.assertTrue(detailedReportContent3.contains("<div class=\"message-info\">after count: 3</div>"));
	}
}
