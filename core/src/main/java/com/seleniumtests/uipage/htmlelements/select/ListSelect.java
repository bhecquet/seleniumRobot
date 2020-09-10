package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.htmlelements.FrameElement;

public class ListSelect extends CommonSelectList implements ISelectList {

	
	// for SPI
	public ListSelect() {
		super(null, null);
	}
	
	public ListSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
	}

	@Override
	public boolean isApplicable() {
		if ("ul".equalsIgnoreCase(parentElement.getTagName())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public List<WebElement> getOptions() {
		options = parentElement.findElements(By.tagName("li"));
		return options;
	}

	@Override
	public void finalizeAction() {
		// nothing to do
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getAttribute("id");
	}

	@Override
	public String getOptionText(WebElement option) {
		return option.getText();
	}
	
	@Override
	public List<WebElement> getAllSelectedOptions() {
		return new ArrayList<>();
	}

	@Override
	public void deselectByIndex(Integer index) {
		throw new ScenarioException("Cannot deselect for list based select");
	}

	@Override
	public void deselectByText(String text) {
		throw new ScenarioException("Cannot deselect for list based select");
	}

	@Override
	public void deselectByValue(String value) {
		throw new ScenarioException("Cannot deselect for list based select");
	}

	@Override
	public void selectByIndex(int index) {
		try {
			WebElement option = options.get(index);
			setSelected(option);
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException("Cannot locate option with index: " + index);
		}
	}

	@Override
	public void selectByText(String text) {
		boolean matched = false;
		for (WebElement option : options) {
            String selectedText;
            if (!option.getAttribute("title").isEmpty()) {
                selectedText = option.getAttribute("title");
            } else {
                selectedText = option.getText();
            }

            if (selectedText.equals(text)) {
                setSelected(option);
                matched = true;
                break;
            }
        }
		
		if (!matched) {
	      throw new NoSuchElementException("Cannot locate element with text: " + text);
	    }

	}

	@Override
	public void selectByValue(String value) {
		boolean matched = false;
		for (WebElement option : options) {
            if (getOptionValue(option).equals(value)) {
                setSelected(option);
                matched =true;
                break;
            }
        }
		if (!matched) {
			throw new NoSuchElementException("Cannot locate option with value: " + value);
	    }

	}

	@Override
	public void setSelected(WebElement option) {
		option.click();
	}

	@Override
	public void setDeselected(WebElement option) {
		throw new ScenarioException("Cannot deselect for list based select");
	}
}
