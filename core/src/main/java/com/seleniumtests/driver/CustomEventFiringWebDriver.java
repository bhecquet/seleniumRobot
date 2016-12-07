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
package com.seleniumtests.driver;

import java.util.Set;
import java.util.TreeSet;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.UselessFileDetector;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Supports file upload in remote webdriver.
 */
public class CustomEventFiringWebDriver extends EventFiringWebDriver {
    private FileDetector fileDetector = new UselessFileDetector();
    private WebDriver driver = null;
    private Set<String> currentHandles;

    public CustomEventFiringWebDriver(final WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public void setFileDetector(final FileDetector detector) {
        if (detector == null) {
            throw new WebDriverException("file detector is null");
        }

        fileDetector = detector;
    }
    
    public void updateWindowsHandles() {
    	if (SeleniumTestsContextManager.isWebTest()) {
    		currentHandles = driver.getWindowHandles();
    	} else {
    		currentHandles = new TreeSet<>();
    	}
    }

    public FileDetector getFileDetector() {
        return fileDetector;
    }

    public WebDriver getWebDriver() {
        return driver;
    }
    
    public Set<String> getCurrentHandles() {
		return currentHandles;
	}
}
