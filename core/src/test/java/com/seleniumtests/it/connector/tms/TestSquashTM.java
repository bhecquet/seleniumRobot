package com.seleniumtests.it.connector.tms;

import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;

/**
 * Integration tests to be used with a local instance of Squash TM (version >= 1.21), installed with defaults credentials
 * @author S047432
 *
 */
public class TestSquashTM extends GenericTest {
	
	private String server = "https://localhost/squash";
	private String user = "user";
	private String password = "pwd";
	private String project = "myProject";

	@Test(groups="no-ti", enabled = true, attributes = {@CustomAttribute(name = "testId", values = "12")})
	public void testCreateCampaign(ITestContext testContext) {
		SquashTMConnector tm = new SquashTMConnector(server, user, password, project);
		SquashTMApi api = tm.getApi();
		Campaign campaign = api.createCampaign("AutoTest", "SubFolder/foo");
		Iteration iteration = api.createIteration(campaign, "myiteration");
		IterationTestPlanItem tpi = api.addTestCaseInIteration(iteration, 826791);
		api.setExecutionResult(tpi, ExecutionStatus.FAILURE, "oups");
		
		
	}
	
	/**
	 * Test recording of result using ITestResult
	 * You must set
	 * - squash URL
	 * - user
	 * - password
	 * - project name
	 * - testId in @Test annotation
	 * @param testContext
	 */
	@Test(groups="no-ti", enabled = true, attributes = {@CustomAttribute(name = "testId", values = "826791")}, expectedExceptions = WebDriverException.class)
	public void testRecordResult(ITestContext testContext) {
		SquashTMConnector tm = new SquashTMConnector(server, user, password, project);
		ITestResult testResult = Reporter.getCurrentTestResult();
		TestNGResultUtils.setSeleniumRobotTestContext(testResult, SeleniumTestsContextManager.getThreadContext());
		Throwable error = new WebDriverException("Oups browser");
		testResult.setThrowable(error);
		testResult.setStatus(ITestResult.FAILURE);
		tm.recordResult(testResult);
		
		
	}
}
