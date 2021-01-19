package com.seleniumtests.it.driver.support.pages;

import org.openqa.selenium.By;

import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.SelectList;

public class DriverTestPageSalesforceLightning extends PageObject {
	

	private static final ButtonElement acceptCookies = new ButtonElement("Accept and continue", By.id("onetrust-accept-btn-handler"));
	public static final SelectList combobox = new SelectList("select", ByC.xTagName("lightning-base-combobox"));
	 
    public DriverTestPageSalesforceLightning() throws Exception {
    	super(null, "https://developer.salesforce.com/docs/component-library/bundle/lightning:combobox/example");
    	
    	if (acceptCookies.isElementPresent(4)) {
    		acceptCookies.click();
    	}
    }
}
