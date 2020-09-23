package com.seleniumtests.connectors.tms.squash.entities;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class IterationTestPlanItem extends Entity {

	public static final String TEST_PLAN_ITEM_URL = "iterations/%d/test-plan";
	public static final String TEST_PLAN_ITEM_EXECUTION_URL = "iteration-test-plan-items/%d/executions";
	
	public TestCase testCase;

	public IterationTestPlanItem(String url, int id, TestCase testCase) {
		super(url, id, null);
		this.testCase = testCase;
	}

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put("_type", "iteration-test-plan-item");
		json.put("id", id);
		return json;
	}
	
	/**
	 * Create an execution for this item
	 * @return
	 */
	public TestPlanItemExecution createExecution() {
		try {
			return TestPlanItemExecution.fromJson(getJSonResponse(buildPostRequest(apiRootUrl + String.format(TEST_PLAN_ITEM_EXECUTION_URL, id))));
		} catch (UnirestException e) {
			throw new ScenarioException("Cannot create execution", e);
		}
	}

	public static IterationTestPlanItem fromJson(JSONObject json) {

		try {
			return new IterationTestPlanItem(
					json.getJSONObject("_links").getJSONObject("self").getString("href"),
					json.getInt("id"), 
					TestCase.fromJson(json.getJSONObject("referenced_test_case"))
					);
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create IterationTestPlanItem from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public static IterationTestPlanItem create(Iteration iteration, TestCase testCase) {
		try {
			
			
			JSONObject body = new JSONObject();
			body.put("_type", "iteration-test-plan-item");
			JSONObject testCaseJson = new JSONObject();
			testCaseJson.put("id", testCase.id);
			testCaseJson.put("_type", "test-case");
			body.put("test_case", testCaseJson);
			
			JSONObject json = getJSonResponse(buildPostRequest(apiRootUrl + String.format(TEST_PLAN_ITEM_URL, iteration.id)).body(body));
			
			
			return IterationTestPlanItem.fromJson(json);

		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot create Iteration test plan for iteration %s and test case %d", iteration.name, testCase.id), e);
		}
				
	}

	public TestCase getTestCase() {
		return testCase;
	}
}
