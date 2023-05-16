package com.seleniumtests.connectors.selenium.fielddetector;

import java.awt.Rectangle;
import java.util.List;
import java.util.stream.Collectors;

import kong.unirest.json.JSONObject;

public class Field {
	private Label label;
	private String className;
	private Field relatedField;
	
	public Field() {}
	
	public Field(int left, int right, int top, int bottom, String text, String className) {
		this.label = new Label(left, right, top, bottom, text);
		this.className = className;
	}
	
	public Field(int left, int right, int top, int bottom, String text, String className, Field relatedField) {
		this.label = new Label(left, right, top, bottom, text);
		this.className = className;
		this.relatedField = relatedField;
	}
	
	public Label getLabel() {
		return label;
	}

	public Field getRelatedField() {
		return relatedField;
	}
	
	public static Field fromJson(JSONObject json) {
		Field field = new Field();
		field.label = Label.fromJson(json);
		field.className = json.getString("class_name");
		if (json.get("related_field") != null) {
			field.relatedField = fromJson(json.getJSONObject("related_field"));
		}
		
		return field;
	}
	
	/**
	 * From a JSONObject get from seleniumServer, returns the list of fields
	 * 
	 * {
	"fields": [
		{
			"class_id": 4,
			"top": 142,
			"bottom": 166,
			"left": 174,
			"right": 210,
			"class_name": "field_with_label",
			"text": null,
			"related_field": {
				"class_id": 0,
				"top": 142,
				"bottom": 165,
				"left": 175,
				"right": 211,
				"class_name": "field",
				"text": null,
				"related_field": null,
				"with_label": false,
				"width": 36,
				"height": 23
			},
			"with_label": true,
			"width": 36,
			"height": 24
		},

	],
	"labels": [
		{
			"top": 3,
			"left": 16,
			"width": 72,
			"height": 16,
			"text": "Join Us",
			"right": 88,
			"bottom": 19
		},

	]
	"error": null,
	"version": "afcc45"
}
	 * 
	 * @param detectionData
	 * @return
	 */
	public static List<Field> fromDetectionData(JSONObject detectionData) {
		return ((List<JSONObject>)detectionData
				.getJSONArray("fields")
				.toList())
				.stream()
				.map(Field::fromJson)
				.collect(Collectors.toList());
	}
	
	public String getText() {
		return label.getText();
	}
	
	public Rectangle getRectangle() {
		return label.getRectangle();
	}
	
	public String getClassName() {
		return className;
	}
	
	/**
	 * Method for changing position of the element, for example to adapt coordinates to the screen (detection may be done on browser capture, not screen capture)
	 */
	public void changePosition(int xOffset, int yOffset) {
		label.changePosition(xOffset, yOffset);
	}
	
	public Rectangle getInnerFieldRectangle() {
		if (relatedField != null) {
			return relatedField.getRectangle();
		} else {
			return null;
		}
	}
	
	public String toString() {
		return String.format("%s[text=%s]: %s", className, getText(), getRectangle());
	}
	
	/**
	 * 2 fields match if the are of the same class and their positions match
	 * @param anOtherField
	 * @return
	 */
	public boolean match(Field anOtherField) {
		return className != null 
				&& className.equals(anOtherField.className) 
				&& label.match(anOtherField.label);
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof Field && this == obj) {
			return true;
		}

		Field field = (Field)obj;

		return this.getLabel().equals(field.getLabel())
				&& this.className.equals(field.className);
			 
	}
	
	@Override
	public int hashCode() {
		if (className != null) {
			return label.hashCode() + className.hashCode();
		} else {
			return label.hashCode();
		}
	}
}
