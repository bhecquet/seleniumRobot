package com.seleniumtests.connectors.tms.squash;

import io.github.bhecquet.SquashTMApi;
import io.github.bhecquet.entities.*;
import org.json.JSONObject;
import org.testng.ITestResult;

import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

public class SquashTMConnector extends TestManager {
	
	public static final String SQUASH_ITERATION = "tms.squash.iteration";
	public static final String SQUASH_CAMPAIGN = "tms.squash.campaign";
	public static final String SQUASH_CAMPAIGN_FOLDER = "tms.squash.campaign.folder";

	private String user;
	private String password;
	private String serverUrl;
	private String projectName;
	private Project project;
	private SquashTMApi api;

	private Map<String, Campaign> campaignCache;
	private Map<String, Iteration> iterationCache;

	public SquashTMConnector() {
		// to be called with init method
		campaignCache = new HashMap<>();
		iterationCache = new HashMap<>();
	}

	/**
	 * Initialize a connection to Squash TM
	 * You can connect to API using
	 * - login/password (will be removed in 2026 on Squash TM)
	 * - API token: in this case, don't specify user, only token as password
	 *
	 * @param url		URL of the squash TM server
	 * @param user		User to connect with (when using login/password)
	 * @param password	Password to connect with (when using login/password) or API token
	 * @param project	The project to connect to on Squash TM
	 */
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
		

		if (serverUrlVar == null || projectVar == null || passwordVar == null) {
			throw new ConfigurationException(String.format("SquashTM access not correctly configured. Environment configuration must contain variables"
					+ " %s, %s, %s", TMS_SERVER_URL, TMS_PASSWORD, TMS_PROJECT));
		}
		
		serverUrl = serverUrlVar;
		projectName = projectVar;
		user = userVar;
		password = passwordVar;
		
		initialized = true;

		// for tests
		if (campaignCache == null) campaignCache = new HashMap<>();
		if (iterationCache == null) iterationCache = new HashMap<>();
	}

	@Override
	public void logout() {
		// no logout

	}

	@Override
	public String getType() {
		return "squash";
	}

	@Override
	public String getTestCaseUrl(ITestResult testResult) {
		Integer testCaseId = getTestCaseId(testResult);
		if (testCaseId != null) {
			return String.format("%s/test-case-workspace/test-case/%d", serverUrl, testCaseId);
		} else {
			return null;
		}
	}

	public SquashTMApi getApi() {
		if (api == null) {
			if (user == null) {
				api = new SquashTMApi(serverUrl, password, true);
			} else {
				api = new SquashTMApi(serverUrl, user, password, true);
			}
		}
		return api;
	}

	@Override
	public void recordResult(ITestResult testResult) {
		
		try {
			SquashTMApi sapi = getApi();
			Project project = Project.get(projectName);
			Integer testId = getTestCaseId(testResult);
			if (testId == null) {
				logger.warn("Results won't be recorded, no testId configured for {}", TestNGResultUtils.getTestName(testResult));
				return;
			}
			Integer datasetId = getDatasetId(testResult);
	
			// campaign
			String campaignName;
			if (TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignName() != null) {
				campaignName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignName();
			} else {
				campaignName = "Selenium " + testResult.getTestContext().getName();
			}

			Campaign campaign;
			if (campaignCache.containsKey(campaignName) && campaignCache.get(campaignName) != null) {
				campaign = campaignCache.get(campaignName);
			} else {
				campaign = Campaign.create(project, campaignName, TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignFolderPath(), new HashMap<>());
				campaignCache.put(campaignName, campaign);
			}
			
			// iteration
			String iterationName;
			if (TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getIterationName() != null) {
				iterationName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getIterationName();
			} else {
				iterationName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getApplicationVersion();
			}

			Iteration iteration;
			if (iterationCache.containsKey(iterationName) && iterationCache.get(iterationName) != null) {
				iteration = iterationCache.get(iterationName);
			} else {
				iteration = Iteration.create(campaign, iterationName);
				iterationCache.put(iterationName, iteration);
			}
			
			IterationTestPlanItem tpi = iteration.addTestCase(testId, datasetId);
			
			
			if (testResult.isSuccess()) {
				sapi.setExecutionResult(tpi, TestPlanItemExecution.ExecutionStatus.SUCCESS);
			} else if (testResult.getStatus() == 2){ // failed
				String comment = null;
				if (testResult.getThrowable() != null) {
					comment = testResult.getThrowable().getMessage();
				}
				sapi.setExecutionResult(tpi, TestPlanItemExecution.ExecutionStatus.FAILURE, comment);
			} else { // skipped or other reason
				sapi.setExecutionResult(tpi, TestPlanItemExecution.ExecutionStatus.BLOCKED);
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
