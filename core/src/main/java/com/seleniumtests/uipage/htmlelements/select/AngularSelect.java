package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class AngularSelect extends CommonSelectList implements ISelectList {


	// for SPI
	public AngularSelect() {
		super(null, null);
	}

	public AngularSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
	}

	@Override
	public boolean isApplicable() {
		if ("ng-select".equalsIgnoreCase(parentElement.getTagName())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isMultipleWithoutFind() {
        return parentElement.getAttribute("class").contains("ng-select-multiple");
    }
	
	@Override
	public List<WebElement> getOptions() {
		parentElement.findElement(By.className("ng-arrow-wrapper")).click();
		options = new HtmlElement("options", By.tagName("ng-dropdown-panel"), frameElement)
				.findHtmlElements(By.className("ng-option"))
				.stream()
				.map(CachedHtmlElement::new)
				.collect(Collectors.toList());
		return options;
	}

	@Override
	public void finalizeAction() {
		HtmlElement selectContent = new HtmlElement("options", By.tagName("ng-dropdown-panel"), frameElement);
		if (selectContent.isElementPresent(0)) {
			parentElement.findElement(By.className("ng-arrow-wrapper")).click();
		}
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getText();
	}
	
	@Override
	public String getOptionText(WebElement option) {
		return option.getText();
	}

	@Override
	public List<WebElement> getAllSelectedOptions() {
		List<WebElement> toReturn = new ArrayList<>();
		
		for (WebElement option : options) {
			if (option.getAttribute("aria-selected").contains("true")) {
				toReturn.add(option);
			}
		}
		
		return toReturn;
	}
	
	@Override
	public void deselectByIndex(Integer index) {
		try {
			WebElement option = options.get(index);
	        setDeselected(option);
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException("Cannot locate element with index: " + index);
		}

	}

	@Override
	public void deselectByText(String text) {
		boolean matched = false;
		for (WebElement option : options) {
            if (option.getText().equals(text)) {
            	setDeselected(option);
            	matched = true;
                break;
            }
        }
		if (!matched) {
	      throw new NoSuchElementException("Cannot locate element with text: " + text);
	    }

	}

	@Override
	public void deselectByValue(String value) {
		boolean matched = false;
		for (WebElement option : options) {
            if (getOptionValue(option).equals(value)) {
            	setDeselected(option);
            	matched = true;
                break;
            }
        }
		if (!matched) {
	      throw new NoSuchElementException("Cannot locate element with value: " + value);
	    }

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
            String selectedText = option.getText();

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
		String selected = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).getAttribute("aria-selected");
		if (selected == null || "false".equals(selected)) {
			// here list should still be visible
			HtmlElement checkbox = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).findElement(By.tagName("input"));
			if (checkbox.isElementPresent(0)) {
				checkbox.click();
			} else {
				((CachedHtmlElement)option).getRealElement().click();
			}
		}

	}

	@Override
	public void setDeselected(WebElement option) {
		if ("true".equals(option.getAttribute("aria-selected"))) {
			HtmlElement checkbox = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).findElement(By.tagName("input"));
			if (checkbox.isElementPresent(0)) {
				checkbox.click();
			} else {
				((CachedHtmlElement)option).getRealElement().click();
			}
		}

	}

}
