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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.it.core.aspects.CalcPage;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestTestLogging extends ReporterTest {

	@BeforeMethod(groups={"ut"})
	public void reset() {
		TestLogging.reset();
	}
	
	/**
	 * Check SeleniumRobot creates log file
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkFileLogger() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassWithWait"});
		Assert.assertTrue(new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory() + "/seleniumRobot.log").isFile());
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file
	 * One test at a time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkLogParsing() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassWithWait"});
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test1").contains("test1 finished"));	
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file
	 * Several tests run at the same time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkLogParsingWithThreads() throws Exception {
		executeSubTest(3, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassWithWait"});
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test1").contains("test1 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test2").contains("test2 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test3").contains("test3 finished"));	
	}
	
	@Test(groups = { "it" })
	public void checkLogParsingWithSeveralThreadsPerTest() throws Exception {
		executeSubTest(2, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassWithWait"});
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test1").contains("test1 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test2").contains("test2 finished"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("test3").contains("test3 finished"));	
	}
	
	@Test(groups = { "it" })
	public void checkLogParsingWithRetry() throws Exception {
		executeSubTest(2, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassWithWait"});	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testSimulatingRetry").contains("TestLogging: [RETRYING] class com.seleniumtests.it.stubclasses.StubTestClassWithWait FAILED, Retrying 1 time"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testSimulatingRetry").contains("testSimulatingRetry starting"));	
	}
	
	/**
	 * Checks logs are correctly extracted from seleniumRobot.log file when test is executed with pages
	 * One test at a time
	 * @throws Exception
	 */
	@Test(groups = { "it" })
	public void checkTestStepHandling() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestSteps"});
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testPage").contains("Start method testPage"));	
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testPage").contains("TestLogging: tell me why"));
		
		// check log level is present
		Assert.assertTrue(SeleniumRobotLogger.getTestLogs().get("testPage").contains("INFO "));	
	}
	

	/**
	 * Check that manual steps create steps and no other steps are created
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testManualSteps(ITestContext testContext) throws Exception {
		
		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
		
		try {
			TestTasks.addStep("Tests start");
			CalcPage cPage = new CalcPage();
			
			TestTasks.addStep("assert exception");
			cPage.assertAction();
		} catch (AssertionError e) {}
		
		// equivalent of "SeleniumRobotTestListener.logLastStep()"
		TestTasks.addStep(null);
		
		List<TestStep> steps = TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult());
		Assert.assertEquals(steps.size(), 2);
	}
	
}
