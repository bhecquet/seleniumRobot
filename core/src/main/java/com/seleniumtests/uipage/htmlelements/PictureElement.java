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
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.util.helper.WaitHelper;

import io.appium.java_client.touch.TapOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;

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
		super(label, pictureFile, detectionThreshold, false);
		
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
	@Override
	@Deprecated
	public void findElement(boolean searchOnly) {
		findElement();
	}
	public File getScreenshotFile() {
		screenshotUtil = getScreenshotUtil(); // update driver
		
		return screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true);		
	}
	
	/**
	 * Take device aspect ratio / zoom into account when storing found rectangle so that click can be done at the right place
	 */
	@Override
	public void setDetectedObjectRectangleAndAspectRatio(Rectangle detectedRectangle, double sizeRatio) {

		WebUIDriver uiDriver = isDriverCreated();
		double pixelAspectRatio = ((CustomEventFiringWebDriver)uiDriver.getDriver()).getDeviceAspectRatio();
		
		// take into account the aspect ratio
		Rectangle updatedRectangle = new Rectangle((int)(detectedRectangle.x / pixelAspectRatio),
				(int)(detectedRectangle.y / pixelAspectRatio),
				(int)(detectedRectangle.height / pixelAspectRatio),
				(int)(detectedRectangle.width / pixelAspectRatio));
		sizeRatio = sizeRatio / pixelAspectRatio;
		
		super.setDetectedObjectRectangleAndAspectRatio(updatedRectangle, sizeRatio);
	}
	
	protected void doAfterPictureSearch() {
		// scroll to element where our picture is so that we will be able to act on it
		// scrolling will display, on top of window, the top of the element
		intoElement.scrollToElement(0);
		WaitHelper.waitForMilliSeconds(500);
		
	}

	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	@ReplayOnError(replayDelayMs=1000, waitAfterAction = true)
	public void clickAt(int xOffset, int yOffset) {
		findElement();

		Point intoElementPos = intoElement.getCoordinates().onPage();
		int relativeX = getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2 - intoElementPos.x;
		int relativeY = getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2 - intoElementPos.y;

		moveAndClick(intoElement, relativeX + (int)(xOffset * getPictureSizeRatio()), relativeY + (int)(yOffset * getPictureSizeRatio()));
	}
	
	@ReplayOnError(replayDelayMs=1000, waitAfterAction = true)
	public void doubleClickAt(int xOffset, int yOffset) {
		findElement();
		
		Point intoElementPos = intoElement.getCoordinates().onPage();
		int relativeX = getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2 - intoElementPos.x;
		int relativeY = getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2 - intoElementPos.y;
		
		moveAndDoubleClick(intoElement, relativeX + (int)(xOffset * getPictureSizeRatio()), relativeY + (int)(yOffset * getPictureSizeRatio()));
	}
	
	@ReplayOnError(replayDelayMs=1000, waitAfterAction = true)
    public void swipe(int xMove, int yMove) {
		findElement();

		int xInit = getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2;
		int yInit = getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2;
		
		createTouchAction().press(PointOption.point(xInit, yInit))
			.waitAction(WaitOptions.waitOptions(Duration.ofMillis(500)))
			.moveTo(PointOption.point(xInit + xMove, yInit + yMove))
			.release()
			.perform();
	}
	
	@ReplayOnError(replayDelayMs=1000, waitAfterAction = true)
    public void tap() {
		findElement();

		createTouchAction()
			.moveTo(PointOption.point(getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2, getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2))
			.tap(TapOptions.tapOptions().withTapsCount(1)).perform();
	}
	
	public void moveAndClick(WebElement element, int coordX, int coordY) {
		move(element, coordX, coordY).pause(200).click().build().perform();
	}
	
	private WebUIDriver isDriverCreated() {
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		if (uiDriver == null) {
			throw new ScenarioException("Driver has not already been created");
		}
		
		CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)uiDriver.getDriver();
    	if (driver == null) {
    		throw new ScenarioException("Driver has not already been created");
    	}
    	return uiDriver;
	}
	
	/**
	 * Move to the center of the picture
	 * @param element	The element to move to
	 * @param coordX	x offset from the center of the element
	 * @param coordY	y offset from the center of the element
	 * @return
	 */
	private Actions move(WebElement element, int coordX, int coordY) {
		
		WebUIDriver uiDriver = isDriverCreated();

		if (SeleniumTestsContextManager.isWebTest()) {
			// issue #133: handle new actions specific case
			// more browsers will be added to this conditions once they are migrated to new composite actions
			if (uiDriver.getConfig().getBrowserType() == BrowserType.FIREFOX) {
				// issue #133: firefox moves to center of element in page
				coordX -= element.getSize().width / 2;
				coordY -= element.getSize().height / 2;
				
			} else if (uiDriver.getConfig().getBrowserType() == BrowserType.INTERNET_EXPLORER
					|| uiDriver.getConfig().getBrowserType() == BrowserType.EDGE
					|| (uiDriver.getConfig().getBrowserType() == BrowserType.CHROME 
						&& uiDriver.getConfig().getMajorBrowserVersion() >= 75)) {
				// issue #180: internet explorer moves to center of element in viewport
				// do not take into accoung pixel aspect ratio, because selenium element coordinates are calculated with zooming (element will always have the same size even if zooming is different)
				Dimension viewportDim = ((CustomEventFiringWebDriver)uiDriver.getDriver()).getViewPortDimensionWithoutScrollbar(false);
				coordX -= Math.min(element.getSize().width, viewportDim.width) / 2;
				coordY -= Math.min(element.getSize().height, viewportDim.height) / 2;
			}
		}
		
		return new Actions(uiDriver.getDriver()).moveToElement(element, coordX, coordY);
	}
	
	public void moveAndDoubleClick(WebElement element, int coordX, int coordY) {
		move(element, coordX, coordY).doubleClick().build().perform();
	}
	
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
		clickAt(xOffset, yOffset);

		new Actions(isDriverCreated().getDriver()).sendKeys(text).build().perform();
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
	
	// TODO: actions for mobile
	// https://discuss.appium.io/t/tapping-on-the-screen-by-using-coordinates/5529/7
	
}
