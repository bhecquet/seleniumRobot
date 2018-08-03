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
    	if (!SeleniumTestsContextManager.isMobileTest()) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
    	if (!(((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).getWebDriver() instanceof AppiumDriver<?>)) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
    	findElement(true);
    	
    	return (PerformsTouchActions) ((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).getWebDriver();    	
    }
}
