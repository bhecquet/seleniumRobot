package com.seleniumtests.it.driver.support.pages;

import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;

import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class DriverTestPageShadowDom extends PageObject {
	
	
	public static final TextFieldElement inputFail1 = new TextFieldElement("", By.id("fail1"));
	public static final HtmlElement divPass1Direct = new HtmlElement("", By.id("pass1Shadow"));
	public static final HtmlElement divPass1Shadow = new HtmlElement("", ByC.shadow(By.id("shadow2"))).findElement(By.id("pass1Shadow"));
	public static final HtmlElement divPass3MultipleShadow = new HtmlElement("", ByC.shadow(By.id("shadow5"), By.id("shadow6"))).findElement(By.id("pass3Shadow"));
	public static final HtmlElement divMultipleShadowElements = new HtmlElement("", ByC.shadow(By.name("doubleShadow"), By.name("doubleSubShadow")));
	public static final HtmlElement shadowElementNotFound = new HtmlElement("", ByC.shadow(By.id("shadow5"), By.id("shadowNotPresent"))).findElement(By.id("pass3Shadow"));

	public static final FrameElement frameWithShadow = new FrameElement("", By.id("frame9"));
	public static final HtmlElement divPassInFrameAndShadow = new HtmlElement("", ByC.shadow(By.id("shadow2Frame")), frameWithShadow)
															.findElement(By.id("pass1InFrameShadow"));
	
	public static final HtmlElement shadowWithFrame = new HtmlElement("", ByC.shadow(By.id("shadow12")));
	public static final FrameElement frameInShadow = new FrameElement("", By.id("frame11"), shadowWithFrame);
	public static final HtmlElement divPassInShadowAndFrame = new HtmlElement("", By.id("pass1"), frameInShadow);
	public static final HtmlElement shadow4Parent = new HtmlElement("", By.id("shadow4Parent"));
	public static final HtmlElement labelInShadowByTagName = new HtmlElement("", ByC.shadow(By.tagName("span")), shadow4Parent).findElement(By.tagName("label"));
	public static final HtmlElement divPass3MultipleShadowTagName = new HtmlElement("", ByC.shadow(By.id("shadow5"), By.tagName("div"))).findElement(By.id("pass3Shadow"));
	
	
	public static final TextFieldElement textInScroll = new HtmlElement("", ByC.shadow(By.id("shadow11"))).findTextFieldElement(By.id("textInScroll"));
	 
    public DriverTestPageShadowDom(boolean openPageURL) throws Exception {
    	this(openPageURL, getPageUrl(SeleniumTestsContextManager.getThreadContext().getBrowser()));
    }

    public DriverTestPageShadowDom(boolean openPageURL, String url) throws Exception {
    	super(inputFail1, openPageURL ? url : null);
    }
	

    public static String getPageUrl(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testShadow.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testShadow.html").getFile();
		}
    }

}
