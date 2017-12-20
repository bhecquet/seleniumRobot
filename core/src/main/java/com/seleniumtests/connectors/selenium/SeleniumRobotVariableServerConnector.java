package com.seleniumtests.connectors.selenium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.SeleniumRobotServerException;

public class SeleniumRobotVariableServerConnector extends SeleniumRobotServerConnector {
	
	public static final String VARIABLE_API_URL = "/variable/api/variable/";
	public static final String EXISTING_VARIABLE_API_URL = "/variable/api/variable/%d/";
		
	public SeleniumRobotVariableServerConnector(String testName) {
		super();
		if (!active) {
			return;
		}
		active = isAlive();
		
		if (active) {
			getInfoFromServer(testName);
		}
	}
	
	@Override
	public boolean isAlive() {
		return isAlive("/variable/api/");
	}

	/**
	 * Retrieve all variables from the server
	 * Display a warning when a custom variable prefix "custom.test.variable." overwrites or is overwritten by a regular one
	 * @return
	 */
	public Map<String, TestVariable> getVariables() {
		if (!active) {
			throw new SeleniumRobotServerException("Server is not active");
		}
		try {
			
			List<String> varNames = new ArrayList<>();
			
			JSONArray variablesJson = getJSonArray(Unirest.get(url + VARIABLE_API_URL)
					.queryString("version", versionId)
					.queryString("environment", environmentId)
					.queryString("test", testCaseId)
					.queryString("format", "json"));
			
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
			
		} catch (UnirestException | JSONException e) {
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
	 * Create or update variable on the server. This helps centralizing some configurations during the test
	 * If this is an existing variable, only update the value. Else, create it with current environment, application, version.
	 * A regular variable with a changed value will be add as a custom variable. There should be no way to update a regular variable from seleniumRobot.
	 * @return
	 */
	public TestVariable upsertVariable(TestVariable variable) {
		
		// this variable does not exist, create it
		// OR this variable is a update of an existing, non custom variable
		if (variable.getId() == null || !variable.getInternalName().startsWith(TestVariable.TEST_VARIABLE_PREFIX)) {
			try {
				JSONObject variableJson = getJSonResponse(Unirest.post(url + VARIABLE_API_URL)
						.field("name", TestVariable.TEST_VARIABLE_PREFIX + variable.getName())
						.field("value", variable.getValue())
						.field("reservable", false)
						.field("environment", environmentId)
						.field("application", applicationId)
						.field("version", versionId));
				
				return TestVariable.fromJsonObject(variableJson);
				
			} catch (UnirestException | JSONException e) {
				throw new SeleniumRobotServerException("cannot upsert variables", e);
			} 
		} else {
			try {
				JSONObject variableJson = getJSonResponse(Unirest.patch(String.format(url + EXISTING_VARIABLE_API_URL, variable.getId()))
						.field("value", variable.getValue()));
				
				return TestVariable.fromJsonObject(variableJson);
				
			} catch (UnirestException | JSONException e) {
				throw new SeleniumRobotServerException("cannot upsert variables", e);
			} 
		}
	}
}
