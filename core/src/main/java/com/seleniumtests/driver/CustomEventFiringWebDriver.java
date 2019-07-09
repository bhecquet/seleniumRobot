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
package com.seleniumtests.driver;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UselessFileDetector;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.neotys.selenium.proxies.NLWebDriver;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.StatisticsStorage;
import com.seleniumtests.core.StatisticsStorage.DriverUsage;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.screenshots.VideoRecorder;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

import net.lightbody.bmp.BrowserMobProxy;

/**
 * This class acts as a proxy for everything related to selenium driver actions (mostly a bypass) or with the machine holding
 * the browser
 * - send keys via keyboard
 * - move mouse
 * - upload file to browser
 * - capture video
 * 
 * When action do not need a real driver, static methods are provided
 * It also handles the grid mode, masking it to requester.
 */
public class CustomEventFiringWebDriver extends EventFiringWebDriver implements HasCapabilities {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(CustomEventFiringWebDriver.class);
    private FileDetector fileDetector = new UselessFileDetector();
    private static final int MAX_DIMENSION = 100000;
    private Set<String> currentHandles;
    private final List<Long> driverPids;
	private final WebDriver driver;
	private final NLWebDriver neoloadDriver;
	private final boolean isWebTest;
	private final DriverMode driverMode;
	private final BrowserInfo browserInfo;
	private final BrowserMobProxy mobProxy;
	private final SeleniumGridConnector gridConnector;
	private MutableCapabilities internalCapabilities = new MutableCapabilities();
    
    private static final String JS_GET_VIEWPORT_SIZE = String.format(
    		"var pixelRatio;" +
    	    "try{pixelRatio = devicePixelRatio} catch(err){pixelRatio=1}" +
            "var height = %d;"
                + "var width = %d;"
                + " if (window.innerHeight) {"
                + "		height = Math.min(window.innerHeight, height);"
                + " }"
                + " if (document.documentElement && document.documentElement.clientHeight) {"
                + "		height = Math.min(document.documentElement.clientHeight, height);"
                + " }"
                + "	var b = document.getElementsByTagName('html')[0]; "
                + "	if (b.clientHeight) {"
                + "		height = Math.min(b.clientHeight, height);"
                + "	}"
                + " if (window.innerWidth) {"
                + "		width = Math.min(window.innerWidth, width);"
                + " } "
                + " if (document.documentElement && document.documentElement.clientWidth) {"
                + "		width = Math.min(document.documentElement.clientWidth, width);"
                + " } "
                + "	var b = document.getElementsByTagName('html')[0]; "
                + "	if (b.clientWidth) {"
                + "		width = Math.min(b.clientWidth, width);"
                + "	}"
                + "	return [width * pixelRatio, height * pixelRatio];", MAX_DIMENSION, MAX_DIMENSION);

    private static final String JS_GET_CURRENT_SCROLL_POSITION =
            "var doc = document.documentElement; "
            + "var x = window.scrollX || ((window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0));"
            + "var y = window.scrollY || ((window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0));"
            + "return [x, y];";


    // IMPORTANT: Notice there's a major difference between scrollWidth
    // and scrollHeight. While scrollWidth is the maximum between an
    // element's width and its content width, scrollHeight might be
    // smaller (!) than the clientHeight, which is why we take the
    // maximum between them.
    private static final String JS_GET_CONTENT_ENTIRE_SIZE =
            "var pixelRatio;" +
    		"try{pixelRatio = devicePixelRatio} catch(err){pixelRatio=1}" +
    		"var scrollWidth = document.documentElement.scrollWidth; " +
            "var bodyScrollWidth = document.body.scrollWidth; " +
            "var totalWidth = Math.max(scrollWidth, bodyScrollWidth); " +
            "var clientHeight = document.documentElement.clientHeight; " +
            "var bodyClientHeight = document.body.clientHeight; " +
            "var scrollHeight = document.documentElement.scrollHeight; " +
            "var bodyScrollHeight = document.body.scrollHeight; " +
            "var maxDocElementHeight = Math.max(clientHeight, scrollHeight); " +
            "var maxBodyHeight = Math.max(bodyClientHeight, bodyScrollHeight); " +
            "var totalHeight = Math.max(maxDocElementHeight, maxBodyHeight); " +
            "return [totalWidth * pixelRatio, totalHeight * pixelRatio];";
    
    // according to https://www.w3schools.com/jsref/prop_element_scrolltop.asp
    // root scrollable (overflow) element is 'document.body' for safari and 'document.documentElement' for other browsers
    private static final String JS_SCROLL_PARENT = "function getScrollParent(element, includeHidden, browserName) {" + 
    		"    var rootElement = browserName === \"safari\" ? document.body: document.documentElement;" +
    		"    var style = getComputedStyle(element);" + 
    		"    var excludeStaticParent = style.position === \"absolute\";" + 
    		"    var overflowRegex = includeHidden ? /(auto|scroll|hidden)/ : /(auto|scroll)/;" + 
    		"" + 
    		"    if (style.position === \"fixed\") return rootElement;" + 
    		"    for (var parent = element; (parent = parent.parentElement);) {" + 
    		"        style = getComputedStyle(parent);" + 
    		"        if (excludeStaticParent && style.position === \"static\") {" + 
    		"            continue;" + 
    		"        }" + 
    		"        if (overflowRegex.test(style.overflow + style.overflowY + style.overflowX)) return parent;" + 
    		"    }" + 
    		"" + 
    		"    return rootElement;" + 
    		"}" +
    		"return getScrollParent(arguments[0], false);";
    
    public static final String NON_JS_UPLOAD_FILE_THROUGH_POPUP = 
    		"var action = 'upload_file_through_popup';";
    public static final String NON_JS_CAPTURE_DESKTOP = 
    		"var action = 'capture_desktop_snapshot_to_base64_string';return '';";
    
    public CustomEventFiringWebDriver(final WebDriver driver) {
    	this(driver, null, null, true, DriverMode.LOCAL, null, null);
    }

	public CustomEventFiringWebDriver(final WebDriver driver, List<Long> driverPids, BrowserInfo browserInfo, Boolean isWebTest, DriverMode localDriver, BrowserMobProxy mobProxy, SeleniumGridConnector gridConnector) {
        super(driver);
        this.driverPids = driverPids == null ? new ArrayList<>(): driverPids;
		this.driver = driver;
		this.browserInfo = browserInfo;
		this.isWebTest = isWebTest;
		this.driverMode = localDriver;
		this.mobProxy = mobProxy;
		this.gridConnector = gridConnector;
		
		// NEOLOAD //
		if (driver instanceof NLWebDriver) {
			neoloadDriver = (NLWebDriver)driver;
		} else {
			neoloadDriver = null;
		}
    }

    public void setFileDetector(final FileDetector detector) {
        if (detector == null) {
            throw new WebDriverException("file detector is null");
        }

        fileDetector = detector;
    }
    
    /**
     * Method for updating window handles when an operation may create a new window (a click action)
     * This is called for Composite actions, native actions (from DriverListener) and JS actions
     */
    public void updateWindowsHandles() {
    	if (isWebTest) {
    		currentHandles = getWindowHandles();
    			
    	} else {
    		currentHandles = new TreeSet<>();
    	}
    }
    
    @Override
    public Set<String> getWindowHandles() {
    	
    	if (!isWebTest) {
    		return new TreeSet<>();
    	}
    	
    	// issue #169: workaround for ios / IE tests where getWindowHandles sometimes fails with: class org.openqa.selenium.WebDriverException: Returned value cannot be converted to List<String>: true
    	for (int i = 0; i < 10; i++) {
			try  {
				return super.getWindowHandles();
			} catch (UnhandledAlertException e) {
	    		logger.info("getWindowHandles: Handling alert");
	    		handleAlert();
	    		return super.getWindowHandles();
			} catch (WebSessionEndedException e) {
				logger.warn("session already terminated");
				return new TreeSet<>();
			} catch (Exception e) {
				logger.info("error getting window handles: " + e.getMessage());
				WaitHelper.waitForSeconds(2);
			}
		}
    	return super.getWindowHandles();
    	
    }
    
    private void handleAlert() {
    	try {
	    	Alert alert = driver.switchTo().alert();
			alert.dismiss();
    	} catch (Exception e) {}
    }
    
    @Override
    public String getWindowHandle() {
    	
    	if (!isWebTest) {
    		return "";
    	}
    	
    	try {
    		return super.getWindowHandle();
    	} catch (UnhandledAlertException e) {
    		logger.info("getWindowHandle: Handling alert");
    		handleAlert();
    		return super.getWindowHandle();
    	}
    }
    
    @Override
    public void close() {
    	try {
    		super.close();
    	} catch (UnhandledAlertException e) {
    		logger.info("close: Handling alert");
    		handleAlert();
    		super.close();
    	}
    }
    
    @Override
    public String getCurrentUrl() {
    	try {
	    	return super.getCurrentUrl();
	    } catch (UnhandledAlertException e) {
    		logger.info("getCurrentUrl: Handling alert");
			handleAlert();
			return super.getCurrentUrl();
		}
    }
    
    @Override
    public String getTitle() {
    	try {
    		return super.getTitle();
    	} catch (UnhandledAlertException e) {
    		logger.info("getTitle: Handling alert");
			handleAlert();
			return super.getTitle();
    	}
    }
    
    

    public FileDetector getFileDetector() {
        return fileDetector;
    }

    /**
     * Returns the Selenium driver
     * @return
     */
    public WebDriver getWebDriver() {
        return driver;
    }
    
    public String getSessionId() {
    	try {
    		return ((RemoteWebDriver)driver).getSessionId().toString();
    	} catch (ClassCastException e) {
    		return UUID.randomUUID().toString();
    	} catch (NullPointerException e) {
    		return null;
    	}
    }
    
    /**
     * Handle WebDriver exception when this method is not implemented
     */
    @Override
    public String getPageSource() {
    	try {
    		return super.getPageSource();
    	} catch (UnhandledAlertException e) {
    		logger.info("getPageSource: Handling alert");
			handleAlert();
			return super.getPageSource();
    	} catch (WebDriverException e) {
    		logger.info("page source not get: " + e.getMessage());
    		return null;
    	}
    }
    
    public Set<String> getCurrentHandles() {
		return currentHandles;
	}
    
    /**
     * get dimensions of the visible part of the page
     * TODO: handle mobile app case
     * @return
     */
    @SuppressWarnings("unchecked")
	public Dimension getViewPortDimensionWithoutScrollbar() {
    	if (isWebTest) {
    		try {
	    		List<Number> dims = (List<Number>)((JavascriptExecutor)driver).executeScript(JS_GET_VIEWPORT_SIZE);
	    		
	    		// issue #238: check we get a non max size
	    		if (dims.get(0).intValue() == MAX_DIMENSION || dims.get(1).intValue() == MAX_DIMENSION) {
	    			driver.switchTo().defaultContent();
		    		dims = (List<Number>)((JavascriptExecutor)driver).executeScript(JS_GET_VIEWPORT_SIZE);
	    		}
	    		
	    		Dimension foundDimension = new Dimension(dims.get(0).intValue(), dims.get(1).intValue());
	    		
	    		// issue #233: prevent too big size at it may be used to process images and Buffe
	    		if (foundDimension.width * foundDimension.height * 8L > Integer.MAX_VALUE) {
	    			return new Dimension(2000, 10000);
	    		} else {
	    			return foundDimension;
	    		}
    		} catch (Exception e) {
    			return driver.manage().window().getSize();
    		}
    	} else {
    		return driver.manage().window().getSize();
    	}
    }
    
    /**
     * Get the whole webpage dimension
     * TODO: handle mobile app case
     * @return
     */

	@SuppressWarnings("unchecked")
    public Dimension getContentDimension() {
    	if (isWebTest) {
    		try {
				List<Number> dims = (List<Number>)((JavascriptExecutor)driver).executeScript(JS_GET_CONTENT_ENTIRE_SIZE);
		    	
		    	// issue #238: check we get a non zero size
		    	if (dims.get(0).intValue() == 0 || dims.get(1).intValue() == 0) {
		    		driver.switchTo().defaultContent();
		    		dims = (List<Number>)((JavascriptExecutor)driver).executeScript(JS_GET_CONTENT_ENTIRE_SIZE);
		    	}
		    	
		    	return new Dimension(dims.get(0).intValue(), dims.get(1).intValue());
		    	
    		} catch (Exception e) {
    			return driver.manage().window().getSize();
    		}
    	} else {
    		return driver.manage().window().getSize();
    	}
    }
	
	/**
	 * TODO: handle mobile app case
	 */
	public void scrollTop() {
		if (isWebTest) {
			((JavascriptExecutor) driver).executeScript("window.top.scroll(0, 0)");
		} 
	}
	
	public void scrollTo(int x, int y) {
		if (isWebTest) {
			((JavascriptExecutor) driver).executeScript(String.format("window.top.scroll(%d, %d)", x, y));
			
			// wait for scrolling end
			Point previousScrollPosition = getScrollPosition();
			int i = 0;
			boolean fixed = false;
			do {
				Point scrollPosition = getScrollPosition();
				if (scrollPosition.x == x && scrollPosition.y == y) {
					break;
				} else if (scrollPosition.x == previousScrollPosition.x && scrollPosition.y == previousScrollPosition.y) {
					if (!fixed) {
						fixed = true;
					} else {
						break;
					}
				}
				previousScrollPosition = scrollPosition;
				WaitHelper.waitForMilliSeconds(100);
				i++;
			} while (i < 10);
		} 
	}
	
	/**
	 * scroll to the given element
	 * we scroll 200 px to the left of the element so that we see all of it
	 * @param element
	 */
	public void scrollToElement(WebElement element, int yOffset) {
		if (isWebTest) {
			try {
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
				WebElement parentScrollableElement = (WebElement) ((JavascriptExecutor) driver).executeScript(JS_SCROLL_PARENT, element, (driver instanceof SafariDriver) ? "safari": "other");
				if (parentScrollableElement != null) {
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop += arguments[1]", parentScrollableElement, yOffset);
				}
				
			} catch (Exception e) {
				// fall back to legacy behavior
				((JavascriptExecutor) driver).executeScript("window.top.scroll(" + Math.max(element.getLocation().x - 200, 0) + "," + Math.max(element.getLocation().y + yOffset, 0) + ")");
			}
		}
	}
	
	/**
	 * TODO: handle mobile app case
	 */
	@SuppressWarnings("unchecked")
	public Point getScrollPosition() {
		if (isWebTest) {
			try {
				List<Number> dims = (List<Number>)((JavascriptExecutor) driver).executeScript(JS_GET_CURRENT_SCROLL_POSITION);
				return new Point(dims.get(0).intValue(), dims.get(1).intValue());
			} catch (Exception e) {
    			return new Point(0, 0);
    		}
		} else {
			throw new WebDriverException("scroll position can only be get for web");
		}
	}
	
	/**
	 * Returns the rectangle of all screens on the system
	 * @return
	 */
	private static Rectangle getScreensRectangle() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		
		Rectangle screenRect = new Rectangle(0, 0, 0, 0);
		for (GraphicsDevice gd : ge.getScreenDevices()) {
		    screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
		}
		return screenRect;
	}
	
	/**
	 * Take screenshot of the desktop and put it in a file
	 * Do not expose this method because we need to check that we have a graphical environment. 
	 */
	private static BufferedImage captureDesktopToBuffer() {
		
		Rectangle screenRect = getScreensRectangle();
		try {
			return new Robot().createScreenCapture(screenRect);
		} catch (AWTException e) {
			throw new ScenarioException("Cannot capture image", e);
		}
		
//		Integer screenWidth = defaultGraphicDevice.getDisplayMode().getWidth();
//		Integer screenHeight = defaultGraphicDevice.getDisplayMode().getHeight();
//		
//		// Capture the screen shot of the area of the screen defined by the rectangle
//		try {
//			return new Robot().createScreenCapture(new Rectangle(screenWidth, screenHeight));
//		} catch (AWTException e) {
//			throw new ScenarioException("Cannot capture image", e);
//		}
	}

	/**
	 * After quitting driver, if it fails, some pids may remain. Kill them
	 */
	@Override
	public void quit() {
		
		// get list of pids we could have to kill. Sometimes, Chrome does not close all its processes
		// so we have to know which processes to kill when driver is still active
		List<Long> pidsToKill = new ArrayList<>();
		if (browserInfo != null && driverMode == DriverMode.LOCAL) {
			pidsToKill.addAll(browserInfo.getAllBrowserSubprocessPids(driverPids));
		}
		Capabilities caps = getCapabilities();
		
		// close windows before quitting (this is the only way to close chrome attached browser when it's not started by selenium)
		try {
			for (String handle: getWindowHandles()) {
				driver.switchTo().window(handle);
				driver.close();
			}
		} catch (Throwable e) {}
		
		
		Long duration = 0L;
		try {
			duration = new Date().getTime() - (Long)internalCapabilities.getCapability(DriverUsage.START_TIME);
		} catch (Exception e) {}
		String gridHub = caps.getCapability(DriverUsage.GRID_HUB) != null ? caps.getCapability(DriverUsage.GRID_HUB).toString(): null;
		String sessionId = caps.getCapability(DriverUsage.SESSION_ID) != null ? caps.getCapability(DriverUsage.SESSION_ID).toString(): null;
		
		// store driver stats
		DriverUsage usage = new DriverUsage(gridHub, 
				(String) caps.getCapability(DriverUsage.GRID_NODE), 
				(Long) internalCapabilities.getCapability(DriverUsage.START_TIME), 
				duration, 
				sessionId, 
				caps.getBrowserName(), 
				(Long) internalCapabilities.getCapability(DriverUsage.STARTUP_DURATION), 
				"");
		StatisticsStorage.addDriverUsage(usage);
		
		try {
			driver.quit();
		} finally {
			
			// wait for browser processes to stop
			WaitHelper.waitForSeconds(2);
			
			// only kill processes in local mode
			if (!pidsToKill.isEmpty()) {
		    	for (Long pid: pidsToKill) {
		    		OSUtilityFactory.getInstance().killProcess(pid.toString(), true);
		    	}
			}
			
		}
	}
	
	/**
	 * Use copy to clipboard and copy-paste keyboard shortcut to write something on upload window
	 */
	public static void uploadFileUsingClipboard(File tempFile) {

		// Copy to clipboard
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempFile.getAbsolutePath()), null);
		Robot robot;
		try {
			robot = new Robot();
		
			WaitHelper.waitForSeconds(1);
	
//			// Press Enter
//			robot.keyPress(KeyEvent.VK_ENTER);
//	
//			// Release Enter
//			robot.keyRelease(KeyEvent.VK_ENTER);
	
			// Press CTRL+V
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
	
			// Release CTRL+V
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_V);
			WaitHelper.waitForSeconds(1);
	
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		} catch (AWTException e) {
			throw new ScenarioException("could not initialize robot to upload file: " + e.getMessage());
		}
	}
	
	/**
	 * Upload file typing file path directly
	 * @param tempFile
	 */
	public static void uploadFileUsingKeyboardTyping(File tempFile) {
		try {
			Keyboard keyboard = new Keyboard();
			Robot robot = keyboard.getRobot();
			
			WaitHelper.waitForSeconds(1);
			
//			// Press Enter
//			robot.keyPress(KeyEvent.VK_ENTER);
//	
//			// Release Enter
//			robot.keyRelease(KeyEvent.VK_ENTER);
			
			keyboard.typeKeys(tempFile.getAbsolutePath());
			
			WaitHelper.waitForSeconds(1);
			
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			
		} catch (AWTException e) {
			throw new ScenarioException("could not initialize robot to upload file typing keys: " + e.getMessage());
		}
	}
	
	public static void uploadFile(String fileName, String base64Content, DriverMode driverMode, SeleniumGridConnector gridConnector) throws IOException {
		
		if (driverMode == DriverMode.LOCAL) {
			byte[] byteArray = base64Content.getBytes();
	        File tempFile = new File("tmp/" + fileName);
	        byte[] decodeBuffer = Base64.decodeBase64(byteArray);
	        FileUtils.writeByteArrayToFile(tempFile, decodeBuffer);
	
	        try {
	        	uploadFileUsingClipboard(tempFile);
	        } catch (IllegalStateException e) {
	        	uploadFileUsingKeyboardTyping(tempFile);
	        }
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.uploadFileToBrowser(fileName, base64Content);
		} else {
			throw new ScenarioException("driver supports uploadFile only in local and grid mode");
		}
	}
	
	/**
	 * move mouse taking screen placing into account. Sometimes, coordinates may be negative if first screen has an other screen on the left 
	 * @param robot
	 */
	private static void moveMouse(Robot robot, int x, int y) {
		Rectangle screenRectangle = getScreensRectangle();
		robot.mouseMove(x + screenRectangle.x, y + screenRectangle.y);
	}
	
	/**
	 * Left clic at coordinates on desktop. Coordinates are from screen point of view
	 * @param x
	 * @param y
	 */
	public static void leftClicOnDesktopAt(int x, int y, DriverMode driverMode, SeleniumGridConnector gridConnector) {
		
		if (driverMode == DriverMode.LOCAL) {
			try {
				Robot robot = new Robot();
				moveMouse(robot, x, y);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			} catch (AWTException e) {
				throw new ScenarioException("leftClicOnDesktopAt: problem using Robot: " + e.getMessage());
			}
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.leftClic(x, y);
		} else {
			throw new ScenarioException("driver supports leftClicOnDesktopAt only in local and grid mode");
		}
	}
	
	/**
	 * Left clic at coordinates on desktop. Coordinates are from screen point of view
	 * @param x
	 * @param y
	 */
	public static void doubleClickOnDesktopAt(int x, int y, DriverMode driverMode, SeleniumGridConnector gridConnector) {
		
		if (driverMode == DriverMode.LOCAL) {
			try {
				Robot robot = new Robot();
				moveMouse(robot, x, y);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				robot.delay(10);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			} catch (AWTException e) {
				throw new ScenarioException("doubleClickOnDesktopAt: problem using Robot: " + e.getMessage());
			}
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.doubleClick(x, y);
		} else {
			throw new ScenarioException("driver supports doubleClickOnDesktopAt only in local and grid mode");
		}
	}
	
	/**
	 * right clic at coordinates on desktop. Coordinates are from screen point of view
	 * @param x
	 * @param y
	 */
	public static void rightClicOnDesktopAt(int x, int y, DriverMode driverMode, SeleniumGridConnector gridConnector) {
		
		if (driverMode == DriverMode.LOCAL) {
			try {
				Robot robot = new Robot();
				moveMouse(robot, x, y);
				robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
			} catch (AWTException e) {
				throw new ScenarioException("rightClicOnDesktopAt: problem using Robot: " + e.getMessage());
			}
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.rightClic(x, y);
		} else {
			throw new ScenarioException("driver supports sendKeysToDesktop only in local and grid mode");
		}
	}
	
	/**
	 * write text to desktop.
	 * @param textToWrite	text to write
	 * @return
	 */
	public static void writeToDesktop(String textToWrite, DriverMode driverMode, SeleniumGridConnector gridConnector) {
		if (driverMode == DriverMode.LOCAL) {
	
			try {
				Keyboard keyboard = new Keyboard();
				keyboard.typeKeys(textToWrite);
			} catch (AWTException e) {
				throw new ScenarioException("writeToDesktop: could not initialize robot to type keys: " + e.getMessage());
			}
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.writeText(textToWrite);
		} else {
			throw new ScenarioException("driver supports sendKeysToDesktop only in local and grid mode");
		}
	}
	
	/**
	 * send keys to desktop
	 * This is useful for typing special keys like ENTER
	 * @param keys
	 */
	public static void sendKeysToDesktop(List<Integer> keyCodes, DriverMode driverMode, SeleniumGridConnector gridConnector) {
		if (driverMode == DriverMode.LOCAL) {
			try {
				Robot robot = new Robot();
				
				WaitHelper.waitForSeconds(1);
				
				for (Integer key: keyCodes) {
					robot.keyPress(key);
					robot.keyRelease(key);
				}
			} catch (AWTException e) {
				throw new ScenarioException("could not initialize robot to type keys: " + e.getMessage());
			}
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.sendKeysWithKeyboard(keyCodes);
		} else {
			throw new ScenarioException("driver supports sendKeysToDesktop only in local and grid mode");
		}
	}
	
	/**
	 * Returns a Base64 string of the desktop
	 * @param driverMode
	 * @param gridConnector
	 * @return
	 */
	public static String captureDesktopToBase64String(DriverMode driverMode, SeleniumGridConnector gridConnector) {
		if (driverMode == DriverMode.LOCAL) {
			BufferedImage bi = captureDesktopToBuffer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStream b64 = new Base64OutputStream(os);
			try {
				ImageIO.write(bi, "png", b64);
				return os.toString("UTF-8");
			} catch (IOException e) {
				return "";
			}
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			return gridConnector.captureDesktopToBuffer();
		} else {
			throw new ScenarioException("driver supports captureDesktopToBase64String only in local and grid mode");
		}
	}
	
	/**
	 * Start video capture using VideoRecorder class
	 * @param driverMode
	 * @param gridConnector
	 * @param videoName		name of the video to record so that it's unique. Only used locally. In remote, grid sessionId is used
	 */
	public static VideoRecorder startVideoCapture(DriverMode driverMode, SeleniumGridConnector gridConnector, File videoFolder, String videoName) {
		if (driverMode == DriverMode.LOCAL) {
			try {
				VideoRecorder recorder = new VideoRecorder(videoFolder, videoName);
				recorder.start();
				return recorder;
			} catch (HeadlessException e) {
				throw new ScenarioException("could not initialize video capture with headless robot: " + e.getMessage());
			}
			
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			gridConnector.startVideoCapture();
			return new VideoRecorder(videoFolder, videoName, false);
		} else {
			throw new ScenarioException("driver supports startVideoCapture only in local and grid mode");
		}
	}
	
	/**
	 * Stop video capture using VideoRecorder class
	 * @param driverMode
	 * @param gridConnector
	 * @throws IOException 
	 */
	public static File stopVideoCapture(DriverMode driverMode, SeleniumGridConnector gridConnector, VideoRecorder recorder) throws IOException {
		if (driverMode == DriverMode.LOCAL && recorder != null) {
			return recorder.stop();
		} else if (driverMode == DriverMode.GRID && gridConnector != null) {
			return gridConnector.stopVideoCapture(Paths.get(recorder.getFolderPath().getAbsolutePath(), recorder.getFileName()).toString());
			
		} else {
			throw new ScenarioException("driver supports stopVideoCapture only in local and grid mode");
		}
	}
	
	/**
	 * Intercept specific scripts to do some non selenium actions
	 * 
	 * @deprecated: should be removed (kept here for compatibility with old robots)
	 */
	@Override
	public Object executeScript(String script, Object... args) {
		
		// to we know this command ?
		if (driverMode == DriverMode.LOCAL && NON_JS_UPLOAD_FILE_THROUGH_POPUP.equals(script)) {
			if (args.length != 2) {
				throw new DriverExceptions("Upload feature through executeScript needs 2 string arguments (file name, base64 content)");
			}
			try {
				uploadFile((String)args[0], (String)args[1], driverMode, gridConnector);
				return null; 
			} catch (IOException e) {
				return null;
			}
			
		} else if (driverMode == DriverMode.LOCAL && NON_JS_CAPTURE_DESKTOP.equals(script)) {
			return captureDesktopToBase64String(driverMode, gridConnector);
		} else {
			return super.executeScript(script, args);
		}
	}
	

    public List<Long> getDriverPids() {
		return driverPids;
	}

	public BrowserInfo getBrowserInfo() {
		return browserInfo;
	}

	@Override
    public Capabilities getCapabilities() {
		try {
			return ((HasCapabilities)driver).getCapabilities();
		} catch (ClassCastException e) {
			return new MutableCapabilities();
		}
    }

	public BrowserMobProxy getMobProxy() {
		return mobProxy;
	}

	// NEOLOAD //
	public NLWebDriver getNeoloadDriver() {
		return neoloadDriver;
	}

	public MutableCapabilities getInternalCapabilities() {
		return internalCapabilities;
	}
}
