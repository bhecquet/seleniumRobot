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

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;
import org.openqa.selenium.By;

public class DriverMultipleFrameDefinitionsPage extends PageObject {

	public static final FrameElement frame = new FrameElement("frame", By.id("myIFrame"));
	public static final FrameElement frame2 = new FrameElement("frame", By.id("myIFrame2"));
	public static final TextFieldElement input = new TextFieldElement("input", By.id("textInIFrameWithValue"), frame);

	public DriverMultipleFrameDefinitionsPage(BrowserType browserType) {
		super(frame, getPageUrl(browserType), browserType, "main", null);
	}

	public String _getText() {
		return input.getValue();
	}

	public void setFrame() {
		input.setFrameElement(frame);
	}
	public void setFrame2() {
		input.setFrameElement(frame2);
	}

	public static String getPageUrl(BrowserType browserType) {
		if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testFrameDefinitionUpdate.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testFrameDefinitionUpdate.html").getFile();
		}
	}
}
