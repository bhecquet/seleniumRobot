package com.seleniumtests.connectors.tms.squash.entities;

import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class ExecutionStep extends Entity {
	
	public static final String EXECUTION_STATUS_URL = "/execution-steps/%d/execution-status";
	public static final String FIELD_ACTION = "action";
	public static final String FIELD_COMMENT = "comment";

	public ExecutionStep(String url, int id, String name) {
		super(url, id, name);
	}

	public static ExecutionStep fromJson(JSONObject json) {
		try {
			return new ExecutionStep(json.getJSONObject("_links").getJSONObject("self").getString("href"),
					json.getInt(FIELD_ID),
					json.getString(FIELD_ACTION));
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create execution step from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public void setStatus(ExecutionStatus status) {
		if (status == null) {
			return;
		}
		try {
			getJSonResponse(buildPatchRequest(String.format("%s/execution-status/%s", url, status.toString())));
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot set result for execution step %d", id));
		}
	}
	
	public void setComment(String comment) {
		
		JSONObject body = new JSONObject();
		body.put("_type", "execution-step");
		body.put(FIELD_COMMENT, comment);
		try {
			getJSonResponse(buildPatchRequest(url).body(body));
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot set result for execution step %d", id));
		}
	}
	


}
