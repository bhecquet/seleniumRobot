package com.seleniumtests.connectors.selenium.fielddetector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

/**
 * Class that will connect to an 'image-field-detector' server to send image and get back buttons, text fields that are detected by Yolo algorithm
 * 
 * @author S047432
 *
 */
public class FieldDetectorConnector {

	private String url;
	private static final String STATUS_URL = "/status";
	private static final String DETECT_URL = "/detect";
	
	/**
	 * 
	 * @param url	URL of the service
	 */
	public FieldDetectorConnector(String url) {
		this.url = url + DETECT_URL;
		
		try {
			HttpResponse<String> response = Unirest.get(url + STATUS_URL).asString();
			if (response.getStatus() != 200) {
				throw new ConfigurationException("Error contacting Image field detector: " + response.getBody());
			}
		} catch (UnirestException e) {
			throw new ConfigurationException("Image field detector cannot be contacted: " + e.getMessage());
		}
	}
	
	public JSONObject detect(File imageFile) {
		if (imageFile == null) {
			throw new ScenarioException("Image file is null");
		}
		if (!imageFile.exists()) {
			throw new ScenarioException(String.format("Image file %s not found", imageFile.getAbsolutePath()));
		}
		
		HttpResponse<JsonNode> fieldDefinition = Unirest.post(url).field("image", imageFile).asJson();
		if (fieldDefinition.getStatus() != 200) {
			try {
				throw new ScenarioException("Field detector returned error: " + fieldDefinition.getBody().getObject().get("error"));
			} catch (NullPointerException e) {
				throw new ScenarioException("Field detector returned error");
			}
		}
		
		return fieldDefinition.getBody().getObject().getJSONObject(imageFile.getName());
		
	}
}
