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
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Rectangle;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumRobotSnapshotServerConnector extends SeleniumRobotServerConnector {
	
	public static final String SESSION_API_URL = "/snapshot/api/session/";
	public static final String TESTCASEINSESSION_API_URL = "/snapshot/api/testcaseinsession/";
	public static final String TESTSTEP_API_URL = "/snapshot/api/teststep/";
	public static final String STEPRESULT_API_URL = "/snapshot/api/stepresult/";
	public static final String EXCLUDE_API_URL = "/snapshot/api/exclude/";
	public static final String SNAPSHOT_API_URL = "/snapshot/upload/image";
	private String sessionUUID;
	private static SeleniumRobotSnapshotServerConnector snapshotConnector;
	
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
			getInfoFromServer(null);
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
		try {
			BrowserType browser = SeleniumTestsContextManager.getGlobalContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			sessionUUID = UUID.randomUUID().toString(); // for uniqueness of the session
			
			JSONObject sessionJson = getJSonResponse(buildPostRequest(url + SESSION_API_URL)
					.field("sessionId", sessionUUID)
					.field("date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
					.field("browser", browser.getBrowserType())
					.field("environment", SeleniumTestsContextManager.getGlobalContext().getTestEnv())
					.field("version", versionId)
					.field("name", sessionName)
					.field("compareSnapshot", SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerCompareSnapshot())
					.field("ttl", SeleniumTestsContextManager.getGlobalContext().getSeleniumRobotServerCompareSnapshotTtl()));
			return sessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create session", e);
		}
	}
	
	/**
	 * Create link between test case and session
	 * @param sessionId		the sessionId which should have been created before
	 * @param testCaseId	the test case Id to link to this session
	 * @return	the id of the created testCaseInSession
	 */
	public Integer createTestCaseInSession(Integer sessionId, Integer testCaseId) {
		if (!active) {
			return null;
		}
		if (sessionId == null) {
			throw new ConfigurationException("testcaseInSessionId should not be null");
		}
		if (testCaseId == null) {
			throw new ConfigurationException("Test case must be previously defined");
		}
		try {
			JSONObject testInSessionJson = getJSonResponse(buildPostRequest(url + TESTCASEINSESSION_API_URL)
					.field("testCase", testCaseId)
					.field("session", sessionId));
			return testInSessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test case", e);
		}
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
		try {
			JSONObject stepJson = getJSonResponse(buildPostRequest(url + TESTSTEP_API_URL)
					.field("name", testStep));
			Integer testStepId = stepJson.getInt("id");
			addCurrentTestStepToTestCase(testStepId, testCaseInSessionId);
			return testStepId;
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test step", e);
		}
	}
	
	/**
	 * Create snapshot
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
		if (stepResultId == null) {
			throw new ConfigurationException("Step result must be previously recorded");
		}
		try {
			File pictureFile = new File(snapshot.getScreenshot().getFullImagePath());
			
			JSONObject snapshotJson = getJSonResponse(buildPostRequest(url + SNAPSHOT_API_URL)
					.field("stepResult", stepResultId)
					.field("sessionId", sessionUUID)
					.field("testCase", testCaseInSessionId)
					.field("image", pictureFile)
					.field("name", snapshot.getName())
					.field("compare", snapshot.getCheckSnapshot().getName())
					);
			Integer snapshotId = snapshotJson.getInt("id");
			
			
			return snapshotId;
		} catch (UnirestException | JSONException e) {
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
					.field("x", excludeZone.x)
					.field("y", excludeZone.y)
					.field("width", excludeZone.width)
					.field("height", excludeZone.height)
					);
			return excludeJson.getInt("id");
			
		} catch (UnirestException | JSONException e) {
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
					.field("step", testStepId)
					.field("testCase", testCaseInSessionId)
					.field("result", result)
					.field("duration", duration)
					.field("stacktrace", logs)
					);
			return resultJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}

	/**
	 * Returns list of test steps in a test case
	 * @return
	 */
	public List<String> getStepListFromTestCase(Integer testCaseInSessionId) {
		if (testCaseInSessionId == null) {
			return new ArrayList<>();
		}
		
		try {

			JSONObject sessionJson = getJSonResponse(buildGetRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId));
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
			
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot add test step to test case", e);
		}
	}
	
	public JSONObject addTestStepsToTestCases(List<String> testSteps, Integer testCaseInSessionId) throws UnirestException {
		if (testSteps.isEmpty()) {
			return new JSONObject();
		}
		
		MultipartBody request = buildPatchRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field("testSteps", testSteps.get(0));
		for (String tc: testSteps.subList(1, testSteps.size())) {
			request = request.field("testSteps", tc);
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
	 * @return							true if snapshot comparison is OK
	 */
	public boolean getTestCaseInSessionComparisonResult(Integer testCaseInSessionId) {
		
		try {
			JSONObject response = null;
			for (int i = 0; i < 3; i++) {
				response = getJSonResponse(buildGetRequest(url + TESTCASEINSESSION_API_URL + testCaseInSessionId));
				if (response.optBoolean("computed", false) && response.has("isOkWithSnapshots")) {
					return response.getBoolean("isOkWithSnapshots");
				} else {
					WaitHelper.waitForSeconds(1);
				}
			}
			return response.optBoolean("isOkWithSnapshots", true);
			
		} catch (UnirestException e) {
			logger.error("Cannot get comparison result for this test case. So result is expected to be OK", e);
			return true;
		}
		
	}

}
