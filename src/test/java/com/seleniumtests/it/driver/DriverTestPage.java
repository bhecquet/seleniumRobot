/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public static final HtmlElement parent  = new HtmlElement("parent", By.id("parent"));
	public static final HtmlElement child  = new HtmlElement("parent", By.id("parent")).findElement(By.className("myClass"), 1);
	public static final HtmlElement divFindName  = new HtmlElement("parent", By.name("divFindName"), 1);
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
    
    //for TestInterceptPage (the loader page of By has to be a PageObject)
    public By findById(String id) {
    	return By.id(id);
    }
    
    private static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser().contains("firefox")) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }
}
