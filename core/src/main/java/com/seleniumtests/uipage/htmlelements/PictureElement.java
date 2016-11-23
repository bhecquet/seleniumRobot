package com.seleniumtests.uipage.htmlelements;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.imaging.ImageDetector;

/**
 * Element which is found inside driver snapshot
 * @author behe
 *
 */
public class PictureElement extends HtmlElement {
	
	private File objectPictureFile;
	private HtmlElement intoElement;
	private Rectangle detectedObjectRectangle;
	private double pictureSizeRatio;
	private Robot robot;

	public PictureElement(String label, String resourcePath, HtmlElement intoElement) {
		try {
			objectPictureFile = createFileFromResource(resourcePath);
		} catch (IOException e) {
			throw new ConfigurationException("Resource cannot be found", e);
		}
		this.intoElement = intoElement;
		if (!SeleniumTestsContextManager.isMobileTest()) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				throw new ScenarioException("Cannot create robot", e);
			}
		}
	}
	
	/**
	 * 
	 * @param label
	 * @param pictureFile
	 * @param intoElement	HtmlElement inside of which our picture is. It allows scrolling to the zone where 
	 * 						picture is searched before doing capture
	 */
	public PictureElement(String label, File pictureFile, HtmlElement intoElement) {
		objectPictureFile = pictureFile;
		this.intoElement = intoElement;
		if (!SeleniumTestsContextManager.isMobileTest()) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				throw new ScenarioException("Cannot create robot", e);
			}
		}
	}
	
	private File createFileFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", null);
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}
	
	protected void findElement() {
		
		// scroll to element where our picture is
		if (intoElement != null) {
			ImageIcon image = new ImageIcon(objectPictureFile.getAbsolutePath());
			intoElement.scrollToElement(200 + image.getIconHeight());
		}
		
		File screenshotFile;
		if (SeleniumTestsContextManager.isMobileTest()) {
			screenshotFile = new ScreenshotUtil().captureWebPageToFile();
		} else {
			screenshotFile = new ScreenshotUtil().captureDesktopToFile();
		}
		if (screenshotFile == null) {
			throw new WebDriverException("Screenshot does not exist");
		}
		ImageDetector detector = new ImageDetector(screenshotFile, objectPictureFile);
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
	@Override
	@ReplayOnError
	public void clickAt(int xOffset, int yOffset) {
		if (SeleniumTestsContextManager.isMobileTest()) {
			throw new ScenarioException("click on picture is not supported on mobile devices");
		}
		findElement();
	    robot.mouseMove(detectedObjectRectangle.x + detectedObjectRectangle.width / 2 + xOffset, detectedObjectRectangle.y + detectedObjectRectangle.height / 2 + yOffset);    
	    robot.mousePress(InputEvent.BUTTON1_MASK);
	    robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	@Override 
	@ReplayOnError
	public void sendKeys(final CharSequence text) {
		
	}
	
	// TODO: actions for mobile
	// https://discuss.appium.io/t/tapping-on-the-screen-by-using-coordinates/5529/7
	
}
