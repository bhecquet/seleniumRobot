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

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

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
		
		System.setProperty("customTestReports", "SUP::json::ti/report.test.vm");

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
			
		// check content of the file. It should contains all fields with a value
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SUP-com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions.json"));
		
		JSONObject json = new JSONObject(detailedReportContent);
		
		Assert.assertEquals(json.getInt("errors"), 0);
		Assert.assertEquals(json.getInt("failures"), 1);
		Assert.assertEquals(json.getString("hostname"), "");
		Assert.assertEquals(json.getString("suiteName"), "testAndSubActions");
		Assert.assertEquals(json.getString("className"), "com.seleniumtests.it.stubclasses.StubTestClass");
		Assert.assertEquals(json.getInt("tests"), 3);
		Assert.assertEquals(json.get("duration").toString(), "15.26");
		Assert.assertTrue(json.getLong("time") > 1518709523620L);
		Assert.assertEquals(json.getJSONArray("testSteps").length(), 3);
		Assert.assertEquals(json.getJSONArray("testSteps").get(0), "Step step 1\\nclick button\\nsendKeys to text field\\nStep step 1.3: open page\\nclick link\\na message\\nsendKeys to password field");
		Assert.assertEquals(json.getString("browser"), "NONE");
		Assert.assertNotNull(json.get("version"));
		Assert.assertTrue(json.getJSONObject("parameters").length() > 70);
		Assert.assertEquals(json.getJSONObject("parameters").getString("testType"), "NON_GUI");
		Assert.assertEquals(json.getJSONObject("parameters").getString("replayTimeOut"), "30");
		Assert.assertEquals(json.getJSONArray("stacktrace").length(), 0);
	}
	
	@Test(groups={"it"})
	public void testSupervisionReport(ITestContext testContext) throws Exception {
		
		System.setProperty("customTestReports", "SUP::xml::reporter/templates/report.supervision.vm");

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
			
		// check content of the file. It should contain error
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SUP-com.seleniumtests.it.stubclasses.StubTestClass.testInError.xml"));
		detailedReportContent = detailedReportContent.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent.contains("<errors><error>				class java.lang.AssertionError: error								at com.seleniumtests"));
		
		String detailedReportContent2 = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "SUP-com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions.xml"));
		detailedReportContent2 = detailedReportContent2.replace("\n", "").replace("\r",  "").replaceAll(">\\s+<", "><");
		Assert.assertTrue(detailedReportContent2.contains("<errors></errors>"));
	}
	
	
	/**
	 * Check information are present in summary report
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDataInSummaryReport(ITestContext testContext) throws Exception {
		
		System.setProperty("customSummaryReports", "summaryResult::json::ti/report.summary.vm");
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		// check content of the file. It should contains all fields with a value
		String detailedReportContent = FileUtils.readFileToString(new File(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath() + File.separator + "summaryResult.json"));
		
		JSONObject json = new JSONObject(detailedReportContent);
		
		Assert.assertEquals(json.getInt("fail"), 2);
		Assert.assertEquals(json.getInt("pass"), 1);
		Assert.assertEquals(json.getInt("skip"), 0);
		Assert.assertEquals(json.getInt("total"), 3);
		
	}
	
	@Test(groups={"it"}, expectedExceptions=ConfigurationException.class)
	public void testTestReportDoesNotExists(ITestContext testContext) throws Exception {
		
		System.setProperty("customTestReports", "SUP::json::ti/report.test.nowhere.vm");
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
	}
	
	@Test(groups={"it"}, expectedExceptions=ConfigurationException.class)
	public void testSummaryReportDoesNotExists(ITestContext testContext) throws Exception {
		
		System.setProperty("customSummaryReports", "SUP::json::ti/report.summary.nowhere.vm");
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});	
	}
	
	// tester si le custom report n'existe pas
	// tester si le summary report n'existe pas
}
