package com.seleniumtests.connectors.tms.squash.entities;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class IterationTestPlanItem extends Entity {

	public static final String TEST_PLAN_ITEM_URL = "iterations/%d/test-plan";
	public static final String TEST_PLAN_ITEM_EXECUTION_URL = "iteration-test-plan-items/%d/executions";
	
	private TestCase testCase;

	private Dataset dataset;

	public IterationTestPlanItem(String url, int id, TestCase testCase, Dataset dataset) {
		super(url, id, null);
		this.testCase = testCase;
		this.dataset = dataset;
	}

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put(FIELD_TYPE, "iteration-test-plan-item");
		json.put(FIELD_ID, id);
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
			JSONObject referencedTestCase = json.optJSONObject("referenced_test_case");
			JSONObject referencedDataset = json.optJSONObject("referenced_dataset");

			return new IterationTestPlanItem(
					json.getJSONObject("_links").getJSONObject("self").getString("href"),
					json.getInt(FIELD_ID), 
					referencedTestCase == null ? null: TestCase.fromJson(referencedTestCase),
					referencedDataset == null ? null: Dataset.fromJson(referencedDataset)
					);
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create IterationTestPlanItem from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public static IterationTestPlanItem create(Iteration iteration, TestCase testCase, Dataset dataset) {
		try {
			
			
			JSONObject body = new JSONObject();
			body.put(FIELD_TYPE, "iteration-test-plan-item");
			JSONObject testCaseJson = new JSONObject();
			testCaseJson.put(FIELD_ID, testCase.id);
			testCaseJson.put(FIELD_TYPE, "test-case");
			body.put("test_case", testCaseJson);

			if (dataset != null) {
				JSONObject datasetJson = new JSONObject();
				datasetJson.put(FIELD_ID, dataset.id);
				datasetJson.put(FIELD_TYPE, "dataset");
				body.put("dataset", datasetJson);
			}

			JSONObject json = getJSonResponse(buildPostRequest(apiRootUrl + String.format(TEST_PLAN_ITEM_URL, iteration.id)).body(body));
			
			
			return IterationTestPlanItem.fromJson(json);

		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot create Iteration test plan for iteration %s and test case %d", iteration.name, testCase.id), e);
		}
				
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public Dataset getDataset() {
		return dataset;
	}
}
