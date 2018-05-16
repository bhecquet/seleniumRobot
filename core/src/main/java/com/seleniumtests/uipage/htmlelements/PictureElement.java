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
import java.time.Duration;

import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;

import io.appium.java_client.AppiumDriver;
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

	public PictureElement() {
		// for mocks
	}
	
	public PictureElement(String label, String resourcePath, HtmlElement intoElement) {
		this(label, resourcePath, intoElement, 0.1);
	}

	public PictureElement(String label, String resourcePath, HtmlElement intoElement, double detectionThreshold) {
		this(label, createFileFromResource(resourcePath), intoElement, detectionThreshold);
		this.resourcePath = resourcePath;
	}
	
	public PictureElement(String label, File pictureFile, HtmlElement intoElement) {
		this(label, pictureFile, intoElement, 0.1);
	}
	
	/**
	 * 
	 * @param label
	 * @param pictureFile
	 * @param intoElement	HtmlElement inside of which our picture is. It allows scrolling to the zone where 
	 * 						picture is searched before doing capture
	 */
	public PictureElement(String label, File pictureFile, HtmlElement intoElement, double detectionThreshold) {		
		if (intoElement == null) {
			if (SeleniumTestsContextManager.isWebTest()) {
				this.intoElement = new HtmlElement("", By.tagName("body"));
			} else {
				this.intoElement = new HtmlElement("", By.xpath("/*"));
			}
		} else {
			this.intoElement = intoElement;
		}
		
		detector = new ImageDetector();
		detector.setDetectionThreshold(detectionThreshold);
		setObjectPictureFile(pictureFile);
		screenshotUtil = new ScreenshotUtil();
		
		driver = (EventFiringWebDriver)WebUIDriver.getWebDriver();
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
		detector.detectExactZoneWithScale();
		detectedObjectRectangle = detector.getDetectedRectangle();
		pictureSizeRatio = detector.getSizeRatio();
		
		// scroll to element where our picture is so that we will be able to act on it
		// scrolling will display, on top of window, the top of the element
		if (intoElement != null) {
			intoElement.scrollToElement(0);
		}
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

		Point intoElementPos = intoElement.getCoordinates().onPage();
		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2 - intoElementPos.x;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2 - intoElementPos.y;
//		System.out.println(intoElementPos);
//		System.out.println(relativeX);
//		System.out.println(relativeY);
//		System.out.println(detectedObjectRectangle.getPoint());
//		System.out.println(detectedObjectRectangle.getDimension());
		moveAndClick(intoElement, relativeX + (int)(xOffset * pictureSizeRatio), relativeY + (int)(yOffset * pictureSizeRatio));
	}
	
	@ReplayOnError
    public void swipe(int xMove, int yMove) {
		findElement(true);
		
		int xInit = detectedObjectRectangle.x + detectedObjectRectangle.width / 2;
		int yInit = detectedObjectRectangle.y + detectedObjectRectangle.height / 2;
		
		new TouchAction(getMobileDriver()).press(xInit, yInit)
			.waitAction(Duration.ofMillis(500))
			.moveTo(xInit + xMove, yInit + yMove)
			.release()
			.perform();
	}
	
	@ReplayOnError
    public void tap() {
		findElement(true);
		new TouchAction(getMobileDriver()).tap(detectedObjectRectangle.x + detectedObjectRectangle.width / 2, detectedObjectRectangle.y + detectedObjectRectangle.height / 2).perform();
	}
	
	public void moveAndClick(WebElement element, int coordX, int coordY) {
		// issue #133: handle new actions specific case
		// more browsers will be added to this conditions once they are migrated to new composite actions
		// 
		if (SeleniumTestsContextManager.isWebTest() && SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			coordX -= element.getSize().width / 2;
			coordY -= element.getSize().height / 2;
		}
		
		new Actions(driver).moveToElement(element, coordX, coordY).click().build().perform();
	}
	
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
		clickAt(xOffset, yOffset);
		
		new Actions(driver).sendKeys(text).build().perform();
	}

	public void sendKeys(final CharSequence text) {
		sendKeys(text, 0, 0);
	}
	
	public boolean isElementPresent() {
		return isElementPresent(0);
	}
	
	/**
	 * check whether picture is present or not on browser or on application (for mobile) as it uses driver capture
	 * @param waitMs
	 * @return
	 */
	public boolean isElementPresent(int waitMs) {
		return isElementPresent(waitMs, true);
	}
	
	/**
	 * Check if picture is visible on desktop. This is only available for desktop tests
	 * @param waitMs
	 * @return
	 */
	public boolean isElementPresentOnDesktop(int waitMs) {
		return isElementPresent(waitMs, false);
	}
	public boolean isElementPresentOnDesktop() {
		return isElementPresent(0, false);
	}
	
	private boolean isElementPresent(int waitMs, boolean captureByDriver) {
		long end = clock.laterBy(waitMs);
		while (clock.isNowBefore(end) || waitMs == 0) {
			try {
				findElement(captureByDriver);
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
	
	private AppiumDriver<?> getMobileDriver() {
		if (!(((CustomEventFiringWebDriver)driver).getWebDriver() instanceof AppiumDriver<?>)) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
		return (AppiumDriver<?>)((CustomEventFiringWebDriver)driver).getWebDriver();
	}
	
	// TODO: actions for mobile
	// https://discuss.appium.io/t/tapping-on-the-screen-by-using-coordinates/5529/7
	
}
