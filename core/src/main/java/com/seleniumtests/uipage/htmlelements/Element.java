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

import org.mockito.internal.matchers.Null;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.decorators.Decorated;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.PageObject;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;


/**
 * parent class for all elements to share code between HtmlElement and non HtmlElement classes
 * @author s047432
 *
 */
public abstract class Element {


	protected Integer replayTimeout = 30;

	protected ThreadLocal<String> fieldName = new ThreadLocal<>();

	protected String label;


	// origin is the class where object is declared
	// origin may be null if object is created outside of any PageObject (which should never happen outside of unit tests)
	protected String origin;

	// the page instance that uses the object.
	// will be different for each thread
	// will be null if Element is not declared as a field of PageObject
	protected ThreadLocal<PageObject> callingPage = new ThreadLocal<>();
	
	protected abstract void findElement(boolean waitForVisibility);
	
	public abstract void click();
	
	public abstract void sendKeys(CharSequence ... text);
	
	public abstract boolean isElementPresent();

	protected Element(String label) {
		this.label = label;
		origin = PageObject.getCallingPage(Thread.currentThread().getStackTrace());
	}
	
	/**
	 * Creates a TouchAction depending on mobile platform. Due to appium 6.0.0 changes
	 * @return
	 */
    protected TouchAction<?> createTouchAction() {
    	String platform = SeleniumTestsContextManager.getThreadContext().getPlatform();
    	PerformsTouchActions performTouchActions = checkForMobile();

    	if (platform.toLowerCase().startsWith("android")) {
    		return new TouchAction<>(performTouchActions);
    	} else if (platform.toLowerCase().startsWith("ios")) {
    		return new TouchAction<>(performTouchActions);
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
    	if (!(((Decorated<WebDriver>)driver.getWebDriver()).getOriginal() instanceof AppiumDriver)) {
    		throw new ScenarioException("action is available only for appium drivers");
    	}
    	findElement(true);
    	
    	return (PerformsTouchActions) driver.getWebDriver();    	
    }


	public int getReplayTimeout() {

    	if (replayTimeout != null) {
    		return replayTimeout;
    	} else {
    		return SeleniumTestsContextManager.getThreadContext().getReplayTimeout();
    	}
	}


	public void setReplayTimeout(int replayTimeout) {
		this.replayTimeout = replayTimeout;
	}

	public void setCallingPage(PageObject page) {
		callingPage.set(page);
	}
	
	public PageObject getCallingPage() {
		return callingPage.get();
	}
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public Class<? extends PageObject> getOriginClass() {
		try {
			return (Class<? extends PageObject>) Class.forName(origin);
		} catch (ClassNotFoundException | NullPointerException e) {
			return getCallingPage() == null ? null : getCallingPage().getClass();
		}
	}

	public String getFieldName() {
		return fieldName.get();
	}

	public void setFieldName(String fieldName) {
		this.fieldName.set(fieldName);
	}

	/**
	 * Returns the label used during initialization.
	 * As this label is optional, it may be empty
	 *
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Get the element name
	 * If label is present and not empty, use it
	 * If label is not present and element is a class / instance field, use it
	 * Else, use the selector
	 * @return
	 */
	public String getName() {
		if (label != null && !label.isEmpty()) {
			return label;
		} else if (fieldName.get() != null) {
			return fieldName.get();
		}
		return null;
	}
}
