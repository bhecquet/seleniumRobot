package com.seleniumtests.connectors.selenium;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.customexception.SeleniumRobotServerException;

public class SeleniumRobotVariableServerConnector extends SeleniumRobotServerConnector {
	
	public static final String VARIABLE_API_URL = "/variable/api/version/";

	private Integer versionId;
	private Integer environmentId;
	private Integer testCaseId;
		
	public SeleniumRobotVariableServerConnector() {
		super();
		if (!active) {
			return;
		}
		active = isAlive();
		
		getInfoFromServer();
	}
	
	/**
	 * Returns the versionId, environmentId and testCaseId from server
	 */
	private void getInfoFromServer() {
		new NotImplementedException();
	}
	
	@Override
	protected boolean isAlive() {
		return isAlive("/variable/api/");
	}

	public Map<String, String> getVariables() {
		if (!active) {
			throw new SeleniumRobotServerException("Server is not active");
		}
		try {
			
			JSONArray variablesJson = getJSonArray(Unirest.get(url + VARIABLE_API_URL)
					.queryString("version", versionId)
					.queryString("environment", environmentId)
					.queryString("test", testCaseId)
					.queryString("format", "json"));
			
			Map<String, String> variables = new HashMap<>();
			for (int i=0; i < variablesJson.length(); i++) {
				JSONObject variableJson = variablesJson.getJSONObject(i);
				variables.put(variableJson.getString("name"), variableJson.getString("value"));
			}
			return variables;
			
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot get variables", e);
		}
	}

}
