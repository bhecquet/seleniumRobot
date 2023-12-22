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
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class DriverScrollingTestPage extends PageObject {
	
	public static final TextFieldElement textElement = new TextFieldElement("Text", By.id("text2"));
	public static final ButtonElement resetButton = new ButtonElement("Reset", By.id("button2"));
	public static final ButtonElement hideButton = new ButtonElement("Hide header / footer", By.id("hide"));
	public static final ButtonElement setButton = new ButtonElement("set", By.id("buttonSet"));
	public static final ButtonElement buttonScrollTop = new ButtonElement("set top", By.name("buttonScrollTop"));
	public static final ButtonElement buttonScrollBottom = new ButtonElement("set bottom", By.name("buttonScrollBottom"));
	public static final ButtonElement dropdownMenu = new ButtonElement("dropdown menu", ByC.text("Dropdown", "button"));
	public static final ButtonElement openModal = new ButtonElement("open modal", By.id("openModalButton"));
	public static final LinkElement menuLink2 = new LinkElement("link 2", By.linkText("Link 2"));
	public static final HtmlElement greenBox = new HtmlElement("button to scroll into view", By.id("greenBox"));
	public static final HtmlElement blueBox = new HtmlElement("button to scroll into view", By.id("blueBox"));
	
	
	private String openedPageUrl;

    public DriverScrollingTestPage(boolean openPageURL, String url) throws Exception {
    	super(textElement, openPageURL ? url : null);
    	openedPageUrl = url;
    }
    
    public DriverScrollingTestPage _writeSomething() {
    	textElement.sendKeys("a text");
    	return this;
    }
    
    public DriverScrollingTestPage _openModal() {
    	openModal.click();
    	return this;
    }
   
    public DriverScrollingTestPage _reset() {
    	resetButton.click();
    	return this;
    }
    
    public static String getPageUrl(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testScrolling.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testScrolling.html").getFile();
		}
    }

	public String getOpenedPageUrl() {
		return openedPageUrl;
	}
	

    public DriverScrollingTestPage _captureSnapshot(String snapshotName) {
    	capturePageSnapshot(snapshotName, SnapshotCheckType.FULL);
    	return this;
    	
    }
}
