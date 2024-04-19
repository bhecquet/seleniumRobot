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

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.remote.ScreenshotException;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Search on picture (desktop or browser capture)
 * @author behe
 *
 */
public abstract class GenericPictureElement extends Element {
	protected static final Logger logger = SeleniumRobotLogger.getLogger(GenericPictureElement.class);

	protected File objectPictureFile;
	protected String resourcePath;
	protected Map<PageObject, Rectangle> detectedObjectRectangle = new HashMap<>();
	protected Map<PageObject, Double> pictureSizeRatio = new HashMap<>();

	protected ImageDetector detector;
	protected boolean searchOnDesktop;
	protected long actionDuration;
	protected ScreenshotUtil screenshotUtil;
	protected Clock clock = Clock.systemUTC();

	protected GenericPictureElement() {
		super("");
		// for mocks
	}
	
	/**
	 * 
	 * @param label
	 * @param pictureFile			picture to search for in snapshot or on desktop
	 * @param detectionThreshold	sensitivity of search between 0 and 1. Be default, 0.1. More sensitivity means search can be less accurate, detect unwanted zones
	 * @param searchOnDesktop		By default, false: search in driver snapshot. If true, we take a desktop screenshot, allowing searching into other elements than browser
	 */
	protected GenericPictureElement(String label, File pictureFile, double detectionThreshold, boolean searchOnDesktop) {
		super(label);
		this.searchOnDesktop = searchOnDesktop;
		
		if (pictureFile != null) {
			detector = new ImageDetector();
			detector.setDetectionThreshold(detectionThreshold);
			setObjectPictureFile(pictureFile);
		}
	}
	
	protected static File createFileFromResource(String resource)  {
		try {
			File tempFile = File.createTempFile("img", null);
			tempFile.deleteOnExit();
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
			
			return tempFile;
		} catch (IOException | NullPointerException e) {
			throw new ConfigurationException(String.format("Resource '%s' cannot be found", resource), e);
		}
	}
	
	protected void findElement(boolean waitForVisibility) {
		findElement();
	}
	
	/**
	 * Clear stored searches
	 * For test
	 */
	public void clearMemory() {
		detectedObjectRectangle.clear();
		pictureSizeRatio.clear();
	}
	
	/**
	 * Search the picture in the screenshot taken by Robot or WebDriver
	 * Robot is used in Desktop mode
	 * WebDriver is used in mobile, because Robot is not available for mobile platforms
	 * 
	 */
	public void findElement() {
		
		// we already searched the picture for this page instance, reuse data
		if (getDetectedObjectRectangle() != null) {
			logger.info(String.format("Picture %s already searched", objectPictureFile));
			return;
		}

		LocalDateTime start = LocalDateTime.now();
	
		File screenshotFile = getScreenshotFile();
		
		if (screenshotFile == null) {
			throw new ScreenshotException("Screenshot does not exist");
		}
		
		// for desktop search, without reference image, do not search
		if (detector != null) {
			detector.setSceneImage(screenshotFile);
			detector.detectExactZoneWithScale();
			setDetectedObjectRectangleAndAspectRatio(detector.getDetectedRectangle(), detector.getSizeRatio());

		} else {
			setDetectedObjectRectangleAndAspectRatio(new Rectangle(0, 0, 0, 0), 1.0);
		}
		actionDuration = Duration.between(start, LocalDateTime.now()).toMillis();
		
		doAfterPictureSearch();
	}
	
	public void setDetectedObjectRectangleAndAspectRatio(Rectangle detectedRectangle, double sizeRatio) {
		detectedObjectRectangle.put(callingPage.get(), detectedRectangle);
		pictureSizeRatio.put(callingPage.get(), sizeRatio);
	}

	
	/**
	 * Get File containing the screeenshot, either on desktop or on browser
	 * @return
	 */
	protected abstract File getScreenshotFile();
	
	/**
	 * Some action to perform once picture has been found
	 */
	protected abstract void doAfterPictureSearch();
	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	public abstract void clickAt(int xOffset, int yOffset);
	
	/**
	 * Double Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	public abstract void doubleClickAt(int xOffset, int yOffset);

	public abstract void swipe(int xMove, int yMove);
	
	public abstract void tap();
	
	public abstract void sendKeys(final CharSequence text, int xOffset, int yOffset);
	
	/**
	 * Click in the center of the found picture
	 */
	public void click() {
		clickAt(0, 0);
	}
	
	/**
	 * Double click in the center of the found picture
	 */
	public void doubleClick() {
		doubleClickAt(0, 0);
	}
	
	public void sendKeys(CharSequence text) {
		sendKeys(text, 0, 0);
	}
	
	public void sendKeys(CharSequence ... text) {
		sendKeys(text[0], 0, 0);
	}
	
	public boolean isElementPresent() {
		return isElementPresent(0);
	}
	
	/**
	 * Check if picture is visible. This is only available for desktop tests
	 * @param waitMs
	 * @return
	 */
	public boolean isElementPresent(int waitMs) {
		Instant end = clock.instant().plusMillis(waitMs);
		while (end.isAfter(clock.instant()) || waitMs == 0) {
			try {
				findElement();
				return true;
			} catch (ImageSearchException e) {
				if (waitMs == 0) {
					return false;
				}
				WaitHelper.waitForMilliSeconds(200);
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		if (resourcePath != null) {
			return String.format("Picture %s from resource %s", label, resourcePath);
		} else if (objectPictureFile != null) {
			return String.format("Picture %s from file %s", label, objectPictureFile.getAbsolutePath());
		} else {
			return String.format("Picture %s", label);
		}
	}

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) {
			return name;
		} else if (resourcePath != null) {
			return resourcePath;
		} else if (objectPictureFile != null) {
			return objectPictureFile.getName();
		} else {
			return "screen";
		}
	}

	public void setObjectPictureFile(File objectPictureFile) {
		this.objectPictureFile = objectPictureFile;
		try {
			detector.setObjectImage(objectPictureFile);
		} catch (ImageSearchException e) {
			throw new ConfigurationException(e.getMessage());
		}
		
	}

	public Rectangle getDetectedObjectRectangle() {
		return detectedObjectRectangle.get(callingPage.get());
	}
	
	public Double getPictureSizeRatio() {
		return pictureSizeRatio.get(callingPage.get());
	}

	public long getActionDuration() {
		return actionDuration;
	}

	public void setActionDuration(long actionDuration) {
		this.actionDuration = actionDuration;
	}

	public File getObjectPictureFile() {
		return objectPictureFile;
	}

	public ImageDetector getDetector() {
		return detector;
	}

	public void setDetector(ImageDetector detector) {
		this.detector = detector;
	}


}
