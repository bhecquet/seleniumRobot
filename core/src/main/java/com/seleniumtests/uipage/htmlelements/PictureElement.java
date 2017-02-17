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

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.TouchAction;

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
	private EventFiringWebDriver driver;
	private ImageDetector detector;
	private ScreenshotUtil screenshotUtil;
	private SystemClock clock = new SystemClock();

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
	 * Search the picture in the screenshot taken by Robot or WebDriver
	 * Robot is used in Desktop mode
	 * WebDriver is used in mobile, because Robot is not available for mobile platforms
	 * 
	 * @param searchOnly
	 */
	public void findElement(boolean searchOnly) {
		
		// scroll to element where our picture is
		if (intoElement != null) {
			ImageIcon image = new ImageIcon(objectPictureFile.getAbsolutePath());
			intoElement.scrollToElement(200 + image.getIconHeight());
		}
		
		File screenshotFile;
		if (searchOnly) {
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
		findElement(true);
		
		Point intoElementPos = intoElement.getCoordinates().inViewPort();
		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2 - intoElementPos.x;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2 - intoElementPos.y;
		
		moveAndClick(intoElement, relativeX + (int)(xOffset * pictureSizeRatio), relativeY + (int)(yOffset * pictureSizeRatio));
	}
	
	public void moveAndClick(WebElement element, int coordX, int coordY) {
		new Actions(driver).moveToElement(intoElement, coordX, coordY).click().build().perform();
	}
	
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
		clickAt(xOffset, yOffset);
		
		new Actions(driver).sendKeys(text).build().perform();
	}

	public void sendKeys(final CharSequence text) {
		sendKeys(text, 0, 0);
	}
	
	/**
	 * Returns true in cas the searched picture is found
	 * @deprecated use isElementPresentInstead
	 * @return
	 */
	@Deprecated
	public boolean isVisible() {
		try {
			findElement(true);
			return true;
		} catch (ImageSearchException e) {
			return false;
		}
	}
	
	public boolean isElementPresent() {
		return isElementPresent(0);
	}
	
	/**
	 * check whether picture is present or not
	 * @param waitMs
	 * @return
	 */
	public boolean isElementPresent(int waitMs) {
		long end = clock.laterBy(waitMs);
		while (clock.isNowBefore(end) || waitMs == 0) {
			try {
				findElement(true);
				return true;
			} catch (ImageSearchException e) {
				if (waitMs == 0) {
					return false;
				}
				WaitHelper.waitForMilliSeconds(200);
				continue;
			}
		}
		return false;
		
	}
	
	@Override
	public String toString() {
		if (resourcePath != null) {
			return "Picture from resource " + resourcePath;
		} else {
			return "Picture from file " + objectPictureFile.getAbsolutePath();
		}
	}

	public void setObjectPictureFile(File objectPictureFile) {
		this.objectPictureFile = objectPictureFile;
		detector.setObjectImage(objectPictureFile);
	}
	
	// TODO: actions for mobile
	// https://discuss.appium.io/t/tapping-on-the-screen-by-using-coordinates/5529/7
	
}
