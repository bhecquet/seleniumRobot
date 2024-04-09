package com.seleniumtests.uipage.htmlelements.select;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;

import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class AngularMaterialSelect extends AngularSelect implements ISelectList {

	private static final String CLASS_MAT_SELECT_PANEL = "mat-select-panel";
	private static final String CLASS_MAT_SELECT_CONTENT = "mat-select-content";
	
	// for SPI
	public AngularMaterialSelect() {
		this(null, null);
	}

	public AngularMaterialSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
		
		locatorClickToOpen = By.className("mat-select-arrow");
		locatorClickToclose = null;
		locatorParentOfDropdown = null; // is present in DOM only when options are displayed
		locatorOption = By.tagName("mat-option");
		locatorCheckboxInOption = By.tagName("mat-pseudo-checkbox");
		
		selectedOptionAttributeName = ATTR_ARIA_SELECTED;
		selectedOptionAttributeValue = "true";
		deselectedOptionAttributeValue = "false";
	}

	@Override
	public boolean isApplicable() {
		return "mat-select".equalsIgnoreCase(parentElement.getTagName());
	}
	
	@Override
	public List<WebElement> getOptions() {
		parentElement.findElement(locatorClickToOpen).click();
		
		if (locatorParentOfDropdown == null) {
			String classes = new HtmlElement("", new ByAll(By.className(CLASS_MAT_SELECT_PANEL), By.className(CLASS_MAT_SELECT_CONTENT)), frameElement).getAttribute("class");
			if (classes.contains(CLASS_MAT_SELECT_CONTENT)) {
				locatorParentOfDropdown = By.className(CLASS_MAT_SELECT_CONTENT);
			} else {
				locatorParentOfDropdown = By.className(CLASS_MAT_SELECT_PANEL);
			}
		}
		
		options = new HtmlElement("options", locatorParentOfDropdown, frameElement)
				.findHtmlElements(locatorOption)
				.stream()
				.map(CachedHtmlElement::new)
				.collect(Collectors.toList());
		return options;
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getAttribute("value");
	}
	
	@Override
	public String getOptionText(WebElement option) {
		if (!option.getAttribute("title").isEmpty()) {
			return option.getAttribute("title");
		} else {
			return option.getText();
		}
	}
}
