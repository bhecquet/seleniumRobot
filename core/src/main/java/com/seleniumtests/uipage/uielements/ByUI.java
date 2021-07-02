package com.seleniumtests.uipage.uielements;

import java.util.regex.Pattern;

public class ByUI {
	
	private Pattern leftOf;
	private Pattern rightOf;
	private Pattern above;
	private Pattern below;
	private ElementType type;
	private Pattern text; // text inside the field (e.g: text of a button)
	

	public static ByUI type(ElementType type) {
		ByUI by = new ByUI();
		by.type = type;
		return by;
	}
	
	public ByUI toLeftOf(Pattern label) {
		leftOf = label;
		return this;
	}
	
	
	public ByUI toLeftOf(String label) {
		leftOf = Pattern.compile(label);
		return this;
	}
	
	public ByUI toRightOf(Pattern label) {
		rightOf = label;
		return this;
	}
	
	public ByUI toRightOf(String label) {
		rightOf = Pattern.compile(label);
		return this;
	}
	
	public ByUI above(Pattern label) {
		above = label;
		return this;
	}
	
	
	public ByUI above(String label) {
		above = Pattern.compile(label);
		return this;
	}
	
	public ByUI below(Pattern label) {
		below = label;
		return this;
	}
	
	public ByUI below(String label) {
		below = Pattern.compile(label);
		return this;
	}
	
	
	public ByUI text(Pattern text) {
		this.text = text;
		return this;
	}
	
	public ByUI text(String text) {
		this.text = Pattern.compile(text);
		return this;
	}

	public Pattern getLeftOf() {
		return leftOf;
	}

	public Pattern getRightOf() {
		return rightOf;
	}

	public ElementType getType() {
		return type;
	}

	public Pattern getText() {
		return text;
	}

	public Pattern getAbove() {
		return above;
	}

	public Pattern getBelow() {
		return below;
	}


}
