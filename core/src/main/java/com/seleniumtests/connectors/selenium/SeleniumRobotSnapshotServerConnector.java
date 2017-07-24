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
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;

public class SeleniumRobotSnapshotServerConnector extends SeleniumRobotServerConnector {
	
	public static final String VERSION_API_URL = "/api/version/";
	public static final String APPLICATION_API_URL = "/api/application/";
	public static final String ENVIRONMENT_API_URL = "/api/environment/";
	public static final String SESSION_API_URL = "/api/session/";
	public static final String TESTCASE_API_URL = "/api/testcase/";
	public static final String TESTSTEP_API_URL = "/api/teststep/";
	public static final String SNAPSHOT_API_URL = "/upload/image";
	private Integer applicationId;
	private Integer versionId;
	private Integer environmentId;
	private Integer sessionId;
	private String sessionUUID;
	private Integer testCaseId;
	private Integer testStepId;
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
		return isAlive("/compare/");
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
			logger.error("cannot create application", e);
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
			logger.error("cannot create version", e);
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
			logger.error("cannot create environment", e);
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
					.field("version", versionId));
			sessionId = sessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create session", e);
		}
	}
	
	/**
	 * Create test case and add it to the current session
	 */
	public void createTestCase(String testName) {
		if (!active) {
			return;
		}
		if (versionId == null) {
			createVersion();
		}
		try {
			JSONObject testJson = getJSonResponse(Unirest.post(url + TESTCASE_API_URL)
					.field("name", testName)
					.field("version", versionId));
			testCaseId = testJson.getInt("id");
			addCurrentTestCaseToSession();
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create test case", e);
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
			logger.error("cannot create test step", e);
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
		if (testStepId == null && testCaseId == null) {
			throw new ConfigurationException("Test step and test case must be previously defined");
		}
		try {
			snapshotId = null;
			getJSonResponse(Unirest.post(url + SNAPSHOT_API_URL)
					.field("step", testStepId)
					.field("sessionId", sessionUUID)
					.field("testCase", testCaseId)
					.field("image", pictureFile)
					);
			snapshotId = 0; // for test only
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create test snapshot", e);
		}
	}
	
	public List<String> getTestListFromSession() {
		if (sessionId == null) {
			return new ArrayList<>();
		}
		
		try {
			
			JSONObject sessionJson = getJSonResponse(Unirest.get(url + SESSION_API_URL + sessionId));
			return sessionJson.getJSONArray("testCases")
					.toList()
					.stream()
					.map(Object::toString)
					.collect(Collectors.toList());
			
		} catch (UnirestException | JSONException e) {
			logger.error("cannot get test case list", e);
		}
		return new ArrayList<>();
	}

	/**
	 * Add the current test case (should have been previously created) to this test session
	 */
	public void addCurrentTestCaseToSession() {
		if (sessionId == null || testCaseId == null) {
			throw new ConfigurationException("Session and Test case must be previously created");
		}
		
		try {
			// get list of tests associated to this session
			List<String> testCases = getTestListFromSession();
			if (!testCases.contains(testCaseId.toString())) {
				testCases.add(testCaseId.toString());
			}
			addTestCasesToSession(testCases);
			
		} catch (UnirestException | JSONException e) {
			logger.error("cannot add test case to session", e);
		}
	}
	
	public JSONObject addTestCasesToSession(List<String> testCases) throws UnirestException {
		if (testCases.isEmpty()) {
			return new JSONObject();
		}
		
		MultipartBody request = Unirest.patch(url + SESSION_API_URL + sessionId + "/").field("testCases", testCases.get(0));
		for (String tc: testCases.subList(1, testCases.size())) {
			request = request.field("testCases", tc);
		}
		return getJSonResponse(request);
	}
	
	/**
	 * Returns list of test steps in a test case
	 * @return
	 */
	public List<String> getStepListFromTestCase() {
		if (testCaseId == null) {
			return new ArrayList<>();
		}
		
		try {

			JSONObject sessionJson = getJSonResponse(Unirest.get(url + TESTCASE_API_URL + testCaseId));
			return sessionJson.getJSONArray("testSteps")
					.toList()
					.stream()
					.map(Object::toString)
					.collect(Collectors.toList());

		} catch (UnirestException | JSONException e) {
			logger.error("cannot get test step list", e);
		}
		return new ArrayList<>();
	}
	
	/**
	 * Add the current test case (should have been previously created) to this test session
	 */
	public void addCurrentTestStepToTestCase() {
		if (testStepId == null || testCaseId == null) {
			throw new ConfigurationException("Test step and Test case must be previously created");
		}
		
		try {
			// get list of tests associated to this session
			List<String> testSteps = getStepListFromTestCase();
			if (!testSteps.contains(testStepId.toString())) {
				testSteps.add(testStepId.toString());
			}
			addTestStepsToTestCases(testSteps);
			
		} catch (UnirestException | JSONException e) {
			logger.error("cannot add test step to test case", e);
		}
	}
	
	public JSONObject addTestStepsToTestCases(List<String> testSteps) throws UnirestException {
		if (testSteps.isEmpty()) {
			return new JSONObject();
		}
		
		MultipartBody request = Unirest.patch(url + TESTCASE_API_URL + testCaseId + "/").field("testSteps", testSteps.get(0));
		for (String tc: testSteps.subList(1, testSteps.size())) {
			request = request.field("testSteps", tc);
		}
		return getJSonResponse(request);
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
}
