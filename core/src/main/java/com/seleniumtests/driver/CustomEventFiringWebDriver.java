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
package com.seleniumtests.driver;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.UselessFileDetector;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

import net.lightbody.bmp.BrowserMobProxy;

/**
 * Supports file upload in remote webdriver.
 */
public class CustomEventFiringWebDriver extends EventFiringWebDriver implements HasCapabilities {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(CustomEventFiringWebDriver.class);
    private FileDetector fileDetector = new UselessFileDetector();
    private Set<String> currentHandles;
    private final List<Long> driverPids;
	private final WebDriver driver;
	private final boolean isWebTest;
	private final DriverMode driverMode;
	private final BrowserInfo browserInfo;
	private final BrowserMobProxy mobProxy;
    
    private static final String JS_GET_VIEWPORT_SIZE =
    		"var pixelRatio;" +
    	    "try{pixelRatio = devicePixelRatio} catch(err){pixelRatio=1}" +
            "var height = 100000;"
                + "var width = 100000;"
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
                + "	return [width * pixelRatio, height * pixelRatio];";

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
    
    public static final String NON_JS_UPLOAD_FILE_THROUGH_POPUP = 
    		"var action = 'upload_file_through_popup';";
    public static final String NON_JS_CAPTURE_DESKTOP = 
    		"var action = 'capture_desktop_snapshot_to_base64_string';return '';";
    
    public CustomEventFiringWebDriver(final WebDriver driver) {
    	this(driver, null, null, true, DriverMode.LOCAL, null);
    }

	public CustomEventFiringWebDriver(final WebDriver driver, List<Long> driverPids, BrowserInfo browserInfo, Boolean isWebTest, DriverMode localDriver, BrowserMobProxy mobProxy) {
        super(driver);
        this.driverPids = driverPids == null ? new ArrayList<>(): driverPids;
		this.driver = driver;
		this.browserInfo = browserInfo;
		this.isWebTest = isWebTest;
		this.driverMode = localDriver;
		this.mobProxy = mobProxy;
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
    		// workaround for ios tests where getWindowHandles sometimes fails
    		for (int i = 0; i < 10; i++) {
    			try  {
    				currentHandles = driver.getWindowHandles();
    				break;
    			} catch (Exception e) {
    				logger.info("getting window handles");
    				WaitHelper.waitForSeconds(2);
    			}
    		}
    			
    	} else {
    		currentHandles = new TreeSet<>();
    	}
    }

    public FileDetector getFileDetector() {
        return fileDetector;
    }

    public WebDriver getWebDriver() {
        return driver;
    }
    
    /**
     * Handle WebDriver exception when this method is not implemented
     */
    @Override
    public String getPageSource() {
    	try {
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
    		List<Long> dims = (List<Long>)((JavascriptExecutor)driver).executeScript(JS_GET_VIEWPORT_SIZE);
    		return new Dimension(dims.get(0).intValue(), dims.get(1).intValue());
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
			List<Long> dims = (List<Long>)((JavascriptExecutor)driver).executeScript(JS_GET_CONTENT_ENTIRE_SIZE);
	    	return new Dimension(dims.get(0).intValue(), dims.get(1).intValue());
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
//			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
			((JavascriptExecutor) driver).executeScript("window.top.scroll(" + Math.max(element.getLocation().x - 200, 0) + "," + Math.max(element.getLocation().y + yOffset, 0) + ")");
		}
	}
	
	/**
	 * TODO: handle mobile app case
	 */
	@SuppressWarnings("unchecked")
	public Point getScrollPosition() {
		if (isWebTest) {
			List<Long> dims = (List<Long>)((JavascriptExecutor) driver).executeScript(JS_GET_CURRENT_SCROLL_POSITION);
			return new Point(dims.get(0).intValue(), dims.get(1).intValue());
		} else {
			throw new WebDriverException("scroll position can only be get for web");
		}
	}
	
	/**
	 * Take screenshot of the desktop and put it in a file
	 */
	private BufferedImage captureDesktopToBuffer() {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultGraphicDevice = ge.getDefaultScreenDevice();
		
		Rectangle screenRect = new Rectangle(0, 0, 0, 0);
		for (GraphicsDevice gd : ge.getScreenDevices()) {
		    screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
		}
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
		try {
			driver.quit();
		} finally {
			// only kill processes in local mode
			if (browserInfo == null || driverMode != DriverMode.LOCAL) {
				return;
			}
			List<Long> pidsToKill = browserInfo.getAllBrowserSubprocessPids(driverPids);
	    	for (Long pid: pidsToKill) {
	    		OSUtilityFactory.getInstance().killProcess(pid.toString(), true);
	    	}
		}
	}
	
	/**
	 * Use copy to clipboard and copy-paste keyboard shortcut to write something on upload window
	 */
	public void uploadFileUsingClipboard(File tempFile) {

		// Copy to clipboard
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempFile.getAbsolutePath()), null);
		Robot robot;
		try {
			robot = new Robot();
		
			WaitHelper.waitForSeconds(1);
	
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
	
			// Release Enter
			robot.keyRelease(KeyEvent.VK_ENTER);
	
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
	public void uploadFileUsingKeyboardTyping(File tempFile) {
		try {
			Keyboard keyboard = new Keyboard();
			Robot robot = keyboard.getRobot();
			
			WaitHelper.waitForSeconds(1);
			
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
	
			// Release Enter
			robot.keyRelease(KeyEvent.VK_ENTER);
			
			keyboard.typeKeys(tempFile.getAbsolutePath());
			
			WaitHelper.waitForSeconds(1);
			
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			
		} catch (AWTException e) {
			throw new ScenarioException("could not initialize robot to upload file typing keys: " + e.getMessage());
		}
	}
	
	public void uploadFile(String fileName, String base64Content) throws IOException {
		
		byte[] byteArray = base64Content.getBytes();
        File tempFile = new File("tmp/" + fileName);
        byte[] decodeBuffer = Base64.decodeBase64(byteArray);
        FileUtils.writeByteArrayToFile(tempFile, decodeBuffer);

        try {
        	uploadFileUsingClipboard(tempFile);
        } catch (IllegalStateException e) {
        	uploadFileUsingKeyboardTyping(tempFile);
        }
	}
	
	/**
	 * Intercept specific scripts to do some non selenium actions
	 */
	@Override
	public Object executeScript(String script, Object... args) {
		
		// to we know this command ?
		if (driverMode == DriverMode.LOCAL && NON_JS_UPLOAD_FILE_THROUGH_POPUP.equals(script)) {
			if (args.length != 2) {
				throw new DriverExceptions("Upload feature through executeScript needs 2 string arguments (file name, base64 content)");
			}
			try {
				uploadFile((String)args[0], (String)args[1]);
				return null; 
			} catch (IOException e) {
				return null;
			}
			
		} else if (driverMode == DriverMode.LOCAL && NON_JS_CAPTURE_DESKTOP.equals(script)) {
			BufferedImage bi = captureDesktopToBuffer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStream b64 = new Base64OutputStream(os);
			try {
				ImageIO.write(bi, "png", b64);
				return os.toString("UTF-8");
			} catch (IOException e) {
				return "";
			}
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
        return ((HasCapabilities)driver).getCapabilities();
    }

	public BrowserMobProxy getMobProxy() {
		return mobProxy;
	}
}
