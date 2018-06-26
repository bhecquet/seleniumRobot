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
import java.time.Duration;
import java.time.LocalDateTime;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.ReplayOnError;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;

/**
 * Element which is found inside driver snapshot
 * @author behe
 *
 */
public class PictureElement extends GenericPictureElement {
	
	private HtmlElement intoElement;

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
	 * @param pictureFile			picture to search for in snapshot or on desktop
	 * @param intoElement			HtmlElement inside of which our picture is. It allows scrolling to the zone where 
	 * 								picture is searched before doing capture
	 * @param detectionThreshold	sensitivity of search between 0 and 1. Be default, 0.1. More sensitivity means search can be less accurate, detect unwanted zones
	 */
	public PictureElement(String label, File pictureFile, HtmlElement intoElement, double detectionThreshold) {		
		super(label, pictureFile, detectionThreshold, false, new ScreenshotUtil());
		
		if (intoElement == null) {
			if (SeleniumTestsContextManager.isWebTest()) {
				this.intoElement = new HtmlElement("", By.tagName("body"));
			} else {
				this.intoElement = new HtmlElement("", By.xpath("/*"));
			}
		} else {
			this.intoElement = intoElement;
		}
	}
	
	public ScreenshotUtil getScreenshotUtil() {
		return new ScreenshotUtil();
	}
	
	/**
	 * Search the picture in the screenshot taken by Robot or WebDriver
	 * Robot is used in Desktop mode
	 * WebDriver is used in mobile, because Robot is not available for mobile platforms
	 * 
	 * @param searchOnly
	 * 
	 * @deprecated use findElement instead
	 */
	@Deprecated
	public void findElement(boolean searchOnly) {
		findElement();
	}
	public File getScreenshotFile() {
		screenshotUtil = getScreenshotUtil(); // update driver
		
		File screenshotFile = screenshotUtil.captureWebPageToFile();
		
		return screenshotFile;
		
	}
	
	protected void doAfterPictureSearch() {
		// scroll to element where our picture is so that we will be able to act on it
		// scrolling will display, on top of window, the top of the element
		intoElement.scrollToElement(0);
	}

	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	@ReplayOnError(replayDelayMs=1000)
	public void clickAt(int xOffset, int yOffset) {
		findElement();

		Point intoElementPos = intoElement.getCoordinates().onPage();
		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2 - intoElementPos.x;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2 - intoElementPos.y;

		moveAndClick(intoElement, relativeX + (int)(xOffset * pictureSizeRatio), relativeY + (int)(yOffset * pictureSizeRatio));
	}
	
	@ReplayOnError(replayDelayMs=1000)
    public void swipe(int xMove, int yMove) {
		findElement();

		int xInit = detectedObjectRectangle.x + detectedObjectRectangle.width / 2;
		int yInit = detectedObjectRectangle.y + detectedObjectRectangle.height / 2;
		
		new TouchAction(getMobileDriver()).press(xInit, yInit)
			.waitAction(Duration.ofMillis(500))
			.moveTo(xInit + xMove, yInit + yMove)
			.release()
			.perform();
	}
	
	@ReplayOnError(replayDelayMs=1000)
    public void tap() {
		findElement();

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
		
		new Actions(WebUIDriver.getWebDriver()).moveToElement(element, coordX, coordY).click().build().perform();
	}
	
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
		clickAt(xOffset, yOffset);

		new Actions(WebUIDriver.getWebDriver()).sendKeys(text).build().perform();
	}
	
	/**
	 * Check if picture is visible on desktop. This is only available for desktop tests
	 * @param waitMs
	 * @return
	 * 
	 * @deprecated use isElementPresent instead
	 */
	@Deprecated
	public boolean isElementPresentOnDesktop(int waitMs) {
		return isElementPresent(waitMs);
	}
	
	/**
	 * @deprecated use isElementPresent instead
	 */
	@Deprecated
	public boolean isElementPresentOnDesktop() {
		return isElementPresent(0);
	}
	
	private AppiumDriver<?> getMobileDriver() {
		if (!(((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).getWebDriver() instanceof AppiumDriver<?>)) {
    		throw new ScenarioException("action is available only for mobile platforms");
    	}
		return (AppiumDriver<?>)((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).getWebDriver();
	}
	
	// TODO: actions for mobile
	// https://discuss.appium.io/t/tapping-on-the-screen-by-using-coordinates/5529/7
	
}
