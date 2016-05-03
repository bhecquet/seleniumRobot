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

/**
 * @author  tbhadauria <tarun.kumar.bhadauria@zalando.de>
 */
public enum TestType {

    WEB("web"),
    APP("app"),
    NON_GUI("NonGUI"),
    APPIUM_WEB_ANDROID("appium_web_android"),
    APPIUM_WEB_IOS("appium_web_ios"),
	APPIUM_APP_ANDROID("appium_app_android"),
	APPIUM_APP_IOS("appium_app_ios");

    String testType;

    TestType(final String testType) {
        this.testType = testType;
    }

    public String getTestType() {
        return testType;
    }

}
