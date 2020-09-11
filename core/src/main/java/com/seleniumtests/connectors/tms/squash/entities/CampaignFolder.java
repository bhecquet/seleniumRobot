package com.seleniumtests.connectors.tms.squash.entities;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class CampaignFolder extends Entity {

	public static final String CAMPAIGN_FOLDER_URL = "campaign-folders";
	
	public Project project;
	public Entity parent;

	public CampaignFolder(String url, int id, String name, Project project, Entity parent) {
		super(url, id, name);
		this.project = project;
		this.parent = parent;
	}
	
	public static List<CampaignFolder> getAll() {
		try {
			JSONObject json = getPagedJSonResponse(buildGetRequest(apiRootUrl + CAMPAIGN_FOLDER_URL));
			
			List<CampaignFolder> campaignFolders = new ArrayList<>();
			if (json.has("_embedded")) {
				for (JSONObject folderJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("campaign-folders").toList()) {
					campaignFolders.add(CampaignFolder.fromJson(folderJson));
				}
			}
			return campaignFolders;
		} catch (UnirestException e) {
			throw new ScenarioException("Cannot get all campaign folders", e);
		}
	}

	public JSONObject asJson() {
		JSONObject json = new JSONObject();
		json.put("_type", "campaign-folder");
		json.put("id", id);
		json.put("name", name);
		return json;
	}

	public static CampaignFolder fromJson(JSONObject json) {
		
		Entity parent;
		if (json.has("parent")) {
			if ("project".equals(json.getJSONObject("parent").getString("_type"))) {
				parent = Project.fromJson(json.getJSONObject("parent"));
			} else if ("campaign-folder".equals(json.getJSONObject("parent").getString("_type"))) {
				parent = CampaignFolder.fromJson(json.getJSONObject("parent"));
			} else {
				parent = null;
			}
		} else {
			parent = null;
		}
		
		Project project = null;
		if (json.has("project")) {
			project = Project.fromJson(json.getJSONObject("project"));
		}
		
		return new CampaignFolder(json.getJSONObject("_links").getJSONObject("self").getString("href"), 
				json.getInt("id"), 
				json.getString("name"),
				project,
				parent
				);
		
		
	}
	
	public static CampaignFolder create(Project project, CampaignFolder parent, String campaignFolderName) {
		try {
			
			JSONObject body = new JSONObject();
			body.put("_type", "campaign-folder");
			body.put("name", campaignFolderName);
			if (parent != null) {
				body.put("parent", parent.asJson());
			} else {
				body.put("parent", project.asJson());
			}
			
			JSONObject json = getJSonResponse(buildPostRequest(apiRootUrl + CAMPAIGN_FOLDER_URL).body(body));
			
			
			return CampaignFolder.fromJson(json);

		} catch (UnirestException e) {
			throw new ScenarioException(String.format("Cannot create campaign %s", campaignFolderName), e);
		}
				
	}

	public static String getCampaignFolderUrl() {
		return CAMPAIGN_FOLDER_URL;
	}

	public Project getProject() {
		return project;
	}

	public Entity getParent() {
		return parent;
	}
}
