/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.uipage.htmlelements.ElementInfo;

import kong.unirest.core.GetRequest;
import kong.unirest.core.MultipartBody;
import kong.unirest.core.UnirestException;
import kong.unirest.core.json.JSONArray;
import kong.unirest.core.json.JSONException;
import kong.unirest.core.json.JSONObject;

public class SeleniumRobotElementInfoServerConnector extends SeleniumRobotServerConnector {
	
	public static final String LIST_ELEMENT_INFO_API_URL = "/elementinfo/api/elementinfos/";
	public static final String ELEMENT_INFO_API_URL = "/elementinfo/api/elementinfo/";

	private static SeleniumRobotElementInfoServerConnector infoServerConnector;

	/**
	 * Get instance of server
	 * @return
	 */
	public static SeleniumRobotElementInfoServerConnector getInstance() {
		if (infoServerConnector == null) {
			infoServerConnector = new SeleniumRobotElementInfoServerConnector(
					SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerActive(),
					SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerUrl(),
					SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerToken()
					);
		} 
		return infoServerConnector;
	}
	
	public SeleniumRobotElementInfoServerConnector(boolean useRequested, String url) {
		this(useRequested, url, null);
	}
		
	public SeleniumRobotElementInfoServerConnector(boolean useRequested, String url, String authToken) {
		super(useRequested, url, authToken);
		if (!active) {
			return;
		}
		active = isAlive();
		
		if (active) {
			getInfoFromServer();
		}
	}
	
	@Override
	public boolean isAlive() {
		return isAlive("/elementinfo/api/");
	}
	
	/**
	 * Retrieve all element information from the server
	 * @return
	 */
	public List<ElementInfo> getElementInfos() {
		if (!active) {
			throw new SeleniumRobotServerException("Server is not active");
		}
		try {
		
			GetRequest request = buildGetRequest(url + LIST_ELEMENT_INFO_API_URL) 
					.queryString("application", applicationId)
					.queryString("version", versionId)
					.queryString("format", "json");
		
			JSONArray eiJson = getJSonArray(request);
			
			List<ElementInfo> elementInfos = new ArrayList<>();
			for (int i=0; i < eiJson.length(); i++) {
				ElementInfo ei = ElementInfo.readFromJson(eiJson.getJSONObject(i).toString());
				elementInfos.add(ei);
			}
			return elementInfos;
			
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot get element infos", e);
		} 
	}
	
	/**
	 * get and store element info to json file
	 */
	public void getAndStoreElementInfos() {
		for (ElementInfo ei: getElementInfos()) {
			try {
				ei.exportToJsonFile(false, null);
				ei.exportToJsonFile(true, null);
			} catch (IOException e) {
				logger.error("error exporting ElementInfo to file: " + e.getMessage());
			}
		}
	}
	
	/**
	 * compare original checkout to updated data. Send updated data to server
	 */
	public void updateElementInfoToServer() {
		
		Map<String, ElementInfo> referenceElementInfos = ElementInfo.getAllStoredElementInfos(true);
		
		// get reference
		for (Entry<String, ElementInfo> currentElementInfoEntry: ElementInfo.getAllStoredElementInfos(false).entrySet()) {
			ElementInfo currentElementInfo = currentElementInfoEntry.getValue();
			ElementInfo referenceElementInfo = referenceElementInfos.get(currentElementInfoEntry.getKey());
			
			// if no reference exists, create it on server
			if (referenceElementInfo == null) {
				// TODO
			} else if (!currentElementInfo.getLastUpdate().equals(referenceElementInfo.getLastUpdate())) {
				try {
					MultipartBody request = buildPatchRequest(url + ELEMENT_INFO_API_URL)
							.field("application", applicationId)
							.field("uuid", currentElementInfo.getId())
							.field("name", currentElementInfo.getName());
					
					if (currentElementInfo.getCoordX().equals(referenceElementInfo.getCoordX())) {
						request = request.field("coordX", currentElementInfo.getCoordX().toString());
					}
					if (currentElementInfo.getCoordY().equals(referenceElementInfo.getCoordY())) {
						request = request.field("coordY", currentElementInfo.getCoordY().toString());
					}
					if (currentElementInfo.getWidth().equals(referenceElementInfo.getWidth())) {
						request = request.field("width", currentElementInfo.getWidth().toString());
					}
					if (currentElementInfo.getHeight().equals(referenceElementInfo.getHeight())) {
						request = request.field("height", currentElementInfo.getHeight().toString());
					}
					if (currentElementInfo.getLocator().equals(referenceElementInfo.getLocator())) {
						request = request.field("locator", currentElementInfo.getLocator());
					}
					if (currentElementInfo.getTagName().equals(referenceElementInfo.getTagName())) {
						request = request.field("tagName", currentElementInfo.getTagName());
					}
					if (currentElementInfo.getText().equals(referenceElementInfo.getText())) {
						request = request.field("text", currentElementInfo.getText());
					}
					if (currentElementInfo.getB64Image().equals(referenceElementInfo.getB64Image())) {
						request = request.field("b64Image", currentElementInfo.getB64Image());
					}
					if (currentElementInfo.getAttributes().equals(referenceElementInfo.getAttributes())) {
						request = request.field("attributes", new JSONObject(currentElementInfo.getAttributes()).toString());
					}
					if (currentElementInfo.getTotalSearch() != referenceElementInfo.getTotalSearch()) {
						request = request.field("totalSearch", "+" + Integer.toString(currentElementInfo.getTotalSearch() - referenceElementInfo.getTotalSearch()));
					}
					if (currentElementInfo.getTagStability() != referenceElementInfo.getTagStability()) {
						request = request.field("tagStability", "+" + Integer.toString(currentElementInfo.getTagStability() - referenceElementInfo.getTagStability()));
					}
					if (currentElementInfo.getTextStability() != referenceElementInfo.getTextStability()) {
						request = request.field("textStability", "+" + Integer.toString(currentElementInfo.getTextStability() - referenceElementInfo.getTextStability()));
					}
					if (currentElementInfo.getRectangleStability() != referenceElementInfo.getRectangleStability()) {
						request = request.field("rectangleStability", "+" + Integer.toString(currentElementInfo.getRectangleStability() - referenceElementInfo.getRectangleStability()));
					}
					if (currentElementInfo.getB64ImageStability() != referenceElementInfo.getB64ImageStability()) {
						request = request.field("b64ImageStability", "+" + Integer.toString(currentElementInfo.getB64ImageStability() - referenceElementInfo.getB64ImageStability()));
					}
					if (currentElementInfo.getAttributesStability() != referenceElementInfo.getAttributesStability()) {
						Map<String, Object> attributeStabilityDiff = new HashMap<>();
						for (String attribute: currentElementInfo.getAttributesStability().keySet()) {
							attributeStabilityDiff.put(attribute, "+" + Integer.toString(currentElementInfo.getAttributesStability().get(attribute) - referenceElementInfo.getAttributesStability().getOrDefault(attribute, 0)));
						}
						request = request.field("attributesStability", new JSONObject(attributeStabilityDiff).toString());
					}
					getJSonResponse(request);
				} catch (UnirestException e) {
					logger.warn("cannot update element info: " + e.getMessage());
				}
			}
		}
	}
}


