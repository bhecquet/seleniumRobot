package com.seleniumtests.ut.core.runner.cucumber;

import java.io.IOException;

import org.openqa.selenium.By;

import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class PageForActions extends PageObject {
	
	private static TextFieldElement textField = new TextFieldElement("text", By.id("text"));

	public PageForActions() throws IOException {
		super();
	}

}
