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
package com.seleniumtests.driver;

import org.openqa.selenium.remote.Browser;

public enum BrowserType {
    FIREFOX("*firefox"),
    INTERNET_EXPLORER("*iexplore"),
    EDGE("*edge"),
    CHROME("*chrome"),
    HTMLUNIT("*htmlunit"),
    SAFARI("*safari"),
    BROWSER("*browser"), // default Android browser
    NONE("*none")
    ;

	private String bType;

    BrowserType(final String type) {
        this.bType = type;
    }
	
    public static BrowserType getBrowserType(final String browserType) {
        if (browserType.toLowerCase().contains("firefox")) {
            return BrowserType.FIREFOX;
        } else if (browserType.toLowerCase().contains("iexplore") || browserType.toLowerCase().contains("internet explorer")) {
            return BrowserType.INTERNET_EXPLORER;
        } else if (browserType.toLowerCase().contains("edge")) {
        	return BrowserType.EDGE;
        } else if (browserType.toLowerCase().contains("chrome")) {
            return BrowserType.CHROME;
        } else if (browserType.toLowerCase().contains("htmlunit")) {
            return BrowserType.HTMLUNIT;
        }  else if (browserType.toLowerCase().contains("safari")) {
            return BrowserType.SAFARI;
        } else if (browserType.toLowerCase().contains("none")) {
        	return BrowserType.NONE;
        } 
        throw new IllegalArgumentException(String.format("browser %s is unknown", browserType));
        
    }

    public String getBrowserType() {
        return this.bType;
    }
    
    public static String getSeleniumBrowserType(BrowserType browserType) {
    	if (browserType == BrowserType.FIREFOX) {
    		return Browser.FIREFOX.browserName();
    	} else if (browserType == BrowserType.CHROME) {
    		return Browser.CHROME.browserName();
    	} else if (browserType == BrowserType.EDGE) {
    		return Browser.EDGE.browserName();
    	} else if (browserType == BrowserType.SAFARI) {
    		return Browser.SAFARI.browserName();
    	} else if (browserType == BrowserType.INTERNET_EXPLORER) {
    		return Browser.IE.browserName();
    	} else if (browserType == BrowserType.HTMLUNIT) {
    		return Browser.HTMLUNIT.browserName();
    	} else {
    		return null;
    	}
    }
    
    public static BrowserType getBrowserTypeFromSeleniumBrowserType(String browserType) {
    	if (Browser.FIREFOX.browserName().equals(browserType)) {
    		return BrowserType.FIREFOX;
    	} else if (Browser.CHROME.browserName().equals(browserType)) {
    		return BrowserType.CHROME;
    	} else if (Browser.EDGE.browserName().equals(browserType)) {
    		return BrowserType.EDGE;
    	} else if (Browser.SAFARI.browserName().equals(browserType)) {
    		return BrowserType.SAFARI;
    	} else if (Browser.IE.browserName().equals(browserType)) {
    		return BrowserType.INTERNET_EXPLORER;
    	} else if (Browser.HTMLUNIT.browserName().equals(browserType)) {
    		return BrowserType.HTMLUNIT;
    	} else {
    		return BrowserType.NONE;
    	}
    }

}
