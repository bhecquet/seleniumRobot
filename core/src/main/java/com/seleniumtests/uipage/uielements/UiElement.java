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
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.ScreenshotException;

import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.uipage.htmlelements.GenericPictureElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.touch.TapOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;

/**
 * Element which is found inside driver snapshot
 * @author behe
 *
 */
public class UiElement {
	
	/* 
	 * TODO
	 * - ne pas refaire de recherche si on en a déjà fait une pour cette page
	 * - faire un reset entre 2 tests
	 */
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(UiElement.class);
	private static FieldDetectorConnector fieldDetectorConnector;
	private static Map<String, List<Field>> fieldsPerPage;
	
	// coordinates of the top-left corner of the viewport in the screen
	private static Map<String, Point> offsetPerPage;
	
	static {
		fieldsPerPage = Collections.synchronizedMap(new HashMap<>());
		offsetPerPage = Collections.synchronizedMap(new HashMap<>());
	}
	
	public enum ElementType {
		
		
		TEXT_FIELD("field_with_label", "field_line_with_label", "field"),
		BUTTON("button"),
		RADIO("radio_with_label", "radio"),
		CHECKBOX("checkbox_with_label", "checkbox"),
		UNKNOWN;
		

		private List<String> classes;
		
		private ElementType(String ... classes) {
			this.classes = Arrays.asList(classes);
		}
		
		/**
		 * Returns the element type from class name
		 * @param className
		 */
		public static ElementType fromClassName(String className) {
			try {
				return ElementType.valueOf(className);
			} catch (IllegalArgumentException ex) {
				for (ElementType type : ElementType.values()) {
			        for (String matcher : type.classes) {
			          if (className.equalsIgnoreCase(matcher)) {
			            return type;
			          }
			        }
			      }
			      return UNKNOWN;
			} 
		}
	}
	
	private HtmlElement intoElement;
	private long actionDuration;
	private ElementType elementType;
	private ScreenshotUtil screenshotUtil;
	private Pattern label;
    private String origin  = null;
	protected Clock clock = Clock.systemUTC();

	protected Rectangle detectedObjectRectangle;

	public UiElement() {
		// for mocks
	}
	


	/**
	 * 
	 * @param label					Text to find
	 * @param elementType			type of element to find
	 */
	public UiElement(String label, ElementType elementType) {
		this(Pattern.compile(label), elementType);
	}
	
	public UiElement(Pattern pattern, ElementType elementType) {		
		this.label = pattern;
		this.elementType = elementType;
		
		if (SeleniumTestsContextManager.isWebTest()) {
			this.intoElement = new HtmlElement("", By.tagName("body"));
		} else {
			this.intoElement = new HtmlElement("", By.xpath("/*"));
		}
		origin = PageObject.getCallingPage(Thread.currentThread().getStackTrace());
		
	}
	
	private static FieldDetectorConnector getInstance() {
		if (fieldDetectorConnector == null) {
			fieldDetectorConnector = new FieldDetectorConnector(SeleniumTestsContextManager.getThreadContext().getImageFieldDetectorServerUrl());
		}
		return fieldDetectorConnector;
	}
	
	public ScreenshotUtil getScreenshotUtil() {
		return new ScreenshotUtil();
	}
	

	public void findElement() {
		FieldDetectorConnector detector = getInstance();

		LocalDateTime start = LocalDateTime.now();
	
		
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
			
			List<Field> fields = detector.detect(screenshotFile);
			for (Field field: fields) {
				field.changePosition(viewportPosition.x, viewportPosition.y);
			}
			fieldsPerPage.put(origin, fields);
		}
		
		for (Field field: fieldsPerPage.get(origin)) {
			if (field.getText() == null || field.getClassName() == null) {
				continue;
			} else if (ElementType.fromClassName(field.getClassName()) == elementType && this.label.matcher(field.getText().trim()).matches()) {
				if (field.getRelatedField() != null) {
					detectedObjectRectangle = field.getRelatedField().getRectangle();
				} else {
					detectedObjectRectangle = field.getRectangle();
				}
			}
		}
		
		actionDuration = Duration.between(start, LocalDateTime.now()).toMillis();
		
		doAfterPictureSearch();
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
			return new Rectangle(detectedRectangle.x, detectedRectangle.y, detectedRectangle.width, detectedRectangle.height);
			
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
	
	protected void doAfterPictureSearch() {
		// scroll to element where our picture is so that we will be able to act on it
		// scrolling will display, on top of window, the top of the element
		intoElement.scrollToElement(0);
		WaitHelper.waitForMilliSeconds(500);
	}
	
	private CustomEventFiringWebDriver getDriver() {
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

	
	@ReplayOnError
	public void clickAt(int xOffset, int yOffset) {
		findElement();

		getDriver().scrollTo(detectedObjectRectangle.x, detectedObjectRectangle.y);
		Point scrollPosition = getDriver().getScrollPosition();

		int relativeX = detectedObjectRectangle.x + getViewportOffset().x - scrollPosition.x + detectedObjectRectangle.width / 2;
		int relativeY = detectedObjectRectangle.y + getViewportOffset().y - scrollPosition.y + detectedObjectRectangle.height / 2;
		
		CustomEventFiringWebDriver.leftClicOnDesktopAt(true, relativeX, relativeY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());

	}
	
	@ReplayOnError
	public void doubleClickAt(int xOffset, int yOffset) {
		findElement();
		
		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2;
		
		moveAndDoubleClick(relativeX + (int)(xOffset), relativeY + (int)(yOffset));
	}
	
	@ReplayOnError
	public void rightClickAt(int xOffset, int yOffset) {
		findElement();
		
		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2;
		
		moveAndRightClick(relativeX + (int)(xOffset), relativeY + (int)(yOffset));
	}
	
    public void swipe(int xMove, int yMove) {
		throw new ScenarioException("swipe is not supported for desktop capture");
	}
	
    public void tap() {
		throw new ScenarioException("tap is not supported for desktop capture");
	}
	
	public void moveAndDoubleClick(int coordX, int coordY) {
		CustomEventFiringWebDriver.doubleClickOnDesktopAt(coordX, coordY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	public void moveAndRightClick(int coordX, int coordY) {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(coordX, coordY, 
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
		return String.format("%s labeled '%s'", elementType, label);
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
}
