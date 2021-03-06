package com.seleniumtests.connectors.tms.squash.entities;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class Campaign extends Entity {


	public static final String CAMPAIGNS_URL = "campaigns";
	public static final String ITERATIONS_URL = "/iterations";

	public Campaign(String url, int id, String name) {
		super(url, id, name);
	}
	

	/**
	 * Get list of all campaigns
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Campaign> getAll() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(apiRootUrl + CAMPAIGNS_URL));
			
			List<Campaign> campaigns = new ArrayList<>();
			if (json.has(FIELD_EMBEDDED)) {
				for (JSONObject folderJson: (List<JSONObject>)json.getJSONObject(FIELD_EMBEDDED).getJSONArray(FIELD_CAMPAIGNS).toList()) {
					campaigns.add(Campaign.fromJson(folderJson));
				}
			}
			return campaigns;
		} catch (UnirestException e) {
			throw new ScenarioException("Cannot get all campaigns", e);
		}
	}
	
	/**
	 * get iterations for the current campaign
	 */
	@SuppressWarnings("unchecked")
	public List<Iteration> getIterations() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(url + String.format(ITERATIONS_URL, id)));
			
			List<Iteration> iterations = new ArrayList<>();
			if (json.has(FIELD_EMBEDDED)) {
				for (JSONObject iterationJson: (List<JSONObject>)json.getJSONObject(FIELD_EMBEDDED).getJSONArray("iterations").toList()) {
					iterations.add(Iteration.fromJson(iterationJson));
				}
			}
			return iterations;
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot get list of iterations for campaign %s", name));
		}
	}

	public static Campaign fromJson(JSONObject json) {
		try {
			return new Campaign(json.getJSONObject("_links").getJSONObject("self").getString("href"),
					json.getInt(FIELD_ID),
					json.getString(FIELD_NAME));
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create Campaign from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
	}
	
	public static Campaign create(Project project, String campaignName, CampaignFolder parentFolder) {
		try {
			
			
			JSONObject body = new JSONObject();
			body.put(FIELD_TYPE, TYPE_CAMPAIGN);
			body.put(FIELD_NAME, campaignName);
			body.put("status", "PLANNED");
			
			JSONObject parent = new JSONObject();
			if (parentFolder == null) {
				parent.put(FIELD_ID, project.id);
				parent.put(FIELD_TYPE, "project");	
			} else {
				parent.put(FIELD_ID, parentFolder.id);
				parent.put(FIELD_TYPE, "campaign-folder");	
			}
			body.put("parent", parent);
			
			JSONObject json = getJSonResponse(buildPostRequest(apiRootUrl + CAMPAIGNS_URL).body(body));
			
			return fromJson(json);

		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot create campaign %s", campaignName), e);
		}
				
	}
}
