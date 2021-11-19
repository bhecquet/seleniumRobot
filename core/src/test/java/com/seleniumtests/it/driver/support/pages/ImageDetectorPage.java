package com.seleniumtests.it.driver.support.pages;

import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;

public class ImageDetectorPage extends PageObject {
	

	private String openedPageUrl;
	
	private static final ButtonElement errorButton = new ButtonElement("error button", By.id("errorButton"));
	
    public ImageDetectorPage() throws Exception {
    	this(getPageUrl(SeleniumTestsContextManager.getThreadContext().getBrowser()));
    }

    public ImageDetectorPage(String url) throws Exception {
    	super(errorButton, url);
    	openedPageUrl = url;
    }
	

    public static String getPageUrl(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("ti/testImageDetection.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("ti/testImageDetection.html").getFile();
		}
    }
    
    public ImageDetectorPage _clickErrorButton() {
    	errorButton.click();
    	return this;
    }

}
