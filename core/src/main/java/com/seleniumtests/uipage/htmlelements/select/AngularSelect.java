package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

/**
 * Class that handles all angular combobox, for several (all) ui libraries as the behaviour is almost the same
 * - you must click on the combobox to add the options in the DOM. Most of the time, it is displayed at the end of the DOM in an other container
 * - you select the requested option
 * - close select
 * <p>
 * To adapt to specific UI libraries, you need to redefine the fields
 * - locatorClickToOpen						locator on which you have to click to make the option display
 * - locatorClickToclose					locator to click to close, or null if "ESC" key should be sent
 * - locatorParentOfDropdown 				locator for the element which stores all options
 * - locatorOption							locator of the option element. This tag should have an attribute allowing to know if it's selected or not
 * - locatorCheckboxInOption				in case option displays a checkbox, this is the locator to find it
 * <p>
 * - selectedOptionAttributeName			name of the attribute in which we search for a value to determine if option is selected. This attribute is searched in the element located by 'locatorOption'
 * - selectedOptionAttributeValue			value of the attribute which says that option is selected
 * - deselectedOptionAttributeValue			value of the attribute which says that option is deselected
 * <p>
 * 
 * If this is not enough, you can also redefine the methods 
 * <code>public List<WebElement> getOptions()</code>: fill 'options' field with the list of HtmlElements representing options 
 * <code>public void finalizeAction()</code>: code to execute after each action, to close the combobox
 * <code>public String getOptionValue(WebElement option)</code>: get the value of an option
 * <code>public String getOptionText(WebElement option)</code>: get the text of an option
 * <code>public boolean isSelected(WebElement option)</code>: say whether the option is selected.
 *
 *
 */
public abstract class AngularSelect extends CommonSelectList {

	protected By locatorClickToOpen;
	protected By locatorClickToclose;		// locator to click to close, or null if "ESC" key should be sent
	protected By locatorParentOfDropdown; // is present in DOM only when options are displayed
	protected By locatorOption;				// locator of the option. This tag should have an attribute allowing to know if it's selected or not
	protected By locatorCheckboxInOption;	// in case option displays a checkbox, this is the locator to find it
	
	
	protected String selectedOptionAttributeName;		// name of the attribute in which we search for a value to determine if option is selected
	protected String selectedOptionAttributeValue;		// value of the attribute which says that option is selected
	protected String deselectedOptionAttributeValue;		// value of the attribute which says that option is deselected
	
	
	protected boolean debug = false;
	
	protected AngularSelect(WebElement parentElement, FrameElement frameElement) {
		super(parentElement, frameElement);
	}
	
	protected AngularSelect(WebElement parentElement, FrameElement frameElement, boolean debug) {
		super(parentElement, frameElement);
		this.debug = debug;
	}

	/**
	 * Method for getting list of options when this list is not available without clicking the list
	 * It first click on element located by 'locatorClickToOpen' element
	 * This should make the element located by 'locatorParentOfDropdown' available in DOM
	 * => we look for all options located by 'locatorOption'
	 * All these options are cached so that combobox can be closed. It will not be necessary to open it to know the value of an element.
	 */
	@Override
	public List<WebElement> getOptions() {
		parentElement.findElement(locatorClickToOpen).click();
		
		HtmlElement dropdownElement = new HtmlElement("options", locatorParentOfDropdown, frameElement);
		if (debug) {
			logger.info("drop down HTML");
			logger.info(dropdownElement.getAttribute("outerHTML"));
		}
		
		options = dropdownElement
				.findHtmlElements(locatorOption)
				.stream()
				.map(CachedHtmlElement::new)
				.collect(Collectors.toList());
		
		return options;
	}
	
	public void debug() {
		getOptions();
		
		logger.info("------------------- OPTIONS --------------");
		for (WebElement option: options) {
			logger.info(option);
			try {
				logger.info("text: '{}'", getOptionText(option));
			} catch (Exception e) {
				logger.info("text cannot be retrieved: {}", e.getMessage());
			}
			try {
				logger.info("value: '{}'", getOptionValue(option));
			} catch (Exception e) {
				logger.info("value cannot be retrieved: {}", e.getMessage());
			}
			try {
				logger.info("isSelected: '{}'", isSelected(option));
			} catch (Exception e) {
				logger.info("is selected cannot be retrieved: {}", e.getMessage());
			}
			logger.info("-----------------------------------------");
		}
		
		finalizeAction();
	}

	/**
	 * Finalize any action. As for these combobox, we must click on it to get option list, it's a way to close it
	 * if 'locatorClickToclose' is not null, we click on it if 'locatorParentOfDropdown' is present
	 * else, we send "ESC" on parent 
	 */
	@Override
	public void finalizeAction() {
		handleAlert();
		HtmlElement selectContent = new HtmlElement("options", locatorParentOfDropdown, frameElement);

		if (selectContent.isElementPresent(0)) {
			if (locatorClickToclose != null) {
				parentElement.findElement(locatorClickToclose).click();
			} else {
				parentElement.sendKeys(Keys.ESCAPE);
			}
		}

	}


	/**
	 * Get the value of option. By default, this is the text of the option.
	 * If it does not fit your needs, override it
	 */
	@Override
	public String getOptionValue(WebElement option) {
		return option.getText();
	}
	
	/**
	 * Get the text of option. By default, this is the getText() of the element.
	 * If it does not fit your needs, override it
	 */
	@Override
	public String getOptionText(WebElement option) {
		return option.getText();
	}

	/**
	 * Returns 'true' if element is selected.
	 * Therefore, we search for attribute 'selectedOptionAttributeName' in the option element
	 * If the value of this attribute contains 'selectedOptionAttributeValue', then we assume option is selected
	 * @param option	option to check for selection
	 */
	public boolean isSelected(WebElement option) {
		String selectedAttribute = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).getAttribute(selectedOptionAttributeName);
		return selectedAttribute != null && selectedAttribute.contains(selectedOptionAttributeValue);
			
	}

	/**
	 * Get list of all selected options
	 */
	@Override
	public List<WebElement> getAllSelectedOptions() {
		return options.stream()
			.filter(this::isSelected)
			.toList();
	}

	@Override
	public void deselectByIndex(Integer index) {
		try {
			WebElement option = options.get(index);
	        setDeselected(option);
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException("Cannot find option with index: " + index);
		}

	}

	@Override
	public void deselectByText(String text) {
		boolean matched = false;
		List<String> knownOptions = new ArrayList<>();
		
		for (WebElement option : options) {
			String optionText = getOptionText(option);
			knownOptions.add(optionText);
			
			if (optionText.equals(text)) {
            	setDeselected(option);
            	matched = true;
                break;
            }
        }
		if (!matched) {
			throw new NoSuchElementException(String.format("Cannot find option with text: %s. Known options are: %s", text, StringUtils.join(knownOptions, ";")));
	    }

	}

	@Override
	public void deselectByValue(String value) {
		boolean matched = false;
		List<String> knownOptions = new ArrayList<>();
		
		for (WebElement option : options) {
			String optionValue = getOptionValue(option);
			knownOptions.add(optionValue);
			
            if (optionValue.equals(value)) {
            	setDeselected(option);
            	matched = true;
                break;
            }
        }
		if (!matched) {
			throw new NoSuchElementException(String.format("Cannot find option with value: %s. Known options are: %s", value, StringUtils.join(knownOptions, ";")));
	    }

	}
	

	@Override
	public void selectByIndex(int index) {
		try {
			WebElement option = options.get(index);
			setSelected(option);
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException("Cannot find option with index: " + index);
		}
	}


	@Override
	public void selectByText(String text) {
		boolean matched = false;
		List<String> knownOptions = new ArrayList<>();
		
		for (WebElement option : options) {

			String optionText = getOptionText(option);
			knownOptions.add(optionText);
            if (optionText.equals(text)) {
                setSelected(option);
                matched = true;
                break;
            }
        }
		
		if (!matched) {
	      throw new NoSuchElementException(String.format("Cannot find option with text: %s. Known options are: %s", text, StringUtils.join(knownOptions, ";")));
	    }

	}


	@Override
	public void selectByValue(String value) {
		boolean matched = false;
		List<String> knownOptions = new ArrayList<>();
		
		for (WebElement option : options) {
			String optionValue = getOptionValue(option);
			knownOptions.add(optionValue);
			
            if (optionValue.equals(value)) {
                setSelected(option);
                matched =true;
                break;
            }
        }
		if (!matched) {
			throw new NoSuchElementException(String.format("Cannot find option with value: %s. Known options are: %s", value, StringUtils.join(knownOptions, ";")));
	    }

	}
	
	private HtmlElement getRealOptionElement(WebElement option) {
		if (option instanceof CachedHtmlElement) {
			return ((HtmlElement)((CachedHtmlElement)option).getRealElement());
		} else if (option instanceof HtmlElement htmlElementOption) {
			return htmlElementOption;
		} else {
			throw new ClassCastException("getRealOptionElement() can only handle HtmlElement options");
		}
	}


	/**
	 * Select the option
	 * if option has a checkbox / radio button which is located by 'locatorCheckboxInOption', then we click on it
	 */
	@Override
	public void setSelected(WebElement option) {
		if (!isSelected(option)) {
			// here list should still be visible
			HtmlElement checkbox;
			if (locatorCheckboxInOption != null) {
				checkbox = getRealOptionElement(option).findElement(locatorCheckboxInOption);
			} else {
				checkbox = null;
			}
				
			if (checkbox != null && checkbox.isElementPresent(0)) {
				checkbox.click();
			} else {
				getRealOptionElement(option).click();
			}
		}

	}

	/**
	 * deselect the option
	 * if option has a checkbox / radio button which is located by 'locatorCheckboxInOption', then we click on it
	 */
	@Override
	public void setDeselected(WebElement option) {
		if (isSelected(option)) {
			HtmlElement checkbox;
			if (locatorCheckboxInOption != null) {
				checkbox = getRealOptionElement(option).findElement(locatorCheckboxInOption);
			} else {
				checkbox = null;
			}
			
			if (checkbox != null && checkbox.isElementPresent(0)) {
				checkbox.click();
			} else {
				getRealOptionElement(option).click();
			}
		}
	}
}
