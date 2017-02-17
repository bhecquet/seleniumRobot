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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.UselessFileDetector;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * Supports file upload in remote webdriver.
 */
public class CustomEventFiringWebDriver extends EventFiringWebDriver {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(CustomEventFiringWebDriver.class);
    private FileDetector fileDetector = new UselessFileDetector();
    private WebDriver driver = null;
    private Set<String> currentHandles;
    
    private static final String JS_GET_VIEWPORT_SIZE =
            "var height = 100000;"
                + "var width = 100000;"
                + " if (window.innerHeight) {"
                + "		height = Math.min(window.innerHeight, height);"
                + " }"
                + " if (document.documentElement && document.documentElement.clientHeight) {"
                + "		height = Math.min(document.documentElement.clientHeight, height);"
                + " }"
                + "	var b = document.getElementsByTagName('html')[0]; "
                + "	if (b.clientHeight) {"
                + "		height = Math.min(b.clientHeight, height);"
                + "	}"
                + " if (window.innerWidth) {"
                + "		width = Math.min(window.innerWidth, width);"
                + " } "
                + " if (document.documentElement && document.documentElement.clientWidth) {"
                + "		width = Math.min(document.documentElement.clientWidth, width);"
                + " } "
                + "	var b = document.getElementsByTagName('html')[0]; "
                + "	if (b.clientWidth) {"
                + "		width = Math.min(b.clientWidth, width);"
                + "	}"
                + "	return [width, height];";

    private static final String JS_GET_CURRENT_SCROLL_POSITION =
            "var doc = document.documentElement; "
            + "var x = window.scrollX || ((window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0));"
            + "var y = window.scrollY || ((window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0));"
            + "return [x, y];";


    // IMPORTANT: Notice there's a major difference between scrollWidth
    // and scrollHeight. While scrollWidth is the maximum between an
    // element's width and its content width, scrollHeight might be
    // smaller (!) than the clientHeight, which is why we take the
    // maximum between them.
    private static final String JS_GET_CONTENT_ENTIRE_SIZE =
            "var scrollWidth = document.documentElement.scrollWidth; " +
            "var bodyScrollWidth = document.body.scrollWidth; " +
            "var totalWidth = Math.max(scrollWidth, bodyScrollWidth); " +
            "var clientHeight = document.documentElement.clientHeight; " +
            "var bodyClientHeight = document.body.clientHeight; " +
            "var scrollHeight = document.documentElement.scrollHeight; " +
            "var bodyScrollHeight = document.body.scrollHeight; " +
            "var maxDocElementHeight = Math.max(clientHeight, scrollHeight); " +
            "var maxBodyHeight = Math.max(bodyClientHeight, bodyScrollHeight); " +
            "var totalHeight = Math.max(maxDocElementHeight, maxBodyHeight); " +
            "return [totalWidth, totalHeight];";

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
    
    /**
     * Handle WebDriver exception when this method is not implemented
     */
    @Override
    public String getPageSource() {
    	try {
    		return super.getPageSource();
    	} catch (WebDriverException e) {
    		logger.info("page source not get: " + e.getMessage());
    		return null;
    	}
    }
    
    public Set<String> getCurrentHandles() {
		return currentHandles;
	}
    
    /**
     * get dimensions of the visible part of the page
     * TODO: handle mobile app case
     * @return
     */
    @SuppressWarnings("unchecked")
	public Dimension getViewPortDimensionWithoutScrollbar() {
    	if (SeleniumTestsContextManager.isWebTest()) {
    		List<Long> dims = (List<Long>)((JavascriptExecutor)driver).executeScript(JS_GET_VIEWPORT_SIZE);
    		return new Dimension(dims.get(0).intValue(), dims.get(1).intValue());
    	} else {
    		return driver.manage().window().getSize();
    	}
    }
    
    /**
     * Get the whole webpage dimension
     * TODO: handle mobile app case
     * @return
     */

	@SuppressWarnings("unchecked")
    public Dimension getContentDimension() {
    	if (SeleniumTestsContextManager.isWebTest()) {
			List<Long> dims = (List<Long>)((JavascriptExecutor)driver).executeScript(JS_GET_CONTENT_ENTIRE_SIZE);
	    	return new Dimension(dims.get(0).intValue(), dims.get(1).intValue());
    	} else {
    		return driver.manage().window().getSize();
    	}
    }
	
	/**
	 * TODO: handle mobile app case
	 */
	public void scrollTop() {
		if (SeleniumTestsContextManager.isWebTest()) {
			((JavascriptExecutor) driver).executeScript("window.top.scroll(0, 0)");
		} 
	}
	
}
