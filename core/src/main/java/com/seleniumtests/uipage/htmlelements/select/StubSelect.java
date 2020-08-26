package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

public class StubSelect implements ISelectList {

	@Override
	public boolean isApplicable() {
		return false;
	}

	@Override
	public List<WebElement> getOptions() {
		return new ArrayList<>();
	}

	@Override
	public void finalizeAction() {

	}

	@Override
	public String getOptionValue(WebElement option) {
		return "";
	}

	@Override
	public String getOptionText(WebElement option) {
		return "";
	}

	@Override
	public List<WebElement> getAllSelectedOptions() {
		return new ArrayList<>();
	}

	@Override
	public void deselectByIndex(Integer index) {
	}

	@Override
	public void deselectByText(String text) {
	}

	@Override
	public void deselectByValue(String value) {
	}

	@Override
	public void selectByIndex(int index) {
	}

	@Override
	public void selectByText(String text) {
	}

	@Override
	public void selectByValue(String value) {
	}

	@Override
	public void setSelected(WebElement option) {
	}

	@Override
	public void setDeselected(WebElement option) {
	}

	@Override
	public WebElement getParentElement() {
		return null;
	}

}
