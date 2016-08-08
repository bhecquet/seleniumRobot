package com.seleniumtests.it.core.aspects;

import java.io.IOException;

import org.openqa.selenium.By;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import cucumber.api.java.en.When;

public class CalcPage extends PageObject {
	
	private static HtmlElement nonElement = new HtmlElement("none", By.id("none")); 
	
	private int result = 0;

	public CalcPage() throws IOException {
		super();
	}
	
	public CalcPage add(int a, int b) {
		result = a + b;
		doNothing();
		return this;
	}
	
	@When("add '(\\d+)' to '(\\d+)'")
	public CalcPage addC(Integer a, Integer b) {
		result = a + b;
		return this;
	}
	
	public CalcPage failAction() {
		throw new DriverExceptions("fail");
	}
	
	public CalcPage add(int a) {
		add(result, a);
		return this;
	}
	
	public CalcPage doNothing() {
		nonElement.doNothing();
		return this;
	}
	
	public ResultPage displayResult() throws IOException {
		return new ResultPage(result);
	}

}
