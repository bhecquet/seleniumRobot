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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.CheckBoxElement;
import com.seleniumtests.uipage.htmlelements.FileUploadElement;
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
	public static final TextFieldElement textElementNotPresentFirstVisible = new TextFieldElement("Text", By.id("text_"), HtmlElement.FIRST_VISIBLE);
	public static final TextFieldElement textElementNotPresent = new TextFieldElement("Text", By.id("text___"));
	public static final HtmlElement elementNotPresentWithTimeout = new HtmlElement("element", By.id("notPresent"), (Integer)null, 5);
	public static final RadioButtonElement radioElement = new RadioButtonElement("Radio", By.id("radioClick"));
	public static final CheckBoxElement checkElement = new CheckBoxElement("Check", By.id("checkboxClick"));
	public static final ButtonElement startButton = new ButtonElement("Start Animation", By.id("button"));
	public static final ButtonElement resetButton = new ButtonElement("Reset", By.id("button2"));
	public static final ButtonElement delayButton = new ButtonElement("Reset", By.id("buttonDelay"));
	public static final ButtonElement delayButtonReset = new ButtonElement("Reset", By.id("buttonDelayReset"));
	public static final HtmlElement greenSquare = new HtmlElement("Green square", By.id("carre"));
	public static final HtmlElement redSquare = new HtmlElement("Red Square", By.id("carre2"));
	public static final HtmlElement bottomSquare = new HtmlElement("Bottom Square", By.id("carreBottom"));
	public static final HtmlElement parent  = new HtmlElement("parent", By.id("parent"));
	public static final HtmlElement child  = parent.findElement(By.className("myClass"), 1);
	public static final HtmlElement divFindName  = new HtmlElement("parent", By.name("divFindName"), 1);
	public static final HtmlElement divFindNameMultipleCriteria  = new HtmlElement("parent", By.name("divFindName").id("divFind2"));
	public static final LinkElement link = new LinkElement("My link", By.id("link"));
	public static final LinkElement link2 = new LinkElement("My link", By.id("link2"));
	public static final LinkElement linkPopup = new LinkElement("My link", By.id("linkPopup"));
	public static final LinkElement linkPopup2 = new LinkElement("My link", By.id("linkPopup2"));
	public static final LinkElement linkAngular = new LinkElement("Link angular", By.id("angularApp"));
	public static final TextFieldElement onBlurField = new TextFieldElement("On Blur", By.id("textOnBlur"));
	public static final TextFieldElement onBlurFieldDest = new TextFieldElement("On Blur", By.id("textOnBlurDest"));
	public static final CheckBoxElement hiddenCheckBox = new CheckBoxElement("check", By.id("hiddenCheckbox"));
	public static final CheckBoxElement hiddenCheckboxByOpacity = new CheckBoxElement("check", By.id("hiddenCheckboxByOpacity"));
	public static final CheckBoxElement hiddenCheckboxByDisplay = new CheckBoxElement("check", By.id("hiddenCheckboxByDisplay"));
	public static final CheckBoxElement hiddenCheckboxByVisibility = new CheckBoxElement("check", By.id("hiddenCheckboxByVisibility"));
	public static final SelectList selectList = new SelectList("list", By.id("select"));
	public static final SelectList selectMultipleList = new SelectList("listMultiple", By.id("selectMultiple"));
	public static final SelectList selectUlLiList = new SelectList("ulLiList", By.id("languages"));
	public static final TextFieldElement ulliListTrigger = new TextFieldElement("listTrigger", By.id("language"));
	public static final Table table = new Table("table", By.id("table"));
	public static final Table emptyTable = new Table("table", By.id("emptyTable"));
	public static final PictureElement picture = new PictureElement("picture", "tu/images/logo_text_field.png", table);
	public static final PictureElement pictureNotPresent = new PictureElement("picture", "tu/images/vosAlertes.png", table);
	public static final TextFieldElement logoText = new TextFieldElement("logoText", By.id("logoText"));
	public static final FileUploadElement upload = new FileUploadElement("upload", By.id("upload"));
	public static final TextFieldElement uploadedFile = new TextFieldElement("uploadedFile", By.id("uploadedFile"));
	public static final TextFieldElement textSelectedId = new TextFieldElement("", By.id("textSelectedId"));
	public static final TextFieldElement textSelectedText = new TextFieldElement("", By.id("textSelectedText"));
	public static final TextFieldElement multiElementFirstText = new TextFieldElement("", By.className("someClass"));
	public static final TextFieldElement multiElementFirstTextWithParent = new HtmlElement("", By.className("otherClass")).findTextFieldElement(By.className("someClass"));
	public static final TextFieldElement multiElementLastText = new TextFieldElement("", By.className("someClass"), -1);
	public static final TextFieldElement multiElementFirstVisibleText = new TextFieldElement("", By.className("someClass"), HtmlElement.FIRST_VISIBLE);
	public static final TextFieldElement multiElementFirstVisibleTextWithParent = new HtmlElement("", By.className("otherClass"), HtmlElement.FIRST_VISIBLE).findTextFieldElement(By.className("someClass"));
	public static final HtmlElement divByClass = new HtmlElement("", By.className("otherClass"), HtmlElement.FIRST_VISIBLE);
	public static final HtmlElement searchByXpath = new HtmlElement("", By.xpath("//option[@value=\"opt1\"]"));
	public static final HtmlElement scrollButton = new HtmlElement("button after scroll", By.id("buttonScroll"));
	public static final HtmlElement greenBox = new HtmlElement("button to scroll into view", By.id("greenBox"));
	public static final ButtonElement bigFooterButton = new ButtonElement("Display Big footer", By.id("displayBigFooter"));
	public static final ButtonElement bigHeaderButton = new ButtonElement("Display Big header", By.id("displayBigHeader"));
	public static final ButtonElement veryBigFooterButton = new ButtonElement("Display Very Big footer", By.id("displayVeryBigFooter"));
	public static final ButtonElement veryBigHeaderButton = new ButtonElement("Display Very Big header", By.id("displayVeryBigHeader"));
	
	// Elements inside others
	public static final HtmlElement parentDiv = new HtmlElement("", By.id("parentDiv"));
	public static final TextFieldElement textElement2 = new HtmlElement("", By.id("parentDiv")).findTextFieldElement(By.name("textField"));
	public static final RadioButtonElement radioElement2 = new HtmlElement("", By.id("parentDiv")).findRadioButtonElement(By.name("radioClick"));
	public static final CheckBoxElement checkElement2 = new HtmlElement("", By.id("parentDiv")).findCheckBoxElement(By.name("checkboxClick"));
	public static final ButtonElement resetButton2 = new HtmlElement("", By.id("parentDiv")).findButtonElement(By.name("resetButton"));
	public static final LinkElement linkElement2 = new HtmlElement("", By.id("parentDiv")).findLinkElement(By.name("googleLink"));
	public static final SelectList selectList2 = new HtmlElement("", By.id("parentDiv")).findSelectList(By.name("select"));
	public static final HtmlElement optionByXpath = new HtmlElement("", By.id("parentDiv")).findElement(By.xpath("//option[@value=\"opt1\"]"));
	public static final HtmlElement optionByRelativeXpath = new HtmlElement("", By.id("parentDiv")).findElement(By.xpath(".//option[@value=\"opt1\"]"));
	public static final Table table2 = new HtmlElement("", By.id("parentDiv")).findTable(By.tagName("table"));
	
	// bug check when we search element in select, and this select is in an other element
	public static final TextFieldElement textInselectUlLiList = new HtmlElement("", By.tagName("body")).findSelectList(By.id("languages")).findTextFieldElement(By.id("inputInSelect"));
	
	
	// Elements inside others with findElements()
	public static final TextFieldElement textElement3 = new HtmlElement("", By.id("parentDiv")).findTextFieldElement(By.name("textField"), 0);
	public static final RadioButtonElement radioElement3 = new HtmlElement("", By.id("parentDiv")).findRadioButtonElement(By.name("radioClick"), 0);
	public static final CheckBoxElement checkElement3 = new HtmlElement("", By.id("parentDiv")).findCheckBoxElement(By.name("checkboxClick"), 0);
	public static final ButtonElement resetButton3 = new HtmlElement("", By.id("parentDiv")).findButtonElement(By.name("resetButton"), 0);
	public static final ButtonElement resetButtonByTagName = new HtmlElement("", By.id("parentDiv")).findButtonElement(By.tagName("button"), 0);
	public static final LinkElement linkElement3 = new HtmlElement("", By.id("parentDiv")).findLinkElement(By.name("googleLink"), 0);
	public static final SelectList selectList3 = new HtmlElement("", By.id("parentDiv")).findSelectList(By.name("select"), 0);
	public static final Table table3 = new HtmlElement("", By.id("parentDiv")).findTable(By.tagName("table"), 0);
	
	// Element inside non existent frame
	public static final FrameElement iframeNotPresent = new FrameElement("IFrame", By.id("noIFrame"));
	public static final HtmlElement elementNotPresentInIFrame = new HtmlElement("", By.tagName("div"), iframeNotPresent);
	
	// Elements for IFrame
	public static final FrameElement iframe = new FrameElement("IFrame", By.id("myIFrame"));
	public static final FrameElement subIframe = new FrameElement("IFrame", By.name("mySecondIFrame"), iframe);
	public static final TextFieldElement textElementIFrame = new TextFieldElement("Text", By.id("textInIFrameWithValue"), iframe);
	public static final TextFieldElement notPresentTextElementIFrame = new TextFieldElement("Text", By.id("textNotInIFrameWithValue"), iframe);
	public static final RadioButtonElement radioElementIFrame = new RadioButtonElement("Radio", By.id("radioClickIFrame"), iframe);
	public static final CheckBoxElement checkElementIFrame = new CheckBoxElement("Check", By.id("checkboxClickIFrame"), iframe);
	public static final ButtonElement buttonIFrame = new ButtonElement("Button", By.id("buttonIFrame"), iframe);
	public static final LinkElement linkIFrame = new LinkElement("My link", By.id("linkIFrame"), iframe);
	public static final SelectList selectListIFrame = new SelectList("list", By.id("selectIFrame"), iframe);
	public static final HtmlElement optionOfSelectListIFrame = selectListIFrame.findElement(By.tagName("option"));
	public static final HtmlElement optionOfSelectListIFrameByText = selectListIFrame.findElement(ByC.text("option1 frame", "option"));
	public static final HtmlElement wrongElementOfSelectListIFrameByText = selectListIFrame.findElement(By.tagName("div"));
	public static final Table tableIFrame = new Table("table", By.id("tableIframe"), iframe);
	public static final HtmlElement rows = new HtmlElement("", By.tagName("tr"), iframe);
	public static final LabelElement labelIFrame = new LabelElement("label", By.id("labelIFrame"), iframe);
	
	// second IFrame on main page
	public static final FrameElement secondIframe = new FrameElement("IFrame", By.tagName("iframe"), 1);
	public static final TextFieldElement textElementSecondIFrame = new TextFieldElement("Text", By.id("textInIFrameWithValue2"), secondIframe);
	
	public static final TextFieldElement textElementSubIFrame = new TextFieldElement("Text", By.id("textInIFrameWithValue2"), subIframe);
	
	// Elements for Angular IFrame
	public static final FrameElement angularIFrame = new FrameElement("IFrame angular", By.id("angularIFrame"));
	public static final SelectList angularSelectListIFrame = new SelectList("list", By.id("angularSelect"), angularIFrame);
	
	private String openedPageUrl;
	
	public DriverTestPage() throws Exception {
        super(textElement);
    }
    
    public DriverTestPage(boolean openPageURL) throws Exception {
    	this(openPageURL, getPageUrl(SeleniumTestsContextManager.getThreadContext().getBrowser()));
    }
    
    public DriverTestPage(boolean openPageURL, BrowserType browserType) throws Exception {
    	super(textElement, getPageUrl(browserType), browserType, "second", null);
    }
    
    public DriverTestPage(BrowserType browserType, Integer attachExistingDriverPort) throws Exception {
    	super(textElement, getPageUrl(browserType), browserType, "second", attachExistingDriverPort);
    }
    
    public DriverTestPage(boolean openPageURL, String url) throws Exception {
    	super(textElement, openPageURL ? url : null);
    	openedPageUrl = url;
    }
    
    //for TestInterceptPage (the loader page of By has to be a PageObject)
    public By findById(String id) {
    	return By.id(id);
    }
    
    public DriverTestPage _writeSomething() {
    	textElement.sendKeys("a text");
    	return this;
    }
    
    public DriverTestPage _writeSomethingOnNonExistentElement() {
    	textElementNotPresent.sendKeys("a text");
    	return this;
    }
    
    /**
     * Test catching exception to show that report displays the error
     * @return
     */
    public DriverTestPage _writeSomethingOnNonExistentElementWithCatch() {
    	
    	try {
    		textElementNotPresent.sendKeys("a text");
    	} catch (Exception e) {}
    	textElement.sendKeys("a text");
    	return this;
    }
    
    public DriverTestPage _reset() {
    	resetButton.click();
    	return this;
    }
    
    public DriverTestPage _captureSnapshot(String snapshotName) {
    	capturePageSnapshot(snapshotName, SnapshotCheckType.FULL.exclude(textElement));
    	return this;
    	
    }
    
    public DriverTestPage _isElementNotPresentDisplayed() {
    	if (textElementNotPresent.isDisplayed()) {
    		logger.info("displayed");
    	}
    	return this;
    }
    
    public DriverTestPage _setPassword(String passWordShort) {
    	textElement.sendKeys(passWordShort);
    	return this;
    }
    
    public DriverTestPage _setPassword2(String pwd) {
    	textElement.sendKeys(pwd);
    	return this;
    }
    
    public DriverSubTestPage _goToNewPage() throws Exception {
    	link.click();
    	selectNewWindow();
    	return new DriverSubTestPage();
    }
    
    public DriverTestPage _setPasswords(String ... passwords) {
    	textElement.sendKeys(passwords[0]);
    	return this;
    }
    
    public DriverTestPage _setPasswords(List<String> passwords) {
    	textElement.sendKeys(passwords.get(0));
    	return this;
    }
    
    public DriverTestPage _sendKeysComposite() {
    	new Actions(driver).moveToElement(textElement).sendKeys("composite").build().perform();
    	new Actions(driver).moveToElement(resetButton).click().build().perform();
    	return this;
    }
    
    public DriverTestPage _clickPicture() {
		picture.clickAt(0, -30);
		return this;
    }
    
    public DriverTestPage _switchAngularWindow() {
    	linkAngular.click();
    	selectNewWindow();
    	return this;
    }
    
    public DriverTestPage _goToFrame() {
    	textElementIFrame.getText();
    	notPresentTextElementIFrame.getTagName();
    	return this;
    }
    
    public static String getPageUrl(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }

	public String getOpenedPageUrl() {
		return openedPageUrl;
	}
}
