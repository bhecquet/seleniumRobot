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
package com.seleniumtests.uipage;

import org.openqa.selenium.By;

/**
 * @author  tbhadauria: tarun.kumar.bhadauria@zalando.de
 */
public class Locator {


	private Locator() {}

    public static By locateByName(final String name) {
        return By.name(name);
    }

    public static By locateById(final String id) {
        return By.id(id);
    }

    public static By locateByCSSSelector(final String cssSelector) {
        return By.cssSelector(cssSelector);
    }

    public static By locateByXPath(final String xPath) {
        return By.xpath(xPath);
    }

    public static By locateByLinkText(final String linkText) {
        return By.linkText(linkText);
    }

    public static By locateByPartialLinkText(final String partialLinkText) {
        return By.partialLinkText(partialLinkText);
    }

    public static By locateByClassName(final String className) {
        return By.className(className);
    }

}
