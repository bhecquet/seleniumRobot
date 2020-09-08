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
	
	public static final String SQUASH_TM_SERVER_URL = "hpAlmServerUrl";
	public static final String SQUASH_TM_PASSWORD = "hpAlmPassword";
	public static final String SQUASH_TM_USER = "hpAlmUser";
	public static final String SQUASH_TM_PROJECT = "hpAlmProject";
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
		config.put(SQUASH_TM_SERVER_URL, url);
		config.put(SQUASH_TM_USER, user);
		config.put(SQUASH_TM_PASSWORD, password);
		config.put(SQUASH_TM_PROJECT, project);
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
		String serverUrlVar = connectParams.optString(SQUASH_TM_SERVER_URL, null);
		String projectVar = connectParams.optString(SQUASH_TM_PROJECT, null);
		String userVar = connectParams.optString(SQUASH_TM_USER, null);
		String passwordVar = connectParams.optString(SQUASH_TM_PASSWORD, null);
		

		if (serverUrlVar == null || projectVar == null || userVar == null || passwordVar == null) {
			throw new ConfigurationException(String.format("SquashTM access not correctly configured. Environment configuration must contain variables"
					+ "%s, %s, %s, %s", SQUASH_TM_SERVER_URL, SQUASH_TM_PASSWORD, SQUASH_TM_USER, SQUASH_TM_PROJECT));
		}
		
		serverUrl = serverUrlVar;
		project = projectVar;
		user = userVar;
		password = passwordVar;
		
		initialized = true;

		api = new SquashTMApi(serverUrl, user, password, project);
	}

	@Override
	public void logout() {
		// no logout

	}

	public SquashTMApi getApi() {
		return api;
	}

	@Override
	public void recordResult(ITestResult testResult) {
		
		try {
			Integer testId = TestNGResultUtils.getTestCaseId(testResult);
			if (testId == null) {
				return;
			}
	
			Campaign campaign = api.createCampaign("Selenium " + testResult.getTestName(), "");
			Iteration iteration = api.createIteration(campaign, TestNGResultUtils.getSeleniumRobotTestContext(testResult).getApplicationVersion());
			
			IterationTestPlanItem tpi = api.addTestCaseInIteration(iteration, testId);
			
			if (testResult.isSuccess()) {
				api.setExecutionResult(tpi, ExecutionStatus.SUCCESS);
			} else if (testResult.getStatus() == 2){ // failed
				api.setExecutionResult(tpi, ExecutionStatus.FAILURE);
			} else { // skipped or other reason
				api.setExecutionResult(tpi, ExecutionStatus.BLOCKED);
			}

		} catch (Exception e) {
			logger.error(String.format("Could not record result for test method %s", TestNGResultUtils.getTestName(testResult)), e);
		}
	}

	@Override
	public void recordResultFiles(ITestResult testResult) {
		// TODO Auto-generated method stub
		
	}

}
