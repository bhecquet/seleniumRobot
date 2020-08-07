/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.driver.support.pages;

import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.CheckBoxElement;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LabelElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.uipage.htmlelements.RadioButtonElement;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class DriverSubTestPage extends PageObject {

	public static final FrameElement subIframe = new FrameElement("IFrame", By.name("mySecondIFrame"));
	public static final TextFieldElement textElement = new TextFieldElement("Text", By.id("textInIFrameWithValue"));
	public static final TextFieldElement textElementSubPage = new TextFieldElement("Text", By.id("textInIFrameWithValue"));
	public static final RadioButtonElement radioElement = new RadioButtonElement("Radio", By.id("radioClickIFrame"));
	public static final CheckBoxElement checkElement = new CheckBoxElement("Check", By.id("checkboxClickIFrame"));
	public static final ButtonElement button = new ButtonElement("Button", By.id("buttonIFrame"));
	public static final LinkElement link = new LinkElement("My link", By.id("linkIFrame"));
	public static final SelectList selectList = new SelectList("list", By.id("selectIFrame"));
	public static final HtmlElement optionOfSelectList = selectList.findElement(By.tagName("option"));
	public static final Table table = new Table("table", By.id("tableIframe"));
	public static final LabelElement label = new LabelElement("label", By.id("labelIFrame"));
	public static final ButtonElement closeButton = new ButtonElement("close Button", By.id("closeButton"));
	
	public static final TextFieldElement textElementSub = new TextFieldElement("Text", By.id("textInIFrameWithValue2"), subIframe);
	
	public DriverSubTestPage() throws Exception {
        super(textElement);
    }
    
    public DriverSubTestPage(boolean openPageURL) throws Exception {
        super(textElement, openPageURL ? getPageUrl() : null);
    }
    
    //for TestInterceptPage (the loader page of By has to be a PageObject)
    public By findById(String id) {
    	return By.id(id);
    }
    
    private static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testIFrame.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testIFrame.html").getFile();
		}
    }
}
