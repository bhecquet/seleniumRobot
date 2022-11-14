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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.SeleniumRobotServer404Exception;
import com.seleniumtests.customexception.SeleniumRobotServerException;

import kong.unirest.GetRequest;
import kong.unirest.MultipartBody;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class SeleniumRobotVariableServerConnector extends SeleniumRobotServerConnector {
	
	private static final String FIELD_APPLICATION = "application";
	private static final String FIELD_OLDER_THAN = "olderThan";
	private static final String FIELD_TEST = "test";
	private static final String FIELD_ENVIRONMENT = "environment";
	private static final String FIELD_RESERVABLE = "reservable";
	private static final String FIELD_TIME_TO_LIVE = "timeToLive";
	private static final String FIELD_VERSION = "version";
	private static final String FIELD_VALUE = "value";
	public static final String VARIABLE_API_URL = "/variable/api/variable/";
	public static final String EXISTING_VARIABLE_API_URL = "/variable/api/variable/%d/";
	
	private Integer testCaseId;

	public SeleniumRobotVariableServerConnector(boolean useRequested, String url, String testName) {
		this(useRequested, url, testName, null);
	}
		
	public SeleniumRobotVariableServerConnector(boolean useRequested, String url, String testName, String authToken) {
		super(useRequested, url, authToken);
		if (!active) {
			return;
		}
		active = isAlive();
		
		if (active) {
			getInfoFromServer();
			testCaseId = createTestCase(testName);
		}
	}
	
	@Override
	public boolean isAlive() {
		return isAlive("/variable/api/");
	}
	
	public Map<String, TestVariable> getVariables() {
		return getVariables(0, -1);
	}
	

	/**
	 * Returns the list of variables whose name is 'name' and value is 'value'. Name or value may be null
	 * @param name		name of the variables to retrieve. If given, only one variable will be get because server only returns one variable for each name
	 * @param value		value of the variables to retrieve
	 * @return			the map of found variables
	 */
	public Map<String, TestVariable> getVariables(String name, String value) {
		return getVariables(0, name, value, true, -1);
	}

	/**
	 * Retrieve all variables from the server
	 * Display a warning when a custom variable prefix "custom.test.variable." overwrites or is overwritten by a regular one
	 * 
	 * @param variablesOlderThanDays number of days since this variable should be created before it can be returned. This only applies to variables which have a time to live (a.k.a: where destroyAfterDays parameter is > 0) 
	 * @return
	 */
	public Map<String, TestVariable> getVariables(Integer variablesOlderThanDays, int variablesReservationDuration) {
		return getVariables(variablesOlderThanDays, null, null, true, variablesReservationDuration);
	}
	
	/**
	 * Retrieve all variables from the server
	 * Display a warning when a custom variable prefix "custom.test.variable." overwrites or is overwritten by a regular one
	 * 
	 * 
	 * @param variablesOlderThanDays 		number of days since this variable should be created before it can be returned. This only applies to variables which have a time to live (a.k.a: where destroyAfterDays parameter is > 0) 
	 * @param name							name of the variables to retrieve. If given, only one variable will be get because server only returns one variable for each name
	 * @param value							value of the variables to retrieve
	 * @param reserve						if true, reserve the reservable variables, else, only return searched variables
	 * @return
	 */
	public Map<String, TestVariable> getVariables(Integer variablesOlderThanDays, String name, String value, boolean reserve) {
		return getVariables(variablesOlderThanDays, name, value, reserve, -1);
	}
	
	/**
	 * Retrieve all variables from the server
	 * Display a warning when a custom variable prefix "custom.test.variable." overwrites or is overwritten by a regular one
	 * 
	 * 
	 * @param variablesOlderThanDays 		number of days since this variable should be created before it can be returned. This only applies to variables which have a time to live (a.k.a: where destroyAfterDays parameter is > 0) 
	 * @param name							name of the variables to retrieve. If given, only one variable will be get because server only returns one variable for each name
	 * @param value							value of the variables to retrieve
	 * @param reserve						if true, reserve the reservable variables, else, only return searched variables
	 * @param variablesReservationDuration	Number of seconds a reservable variable will be reserved
	 * @return
	 */
	public Map<String, TestVariable> getVariables(Integer variablesOlderThanDays, String name, String value, boolean reserve, int variablesReservationDuration) {
		if (!active) {
			throw new SeleniumRobotServerException("Server is not active");
		}
		try {
			
			List<String> varNames = new ArrayList<>();
			
			GetRequest request = buildGetRequest(url + VARIABLE_API_URL)
					.queryString(FIELD_VERSION, versionId)
					.queryString(FIELD_ENVIRONMENT, environmentId)
					.queryString(FIELD_TEST, testCaseId)
					.queryString(FIELD_OLDER_THAN, variablesOlderThanDays)
					.queryString("reserve", reserve)
					.queryString("format", "json");
			
			if (variablesReservationDuration > 0) {
				request = request.queryString("reservationDuration", variablesReservationDuration * 60);
			}
			if (name != null) {
				request = request.queryString(FIELD_NAME, name);
			}
			if (value != null) {
				request = request.queryString(FIELD_VALUE, value);
			}
			
			JSONArray variablesJson = getJSonArray(request);
			
			Map<String, TestVariable> variables = new HashMap<>();
			for (int i=0; i < variablesJson.length(); i++) {
				TestVariable variable = TestVariable.fromJsonObject(variablesJson.getJSONObject(i));
				
				if (varNames.contains(variable.getName())) {
					logger.warn(String.format("variable %s has already been get. Check no custom (added by test script) variable has the same name as a regular one", variable.getName()));
				} else {
					variables.put(variable.getName(), variable);
					varNames.add(variable.getName());
				}
			}
			return variables;
			
		} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
			throw new SeleniumRobotServerException("cannot get variables", e);
		} 
	}
	
	/**
	 * convert Map<String, TestVariable> to Map<String, String>
	 * @param rawVariables
	 * @return
	 */
	public static Map<String, String> convertRawTestVariableMapToKeyValuePairs(Map<String, TestVariable> rawVariables) {
		Map<String, String> variables = new HashMap<>();
		for (Entry<String, TestVariable> entry: rawVariables.entrySet()) {
			variables.put(entry.getKey(), entry.getValue().getValue());
		}
		return variables;
	}
	
	/**
	 * Each variable marked as reservable is unreserved
	 * @param variables
	 */
	public void unreserveVariables(List<TestVariable> variables) {
		for (TestVariable variable: variables) {
			unreserveVariable(variable);
		}
	}
	
	private void unreserveVariable(TestVariable variable) {
		if (variable.isReservable() && variable.getId() != null) {
			try {
				getJSonResponse(buildPatchRequest(String.format(url + EXISTING_VARIABLE_API_URL, variable.getId()))
						.field("releaseDate", ""));
			} catch (UnirestException e) {
				logger.warn("variable could not be unreserved, server will do it automatically after reservation period: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Create or update variable on the server. This helps centralizing some configurations during the test
	 * If this is an existing variable, only update the value. Else, create it with current environment, application, version.
	 * A regular variable with a changed value will be added as a custom variable. There should be no way to update a regular variable from seleniumRobot.
	 * Variable is created with "internal" flag set
	 * @param variable				the TestVariable instance to save
	 * @param specificToVersion		if true, this variable will be assigned to the current application version. Else, it's assigned to the whole application
	 * @return the updated TestVariable
	 */
	public TestVariable upsertVariable(TestVariable variable, boolean specificToVersion) {
		
		// this variable does not exist, create it
		// OR this variable is an update of an existing, non custom variable
		//     this copy would not contain the "test" reference 
		if (variable.getId() == null || !variable.getInternalName().startsWith(TestVariable.TEST_VARIABLE_PREFIX)) {
			try {
				return createVariable(variable, specificToVersion);
			} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
				throw new SeleniumRobotServerException("cannot upsert variables", e);
			} 
		} else {
			try {
				return updateVariable(variable);
				
			// in case variable has been deleted in the interval (between test start and now), recreate it
			} catch (SeleniumRobotServer404Exception e) {
				return createVariable(variable, specificToVersion);
			} catch (UnirestException | JSONException | SeleniumRobotServerException e) {
				throw new SeleniumRobotServerException("cannot upsert variables", e);
			} 
		}
	}

	/**
	 * Update variable on the server
	 * @param variable
	 * @return
	 */
	private TestVariable updateVariable(TestVariable variable) {
		JSONObject variableJson = getJSonResponse(buildPatchRequest(String.format(url + EXISTING_VARIABLE_API_URL, variable.getId()))
				.field(FIELD_VALUE, variable.getValue())
				.field(FIELD_RESERVABLE, String.valueOf(variable.isReservable()))
				.field(FIELD_TIME_TO_LIVE, String.valueOf(variable.getTimeToLive())));
		
		return TestVariable.fromJsonObject(variableJson);
	}

	/**
	 * create the variable on the server
	 * @param variable
	 * @param specificToVersion
	 * @return
	 */
	private TestVariable createVariable(TestVariable variable, boolean specificToVersion) {
		MultipartBody request = buildPostRequest(url + VARIABLE_API_URL)
				.field(FIELD_NAME, TestVariable.TEST_VARIABLE_PREFIX + variable.getName())
				.field(FIELD_VALUE, variable.getValue())
				.field(FIELD_RESERVABLE, String.valueOf(variable.isReservable()))
				.field(FIELD_ENVIRONMENT, environmentId.toString())
				.field(FIELD_APPLICATION, applicationId.toString())
				.field("internal", String.valueOf(true))
				.field(FIELD_TIME_TO_LIVE, String.valueOf(variable.getTimeToLive()));
		
		if (specificToVersion) {
			request = request.field(FIELD_VERSION, versionId.toString());
		}
		
		JSONObject variableJson = getJSonResponse(request);
		
		return TestVariable.fromJsonObject(variableJson);
	}
}
