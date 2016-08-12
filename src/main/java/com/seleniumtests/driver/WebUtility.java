/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

package com.seleniumtests.driver;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.TestLogging;

public class WebUtility {
	
	protected static final Logger logger = TestLogging.getLogger(WebUtility.class);
	
    private WebDriver driver;

    public WebUtility(final WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Resize window to given dimensions.
     *
     * @param  width
     * @param  height
     */
    public void resizeWindow(final int width, final int height) {
        try {
            TestLogging.logWebStep("Resize browser window to width " + width + " height " + height, false);

            Dimension size = new Dimension(width, height);
            driver.manage().window().setPosition(new Point(0, 0));
            driver.manage().window().setSize(size);
        } catch (Exception ex) {
        	logger.error(ex);
        }
    }

    public void maximizeWindow() {
        try {
        	if (SeleniumTestsContextManager.getThreadContext().getTestType().isMobile()) {
                return;
            }

            driver.manage().window().maximize();
        } catch (Exception ex) {

            try {
                ((JavascriptExecutor) driver).executeScript(
                    "if (window.screen){window.moveTo(0, 0);window.resizeTo(window.screen.availWidth,window.screen.availHeight);}");
            } catch (Exception ignore) {
                TestLogging.log("Unable to maximize browser window. Exception occured: " + ignore.getMessage());
            }
        }
    }

}
