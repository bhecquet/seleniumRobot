package com.seleniumtests.uipage.uielements;

public class TextFieldUiElement extends UiElement {

	public TextFieldUiElement(ByUI by) {
		super(by.withType(ElementType.TEXT_FIELD));
	}
}
