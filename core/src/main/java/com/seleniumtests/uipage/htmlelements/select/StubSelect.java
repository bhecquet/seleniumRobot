package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.FrameElement;

public class StubSelect extends CommonSelectList implements ISelectList {

	public StubSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
	}
	
	public StubSelect() {
		super(null, null);
	}

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
		// nothing to do
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
		// stub
	}

	@Override
	public void deselectByText(String text) {
		// stub
	}

	@Override
	public void deselectByValue(String value) {
		// stub
	}

	@Override
	public void selectByIndex(int index) {
		// stub
	}

	@Override
	public void selectByText(String text) {
		// stub
	}

	@Override
	public void selectByValue(String value) {
		// stub
	}

	@Override
	public void setSelected(WebElement option) {
		// stub
	}

	@Override
	public void setDeselected(WebElement option) {
		// stub
	}
}
