package com.seleniumtests.connectors.selenium.fielddetector;

import kong.unirest.json.JSONObject;

public class Label {

	private String text;
	private int top;
	private int bottom;
	private int left;
	private int right;
	private int width;
	private int height;
	
	
	public String getText() {
		return text;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	
	public static Label fromJson(JSONObject json) {
		Label label = new Label();
		
		label.top = json.getInt("top");
		label.bottom = json.getInt("bottom");
		label.left = json.getInt("left");
		label.right = json.getInt("right");
		label.width = json.getInt("width");
		label.height = json.getInt("height");
		label.text = json.optString("text", null);
		
		return label;
	}
}
