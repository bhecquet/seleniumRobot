package com.seleniumtests.it.driver.support.pages;

import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import org.openqa.selenium.By;

import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.SelectList;

public class DriverTestPageSalesforceLightning extends PageObject {
	

	private static final ButtonElement acceptCookies = new ButtonElement("Accept and continue", By.id("onetrust-accept-btn-handler"));
	public static final HtmlElement parent = new HtmlElement("", ByC.shadow(By.tagName("doc-component-playground")));
	public static final FrameElement frame = new FrameElement("", By.tagName("iframe"), parent);
	public static final SelectList combobox = new SelectList("select", By.xpath("//lightning-base-combobox"), frame);
	 
    public DriverTestPageSalesforceLightning() throws Exception {
    	super(null, "https://developer.salesforce.com/docs/platform/lightning-component-reference/guide/lightning-combobox.html?type=Example");
    	
    	if (acceptCookies.isElementPresent(4)) {
    		acceptCookies.click();
    	}
    }
}
