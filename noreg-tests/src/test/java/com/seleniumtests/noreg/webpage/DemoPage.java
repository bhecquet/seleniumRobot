package com.seleniumtests.noreg.webpage;

import java.io.IOException;

import org.openqa.selenium.By;

import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class DemoPage extends PageObject {
	
	private static final TextFieldElement textBox = new TextFieldElement("txt", By.id("txt"));
	private static final ButtonElement goButton = new ButtonElement("go Button", By.id("go"));
	private static final ButtonElement resetButton = new ButtonElement("reset Button", By.id("reset"));
	
	public DemoPage(String url) throws IOException {
		super(textBox, url);
	}
	
	public DemoPage fillForm() {
		goButton.click();
		return this;
	}
	
	public DemoPage resetForm() {
		resetButton.click();
		return this;
	}
	
	public String getText() {
		return textBox.getValue();
	}
}
