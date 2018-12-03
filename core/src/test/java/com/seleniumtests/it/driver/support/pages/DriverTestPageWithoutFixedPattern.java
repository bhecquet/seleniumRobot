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

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.uipage.htmlelements.ScreenZone;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

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
	public static final PictureElement googlePictureWithFile = new PictureElement("picture", Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile(), null);
	public static final ScreenZone googleForDesktop = new ScreenZone("picture", "tu/googleSearch.png");
	public static final ScreenZone googleForDesktopWithFile = new ScreenZone("picture", Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile());
	public static final ScreenZone firefoxForDesktop = new ScreenZone("picture", "tu/images/logo_text_field.png");
	public static final ScreenZone zoneNotPresent = new ScreenZone("picture", "tu/images/vosAlertes.png");
	public static final TextFieldElement logoText = new TextFieldElement("logoText", By.id("logoText"));
	
	private String openedPageUrl;
	
	public DriverTestPageWithoutFixedPattern() throws Exception {
        super(textElement);
    }
    
    public DriverTestPageWithoutFixedPattern(boolean openPageURL) throws Exception {
        super(textElement, openPageURL ? getPageUrl() : null);
    }

    public DriverTestPageWithoutFixedPattern(boolean openPageURL, String url) throws Exception {
    	super(textElement, openPageURL ? url : null);
    	openedPageUrl = url;
    }
    
    //for TestInterceptPage (the loader page of By has to be a PageObject)
    public By findById(String id) {
    	return By.id(id); 
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
