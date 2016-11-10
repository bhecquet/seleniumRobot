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
package com.seleniumtests.uipage.htmlelements;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.MarionetteDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.SwipeElementDirection;


/**
 * Provides methods to interact with a web page. All HTML element (ButtonElement, LinkElement, TextFieldElement, etc.)
 * extends from this class.
 */
public class HtmlElement {

    protected static final Logger logger = SeleniumRobotLogger.getLogger(HtmlElement.class);


    protected WebDriver driver;
    protected WebElement element = null;
    private String label = null;
    private HtmlElement parent = null;
    private FrameElement frameElement = null;
    private int elementIndex = -1;
    private By by = null;

    public HtmlElement() {
    	this("", By.id(""));
    }
    
    /**
     * Find element using BY locator. Make sure to initialize the driver before calling findElement()
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     *
     * @sample  {@code new HtmlElement("UserId", By.id(userid))}
     */
    
    public HtmlElement(final String label, final By by) {
    	this(label, by, -1);
    }
    
    /**
     * Find element using BY locator. 
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	index  - index of the element to find. In this case, robot will search the Nth element corresponding to
     * 					 the By parameter. Equivalent to new HtmlElement(label, by).findElements().get(N)
     *
     * @sample  {@code new HtmlElement("UserId", By.id(userid), 2)}
     */
    public HtmlElement(final String label, final By by, final int index) {
        this.label = label;
        this.by = by;
        this.elementIndex = index;
        this.frameElement = null;
    }
    
    public HtmlElement(final String label, final By by, final FrameElement frame) {
    	this(label, by, frame, -1);
    }
    
    public HtmlElement(final String label, final By by, final FrameElement frame, final int index) {
    	this.label = label;
    	this.by = by;
    	this.elementIndex = index;
    	this.frameElement = frame;
    }
    
    private HtmlElement(final String label, final By by, final HtmlElement parent) {
    	this(label, by, parent, -1);
    }
    
    private HtmlElement(final String label, final By by, final HtmlElement parent, final int index) {
    	this.label = label;
    	this.by = by;
    	this.parent = parent;
    	this.elementIndex = index;
    	this.frameElement = null;
    }

    @ReplayOnError
    public void click() {
        findElement(true);
        element.click();   
    }
    
    /**
     * Click element in native way by Actions.
     *
     * <p/>
     * <pre class="code">
       clickAt(1, 1);
     * </pre>
     *
     * @param  value
     */
    @ReplayOnError
    public void clickAt(int xOffset, int yOffset) {
        findElement(true);

        try {
            new Actions(driver).moveToElement(element, xOffset, yOffset).click()
                .perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
            element.click();
        }
    }

    @ReplayOnError
    public void simulateClick() {
        findElement(true);

        String mouseOverScript =
            "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(mouseOverScript, element);
        WaitHelper.waitForSeconds(2);

        String clickScript =
            "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('click', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onclick');}";
        js.executeScript(clickScript, element);
        WaitHelper.waitForSeconds(2);
    }
    
    @ReplayOnError
    public void simulateSendKeys(CharSequence... keysToSend) {
    	findElement(true);
    		
    	// click on element before sending keys through keyboard
    	element.click();
    	JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].focus();", element);
        
        if (((CustomEventFiringWebDriver)driver).getWebDriver() instanceof MarionetteDriver) {
        	logger.warn("using specific Marionette method");
        	js.executeScript(String.format("arguments[0].value='%s';", keysToSend[0].toString()), element);
        } else {
			// use keyboard to type
			((CustomEventFiringWebDriver)driver).getKeyboard().sendKeys(keysToSend);
        }
    }

    @ReplayOnError
    public void simulateMoveToElement(final int x, final int y) {
        findElement(true);
        ((JavascriptExecutor) driver).executeScript(
            "function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|errorLogger|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; " +
            "simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]);",
            element, x, y);

    }
    
    /**
     * Find elements inside this element
     * @param by
     */
    @ReplayOnError
    public List<WebElement> findElements(By by) {
    	
    	// find the root element
    	findElement();
        return element.findElements(by);
    }
    
    /**
     * Find an element inside an other one
     * @param by
     * @return
     */
    public HtmlElement findElement(By by) {
    	return new HtmlElement(label, by, this);
    }
    
    /**
     * Find the Nth element inside an other one
     * Equivalent to HtmlElement("", By.id("0").findElements(By.id("1")).get(0); 
     * except that any action on the found element will be retried if it fails
     * @param by 	locator of the element list
     * @param index	index in the list to get
     * @return
     */
    public HtmlElement findElement(By by, int index) {
    	return new HtmlElement(label, by, this, index);
    }
    
    protected void findElement() {
    	findElement(false);
    }
    

    /**
     * Finds the element using By type. Implicit Waits is built in createWebDriver() in WebUIDriver to handle dynamic
     * element problem. This method is invoked before all the basic operations like click, sendKeys, getText, etc. Use
     * waitForPresent to use Explicit Waits to deal with special element which needs long time to present.
     */
    protected void findElement(boolean waitForVisibility) {
        
        // if a parent is defined, search for it before getting the sub element
        if (parent != null) {
        	parent.findElement();
        	if (elementIndex < 0) {
        		element = parent.element.findElement(by);
        	} else {
        		element = parent.element.findElements(by).get(elementIndex);
        	}
        } else {
	        driver = updateDriver();
	        if (elementIndex < 0) {
	        	element = driver.findElement(by);
	        } else {
	        	element = driver.findElements(by).get(elementIndex);
	        }
	        
        }
        makeWebElementVisible(element);
        
        // wait for element to be really visible. should be done only for actions on element
        if (waitForVisibility) {
        	new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(element));
        }
    }
    
    protected void changeCssAttribute(WebElement element, String cssProperty, String cssPropertyValue) {
		String javascript = "arguments[0].style." + cssProperty + "='" + cssPropertyValue + "';";
		((JavascriptExecutor) driver).executeScript(javascript, element); 
	}
    
    /**
	 * Make element visible. Sometimes useful when real elements are backed by an image element
	 */
	protected void makeWebElementVisible(WebElement element) {
		if (SeleniumTestsContextManager.isWebTest()) {
			try {
				
				if (element.getLocation().x < 0) {
					Long viewportHeight = (Long)((JavascriptExecutor) driver).executeScript("return document.documentElement.clientHeight");
					Integer heightPosition = element.getLocation().y > viewportHeight ? element.getLocation().y - viewportHeight.intValue(): element.getLocation().y;
					changeCssAttribute(element, "left", "20px");
					changeCssAttribute(element, "top", heightPosition + "px"); 
					changeCssAttribute(element, "position", "fixed");
				}
				if (element.getAttribute("style").toLowerCase().replace(" ", "").contains("display:none")) {
					changeCssAttribute(element, "display", "block");
				}
				changeCssAttribute(element, "zIndex", "1000");
			} catch (Exception e) {
				return;
			}
				
			// wait for element to be displayed
			try {
				new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(element));
			} catch (ElementNotVisibleException e) {
				TestLogging.info(String.format("element %s not visible", element));
			} catch (Exception e) {
				logger.warn("Could not make element visible");
			}
			
		}
	}

    /**
     * Get all elements in the current page with same locator.
     *
     * @return
     */
	@ReplayOnError
    public List<WebElement> findElements() {
        findElement();

        return driver.findElements(by);
    }

    /**
     * Gets an attribute (using standard key-value pair) from the underlying attribute.
     *
     * @param   name
     *
     * @return
     */
	@ReplayOnError
    public String getAttribute(final String name) {
        findElement();

        return element.getAttribute(name);
    }

    /**
     * Returns the BY locator stored in the HtmlElement.
     *
     * @return
     */
    public By getBy() {
        return by;
    }

    /**
     * Returns the value for the specified CSS key.
     *
     * @param   propertyName
     *
     * @return
     */
    @ReplayOnError
    public String getCssValue(final String propertyName) {
        findElement();

        return element.getCssValue(propertyName);
    }

    /**
     * Get and refresh underlying WebDriver.
     */
    protected WebDriver updateDriver() {
        return WebUIDriver.getWebDriver();
    }
    
    public WebDriver getDriver() {
    	return driver;
    }

    public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	/**
     * Returns the underlying WebDriver WebElement.
     *
     * @return
     */
    @ReplayOnError
    public WebElement getElement() {
    	findElement(true);
        return element;
    }

    /**
     * Executes the given JavaScript against the underlying WebElement.
     *
     * @param   script
     *
     * @return
     */
    @ReplayOnError
    public String getEval(final String script) {
        findElement();
        
        return (String) ((JavascriptExecutor) driver).executeScript(script, element);
    }

    /**
     * Returns the 'height' property of the underlying WebElement's Dimension.
     *
     * @return
     */
    @ReplayOnError
    public int getHeight() {
        findElement();

        return element.getSize().getHeight();
    }

    /**
     * Returns the label used during initialization.
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the Point location of the underlying WebElement.
     *
     * @return
     */
    @ReplayOnError
    public Point getLocation() {
        findElement();

        return element.getLocation();
    }

    /**
     * Returns the Dimension property of the underlying WebElement.
     *
     * @return
     */
    @ReplayOnError
    public Dimension getSize() {
        findElement();

        return element.getSize();
    }

    /**
     * Returns the HTML Tag for the underlying WebElement (div, a, input, etc).
     *
     * @return
     */
    @ReplayOnError
    public String getTagName() {
        findElement();

        return element.getTagName();
    }

    /**
     * Returns the text body of the underlying WebElement.
     *
     * @return
     */
    @ReplayOnError
    public String getText() {
        findElement();

        return element.getText();
    }

    /**
     * Returns the 'value' attribute of the underlying WebElement.
     *
     * @return
     */
    @ReplayOnError
    public String getValue() {
        findElement();

        return element.getAttribute("value");
    }

    /**
     * Returns the 'width' property of the underlying WebElement's Dimension.
     *
     * @return
     */
    @ReplayOnError
    public int getWidth() {
        findElement();

        return element.getSize().getWidth();
    }

    /**
     * Indicates whether or not the web element is currently displayed in the browser.
     *
     * @return
     */
    @ReplayOnError
    public boolean isDisplayed() {

        try {
            findElement();

            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Searches for the element using the BY locator, and indicates whether or not it exists in the page. This can be
     * used to look for hidden objects, whereas isDisplayed() only looks for things that are visible to the user
     *
     * @return
     */
    public boolean isElementPresent(int timeout) {        
        try {
    		waitForPresent(timeout);
    		return true;
    	} catch (TimeoutException e) {
    		return false;
    	}
    }
    public boolean isElementPresent() { 
    	return isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }

    /**
     * Indicates whether or not the element is enabled in the browser.
     *
     * @return
     */
    @ReplayOnError
    public boolean isEnabled() {
        findElement();

        return element.isEnabled();
    }

    /**
     * Indicates whether or not the element is selected in the browser.
     *
     * @return
     */
    @ReplayOnError
    public boolean isSelected() {
        findElement();

        return element.isSelected();
    }

    /**
     * Whether or not the indicated text is contained in the element's getText() attribute.
     *
     * @param   text
     *
     * @return
     */
    public boolean isTextPresent(final String pattern) {
        String text = getText();
        return text != null && (text.contains(pattern) || text.matches(pattern));
    }

    /**
     * Forces a mouseDown event on the WebElement.
     */
    @ReplayOnError
    public void mouseDown() {
        findElement(true);

        Locatable item = (Locatable) element;
        Mouse mouse = ((HasInputDevices) driver).getMouse();
        mouse.mouseDown(item.getCoordinates());
    }

    /**
     * Forces a mouseOver event on the WebElement.
     */
    @ReplayOnError
    public void mouseOver() {
        findElement(true);

        Locatable hoverItem = (Locatable) element;
        Mouse mouse = ((HasInputDevices) driver).getMouse();
        mouse.mouseMove(hoverItem.getCoordinates());
    }

    /**
     * Forces a mouseOver event on the WebElement using simulate by JavaScript way for some dynamic menu.
     */
    @ReplayOnError
    public void simulateMouseOver() {
        findElement(true);

        String mouseOverScript =
            "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(mouseOverScript, element);
    }

    /**
     * Forces a mouseUp event on the WebElement.
     */
    @ReplayOnError
    public void mouseUp() {
        findElement(true);

        Locatable item = (Locatable) element;
        Mouse mouse = ((HasInputDevices) driver).getMouse();
        mouse.mouseUp(item.getCoordinates());
    }
    
    public void sendKeys(final CharSequence text) {
    	// Appium seems to clear field before writing
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
            sendKeys(text, false);
        } else {
        	sendKeys(text, true);
        }
    }

    /**
     * Sends the indicated CharSequence to the WebElement.
     *
     * @param  arg0
     * @param 	clear	if true, clear field before writing
     */
    @ReplayOnError
    public void sendKeys(final CharSequence arg0, final boolean clear) {
        findElement(true);
        
        if (clear) {
        	element.clear();
        } 
        element.sendKeys(arg0);
    }

    /**
     * Method, which should never be used.
     */
    protected void sleep(final int waitTime) throws InterruptedException {
        Thread.sleep(waitTime);
    }
    
    /**
	 * Returns string matching the pattern
	 * 
	 * @param pattern			pattern to find in element text or one of its attribute
	 * @param attributeName		name of the attribute to look for
	 * @return found string
	 */
    @ReplayOnError
	public String findPattern(Pattern pattern, String attributeName) {
		findElement();
		String attributeValue;
		if ("text".equals(attributeName)) {
			attributeValue = element.getText();
		} else {
			attributeValue = element.getAttribute(attributeName);
		}

		Matcher matcher = pattern.matcher(attributeValue);
		if (matcher.matches() && matcher.groupCount() > 0) {
			return matcher.group(1);
		}

		return "";
	}
    
    /**
	 * Returns URL present in one of the element attributes
	 * @param attributeName		attribute name in which we should look at. Give "text" to search in value
	 * @return 					the found link
	 */
	public String findLink(String attributeName) {
		
		// link <a href="#" id="linkPopup2" onclick="window.open('http://www.infotel.com/', '_blank');">
		String link = findPattern(Pattern.compile(".*(http://.*?)'\"?.*"), attributeName);
		if (!"".equals(link)) {
			return link;
		}
		
		// link with simple quotes  <a href="#" id="linkPopup" onclick='window.open("http://www.infotel.com/", "_blank");'>
		link = findPattern(Pattern.compile(".*(http://.*?)\"'?.*"), attributeName);
		if (!"".equals(link)) {
			return link;
		}
		
		// no quotes
		return findPattern(Pattern.compile(".*(http://.*)"), attributeName);
	}

    /**
     * Converts the Type, Locator and LabelElement attributes of the HtmlElement into a readable and report-friendly
     * string.
     *
     * @return
     */
    public String toHTML() {
        return getClass().getSimpleName().toLowerCase() +
            " <a style=\"font-style:normal;color:#8C8984;text-decoration:none;\" href=# \">" +
            getLabel() + ",: " + getBy().toString() + "</a>";
    }

    /**
     * Returns a friendly string, representing the HtmlElement's Type, LabelElement and Locator.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getLabel() +
            ", by={" + getBy().toString() + "}";
    }
    
    /**
     * Method created for test purpose only
     */
    @ReplayOnError
    public void doNothing() {
    	// do nothing
    }
    
    /*
     * Methods for mobile actions only
     */
    /**
     * Check if the current platform is a mobile platform
     * if it's the case, search for the element, else, raise a ScenarioException
     */
    private void checkForMobile() {
    	if (!(((CustomEventFiringWebDriver)driver).getWebDriver() instanceof AppiumDriver<?>)) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
    	findElement(true);
    }
    
    /**
     * findElement returns a EventFiringWebElement which is not compatible with MobileElement
     * Get the unerlying element and return it
     */
    private WebElement getUnderlyingElement(WebElement element) {
    	
    	if (element.getClass().getName().contains("EventFiringWebElement")) {
    		try {
				Method getWrappedElementMethod = element.getClass().getDeclaredMethod("getWrappedElement");
				getWrappedElementMethod.setAccessible(true);
				return (WebElement) getWrappedElementMethod.invoke(element);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new DriverExceptions("cannot get wrapped Element", e);
			}
    	} else {
    		return element;
    	}
    	
    }
    
    @ReplayOnError
    public Point getCenter() {
    	checkForMobile();
    	return ((MobileElement)getUnderlyingElement(element)).getCenter();
    }
    
    @ReplayOnError
    public void pinch() {
    	checkForMobile();
    	((MobileElement)getUnderlyingElement(element)).pinch();
    }
    
    /**
     * Convenience method for swiping on the given element to the given direction
     * @param direction		UP, DOWN, LEFT, RIGHT
     * @param duration		amount of time in milliseconds for the entire swipe action to take
     */
    @ReplayOnError
    public void swipe(SwipeElementDirection direction, int duration) {
    	checkForMobile();
    	((MobileElement)getUnderlyingElement(element)).swipe(direction, duration);
    }
    
    /**
     * Convenience method for swiping on the given element to the given direction
     * @param direction				UP, DOWN, LEFT, RIGHT
     * @param offsetFromStartBorder
     * @param offsetFromEndBorder 	offsetFromEndBorder is the offset from the border of the element where the swiping 
     * 								should be started. If direction is UP then this is offset from the bottom of the element. 
     * 								If direction is DOWN then this is offset from the top of the element. If direction is RIGHT 
     * 								then this is offset from the left border of the element. If direction is LEFT then this is 
     * 								offset from the right border of the element.
     * @param duration				amount of time in milliseconds for the entire swipe action to take
     */
    @ReplayOnError
    public void swipe(SwipeElementDirection direction, int offsetFromStartBorder, int offsetFromEndBorder, int duration) {
    	checkForMobile();
    	((MobileElement)getUnderlyingElement(element)).swipe(direction, offsetFromStartBorder, offsetFromEndBorder, duration);
    }
    
    @ReplayOnError
    public void tap(int fingers, int duration) {
    	checkForMobile();
    	((MobileElement)getUnderlyingElement(element)).tap(fingers, duration);
    }
    
    @ReplayOnError
    public void zoom() {
    	checkForMobile();
    	((MobileElement)getUnderlyingElement(element)).zoom();
    }
    
    /**
     * Wait element to present using Explicit Waits with default EXPLICIT_WAIT_TIME_OUT = 15 seconds.
     */
    public void waitForPresent() {
        waitForPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }

    /**
     * Wait element to present using Explicit Waits with timeout in seconds. This method is used for special element
     * which needs long time to present.
     */
    @ReplayOnError
    public void waitForPresent(final int timeout) {
    	
    	// refresh driver
    	driver = updateDriver();
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
        
    }

	public FrameElement getFrameElement() {
		return frameElement;
	}
}
