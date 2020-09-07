package com.seleniumtests.connectors.tms.squash.entities;

import kong.unirest.json.JSONObject;

/**
 * Object representing a test case in Squash TM
 * @author S047432
 *
 */
public class TestCase extends Entity {

	public TestCase(int id) {
		super("", id, null);
	}

	public TestCase(int id, String url) {
		super(url, id, null);
	}
	

	public static TestCase fromJson(JSONObject json) {

		return new TestCase (
				json.getInt("id"), 
				json.getJSONObject("_links").getJSONObject("self").getString("href")
				);
	}
}
