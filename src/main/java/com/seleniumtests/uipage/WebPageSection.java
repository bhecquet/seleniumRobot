/*
 * Copyright 2015 www.seleniumtests.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.uipage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.reporter.TestLogging;

public abstract class WebPageSection extends BasePage {

    private String name = null;
    private String locator = null;
    protected WebElement element = null;
    private By by = null;

    public WebPageSection(final String name) {
        super();
        this.name = name;
    }

    public WebPageSection(final String name, final By by) {
        super();
        this.name = name;
        this.by = by;
    }

    /**
     * Captures page snapshot.
     */
    @Override
    public void capturePageSnapshot() {

        ScreenShot screenShot = new ScreenshotUtil(driver).captureWebPageSnapshot();
        String title = screenShot.getTitle();

        TestLogging.logWebOutput(title + " (" + TestLogging.buildScreenshotLog(screenShot) + ")", false);
    }

    public String getLocator() {
        return locator;
    }

    public String getName() {
        return name;
    }

    public By getBy() {
        return by;
    }

    public boolean isPageSectionPresent() {
        return isElementPresent(by);
    }
}
