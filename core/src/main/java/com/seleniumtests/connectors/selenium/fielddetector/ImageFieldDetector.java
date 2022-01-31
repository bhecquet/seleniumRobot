package com.seleniumtests.connectors.selenium.fielddetector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
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
	private double resizeFactor;
	private FieldDetectorConnector fieldDetectorInstance;
	private JSONObject detectionJsonData;
	private FieldType fieldTypeToDetect;
	
	public enum FieldType {
		ALL_FORM_FIELDS,			// all fields: radio, text fields, buttons, ...
		ERROR_MESSAGES_AND_FIELDS;	// error messages and fields in error
	}
	
	public ImageFieldDetector(File image) {
		this(image, 1);
	}
	public ImageFieldDetector(File image, double resizeFactor) {
		this(image, resizeFactor, FieldType.ALL_FORM_FIELDS);
	}
	
	/**
	 * Initialize the image field detector
	 * Depending on fieldTypeToDetect, it will use either a yolo model or an other
	 * @param image					the image to detect fields in
	 * @param resizeFactor			resizing of input image. With a factor > 1, detection will be better but longer
	 * @param fieldTypeToDetect		ALL_FORM_FIELDS: all fields: radio, text fields, buttons, ...
	 * 								ERROR_MESSAGE_AND_FIELDS: error messages and fields in error
	 */
	public ImageFieldDetector(File image, double resizeFactor, FieldType fieldTypeToDetect) {
		this.image = image;
		this.resizeFactor = resizeFactor;
		this.fieldTypeToDetect = fieldTypeToDetect;
		fieldDetectorInstance = SeleniumTestsContextManager.getThreadContext().getFieldDetectorInstance();
		if (fieldDetectorInstance == null) {
			throw new ConfigurationException("Image Field detector has not been properly configured");
		}
	}
	
	private void callDetector() {
		if (detectionJsonData == null) {
			if (fieldTypeToDetect == FieldType.ALL_FORM_FIELDS) {
				detectionJsonData = fieldDetectorInstance.detect(image, resizeFactor);
			} else if (fieldTypeToDetect == FieldType.ERROR_MESSAGES_AND_FIELDS) {
				detectionJsonData = fieldDetectorInstance.detectError(image, resizeFactor);
			}
		}
	}
	
	/**
	 * get list of fields for the image
	 * if detection has not already been done, do it
	 * @return
	 */
	public List<Field> detectFields() {
		callDetector();
		
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
		callDetector();

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
