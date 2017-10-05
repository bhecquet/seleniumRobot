package com.seleniumtests.connectors.selenium;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class SeleniumRobotServerConnector {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotServerConnector.class);

	// api to get items from name
	public static final String NAMED_VERSION_API_URL = "/commons/api/gversion/";
	public static final String NAMED_APPLICATION_API_URL = "/commons/api/gapplication/";
	public static final String NAMED_ENVIRONMENT_API_URL = "/commons/api/genvironment/";
	public static final String NAMED_TESTCASE_API_URL = "/commons/api/gtestcase/";
	
	public static final String VERSION_API_URL = "/commons/api/version/";
	public static final String APPLICATION_API_URL = "/commons/api/application/";
	public static final String ENVIRONMENT_API_URL = "/commons/api/environment/";
	public static final String TESTCASE_API_URL = "/commons/api/testcase/";

	protected String url;
	protected boolean active = false;
	protected Integer applicationId;
	protected Integer versionId;
	protected Integer environmentId;
	protected Integer testCaseId;
	
	public SeleniumRobotServerConnector() {
		active = isActive();
	}
	
	public abstract boolean isAlive();
	
	protected boolean isAlive(String testUrl) {
		Unirest.setTimeouts(1500, 1500);
		try {
			return Unirest.get(url + testUrl).asString().getStatus() == 200;
		} catch (UnirestException e) {
			return false;
		} finally {
			Unirest.setTimeouts(10000, 60000);
		}
	}
	
	protected boolean isActive() {
		if (SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerActive()) {
			url = SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerUrl();
			return true;
		} else {
			logger.warn("selenium server won't be used, key 'seleniumrobotServerActive' is not available in testng configuration or in environment variable.");
			return false;
		} 
	}
	
	/**
	 * Returns the versionId, environmentId and testCaseId from server
	 */
	protected void getInfoFromServer(String testName) {
		applicationId = getApplicationId();
		versionId = getVersionId();
		environmentId = getEnvironmentId();
		
		if (testName != null) {
			testCaseId = getTestCaseId(testName);
		}
	}
	
	/**
	 * Returns the application id in variable server, from the name of the tested application
	 * @throws ConfigurationException when environment does not exist to force user to register its variables
	 * @return
	 */
	public int getApplicationId() {
		if (applicationId != null) {
			return applicationId;
		}
		try {
			JSONObject response = getJSonResponse(Unirest.get(url + NAMED_APPLICATION_API_URL)
					.queryString("name", SeleniumTestsContextManager.getApplicationName()));
			applicationId = response.getInt("id");
			return applicationId;
		} catch (UnirestException e) {
			throw new ConfigurationException(String.format("Application %s does not exist in variable server, please create it", SeleniumTestsContextManager.getApplicationName()));
		}
	}
	
	/**
	 * Returns the environment id in variable server, from the name of the tested env
	 * @throws ConfigurationException when environment does not exist to force user to register its variables
	 * @return
	 */
	public int getEnvironmentId() {
		if (environmentId != null) {
			return environmentId;
		}
		try {
			JSONObject response = getJSonResponse(Unirest.get(url + NAMED_ENVIRONMENT_API_URL)
					.queryString("name", SeleniumTestsContextManager.getThreadContext().getTestEnv()));
			environmentId = response.getInt("id");
			return environmentId;
		} catch (UnirestException e) {
			throw new ConfigurationException(String.format("Environment %s does not exist in variable server, please create it or use an other one", SeleniumTestsContextManager.getThreadContext().getTestEnv()));
		}
	}
	
	/**
	 * Returns the version id in variable server. Create it if it does not exist
	 * @return
	 */
	public int getVersionId() {
		if (versionId != null) {
			return versionId;
		}
		
		createVersion();
		return versionId;
	}
	
	
	/**
	 * Returns the testcase id in variable server or create it if it does not exist
	 * @return
	 */
	public int getTestCaseId(String testName) {
		if (testCaseId != null) {
			return testCaseId;
		}
		createTestCase(testName);
		return testCaseId;
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
	 * create version
	 * If version name already exists on server, it's id will be returned. Else, a new one will be created
	 */
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
	
	protected JSONObject getJSonResponse(BaseRequest request) throws UnirestException {
		HttpResponse<String> response = request.asString();
		
		if (response.getStatus() >= 400) {
			throw new UnirestException(String.format("request to %s failed: %s", request.getHttpRequest().getUrl(), response.getStatusText()));
		}
		
		if (response.getStatus() == 204) {
			return new JSONObject();
		}
		
		return new JSONObject(response.getBody());
	}
	
	protected JSONArray getJSonArray(BaseRequest request) throws UnirestException {
		HttpResponse<String> response = request.asString();
		
		if (response.getStatus() >= 400) {
			throw new UnirestException(String.format("request to %s failed: %s", request.getHttpRequest().getUrl(), response.getStatusText()));
		}
		
		if (response.getStatus() == 204) {
			return new JSONArray();
		}
		
		return new JSONArray(response.getBody());
	}
	
	public boolean getActive() {
		return active;
	}

	/*
	 * For Test purpose only
	 */
	public void setApplicationId(Integer applicationId) {
		this.applicationId = applicationId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	public void setTestCaseId(Integer testCaseId) {
		this.testCaseId = testCaseId;
	}
}
