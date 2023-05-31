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
package com.seleniumtests.connectors.selenium;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openqa.selenium.Rectangle;
import org.testng.ITestResult;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.util.helper.WaitHelper;

import kong.unirest.HttpResponse;
import kong.unirest.MultipartBody;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import rp.com.google.common.io.Files;

public class SeleniumRobotSnapshotServerConnector extends SeleniumRobotServerConnector {
	
	private static final String NAPSHOT_DOES_NOT_EXIST_ERROR = "Provided snapshot does not exist";
	private static final String FIELD_IMAGE = "image";
	private static final String FIELD_IS_OK_WITH_SNAPSHOTS = "isOkWithSnapshots";
	private static final String FIELD_COMPUTING_ERROR = "computingError";
	private static final String FIELD_STEP = "step";
	private static final String FIELD_TEST_STEPS = "testSteps";
	private static final String FIELD_SESSION = "session";
	private static final String FIELD_TEST_CASE = "testCase";
	public static final String SESSION_API_URL = "/snapshot/api/session/";
	public static final String TESTCASEINSESSION_API_URL = "/snapshot/api/testcaseinsession/";
	public static final String TESTSTEP_API_URL = "/snapshot/api/teststep/";
	public static final String STEPRESULT_API_URL = "/snapshot/api/stepresult/";
	public static final String EXCLUDE_API_URL = "/snapshot/api/exclude/";
	public static final String SNAPSHOT_API_URL = "/snapshot/upload/image";
	public static final String STEP_REFERENCE_API_URL = "/snapshot/stepReference/";
	private String sessionUUID;
	private static SeleniumRobotSnapshotServerConnector snapshotConnector;

	protected static final int MAX_TESTSESSION_NAME_LENGHT = 100;
	protected static final int MAX_TESTCASEINSESSION_NAME_LENGHT = 100;
	protected static final int MAX_SNAPSHOT_NAME_LENGHT = 100;
	protected static final int MAX_TESTSTEP_NAME_LENGHT = 100;
	
	public enum SnapshotComparisonResult {
		OK,
		KO,
		NOT_DONE
	}
	
	public static SeleniumRobotSnapshotServerConnector getInstance() {
		if (snapshotConnector == null) {
			snapshotConnector = new SeleniumRobotSnapshotServerConnector(
					SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerActive(),
					SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerUrl(),
					SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerToken()
					);
		} 
		return snapshotConnector;
	}

	public SeleniumRobotSnapshotServerConnector(final boolean useRequested, final String url) {
		this(useRequested, url, null);
	}
	
	public SeleniumRobotSnapshotServerConnector(final boolean useRequested, final String url, String authToken) {
		super(useRequested, url, authToken);
		if (!active) {
			return; 
		}
		active = isAlive();
		if (active) {
			getInfoFromServer();
		}
	}
	
	@Override
	public boolean isAlive() {
		return isAlive("/snapshot/");
	}
	
	/**
	 * Create a test session
	 * @return
	 */
	public Integer createSession(String sessionName) {
		if (!active) {
			return null;
		}
		if (applicationId == null) {
			throw new SeleniumRobotServerException(String.format("Application %s has not been created", SeleniumTestsContextManager.getApplicationName()));
		}
		if (environmentId == null) {
			throw new SeleniumRobotServerException(String.format("Environment %s has not been created", SeleniumTestsContextManager.getGlobalContext().getTestEnv()));
		}
		if (versionId == null) {
			createVersion();
		}
		
		String strippedSessionName = sessionName.length() > MAX_TESTSESSION_NAME_LENGHT ? sessionName.substring(0, MAX_TESTSESSION_NAME_LENGHT): sessionName;
		
		try {
			BrowserType browser = SeleniumTestsContextManager.getGlobalContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			sessionUUID = UUID.randomUUID().toString(); // for uniqueness of the session
			
			JSONObject sessionJson = getJSonResponse(buildPostRequest(url + SESSION_API_URL)
					.field("sessionId", sessionUUID)
					.field("date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
					.field("browser", browser.getBrowserType())
					.field("environment", SeleniumTestsContextManager.getGlobalContext().getTestEnv())
					.field("version", versionId.toString())
					.field(FIELD_NAME, strippedSessionName)
					.field("compareSnapshot", String.valueOf(SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerCompareSnapshot()))
					.field("ttl", String.format("%d days", SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerCompareSnapshotTtl()))); // format is 'x days' as this is the way Django expect a duration in days
			return sessionJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create session", e);
		}
	}
	
	/**
	 * Create link between test case and session
	 * @param sessionId		the sessionId which should have been created before
	 * @param testCaseId	the test case Id to link to this session
	 * @return	the id of the created testCaseInSession
	 * @deprecated use the same method with name parameter
	 */
	@Deprecated
	public Integer createTestCaseInSession(Integer sessionId, Integer testCaseId) {
		return createTestCaseInSession(sessionId, testCaseId, "");
	}
	
	/**
	 * Create link between test case and session
	 * @param sessionId		the sessionId which should have been created before
	 * @param testCaseId	the test case Id to link to this session
	 * @param name			name of the test case in this session. This is to distinguish the test case (e.g: 'test1') and its full name (e.g: 'test1-1'), when executed with dataprovider
	 * @return	the id of the created testCaseInSession
	 */
	public Integer createTestCaseInSession(Integer sessionId, Integer testCaseId, String name) {
		if (!active) {
			return null;
		}
		if (sessionId == null) {
			throw new ConfigurationException("testcaseInSessionId should not be null");
		}
		if (testCaseId == null) {
			throw new ConfigurationException("Test case must be previously defined");
		}
		
		String strippedName = name.length() > MAX_TESTCASEINSESSION_NAME_LENGHT ? name.substring(0, MAX_TESTCASEINSESSION_NAME_LENGHT): name;
		
		try {
			JSONObject testInSessionJson = getJSonResponse(buildPostRequest(url + TESTCASEINSESSION_API_URL)
					.field(FIELD_TEST_CASE, testCaseId)
					.field(FIELD_SESSION, sessionId.toString())
					.field(FIELD_NAME, strippedName));
			return testInSessionJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create test case", e);
		}
	}
	
	/**
	 * Returns testStepName or a shorter version if it's too long
	 * @return
	 */
	private String getTestStepName(String testStepName) {
		return testStepName.length() > MAX_TESTSTEP_NAME_LENGHT ? testStepName.substring(0, MAX_TESTSTEP_NAME_LENGHT): testStepName;
	}
	

	/**
	 * Create test step and add it to the current test case
	 * @param testStep				name of the test step
	 * @param testCaseInSessionId	id of the test case in session, so that we can add this step to the test case
	 * @return	id of the created teststep
	 */
	public Integer createTestStep(String testStep, Integer testCaseInSessionId) {
		if (!active) {
			return null;
		}
		
		String strippedName = getTestStepName(testStep);
		
		try {
			JSONObject stepJson = getJSonResponse(buildPostRequest(url + TESTSTEP_API_URL)
					.field(FIELD_NAME, strippedName));
			Integer testStepId = stepJson.getInt("id");
			addCurrentTestStepToTestCase(testStepId, testCaseInSessionId);
			return testStepId;
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create test step", e);
		}
	}
	
	/**
	 * Get reference snapshot from server
	 * This is useful when a step fails and we want to get the reference to allow comparison
	 */
	public File getReferenceSnapshot(Integer stepResultId) {
		if (!active) {
			return null;
		}

		checkStepResult(stepResultId);
		
		try {

			File tmpFile = File.createTempFile("img", ".png");
			HttpResponse<byte[]> response = buildGetRequest(url + STEP_REFERENCE_API_URL + stepResultId + "/").asBytes();
			
			
			if (response.getStatus() == 200) {
				Files.write(response.getBody(), tmpFile);
				return tmpFile;
			} else {
				logger.warn("No reference found");
				return null;
			}
			
		} catch (UnirestException | SeleniumRobotServerException | IOException e) {
			throw new SeleniumRobotServerException("cannot get reference snapshot", e);
		}
	}

	/**
	 * @param stepResultId
	 */
	private void checkStepResult(Integer stepResultId) {
		if (stepResultId == null) {
			throw new ConfigurationException("Step result must be previously recorded");
		}
	}
	
	/**
	 * Create snapshot that shows the status of a step
	 */
	public void createStepReferenceSnapshot(Snapshot snapshot, Integer stepResultId) {
		if (!active) {
			return ;
		}

		checkStepResult(stepResultId);
		if (snapshot == null || snapshot.getScreenshot() == null || snapshot.getScreenshot().getFullImagePath() == null) {
			throw new SeleniumRobotServerException(NAPSHOT_DOES_NOT_EXIST_ERROR);
		}
		
		try {
			File pictureFile = new File(snapshot.getScreenshot().getFullImagePath());
			
			getJSonResponse(buildPostRequest(url + STEP_REFERENCE_API_URL)
					.field("stepResult", stepResultId)
					.field(FIELD_IMAGE, pictureFile)
					
					);
			
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create step reference snapshot", e);
		}
	}
	
	/**
	 * Send snapshot to server, for comparison, and check there is no difference with the reference picture
	 * This method will return true if
	 * - comparison is OK
	 * It returns null if:
	 * - server is inactive
	 * - computing error occurred
	 * - server side error ( e.g: if the server is not up to date)
	 */
	public SnapshotComparisonResult checkSnapshotHasNoDifferences(Snapshot snapshot, String testName, String stepName) {
		
		if (!active) {
			return SnapshotComparisonResult.NOT_DONE;
		}
		if (testName == null) {
			throw new ConfigurationException("testName must not be null");
		}
		if (stepName == null) {
			throw new ConfigurationException("stepName must not be null");
		}
		if (snapshot == null || snapshot.getScreenshot() == null || snapshot.getScreenshot().getFullImagePath() == null) {
			throw new SeleniumRobotServerException(NAPSHOT_DOES_NOT_EXIST_ERROR);
		}
		
		String snapshotName = snapshot.getName().length() > MAX_SNAPSHOT_NAME_LENGHT ? snapshot.getName().substring(0, MAX_SNAPSHOT_NAME_LENGHT): snapshot.getName(); 
		
		try {
			File pictureFile = new File(snapshot.getScreenshot().getFullImagePath());
			BrowserType browser = SeleniumTestsContextManager.getGlobalContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			String strippedTestName = getTestName(testName);
			String strippedStepName = getTestStepName(stepName);
			
			JSONObject snapshotJson = getJSonResponse(buildPutRequest(url + SNAPSHOT_API_URL)
					.socketTimeout(5000)
					.field(FIELD_IMAGE, pictureFile)
					.field(FIELD_NAME, snapshotName)
					.field("compare", snapshot.getCheckSnapshot().getName())
					.field("diffTolerance", String.valueOf(snapshot.getCheckSnapshot().getErrorThreshold()))
					.field("versionId", versionId.toString())
					.field("environmentId", environmentId.toString())
					.field("browser", browser.getBrowserType())
					.field("testCaseName", strippedTestName)
					.field("stepName", strippedStepName)
					);
			
			if (snapshotJson != null) {
				String computingError = snapshotJson.getString(FIELD_COMPUTING_ERROR);
				Float diffPixelPercentage = snapshotJson.getFloat("diffPixelPercentage");
				Boolean tooManyDiffs = snapshotJson.getBoolean("tooManyDiffs");
				
				if (!computingError.isEmpty()) {
					return SnapshotComparisonResult.NOT_DONE;
				} else if (Boolean.TRUE.equals(tooManyDiffs)) {
					logger.error(String.format("Snapshot comparison for %s has a difference of %.2f%% with reference", snapshot.getName(), diffPixelPercentage));
					return SnapshotComparisonResult.KO;
				} else {
					return SnapshotComparisonResult.OK;
				}
			} else {
				return SnapshotComparisonResult.NOT_DONE;
			}
			
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			// in case selenium server is not up to date, we shall not raise an error / retry
			logger.error("cannot send snapshot to server", e);
			return SnapshotComparisonResult.NOT_DONE;
		}
		
		
		
	}
	
	/**
	 * Create snapshot on server that will be used to show differences between 2 versions of the application
	 */
	public Integer createSnapshot(Snapshot snapshot, Integer sessionId, Integer testCaseInSessionId, Integer stepResultId) {
		if (!active) {
			return null;
		}
		if (sessionId == null) {
			throw new ConfigurationException("Session must be previously recorded");
		}
		if (testCaseInSessionId == null) {
			throw new ConfigurationException("TestCaseInSession must be previously recorded");
		}
		checkStepResult(stepResultId);
		if (snapshot == null || snapshot.getScreenshot() == null || snapshot.getScreenshot().getFullImagePath() == null) {
			throw new SeleniumRobotServerException(NAPSHOT_DOES_NOT_EXIST_ERROR);
		}
		
		String snapshotName = snapshot.getName().length() > MAX_SNAPSHOT_NAME_LENGHT ? snapshot.getName().substring(0, MAX_SNAPSHOT_NAME_LENGHT): snapshot.getName(); 
		
		try {
			File pictureFile = new File(snapshot.getScreenshot().getFullImagePath());
			
			JSONObject snapshotJson = getJSonResponse(buildPostRequest(url + SNAPSHOT_API_URL)
					.field("stepResult", stepResultId)
					.field("sessionId", sessionUUID)
					.field(FIELD_TEST_CASE, testCaseInSessionId.toString())
					.field(FIELD_IMAGE, pictureFile)
					.field(FIELD_NAME, snapshotName)
					.field("compare", snapshot.getCheckSnapshot().getName())
					.field("diffTolerance", String.valueOf(snapshot.getCheckSnapshot().getErrorThreshold()))
					);
			return snapshotJson.getInt("id");
			
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}
	
	/**
	 * Send exclude zones, stored in snapshot to the server
	 */
	public Integer createExcludeZones(Rectangle excludeZone, Integer snapshotId) {
		if (!active) {
			return null;
		}
		if (snapshotId == null) {
			throw new ConfigurationException("snapshotId must be provided");
		}

		try {
			JSONObject excludeJson = getJSonResponse(buildPostRequest(url + EXCLUDE_API_URL)
					.field("snapshot", snapshotId)
					.field("x", String.valueOf(excludeZone.x))
					.field("y", String.valueOf(excludeZone.y))
					.field("width", String.valueOf(excludeZone.width))
					.field("height", String.valueOf(excludeZone.height))
					);
			return excludeJson.getInt("id");
			
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create exclude zone", e);
		}
	}
	
	/**
	 * Record step result
	 * @param result	step result (true of false)
	 * @param logs		step details
	 * @param duration	step duration in milliseconds
	 * @return the stepResult id stored on server
	 */
	public Integer recordStepResult(Boolean result, String logs, long duration, Integer sessionId, Integer testCaseInSessionId, Integer testStepId) {
		if (!active) {
			return null;
		}
		if (sessionId == null) {
			throw new ConfigurationException("Test session must be previously defined");
		}
		if (testCaseInSessionId == null) {
			throw new ConfigurationException("TestCaseInSession must be previously defined");
		}
		if (testStepId == null) {
			throw new ConfigurationException("Test step and test case in session must be previously defined");
		}
		try {
			JSONObject resultJson = getJSonResponse(buildPostRequest(url + STEPRESULT_API_URL)
					.field(FIELD_STEP, testStepId)
					.field(FIELD_TEST_CASE, testCaseInSessionId.toString())
					.field("result", result.toString())
					.field("duration", String.valueOf(duration))
					.field("stacktrace", logs)
					);
			return resultJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}

	/**
	 * Returns list of test steps in a test case
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getStepListFromTestCase(Integer testCaseInSessionId) {
		if (testCaseInSessionId == null) {
			return new ArrayList<>();
		}
		
		try {

			JSONObject sessionJson = getJSonResponse(buildGetRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId));
			return (List<String>) sessionJson.getJSONArray(FIELD_TEST_STEPS)
					.toList()
					.stream()
					.map(Object::toString)
					.collect(Collectors.toList());

		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot get test step list", e);
		}
	}
	
	/**
	 * Add the current test case (should have been previously created) to this test session
	 */
	public void addCurrentTestStepToTestCase(Integer testStepId, Integer testCaseInSessionId) {
		if (testStepId == null || testCaseInSessionId == null) {
			throw new ConfigurationException("Test step and Test case in session must be previously created");
		}
		
		try {
			// get list of tests associated to this session
			List<String> testSteps = getStepListFromTestCase(testCaseInSessionId);
			if (!testSteps.contains(testStepId.toString())) {
				testSteps.add(testStepId.toString());
			}
			addTestStepsToTestCases(testSteps, testCaseInSessionId);
			
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot add test step to test case", e);
		}
	}
	
	public JSONObject addTestStepsToTestCases(List<String> testSteps, Integer testCaseInSessionId)  {
		if (testSteps.isEmpty()) {
			return new JSONObject();
		}
		
		MultipartBody request = buildPatchRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field(FIELD_TEST_STEPS, testSteps.get(0));
		for (String tc: testSteps.subList(1, testSteps.size())) {
			request = request.field(FIELD_TEST_STEPS, tc);
		}
		return getJSonResponse(request);
	}
	
	public void addLogsToTestCaseInSession(Integer testCaseInSessionId, String logs) {
		if (testCaseInSessionId == null) {
			throw new ConfigurationException("testcaseInSessionId should not be null");
		}
		
		try {
			getJSonResponse(buildPatchRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field("stacktrace", logs));
		} catch (UnirestException e) {
			throw new SeleniumRobotServerException("cannot add logs to test case", e);
		}
	}
	
	public String getSessionUUID() {
		return sessionUUID;
	}
	
	/**
	 * Get the comparison result of snapshots. If we cannot get the information, return true
	 * @param testCaseInSessionId		id of the test case in this test sessions.
	 * @param errorMessage				the messages coming from server if some comparison error occurred (error during computing). 
	 * 									An empty errorMessage means all snapshots have been processed whatever the comparison result is
	 * @return							integer with test result. Values are the one from ITestResult
	 */
	public int getTestCaseInSessionComparisonResult(Integer testCaseInSessionId, StringBuilder errorMessage) {
		
		logger.info("Getting snapshot comparison result for " + testCaseInSessionId);
		try {
			JSONObject response = null;
			for (int i = 0; i < 5; i++) {
				response = getJSonResponse(buildGetRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId));
				
				// 'isOkWithSnapshots' can take 3 values
				// - 'true' if all comparison are OK
				// - 'false' if at least 1 comparison fails
				// - 'null' if all comparison failed to be computed (e.g: due to bug on server) or at least one comparison is OK but all others are not computed
				// So if no computing error is returned, having 'null' means that no snapshot has been sent to server (according to 'snapshotServer/models.py' code)
				if (response.optBoolean("computed", false) && response.has(FIELD_IS_OK_WITH_SNAPSHOTS)) {
					return displaySnapshotComparisonError(response, errorMessage);
					
				} else {
					WaitHelper.waitForSeconds(1);
				}
			}
			if (response != null) {
				logger.info("Comparison result took too long to compute");
				return displaySnapshotComparisonError(response, errorMessage);
			} else {
				logger.error("Comparison result is null, setting to 'true'");
				return ITestResult.SKIP;
			}
			
		} catch (UnirestException e) {
			logger.error("Cannot get comparison result for this test case", e);
			return ITestResult.SKIP;
		}
		
	}
	
	private int displaySnapshotComparisonError(JSONObject response, StringBuilder errorMessage) {
		if (!response.optBoolean(FIELD_IS_OK_WITH_SNAPSHOTS, false) 
				&& response.optJSONArray(FIELD_COMPUTING_ERROR) != null 
				&& !response.optJSONArray(FIELD_COMPUTING_ERROR).isEmpty()) {
			logger.error("Errors while computing snapshot comparisons: \n" + response.optJSONArray(FIELD_COMPUTING_ERROR).join("\n"));
			errorMessage.append(response.optJSONArray(FIELD_COMPUTING_ERROR).join("\n"));
		}
		
		if (response.has(FIELD_IS_OK_WITH_SNAPSHOTS)) {
			if (response.isNull(FIELD_IS_OK_WITH_SNAPSHOTS)) {
				return ITestResult.SKIP;
			} else {
				return response.getBoolean(FIELD_IS_OK_WITH_SNAPSHOTS) ? ITestResult.SUCCESS: ITestResult.FAILURE;
			}
		} else {
			return ITestResult.SKIP;
		}
	}

}
