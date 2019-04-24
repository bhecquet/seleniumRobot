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
package com.seleniumtests.uipage.htmlelements;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidTouchAction;
import io.appium.java_client.ios.IOSTouchAction;

/**
 * parent class for all elements to share code between HtmlElement and non HtmlElement classes
 * @author s047432
 *
 */
public abstract class Element {

	protected abstract void findElement(boolean waitForVisibility);
	
	/**
	 * Creates a TouchAction depending on mobile platform. Due to appium 6.0.0 changes
	 * @return
	 */
    protected TouchAction<?> createTouchAction() {
    	String platform = SeleniumTestsContextManager.getThreadContext().getPlatform();
    	PerformsTouchActions performTouchActions = checkForMobile();

    	if (platform.toLowerCase().startsWith("android")) {
    		TouchAction<AndroidTouchAction> touchAction = new TouchAction<>(performTouchActions);
    		return touchAction;
    	} else if (platform.toLowerCase().startsWith("ios")) {
    		TouchAction<IOSTouchAction> touchAction = new TouchAction<>(performTouchActions);
    		return touchAction;
    	} else {
    		throw new ConfigurationException(String.format("%s platform is not supported", platform));
    	}
    }
    
    /**
     * Check if the current platform is a mobile platform
     * if it's the case, search for the element, else, raise a ScenarioException
     */
    protected PerformsTouchActions checkForMobile() {
    	CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false);
    	if (driver == null) {
    		throw new ScenarioException("Driver has not already been created");
    	}
    	
    	if (!SeleniumTestsContextManager.isMobileTest()) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
    	if (!(driver.getWebDriver() instanceof AppiumDriver<?>)) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
    	findElement(true);
    	
    	return (PerformsTouchActions) driver.getWebDriver();    	
    }
}
