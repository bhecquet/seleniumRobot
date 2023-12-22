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

import java.nio.file.Paths;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.uipage.htmlelements.ScreenZone;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;
import com.seleniumtests.uipage.uielements.ByUI;
import com.seleniumtests.uipage.uielements.TextFieldUiElement;

/**
 * Same as DriverTestPage, but colored rows are not displayed in this page
 * @author s047432
 *
 */
public class DriverTestPageWithoutFixedPattern extends PageObject {
	
	public static final TextFieldElement textElement = new TextFieldElement("Text", By.id("text2"));
	public static final Table table = new Table("table", By.id("table"));
	public static final PictureElement picture = new PictureElement("picture", "tu/images/logo_text_field.png", table);
	public static final PictureElement pictureNotPresent = new PictureElement("picture", "tu/images/vosAlertes.png", table);
	public static final PictureElement googlePicture = new PictureElement("picture", "tu/googleSearch.png", null);
	private static final PictureElement googlePrivatePicture = new PictureElement("picture", "tu/googleSearch.png", null);
	public static final PictureElement googlePictureWithFile = new PictureElement("picture", Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile(), null);
	public static final ScreenZone googleForDesktop = new ScreenZone("picture", "tu/googleSearch.png");
	public static final ScreenZone googleForDesktopWithFile = new ScreenZone("picture", Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile());
	public static final ScreenZone firefoxForDesktop = new ScreenZone("picture", "tu/images/logo_text_field.png");
	public static final ScreenZone zoneNotPresent = new ScreenZone("picture", "tu/images/vosAlertes.png");
	public static final LinkElement newpage = new LinkElement("New Page", By.id("newpage"));
	public static final TextFieldElement logoText = new TextFieldElement("logoText", By.id("logoText"));
	
	public static final FrameElement iframe = new FrameElement("IFrame", By.id("myIFrame"));
	public static final TextFieldElement textElementIFrame = new TextFieldElement("Text", By.id("textInIFrameWithValue"), iframe);
	
	public static final TextFieldUiElement uiTextElement = new TextFieldUiElement(ByUI.toRightOf("Mon label.*"));
	public static final TextFieldUiElement uiTextElementBelow = new TextFieldUiElement(ByUI.below(".* onblur"));
	
	private String openedPageUrl;
	
	public DriverTestPageWithoutFixedPattern()  {
        super(textElement);
		clearPictureMemories();
	}

	private static void clearPictureMemories() {
		googleForDesktop.clearMemory();
		googleForDesktopWithFile.clearMemory();
		firefoxForDesktop.clearMemory();
		googlePicture.clearMemory();
		googlePrivatePicture.clearMemory();
		googlePictureWithFile.clearMemory();
	}

    public DriverTestPageWithoutFixedPattern(boolean openPageURL, String url) {
    	super(textElement, openPageURL ? url : null);
    	openedPageUrl = url;
		clearPictureMemories();
    }
    
    public void clickGooglePicture() {
    	googlePicture.click();
    }
    
    public void clickGooglePrivatePicture() {
    	googlePrivatePicture.click();
    }
    
    //for TestInterceptPage (the loader page of By has to be a PageObject)
    public By findById(String id) {
    	return By.id(id); 
    }
    
    public void move() {
    	CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)(getDriver());
    	HtmlElement html = new HtmlElement("", By.tagName("html"));
    	driver.manage().window().getPosition();
    	driver.manage().window().getSize();
    	driver.getViewPortDimensionWithoutScrollbar();
    	new Actions(driver).moveToElement(html).moveByOffset(10, 100).click().perform();
    	driver.getCurrentUrl();
    }
    

    public DriverTestPageWithoutFixedPattern _goToNewPage() {
    	newpage.click();
    	return this;
    }
    
    public static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testWithoutFixedPattern.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testWithoutFixedPattern.html").getFile();
		}
    }
    
	public String getOpenedPageUrl() {
		return openedPageUrl;
	}
}
