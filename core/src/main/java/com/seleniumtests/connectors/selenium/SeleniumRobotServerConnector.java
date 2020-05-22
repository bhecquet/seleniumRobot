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

import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.GetRequest;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public abstract class SeleniumRobotServerConnector {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotServerConnector.class);

	// api to get items from name
	public static final String NAMED_VERSION_API_URL = "/commons/api/gversion/";
	public static final String NAMED_APPLICATION_API_URL = "/commons/api/gapplication/";
	public static final String NAMED_ENVIRONMENT_API_URL = "/commons/api/genvironment/";
	public static final String NAMED_TESTCASE_API_URL = "/commons/api/gtestcase/";

	public static final String PING_API_URL = "/variable/api/";
	
	public static final String VERSION_API_URL = "/commons/api/version/";
	public static final String APPLICATION_API_URL = "/commons/api/application/";
	public static final String ENVIRONMENT_API_URL = "/commons/api/environment/";
	public static final String TESTCASE_API_URL = "/commons/api/testcase/";

	protected String url;
	protected String authToken;
	protected boolean active = false;
	protected boolean useRequested = false;
	protected Integer applicationId;
	protected Integer versionId;
	protected Integer environmentId;
	
	public SeleniumRobotServerConnector(boolean useRequested, String url) {
		this(useRequested, url, null);
	}
	public SeleniumRobotServerConnector(boolean useRequested, String url, String authToken) {
		this.useRequested = useRequested;
		this.url = url;
		this.authToken = authToken;
		
		if (this.authToken != null) {
			this.authToken = "Token " + authToken;
		}
		active = isActive();
	}
	
	public abstract boolean isAlive();
	
	/**
	 * Test if the server is alive
	 * @param testUrl	the URL to test for "alive". It MUST begin with "/" as root url only contains http://<host>:<port>
	 * @return
	 */
	protected boolean isAlive(String testUrl) {
		
		try (
			UnirestInstance unirest = Unirest.spawnInstance();
			) {
			
			HttpResponse<String> reply;
			if (authToken != null) {
				reply = unirest.get(url + testUrl).header("Authorization", authToken).asString();
			} else {
				reply = unirest.get(url + testUrl).asString();
			}
			int status = reply.getStatus();
	
			// server may be in HTTPS whereas we call it in HTTP
			// change base url and try again
			if ((status == 308 || status == 301) && !url.toLowerCase().startsWith("https")) {
				String newLocation = reply.getHeaders().getFirst("Location");
				url = newLocation.replace(testUrl, "");
				status = buildGetRequest(url + testUrl).asString().getStatus();
			}
			
			if (status == 401) {
				logger.error("-------------------------------------------------------------------------------------");
				logger.error("Access to seleniumRobot server unauthorized, access token must be provided");
				logger.error("Token can be set via 'seleniumRobotServerToken' parameter: '-DseleniumRobotServerToken=<token>");
				logger.error(String.format("Access token can be generated using your browser at %s/api-token-auth/?username=<user>&password=<password>", url));
				logger.error("-------------------------------------------------------------------------------------");
				throw new ConfigurationException("Access to seleniumRobot server unauthorized, access token must be provided by adding '-DseleniumRobotServerToken=<token>' to test command line");
			
			} else {
				return status == 200;
			}
		} catch (UnirestException e) {
			return false;
		} 
	}
	
	protected boolean isActive() {
		if (useRequested) {
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
			JSONObject response = getJSonResponse(buildGetRequest(url + NAMED_APPLICATION_API_URL)
					.queryString("name", SeleniumTestsContextManager.getApplicationName()));
			applicationId = response.getInt("id");
			return applicationId;
		} catch (UnirestException | SeleniumRobotServerException e) {
			throw new ConfigurationException(String.format("Application %s not get from variable server: %s", SeleniumTestsContextManager.getApplicationName(), e.getMessage()));
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
			JSONObject response = getJSonResponse(buildGetRequest(url + NAMED_ENVIRONMENT_API_URL)
					.queryString("name", SeleniumTestsContextManager.getGlobalContext().getTestEnv()));
			environmentId = response.getInt("id");
			return environmentId;
		} catch (UnirestException | SeleniumRobotServerException e) {
			throw new ConfigurationException(String.format("Environment %s not get from variable server: %s", SeleniumTestsContextManager.getGlobalContext().getTestEnv(), e.getMessage()));
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
	 * Create test case and add it to the current session
	 */
	public Integer createTestCase(String testName) {
		if (!active) {
			return null;
		}
		if (testName == null || testName.isEmpty()) {
			throw new ConfigurationException("testName must not be null or empty");
		}
		
		if (applicationId == null) {
			createApplication();
		}

		try {
			JSONObject testJson = getJSonResponse(buildPostRequest(url + TESTCASE_API_URL)
					.field("name", testName)
					.field("application", applicationId.toString()));
			return testJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
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
			JSONObject versionJson = getJSonResponse(buildPostRequest(url + VERSION_API_URL)
					.field("name", SeleniumTestsContextManager.getApplicationVersion())
					.field("application", applicationId.toString()));
			versionId = versionJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create version", e);
		}
	}
	
	public void createEnvironment() {
		if (!active) {
			return;
		}
		try {
			JSONObject envJson = getJSonResponse(buildPostRequest(url + ENVIRONMENT_API_URL)
					.field("name", SeleniumTestsContextManager.getGlobalContext().getTestEnv()));
			environmentId = envJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create environment", e);
		}
	}

	public void createApplication() {
		if (!active) {
			return;
		}
		try {
			JSONObject applicationJson = getJSonResponse(buildPostRequest(url + APPLICATION_API_URL)
					.field("name", SeleniumTestsContextManager.getApplicationName()));
			applicationId = applicationJson.getInt("id");
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot create application", e);
		}
	}
	
	protected GetRequest buildGetRequest(String url) {
		if (authToken != null) {
			return Unirest.get(url).header("Authorization", authToken);
		} else {
			return Unirest.get(url);
		}
	}
	
	protected HttpRequestWithBody buildPostRequest(String url) {
		if (authToken != null) {
			return Unirest.post(url).header("Authorization", authToken);
		} else {
			return Unirest.post(url);
		}
	}
	
	protected HttpRequestWithBody buildPatchRequest(String url) {
		if (authToken != null) {
			return Unirest.patch(url).header("Authorization", authToken);
		} else {
			return Unirest.patch(url);
		}
	}
	
	protected JSONObject getJSonResponse(HttpRequest request) {

		HttpResponse<String> response = request.asString();

		if (response.getStatus() == 423) {
			String error = new JSONObject(response.getBody()).getString("detail");
			throw new SeleniumRobotServerException(error);
		}
		
		if (response.getStatus() >= 400) {
			String error = "unknown";
			try {
				error = new JSONObject(response.getBody()).getString("detail");
			} catch (JSONException e) {
				throw new SeleniumRobotServerException(String.format("request to %s failed: %s", request.getUrl(), response.getBody()));
			} catch (Exception e) {
				throw new UnirestException(String.format("request to %s failed: %s", request.getUrl(), response.getStatusText()));
			}
			
			if (response.getStatus() == 401) {
				error += "You need to provide the API token through 'seleniumRobotServerToken' parameter";
			}

			throw new SeleniumRobotServerException(String.format("request to %s failed: %s", request.getUrl(), error));
		}
		
		if (response.getStatus() == 204) {
			return new JSONObject();
		}
		
		return new JSONObject(response.getBody());
	}
	
	protected JSONArray getJSonArray(HttpRequest request)  {
		HttpResponse<String> response = request.asString();
		

		if (response.getStatus() == 423) {
			String error = new JSONObject(response.getBody()).getString("detail");
			throw new SeleniumRobotServerException(error);
		}
		
		if (response.getStatus() >= 400) {
			try {
				String error = new JSONObject(response.getBody()).getString("detail");
				throw new SeleniumRobotServerException(String.format("request to %s failed: %s", request.getUrl(), error));
			} catch (Exception e) {
				throw new UnirestException(String.format("request to %s failed: %s", request.getUrl(), response.getStatusText()));
			}
			
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
	public String getUrl() {
		return url;
	}
}
