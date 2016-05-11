package com.seleniumtests.it.driver;

import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.webelements.ButtonElement;
import com.seleniumtests.webelements.CheckBoxElement;
import com.seleniumtests.webelements.HtmlElement;
import com.seleniumtests.webelements.LinkElement;
import com.seleniumtests.webelements.PageObject;
import com.seleniumtests.webelements.RadioButtonElement;
import com.seleniumtests.webelements.TextFieldElement;

public class DriverTestPage extends PageObject {
	
	public static final TextFieldElement textElement = new TextFieldElement("Text", By.id("text2"));
	public static final RadioButtonElement radioElement = new RadioButtonElement("Radio", By.id("radioClick"));
	public static final CheckBoxElement checkElement = new CheckBoxElement("Check", By.id("checkboxClick"));
	public static final ButtonElement startButton = new ButtonElement("Start Animation", By.id("button"));
	public static final ButtonElement resetButton = new ButtonElement("Reset", By.id("button2"));
	public static final ButtonElement delayButton = new ButtonElement("Reset", By.id("buttonDelay"));
	public static final ButtonElement delayButtonReset = new ButtonElement("Reset", By.id("buttonDelayReset"));
	public static final HtmlElement greenSquare = new HtmlElement("Green square", By.id("carre"));
	public static final HtmlElement redSquare = new HtmlElement("Red Square", By.id("carre2"));
	public static final LinkElement link = new LinkElement("My link", By.id("link"));
	public static final LinkElement linkPopup = new LinkElement("My link", By.id("linkPopup"));
	public static final LinkElement linkPopup2 = new LinkElement("My link", By.id("linkPopup2"));
	public static final TextFieldElement onBlurField = new TextFieldElement("On Blur", By.id("textOnBlur"));
	public static final TextFieldElement onBlurFieldDest = new TextFieldElement("On Blur", By.id("textOnBlurDest"));
	public static final CheckBoxElement hiddenCheckBox = new CheckBoxElement("check", By.id("hiddenCheckbox"));

	public DriverTestPage() throws Exception {
        super(textElement);
    }
    
    public DriverTestPage(boolean openPageURL) throws Exception {
        super(textElement, openPageURL ? getPageUrl() : null);
    }
    
    private static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getWebRunBrowser().contains("firefox")) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }
}
