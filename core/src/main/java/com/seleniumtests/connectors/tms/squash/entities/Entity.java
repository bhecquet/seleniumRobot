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
import kong.unirest.json.JSONObject;

public class Entity {
	

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
		return Unirest.get(url).basicAuth(user, password).headerReplace("Content-Type", "application/json");
	}
	
	protected static HttpRequestWithBody buildPostRequest(String url) {
		return Unirest.post(url).basicAuth(user, password).headerReplace("Content-Type", "application/json");
	}
	
	protected static HttpRequestWithBody buildPatchRequest(String url) {
		return Unirest.patch(url).basicAuth(user, password).headerReplace("Content-Type", "application/json");
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
				for (String key: ((HttpResponse<JsonNode>)json).getBody().getObject().getJSONObject("_embedded").keySet()) {
					for (JSONObject entity: (List<JSONObject>)((HttpResponse<JsonNode>)json).getBody().getObject().getJSONObject("_embedded").getJSONArray(key).toList()) {
						finalJson.getJSONObject("_embedded").accumulate(key, entity);
					}
				}
			}
		}
		
		return finalJson;
	}
	
	protected static JSONObject getJSonResponse(HttpRequest request) {

		HttpResponse<JsonNode> response = request.asJson();
		
		if (response.getStatus() >= 400) {
			if (response.getBody() != null) {
				throw new ScenarioException(String.format("request to %s failed: %s", request.getUrl(), response.getBody().toPrettyString()));	
			} else {
				throw new ScenarioException(String.format("request to %s failed", request.getUrl()));	
			}
		}
		
		if (response.getStatus() == 204) {
			return new JSONObject();
		}
		
		if (response.getBody() == null) {
			return new JSONObject();
		}
		
		return response.getBody().getObject();
	}
}
