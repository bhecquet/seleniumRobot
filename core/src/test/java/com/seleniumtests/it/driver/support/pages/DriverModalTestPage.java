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
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.ButtonElement;

public class DriverModalTestPage extends PageObject {
	

	public static final ButtonElement openModal = new ButtonElement("open modal", By.id("openModalButton"));
	
	
	private String openedPageUrl;
	
	public DriverModalTestPage() {
        super(openModal);
    }
    
    public DriverModalTestPage(boolean openPageURL) {
    	this(openPageURL, getPageUrl(SeleniumTestsContextManager.getThreadContext().getBrowser()));
    }
    
    public DriverModalTestPage(boolean openPageURL, BrowserType browserType) {
    	super(openModal, getPageUrl(browserType), browserType, "second", null);
    }
    
    public DriverModalTestPage(boolean openPageURL, String url)  {
    	super(openModal, openPageURL ? url : null);
    	openedPageUrl = url;
    }
   
    
    public DriverModalTestPage _openModal() {
    	openModal.click();
    	return this;
    }
  
    public static String getPageUrl(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testModal.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testModal.html").getFile();
		}
    }

	public String getOpenedPageUrl() {
		return openedPageUrl;
	}
	

    public DriverModalTestPage _captureSnapshot(String snapshotName) {
    	capturePageSnapshot(snapshotName, SnapshotCheckType.FULL);
    	return this;
    	
    }
}
