package com.seleniumtests.uipage.htmlelements.select;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class SalesforceLigntningSelect implements ISelectList {
	

	protected List<WebElement> options;
	protected WebElement parentElement;
	protected FrameElement frameElement;

	public SalesforceLigntningSelect(WebElement parentElement, FrameElement frameElement) {
		this.parentElement = parentElement;
		this.frameElement = frameElement;
	}
	
	@Override
	public boolean isApplicable() {
		if ("lightning-base-combobox".equalsIgnoreCase(parentElement.getTagName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<WebElement> getOptions() {
		parentElement.findElement(ByC.xTagName("input")).click();
		options = parentElement.findElements(ByC.xTagName("lightning-base-combobox-item"))
				.stream()
				.collect(Collectors.toList());
		return options;
	}

	/**
	 * Close the combobox
	 */
	@Override
	public void finalizeAction() {
		if ("true".equalsIgnoreCase(parentElement.findElement(ByC.attribute("role", "combobox")).getAttribute("aria-expanded"))) {
			parentElement.findElement(ByC.xTagName("input")).click();
		}
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getAttribute("id");
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
		return option.findElements(ByC.xTagName("span")).get(1).findElement(ByC.xTagName("span")).getAttribute("title");
	}

	@Override
	public List<WebElement> getAllSelectedOptions() {
		return options.stream()
			.filter(el -> "true".equalsIgnoreCase(el.getAttribute("aria-selected")))
			.collect(Collectors.toList());
	}

	@Override
	public void setSelected(WebElement option) {
		if ("false".equals(option.getAttribute("aria-selected"))) {
			option.click();
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
		for (WebElement option : options) { // lightning-base-combobox-item element

            if (getOptionText(option).equals(text)) {
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
		throw new ScenarioException("Cannot select by value for LWC select");
	}
	
	@Override
	public void deselectByIndex(Integer index) {
		throw new ScenarioException("Cannot deselect by index for LWC select");
	}

	@Override
	public void deselectByText(String text) {
		throw new ScenarioException("Cannot deselect by text for LWC select");
	}

	@Override
	public void deselectByValue(String value) {
		throw new ScenarioException("Cannot deselect for by value LWC select");
	}

	@Override
	public void setDeselected(WebElement option) {
		if ("true".equals(option.getAttribute("aria-selected"))) {
			option.click();
		}

	}

	@Override
	public WebElement getParentElement() {
		return parentElement;
	}
}
