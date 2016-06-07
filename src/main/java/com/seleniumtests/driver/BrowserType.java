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
    Safari("*safari"),
    Browser("*browser"), // default Android browser
    PhantomJS("*phantomjs"),
    ;

    public static BrowserType getBrowserType(final String browserType) {
        if (browserType.equalsIgnoreCase("*firefox") || browserType.equalsIgnoreCase("firefox")) {
            return BrowserType.FireFox;
        } else if (browserType.equalsIgnoreCase("*iexplore") || browserType.equalsIgnoreCase("iexplore")) {
            return BrowserType.InternetExplore;
        } else if (browserType.equalsIgnoreCase("*chrome") || browserType.equalsIgnoreCase("chrome")) {
            return BrowserType.Chrome;
        } else if (browserType.equalsIgnoreCase("*htmlunit") || browserType.equalsIgnoreCase("htmlunit")) {
            return BrowserType.HtmlUnit;
        } else if (browserType.equalsIgnoreCase("*safari") || browserType.equalsIgnoreCase("safari")) {
            return BrowserType.Safari;
        } else if (browserType.equalsIgnoreCase("*browser") || browserType.equalsIgnoreCase("browser")) {
        	return BrowserType.Browser;
        } else if (browserType.equalsIgnoreCase("*phantomjs") || browserType.equalsIgnoreCase("phantomjs")) {
            return BrowserType.PhantomJS;
        } else {
            return BrowserType.FireFox;
        }
    }

    private String browserType;

    BrowserType(final String type) {
        this.browserType = type;
    }

    public String getBrowserType() {
        return this.browserType;
    }

}
