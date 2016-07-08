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

public enum BrowserType {
    FireFox("*firefox"),
    InternetExplore("*iexplore"),
    Chrome("*chrome"),
    HtmlUnit("*htmlunit"),
    Opera("*opera"),
    Safari("*safari"),
    Browser("*browser"), // default Android browser
    PhantomJS("*phantomjs"),
    ;

	private String bType;

    BrowserType(final String type) {
        this.bType = type;
    }
	
    public static BrowserType getBrowserType(final String browserType) {
        if (browserType.toLowerCase().contains("firefox")) {
            return BrowserType.FireFox;
        } else if (browserType.toLowerCase().contains("iexplore")) {
            return BrowserType.InternetExplore;
        } else if (browserType.toLowerCase().contains("chrome")) {
            return BrowserType.Chrome;
        } else if (browserType.toLowerCase().contains("opera")) {
        	return BrowserType.Opera;
        } else if (browserType.toLowerCase().contains("htmlunit")) {
            return BrowserType.HtmlUnit;
        }  else if (browserType.toLowerCase().contains("safari")) {
            return BrowserType.Safari;
        } else if (browserType.toLowerCase().contains("browser")) {
        	return BrowserType.Browser;
        } else if (browserType.toLowerCase().contains("phantomjs")) {
            return BrowserType.PhantomJS;
        } 
        throw new IllegalArgumentException(String.format("browser %s is unknown", browserType));
        
    }

    public String getBrowserType() {
        return this.bType;
    }

}
