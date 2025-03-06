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

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.Step;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.*;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class DriverPDFPage extends PageObject {

	public static final LinkElement link = new LinkElement("My link", By.linkText("Click to open in a new tab"));
	public static final LinkElement linkDownload = new LinkElement("My link", By.linkText("Click to download file"));


	public DriverPDFPage(String url) {
        super(link, url);
	}

	public DriverPDFPage(BrowserType browserType) {
		super(link, getPageUrl(browserType), browserType, "main", null);
	}

	public DriverPDFPage clickPDF() {
		link.click();
		return this;
	}

	public DriverPDFPage clickPDFToDownload() {
		linkDownload.click();
		return this;
	}

	public static String getPageUrl(BrowserType browserType) {
		if (browserType == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/testpdf.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/testpdf.html").getFile();
		}
	}
}
