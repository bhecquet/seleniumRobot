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
package com.seleniumtests.uipage.htmlelements;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.ReplayOnError;

public class ButtonElement extends HtmlElement {

    public ButtonElement(final String label, final By by) {
        super(label, by);
    }
    
    public ButtonElement(final String label, final By by, final int index) {
    	super(label, by, index);
    }
    
    public ButtonElement(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }
    
    public ButtonElement(final String label, final By by, final FrameElement frame, final int index) {
    	super(label, by, frame, index);
    }

    @Override
    public void click() {

        BrowserType browser = WebUIDriver.getWebUIDriver().getConfig().getBrowser();
        if (browser == BrowserType.INTERNETEXPLORER) {
            super.sendKeys(Keys.ENTER);
        } else {
            super.click();
        }
    }

    @ReplayOnError
    public void submit() {
        findElement(true);
        element.submit();
    }
}
