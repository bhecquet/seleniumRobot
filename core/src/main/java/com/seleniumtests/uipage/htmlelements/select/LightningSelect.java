package com.seleniumtests.uipage.htmlelements.select;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.FrameElement;

/**
 * Angular select list with LDS look'n feel
 * @author S047432
 *
 */
public class LightningSelect extends AngularSelect implements ISelectList {


	// for SPI
	public LightningSelect() {
		this(null, null);
	}
	
	public LightningSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement, false);
		
		locatorClickToOpen = By.className("slds-combobox__input");
		locatorClickToclose = By.className("slds-combobox__input"); // the same because ESC causes wait
		locatorParentOfDropdown = By.className("cdk-overlay-connected-position-bounding-box"); 
		locatorOption = By.className("slds-listbox__option_plain");
		locatorCheckboxInOption = null;
		
		selectedOptionAttributeName = ATTR_ARIA_SELECTED;
		selectedOptionAttributeValue = "true";
		deselectedOptionAttributeValue = "false";
	}

	public static String getUiLibrary() {
		return "Lightning"; // mimic salesforce lightning for angular applications
	}
	
	@Override
	public String getOptionText(WebElement option) {
		return option.findElement(By.className("slds-truncate")).getAttribute("title");
	}

	@Override
	public boolean isApplicable() {
		return parentElement.getAttribute("class").contains("slds-combobox_container");
	}
	
	@Override
	public boolean isMultipleWithoutFind() {
        return false;
    }

}
