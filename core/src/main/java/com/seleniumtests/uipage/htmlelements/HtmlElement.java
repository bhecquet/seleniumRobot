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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.seleniumtests.uipage.selectorupdaters.MobileAndroidAppUpdater;
import com.seleniumtests.uipage.selectorupdaters.MobileWebViewUpdater;
import com.seleniumtests.uipage.selectorupdaters.ShadowDomRootUpdater;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.ScreenshotException;
import org.openqa.selenium.support.decorators.Decorated;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.ExpectedConditionsC;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.android.AndroidDriver;


/**
 * Provides methods to interact with a web page. All HTML element (ButtonElement, LinkElement, TextFieldElement, etc.)
 * extends from this class.
 * 
 * 
 */
public class HtmlElement extends Element implements WebElement, Locatable { 

	
	// WARNING!!!: we use the deprecated Locatable interface because it's used by Actions class
	// unit test TestPicutreElement.testClick() fails if the new interface is used
	// so wait to this old interface to be really removed

    protected static Logger logger = SeleniumRobotLogger.getLogger(HtmlElement.class);
	private static ScenarioLogger scenarioLogger = ScenarioLogger.getScenarioLogger(TestRetryAnalyzer.class);
	
    public static final Integer FIRST_VISIBLE = Integer.MAX_VALUE;
    public static final Integer OPTIMAL_SCROLLING = Integer.MAX_VALUE;
    
    private static final String JS_CLICK_TRIPLE = 
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

    private static final String JS_CLICK_DOUBLE = 
    		"if(document.createEvent){"
    		+ "   var evObj = document.createEvent('MouseEvents');"
    		+ "   evObj.initEvent('dblclick', true, false); "
    		+ "   arguments[0].dispatchEvent(evObj);"
    		+ "} else if(document.createEventObject) { "
    		+ "   arguments[0].fireEvent('ondblclick');"
    		+ "}";
    
	private ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private ThreadLocal<WebElement> element = new ThreadLocal<>();
    protected ThreadLocal<SearchContext> searchContext = new ThreadLocal<>(); // if searchContext is a WebElement, then, element and searchContext will be the same. Used to store ShadowRoot which are not WebElements

    protected HtmlElement parent = null;
    protected FrameElement frameElement = null;
    private boolean scrollToElementBeforeAction = false;
    private Integer elementIndex = -1;
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
    
    public HtmlElement(String label, By by) {
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
    public HtmlElement(String label, By by, Integer index) {
    	this(label, by, (FrameElement)null, index);
    }
    public HtmlElement(String label, By by, Integer index, Integer replayTimeout) {
    	this(label, by, null, index, replayTimeout);
    }
    

    /**
     * Find element using BY locator into a frame
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	frame  - frame element into which we must switch before searching the element
     */
    public HtmlElement(String label, By by, FrameElement frame) {
    	this(label, by, frame, null);
    }
    

    /**
     * Find the nth element using BY locator into a frame
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	frame  - frame element into which we must switch before searching the element
     * @param	index  - index of the element to find. In this case, robot will search the Nth element corresponding to
     * 					 the By parameter. Equivalent to new HtmlElement(label, by).findElements().get(N)
     * 					 If index is null, use <code>driver.findElement(By)</code> intenally
     *  				 If index is negative, search from the last one (-1)
     *  			     If index is HtmlElement.FIRST_VISIBLE, search the first visible element
     */
    public HtmlElement(String label, By by, FrameElement frame, Integer index) {
    	this(label, by, frame, null, index, null);
    }
    public HtmlElement(String label, By by, FrameElement frame, Integer index, Integer replayTimeout) {
    	this(label, by, frame, null, index, replayTimeout);
    }
    

    /**
     * Find element using BY locator into an other element.This help focusing on a page zone for searching an element
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	parent - Parent element to search before searching this element
     */
    public HtmlElement(String label, By by, HtmlElement parent) {
    	this(label, by, parent, null);
    }
    

    /**
     * Find the nth element using BY locator into an other element.This help focusing on a page zone for searching an element
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	parent - Parent element to search before searching this element
     * @param	index  - index of the element to find. In this case, robot will search the Nth element corresponding to
     * 					 the By parameter. Equivalent to new HtmlElement(label, by).findElements().get(N)
     * 					 If index is null, use <code>driver.findElement(By)</code> intenally
     *  				 If index is negative, search from the last one (-1)
     *  			     If index is HtmlElement.FIRST_VISIBLE, search the first visible element
     */
    public HtmlElement(String label, By by, HtmlElement parent, Integer index) {
    	this(label, by, null, parent, index, null);
    }
    public HtmlElement(String label, By by, HtmlElement parent, Integer index, Integer replayTimeout) {
    	this(label, by, null, parent, index, replayTimeout);
    }
    
    /**
     * @deprecated Should not be used as the element is either directly search in a frame or in a parent element which itself is in a frame, not both
     */
    @Deprecated
    public HtmlElement(String label, By by, FrameElement frame, HtmlElement parent, Integer index) {
    	this(label, by, frame, parent, index, null);	
    }
    
    /**
     * Find the nth element using BY locator into an other element or a frame.This help focusing on a page zone for searching an element
     * Frame and parent element are mutually exclusive
     *
     * @param   label  - element name for logging
     * @param   by     - By type
     * @param	frame  - frame element into which we must switch before searching the element
     * @param	parent - Parent element to search before searching this element
     * @param	index  - index of the element to find. In this case, robot will search the Nth element corresponding to
     * 					 the By parameter. Equivalent to new HtmlElement(label, by).findElements().get(N)
     * 					 If index is null, use <code>driver.findElement(By)</code> intenally
     *  				 If index is negative, search from the last one (-1)
     *  			     If index is HtmlElement.FIRST_VISIBLE, search the first visible element
     * @param	replayTimeout - how much time we must wait for the element to be present for playing with it
     */
    protected HtmlElement(String label, By by, FrameElement frame, HtmlElement parent, Integer index, Integer replayTimeout) {
		super(label);
    	this.by = by;
    	this.elementIndex = index;
    	this.parent = parent;

    	if (parent != null && frame != null) {
    		scenarioLogger.error("parent element and frame cannot be set together. If you want to search a element with parent in a frame, define a frame for this parent");
    	} else {
    		this.frameElement = frame;
    	}
    	this.replayTimeout = replayTimeout;

    }

    /**
     * Native click
     */
	@ReplayOnError(waitAfterAction = true)
    public void click() {
        findElement(true);
        
		outlineElement(getRealElementNoSearch());
		getRealElementNoSearch().click();
    }
    
    /**
     * Click with CompositeActions
     */
	@ReplayOnError(waitAfterAction = true)
    public void clickAction() {
    	findElement(true);

		outlineElement(getRealElementNoSearch());
    	try {
			new Actions(getDriver()).click(getRealElementNoSearch()).perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
        }
    }
    
    /**
     * Double Click with CompositeActions
     */
	@ReplayOnError(waitAfterAction = true)
    public void doubleClickAction() {
    	findElement(true);

		outlineElement(getRealElementNoSearch());
    	try {
			new Actions(getDriver()).doubleClick(getRealElementNoSearch()).perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
        }
    }
    
	@ReplayOnError(waitAfterAction = true)
	public void clickMouse() {

		Rectangle viewportPosition = detectViewPortPosition();

		// always scroll to element so that we can click on it with mouse
		setScrollToElementBeforeAction(true);
		findElement(true);

		outlineElement(getRealElementNoSearch());

		Rectangle elementRect = getRect();
		Point scrollPosition = ((CustomEventFiringWebDriver) getDriver()).getScrollPosition();

		CustomEventFiringWebDriver.leftClicOnDesktopAt(true,
				elementRect.x + elementRect.width / 2 + viewportPosition.x - scrollPosition.x,
				elementRect.y + elementRect.height / 2 + viewportPosition.y - scrollPosition.y,
				SeleniumTestsContextManager.getThreadContext().getRunMode(),
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());

	}

	private File getDesktopScreenshotFile() {
		ScreenshotUtil screenshotUtil = new ScreenshotUtil(); // update driver
		return screenshotUtil.capture(SnapshotTarget.MAIN_SCREEN, File.class, true);
	}

	private File getViewportScreenshotFile() {
		ScreenshotUtil screenshotUtil = new ScreenshotUtil(); // update driver
		return screenshotUtil.capture(SnapshotTarget.VIEWPORT, File.class, true);
	}

	private Rectangle detectViewPortPosition() {
		BufferedImage image;
		try {
			image = ImageProcessor.loadFromFile(getViewportScreenshotFile());

			// do not take the full width of the picture as sometimes, browsers like Edge display suggestions / popup in the right corner
			BufferedImage croppedImage = ImageProcessor.cropImage(image, 0, 0, image.getWidth() / 2, 150);
			File cropScreenshotFile = File.createTempFile("img", ".png");
			ImageIO.write(croppedImage, "png", cropScreenshotFile);

			File desktopScreenshotFile = getDesktopScreenshotFile();
			if (desktopScreenshotFile == null) {
				throw new ScreenshotException("Desktop screenshot does not exist");
			}

			ImageDetector imageDetector = new ImageDetector(desktopScreenshotFile, cropScreenshotFile, 0.2);
			imageDetector.detectExactZoneWithoutScale();
			org.openqa.selenium.Rectangle detectedRectangle = imageDetector.getDetectedRectangle();
			return new Rectangle(detectedRectangle.x, detectedRectangle.y, detectedRectangle.height,
					detectedRectangle.width);

		} catch (IOException e) {
			throw new ScreenshotException("Error getting position of viewport: " + e.getMessage());
		}
	}

    /**
     * Click element in native way by Actions.
     *
     * <p/>
	 *
     * <pre class="code">
	 * clickAt(1, 1);
     * </pre>
     *
     * @param xOffset	X offset to click to
	 * @param yOffset	Y offset to click to
     */
	@ReplayOnError(waitAfterAction = true)
    public void clickAt(int xOffset, int yOffset) {
    	findElement();
		((CustomEventFiringWebDriver) getDriver()).scrollToElement(getRealElementNoSearch(), yOffset);

		outlineElement(getRealElementNoSearch());
        try {
			new Actions(getDriver()).moveToElement(getRealElementNoSearch(), xOffset, yOffset).click().perform();
        } catch (InvalidElementStateException e) {
            logger.error(e);
			getRealElementNoSearch().click();
        }
    }

    /**
     * Click with javascript
     */
	@ReplayOnError(waitAfterAction = true)
    public void simulateClick() {
    	if (SeleniumTestsContextManager.isWebTest()) {
    		((CustomEventFiringWebDriver)updateDriver()).updateWindowsHandles();
    	}
    	
        findElement(true);
		outlineElement(getRealElementNoSearch());
        DriverConfig driverConfig = WebUIDriver.getWebUIDriver(false).getConfig();

		// on modern browsers, dispatchEvent is used, and so, action is synchronous
		// on other browsers, fireEvent does not seem to be synchronous, so we wait a bit
		boolean waitAfterEvent = false;

        String mouseOverScript;
        if ((driverConfig.getBrowserType() == BrowserType.FIREFOX && FirefoxDriverFactory.isMarionetteMode())
            	|| driverConfig.getBrowserType() == BrowserType.EDGE
        		|| driverConfig.getBrowserType() == BrowserType.CHROME) {
        		mouseOverScript = "var event = new MouseEvent('mouseover', {view: window, bubbles: true, cancelable: true}) ; arguments[0].dispatchEvent(event);";
            } else {
				waitAfterEvent = true;
            	mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            }
        
		executeScript(mouseOverScript, getRealElementNoSearch());
		if (waitAfterEvent) {
			WaitHelper.waitForSeconds(2);
		}
        
        String clickScript = "";
        if ((driverConfig.getBrowserType() == BrowserType.FIREFOX && FirefoxDriverFactory.isMarionetteMode())
        		|| driverConfig.getBrowserType() == BrowserType.EDGE
            	|| driverConfig.getBrowserType() == BrowserType.CHROME) {
        	clickScript = "var event = new MouseEvent('click', {view: window, bubbles: true, cancelable: true}) ;"
            			+ "arguments[0].dispatchEvent(event);";
        } else {
        	clickScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('click', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onclick');}";
        }

		executeScript(clickScript, getRealElementNoSearch());
		if (waitAfterEvent) {
			WaitHelper.waitForSeconds(2);
		}
    }
    
	@ReplayOnError(waitAfterAction = true)
    public void simulateDoubleClick() {
        findElement(true);
		outlineElement(getRealElementNoSearch());
        
        DriverConfig driverConfig = WebUIDriver.getWebUIDriver(false).getConfig();
        
        String doubleClickScript;
        if ((driverConfig.getBrowserType() == BrowserType.FIREFOX && FirefoxDriverFactory.isMarionetteMode())
        		|| driverConfig.getBrowserType() == BrowserType.EDGE
            	|| driverConfig.getBrowserType() == BrowserType.CHROME) {
        		doubleClickScript = "var event = new MouseEvent('dblclick', {view: window, bubbles: true, cancelable: true}) ;"
                			+ "arguments[0].dispatchEvent(event);";
            } else {
            	doubleClickScript = JS_CLICK_DOUBLE;
            }
        
		executeScript(doubleClickScript, getRealElementNoSearch());

    }
    
	@ReplayOnError(waitAfterAction = true)
    public void simulateSendKeys(CharSequence... keysToSend) {
    	findElement(true);
    		
    	// click on element before sending keys through keyboard
		getRealElementNoSearch().click();
		executeScript("arguments[0].focus();", getRealElementNoSearch());

        if (keysToSend.length == 0) {
        	executeScript("arguments[0].value='';", getRealElementNoSearch());
        } else {
        	executeScript(String.format("arguments[0].value='%s';", keysToSend[0].toString()), getRealElementNoSearch());
        }

    }

	@ReplayOnError(waitAfterAction = true)
    public void simulateMoveToElement(int x, int y) {
        findElement(true);
        executeScript(
            "function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|errorLogger|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; " +
            "simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]);",
            getRealElementNoSearch(),
            x, 
            y);

    }
    
    /**
     * Find elements inside this element
     * @param by
     * @return 	List of selenium WebElement 
     */
    @Override
    @ReplayOnError
    public List<WebElement> findElements(By by) {
    	
    	// find the root element
    	findElement(false, false);
		List<WebElement> elements = getRealElementNoSearch().findElements(by);
    	
    	// throw exception so that behavior is the same as with 'findElements()' call which retries search
    	if (elements.isEmpty()) {
    		throw new NoSuchElementException("No elements found for " + by.toString());
    	} else {
    		return elements;
    	}
    }

    /**
     * Find elements inside this element
	 * 
     * @param childBy
     * @return	List of HtmlElement's based on real WebElement
     */
    @ReplayOnError
    public List<WebElement> findHtmlElements(By childBy) {
    	
    	// find the root element
    	findElement(false, false);
    	List<WebElement> htmlElements = new ArrayList<>();
		List<WebElement> elements = getRealElementNoSearch().findElements(childBy);
    	
    	// throw exception so that behavior is the same as with 'findElements()' call which retries search
    	if (elements.isEmpty()) {
    		throw new NoSuchElementException("No elements found for " + childBy.toString());
    	}
    	
    	for (int i = 0; i < elements.size(); i++) {
    		// frame set to null as we expect the frames are searched in the parent element
    		htmlElements.add(new HtmlElement("", childBy, this, i));
    	}
    	return htmlElements;
    }
    
    /**
     * Find an element inside an other one
	 * 
     * @param by
     * @return
     */
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
	 * Change CSS attribute of the element by setting if via javascript:
	 * arguments[0].style.<attribute>=<value>
	 * 
     * @param cssProperty
     * @param cssPropertyValue
     */
    public void changeCssAttribute(String cssProperty, String cssPropertyValue) {
    	findElement(false, false);
    	
		changeCssAttribute(getRealElementNoSearch(), cssProperty, cssPropertyValue);
	}
    
    /**
     * Execute arbitrary script on this element. Renaming of getEval
	 * 
	 * @param javascript the script to execute. It MUST contain 'arguments[0]' and may
	 *               return something
     * @return 		arbitrary value. You must cast it
     */
    @ReplayOnError
    public Object executeScript(String javascript, Object... args) {
    	findElement(false, false);
    	
		return executeScript(javascript, getRealElementNoSearch(), args);
    }
    

    /**
     * Execute arbitrary script on the provided element
	 * 
     * @param element	the WebElement on which we call the script
	 * @param javascript  the script to execute. It MUST contain 'arguments[0]' and may
	 *                return something
	 * @param args    optional arguments to pass to the script. They should ba
	 *                accessed using 'arguments[1]' ...
     * @return 		arbitrary value. You must cast it
     */
    protected Object executeScript(String javascript, WebElement element, Object... args) {

    	if (element == null) {
    		throw new ScenarioException("element should have been previously searched"); 
    	}
    	
    	if (element instanceof HtmlElement) {
    		throw new ScenarioException("Only real elements should be provided, not HtmlElement"); 
    	}
    	
    	if (!javascript.contains("arguments[0]")) {
    		throw new ScenarioException("JS script MUST contain 'arguments[0]' as reference");
    	}
		return ((JavascriptExecutor) getDriver()).executeScript(javascript, element, args);
    }
    

    /**
     * Finds the element using By type. Implicit Waits is built in createWebDriver() in WebUIDriver to handle dynamic
     * element problem. This method is invoked before all the basic operations like click, sendKeys, getText, etc. Use
     * waitForPresent to use Explicit Waits to deal with special element which needs long time to present.
     * @param waitForVisibility		wait for element to be visible
	 * @param makeVisible       whether we try to make the element visible. Should
	 *                          be true except when trying to know if element is
	 *                          displayed
     */
    public void findElement(boolean waitForVisibility, boolean makeVisible) {

    	ElementInfo elementInfo = null;
    	
    	// search element information. Do not stop if something goes wrong here
    	if (SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch() != ElementInfo.Mode.FALSE) {
    		try {
    			elementInfo = ElementInfo.getInstance(this);
    		} catch (Exception e) {
    			logger.info("Error getting element info");
    		}
    	}
    	
    	// if a parent is defined, search for it before getting the sub element
		setDriver(updateDriver());
        if (parent != null) {
        	parent.findElement(false, false);
        	
			// issue #166: add a dot in front of xpath expression if we search the element
			// inside a parent
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
        	
        	searchContext.set(findSeleniumElement(parent.searchContext.get(), elementInfo));
        	

        } else {
        	searchContext.set(findSeleniumElement(getDriver(), elementInfo));
        }
        
        try {
        	setElement((WebElement)searchContext.get());
        	
        	if (makeVisible) { 
            	makeWebElementVisible(getRealElementNoSearch());
            	
            	if (scrollToElementBeforeAction) {
            		((CustomEventFiringWebDriver)getDriver()).scrollToElement(getRealElementNoSearch(), OPTIMAL_SCROLLING);
            	}
            }
            
		// wait for element to be really visible. should be done only for actions on
		// element
            if (waitForVisibility && makeVisible) {
            	try {
            		new WebDriverWait(getDriver(), Duration.ofSeconds(1)).until(ExpectedConditions.visibilityOf(getRealElementNoSearch()));
            	} catch (TimeoutException e) {
            		logger.error(String.format("Element %s has never been made visible", toString()));
            	}
            }
            
            // If we are here, element has been found, update elementInformation
            if (elementInfo != null) {
            	try {
    	        	elementInfo.updateInfo(this);
    	        	elementInfo.exportToJsonFile(false, this);
            	} catch (Exception e) {
            		logger.warn("Error storing element information: " + e.getMessage());
            	}
            }
        	
        } catch (ClassCastException e) {
        	// in case it's a ShadowRoot (not a WebElement, do not try to make it visible
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
    private SearchContext findSeleniumElement(SearchContext context, ElementInfo elementInfo) {
    	
    	enterFrame();
    	
    	SearchContext seleniumElement;
    	try {
    		replaceSelector();
    		
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
     *
     */
    public void replaceSelector() {

		boolean isWebTest = ((CustomEventFiringWebDriver)getDriver()).isWebTest();
		String platform = SeleniumTestsContextManager.getThreadContext().getPlatform();
		boolean isAppTest = SeleniumTestsContextManager.isMobileAppTest();
		String packageName = null;
		try {
			WebDriver drv = ((CustomEventFiringWebDriver)getDriver()).getOriginalDriver();
			packageName = ((AndroidDriver)drv).getCurrentPackage();
		} catch (Exception e) {}

    	new ShadowDomRootUpdater().update(this);
    	new MobileWebViewUpdater(isWebTest, isAppTest).update(this);
		new MobileAndroidAppUpdater(platform, packageName, isWebTest, isAppTest).update(this);

    }
        
    /**
     * returns an element depending on configured index
	 * 
     * @param allElements
     */
    protected <T extends SearchContext> T getElementByIndex(List<T> allElements) {
    	if (elementIndex != null && elementIndex.equals(FIRST_VISIBLE)) {
			for (T el: allElements) {
				if (el instanceof WebElement &&  ((WebElement)el).isDisplayed()) {
					return (T)el;
				}
			}
			throw new NoSuchElementException("no visible element has been found for " + by.toString());
    	} else if (elementIndex != null && elementIndex < 0) {
    		return allElements.get(allElements.size() + elementIndex);
    	} else {
    		if (elementIndex == null) {
    			elementIndex = 0;
    		}
    		try {
    			return (T)allElements.get(elementIndex);
    		} catch (IndexOutOfBoundsException e) {
    			throw new NoSuchElementException(String.format("No element found for locator %s with index %d", by.toString(), elementIndex));
    		}
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
			WebElement frameWebElement;
			
			SearchContext searchContext;
			if (frameEl.parent != null) {
				frameEl.parent.findElement(false, false);
				searchContext = frameEl.parent.getRealElementNoSearch();
			} else {
				searchContext = getDriver();
			}

			try {
				List<WebElement> frameElements = searchContext.findElements(frameEl.getBy());
				frameWebElement = frameEl.getElementByIndex(frameElements);
			} catch (NoSuchElementException e) {
				throw new NoSuchFrameException(e.getMessage());
			}

			((CustomEventFiringWebDriver) getDriver()).scrollToElement(frameWebElement, -20);
			getDriver().switchTo().frame(frameWebElement);
		}
    }
    
    protected void changeCssAttribute(WebElement element, String cssProperty, String cssPropertyValue) {
		String javascript = "arguments[0].style." + cssProperty + "='" + cssPropertyValue + "';";
		executeScript(javascript, element); 
	}
    
    /**
     * outlines the element before acting on it
     * Element must have been searched before
     */
	protected void outlineElement(WebElement localElement) {
		if (localElement == null || !SeleniumTestsContextManager.isWebTest()
				|| !SeleniumTestsContextManager.getThreadContext().getDebug().contains(DebugMode.GUI)) {
    		return;
    	}
    	
		changeCssAttribute(localElement, "outline", "2px solid red");
    	WaitHelper.waitForMilliSeconds(250);
		changeCssAttribute(localElement, "outline", "");
    }
    
    /**
	 * Make element visible. Sometimes useful when real elements are backed by an image element
	 */
	protected void makeWebElementVisible(WebElement localElement) {
		if (SeleniumTestsContextManager.isWebTest()) {
			if (localElement.isDisplayed()) {
				return;
			}
			try {
				
				if (localElement.getLocation().x < 0) {
					Long viewportHeight = (Long) ((JavascriptExecutor) getDriver())
							.executeScript("return document.documentElement.clientHeight");
					Integer heightPosition = localElement.getLocation().y > viewportHeight
							? localElement.getLocation().y - viewportHeight.intValue()
							: localElement.getLocation().y;
					changeCssAttribute(localElement, "left", "20px");
					changeCssAttribute(localElement, "top", heightPosition + "px");
					changeCssAttribute(localElement, "position", "inherit");
				}
				if (Boolean.TRUE.equals(executeScript("return getComputedStyle(arguments[0]).display === 'none'", localElement))) {
					changeCssAttribute(localElement, "display", "block");
				}
				if (Boolean.TRUE.equals(executeScript("return getComputedStyle(arguments[0]).visibility !== 'visible'", localElement))) {
					changeCssAttribute(localElement, "visibility", "visible");
				}
				if (Boolean.TRUE.equals(executeScript("return getComputedStyle(arguments[0]).opacity === '0'", localElement))) {
					changeCssAttribute(localElement, "opacity", "1");
				}

				changeCssAttribute(localElement, "zIndex", "100000");
			} catch (Exception e) {
				return;
			}
				
			// wait for element to be displayed
			try {
				new WebDriverWait(getDriver(), Duration.ofSeconds(1)).until(ExpectedConditions.visibilityOf(localElement));
			} catch (ElementNotInteractableException e) {
				scenarioLogger.info(String.format("element %s not visible", localElement));
			} catch (Exception e) {
				logger.warn("Could not make element visible", e);
			}
			
		}
	}
	
	/**
	 * Scroll to element
	 * 
	 * @param yOffset
	 */	
	public void scrollToElement(int yOffset) {
		findElement();
		((CustomEventFiringWebDriver) getDriver()).scrollToElement(getRealElementNoSearch(), yOffset);
	}

    /**
     * Get all elements in the current page with same locator.
     *
     * @return
     */
	@ReplayOnError
    public List<WebElement> findElements() {
		
		// call findElement to enter any specified frame and search for parent elements
		findElement(false, false);

        // issue #167: if we have a parent, search elements inside it
        if (parent != null) {
			return parent.getRealElementNoSearch().findElements(by);
        } else {
			return getDriver().findElements(by);
        }
    }

    /**
	 * Gets an attribute (using standard key-value pair) from the underlying
	 * attribute.
     *
     * @param   name
     *
     * @return
     */
	@ReplayOnError
    public String getAttribute(String name) {
        findElement(false, false);

		return getRealElementNoSearch().getAttribute(name);
    }
	
	@Override
	@ReplayOnError
	public String getDomAttribute(String name) {
		findElement(false, false);
		
		return getRealElementNoSearch().getDomAttribute(name);
	}
	
	@Override
	@ReplayOnError
	public String getDomProperty(String name) {
		findElement(false, false);
		
		return getRealElementNoSearch().getDomProperty(name);
	}
	
	@Override
	@ReplayOnError
	public String getAriaRole() {
		findElement(false, false);
		
		return getRealElementNoSearch().getAriaRole();
	}
	
	@Override
	@ReplayOnError
	public String getAccessibleName() {
		findElement(false, false);
		
		return getRealElementNoSearch().getAccessibleName();
	}
	
	@Override
	@ReplayOnError
	public SearchContext getShadowRoot() {
		findElement(false, false);
		
		return getRealElementNoSearch().getShadowRoot();
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
    public String getCssValue(String propertyName) {
        findElement(false, false);

		return getRealElementNoSearch().getCssValue(propertyName);
    }

    /**
     * Get underlying WebDriver.
     */
    public WebDriver updateDriver() {
		setDriver(WebUIDriver.getWebDriver(false));
		if (getDriver() == null) {
    		throw new ScenarioException("Driver has not already been created");
    	}
		return getDriver();
    }
    
    public WebDriver getDriver() {
		return driver.get();
    }

    public void setDriver(WebDriver driver) {
		this.driver.set(driver);
	}

	/**
     * Returns the underlying WebDriver WebElement.
	 * Search is always done
     *
     * @return
     */
    @ReplayOnError
    public WebElement getElement() {
    	findElement(true);
		return getRealElementNoSearch();
    }

    /**
     * Executes the given JavaScript against the underlying WebElement.
     *
     * @param   script
     * @deprecated ...
     *
     * @return
     */
    @ReplayOnError
    @Deprecated
    public String getEval(String script) {
        findElement(false, false);
        
		return (String) ((JavascriptExecutor) getDriver()).executeScript(script, getRealElementNoSearch());
    }

    /**
     * Returns the 'height' property of the underlying WebElement's Dimension.
     *
     * @return
     */
    @ReplayOnError
    public int getHeight() {
        findElement(false, false);

		return getRealElementNoSearch().getSize().getHeight();
    }


    /**
     * Gets the Point location of the underlying WebElement.
     *
     * @return
     */
    @Override
    @ReplayOnError
    public Point getLocation() {
        findElement(false, false);

		return getRealElementNoSearch().getLocation();
    }
    

	@Override
	@ReplayOnError
	public Rectangle getRect() {
		findElement(false, false);
		return new Rectangle(getRealElementNoSearch().getLocation(), getRealElementNoSearch().getSize());
	}

    /**
     * Returns the Dimension property of the underlying WebElement.
     *
     * @return
     */
    @Override
    @ReplayOnError
    public Dimension getSize() {
        findElement(false, false);

		return getRealElementNoSearch().getSize();
    }

    /**
     * Returns the HTML Tag for the underlying WebElement (div, a, input, etc).
     *
     * @return
     */
    @Override
    @ReplayOnError
    public String getTagName() {
        findElement(false, false);

		return getRealElementNoSearch().getTagName();
    }

    /**
     * Returns the text body of the underlying WebElement.
     *
     * @return
     */
    @Override
    @ReplayOnError
    public String getText() {
        findElement(false, false);

		return getRealElementNoSearch().getText();
    }

    /**
     * Returns the 'value' attribute of the underlying WebElement or '' if no value present
     *
     * @return
     */
    @ReplayOnError
    public String getValue() {
        findElement(false, false);
        String value = getRealElementNoSearch().getDomProperty("value");
        
		return value == null ? "": value;
    }

    /**
     * Returns the 'width' property of the underlying WebElement's Dimension.
     *
     * @return
     */
    @ReplayOnError
    public int getWidth() {
        findElement(false, false);

		return getRealElementNoSearch().getSize().getWidth();
    }

    /**
	 * Indicates whether or not the web element is currently displayed in the
	 * browser.
     *
     * @return
     */
    @Override
    public boolean isDisplayed() {
        try {
            return isDisplayedRetry();
        } catch (WebDriverException e) {
        	scenarioLogger.warn("Element not displayed / not found. For searching if element is present and/or displayed, use isElementPresentAndDisplayed() instead");
            return false;
        } catch (Exception e) {
        	return false;

        }
    }
    
    @ReplayOnError
    public boolean isDisplayedRetry() {
    	findElement(false, false);
		outlineElement(getRealElementNoSearch());
		return getRealElementNoSearch().isDisplayed();
    }

    /**
     * Searches for the element using the BY locator, and indicates whether or not it exists in the page. This can be
     * used to look for hidden objects, whereas isDisplayed() only looks for things that are visible to the user
     * 
	 * Note that when requested element has "HtmlElement.FIRST_VISIBLE" index,
	 * isElementPresent acts as isElementPresentAndDisplayed
     * 
     * @param timeout 	timeout in seconds
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
     * Search for an element to be present AND displayed
	 * If element is present and not displayed when method is called, it will wait for it to appear
	 * 
     * @param timeout	timeout in seconds
     * @return false if the element is not present or present but not displayed
     */
    public boolean isElementPresentAndDisplayed(int timeout) {        
        try {
			long start = System.currentTimeMillis();
    		waitForPresent(timeout);
			Long duration = (System.currentTimeMillis() - start) / 1000;

			waitFor(Math.max(1, timeout - duration.intValue()), ExpectedConditions.visibilityOf(this));
    		return true;
    	} catch (TimeoutException e) {
			scenarioLogger.warn(String.format("Element %s is not present", getBy()));
    		return false;
    	}
    }
    public boolean isElementPresentAndDisplayed() { 
    	return isElementPresentAndDisplayed(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }
    

    /**
     * Indicates whether or not the element is enabled in the browser.
     *
     * @return
     */
    @Override
    @ReplayOnError
    public boolean isEnabled() {
        findElement(false, false);

		return getRealElementNoSearch().isEnabled();
    }

    /**
     * Indicates whether or not the element is selected in the browser.
     *
     * @return
     */
    @Override
    @ReplayOnError
    public boolean isSelected() {
        findElement(false, false);

		return getRealElementNoSearch().isSelected();
    }

    /**
	 * Whether or not the indicated text is contained in the element's getText()
	 * attribute.
     *
     * @param   pattern
     *
     * @return
     */
    public boolean isTextPresent(String pattern) {
        String text = getText();
        return text != null && (text.contains(pattern) || text.matches(pattern));
    }
    
    /**
	 * @deprecated (due to selenium Mouse deprecation) Forces a mouseDown event on
	 *             the WebElement.
     */
    @Deprecated
    public void mouseDown() {
        logger.error("use 'new Actions(driver).moveToElement(element).click().perform();' instead");
    }

    /**
	 * @deprecated (due to selenium Mouse deprecation) Forces a mouseOver event on
	 *             the WebElement.
     */
    @Deprecated
    public void mouseOver() {
    	logger.error("use 'new Actions(driver).moveToElement(element).click().perform();' instead");
    }

    /**
	 * Forces a mouseOver event on the WebElement using simulate by JavaScript way
	 * for some dynamic menu.
     */
	@ReplayOnError(waitAfterAction = true)
    public void simulateMouseOver() {
        findElement(true); // search element first because we want it to be visible

		String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
		executeScript(mouseOverScript, getRealElementNoSearch());
    }
    
    @Override
    public void sendKeys(CharSequence... keysToSend) {
    	sendKeys(true, keysToSend);
    }
    
    public void sendKeys(boolean blurAfter, CharSequence... keysToSend) {
    	// Appium seems to clear field before writing
		TestType testType = SeleniumTestsContextManager.getThreadContext().getTestType();
		boolean clearField = !(testType.family() == TestType.APP && testType.isMobile());
    	sendKeys(clearField, blurAfter, keysToSend);

    }
    
    protected void blur() {
		if (SeleniumTestsContextManager.isWebTest() && "input".equalsIgnoreCase(getRealElementNoSearch().getTagName())) {
    		try {
				executeScript("arguments[0].blur();", getRealElementNoSearch());
    		} catch (Exception e) {	
    			logger.error("Error doing 'blur'", e);
    		}
    	}
    }
    
    /**
	 * Wait few time between keys typing
	 * @param keysToSend
	 * @throws InterruptedException
	 */
	@ReplayOnError(waitAfterAction = true)
	public void sendKeysAction(long duration, CharSequence... keysToSend) {
		findElement(true);
		Actions send = new Actions(getDriver()).moveToElement(getRealElementNoSearch()).click();
		for (CharSequence word : keysToSend) {
			for (int i = 0; i < word.length(); i++) {
				char extractLetter = word.charAt(i);
				String letter = String.valueOf(extractLetter);

				send = send.sendKeys(letter).pause(duration);
			}

		}
		send.perform();
	}

	/**
	 * Send keys through composite actions /!\ does not clear text before and no
	 * blur after
     * 
     * @param keysToSend
     */
	@ReplayOnError(waitAfterAction = true)
    public void sendKeysAction(CharSequence... keysToSend) {
    	findElement(true);
		new Actions(getDriver()).sendKeys(getRealElementNoSearch(), keysToSend).build().perform();
	}

	/**
	 * Send keys using real keyboard
	 * 
	 * @param keysToSend
	 */
	public void sendKeysKeyboard(CharSequence... keysToSend) {
		clickMouse();
		for (CharSequence keys : keysToSend) {
			CustomEventFiringWebDriver.writeToDesktop(keys.toString(),
					SeleniumTestsContextManager.getThreadContext().getRunMode(),
					SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
		}

    }

    /**
     * Sends the indicated CharSequence to the WebElement.
     *
     * @param 	clear		if true, clear field before writing
     * @param	blurAfter	if true, do blur() after sendKeys has been done
     * @param   keysToSend	write this text
     */
	@ReplayOnError(waitAfterAction = true)
    public void sendKeys(boolean clear, boolean blurAfter, CharSequence... keysToSend) {
        findElement(true);
        
        if (clear) {
			getRealElementNoSearch().clear();
        } 
		getRealElementNoSearch().sendKeys(keysToSend);
        
        if (blurAfter) {
        	blur();
        }
    }
    
    @Override
    @ReplayOnError
    public void clear() {
    	findElement(true);
		getRealElementNoSearch().clear();
    }

    /**
     * Method, which should never be used.
     */
    protected void sleep(int waitTime) throws InterruptedException {
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
		findElement(false, false);
		String attributeValue;
		if ("text".equals(attributeName)) {
			attributeValue = getRealElementNoSearch().getText();
		} else {
			attributeValue = getRealElementNoSearch().getAttribute(attributeName);
		}

		Matcher matcher = pattern.matcher(attributeValue);
		if (matcher.matches() && matcher.groupCount() > 0) {
			return matcher.group(1);
		}

		return "";
	}
    
    /**
	 * Returns URL present in one of the element attributes
	 * 
	 * @param attributeName attribute name in which we should look at. Give "text"
	 *                      to search in value
	 * @return 					the found link
	 */
	public String findLink(String attributeName) {
		
		// link <a href="#" id="linkPopup2" onclick="window.open('http://www.infotel.com/', '_blank');">
		String link = findPattern(Pattern.compile(".*(http://.+?)'\"?.*"), attributeName);
		if (!"".equals(link)) {
			return link;
		}
		
		// link with simple quotes  <a href="#" id="linkPopup" onclick='window.open("http://www.infotel.com/", "_blank");'>
		link = findPattern(Pattern.compile(".*(http://.+?)\"'?.*"), attributeName);
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
	private WebElement getUnderlyingElement(WebElement localElement) {
    	try {
    		return (RemoteWebElement)((Decorated<WebElement>)localElement).getOriginal();
    	} catch (ClassCastException e) {
    		return localElement;
    	}
    }
    
    @ReplayOnError
    public Point getCenter() {
		Rectangle rectangle = getRealElement().getRect();
		return new Point(rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    }

    /**
     * Wait element to present using Explicit Waits with default EXPLICIT_WAIT_TIME_OUT = 15 seconds.
     */
    public void waitForPresent() {
        waitForPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }
    
    /**
     * Wait element not to be present using Explicit Waits with default EXPLICIT_WAIT_TIME_OUT = 15 seconds.
     */
    public void waitForNotPresent() {
    	waitForNotPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }
    
    /**
     * Wait element to be visible using Explicit Waits with default EXPLICIT_WAIT_TIME_OUT = 15 seconds.
     */
    public void waitForVisibility() {
    	waitForVisibility(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }
    
    /**
     * Wait element not to be visible using Explicit Waits with default EXPLICIT_WAIT_TIME_OUT = 15 seconds.
     */
    public void waitForInvisibility() {
    	waitForInvisibility(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout());
    }
    
	public void setImplicitWaitTimeout(final double timeout) {
        try {
        	getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds((int)timeout));
        } catch (Exception ex) {
        	logger.error(ex);
        }
    }
	
	public void setImplicitWaitTimeout(int timeout, TemporalUnit unit) {
		try {
			getDriver().manage().timeouts().implicitlyWait(Duration.of((long)timeout, unit));
		} catch (Exception ex) {
			logger.error(ex);
		}
	}
    
    /**
	 * Wait element to present using Explicit Waits with timeout in seconds. This
	 * method is used for special element which needs long time to present. This
	 * method is replayed because it may fail if frame is not present at start. The
	 * replay is not done if TimeOutException raises (see ReplayAction class)
	 * 
	 * @param timeout timeout in seconds. Set a minimal value of 1 sec to avoid not
	 *                searching for element
     */
    @ReplayOnError
    public void waitForPresent(int timeout) {
    	
    	// refresh driver
		setDriver(updateDriver());
    	
    	try {
    		setImplicitWaitTimeout(510, ChronoUnit.MILLIS);
    	
	    	Clock clock = Clock.systemUTC();
	    	Instant end = clock.instant().plusSeconds(Math.max(1, timeout));
	    	
	    	while (end.isAfter(clock.instant())) {
	    		try {
		    		WebElement elt = new WebDriverWait(getDriver(), Duration.ofMillis(0)).ignoring(ConfigurationException.class, ScenarioException.class).until(ExpectedConditionsC.presenceOfElementLocated(this));
		            outlineElement(elt);
		    		return;
	    		} catch (TimeoutException e) {
	    			// nothing to do
	    		} 
	    	}
	    	throw new TimeoutException("Element is not present", new NoSuchElementException(toString()));
    	} finally {
    		setImplicitWaitTimeout(SeleniumTestsContextManager.getThreadContext().getImplicitWaitTimeout());
    	}
    }

    public void waitFor(int timeout, ExpectedCondition<?> condition) {
    	
		// refresh driver
		setDriver(updateDriver());
		
    	try {
    		setImplicitWaitTimeout(510, ChronoUnit.MILLIS);
	    	Clock clock = Clock.systemUTC();
	    	Instant end = clock.instant().plusSeconds(Math.max(1, timeout));
	    	
	    	while (end.isAfter(clock.instant())) {
	    		try {
	    			new WebDriverWait(getDriver(), Duration.ofSeconds(timeout))
							.ignoring(ConfigurationException.class, ScenarioException.class)
							.until(condition);
		    		return;
	    		} catch (TimeoutException e) {
	    			// nothing to do
	    		}
	    	}
	    	throw new TimeoutException("Element is not present", new NoSuchElementException(toString()));
	    
    	} finally {
    		setImplicitWaitTimeout(SeleniumTestsContextManager.getThreadContext().getImplicitWaitTimeout());
    	}
    }
    
    public void waitForNotPresent(int timeout) {
    	waitFor(timeout, ExpectedConditionsC.absenceOfElementLocated(this));
    }
    
    public void waitForVisibility(int timeout) {
    	waitFor(timeout, ExpectedConditions.visibilityOf(this));	
    }
    
    public void waitForInvisibility(int timeout) {
    	waitFor(timeout, ExpectedConditions.invisibilityOf(this));
    }

	public FrameElement getFrameElement() {
		return frameElement;
	}

	public void setFrameElement(FrameElement frameElement) {
		this.frameElement = frameElement;
	}
	
	/**
	 * Sometimes, the frame defined for the element may change
	 * 
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
		return getRealElementNoSearch().getScreenshotAs(target);
	}

	@Override
	@ReplayOnError(waitAfterAction = true)
	public void submit() {
		findElement(true);
		getRealElementNoSearch().submit();
	}

	@Override
	@ReplayOnError
	public Coordinates getCoordinates() {
		findElement(false, false);
		return ((Locatable) getRealElementNoSearch()).getCoordinates();
	}
	
	public Map<String, Object> toJson() {
		findElement();
		return ((RemoteWebElement) getUnderlyingElement(getRealElementNoSearch())).toJson();
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
	 * 
	 * @param element
	 */
	public void setElement(WebElement element) {
		this.element.set(element);
	}

	public Integer getElementIndex() {
		return elementIndex;
	}
	
	/**
	 * Directly returns the real element even if it has not been searched
	 * @return
	 */
	protected WebElement getRealElementNoSearch() {
		return element.get();
	}

	/**
	 * Returns the real web element. issue #313: Search the element if it has not
	 * been searched before
	 * 
	 * @return
	 */
	public WebElement getRealElement() {
		if (getRealElementNoSearch() == null) {
			findElement();
		}
		return getRealElementNoSearch();
	}

	/**
	 * Change the search index of the element (its order in element list)
	 * 
	 * @param elementIndex
	 */
	public void setElementIndex(Integer elementIndex) {
		this.elementIndex = elementIndex;
	}

	public boolean isScrollToElementBeforeAction() {
		return scrollToElementBeforeAction;
	}

	public void setScrollToElementBeforeAction(boolean scrollToElementBeforeAction) {
		this.scrollToElementBeforeAction = scrollToElementBeforeAction;
	}

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) {
			return name;
		} else {
			return getBy().toString();
		}
	}
}
