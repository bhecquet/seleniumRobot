/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.it.driver;

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
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.uipage.htmlelements.RadioButtonElement;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

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
	public static final LinkElement link2 = new LinkElement("My link", By.id("link2"));
	public static final LinkElement linkPopup = new LinkElement("My link", By.id("linkPopup"));
	public static final LinkElement linkPopup2 = new LinkElement("My link", By.id("linkPopup2"));
	public static final TextFieldElement onBlurField = new TextFieldElement("On Blur", By.id("textOnBlur"));
	public static final TextFieldElement onBlurFieldDest = new TextFieldElement("On Blur", By.id("textOnBlurDest"));
	public static final CheckBoxElement hiddenCheckBox = new CheckBoxElement("check", By.id("hiddenCheckbox"));
	public static final SelectList selectList = new SelectList("list", By.id("select"));
	public static final SelectList selectMultipleList = new SelectList("listMultiple", By.id("selectMultiple"));
	public static final SelectList selectUlLiList = new SelectList("ulLiList", By.id("languages"));
	public static final TextFieldElement ulliListTrigger = new TextFieldElement("listTrigger", By.id("language"));
	public static final Table table = new Table("table", By.id("table"));
	public static final Table emptyTable = new Table("table", By.id("emptyTable"));
	public static final PictureElement picture = new PictureElement("picture", "tu/images/logo_text_field.png", table);
	public static final PictureElement pictureNotPresent = new PictureElement("picture", "tu/images/vosAlertes.png", table);
	public static final TextFieldElement logoText = new TextFieldElement("logoText", By.id("logoText"));
	
	// Elements inside others
	public static final TextFieldElement textElement2 = new HtmlElement("", By.tagName("body")).findTextFieldElement(By.id("text2"));
	public static final RadioButtonElement radioElement2 = new HtmlElement("", By.tagName("body")).findRadioButtonElement(By.id("radioClick"));
	public static final CheckBoxElement checkElement2 = new HtmlElement("", By.tagName("body")).findCheckBoxElement(By.id("checkboxClick"));
	public static final ButtonElement startButton2 = new HtmlElement("", By.tagName("body")).findButtonElement(By.id("button"));
	public static final LinkElement linkElement2 = new HtmlElement("", By.tagName("body")).findLinkElement(By.id("link"));
	public static final SelectList selectList2 = new HtmlElement("", By.tagName("body")).findSelectList(By.id("select"));
	public static final Table table2 = new HtmlElement("", By.tagName("body")).findTable(By.id("table"));
	
	// Elements inside others with findElements()
	public static final TextFieldElement textElement3 = new HtmlElement("", By.tagName("body")).findTextFieldElement(By.id("text2"), 0);
	public static final RadioButtonElement radioElement3 = new HtmlElement("", By.tagName("body")).findRadioButtonElement(By.id("radioClick"), 0);
	public static final CheckBoxElement checkElement3 = new HtmlElement("", By.tagName("body")).findCheckBoxElement(By.id("checkboxClick"), 0);
	public static final ButtonElement startButton3 = new HtmlElement("", By.tagName("body")).findButtonElement(By.id("button"), 0);
	public static final LinkElement linkElement3 = new HtmlElement("", By.tagName("body")).findLinkElement(By.id("link"), 0);
	public static final SelectList selectList3 = new HtmlElement("", By.tagName("body")).findSelectList(By.id("select"), 0);
	public static final Table table3 = new HtmlElement("", By.tagName("body")).findTable(By.id("table"), 0);
	
	// Elements for IFrame
	public static final FrameElement iframe = new FrameElement("IFrame", By.id("myIFrame"));
	public static final FrameElement subIframe = new FrameElement("IFrame", By.name("mySecondIFrame"), iframe);
	public static final TextFieldElement textElementIFrame = new TextFieldElement("Text", By.id("textInIFrameWithValue"), iframe);
	public static final RadioButtonElement radioElementIFrame = new RadioButtonElement("Radio", By.id("radioClickIFrame"), iframe);
	public static final CheckBoxElement checkElementIFrame = new CheckBoxElement("Check", By.id("checkboxClickIFrame"), iframe);
	public static final ButtonElement buttonIFrame = new ButtonElement("Button", By.id("buttonIFrame"), iframe);
	public static final LinkElement linkIFrame = new LinkElement("My link", By.id("linkIFrame"), iframe);
	public static final SelectList selectListIFrame = new SelectList("list", By.id("selectIFrame"), iframe);
	public static final Table tableIFrame = new Table("table", By.id("tableIframe"), iframe);
	public static final LabelElement labelIFrame = new LabelElement("label", By.id("labelIFrame"), iframe);
	
	public static final TextFieldElement textElementSubIFrame = new TextFieldElement("Text", By.id("textInIFrameWithValue2"), subIframe);
	
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
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }
}
