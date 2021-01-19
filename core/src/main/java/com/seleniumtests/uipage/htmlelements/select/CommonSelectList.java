package com.seleniumtests.uipage.htmlelements.select;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.ElementInfo;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class CommonSelectList implements ISelectList {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(CommonSelectList.class);
	protected WebElement parentElement;
	protected FrameElement frameElement;
	protected List<WebElement> options;

	protected static final String ATTR_ARIA_SELECTED = "aria-selected";

	public CommonSelectList(WebElement parentElement, FrameElement frameElement) {
		this.parentElement = parentElement;
		this.frameElement = frameElement;
	}
	
    public WebElement getParentElement() {
    	return parentElement;
    }

	public boolean isMultipleWithoutFind() {
		String value = getParentElement().getAttribute("multiple");
        return value != null && !"false".equals(value);
	}
}
