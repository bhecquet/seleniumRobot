package com.seleniumtests.uipage.htmlelements;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Element which is found inside driver snapshot
 * @author behe
 *
 */
public class PictureElement {
	
	private File objectPictureFile;
	private String resourcePath;
	private HtmlElement intoElement;
	private Rectangle detectedObjectRectangle;
	private double pictureSizeRatio;
	private Robot robot;
	private EventFiringWebDriver driver;
	private ImageDetector detector;
	private ScreenshotUtil screenshotUtil;
	

	private static Logger logger = SeleniumRobotLogger.getLogger(PictureElement.class);
	
	public PictureElement() {
		// for mocks
	}

	public PictureElement(String label, String resourcePath, HtmlElement intoElement) {
		this(label, createFileFromResource(resourcePath), intoElement);
		this.resourcePath = resourcePath;
		driver = (EventFiringWebDriver)WebUIDriver.getWebDriver();
		
	}
	
	/**
	 * 
	 * @param label
	 * @param pictureFile
	 * @param intoElement	HtmlElement inside of which our picture is. It allows scrolling to the zone where 
	 * 						picture is searched before doing capture
	 */
	public PictureElement(String label, File pictureFile, HtmlElement intoElement) {
		this.intoElement = intoElement;
		if (isRobotUsable()) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				throw new ScenarioException("Cannot create robot", e);
			}
		}
		detector = new ImageDetector();
		setObjectPictureFile(pictureFile);
		screenshotUtil = new ScreenshotUtil();
	}
	
	private static File createFileFromResource(String resource)  {
		try {
			File tempFile = File.createTempFile("img", null);
			tempFile.deleteOnExit();
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
			
			return tempFile;
		} catch (IOException e) {
			throw new ConfigurationException("Resource cannot be found", e);
		}
	}
	
	/**
	 * Check whether Robot is usable
	 * It's only available in local mode (for now)
	 * @return
	 */
	public boolean isRobotUsable() {
		if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL
				&& !SeleniumTestsContextManager.isMobileTest()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Search the picture in the screenshot taken by Robot or WebDriver
	 * Robot is used in Desktop mode
	 * WebDriver is used in mobile, because Robot is not available for mobile platforms
	 * 
	 * @param searchOnly
	 */
	protected void findElement(boolean searchOnly) {
		
		// scroll to element where our picture is
		if (intoElement != null) {
			ImageIcon image = new ImageIcon(objectPictureFile.getAbsolutePath());
			intoElement.scrollToElement(200 + image.getIconHeight());
		}
		
		File screenshotFile;
		if (robot == null || searchOnly) {
			screenshotFile = screenshotUtil.captureWebPageToFile();
		} else {
			screenshotFile = screenshotUtil.captureDesktopToFile();
		}
		if (screenshotFile == null) {
			throw new WebDriverException("Screenshot does not exist");
		}
		detector.setSceneImage(screenshotFile);
		detector.detectCorrespondingZone();
		detectedObjectRectangle = detector.getDetectedRectangle();
		pictureSizeRatio = detector.getSizeRatio();
	}


	/**
	 * Click in the center of the found picture
	 */
	public void click() {
		clickAt(0, 0);
	}
	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	@ReplayOnError
	public void clickAt(int xOffset, int yOffset) {
		if (robot == null) {
			throw new ScenarioException("click on picture is not supported on mobile devices and remote mode");
		}
		findElement(false);
	    robot.mouseMove(detectedObjectRectangle.x + detectedObjectRectangle.width / 2 + (int)(xOffset * pictureSizeRatio), 
	    			    detectedObjectRectangle.y + detectedObjectRectangle.height / 2 +(int)(yOffset * pictureSizeRatio));    
	    robot.mousePress(InputEvent.BUTTON1_MASK);
	    robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
		clickAt(xOffset, yOffset);
		
		for (int i=0; i < text.length(); i++) {
			char ch = text.charAt(i);
			int keyCode = KeyEvent.getExtendedKeyCodeForChar((int)ch);
			boolean shift = false;
			if ((Character.isUpperCase(ch) && Character.isLetter(ch))
					|| Character.isDigit(ch)) {
				shift = true;
			}
			
			try {
				if (shift) {
					robot.keyPress(KeyEvent.VK_SHIFT);
				}
				robot.keyPress(keyCode);
				robot.keyRelease(keyCode);
				if (shift) {
					robot.keyRelease(KeyEvent.VK_SHIFT);
				}
			} catch (IllegalArgumentException e) {
				logger.warn(String.format("Character %s could not be written", text.charAt(i)), e);
			}
		}
	}

	public void sendKeys(final CharSequence text) {
		sendKeys(text, 0, 0);
	}
	
	/**
	 * Returns true in cas the searched picture is found
	 * @return
	 */
	public boolean isVisible() {
		try {
			findElement(true);
			return true;
		} catch (ImageSearchException e) {
			return false;
		}
	}
	
	@Override
	public String toString() {
		if (resourcePath != null) {
			return "Picture from resource " + resourcePath;
		} else {
			return "Picture from file " + objectPictureFile.getAbsolutePath();
		}
	}
	
	
	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	public void setObjectPictureFile(File objectPictureFile) {
		this.objectPictureFile = objectPictureFile;
		detector.setObjectImage(objectPictureFile);
	}
	
	// TODO: actions for mobile
	// https://discuss.appium.io/t/tapping-on-the-screen-by-using-coordinates/5529/7
	
}
