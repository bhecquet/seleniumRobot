package com.seleniumtests.uipage.uielements;

import java.util.Arrays;
import java.util.List;

public enum ElementType {
	
	
	TEXT_FIELD("field_with_label", "field_line_with_label", "field"),
	BUTTON("button"),
	RADIO("radio_with_label", "radio"),
	CHECKBOX("checkbox_with_label", "checkbox"),
	UNKNOWN;
	

	private List<String> classes;
	
	private ElementType(String ... classes) {
		this.classes = Arrays.asList(classes);
	}
	
	/**
	 * Returns the element type from class name
	 * @param className
	 */
	public static ElementType fromClassName(String className) {
		try {
			return ElementType.valueOf(className);
		} catch (IllegalArgumentException ex) {
			for (ElementType type : ElementType.values()) {
		        for (String matcher : type.classes) {
		          if (className.equalsIgnoreCase(matcher)) {
		            return type;
		          }
		        }
		      }
		      return UNKNOWN;
		} 
	}
}