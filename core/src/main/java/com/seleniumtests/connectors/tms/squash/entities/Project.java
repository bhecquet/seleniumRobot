package com.seleniumtests.connectors.tms.squash.entities;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class Project extends Entity {

	public static final String PROJECTS_URL = "projects";
	public static final String CAMPAIGNS_URL = "/campaigns";

	public Project(String url, int id, String name) {
		super(url, id, name);
	}

	/**
	 * Returns the list of projects accessible to this user
	 * @return
	 */
	public static List<Project> getAll() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(apiRootUrl + PROJECTS_URL));
	
			List<Project> projects = new ArrayList<>();
			if (json.has("_embedded")) {
				for (JSONObject projectJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("projects").toList()) {
					projects.add(Project.fromJson(projectJson));
				}
			}
			return projects;
		} catch (UnirestException e) {
			throw new ScenarioException("Cannot get all projects", e);
		}
	}

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put("_type", "project");
		json.put("id", id);
		json.put("name", name);
		return json;
	}

	public static Project fromJson(JSONObject json) {
		return new Project(
				json.getJSONObject("_links").getJSONObject("self").getString("href"),
				json.getInt("id"), 
				json.getString("name"));
	}
	
	public List<Campaign> getCampaigns() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(url + CAMPAIGNS_URL));
			
			List<Campaign> campaigns = new ArrayList<>();
			if (json.has("_embedded")) {
				for (JSONObject folderJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("campaigns").toList()) {
					campaigns.add(Campaign.fromJson(folderJson));
				}
			}
			return campaigns;
		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot get list of campaigns for project %s", name));
		}
	}

	
}
