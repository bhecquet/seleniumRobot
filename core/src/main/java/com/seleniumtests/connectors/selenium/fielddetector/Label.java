package com.seleniumtests.connectors.selenium.fielddetector;

import java.awt.Rectangle;

import com.seleniumtests.util.StringUtility;

import kong.unirest.json.JSONObject;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class Label {

	private String text;
	private int top;
	private int bottom;
	private int left;
	private int right;
	private int width;
	private int height;
	
	private SimilarityStrategy strategy = new JaroWinklerStrategy();
	private StringSimilarityService stringService = new StringSimilarityServiceImpl(strategy);
	
	public Label() {}
	
	// for test purpose
	public Label(int left, int right, int top, int bottom, String text) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.text = text;
		this.width = right - left;
		this.height = bottom - top;
	}
	
	
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

	public Rectangle getRectangle() {
		return new Rectangle(left, top, width, height);
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
	
	/**
	 * Check if label center is inside a Field rectangle
	 * @return
	 */
	public boolean isInside(Field field) {
		return isInside(field.getLabel());
	}
	
	public boolean isInside(Label label) {
		int centerX = (right + left) / 2;
		int centerY = (top + bottom) / 2;
		
		return centerX > label.left 
				&& centerX < label.right
				&& centerY > label.top
				&& centerY < label.bottom;
	}
	
	/**
	 * Is field on the right of this label
	 * - center of field is on the right of center of label
	 * - center (Y) of field is between top and bottom of label
	 * @param field
	 * @return
	 */
	public boolean isFieldRightOf(Field field) {
		int centerX = (right + left) / 2;
		int fieldCenterX = (field.getLabel().right + field.getLabel().left) / 2;
		int fieldCenterY = (field.getLabel().top + field.getLabel().bottom) / 2;

		return centerX < fieldCenterX 
				&& fieldCenterY > top
				&& fieldCenterY < bottom;
	}
	
	/**
	 * Is field on the right of this label
	 * - center of field is on the left of center of label
	 * - center (Y) of field is between top and bottom of label
	 * @param field
	 * @return
	 */
	public boolean isFieldLeftOf(Field field) {
		int centerX = (right + left) / 2;
		int fieldCenterX = (field.getLabel().right + field.getLabel().left) / 2;
		int fieldCenterY = (field.getLabel().top + field.getLabel().bottom) / 2;
		
		return centerX > fieldCenterX 
				&& fieldCenterY > top
				&& fieldCenterY < bottom;
	}
	
	/**
	 * Is field above this label
	 * 
	 * - center (Y) of field is on the top of center of label
	 * - center (X) of field is between left and right of label
	 * @param field
	 * @return
	 */
	public boolean isFieldAbove(Field field) {
		int labelCenterY = (top + bottom) / 2;
		int fieldCenterX = (field.getLabel().right + field.getLabel().left) / 2;
		int fieldCenterY = (field.getLabel().top + field.getLabel().bottom) / 2;
		
		return labelCenterY > fieldCenterY
				&& fieldCenterX > left
				&& fieldCenterX < right;
	}
	
	/**
	 * Is field below this label
	 * - center (Y) of field is on the top of center of label
	 * - center (X) of field is between left and right of label
	 * @param field
	 * @return
	 */
	public boolean isFieldBelow(Field field) {
		int labelCenterY = (top + bottom) / 2;
		int fieldCenterX = (field.getLabel().right + field.getLabel().left) / 2;
		int fieldCenterY = (field.getLabel().top + field.getLabel().bottom) / 2;
		
		return labelCenterY < fieldCenterY
				&& fieldCenterX > left
				&& fieldCenterX < right;
	}
	
	
	
	public void changePosition(int xOffset, int yOffset) {
		left += xOffset;
		top += yOffset;
		right = left + width;
		bottom = top + height;
	}
	
	/**
	 * Check if 'anOtherLabel' matches this label
	 * - text matches
	 * - position matches
	 * @param anOtherLabel
	 * @return
	 */
	public boolean match(Label anOtherLabel) {
		if (text != null && anOtherLabel.text != null
				&& (text.equals(anOtherLabel.text) 
				|| stringService.score(text, anOtherLabel.text) > 0.9)
				&& isInside(anOtherLabel)) {
			return true;
		} else {
			return false;
		}
	}
}
