package com.seleniumtests.uipage.htmlelements.select;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.FrameElement;

public class NgSelect extends AngularSelect implements ISelectList {


	// for SPI
	public NgSelect() {
		this(null, null);
	}

	public NgSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
		
		locatorClickToOpen = By.className("ng-arrow-wrapper");
		locatorClickToclose = By.className("ng-arrow-wrapper");
		locatorParentOfDropdown = By.tagName("ng-dropdown-panel"); // is present in DOM only when options are displayed
		locatorOption = By.className("ng-option");
		locatorCheckboxInOption = By.tagName("input");
		
		selectedOptionAttributeName = ATTR_ARIA_SELECTED;
		selectedOptionAttributeValue = "true";
		deselectedOptionAttributeValue = "false";
	}

	public static String getUiLibrary() {
		return "Angular";
	}

	@Override
	public boolean isApplicable() {
		return "ng-select".equalsIgnoreCase(parentElement.getTagName());
	}
	
	@Override
	public boolean isMultipleWithoutFind() {
        return parentElement.getDomAttribute("class").contains("ng-select-multiple");
    }

}
