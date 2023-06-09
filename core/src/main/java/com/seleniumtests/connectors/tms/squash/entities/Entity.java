package com.seleniumtests.connectors.tms.squash.entities;

import java.util.List;

import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.GetRequest;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.PagedList;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONElement;
import kong.unirest.json.JSONObject;

public class Entity {
	
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String MIMETYPE_APPLICATION_JSON = "application/json";
	protected static final String FIELD_NAME = "name";
	protected static final String FIELD_ID = "id";
	protected static final String FIELD_TYPE = "_type";
	protected static final String FIELD_EMBEDDED = "_embedded";
	protected static final String FIELD_PARENT = "parent";
	protected static final String FIELD_CAMPAIGNS = "campaigns";
	protected static final String FIELD_CAMPAIGN_FOLDERS = "campaign-folders";
	protected static final String FIELD_PROJECTS = "projects";
	
	protected static final String TYPE_PROJECT = "project";
	protected static final String TYPE_CAMPAIGN_FOLDER = "campaign-folder";
	protected static final String TYPE_CAMPAIGN = "campaign";
	protected static final String TYPE_ITERATION = "iteration";
	
	private static String user;
	private static String password;
	
	protected static String apiRootUrl;
	protected String url;
	protected int id;
	protected String name;
	
	public static void configureEntity(String user, String password, String apiRootUrl) {
		Entity.user = user;
		Entity.password = password;
		Entity.apiRootUrl = apiRootUrl;
	}
	
	public Entity( String url, int id, String name) {
		this.url = url;
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}

	protected static GetRequest buildGetRequest(String url) {
		return Unirest.get(url).basicAuth(user, password).headerReplace(HEADER_CONTENT_TYPE, MIMETYPE_APPLICATION_JSON);
	}
	
	protected static HttpRequestWithBody buildPostRequest(String url) {
		return Unirest.post(updateUrl(url)).basicAuth(user, password).headerReplace(HEADER_CONTENT_TYPE, MIMETYPE_APPLICATION_JSON);
	}
	
	protected static HttpRequestWithBody buildPatchRequest(String url) {
		return Unirest.patch(updateUrl(url)).basicAuth(user, password).headerReplace(HEADER_CONTENT_TYPE, MIMETYPE_APPLICATION_JSON);
	}
	
	/**
	 * As POST and PATCH requests won't follow redirect (HTTP RFC 2616), and if squash is behind a reverse proxy which does the HTTPS end point, all URLs that squash will 
	 * reply will be in HTTP
	 * So, replace http by https if root url is in https
	 * @param url
	 * @return
	 */
	private static String updateUrl(String url) {
		if (Entity.apiRootUrl.startsWith("https://")) {
			return url.replace("http://", "https://");
		} else {
			return url;
		}
	}
	
	/**
	 * Retrieve a list of objects among multiple pages
	 * Search keys in "_embedded" value to accumulate them
	 * @param request
	 * @return
	 */
	protected static JSONObject getPagedJSonResponse(HttpRequest<?> request) {
		JSONObject finalJson = null;
		
		PagedList<JsonNode> result =  request
				.queryString("size", 20).asPaged(
                        r -> ((HttpRequest) r).asJson(),
                        r -> {
	                        	JSONObject links = ((HttpResponse<JsonNode>) r).getBody().getObject().getJSONObject("_links");
	                        	if (links.has("next")) {
	                        		return links.getJSONObject("next").getString("href");
	                        	} else {
	                        		return null;
	                        	}
                        	}
                );

		for (Object json: result.toArray()) {
			if (finalJson == null) {
				finalJson = ((HttpResponse<JsonNode>)json).getBody().getObject();
			} else {
				for (String key: ((HttpResponse<JsonNode>)json).getBody().getObject().getJSONObject(FIELD_EMBEDDED).keySet()) {
					for (JSONObject entity: (List<JSONObject>)((HttpResponse<JsonNode>)json).getBody().getObject().getJSONObject(FIELD_EMBEDDED).getJSONArray(key).toList()) {
						finalJson.getJSONObject(FIELD_EMBEDDED).accumulate(key, entity);
					}
				}
			}
		}
		
		return finalJson;
	}
	
	/**
	 * get JSONObject in reply to the request
	 * if reply code is 204 or if body is empty, object is empty
	 * @param request
	 * @return
	 */
	protected static JSONObject getJSonResponse(HttpRequest<?> request) {
		JsonNode responseBody =  getJSonNodeResponse(request);
		if (responseBody == null) {
			return new JSONObject();
		} else {
			return responseBody.getObject();
		}
	}
	
	/**
	 * get JSONArray in reply to the request
	 * if reply code is 204 or if body is empty, array is empty
	 * @param request
	 * @return
	 */
	protected static JSONArray getArrayJSonResponse(HttpRequest<?> request) {
		JsonNode responseBody =  getJSonNodeResponse(request);
		if (responseBody == null) {
			return new JSONArray();
		} else {
			return responseBody.getArray();
		}
	}
	
	private static JsonNode getJSonNodeResponse(HttpRequest<?> request) {

		HttpResponse<JsonNode> response = request.asJson();
		
		if (response.getStatus() >= 400) {
			if (response.getBody() != null) {
				throw new ScenarioException(String.format("request to %s failed[%d]: %s", request.getUrl(), response.getStatus(), response.getBody().toPrettyString()));	
			} else {
				throw new ScenarioException(String.format("request to %s failed", request.getUrl()));	
			}
		}
		
		if (response.getStatus() == 204) {
			return null;
		}

		return response.getBody();
	}
}
