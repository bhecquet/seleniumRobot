package com.seleniumtests.connectors.tms.squash;

import io.github.bhecquet.SquashTMApi;
import io.github.bhecquet.entities.*;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.ITestResult;

import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            project = Project.get(projectName);
            Integer testId = getTestCaseId(testResult);
            if (testId == null) {
                logger.warn("Results won't be recorded, no testId configured for {}", TestNGResultUtils.getTestName(testResult));
                return;
            }
            Integer datasetId = getDatasetId(testResult);

            Campaign campaign = getOrCreateCampaign(testResult);
            Iteration iteration = getOrCreateIteration(testResult, campaign);

            IterationTestPlanItem tpi = iteration.addTestCase(testId, datasetId);

            applyExecutionResult(sapi, tpi, testResult);

        } catch (Exception e) {
            logger.error(String.format("Could not record result for test method %s: %s", TestNGResultUtils.getTestName(testResult), e.getMessage()));
        }
    }

    private Campaign getOrCreateCampaign(ITestResult testResult) {
        String campaignName;
        if (TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignName() != null) {
            campaignName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignName();
        } else {
            campaignName = "Selenium " + testResult.getTestContext().getName();
        }

        Campaign campaign = campaignCache.get(campaignName);
        if (campaign == null) {
            campaign = Campaign.create(project, campaignName, TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getCampaignFolderPath(), new HashMap<>());
            campaignCache.put(campaignName, campaign);
        }
        return campaign;
    }

    private Iteration getOrCreateIteration(ITestResult testResult, Campaign campaign) {
        String iterationName;
        if (TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getIterationName() != null) {
            iterationName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).testManager().getIterationName();
        } else {
            iterationName = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getApplicationVersion();
        }

        Iteration iteration = iterationCache.get(iterationName);
        if (iteration == null) {
            iteration = Iteration.create(campaign, iterationName);
            iterationCache.put(iterationName, iteration);
        }
        return iteration;
    }

    private void applyExecutionResult(SquashTMApi sapi, IterationTestPlanItem tpi, ITestResult testResult) {
        if (testResult.isSuccess()) {
            recordSuccessResult(sapi, tpi);
        } else if (testResult.getStatus() == 2) { // failed
            sapi.setExecutionResult(tpi, TestPlanItemExecution.ExecutionStatus.FAILURE); //comment is set in setExecutionStepStatus
            setExecutionStepStatus(tpi, testResult);
        } else { // skipped or other reason
            sapi.setExecutionResult(tpi, TestPlanItemExecution.ExecutionStatus.BLOCKED);
        }
    }

    private void recordSuccessResult(SquashTMApi sapi, IterationTestPlanItem tpi) {
        sapi.setExecutionResult(tpi, TestPlanItemExecution.ExecutionStatus.SUCCESS);
        tpi.completeDetails();
        Execution lastExecution = getLastExecution(tpi);
        if (lastExecution != null) {
            for (ExecutionStep es : lastExecution.getExecutionSteps()) {
                es.setStatus(TestPlanItemExecution.ExecutionStatus.SUCCESS);
            }
        }
    }

    public void setExecutionStepStatus(IterationTestPlanItem tpi, ITestResult testResult) {
        tpi.completeDetails();
        Execution lastExecution = getLastExecution(tpi);
        if (lastExecution == null) {
            return;
        }

        List<TestStep> resultTestStepList = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
        List<ExecutionStep> squashTestStepList = lastExecution.getExecutionSteps();

        if (!getUpdateTestManager(testResult)) {
            markAllStepsBlockedExceptFirstFailure(squashTestStepList);
        } else {
            // Match squash steps with result steps by name pattern and propagate status.
            // On mismatch: mark as FAILURE (or BLOCKED if a previous step already failed), then block all remaining steps.
            int maxIndex = Math.min(squashTestStepList.size(), resultTestStepList.size());
            int index = propagateStepStatuses(squashTestStepList, resultTestStepList, testResult, maxIndex);

            // Block all remaining squash steps after a mismatch or when result steps are exhausted
            int startBlock = index < maxIndex ? index + 1 : index;
            blockSteps(squashTestStepList, startBlock);
        }
    }

    private void markAllStepsBlockedExceptFirstFailure(List<ExecutionStep> squashTestStepList) {
        for (ExecutionStep es : squashTestStepList) {
            es.setStatus(TestPlanItemExecution.ExecutionStatus.BLOCKED);
        }
        squashTestStepList.getFirst().setStatus(TestPlanItemExecution.ExecutionStatus.FAILURE);
    }

    private int propagateStepStatuses(List<ExecutionStep> squashTestStepList, List<TestStep> resultTestStepList, ITestResult testResult, int maxIndex) {
        boolean stepKO = false;
        int index;
        for (index = 0; index < maxIndex; index++) {
            ExecutionStep squashStep = squashTestStepList.get(index);
            squashStep.completeDetails();
            TestStep resultStep = resultTestStepList.get(index);

            if (!stepNamesMatch(squashStep, resultStep)) {
                handleStepNameMismatch(squashStep, stepKO, testResult);
                break;
            }

            stepKO = applyStepStatus(squashStep, resultStep, testResult, stepKO);
        }
        return index;
    }

    private boolean stepNamesMatch(ExecutionStep squashStep, TestStep resultStep) {
        return squashStep.getName().replace("<p>", "").replace("</p>", "").matches(resultStep.getId() + " - .*");
    }

    private void handleStepNameMismatch(ExecutionStep squashStep, boolean stepKO, ITestResult testResult) {
        // Name mismatch: mark as FAILURE if no prior KO, otherwise BLOCKED
        squashStep.setStatus(stepKO
                ? TestPlanItemExecution.ExecutionStatus.BLOCKED
                : TestPlanItemExecution.ExecutionStatus.FAILURE);
        if (!stepKO) {
            squashStep.setComment(getThrowableMessage(testResult));
        }
    }

    private boolean applyStepStatus(ExecutionStep squashStep, TestStep resultStep, ITestResult testResult, boolean stepKO) {
        squashStep.setStatus(switch (resultStep.getStepStatus()) {
            case TestStep.StepStatus.FAILED -> {
                squashStep.setComment(getThrowableMessage(testResult));
                yield TestPlanItemExecution.ExecutionStatus.FAILURE;
            }
            case TestStep.StepStatus.SUCCESS -> TestPlanItemExecution.ExecutionStatus.SUCCESS;
            default -> TestPlanItemExecution.ExecutionStatus.BLOCKED;
        });
        return stepKO || resultStep.getStepStatus() == TestStep.StepStatus.FAILED;
    }

    private String getThrowableMessage(ITestResult testResult) {
        return testResult.getThrowable() != null ? testResult.getThrowable().getMessage() : null;
    }

    private void blockSteps(List<ExecutionStep> squashTestStepList, int from) {
        for (int remaining = from; remaining < squashTestStepList.size(); remaining++) {
            squashTestStepList.get(remaining).setStatus(TestPlanItemExecution.ExecutionStatus.BLOCKED);
        }
    }

    public Execution getLastExecution(IterationTestPlanItem tpi) {
        List<Execution> allExe = tpi.getExecutions();
        for (Execution exe : allExe) {
            if (Objects.equals(exe.getLastExecutedOn(), tpi.getLastExecutedOn())) {
                exe.completeDetails();
                return exe;
            }
        }
        return null;
    }

    @Override
    public void updateTestCase(ITestResult testResult) {

        try {
            if (!getUpdateTestManager(testResult)) {
                return;
            }
            Integer testCaseId = getTestCaseId(testResult);
            if (testCaseId == null) {
                logger.warn("Test Case won't be updated, no testCaseId configured for {}", TestNGResultUtils.getTestName(testResult));
                return;
            }

            TestCase testCase = TestCase.get(testCaseId);
            testCase.completeDetails();
            updateTestCaseDescription(testResult, testCase);
            deleteExistingTestSteps(testCase);
            createTestSteps(testResult, testCase);

        } catch (Exception e) {
            logger.error(String.format("Could not update Test Case for test method %s: %s", TestNGResultUtils.getTestName(testResult), e.getMessage()));
        }
    }

    private void updateTestCaseDescription(ITestResult testResult, TestCase testCase) {
        Map<String, Object> testCaseUpdatedDatas = new HashMap<>();
        testCaseUpdatedDatas.put("description", testResult.getMethod().getDescription());
        testCase.update(testCase.getId(), testCaseUpdatedDatas);
    }

    private void deleteExistingTestSteps(TestCase testCase) {
        //Delete all steps in test case
        //get all ids
        List<String> oldStepsIds = new ArrayList<>();
        for (io.github.bhecquet.entities.TestStep oldTestStep : testCase.getTestSteps()) {
            oldStepsIds.add(String.valueOf(oldTestStep.getId()));
        }
        //call squash API
        if (!oldStepsIds.isEmpty()) {
            io.github.bhecquet.entities.TestStep.delete(String.join(",", oldStepsIds));
        }
    }

    private void createTestSteps(ITestResult testResult, TestCase testCase) {
        List<TestStep> testStepList = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
        for (TestStep testStep : testStepList) {
            if (StringUtils.isBlank(testStep.getDescription())) {
                continue; // skip step if description is null or blank
            }
            createTestStep(testCase, testStep);
        }
    }

    private void createTestStep(TestCase testCase, TestStep testStep) {
        Map<String, Object> datas = new HashMap<>();
        datas.put("action", String.format("%s - %s", testStep.getId(), testStep.getDescription()));
        datas.put("expected_result", testStep.getExpectedResult());
        io.github.bhecquet.entities.TestStep newTestStep = io.github.bhecquet.entities.TestStep.create(testCase.getId(), datas);
        //add attachment if exists
        for (Snapshot snapshot : testStep.getSnapshots()) {
            if (snapshot.getCheckSnapshot() == SnapshotCheckType.NONE || snapshot.getCheckSnapshot() == SnapshotCheckType.FULL) {
                newTestStep.uploadAttachment(new File(snapshot.getScreenshot().getOutputDirectory() + "/" + snapshot.getScreenshot().getImagePath()), newTestStep.getId());
            }
        }
    }

	@Override
	public void recordResultFiles(ITestResult testResult) {
		// TODO Auto-generated method stub
		
	}

}
