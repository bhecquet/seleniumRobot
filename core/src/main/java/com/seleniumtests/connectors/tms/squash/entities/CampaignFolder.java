package com.seleniumtests.connectors.tms.squash.entities;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class CampaignFolder extends Entity {

	public static final String CAMPAIGN_FOLDER_URL = "campaign-folders";
	
	private Project project;
	private Entity parent;

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
				for (JSONObject folderJson: (List<JSONObject>)json.getJSONObject(FIELD_EMBEDDED).getJSONArray(FIELD_CAMPAIGN_FOLDERS).toList()) {
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
		json.put(FIELD_TYPE, TYPE_CAMPAIGN_FOLDER);
		json.put(FIELD_ID, id);
		json.put(FIELD_NAME, name);
		return json;
	}

	public static CampaignFolder fromJson(JSONObject json) {
		
		try {
			Entity parent;
			if (json.has(FIELD_PARENT)) {
				if (TYPE_PROJECT.equals(json.getJSONObject(FIELD_PARENT).getString(FIELD_TYPE))) {
					parent = Project.fromJson(json.getJSONObject(FIELD_PARENT));
				} else if (TYPE_CAMPAIGN_FOLDER.equals(json.getJSONObject(FIELD_PARENT).getString(FIELD_TYPE))) {
					parent = CampaignFolder.fromJson(json.getJSONObject(FIELD_PARENT));
				} else {
					parent = null;
				}
			} else {
				parent = null;
			}
			
			Project project = null;
			if (json.has(TYPE_PROJECT)) {
				project = Project.fromJson(json.getJSONObject(TYPE_PROJECT));
			}
			
			return new CampaignFolder(json.getJSONObject("_links").getJSONObject("self").getString("href"), 
					json.getInt(FIELD_ID), 
					json.getString(FIELD_NAME),
					project,
					parent
					);
		} catch (JSONException e) {
			throw new ScenarioException(String.format("Cannot create CampaignFolder from JSON [%s] data: %s", json.toString(), e.getMessage()));
		}
		
	}
	
	public static CampaignFolder create(Project project, CampaignFolder parent, String campaignFolderName) {
		try {
			
			JSONObject body = new JSONObject();
			body.put(FIELD_TYPE, TYPE_CAMPAIGN_FOLDER);
			body.put(FIELD_NAME, campaignFolderName);
			if (parent != null) {
				body.put(FIELD_PARENT, parent.asJson());
			} else {
				body.put(FIELD_PARENT, project.asJson());
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
