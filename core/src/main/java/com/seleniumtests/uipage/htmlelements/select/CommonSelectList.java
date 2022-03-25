package com.seleniumtests.uipage.htmlelements.select;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class CommonSelectList implements ISelectList {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(CommonSelectList.class);
	protected WebElement parentElement;
	protected FrameElement frameElement;
	protected WebDriver driver;
	protected List<WebElement> options;

	protected static final String ATTR_ARIA_SELECTED = "aria-selected";

	protected CommonSelectList(WebElement parentElement, FrameElement frameElement) {
		this.parentElement = parentElement;
		this.frameElement = frameElement;
	}
	
    public WebElement getParentElement() {
    	return parentElement;
    }

	public boolean isMultipleWithoutFind() {
		String value = getParentElement().getDomAttribute("multiple");
        return value != null && !"false".equals(value);
	}

	public WebDriver getDriver() {
		return driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}
	
	/**
	 * If an alert is present, dismiss it
	 */
	protected void handleAlert() {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(0)).until(ExpectedConditions.alertIsPresent());
			driver.switchTo().alert().dismiss();
		} catch (TimeoutException e) {
			// no problem if no alert is there
		}
	}
}
