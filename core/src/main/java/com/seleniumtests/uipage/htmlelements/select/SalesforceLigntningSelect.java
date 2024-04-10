package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.FrameElement;

public class SalesforceLigntningSelect extends AngularSelect implements ISelectList {

	
	// for SPI
	public SalesforceLigntningSelect() {
		this(null, null);
	}
	
	public SalesforceLigntningSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
		

		locatorClickToOpen = ByC.xTagName("button");
		locatorClickToclose = ByC.xTagName("button");
		locatorParentOfDropdown = By.tagName("ng-dropdown-panel"); // is present in DOM only when options are displayed
		locatorOption = ByC.xTagName("lightning-base-combobox-item");
		locatorCheckboxInOption = null;
		
		selectedOptionAttributeName = ATTR_ARIA_SELECTED;
		selectedOptionAttributeValue = "true";
		deselectedOptionAttributeValue = "false";
	}
	
	@Override
	public boolean isApplicable() {
		return "lightning-base-combobox".equalsIgnoreCase(parentElement.getTagName());
	}

	@Override
	public List<WebElement> getOptions() {
		parentElement.findElement(locatorClickToOpen).click();
		options = parentElement.findElements(locatorOption)
				.stream()
				.collect(Collectors.toList());
		return options;
	}

	/**
	 * Close the combobox in case it's a multi-select
	 */
	@Override
	public void finalizeAction() {
		handleAlert();
		if ("true".equalsIgnoreCase(parentElement.findElement(ByC.attribute("aria-haspopup", "listbox")).getDomAttribute("aria-expanded"))) {
			parentElement.findElement(locatorClickToclose).click();
		}
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getDomAttribute("data-value");
	}
	
	/**
	 * 	<lightning-base-combobox-item lightning-basecombobox_basecombobox="" data-item-id="input-1729-2" data-value="102" role="option" id="input-1729-2-1729" class="slds-media slds-listbox__option slds-media_center slds-media_small slds-listbox__option_plain" aria-selected="false">
	 * 		<span class="slds-media__figure slds-listbox__option-icon">
	 * 			<lightning-icon class="slds-icon-utility-check slds-icon_container">
	 * 				<lightning-primitive-icon lightning-primitiveicon_primitiveicon-host=""><svg lightning-primitiveIcon_primitiveIcon="" focusable="false" data-key="check" aria-hidden="true" class="slds-icon slds-icon-text-default slds-icon_x-small"><use lightning-primitiveIcon_primitiveIcon="" xlink:href="/_slds/icons/utility-sprite/svg/symbols.svg?cache=9.31.2#check"></use></svg></lightning-primitive-icon>
	 * 			</lightning-icon>
	 * 		</span>
	 * 		<span class="slds-media__body">
	 * 			<span title="Audi" class="slds-truncate">Audi</span>
	 * 		</span>
	 * 	</lightning-base-combobox-item>
	 * 
	 * 
	 */
	@Override
	public String getOptionText(WebElement option) {
		try {
			return option.findElement(By.xpath(".//span[@title]")).getAttribute("title");

		} catch (NoSuchElementException e) {
			// sometimes, the span sub-element is not present, so the above line fails. Fall back to getText() method
			return option.getText();
		}
	}
	
	@Override
	public boolean isSelected(WebElement option) {
		String selectedAttribute = option.getAttribute(ATTR_ARIA_CHECKED).toLowerCase();
		return selectedAttribute != null && selectedAttribute.contains("true");
			
	}
	

	@Override
	public void setDeselected(WebElement option) {
		if (isSelected(option)) {
			option.click();
		}
	}
	
	@Override
	public void setSelected(WebElement option) {
		if (!isSelected(option)) {
			option.click();
		}
	}
	
	@Override
	public void selectByValue(String value) {
		throw new UnsupportedOperationException("Cannot select by value for LWC select");
	}

	/**
	 * Try the quick track where we search option directly
	 * If this does not work, fall back to slower behaviour where all options are read
	 * This will at least help to have a nice error message
	 * @param text
	 */
	/*@Override
	public void selectByText(String text) {
		try {
			WebElement option = parentElement.findElement(By.xpath(String.format(".//*[@title='%s']", text)));
			setSelected(option);
		} catch (WebDriverException e) {
			super.selectByText(text);
		}
	}*/
	
	@Override
	public void deselectByIndex(Integer index) {
		throw new UnsupportedOperationException("Cannot deselect by index for LWC select");
	}

	@Override
	public void deselectByText(String text) {
		throw new UnsupportedOperationException("Cannot deselect by text for LWC select");
	}

	@Override
	public void deselectByValue(String value) {
		throw new UnsupportedOperationException("Cannot deselect for by value LWC select");
	}

}
