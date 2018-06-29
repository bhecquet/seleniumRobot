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

public class DriverSubAngularTestPage extends PageObject {

	public static final SelectList selectList = new SelectList("list", By.id("angularSelect"));
	public static final SelectList selectMultipleList = new SelectList("list", By.id("angularMultipleSelect"));
	public static final CheckBoxElement checkbox = new CheckBoxElement("checkbox", By.id("angularCheckbox"));
	public static final CheckBoxElement checkboxInput = new CheckBoxElement("checkbox", By.id("angularCheckbox-input"));
	public static final RadioButtonElement radio = new RadioButtonElement("radio", By.id("angularRadio1"));

	private String openedPageUrl;
	
	public DriverSubAngularTestPage() throws Exception {
        super(selectList);
    }
    
    public DriverSubAngularTestPage(boolean openPageURL) throws Exception {
        super(selectList, openPageURL ? getPageUrl() : null);
    }
    
    public DriverSubAngularTestPage(boolean openPageURL, String url) throws Exception {
    	super(selectList, openPageURL ? url : null);
    	openedPageUrl = url;
    }
    
    private static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/angularApp/index.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/angularApp/index.html").getFile();
		}
    }
}
