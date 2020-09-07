package com.seleniumtests.connectors.tms.squash.entities;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
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
	public static List<Campaign> getAll() {
		JSONObject json = getPagedJSonResponse(buildGetRequest(apiRootUrl + CAMPAIGNS_URL));
		
		List<Campaign> campaigns = new ArrayList<>();
		if (json.has("_embedded")) {
			for (JSONObject folderJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("campaigns").toList()) {
				campaigns.add(Campaign.fromJson(folderJson));
			}
		}
		return campaigns;
	}
	
	/**
	 * get iterations for the current campaign
	 */
	public List<Iteration> getIterations() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(url + String.format(ITERATIONS_URL, id)));
			
			List<Iteration> iterations = new ArrayList<>();
			if (json.has("_embedded")) {
				for (JSONObject iterationJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("iterations").toList()) {
					iterations.add(Iteration.fromJson(iterationJson));
				}
			}
			return iterations;
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot get list of iterations for campaign %s", name));
		}
	}

	public static Campaign fromJson(JSONObject json) {
		return new Campaign(json.getJSONObject("_links").getJSONObject("self").getString("href"),
				json.getInt("id"),
				json.getString("name"));
	}
	
	public static Campaign create(Project project, String campaignName, CampaignFolder parentFolder) {
		try {
			
			
			JSONObject body = new JSONObject();
			body.put("_type", "campaign");
			body.put("name", campaignName);
			body.put("status", "PLANNED");
			
			JSONObject parent = new JSONObject();
			if (parentFolder == null) {
				parent.put("id", project.id);
				parent.put("_type", "project");	
			} else {
				parent.put("id", parentFolder.id);
				parent.put("_type", "campaign-folder");	
			}
			body.put("parent", parent);
			
			JSONObject json = getJSonResponse(buildPostRequest(apiRootUrl + CAMPAIGNS_URL).body(body));
			
			return fromJson(json);

		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot create campaign %s", campaignName), e);
		}
				
	}
}
