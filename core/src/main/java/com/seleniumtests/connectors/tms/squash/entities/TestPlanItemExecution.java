package com.seleniumtests.connectors.tms.squash.entities;

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
	
	public TestPlanItemExecution(int id, String name, String url) {
		super(url, id, name);
	}
	

	public static TestPlanItemExecution fromJson(JSONObject json) {

		return new TestPlanItemExecution (
				json.getInt("id"), 
				json.getString("name"), 
				json.getJSONObject("_links").getJSONObject("self").getString("href")
				);
	}
	
	public void setResult(ExecutionStatus result) {
		JSONObject body = new JSONObject();
		body.put("_type", "execution");
		body.put("execution_status", result.toString());
		getJSonResponse(buildPatchRequest(url).body(body));
	}
}
