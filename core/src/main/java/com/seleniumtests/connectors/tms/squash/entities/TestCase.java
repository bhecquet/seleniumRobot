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
public class TestCase extends Entity {
	
	private static final String TEST_CASE_URL = "test-cases/%s";

	public TestCase(int id) {
		super("", id, null);
	}

	public TestCase(int id, String url) {
		super(url, id, null);
	}
	

	public static TestCase fromJson(JSONObject json) {
		try {
			return new TestCase (
					json.getInt(FIELD_ID), 
					json.getJSONObject("_links").getJSONObject("self").getString("href")
					);
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create TestCase from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public static TestCase get(int id) {
		try {
			return fromJson(getJSonResponse(buildGetRequest(apiRootUrl + String.format(TEST_CASE_URL, id))));
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Test Case %d does not exist", id));
		}	
	}
}
