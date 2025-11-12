package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.seleniumtests.uipage.htmlelements.FrameElement;

public class NativeSelect extends CommonSelectList implements ISelectList {

	private Select select;

	
	// for SPI
	public NativeSelect() {
		super(null, null);
	}
	
	public NativeSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
	}
	
	@Override
	public boolean isApplicable() {
		return "select".equalsIgnoreCase(parentElement.getTagName());
	}
	
	@Override
	public List<WebElement> getOptions() {

		select = new Select(parentElement);
		options = select.getOptions();
		return options;
	}
	
	@Override
	public void finalizeAction() {
		// nothing to finalize
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getDomAttribute("value");
	}

	@Override
	public String getOptionText(WebElement option) {
		return option.getText();
	}

	@Override
	public List<WebElement> getAllSelectedOptions() {
		List<WebElement> toReturn = new ArrayList<>();
		
		for (WebElement option : options) {
			if (option.isSelected()) {
				toReturn.add(option);
			}
		}
		
		return toReturn;
	}

	@Override
	public void deselectByIndex(Integer index) {
		select.deselectByIndex(index);

	}

	@Override
	public void deselectByText(String text) {
		select.deselectByVisibleText(text);

	}

	@Override
	public void deselectByValue(String value) {
		select.deselectByValue(value);

	}

	@Override
	public void selectByIndex(int index) {
		select.selectByIndex(index);

	}

	@Override
	public void selectByText(String text) {
		select.selectByVisibleText(text);

	}

	@Override
	public void selectByValue(String value) {
		select.selectByValue(value);

	}

	@Override
	public void setSelected(WebElement option) {
		select.selectByVisibleText(option.getText());

	}

	@Override
	public void setDeselected(WebElement option) {
		select.deselectByVisibleText(option.getText());

	}

}
