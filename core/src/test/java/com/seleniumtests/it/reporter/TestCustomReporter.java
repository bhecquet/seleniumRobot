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

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

public class TestCustomReporter extends ReporterTest {
	
	/**
	 * Check information are present in detailed report
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDataInReport(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty("customTestReports", "SUP::json::ti/report.test.vm");
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
				
			// check content of the file. It should contains all fields with a value
			String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "SUP-result.json").toFile());
			System.out.println(detailedReportContent);
			JSONObject json = new JSONObject(detailedReportContent);
			
			Assert.assertEquals(json.getInt("errors"), 0);
			Assert.assertEquals(json.getInt("failures"), 1);
			Assert.assertEquals(json.getString("hostname"), "");
			Assert.assertEquals(json.getString("suiteName"), "testAndSubActions");
			Assert.assertEquals(json.getString("className"), "com.seleniumtests.it.stubclasses.StubTestClass");
			Assert.assertEquals(json.getInt("tests"), 6);
			Assert.assertTrue(Float.parseFloat(json.get("duration").toString()) > 15);
			Assert.assertTrue(json.getLong("time") > 1518709523620L);
			Assert.assertEquals(json.getJSONArray("testSteps").length(), 6);
			Assert.assertEquals(json.getJSONArray("testSteps").get(2), "Step step 1\\nclick button\\nsendKeys to text field\\nStep step 1.3: open page\\nclick link\\na message\\nsendKeys to password field");
			Assert.assertEquals(json.getString("browser"), "NONE");
			Assert.assertNotNull(json.get("version"));
			Assert.assertTrue(json.getJSONObject("parameters").length() > 70);
			Assert.assertEquals(json.getJSONObject("parameters").getString("testType"), "NON_GUI");
			Assert.assertEquals(json.getJSONObject("parameters").getInt("replayTimeOut"), 30);
			Assert.assertEquals(json.getJSONArray("stacktrace").length(), 0);
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	@Test(groups={"it"})
	public void testSupervisionReport(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty("customTestReports", "SUP::xml::reporter/templates/report.supervision.vm");
	
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
				
			// check content of the file. It should contain error
			String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testInError", "SUP-result.xml").toFile());
			detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
			Assert.assertTrue(detailedReportContent.contains("<errors><error>				class java.lang.AssertionError: error								at com.seleniumtests"));
			
			String detailedReportContent2 = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "SUP-result.xml").toFile());
			detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
			Assert.assertTrue(detailedReportContent2.contains("<errors></errors>"));
			
			// check parameters are there
			Assert.assertTrue(detailedReportContent2.contains("<param name=\"runMode\" value=\"LOCAL\"/>"));
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	
	/**
	 * Check information are present in summary report
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDataInSummaryReport(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty("customSummaryReports", "summaryResult::json::ti/report.summary.vm");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
			
			// check content of the file. It should contains all fields with a value
			String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "summaryResult.json"));
			
			JSONObject json = new JSONObject(detailedReportContent);
			
			Assert.assertEquals(json.getInt("fail"), 2);
			Assert.assertEquals(json.getInt("pass"), 1);
			Assert.assertEquals(json.getInt("skip"), 0);
			Assert.assertEquals(json.getInt("total"), 3);
		} finally {
			System.clearProperty("customSummaryReports");
		}
		
	}
	
	@Test(groups={"it"}, expectedExceptions=ConfigurationException.class)
	public void testTestReportDoesNotExists(ITestContext testContext) throws Exception {
		try {
			System.setProperty("customTestReports", "SUP::json::ti/report.test.nowhere.vm");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	@Test(groups={"it"}, expectedExceptions=ConfigurationException.class)
	public void testSummaryReportDoesNotExists(ITestContext testContext) throws Exception {
		try {
			System.setProperty("customSummaryReports", "SUP::json::ti/report.summary.nowhere.vm");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		} finally {
			System.clearProperty("customSummaryReports");
		}
	}
	
	@Test(groups={"it"})
	public void testXmlCharacterEscape(ITestContext testContext) throws Exception {
		try {
			System.setProperty("customTestReports", "SUP::xml::reporter/templates/report.supervision.vm");
			
			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});
			
			String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "SUP-result.xml").toFile());
			
			// check step 1 has been encoded
			Assert.assertTrue(detailedReportContent.contains("<name>step 1 &lt;&gt;&quot;&apos;&amp;/</name>"));
			
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	@Test(groups={"it"})
	public void testJsonCharacterEscape(ITestContext testContext) throws Exception {
		try {
			System.setProperty("customTestReports", "SUP::json::ti/report.test.vm");
			
			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForEncoding"});
			
			String detailedReportContent = FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testAndSubActions", "SUP-result.json").toFile());
			
			// check step 1 has been encoded
			Assert.assertTrue(detailedReportContent.contains("Step step 1 <>\\\\\"'&\\\\/\\\\nclick button  <>\\\\\"'&\\\\na message <>\\\\\"'&"));
			
		} finally {
			System.clearProperty("customTestReports");
		}
	}
	
	// tester si le custom report n'existe pas
	// tester si le summary report n'existe pas
}
