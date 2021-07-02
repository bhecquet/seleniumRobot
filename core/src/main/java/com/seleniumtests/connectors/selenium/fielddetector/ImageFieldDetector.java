package com.seleniumtests.connectors.selenium.fielddetector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.uipage.uielements.UiElement;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

/**
 * Class for detecting fields and labeld for a single image
 * @author S047432
 *
 */
public class ImageFieldDetector {

	private static final Logger logger = SeleniumRobotLogger.getLogger(ImageFieldDetector.class);
	private File image;
	private FieldDetectorConnector fieldDetectorInstance;
	private JSONObject detectionJsonData;
	
	public ImageFieldDetector(File image) {
		this.image = image;
		fieldDetectorInstance = SeleniumTestsContextManager.getThreadContext().getFieldDetectorInstance();
	}
	
	/**
	 * get list of fields for the image
	 * if detection has not already been done, do it
	 * @return
	 */
	public List<Field> detectFields() {
		if (detectionJsonData == null) {
			detectionJsonData = fieldDetectorInstance.detect(image);
		}
		
		List<Field> fields = new ArrayList<>();
		try {
			List<JSONObject> jsonFields = detectionJsonData.getJSONArray("fields").toList();
			
			
			for (JSONObject jsonNode: jsonFields) {
				fields.add(Field.fromJson(jsonNode));
			}

		} catch (JSONException e) {
			logger.warn("No fields returned");
		}
		return fields;
	}
		
	/**
	 * get list of labels for the image
	 * if detection has not already been done, do it
	 * @return
	 */
	public List<Label> detectLabels() {
		if (detectionJsonData == null) {
			detectionJsonData = fieldDetectorInstance.detect(image);
		}

		List<Label> labels = new ArrayList<>();
		
		try {
			List<JSONObject> jsonLabels = detectionJsonData.getJSONArray("labels").toList();
			
			for (JSONObject jsonNode: jsonLabels) {
				labels.add(Label.fromJson(jsonNode));
			}
		} catch (JSONException e) {
			logger.warn("No labels returned");
		}
		
		return labels;
	}
}
