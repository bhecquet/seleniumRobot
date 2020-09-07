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

	public CampaignFolder(String name, int id, String url, Project project, Entity parent) {
		super(url, id, name);
		this.project = project;
		this.parent = parent;
	}
	
	public static List<CampaignFolder> getAll() {
		JSONObject json = getPagedJSonResponse(buildGetRequest(apiRootUrl + CAMPAIGN_FOLDER_URL));
		
		List<CampaignFolder> campaignFolders = new ArrayList<>();
		if (json.has("_embedded")) {
			for (JSONObject folderJson: (List<JSONObject>)json.getJSONObject("_embedded").getJSONArray("campaign-folders").toList()) {
				campaignFolders.add(CampaignFolder.fromJson(folderJson));
			}
		}
		return campaignFolders;
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
		
		return new CampaignFolder(json.getString("name"), 
				json.getInt("id"), 
				json.getJSONObject("_links").getJSONObject("self").getString("href"),
				project,
				parent
				);
		
		
	}
	
	public static CampaignFolder create(Project project, CampaignFolder parent, String campaignFolderName) {
		try {
			
			for (CampaignFolder existingFolder: getAll()) {
				if (existingFolder.getName().equals(campaignFolderName) 
						&& (existingFolder.project == null || existingFolder.project != null && existingFolder.project.id == project.id)
						&& (existingFolder.parent == null 
							|| parent == null && existingFolder.parent != null && existingFolder.parent instanceof Project
							|| (parent != null && existingFolder.parent != null && existingFolder.parent instanceof CampaignFolder && existingFolder.parent.id == parent.id))) {
					return existingFolder;
				}
			}
			
			JSONObject body = new JSONObject();
			body.put("_type", "campaign-folder");
			body.put("name", campaignFolderName);
//			body.put("project", project.asJson());
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
}
