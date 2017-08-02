package com.seleniumtests.connectors.selenium;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.BrowserType;

public class SeleniumRobotSnapshotServerConnector extends SeleniumRobotServerConnector {
	
	public static final String VERSION_API_URL = "/snapshot/api/version/";
	public static final String APPLICATION_API_URL = "/snapshot/api/application/";
	public static final String ENVIRONMENT_API_URL = "/snapshot/api/environment/";
	public static final String SESSION_API_URL = "/snapshot/api/session/";
	public static final String TESTCASE_API_URL = "/snapshot/api/testcase/";
	public static final String TESTCASEINSESSION_API_URL = "/snapshot/api/testcaseinsession/";
	public static final String TESTSTEP_API_URL = "/snapshot/api/teststep/";
	public static final String STEPRESULT_API_URL = "/snapshot/api/stepresult/";
	public static final String SNAPSHOT_API_URL = "/snapshot/upload/image";
	private Integer applicationId;
	private Integer versionId;
	private Integer environmentId;
	private Integer sessionId;
	private String sessionUUID;
	private Integer testCaseId;
	private Integer testCaseInSessionId;
	private Integer testStepId;
	private Integer stepResultId;
	private Integer snapshotId;

	public SeleniumRobotSnapshotServerConnector() {
		super();
		if (!active) {
			return;
		}
		active = isAlive();
	}
	
	@Override
	protected boolean isAlive() {
		return isAlive("/snapshot/");
	}

	public void createApplication() {
		if (!active) {
			return;
		}
		try {
			JSONObject applicationJson = getJSonResponse(Unirest.post(url + APPLICATION_API_URL)
					.field("name", SeleniumTestsContextManager.getApplicationName()));
			applicationId = applicationJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create application", e);
		}
	}
	
	public void createVersion() {
		if (!active) {
			return;
		}
		if (applicationId == null) {
			createApplication();
		}
		try {
			JSONObject versionJson = getJSonResponse(Unirest.post(url + VERSION_API_URL)
					.field("name", SeleniumTestsContextManager.getApplicationVersion())
					.field("application", applicationId));
			versionId = versionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create version", e);
		}
	}
	
	public void createEnvironment() {
		if (!active) {
			return;
		}
		try {
			JSONObject envJson = getJSonResponse(Unirest.post(url + ENVIRONMENT_API_URL)
					.field("name", SeleniumTestsContextManager.getThreadContext().getTestEnv()));
			environmentId = envJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create environment", e);
		}
	}
	
	public void createSession() {
		if (!active) {
			return;
		}
		if (environmentId == null) {
			createEnvironment();
		}
		if (versionId == null) {
			createVersion();
		}
		try {
			BrowserType browser = SeleniumTestsContextManager.getThreadContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			sessionUUID = UUID.randomUUID().toString();
			
			JSONObject sessionJson = getJSonResponse(Unirest.post(url + SESSION_API_URL)
					.field("sessionId", sessionUUID)
					.field("date", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
					.field("browser", browser.getBrowserType())
					.field("environment", SeleniumTestsContextManager.getThreadContext().getTestEnv())
					.field("version", versionId)
					.field("compareSnapshot", SeleniumTestsContextManager.getThreadContext().getCompareSnapshot()));
			sessionId = sessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create session", e);
		}
	}
	
	/**
	 * Create test case and add it to the current session
	 */
	public void createTestCase(String testName) {
		if (!active) {
			return;
		}
		if (applicationId == null) {
			createApplication();
		}

		try {
			JSONObject testJson = getJSonResponse(Unirest.post(url + TESTCASE_API_URL)
					.field("name", testName)
					.field("application", applicationId));
			testCaseId = testJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test case", e);
		}
	}
	
	/**
	 * Create link between test case and session
	 */
	public void createTestCaseInSession() {
		if (!active) {
			return;
		}
		if (sessionId == null) {
			createSession();
		}
		if (testCaseId == null) {
			throw new ConfigurationException("Test case must be previously defined");
		}
		try {
			JSONObject testInSessionJson = getJSonResponse(Unirest.post(url + TESTCASEINSESSION_API_URL)
					.field("testCase", testCaseId)
					.field("session", sessionId));
			testCaseInSessionId = testInSessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test case", e);
		}
	}
	

	/**
	 * Create test step and add it to the current test case
	 */
	public void createTestStep(String testStep) {
		if (!active) {
			return;
		}
		try {
			JSONObject stepJson = getJSonResponse(Unirest.post(url + TESTSTEP_API_URL)
					.field("name", testStep));
			testStepId = stepJson.getInt("id");
			addCurrentTestStepToTestCase();
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test step", e);
		}
	}
	
	/**
	 * Create snapshot
	 */
	public void createSnapshot(File pictureFile) {
		if (!active) {
			return;
		}
		if (sessionId == null) {
			createSession();
		}
		if (testCaseInSessionId == null) {
			createTestCaseInSession();
		}
		if (stepResultId == null) {
			throw new ConfigurationException("Step result must be previously recorded");
		}
		try {
			snapshotId = null;
			getJSonResponse(Unirest.post(url + SNAPSHOT_API_URL)
					.field("stepResult", stepResultId)
					.field("sessionId", sessionUUID)
					.field("testCase", testCaseInSessionId)
					.field("image", pictureFile)
					);
			snapshotId = 0; // for test only
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}
	
	/**
	 * Record step result
	 */
	public void recordStepResult(Boolean result, String logs) {
		if (!active) {
			return;
		}
		if (sessionId == null) {
			createSession();
		}
		if (testCaseInSessionId == null) {
			createTestCaseInSession();
		}
		if (testStepId == null) {
			throw new ConfigurationException("Test step and test case in session must be previously defined");
		}
		try {
			stepResultId = null;
			JSONObject resultJson = getJSonResponse(Unirest.post(url + STEPRESULT_API_URL)
					.field("step", testStepId)
					.field("testCase", testCaseInSessionId)
					.field("result", result)
					.field("stacktrace", logs)
					);
			stepResultId = resultJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}

	/**
	 * Returns list of test steps in a test case
	 * @return
	 */
	public List<String> getStepListFromTestCase() {
		if (testCaseInSessionId == null) {
			return new ArrayList<>();
		}
		
		try {

			JSONObject sessionJson = getJSonResponse(Unirest.get(url + TESTCASEINSESSION_API_URL + testCaseInSessionId));
			return sessionJson.getJSONArray("testSteps")
					.toList()
					.stream()
					.map(Object::toString)
					.collect(Collectors.toList());

		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot get test step list", e);
		}
	}
	
	/**
	 * Add the current test case (should have been previously created) to this test session
	 */
	public void addCurrentTestStepToTestCase() {
		if (testStepId == null || testCaseInSessionId == null) {
			throw new ConfigurationException("Test step and Test case in session must be previously created");
		}
		
		try {
			// get list of tests associated to this session
			List<String> testSteps = getStepListFromTestCase();
			if (!testSteps.contains(testStepId.toString())) {
				testSteps.add(testStepId.toString());
			}
			addTestStepsToTestCases(testSteps);
			
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot add test step to test case", e);
		}
	}
	
	public JSONObject addTestStepsToTestCases(List<String> testSteps) throws UnirestException {
		if (testSteps.isEmpty()) {
			return new JSONObject();
		}
		
		MultipartBody request = Unirest.patch(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field("testSteps", testSteps.get(0));
		for (String tc: testSteps.subList(1, testSteps.size())) {
			request = request.field("testSteps", tc);
		}
		return getJSonResponse(request);
	}
	
	public void addLogsToTestCaseInSession(String logs) {
		if (testCaseInSessionId == null) {
			createTestCaseInSession();
		}
		
		try {
			getJSonResponse(Unirest.patch(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field("stacktrace", logs));
		} catch (UnirestException e) {
			throw new SeleniumRobotServerException("cannot add logs to test case", e);
		}
	}
	
	private JSONObject getJSonResponse(BaseRequest request) throws UnirestException {
		HttpResponse<String> response = request.asString();
		
		if (response.getStatus() >= 400) {
			throw new UnirestException(String.format("request to %s failed: %s", request.getHttpRequest().getUrl(), response.getStatusText()));
		}
		
		if (response.getStatus() == 204) {
			return new JSONObject();
		}
		
		return new JSONObject(response.getBody());
	}
	

	public Integer getApplicationId() {
		return applicationId;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public Integer getTestCaseId() {
		return testCaseId;
	}

	public Integer getTestStepId() {
		return testStepId;
	}

	public Integer getSnapshotId() {
		return snapshotId;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public Integer getTestCaseInSessionId() {
		return testCaseInSessionId;
	}

	public Integer getStepResultId() {
		return stepResultId;
	}
}
