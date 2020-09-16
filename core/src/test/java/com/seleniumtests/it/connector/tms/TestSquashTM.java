package com.seleniumtests.it.connector.tms;

import org.testng.ITestContext;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;

/**
 * Integration tests to be used with a local instance of Squash TM (version >= 1.21), installed with defaults credentials
 * @author S047432
 *
 */
public class TestSquashTM extends GenericTest {

	@Test(groups="no-ti", enabled = false, attributes = {@CustomAttribute(name = "testId", values = "12")})
	public void testCreateCampaign(ITestContext testContext) {
		SquashTMConnector tm = new SquashTMConnector("http://localhost:8080/squash", "admin", "admin", "Test Project-1");
		SquashTMApi api = tm.getApi();
		Campaign campaign = api.createCampaign("AutoTest", "SubFolder/foo");
		Iteration iteration = api.createIteration(campaign, "myiteration");
		IterationTestPlanItem tpi = api.addTestCaseInIteration(iteration, 239);
		api.setExecutionResult(tpi, ExecutionStatus.FAILURE);
	}
}
