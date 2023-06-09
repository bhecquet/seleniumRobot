package com.seleniumtests.connectors.tms.squash.entities;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
	

	public static final String FIELD_EXECUTION_STATUS = "execution_status";
	
	List<ExecutionStep> steps;
	
	public TestPlanItemExecution(String url, int id, String name) {
		super(url, id, name);
		steps = new ArrayList<>();
	}
	

	public static TestPlanItemExecution fromJson(JSONObject json) {

		try {
			TestPlanItemExecution testPlanItemExecution = new TestPlanItemExecution (
					json.getJSONObject("_links").getJSONObject("self").getString("href"),
					json.getInt(FIELD_ID), 
					json.getString(FIELD_NAME)
					);
			
			for (JSONObject jsonStep: (List<JSONObject>)json.getJSONArray("execution_steps").toList()) {
				testPlanItemExecution.steps.add(ExecutionStep.fromJson(jsonStep));
			}
			
			return testPlanItemExecution;
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create TestPlanItemException from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public void setResult(ExecutionStatus result, String comment) {
		
		if (!steps.isEmpty()) {
			steps.get(0).setStatus(result);
			if (comment != null) {
				steps.get(0).setComment(comment);
			}
		}
		
		JSONObject body = new JSONObject();
		body.put("_type", "execution");
		body.put(FIELD_EXECUTION_STATUS, result.toString());
		try {
			getJSonResponse(buildPatchRequest(String.format("%s?fields=execution_status", url)).body(body));
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot set result for execution %d", id));
		}
	}


	public List<ExecutionStep> getSteps() {
		return steps;
	}


	public void setSteps(List<ExecutionStep> steps) {
		this.steps = steps;
	}
}
