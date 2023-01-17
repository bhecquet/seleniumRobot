package com.seleniumtests.connectors.tms.squash;

import org.json.JSONObject;
import org.testng.ITestResult;

import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;

public class SquashTMConnector extends TestManager {
	
	public static final String SQUASH_ITERATION = "tms.squash.iteration";
	public static final String SQUASH_CAMPAIGN = "tms.squash.campaign";

	private String user;
	private String password;
	private String serverUrl;
	private String project;
	private SquashTMApi api;
	
	public SquashTMConnector() {
		// to be called with init method
	}
	
	public SquashTMConnector(String url, String user, String password, String project) {
		this();
		JSONObject config = new JSONObject();
		config.put(TMS_SERVER_URL, url);
		config.put(TMS_USER, user);
		config.put(TMS_PASSWORD, password);
		config.put(TMS_PROJECT, project);
		init(config);
	}
	

	@Override
	public void recordResult() {
		// Nothing to do

	}

	@Override
	public void recordResultFiles() {
		// Nothing to do

	}

	@Override
	public void login() {
		// no login
	}

	@Override
	public void init(JSONObject connectParams) {
		String serverUrlVar = connectParams.optString(TMS_SERVER_URL, null);
		String projectVar = connectParams.optString(TMS_PROJECT, null);
		String userVar = connectParams.optString(TMS_USER, null);
		String passwordVar = connectParams.optString(TMS_PASSWORD, null);
		

		if (serverUrlVar == null || projectVar == null || userVar == null || passwordVar == null) {
			throw new ConfigurationException(String.format("SquashTM access not correctly configured. Environment configuration must contain variables"
					+ "%s, %s, %s, %s", TMS_SERVER_URL, TMS_PASSWORD, TMS_USER, TMS_PROJECT));
		}
		
		serverUrl = serverUrlVar;
		project = projectVar;
		user = userVar;
		password = passwordVar;
		
		initialized = true;

		
	}

	@Override
	public void logout() {
		// no logout

	}

	public SquashTMApi getApi() {
		if (api == null) {
			api = new SquashTMApi(serverUrl, user, password, project);
		}
		return api;
	}

	@Override
	public void recordResult(ITestResult testResult) {
		
		try {
			SquashTMApi sapi = getApi();
			Integer testId = getTestCaseId(testResult);
			if (testId == null) {
				return;
			}
	
			// campaign
			String campaignName;
			if (TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignName() != null) {
				campaignName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignName();
			} else {
				campaignName = "Selenium " + testResult.getTestContext().getName();
			}
			
			Campaign campaign = sapi.createCampaign(campaignName, "");
			
			// iteration
			String iterationName;
			if (TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getIterationName() != null) {
				iterationName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getIterationName();
			} else {
				iterationName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getApplicationVersion();
			}
			
			Iteration iteration = sapi.createIteration(campaign, iterationName);
			
			IterationTestPlanItem tpi = sapi.addTestCaseInIteration(iteration, testId);
			
			if (testResult.isSuccess()) {
				sapi.setExecutionResult(tpi, ExecutionStatus.SUCCESS);
			} else if (testResult.getStatus() == 2){ // failed
				sapi.setExecutionResult(tpi, ExecutionStatus.FAILURE);
			} else { // skipped or other reason
				sapi.setExecutionResult(tpi, ExecutionStatus.BLOCKED);
			}

		} catch (Exception e) {
			logger.error(String.format("Could not record result for test method %s: %s", TestNGResultUtils.getTestName(testResult), e.getMessage()));
		}
	}

	@Override
	public void recordResultFiles(ITestResult testResult) {
		// TODO Auto-generated method stub
		
	}

}
