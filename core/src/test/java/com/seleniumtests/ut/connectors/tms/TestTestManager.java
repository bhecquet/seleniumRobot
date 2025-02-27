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
package com.seleniumtests.ut.connectors.tms;

import com.seleniumtests.connectors.tms.ITestManager;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;


public class TestTestManager extends GenericTest {
	
	@Test(groups={"ut"})
	public void testTmsSelectionSquashTm() {
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertTrue(manager instanceof SquashTMConnector);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testTmsSelectionWrongType() {
		String config = "{'tmsType': 'spira', 'tmsRun': '3'}";
		TestManager.getInstance(new JSONObject(config));
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testTmsSelectionNoType() {
		String config = "{'tmsType': '3'}";
		TestManager.getInstance(new JSONObject(config));
	}
	
	
	/**
	 * #545: test the case where test id is set inside test, not as annotation parameter
	 */
	@Test(groups={"ut"})
	public void testTestCaseIdFromContext() {
		ITestResult tr = Reporter.getCurrentTestResult();
		SeleniumTestsContextManager.getThreadContext().testManager().setTestId(23);
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertEquals(manager.getTestCaseId(tr), (Integer)23);
	}
	
	@Test(groups={"ut"}, attributes = {@CustomAttribute(name = "testId", values = "12")})
	public void testTestCaseId() {
		ITestResult tr = Reporter.getCurrentTestResult();
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertEquals(manager.getTestCaseId(tr), (Integer)12);
	}
	
	@Test(groups={"ut"}, attributes = {@CustomAttribute(name = "testId", values = "foo")})
	public void testTestCaseIdWrongFormat() {
		ITestResult tr = Reporter.getCurrentTestResult();
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertNull(manager.getTestCaseId(tr));
	}
	
	@Test(groups={"ut"})
	public void testTestCaseIdNotDefined() {
		ITestResult tr = Reporter.getCurrentTestResult();
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertNull(manager.getTestCaseId(tr));
	}

	@Test(groups={"ut"})
	public void testDatasetIdFromContext() {
		ITestResult tr = Reporter.getCurrentTestResult();
		SeleniumTestsContextManager.getThreadContext().testManager().setDatasetId(23);
		TestNGResultUtils.setSeleniumRobotTestContext(tr, SeleniumTestsContextManager.getThreadContext());
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertEquals(manager.getDatasetId(tr), (Integer)23);
	}

	@Test(groups={"ut"}, attributes = {@CustomAttribute(name = "testId", values = "12"), @CustomAttribute(name = "datasetId", values = "13")})
	public void testDatasetId() {
		ITestResult tr = Reporter.getCurrentTestResult();
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertEquals(manager.getDatasetId(tr), (Integer)13);
	}

	@Test(groups={"ut"}, attributes = {@CustomAttribute(name = "testId", values = "12"), @CustomAttribute(name = "datasetId", values = "foo")})
	public void testDatasetIdWrongFormat() {
		ITestResult tr = Reporter.getCurrentTestResult();
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertNull(manager.getDatasetId(tr));
	}

	@Test(groups={"ut"})
	public void testDatasetIdNotDefined() {
		ITestResult tr = Reporter.getCurrentTestResult();
		String config = "{'tmsType': 'squash'}";
		ITestManager manager = TestManager.getInstance(new JSONObject(config));
		Assert.assertNull(manager.getDatasetId(tr));
	}
}
