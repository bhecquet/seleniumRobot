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

public enum BrowserType {
    FIREFOX("*firefox"),
    INTERNETEXPLORER("*iexplore"),
    EDGE("*edge"),
    CHROME("*chrome"),
    HTMLUNIT("*htmlunit"),
    OPERA("*opera"),
    SAFARI("*safari"),
    BROWSER("*browser"), // default Android browser
    PHANTOMJS("*phantomjs"),
    NONE("*none")
    ;

	private String bType;

    BrowserType(final String type) {
        this.bType = type;
    }
	
    public static BrowserType getBrowserType(final String browserType) {
        if (browserType.toLowerCase().contains("firefox")) {
            return BrowserType.FIREFOX;
        } else if (browserType.toLowerCase().contains("iexplore")) {
            return BrowserType.INTERNETEXPLORER;
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
        } else if (browserType.toLowerCase().contains("browser")) {
        	return BrowserType.BROWSER;
        } else if (browserType.toLowerCase().contains("phantomjs")) {
            return BrowserType.PHANTOMJS;
        } else if (browserType.toLowerCase().contains("none")) {
        	return BrowserType.NONE;
        } 
        throw new IllegalArgumentException(String.format("browser %s is unknown", browserType));
        
    }

    public String getBrowserType() {
        return this.bType;
    }

}
