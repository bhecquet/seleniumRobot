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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.testanalysis.ErrorCauseFinder;
import com.seleniumtests.reporter.reporters.ReporterControler;

@PrepareForTest({ErrorCauseFinder.class, ReporterControler.class, SeleniumTestsContext.class})
public class TestReporterControler extends ReporterTest {

	
	@Mock
	private ErrorCauseFinder errorCauseFinder;
	
	/**
	 * Check that files created by robot but not integrated to tests are deleted
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testUnusedCaptureAreDeleted() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverNativeActions"});
		
		// if a file belongs to a step, it's renamed
		for (File htmlFile: Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverNativeActions", "htmls").toFile().listFiles()) {
			Assert.assertTrue(htmlFile.getName().startsWith("testDriver"));
		}
		for (File imgFile: Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverNativeActions", "screenshots").toFile().listFiles()) {
			Assert.assertTrue(imgFile.getName().startsWith("testDriver"));
		}
	}
	
	/**
	 * Checks that if a test is retried, captures of the last execution are kept (issue #121)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testUnusedCaptureAreDeletedWhenTestFails() throws Exception {
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverWithFailure"});
		
		// check files are there
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithFailure", "htmls").toFile().listFiles().length, 2);
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithFailure", "screenshots").toFile().listFiles().length, 2);
		
		// if a file belongs to a step, it's renamed
		for (File htmlFile: Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithFailure", "htmls").toFile().listFiles()) {
			Assert.assertTrue(htmlFile.getName().startsWith("testDriverWithFailure"));
		}
		for (File imgFile: Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverWithFailure", "screenshots").toFile().listFiles()) {
			Assert.assertTrue(imgFile.getName().startsWith("testDriverWithFailure"));
		}
	}
	
	/**
	 * Check that with driver starting and operations in BeforeMethod method, screenshots are correctly handled
	 * - copied in the relevant folder
	 * - always present at the end of the test 
	 */
	@Test(groups={"it"})
	public void testBeforeMethodCapturesArePresent() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty("startLocation", "beforeMethod");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener5.test1Listener5"}, "", "stub1");
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
		}
		
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();

		
		// check that there is not folder named 'beforeMethod' or 'startTestMethod', which correspond to @BeforeMethod annotated methods
		for (String fileName: new File(outDir).list()) {
			if (fileName.startsWith("beforeMethod") || fileName.startsWith("startTestMethod")) {
				Assert.fail("execution of '@BeforeMethod' should not create output folders");
			}
		}
		
		// check that a 'before-test1Listener5' has been created and (iisue #399) does not contain html capture
		Assert.assertTrue(Arrays.asList(new File(outDir).list()).contains("before-test1Listener5"));
		Assert.assertEquals(Paths.get(outDir, "before-test1Listener5", "htmls").toFile().list().length, 0);
		
		// check that a 'test1Listener5' has been created and contains html capture from "before" step and test step
		Assert.assertEquals(Paths.get(outDir, "test1Listener5", "htmls").toFile().list().length, 2);
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
		
		String mainReportContent = readSummaryFile();
		
		// check main result is skipped with step failed indicated as a link
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-1\">3<sup><a href=\"#\" data-toggle=\"tooltip\" class=\"failedStepsTooltip\" title=\"2 step(s) failed\">*</a></sup></td>"));
		
		
		String detailedReportContent = readTestMethodResultFile("testWithABeforeMethodError");

		// check test is skipped as before method failed
		Assert.assertTrue(detailedReportContent.contains("<header class='main-header header-skipped'>"));
		
		// Check details of the configuration error is displayed in report (this behaviour is controled by TestNG which adds exception from BeforeXXX to test method throwable)
		Assert.assertTrue(detailedReportContent.contains("<div>class com.seleniumtests.customexception.ConfigurationException: Some error before method</div>"));
		
		// check we have a step for BeforeMethod and it's marked as failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Pre test step: beforeMethod"));
		
		// Check details of the configuration error is displayed in report
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error\">\\s+class com.seleniumtests.customexception.ConfigurationException: Some error before method.*"));

		// check that when test is skipped, a message on test status is displayed
		Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: Test has not started or has been skipped</div>"));
				
		
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
		
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForConfigurationError2"}, ParallelMode.NONE, new String[] {"testWithAfterMethodError"}); 
		
		String mainReportContent = readSummaryFile();
		
		// check main result is skipped with step failed in red
		Assert.assertTrue(mainReportContent.contains("<td name=\"stepsTotal-1\">5<sup><a href=\"#\" data-toggle=\"tooltip\" class=\"failedStepsTooltip\" title=\"1 step(s) failed\">*</a></sup></td>"));
		
		String detailedReportContent = readTestMethodResultFile("testWithAfterMethodError");
		
		// Check details of the configuration error is displayed in report
		Assert.assertTrue(detailedReportContent.matches(".*<div class=\"message-error\">\\s+class com.seleniumtests.customexception.ConfigurationException: Some error after method.*"));
		
		// check test is still OK as only after method failed
		Assert.assertTrue(detailedReportContent.contains("<header class='main-header header-success'>"));
		
		// check execution log does not contain our post configuration step
		Assert.assertFalse(detailedReportContent.contains("<div>class com.seleniumtests.customexception.ConfigurationException: Some error after method</div>"));
		
		// check we have a step for AfterMethod and it's marked as failed
		Assert.assertTrue(detailedReportContent.contains("<div class=\"box collapsed-box failed\"><div class=\"box-header with-border\"><button type=\"button\" class=\"btn btn-box-tool\" data-widget=\"collapse\"><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> Post test step: afterMethod"));
		
		// check logs written in @AfterXXX are present in execution logs
		Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: some warning</div>"));
		
		// check that when test is OK, a message on test status is displayed
		Assert.assertTrue(detailedReportContent.contains("[main] ScenarioLogger: Test is OK"));
		
	}
	
	/**
	 * Check that all configuration steps are logged in detailed report as pre / post test actions
	 * Also check that configuration step name does not contain method arguments
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportDetailsAllConfigurationSteps() throws Exception {
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForListener1"}); 
		
		String detailedReportContent = readTestMethodResultFile("test1Listener1");
		
		Assert.assertTrue(detailedReportContent.contains("</i></button><span class=\"step-title\"> Pre test step: beforeMethod -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button><span class=\"step-title\"> Pre test step: beforeTest -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button><span class=\"step-title\"> Pre test step: beforeClass -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button><span class=\"step-title\"> Post test step: afterMethod -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button><span class=\"step-title\"> Post test step: afterClass -"));
		Assert.assertTrue(detailedReportContent.contains("</i></button><span class=\"step-title\"> Post test step: afterTest -"));
		
		// check reference to configuration methods for class / test / method are in both results (some are common)
		String detailedReportContent2 = readTestMethodResultFile("test2Listener1");
		
		Assert.assertTrue(detailedReportContent2.contains("</i></button><span class=\"step-title\"> Pre test step: beforeMethod -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button><span class=\"step-title\"> Pre test step: beforeTest -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button><span class=\"step-title\"> Pre test step: beforeClass -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button><span class=\"step-title\"> Post test step: afterMethod -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button><span class=\"step-title\"> Post test step: afterClass -"));
		Assert.assertTrue(detailedReportContent2.contains("</i></button><span class=\"step-title\"> Post test step: afterTest -"));
	}
	
	/**
	 * detailed report should contain only configuration steps corresponding to the test method / test class / test (TestNG test)
	 * By default, test context contains all configuration methods. Check we filter them and we have only one configuration step even if it's retried
	 * (case where test method fails and is retried, \@BeforeMethod is then called several times
	 */
	@Test(groups={"it"})
	public void testReportDetailsOnlyTestConfigurationSteps() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		
		String detailedReportContent = readTestMethodResultFile("testAndSubActions");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent, "</i></button><span class=\"step-title\"> Pre test step: set -"), 1);
		
		String detailedReportContent2 = readTestMethodResultFile("testInError");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent2, "</i></button><span class=\"step-title\"> Pre test step: set -"), 1);
		
		// check that when test is KO, error cause is displayed
		Assert.assertTrue(detailedReportContent2.contains("[main] ScenarioLogger: Test is KO with error: class java.lang.AssertionError: error"));
		
		String detailedReportContent3 = readTestMethodResultFile("testWithException");
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "</i></button><span class=\"step-title\"> Pre test step: set -"), 1);
		Assert.assertEquals(StringUtils.countMatches(detailedReportContent3, "</i></button><span class=\"step-title\"> Post test step: reset -"), 1);
		
		// in case of test method error, it is retried so each Before/After method is also replayed. Check it's the last one we have
		Assert.assertTrue(detailedReportContent3.contains("<div class=\"message-info\">before count: 2</div>"));
		Assert.assertTrue(detailedReportContent3.contains("<div class=\"message-info\">after count: 3</div>"));
	}
	
	/**
	 * Check we try to find error cause when
	 * - findErrorCause=true
	 * - seleniumServer is active
	 * - result recording is active (meaning we also record step references)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorCauseSearched() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.FIND_ERROR_CAUSE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, SERVER_URL);
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			
			PowerMockito.whenNew(ErrorCauseFinder.class).withAnyArguments().thenReturn(errorCauseFinder);
			configureMockedSnapshotServerConnection();
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverNativeActions"});
			
			// we search only once for each test result, at the end of test suite
			verify(errorCauseFinder).findErrorCause();
			
		} finally {
			System.clearProperty(SeleniumTestsContext.FIND_ERROR_CAUSE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
		
	}
	
	/**
	 * Check we do not try to find error cause when
	 * - findErrorCause=false
	 * - seleniumServer is active
	 * - result recording is active (meaning we also record step references)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorCauseNotSearchedFlagFalse() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.FIND_ERROR_CAUSE, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, SERVER_URL);
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "true");
			
			PowerMockito.whenNew(ErrorCauseFinder.class).withAnyArguments().thenReturn(errorCauseFinder);
			configureMockedSnapshotServerConnection();
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverNativeActions"});
			
			// we search only once for each test result, at the end of test suite
			verify(errorCauseFinder, never()).findErrorCause();
			
		} finally {
			System.clearProperty(SeleniumTestsContext.FIND_ERROR_CAUSE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
		
	}
	
	/**
	 * Check we do not try to find error cause when
	 * - findErrorCause=true
	 * - seleniumServer is active
	 * - result recording is inactive 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testErrorCauseNotSearchedNoRecordResult() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.FIND_ERROR_CAUSE, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, SERVER_URL);
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS, "false");
			
			PowerMockito.whenNew(ErrorCauseFinder.class).withAnyArguments().thenReturn(errorCauseFinder);
			configureMockedSnapshotServerConnection();
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverNativeActions"});
			
			// we search only once for each test result, at the end of test suite
			verify(errorCauseFinder, never()).findErrorCause();
			
		} finally {
			System.clearProperty(SeleniumTestsContext.FIND_ERROR_CAUSE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_RECORD_RESULTS);
		}
		
	}
}
