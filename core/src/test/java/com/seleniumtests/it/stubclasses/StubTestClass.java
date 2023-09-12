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
package com.seleniumtests.it.stubclasses;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.Mask;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.it.core.aspects.CalcPage;
import com.seleniumtests.reporter.info.HyperlinkInfo;
import com.seleniumtests.reporter.info.StringInfo;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.ScenarioLogger;

public class StubTestClass extends StubParentClass {
	
	private static int count = 0;
	public static boolean failed = false;
	private boolean maskPassword;
	
	@BeforeClass(groups={"stub"})
	public void setCount() {
		WaitHelper.waitForMilliSeconds(100);
		count = 0;
		failed = false;
	}

	@BeforeMethod(groups={"stub"})
	public void set(Method method) {
		WaitHelper.waitForMilliSeconds(100);
		logger.info("before count: " + count);
		maskPassword = SeleniumTestsContextManager.getThreadContext().getMaskedPassword();
	}
	
	@AfterMethod(groups={"stub"})
	public void reset(Method method) {
		logger.info("after count: " + count);
	}
	
	@Test(groups="stub", description="a test with steps")
	public void testAndSubActions() throws IOException {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		
		
		File tmpImg = File.createTempFile("img", "_with_very_very_very_long_name_to_be_shortened.png");
		File tmpHtml = File.createTempFile("html", "_with_very_very_very_long_name_to_be_shortened.html");
		
		ScreenShot screenshot = new ScreenShot(tmpImg, tmpHtml);
		step1.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		
		ScreenShot screenshot2 = new ScreenShot(tmpImg, tmpHtml);
		step1.addSnapshot(new Snapshot(screenshot2, null, SnapshotCheckType.FULL), 1, null);
		
		step1.setActionException(new WebDriverException("driver exception"));
		TestStep subStep1 = new TestStep("step 1.3: open page", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		subStep1.addAction(new TestAction("click link", false, new ArrayList<>()));
		subStep1.addMessage(new TestMessage("a message", MessageType.LOG));
		subStep1.addAction(new TestAction("sendKeys to password field", false, new ArrayList<>()));
		step1.addAction(subStep1);
		WaitHelper.waitForSeconds(3);
		step1.setDuration(1230L);
		TestStep step2 = new TestStep("step 2", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step2.setDuration(14030L);
		TestStepManager.logTestStep(step1);
		TestStepManager.logTestStep(step2);
		
		tmpImg.deleteOnExit();
		tmpHtml.deleteOnExit();
	}

	/**
	 * An other test that throws exception
	 */
	@Test(groups="stub", dependsOnMethods="testAndSubActions")
	public void testWithException2() {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		throw new DriverExceptions("some exception");
	}
	
	@Test(groups="stub")
	public void testInError() {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		TestStepManager.setCurrentRootTestStep(step1);
		TestStepManager.getParentTestStep().addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.getParentTestStep().addMessage(new TestMessage("click ok", MessageType.INFO));
		logger.warn("Some warning message");
		logger.info("Some Info message");
		logger.error("Some Error message");
		((ScenarioLogger)logger).log("Some log message");
		((ScenarioLogger)logger).logTestValue("key", "we found a value of", "10");
		
		TestStepManager.getParentTestStep().addAction(new TestAction("send keyboard action", false, new ArrayList<>()));
		TestStepManager.logTestStep(TestStepManager.getCurrentRootTestStep());
		Assert.fail("error");
	}
	
	@Test(groups="stub")
	public void testSkipped() {
		throw new SkipException("skip this test");
	}
	
	@Test(groups="stub")
	public void testWithException() {
		count++;
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		throw new DriverExceptions("some exception");
	}
	

	@DataProvider(name = "data")
	 public Object[][] data() {
		return new String[][] {new String[] {"data2"}};
    }
	
	@Test(groups="stub", dataProvider = "data")
	public void testWithExceptionAndDataProvider(String data) {
		count++;
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		throw new DriverExceptions("some exception");
	}
	
	/**
	 * Increase max retry
	 */
	@Test(groups="stub")
	public void testWithExceptionAndMaxRetryIncreased() {
		count++;
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		
		try {
			throw new DriverExceptions("some exception");
		} finally {
			if (count < 3) {
				increaseMaxRetry();
			}
		}
	}
	
	/**
	 * Increase max retry above limit
	 */
	@Test(groups="stub")
	public void testWithExceptionAndMaxRetryIncreasedWithLimit() {
		count++;
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		
		try {
			throw new DriverExceptions("some exception");
		} finally {
			if (count < 4) {
				increaseMaxRetry();
			}
		}
	}
	
	
	/**
	 * Issue #229: test with a step in error but test OK
	 */
	@Test(groups="stub")
	public void testOkWithOneStepFailed() {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction("first action failed", true, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
	}
	
	/**
	 * Test which fails only on first execution
	 */
	@Test(groups="stub")
	public void testWithExceptionOnFirstExec() {

		TestStep step1 = new TestStep("step 10", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		
		if (!failed) {
			failed = true;
			throw new DriverExceptions("some exception");
		}
	}
	
	/**
	 * Test which fails only on first execution
	 */
	@Test(groups="stub")
	public void testWithSocketTimeoutOnFirstExec() {
		
		TestStep step1 = new TestStep("step 10", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction(String.format("played %d times", count), false, new ArrayList<>()));
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		
		if (!failed) {
			failed = true;
			throw new WebDriverException("Session [6919ba25-53b6-4615-bd59-e97399bf1e12] was terminated due to SO_TIMEOUT");
		}
	}
	
	
	@Test(groups="stub", testName="A test which is <OK> é&")
	public void testOkWithTestName() throws IOException {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
	}
	
	@DataProvider(name = "data2")
	 public Object[][] data2() {
		return new String[][] {new String[] {"data2", "data3"}, new String[] {"data4", "data5"}};
	}
	@DataProvider(name = "data3")
	public Object[][] data3() {
		return new Integer[][] {new Integer[] {12, 123456}, 
			new Integer[] {13, 12345},
		new Integer[] {14, null}
		};
	}
	
	@Test(groups="stub", testName="A test which is OK (${arg0}, ${arg1})", dataProvider = "data2")
	public void testOkWithTestNameAndDataProvider(String col1, String col2) throws IOException {
		logger.info(String.format("%s,%s", col1, col2));
	}
	
	@Test(groups="stub", dataProvider = "data3")
	public void testOkWithPasswordDataProvider(Integer col1, @Mask Integer sensibleData) throws IOException {
		logger.info(String.format("%d,%d", col1, sensibleData == null ? -1: sensibleData));
		
		new CalcPage()
			.add(1, sensibleData);
	}

	@Test(groups="stub")
	public void testOk() throws IOException {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
	}
	

	@Test(groups="stub", description="a test with infos")
	public void testWithInfo1() throws IOException {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		TestStepManager.logTestStep(step1);

		addTestInfo("bugé <\"ID\">", new StringInfo("12"));
	}
	
	@Test(groups="stub", description="a test with infos")
	public void testWithInfo2() throws IOException {
		TestStep step1 = new TestStep("step 1", Reporter.getCurrentTestResult(), new ArrayList<>(), maskPassword);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		
		addTestInfo("user ID", new HyperlinkInfo("12345", "http://foo/bar/12345"));
	}
	
	@Test(groups="stub", dataProvider = "dataset")
	public void testStandardDataProvider(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}
	@Test(groups="stub", dataProvider = "datasetSemicolon")
	public void testStandardDataProviderSemicolon(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}
	@Test(groups="stub", dataProvider = "datasetWithHeader")
	public void testStandardDataProviderWithHeader(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}
	@Test(groups="stub", dataProvider = "datasetSemicolonWithHeader")
	public void testStandardDataProviderSemicolonWithHeader(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}

	@Test(groups="stub", dataProvider = "dataset")
	public void testStandardXlsxDataProvider(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}
	
	@Test(groups="stub", dataProvider = "dataset")
	public void testStandardDataProviderNoFile(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}
	
	@Test(groups="stub")
	public void testLogSameInfoMultipleTimes() {
		for (int i=0; i < 20; i++) {
			logger.info("something interesting");
			WaitHelper.waitForMilliSeconds(50);
		}
		for (int i=0; i < 2; i++) {
			logger.info("something else interesting");
			WaitHelper.waitForMilliSeconds(100);
		}
		
	}
	
	@Test(groups="stub")
	public void testLogSameInfoMultipleTimesLong() {
		for (int i=0; i < 15; i++) {
			logger.info("something interesting");
			WaitHelper.waitForSeconds(5);
		}
		
	}
	
	@Test(groups="stub")
	public void testLogSameInfoMultipleTimes2() {
		for (int i=0; i < 15; i++) {
			logger.info("something interesting");
			WaitHelper.waitForMilliSeconds(50);
		}
		for (int i=0; i < 3; i++) {
			logger.info("something else interesting");
			WaitHelper.waitForMilliSeconds(100);
		}
		
	}
}
