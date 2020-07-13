package com.seleniumtests.uipage.htmlelements.select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByAll;

import com.seleniumtests.uipage.htmlelements.CachedHtmlElement;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class AngularMaterialSelect implements ISelectList {

	protected List<WebElement> options;
	protected WebElement parentElement;
	protected FrameElement frameElement;
	private String optionsHolderClassName = null;
	

	public AngularMaterialSelect(WebElement parentElement, FrameElement frameElement) {
		this.parentElement = parentElement;
		this.frameElement = frameElement;
	}

	@Override
	public boolean isApplicable() {
		if ("mat-select".equalsIgnoreCase(parentElement.getTagName())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public List<WebElement> getOptions() {
		parentElement.findElement(By.className("mat-select-arrow")).click();
		
		if (optionsHolderClassName == null) {
			String classes = new HtmlElement("", new ByAll(By.className("mat-select-panel"), By.className("mat-select-content")), frameElement).getAttribute("class");
			if (classes.contains("mat-select-content")) {
				optionsHolderClassName = "mat-select-content";
			} else {
				optionsHolderClassName = "mat-select-panel";
			}
		}
		
		options = new HtmlElement("options", By.className(optionsHolderClassName), frameElement)
				.findHtmlElements(By.tagName("mat-option"))
				.stream()
				.map(CachedHtmlElement::new)
				.collect(Collectors.toList());
		return options;
	}

	@Override
	public void finalizeAction() {
		HtmlElement selectContent = new HtmlElement("options", By.className(optionsHolderClassName), frameElement);
		if (selectContent.isElementPresent()) {
			parentElement.sendKeys(Keys.ESCAPE);
		}
	}

	@Override
	public String getOptionValue(WebElement option) {
		return option.getAttribute("value");
	}
	
	@Override
	public String getOptionText(WebElement option) {
		return option.getText();
	}

	@Override
	public List<WebElement> getAllSelectedOptions() {
		List<WebElement> toReturn = new ArrayList<>();
		
		for (WebElement option : options) {
			if (option.getAttribute("class").contains("mat-selected")) {
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
		String selected = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).getAttribute("aria-selected");
		if (selected == null || "false".equals(selected)) {
			// here list should still be visible
			HtmlElement checkbox = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).findElement(By.tagName("mat-pseudo-checkbox"));
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
			HtmlElement checkbox = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).findElement(By.tagName("mat-pseudo-checkbox"));
			if (checkbox.isElementPresent(0)) {
				checkbox.click();
			} else {
				((CachedHtmlElement)option).getRealElement().click();
			}
		}

	}

}
