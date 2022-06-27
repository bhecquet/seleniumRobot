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
package com.seleniumtests.uipage.uielements;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.ScreenshotException;

import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Element which is found inside driver snapshot
 * @author behe
 *
 */
public class UiElement {
	
	/* 
	 * TODO
	 * - faire un reset entre 2 tests
	 */
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(UiElement.class);
	private static Map<String, List<Field>> fieldsPerPage;
	private static Map<String, List<Label>> labelsPerPage;
	
	// coordinates of the top-left corner of the viewport in the screen
	private static Map<String, Point> offsetPerPage;
	
	static {
		resetPageInformation();
	}
	
	private long actionDuration;
	private ElementType elementType;
	private ScreenshotUtil screenshotUtil;
	private ByUI by;
	private boolean resetSearch;
    private String origin  = null;
	protected Clock clock = Clock.systemUTC();

	protected Rectangle detectedObjectRectangle;

	public UiElement() {
		// for mocks
	}
	
	public UiElement(ByUI by) {
		this(by, false);
	}
	
	/**
	 * 
	 * @param by			search criteria
	 * @param resetSearch	if true, a new capture will be taken to refresh screen (in case GUI changed on this page)
	 */
	public UiElement(ByUI by, boolean resetSearch) {
		this.by = by;
		this.elementType = by.getType();
		
		origin = PageObject.getCallingPage(Thread.currentThread().getStackTrace());
		this.resetSearch = resetSearch;
	}

	/**
	 * Reset any information for any page. Mainly used for tests
	 */
	public static void resetPageInformation() {
		fieldsPerPage = Collections.synchronizedMap(new HashMap<>());
		labelsPerPage = Collections.synchronizedMap(new HashMap<>());
		offsetPerPage = Collections.synchronizedMap(new HashMap<>());
	}
	
	/**
	 * remove information relative to a specific page
	 * @param pageName
	 */
	private void resetPageInformation(String pageName) {
		fieldsPerPage.remove(pageName);
		labelsPerPage.remove(pageName);
		offsetPerPage.remove(pageName);
	}
	
	public ScreenshotUtil getScreenshotUtil() {
		return new ScreenshotUtil();
	}
	
	public ImageFieldDetector getImageFieldDetector(File screenshotFile) {
		return new ImageFieldDetector(screenshotFile);
	}
	
	/**
	 * Check search criteria has the required information
	 */
	private void checkElementToSearch() {
		if (by.getType() == null) {
			throw new ConfigurationException("Element type is mandatory to search a field");
		} else if (by.getLeftOf() == null && by.getRightOf() == null && by.getAbove() == null && by.getBelow() == null && by.getText() == null) {
			throw new ConfigurationException("At least one of 'above', 'below', 'rightOf', 'leftOf', 'text' must be defined");
		}
	}

	public void findElement() {

		LocalDateTime start = LocalDateTime.now();
		
		checkElementToSearch();
		
		if (resetSearch) {
			resetPageInformation(origin);
		}
		
		// search fields if we do not have one for this page
		if (!fieldsPerPage.containsKey(origin)) {
			File screenshotFile = getScreenshotFile();
			if (screenshotFile == null) {
				throw new ScreenshotException("Screenshot does not exist");
			}
			
			// TODO: handle other cases than browser
			// try to find viewport position so that we can match a position on browser capture with the same position on screen
			// we assume that browser is started on main screen in a multi-screen environment
			Rectangle viewportPosition = detectViewPortPosition(screenshotFile);
			offsetPerPage.put(origin, new Point(viewportPosition.x, viewportPosition.y));
			
			ImageFieldDetector detector = getImageFieldDetector(screenshotFile);
			List<Field> fields = detector.detectFields();
			for (Field field: fields) {
				field.changePosition(viewportPosition.x, viewportPosition.y);
			}
			fieldsPerPage.put(origin, fields);
			
			List<Label> labels = detector.detectLabels();
			for (Label lbl: labels) {
				lbl.changePosition(viewportPosition.x, viewportPosition.y);
			}
			labelsPerPage.put(origin, labels);
		}
		
		findElementByPosition();
		
		actionDuration = Duration.between(start, LocalDateTime.now()).toMillis();
		
	}
	
	private void findElementByPosition() {

		// search the fields with label
		Label labelRightOf = null;
		Label labelLeftOf = null;
		Label labelAbove = null;
		Label labelBelow = null;
		Label labelText = null;
		if (by.getRightOf() != null) {
			for (Label lbl: labelsPerPage.get(origin)) {
				if (by.getRightOf().matcher(lbl.getText().trim()).matches()) {
					labelRightOf = lbl;
					break;
				}
			}
		}
		if (by.getLeftOf() != null) {
			for (Label lbl: labelsPerPage.get(origin)) {
				if (by.getLeftOf().matcher(lbl.getText().trim()).matches()) {
					labelLeftOf = lbl;
					break;
				}
			}
		}
		if (by.getAbove() != null) {
			for (Label lbl: labelsPerPage.get(origin)) {
				if (by.getAbove().matcher(lbl.getText().trim()).matches()) {
					labelAbove = lbl;
					break;
				}
			}
		}
		if (by.getBelow() != null) {
			for (Label lbl: labelsPerPage.get(origin)) {
				if (by.getBelow().matcher(lbl.getText().trim()).matches()) {
					labelBelow = lbl;
					break;
				}
			}
		}
		if (by.getText() != null) {
			for (Label lbl: labelsPerPage.get(origin)) {
				if (by.getText().matcher(lbl.getText().trim()).matches()) {
					labelText = lbl;
					break;
				}
			}
		}
		
		if (labelLeftOf == null && labelRightOf == null && labelText == null && labelAbove == null && labelBelow == null) {
			throw new ConfigurationException(String.format("No label could be found matching search criteria [%s]", by));
		}
		
		
		for (Field field: fieldsPerPage.get(origin)) {

			if (ElementType.fromClassName(field.getClassName()) == elementType
					&& field.getRelatedField() == null // only use raw fields
					&& (labelText == null || labelText.isInside(field)) 
					&& (labelRightOf == null || labelRightOf.isFieldRightOf(field))
					&& (labelLeftOf == null || labelLeftOf.isFieldLeftOf(field))
					&& (labelAbove == null || labelAbove.isFieldAbove(field))
					&& (labelBelow == null || labelBelow.isFieldBelow(field))
					) {
				detectedObjectRectangle = field.getRectangle();
				return;
			} 
		
		}
		throw new ConfigurationException(String.format("No field could be found matching search criteria [%s]", by));
	}
	
	/**
	 * gets the position of the browser viewport compared to screen
	 * @return
	 */
	private Point getViewportOffset() {
		return offsetPerPage.get(origin);
	}
	
	private Rectangle detectViewPortPosition(File screenshotFile) {
		BufferedImage image;
		try {
			image = ImageProcessor.loadFromFile(screenshotFile);
		
			BufferedImage croppedImage = ImageProcessor.cropImage(image, 0, 0, image.getWidth(), 50);
			File cropScreenshotFile = File.createTempFile("img", ".png");
			ImageIO.write(croppedImage, "png", cropScreenshotFile);
			
			File desktopScreenshotFile = getDesktopScreenshotFile();
			if (desktopScreenshotFile == null) {
				throw new ScreenshotException("Desktop screenshot does not exist");
			}
			
			ImageDetector imageDetector = new ImageDetector(desktopScreenshotFile, cropScreenshotFile, 0.2);
			imageDetector.detectExactZoneWithoutScale();
			org.openqa.selenium.Rectangle detectedRectangle = imageDetector.getDetectedRectangle();
			return new Rectangle(detectedRectangle.x, detectedRectangle.y, detectedRectangle.height, detectedRectangle.width);
			
		} catch (IOException e) {
			throw new ScreenshotException("Error getting position of viewport: " + e.getMessage());
		}
	}

	public File getScreenshotFile() {
		screenshotUtil = getScreenshotUtil(); // update driver
		
		return screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true);		
	}
	
	public File getDesktopScreenshotFile() {
		screenshotUtil = getScreenshotUtil(); // update driver
		
		return screenshotUtil.capture(SnapshotTarget.MAIN_SCREEN, File.class, true);		
	}

	public CustomEventFiringWebDriver getDriver() {
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		if (uiDriver == null) {
			throw new ScenarioException("Driver has not already been created");
		}
		CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)uiDriver.getDriver();
    	if (driver == null) {
    		throw new ScenarioException("Driver has not already been created");
    	}
    	return driver;
	}

	public void click() {
		clickAt(0, 0);
	}
	
	
	@ReplayOnError(waitAfterAction = true)
	public void clickAt(int xOffset, int yOffset) {
		Point relativePoint = findElementPosition(xOffset, yOffset);
		
		CustomEventFiringWebDriver.leftClicOnDesktopAt(true, relativePoint.x, relativePoint.y, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());

	}
	
	/**
	 * Find the element
	 * Scroll to it
	 * Compute mouse position to be able to click on it
	 * 
	 * @param xOffset	offset from the center of the element to click on 
	 * @param yOffset	offset from the center of the element to click on 
	 * @return
	 */
	private Point findElementPosition(int xOffset, int yOffset) {
		findElement();

		// as field and label positions has been modified to be relative to screen and not to viewport, when scrolling in viewport, we must take into account viewport position
		getDriver().scrollTo(detectedObjectRectangle.x - getViewportOffset().x, detectedObjectRectangle.y - getViewportOffset().y);
		Point scrollPosition = getDriver().getScrollPosition();

		// TODO: handle negative offset when scrolling
		int relativeX = detectedObjectRectangle.x - scrollPosition.x + detectedObjectRectangle.width / 2 + xOffset;
		int relativeY = detectedObjectRectangle.y - scrollPosition.y + detectedObjectRectangle.height / 2 + yOffset;
		
		return new Point(relativeX, relativeY);
	}
	
	public void doubleClick() {
		doubleClickAt(0, 0);
	}
	
	@ReplayOnError(waitAfterAction = true)
	public void doubleClickAt(int xOffset, int yOffset) {
		Point relativePoint = findElementPosition(xOffset, yOffset);
		
		CustomEventFiringWebDriver.doubleClickOnDesktopAt(true, relativePoint.x, relativePoint.y, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	public void rightClick() {
		rightClickAt(0, 0);
	}
	
	@ReplayOnError(waitAfterAction = true)
	public void rightClickAt(int xOffset, int yOffset) {
		Point relativePoint = findElementPosition(xOffset, yOffset);
		
		CustomEventFiringWebDriver.rightClicOnDesktopAt(true, relativePoint.x, relativePoint.y, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	/**
	 * Send text to desktop using keyboard at xOffset, yOffset. Before sending keys, we robot clicks on position to gain focus
	 * @param xOffset	
	 * @param yOffset
	 * @param text		Text to write
	 */
	public void sendKeys(CharSequence text) {
		sendKeys(0, 0, text);
	}
	public void sendKeys(int xOffset, int yOffset, final CharSequence text) {
		clickAt(xOffset, yOffset);
		
		CustomEventFiringWebDriver.writeToDesktop(text.toString(), 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	/**
	 * Example of use: page.zone.sendKeys(0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
	 * Beware of key mapping which may be different depending on locale and keyboard. Use this to send control keys like "VK_ENTER"
	 * @param xOffset
	 * @param yOffset
	 * @param events	Key events to send
	 */
	public void sendKeys(int xOffset, int yOffset, Integer ... events) {
		clickAt(xOffset, yOffset);
		
		CustomEventFiringWebDriver.sendKeysToDesktop( 
				Arrays.asList(events),
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	/**
	 * Example of use: page.zone.sendKeys(KeyEvent.VK_A, KeyEvent.VK_B);
	 * Beware of key mapping which may be different depending on locale and keyboard. Use this to send control keys like "VK_ENTER"
	 * Keys will be typed anywhere where focus is
	 * @param events	Key events to send
	 */
	public void sendKeysNoFocus(Integer ... events) {
		CustomEventFiringWebDriver.sendKeysToDesktop( 
				Arrays.asList(events),
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
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
			} catch (ImageSearchException | ConfigurationException e) {
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
		return String.format("Element located by %s", by);
	}

	public Rectangle getDetectedObjectRectangle() {
		return detectedObjectRectangle;
	}

	public long getActionDuration() {
		return actionDuration;
	}

	public void setActionDuration(long actionDuration) {
		this.actionDuration = actionDuration;
	}

	public static Map<String, List<Field>> getFieldsPerPage() {
		return fieldsPerPage;
	}

	public static Map<String, List<Label>> getLabelsPerPage() {
		return labelsPerPage;
	}

	public static Map<String, Point> getOffsetPerPage() {
		return offsetPerPage;
	}
}
