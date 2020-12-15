package com.seleniumtests.it.driver.support.pages;

import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class DriverTestPageShadowDom extends PageObject {
	

	private String openedPageUrl;
	
	public static final TextFieldElement inputFail1 = new TextFieldElement("", By.id("fail1"));
	public static final HtmlElement divPass1Direct = new HtmlElement("", By.id("pass1Shadow"));
	public static final HtmlElement divPass1Shadow = new HtmlElement("", ByC.shadow(By.id("shadow2"))).findElement(By.id("pass1Shadow"));
	public static final HtmlElement divPass3MultipleShadow = new HtmlElement("", ByC.shadow(By.id("shadow5"), By.id("shadow6"))).findElement(By.id("pass3Shadow"));
	public static final HtmlElement divMultipleShadowElements = new HtmlElement("", ByC.shadow(By.name("doubleShadow"), By.name("doubleSubShadow")));
	public static final HtmlElement shadowElementNotFound = new HtmlElement("", ByC.shadow(By.id("shadow5"), By.id("shadowNotPresent"))).findElement(By.id("pass3Shadow"));

	 
    public DriverTestPageShadowDom(boolean openPageURL) throws Exception {
    	this(openPageURL, getPageUrl(SeleniumTestsContextManager.getThreadContext().getBrowser()));
    }

    public DriverTestPageShadowDom(boolean openPageURL, String url) throws Exception {
    	super(inputFail1, openPageURL ? url : null);
    	openedPageUrl = url;
    }
	

    public static String getPageUrl(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testShadow.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testShadow.html").getFile();
		}
    }

}
