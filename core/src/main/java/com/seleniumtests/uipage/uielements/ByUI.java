package com.seleniumtests.uipage.uielements;

import java.util.regex.Pattern;

public class ByUI {
	
	protected Pattern leftOf;
	protected Pattern rightOf;
	protected Pattern above;
	protected Pattern below;
	protected ElementType type;
	protected Pattern text; // text inside the field (e.g: text of a button)
	
	
	/**
	 * Search UI element to the right of label
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public static ByUI label(Pattern label) {
		return new ByUI().toTheRightOfLabel(label);
	}
	
	/**
	 * Search UI element to the left of label
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public static ByUI toLeftOf(Pattern label) {
		return new ByUI().toTheLeftOfLabel(label);
	}

	/**
	 * Search UI element to the left of label
	 * @param label		string to match the label text
	 * @return
	 */
	public static ByUI toLeftOf(String label) {
		return new ByUI().toTheLeftOfLabel(label);
	}

	/**
	 * Search UI element to the right of label
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public static ByUI toRightOf(Pattern label) {
		return new ByUI().toTheRightOfLabel(label);
	}

	/**
	 * Search UI element to the right of label
	 * @param label		string to match the label text
	 * @return
	 */
	public static ByUI toRightOf(String label) {
		return new ByUI().toTheRightOfLabel(label);
	}

	/**
	 * Search UI element above label
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public static ByUI above(Pattern label) {
		return new ByUI().aboveLabel(label);
	}

	/**
	 * Search UI element above label
	 * @param label		string to match the label text
	 * @return
	 */
	public static ByUI above(String label) {
		return new ByUI().aboveLabel(label);
	}

	/**
	 * Search UI element below label
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public static ByUI below(Pattern label) {
		return new ByUI().belowLabel(label);
	}

	/**
	 * Search UI element below label
	 * @param label		string to match the label text
	 * @return
	 */
	public static ByUI below(String label) {
		return new ByUI().belowLabel(label);
	}

	/**
	 * Search UI element with text containing
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public static ByUI text(Pattern label) {
		return new ByUI().textMatching(label);
	}
	
	/**
	 * Search UI element with text containing
	 * @param label		String to match the label text
	 * @return
	 */
	public static ByUI text(String label) {
		return new ByUI().textMatching(label);
	}

	public static ByUI type(ElementType type) {
		ByUI by = new ByUI();
		by.type = type;
		return by;
	}

	/**
	 * Search UI element to the left of label
	 * Equivalent to ByUI.toLeftOf(<pattern>)
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public ByUI toTheLeftOfLabel(Pattern label) {
		leftOf = label;
		return this;
	}

	/**
	 * Search UI element to the left of label
	 * Equivalent to ByUI.toLeftOf(<string>)
	 * @param label		String to match the label text
	 * @return
	 */
	public ByUI toTheLeftOfLabel(String label) {
		leftOf = Pattern.compile(label);
		return this;
	}

	/**
	 * Search UI element to the right of label
	 * Equivalent to ByUI.toRightOf(<pattern>)
	 * @param label		pattern to match the label text
	 * @return
	 */
	public ByUI toTheRightOfLabel(Pattern label) {
		rightOf = label;
		return this;
	}
	
	/**
	 * Search UI element to the right of label
	 * Equivalent to ByUI.toRightOf(<string>)
	 * @param label		string to match the label text
	 * @return
	 */
	public ByUI toTheRightOfLabel(String label) {
		rightOf = Pattern.compile(label);
		return this;
	}
	

	/**
	 * Search UI element above label
	 * Equivalent to ByUI.above(<pattern>)
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public ByUI aboveLabel(Pattern label) {
		above = label;
		return this;
	}
	

	/**
	 * Search UI element above label
	 * Equivalent to ByUI.above(<string>)
	 * @param label		string to match the label text
	 * @return
	 */
	public ByUI aboveLabel(String label) {
		above = Pattern.compile(label);
		return this;
	}
	

	/**
	 * Search UI element below label
	 * Equivalent to ByUI.below(<pattern>)
	 * @param label		pattern to match the label text
	 * @return
	 */
	public ByUI belowLabel(Pattern label) {
		below = label;
		return this;
	}

	/**
	 * Search UI element below label
	 * Equivalent to ByUI.below(<string>)
	 * @param label		string to match the label text
	 * @return
	 */
	public ByUI belowLabel(String label) {
		below = Pattern.compile(label);
		return this;
	}
	

	/**
	 * Search UI element with text containing
	 * Equivalent to ByUI.text(<pattern>)
	 * @param label		Pattern to match the label text
	 * @return
	 */
	public ByUI textMatching(Pattern text) {
		this.text = text;
		return this;
	}
	
	/**
	 * Search UI element with text containing
	 * Equivalent to ByUI.text(<string>)
	 * @param label		String to match the label text
	 * @return
	 */
	public ByUI textMatching(String text) {
		this.text = Pattern.compile(text);
		return this;
	}
	
	/**
	 * Search UI element with given type
	 * @param type
	 * @return
	 */
	public ByUI withType(ElementType type) {
		this.type = type;
		return this;
	}
	
	public String toString() {
		StringBuilder descr = new StringBuilder(String.format("ByUI(type='%s'", type));
		if (leftOf != null) {
			descr.append(String.format(", leftOf='%s'", leftOf.toString()));
		}
		if (rightOf != null) {
			descr.append(String.format(", rightOf='%s'", rightOf.toString()));
		}
		if (above != null) {
			descr.append(String.format(", above='%s'", above.toString()));
		}
		if (below != null) {
			descr.append(String.format(", below='%s'", below.toString()));
		}
		if (text != null) {
			descr.append(String.format(", text='%s'", text.toString()));
		}
		
		descr.append(")");
		return descr.toString();
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
