package com.seleniumtests.connectors.tms.squash.entities;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

/**
 * Object representing a test case in Squash TM
 * @author S047432
 *
 */
public class TestPlanItemExecution extends Entity {
	
	public enum ExecutionStatus {
		RUNNING,
		READY,
		SUCCESS,
		FAILURE,
		BLOCKED
	}
	
	public TestPlanItemExecution(String url, int id, String name) {
		super(url, id, name);
	}
	

	public static TestPlanItemExecution fromJson(JSONObject json) {

		try {
			return new TestPlanItemExecution (
					json.getJSONObject("_links").getJSONObject("self").getString("href"),
					json.getInt(FIELD_ID), 
					json.getString(FIELD_NAME)
					);
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create TestPlanItemException from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public void setResult(ExecutionStatus result) {
		JSONObject body = new JSONObject();
		body.put("_type", "execution");
		body.put("execution_status", result.toString());
		try {
			getJSonResponse(buildPatchRequest(String.format("%s?fields=execution_status", url)).body(body));
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot set result for execution %d", id));
		}
	}
}
