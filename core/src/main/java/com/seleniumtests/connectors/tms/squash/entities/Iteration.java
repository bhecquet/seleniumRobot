package com.seleniumtests.connectors.tms.squash.entities;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class Iteration extends Entity {
	
	public static final String ITERATIONS_URL = "campaigns/%s/iterations";

	public Iteration(String url, int id, String name) {
		super(url, id, name);
	}
	
	public IterationTestPlanItem addTestCase(TestCase testCase) {
		return IterationTestPlanItem.create(this, testCase);
	}
	
	public List<IterationTestPlanItem> getAllTestCases() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(apiRootUrl + String.format(IterationTestPlanItem.TEST_PLAN_ITEM_URL, id)));
			
			List<IterationTestPlanItem> testPlanItems = new ArrayList<>();
			if (json.has("_embedded")) {
				for (JSONObject tpiJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("test-plan").toList()) {
					testPlanItems.add(IterationTestPlanItem.fromJson(tpiJson));
				}
			}
			return testPlanItems;
		} catch (UnirestException e) {
			throw new ScenarioException("Cannot get all test cases", e);
		}
	}


	public static Iteration fromJson(JSONObject json) {
		return new Iteration(json.getJSONObject("_links").getJSONObject("self").getString("href"),
				json.getInt("id"),
				json.getString("name"));
	}
	

	public static Iteration create(Campaign campaign, String iterationName) {
		try {
			
			JSONObject body = new JSONObject();
			body.put("_type", "iteration");
			body.put("name", iterationName);
			
			JSONObject parent = new JSONObject();
			parent.put("id", campaign.id);
			parent.put("_type", "campaign");	
			body.put("parent", parent);
			
			JSONObject json = getJSonResponse(buildPostRequest(String.format(apiRootUrl + ITERATIONS_URL, campaign.id)).body(body));

			return fromJson(json);

		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot create campaign %s", iterationName), e);
		}
				
	}

}
