package com.seleniumtests.it.driver.support.pages;

import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.SelectList;

public class DriverTestAngularFrame extends PageObject {


	// Elements for Angular IFrame
	public static final FrameElement angularIFrame = new FrameElement("IFrame angular", By.id("angularIFrame"));
	public static final SelectList angularSelectListIFrame = new SelectList("list", By.id("angularSelect"), angularIFrame);
	

	private String openedPageUrl;
	
	public DriverTestAngularFrame() throws Exception {
        super(angularIFrame);
    }
    
    public DriverTestAngularFrame(boolean openPageURL) throws Exception {
        super(angularIFrame, openPageURL ? getPageUrl() : null);
    }
    
    public DriverTestAngularFrame(boolean openPageURL, String url) throws Exception {
    	super(angularIFrame, openPageURL ? url : null);
    	openedPageUrl = url;
    }
    
    private static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testAngularIFrame.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testAngularIFrame.html").getFile();
		}
    }
}
