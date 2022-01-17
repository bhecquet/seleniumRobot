package com.seleniumtests.connectors.selenium.fielddetector;

import java.io.File;

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
	public static final String STATUS_URL = "/status";
	public static final String DETECT_URL = "/detect";
	public static final String DETECT_ERROR_URL = "/detectError";
	private static FieldDetectorConnector fieldDetectorConnector;
	
	
	/**
	 * For test only
	 * @param fieldDetectorConnector
	 */
	public static void resetFieldDetectorConnector() {
		FieldDetectorConnector.fieldDetectorConnector = null;
	}

	public static FieldDetectorConnector getInstance(String url) {
		if (fieldDetectorConnector == null) {
			fieldDetectorConnector = new FieldDetectorConnector(url);
		} 
		return fieldDetectorConnector;
	}
	
	/**
	 * 
	 * @param url	URL of the service
	 */
	private FieldDetectorConnector(String url) {
		this.url = url;
		
		try {
			HttpResponse<String> response = Unirest.get(url + STATUS_URL).asString();
			if (response.getStatus() != 200) {
				throw new ConfigurationException("Error contacting Image field detector: " + response.getBody());
			}
		} catch (UnirestException e) {
			throw new ConfigurationException("Image field detector cannot be contacted: " + e.getMessage());
		}
	}
	
	/**
	 * Detect fields and labels
	 * @param imageFile
	 * @return
	 */
	public JSONObject detect(File imageFile) {
		return detect(imageFile, 1);
	}
	
	/**
	 * Detect fields and labels
	 * @param imageFile
	 * @return
	 */
	public JSONObject detect(File imageFile, double resizeFactor) {
		return detect(imageFile, resizeFactor, DETECT_URL);
	}

	/**
	 * Detect error message and fieids in error
	 * @param imageFile
	 * @return
	 */
	public JSONObject detectError(File imageFile) {
		return detectError(imageFile, 1);
	}
	
	/**
	 * Detect error message and fieids in error
	 * @param imageFile
	 * @return
	 */
	public JSONObject detectError(File imageFile, double resizeFactor) {
		return detect(imageFile, resizeFactor, DETECT_ERROR_URL);
	}
	
	/**
	 * Send image to image field detector and retrieve the box and text
	 * @param imageFile
	 * @param resizeFactor
	 * @param urlPath
	 * @return
	 */
	private JSONObject detect(File imageFile, double resizeFactor, String urlPath) {
		if (imageFile == null) {
			throw new ScenarioException("Image file is null");
		}
		if (!imageFile.exists()) {
			throw new ScenarioException(String.format("Image file %s not found", imageFile.getAbsolutePath()));
		}
		
		HttpResponse<JsonNode> fieldDefinition = Unirest.post(url + urlPath)
				.field("resize", resizeFactor)
				.field("image", imageFile)
				.asJson();
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



