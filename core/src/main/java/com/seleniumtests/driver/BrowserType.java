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

public enum BrowserType {
    FIREFOX("*firefox"),
    INTERNET_EXPLORER("*iexplore"),
    EDGE("*edge"),
    CHROME("*chrome"),
    HTMLUNIT("*htmlunit"),
    OPERA("*opera"),
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
        } else if (browserType.toLowerCase().contains("opera")) {
        	return BrowserType.OPERA;
        } else if (browserType.toLowerCase().contains("htmlunit")) {
            return BrowserType.HTMLUNIT;
        }  else if (browserType.toLowerCase().contains("safari")) {
            return BrowserType.SAFARI;
        } else if (browserType.toLowerCase().contains("browser") || browserType.toLowerCase().contains("android")) {
        	return BrowserType.BROWSER;
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
    		return org.openqa.selenium.remote.BrowserType.FIREFOX;
    	} else if (browserType == BrowserType.CHROME) {
    		return org.openqa.selenium.remote.BrowserType.CHROME;
    	} else if (browserType == BrowserType.EDGE) {
    		return org.openqa.selenium.remote.BrowserType.EDGE;
    	} else if (browserType == BrowserType.SAFARI) {
    		return org.openqa.selenium.remote.BrowserType.SAFARI;
    	} else if (browserType == BrowserType.INTERNET_EXPLORER) {
    		return org.openqa.selenium.remote.BrowserType.IE;
    	} else if (browserType == BrowserType.HTMLUNIT) {
    		return org.openqa.selenium.remote.BrowserType.HTMLUNIT;
    	} else if (browserType == BrowserType.BROWSER) {
    		return org.openqa.selenium.remote.BrowserType.ANDROID;
    	} else {
    		return null;
    	}
    }
    
    public static BrowserType getBrowserTypeFromSeleniumBrowserType(String browserType) {
    	if (org.openqa.selenium.remote.BrowserType.FIREFOX.equals(browserType)) {
    		return BrowserType.FIREFOX;
    	} else if (org.openqa.selenium.remote.BrowserType.CHROME.equals(browserType)) {
    		return BrowserType.CHROME;
    	} else if (org.openqa.selenium.remote.BrowserType.EDGE.equals(browserType)) {
    		return BrowserType.EDGE;
    	} else if (org.openqa.selenium.remote.BrowserType.SAFARI.equals(browserType)) {
    		return BrowserType.SAFARI;
    	} else if (org.openqa.selenium.remote.BrowserType.IE.equals(browserType)) {
    		return BrowserType.INTERNET_EXPLORER;
    	} else if (org.openqa.selenium.remote.BrowserType.HTMLUNIT.equals(browserType)) {
    		return BrowserType.HTMLUNIT;
    	} else if (org.openqa.selenium.remote.BrowserType.ANDROID.equals(browserType)) {
    		return BrowserType.BROWSER;
    	} else {
    		return BrowserType.NONE;
    	}
    }

}
