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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.internal.HasIdentity;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.MobileElement;
import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;


/**
 * Provides methods to interact with a web page. All HTML element (ButtonElement, LinkElement, TextFieldElement, etc.)
 * extends from this class.
 * 
 * 
 */
public class HtmlElement extends Element implements WebElement, Locatable, HasIdentity { 
	// WARNING!!!: we use the deprecated Locatable interface because it's used by Actions class
	// unit test TestPicutreElement.testClick() fails if the new interface is used
	// so wait to this old interface to be really removed

    protected static final Logger logger = SeleniumRobotLogger.getLogger(HtmlElement.class);
    public static final Integer FIRST_VISIBLE = Integer.MAX_VALUE;
    
    String JS_CLICK_TRIPLE = 
    		  "var target = arguments[0];" +
    		  "emit('mousedown', {buttons: 1}); " +
    		  "emit('mouseup',   {});" +
    		  "emit('mousedown', {buttons: 1}); " +
    		  "emit('mouseup',   {});" +
    		  "emit('mousedown', {buttons: 1}); " +
    		  "emit('mouseup',   {});" +
    		  "emit('click',     {detail: 3});  " +
    		  "" +
    		  "function emit(name, init) {" +
    		    "target.dispatchEvent(new MouseEvent(name, init));" +
    		  "}" ;

    String JS_CLICK_DOUBLE = 
    		"if(document.createEvent){"
    		+ "		var evObj = document.createEvent('MouseEvents');"
    		+ "		evObj.initEvent('dblclick', true, false); "
    		+ "		arguments[0].dispatchEvent(evObj);"
    		+ "} else if(document.createEventObject) { "
    		+ "		arguments[0].fireEvent('ondblclick');"
    		+ "}";
    
    protected WebDriver driver;
    protected WebElement element = null;
    protected String label = null;
    protected HtmlElement parent = null;
    protected FrameElement frameElement = null;
    private Integer elementIndex = -1;
    private By by = null;
    private String origin  = null;

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
    	this(label, by, (Integer)null);
    }
    
    /**
     * Find element using BY locator. 
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	index  - index of the element to find. In this case, robot will search the Nth element corresponding to
     * 					 the By parameter. Equivalent to new HtmlElement(label, by).findElements().get(N)
     * 					 If index is null, use <code>driver.findElement(By)</code> intenally
     *  				 If index is negative, search from the last one (-1)
     *  			     If index is HtmlElement.FIRST_VISIBLE, search the first visible element
     *
     * @sample  {@code new HtmlElement("UserId", By.id(userid), 2)}
     */
    public HtmlElement(final String label, final By by, final Integer index) {
    	this(label, by, null, index);
    }
    
    public HtmlElement(final String label, final By by, final FrameElement frame) {
    	this(label, by, frame, null);
    }
    
    public HtmlElement(final String label, final By by, final FrameElement frame, final Integer index) {
    	this(label, by, frame, null, index);
    }
    
    public HtmlElement(final String label, final By by, final HtmlElement parent) {
    	this(label, by, parent, null);
    }
    
    public HtmlElement(final String label, final By by, final HtmlElement parent, final Integer index) {
    	this(label, by, null, parent, index);
    }
    
    public HtmlElement(final String label, final By by, final FrameElement frame, final HtmlElement parent, final Integer index) {
    	this.label = label;
    	this.by = by;
    	this.elementIndex = index;
    	this.frameElement = frame;
    	this.parent = parent;
    	
    	origin = PageObject.getCallingPage(Thread.currentThread().getStackTrace());
    }

    /**
     * Native click
     */
    @ReplayOnError
    public void click() {
        findElement(true);
        element.click();   
    }
    
    /**
     * Click with CompositeActions
     */
    @ReplayOnError
    public void clickAction() {
    	findElement(true);
    	
    	// element must be fully visible (for example checkbox), to be actionned
		((CustomEventFiringWebDriver)driver).scrollToElement(element, -20);	
    	try {
            new Actions(driver).click(element).perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
        }
    }
    
    /**
     * Double Click with CompositeActions
     */
    @ReplayOnError
    public void doubleClickAction() {
    	findElement(true);
    	
    	// element must be fully visible (for example checkbox), to be actionned
		((CustomEventFiringWebDriver)driver).scrollToElement(element, -20);	
    	try {
            new Actions(driver).doubleClick(element).perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
        }
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
    	findElement();
		((CustomEventFiringWebDriver)driver).scrollToElement(element, yOffset);
        
        try {
            new Actions(driver).moveToElement(element, xOffset, yOffset).click()
                .perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
            element.click();
        }
    }

    /**
     * Click with javascript
     */
    @ReplayOnError
    public void simulateClick() {
    	if (SeleniumTestsContextManager.isWebTest()) {
    		((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).updateWindowsHandles();
    	}
    	
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
    public void simulateDoubleClick() {
        findElement(true);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(JS_CLICK_DOUBLE, element);

    }
    
    @ReplayOnError
    public void simulateSendKeys(CharSequence... keysToSend) {
    	findElement(true);
    		
    	// click on element before sending keys through keyboard
    	element.click();
    	JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].focus();", element);
        
        WebDriver realDriver = ((CustomEventFiringWebDriver)driver).getWebDriver();
        
        // handle org.openqa.selenium.UnsupportedCommandException: sendKeysToActiveElement which are not available for firefox and IE
        if ((realDriver instanceof FirefoxDriver && FirefoxDriverFactory.isMarionetteMode())
        		|| realDriver instanceof InternetExplorerDriver) {
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
    @Override
    @ReplayOnError
    public List<WebElement> findElements(By by) {
    	
    	// find the root element
    	findElement();
        return element.findElements(by);
    }

    @ReplayOnError
    public List<WebElement> findHtmlElements(By by) {
    	
    	// find the root element
    	findElement();
    	List<WebElement> htmlElements = new ArrayList<>();
    	List<WebElement> elements = element.findElements(by);
    	for (int i = 0; i < elements.size(); i++) {
    		htmlElements.add(new HtmlElement("", by, frameElement, parent, i));
    	}
    	return htmlElements;
    }
    
    /**
     * Find an element inside an other one
     * @param by
     * @return
     */
	@SuppressWarnings("unchecked")
	@Override
    public HtmlElement findElement(By by) {
    	return new HtmlElement(label, by, this);
    }
    
    public ButtonElement findButtonElement(By by) {
    	return new ButtonElement(label, by, this);
    }
    public CheckBoxElement findCheckBoxElement(By by) {
    	return new CheckBoxElement(label, by, this);
    }
    public ImageElement findImageElement(By by) {
    	return new ImageElement(label, by, this);
    }
    public LabelElement findLabelElement(By by) {
    	return new LabelElement(label, by, this);
    }
    public LinkElement findLinkElement(By by) {
    	return new LinkElement(label, by, this);
    }
    public RadioButtonElement findRadioButtonElement(By by) {
    	return new RadioButtonElement(label, by, this);
    }
    public SelectList findSelectList(By by) {
    	return new SelectList(label, by, this);
    }
    public Table findTable(By by) {
    	return new Table(label, by, this);
    }
    public TextFieldElement findTextFieldElement(By by) {
    	return new TextFieldElement(label, by, this);
    }
    
    /**
     * Find the Nth element inside an other one
     * Equivalent to HtmlElement("", By.id("0").findElements(By.id("1")).get(0); 
     * except that any action on the found element will be retried if it fails
     * @param by 	locator of the element list
     * @param index	index in the list to get
     * @return
     */
    public HtmlElement findElement(By by, Integer index) {
    	return new HtmlElement(label, by, this, index);
    }
    
    public ButtonElement findButtonElement(By by, Integer index) {
    	return new ButtonElement(label, by, this, index);
    }
    public CheckBoxElement findCheckBoxElement(By by, Integer index) {
    	return new CheckBoxElement(label, by, this, index);
    }
    public ImageElement findImageElement(By by, Integer index) {
    	return new ImageElement(label, by, this, index);
    }
    public LabelElement findLabelElement(By by, Integer index) {
    	return new LabelElement(label, by, this, index);
    }
    public LinkElement findLinkElement(By by, Integer index) {
    	return new LinkElement(label, by, this, index);
    }
    public RadioButtonElement findRadioButtonElement(By by, Integer index) {
    	return new RadioButtonElement(label, by, this, index);
    }
    public SelectList findSelectList(By by, Integer index) {
    	return new SelectList(label, by, this, index);
    }
    public Table findTable(By by, Integer index) {
    	return new Table(label, by, this, index);
    }
    public TextFieldElement findTextFieldElement(By by, Integer index) {
    	return new TextFieldElement(label, by, this, index);
    }
    
    protected void findElement() {
    	findElement(false, true);
    }
    
    protected void findElement(boolean waitForVisibility) {
    	findElement(waitForVisibility, true);
    }
    

    /**
     * Finds the element using By type. Implicit Waits is built in createWebDriver() in WebUIDriver to handle dynamic
     * element problem. This method is invoked before all the basic operations like click, sendKeys, getText, etc. Use
     * waitForPresent to use Explicit Waits to deal with special element which needs long time to present.
     * @param waitForVisibility		wait for element to be visible
     * @param makeVisible			whether we try to make the element visible. Should be true except when trying to know if element is displayed
     */
    public void findElement(boolean waitForVisibility, boolean makeVisible) {
        // TODO: https://discuss.appium.io/t/how-can-i-scroll-to-an-element-in-appium-im-using-android-native-app/10618/14
    	// String DESTINATION_ELEMENT_TEXT= "KUBO";
    	//((AndroidDriver) driver).findElementByAndroidUIAutomator("new UiScrollable(new UiSelector())
    	//		.scrollIntoView(new UiSelector().text(DESTINATION_ELEMENT_TEXT))");
        
    	ElementInfo elementInfo = null;
    	
    	// search element information. Do not stop if something goes wrong here
    	if (SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch() != ElementInfo.Mode.FALSE) {
    		try {
    			elementInfo = ElementInfo.getInstance(this);
    		} catch (Throwable e) {}
    	}
    	
    	// if a parent is defined, search for it before getting the sub element
    	driver = updateDriver();
        if (parent != null) {
        	parent.findElement();
        	
        	// issue #166: add a dot in front of xpath expression if we search the element inside a parent
        	if (by instanceof ByXPath) {
        		try {
					Field xpathExpressionField = ByXPath.class.getDeclaredField("xpathExpression");
					xpathExpressionField.setAccessible(true);
					String xpath = (String)xpathExpressionField.get(by);
					
					if (xpath.startsWith("//")) {
						by = By.xpath("." + xpath);
					}
					
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new CustomSeleniumTestsException(e);
				}
        		
        	}
        	
        	element = findSeleniumElement(parent.element, elementInfo);

        } else {
        	element = findSeleniumElement(driver, elementInfo);
        }
        
        
        if (makeVisible) { 
        	makeWebElementVisible(element);
    		((CustomEventFiringWebDriver)driver).scrollToElement(element, -20);	
        }
        
        // wait for element to be really visible. should be done only for actions on element
        if (waitForVisibility && makeVisible) {
        	new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(element));
        }
        
        // If we are here, element has been found, update elementInformation
        if (elementInfo != null) {
        	try {
	        	elementInfo.updateInfo(this, driver);
	        	elementInfo.exportToJsonFile(this);
        	} catch (Throwable e) {
        		logger.warn("Error storing element information: " + e.getMessage());
        	}
        }
    }
    
    /**
     * Call driver to really search the element
     * If index is specified, return the Nth element corresponding to search
     * 
     * @param context
     * @param elementInfo
     * @return
     */
    private WebElement findSeleniumElement(SearchContext context, ElementInfo elementInfo) {
    	enterFrame();
    	
		WebElement seleniumElement;
    	try {
	    	if (elementIndex == null) {
	    		seleniumElement = context.findElement(by);
	    	} else {
	    		seleniumElement = getElementByIndex(context.findElements(by));
	    	}
	    	return seleniumElement;
    	} catch (WebDriverException e) {
    		
    		// element not found, raise exception
    		// this code is here to prepare advanced element search
    		throw e;
    	}
    }
        
    /**
     * returns an element depending on configured index
     * @param allElements
     */
    private WebElement getElementByIndex(List<WebElement> allElements) {
    	if (elementIndex == FIRST_VISIBLE) {
			for (WebElement el: allElements) {
				if (el.isDisplayed()) {
					return el;
				}
			}
			throw new WebDriverException("no visible element has been found for " + by.toString());
    	} else if (elementIndex < 0) {
    		return allElements.get(allElements.size() + elementIndex);
		} else {
			if (elementIndex == null) {
				elementIndex = 0;
			}
			return allElements.get(elementIndex);
		}
    }
    
    /**
     * Method for going into the right frame before doing anything else
     * this method should be called each time we need to get an element
     * Therefore, it's used inside findElement() method
     */
    private void enterFrame() {
    	List<FrameElement> frameTree = new ArrayList<>();
    	FrameElement frame = getFrameElement();
		
		while (frame != null) {
			frameTree.add(0, frame);
			frame = frame.getFrameElement();
		}

		for (FrameElement frameEl: frameTree) {
			WebElement frameWebElement = driver.findElement(frameEl.getBy());
			((CustomEventFiringWebDriver)driver).scrollToElement(frameWebElement, -20);		
			driver.switchTo().frame(frameWebElement);
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
			if (element.isDisplayed()) {
				return;
			}
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
//				changeCssAttribute(element, "clip", "auto");
				changeCssAttribute(element, "zIndex", "100000");
			} catch (Exception e) {
				return;
			}
				
			// wait for element to be displayed
			try {
				new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(element));
			} catch (ElementNotVisibleException e) {
				TestLogging.info(String.format("element %s not visible", element));
			} catch (Exception e) {
				logger.warn("Could not make element visible", e);
			}
			
		}
	}
	
	/**
	 * Move to element
	 * @param element
	 */	
	public void scrollToElement(int yOffset) {
		findElement();
		((CustomEventFiringWebDriver)driver).scrollToElement(element, yOffset);		
	}

    /**
     * Get all elements in the current page with same locator.
     *
     * @return
     */
	@ReplayOnError
    public List<WebElement> findElements() {
		
		// call findElement to enter any specified frame and search for parent elements
        findElement();

        // issue #167: if we have a parent, search elements inside it
        if (parent != null) {
        	return parent.element.findElements(by);
        } else {
        	return driver.findElements(by);
        }
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
    @Override
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
    @Override
    @ReplayOnError
    public Point getLocation() {
        findElement();

        return element.getLocation();
    }
    

	@Override
	@ReplayOnError
	public Rectangle getRect() {
		findElement();
		return new Rectangle(element.getLocation(), element.getSize());
	}

    /**
     * Returns the Dimension property of the underlying WebElement.
     *
     * @return
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean isDisplayed() {
        try {
            return isDisplayedRetry();
        } catch (Exception e) {
            return false;
        }
    }
    
    @ReplayOnError
    public boolean isDisplayedRetry() {
    	findElement(false, false);
        return element.isDisplayed();
    }

    /**
     * Searches for the element using the BY locator, and indicates whether or not it exists in the page. This can be
     * used to look for hidden objects, whereas isDisplayed() only looks for things that are visible to the user
     * @param timeout timeout in seconds
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
    @Override
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
    @Override
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
    
    @Override
    public void sendKeys(CharSequence... keysToSend) {
    	sendKeys(true, keysToSend);
    }
    
    public void sendKeys(final boolean blurAfter, CharSequence... keysToSend) {
    	// Appium seems to clear field before writing
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
    		sendKeys(false, blurAfter, keysToSend);
    	} else {
    		sendKeys(true, blurAfter, keysToSend);
    	}
    }
    
    protected void blur() {
    	if (SeleniumTestsContextManager.isWebTest() && "input".equalsIgnoreCase(element.getTagName())) {
    		try {
    			((JavascriptExecutor) driver).executeScript("arguments[0].blur();", element);
    		} catch (Exception e) {	
    			logger.error(e);
    		}
    	}
    }
    
    /**
     * Send keys through composite actions
     * /!\ does not clear text before and no blur after
     * 
     * @param keysToSend
     */
    @ReplayOnError
    public void sendKeysAction(CharSequence... keysToSend) {
    	findElement(true);
    	new Actions(driver).sendKeys(element, keysToSend).build().perform();
    }

    /**
     * Sends the indicated CharSequence to the WebElement.
     *
     * @param 	clear		if true, clear field before writing
     * @param	blurAfter	if true, do blur() after sendKeys has been done
     * @param   keysToSend	write this text
     */
    @ReplayOnError
    public void sendKeys(final boolean clear, final boolean blurAfter, CharSequence... keysToSend) {
        findElement(true);
        
        if (clear) {
        	element.clear();
        } 
        element.sendKeys(keysToSend);
        
        if (blurAfter) {
        	blur();
        }
    }
    
    @Override
    @ReplayOnError
    public void clear() {
    	findElement(true);
    	element.clear();
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
        String elDescr = getClass().getSimpleName() + " " + getLabel() + ", by={" + getBy().toString() + "}";
        if (parent != null) {
        	elDescr += ", sub-element of " + parent.toString();
        }
        return elDescr;
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
    	try {
    		checkForMobile();
    		return ((MobileElement)getUnderlyingElement(element)).getCenter();
    	} catch (ScenarioException e) {
    		Rectangle rectangle = element.getRect();
    		return new Point(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    	}	
    }
    
    @ReplayOnError
    public void pinch() {
    	PerformsTouchActions performTouchActions = checkForMobile();
    	MobileElement mobElement = (MobileElement) getUnderlyingElement(element);
    	
    	// code taken from appium
		MultiTouchAction multiTouch = new MultiTouchAction(performTouchActions);
		
		Point upperLeft = mobElement.getLocation();
		Point center = mobElement.getCenter();
		int yOffset = center.getY() - upperLeft.getY();
		
		TouchAction<?> action0 = createTouchAction().press(ElementOption.element(mobElement, center.getX(), center.getY() - yOffset))
																	.moveTo(ElementOption.element(mobElement))
																	.release();
		TouchAction<?> action1 = createTouchAction().press(ElementOption.element(mobElement, center.getX(), center.getY() + yOffset))
																	.moveTo(ElementOption.element(mobElement))
																	.release();
		
		multiTouch.add(action0).add(action1).perform();

    }
    
    /**
     * Convenience method for swiping on the given element to the given direction
     * @param xOffset	X offset from the top-left corner of the element
     * @param yOffset	Y offset from the top-left corner of the element
     * @param xMove		Movement amplitude on x axis
     * @param yMove		Movement amplitude on y axis
     */
    @ReplayOnError
    public void swipe(int xOffset, int yOffset, int xMove, int yMove) {
    	MobileElement mobElement = (MobileElement) getUnderlyingElement(element);
        
        createTouchAction().press(ElementOption.element(mobElement, xOffset, yOffset))
			.waitAction()
			.moveTo(ElementOption.element(mobElement, xMove, yMove))
			.release().perform();
    }
    
    /**
     * Tap with X fingers on screen
     * @param fingers	number of fingers to tap with
     * @param duration	duration in ms to wait before releasing
     */
    @ReplayOnError
    public void tap(int fingers, int duration) {
    	PerformsTouchActions performTouchActions = checkForMobile();
    	MobileElement mobElement = (MobileElement) getUnderlyingElement(element);
    
    	// code from appium
    	MultiTouchAction multiTouch = new MultiTouchAction(performTouchActions);

        for (int i = 0; i < fingers; i++) {
            TouchAction<?> tap = createTouchAction();
            multiTouch.add(tap.press(ElementOption.element(mobElement)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(duration))).release());
        }

        multiTouch.perform();
    }
    
    @ReplayOnError
    public void zoom() {
    	PerformsTouchActions performTouchActions = checkForMobile();
    	MobileElement mobElement = (MobileElement) getUnderlyingElement(element);
    	
    	MultiTouchAction multiTouch = new MultiTouchAction(performTouchActions);

        Point upperLeft = mobElement.getLocation();
        Point center = mobElement.getCenter();
        int yOffset = center.getY() - upperLeft.getY();

        TouchAction<?> action0 = createTouchAction().press(PointOption.point(center.getX(), center.getY()))
                												.moveTo(ElementOption.element(mobElement, center.getX(), center.getY() - yOffset))
                												.release();
        TouchAction<?> action1 = createTouchAction().press(PointOption.point(center.getX(), center.getY()))
                												.moveTo(ElementOption.element(mobElement, center.getX(), center.getY() + yOffset))
                												.release();
        multiTouch.add(action0).add(action1).perform();
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
     * This method is replayed because it may fail if frame is not present at start. The replay is not done if TimeOutException raises (see ReplayAction class)
     * @param timeout	timeout in seconds
     */
    @ReplayOnError
    public void waitForPresent(final int timeout) {
    	
    	// refresh driver
    	driver = updateDriver();
    	enterFrame();
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
        
    }

	public FrameElement getFrameElement() {
		return frameElement;
	}

	public void setFrameElement(FrameElement frameElement) {
		this.frameElement = frameElement;
	}
	
	/**
	 * Sometimes, the frame defined for the element may change
	 * @param frameElement
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends HtmlElement> T changeFrame(FrameElement frameElement, Class<T> type) {
		setFrameElement(frameElement);

		return (T)this;
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> target) {
		findElement();
		if (((HasCapabilities) driver).getCapabilities().getCapability(CapabilityType.TAKES_SCREENSHOT) != null) {
			return element.getScreenshotAs(target);
		} else {
			return null;
		}
	}

	@Override
	@ReplayOnError
	public void submit() {
		findElement(true);
		element.submit();
	}

	@Override
	@ReplayOnError
	public Coordinates getCoordinates() {
		findElement();
		return ((Locatable)element).getCoordinates();
	}
	
	@Override
	public String getId() {
		findElement();
		return ((HasIdentity)getUnderlyingElement(element)).getId();
	}
	
	public Map<String, Object> toJson() {
		findElement();
		return ((RemoteWebElement)getUnderlyingElement(element)).toJson();
	}
	
	public HtmlElement getParent() {
		return parent;
	}

	public void setParent(HtmlElement parent) {
		this.parent = parent;
	}

	public void setBy(By by) {
		this.by = by;
	}

	/**
	 * USE ONLY for testing
	 * @param element
	 */
	public void setElement(WebElement element) {
		this.element = element;
	}

	public Integer getElementIndex() {
		return elementIndex;
	}
	
	/**
	 * Returns the real web element or null if it has not been searched
	 * @return
	 */
	public WebElement getRealElement() {
		return element;
	}

	/**
	 * Change the search index of the element (its order in element list)
	 * @param elementIndex
	 */
	public void setElementIndex(Integer elementIndex) {
		this.elementIndex = elementIndex;
	}

	public String getOrigin() {
		return origin;
	}


}
