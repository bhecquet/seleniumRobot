/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.SelectList;

public class DriverTestAngularFrame extends PageObject {


	// Elements for Angular IFrame
	public static final FrameElement angularIFrame = new FrameElement("IFrame angular", By.id("angularIFrame"));
	public static final SelectList angularSelectListIFrame = new SelectList("list", By.id("angularSelect"), angularIFrame);
	
	
	public DriverTestAngularFrame() throws Exception {
        super(angularIFrame);
    }
    
    public DriverTestAngularFrame(boolean openPageURL) throws Exception {
        super(angularIFrame, openPageURL ? getPageUrl() : null);
    }
    
    public DriverTestAngularFrame(boolean openPageURL, String url) throws Exception {
    	super(angularIFrame, openPageURL ? url : null);
    }
    
    private static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testAngularIFrame.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testAngularIFrame.html").getFile();
		}
    }
}
