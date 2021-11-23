package com.seleniumtests.connectors.selenium.fielddetector;

import java.awt.Rectangle;

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
}
