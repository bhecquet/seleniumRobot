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
import java.util.Arrays;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.ReplayOnError;

/**
 * Defines a zone on desktop where we should search for a control to interract with
 * @author s047432
 *
 */
public class ScreenZone extends GenericPictureElement {

	
	public ScreenZone() {
		// for mocks
	}
	
	public ScreenZone(String label) {
		this(label, (File)null, 0.1);
	}
	
	public ScreenZone(String label, String resourcePath) {
		this(label, resourcePath, 0.1);
	}
	
	/**
	 * 
	 * @param label					any text
	 * @param resourcePath			path (from resources folder) where to retrieve image to search
	 * @param detectionThreshold	sensibility of detection between 0 and 1. 0 means very strict, 1 means it will find matching for everything. Default is 0.1
	 */
	public ScreenZone(String label, String resourcePath, double detectionThreshold) {
		this(label, createFileFromResource(resourcePath), detectionThreshold);
		this.resourcePath = resourcePath;
	}
	
	public ScreenZone(String label, File pictureFile) {
		this(label, pictureFile, 0.1);
	}
	
	public ScreenZone(String label, File pictureFile, double detectionThreshold) {		
		super(label, pictureFile, detectionThreshold, true);
	}
	
	public ScreenshotUtil getScreenshotUtil() {
		return new ScreenshotUtil(null);
	}
	
	/**
	 * Search the picture in the screenshot taken by Robot
	 * 
	 */
	protected File getScreenshotFile() {
		screenshotUtil = getScreenshotUtil(); // keep this for unit tests

		// issue #136: we don't need driver when checking desktop
		return screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true);
	}
	
	protected void doAfterPictureSearch() {
		// nothing to do
	}

	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	@ReplayOnError(waitAfterAction = true)
	public void clickAt(int xOffset, int yOffset) {
		clickAt(false, xOffset, yOffset);
	}
	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution.
	 * Set the mainScreen parameter to true if you want to force the click on the main screen instead of the top-left one.
	 */
	@ReplayOnError(waitAfterAction = true)
	public void clickAt(boolean mainScreen, int xOffset, int yOffset) {
		findElement();

		int relativeX = getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2;
		int relativeY = getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2;

		moveAndLeftClick(mainScreen, relativeX + (int)(xOffset * getPictureSizeRatio()), relativeY + (int)(yOffset * getPictureSizeRatio()));
	}
	
	@ReplayOnError(waitAfterAction = true)
	public void doubleClickAt(int xOffset, int yOffset) {
		doubleClickAt(false, xOffset, yOffset);
	}
	
	@ReplayOnError(waitAfterAction = true)
	public void doubleClickAt(boolean mainScreen, int xOffset, int yOffset) {
		findElement();
		
		int relativeX = getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2;
		int relativeY = getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2;
		
		moveAndDoubleClick(mainScreen, relativeX + (int)(xOffset * getPictureSizeRatio()), relativeY + (int)(yOffset * getPictureSizeRatio()));
	}
	
	@ReplayOnError(waitAfterAction = true)
	public void rightClickAt(int xOffset, int yOffset) {
		rightClickAt(false, xOffset, yOffset);
	}
	
	@ReplayOnError(waitAfterAction = true)
	public void rightClickAt(boolean mainScreen, int xOffset, int yOffset) {
		findElement();
		
		int relativeX = getDetectedObjectRectangle().x + getDetectedObjectRectangle().width / 2;
		int relativeY = getDetectedObjectRectangle().y + getDetectedObjectRectangle().height / 2;
		
		moveAndRightClick(mainScreen, relativeX + (int)(xOffset * getPictureSizeRatio()), relativeY + (int)(yOffset * getPictureSizeRatio()));
	}
	
    public void swipe(int xMove, int yMove) {
		throw new ScenarioException("swipe is not supported for desktop capture");
	}
	
    public void tap() {
		throw new ScenarioException("tap is not supported for desktop capture");
	}
	
    public void moveAndLeftClick(int coordX, int coordY) {
    	moveAndLeftClick(false, coordX, coordY);
    }
    
	public void moveAndLeftClick(boolean mainScreen, int coordX, int coordY) {
		CustomEventFiringWebDriver.leftClicOnDesktopAt(mainScreen, coordX, coordY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	public void moveAndDoubleClick(int coordX, int coordY) {
		moveAndDoubleClick(false, coordX, coordY);
	}
	
	public void moveAndDoubleClick(boolean mainScreen, int coordX, int coordY) {
		CustomEventFiringWebDriver.doubleClickOnDesktopAt(mainScreen, coordX, coordY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	public void moveAndRightClick(int coordX, int coordY) {
		moveAndRightClick(false, coordX, coordY);
	}
	
	public void moveAndRightClick(boolean mainScreen, int coordX, int coordY) {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(mainScreen, coordX, coordY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	/**
	 * @deprecated Replaced by sendKeys(int xOffset, int yOffset, final CharSequence text)
	 * 
	 */
	@Deprecated
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
		clickAt(xOffset, yOffset);
		
		CustomEventFiringWebDriver.writeToDesktop(text.toString(), 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
	
	/**
	 * Send text to desktop using keyboard at xOffset, yOffset. Before sending keys, we robot clicks on position to gain focus
	 * @param xOffset	
	 * @param yOffset
	 * @param text		Text to write
	 */
	public void sendKeys(int xOffset, int yOffset, final CharSequence text) {
		sendKeys(false, xOffset, yOffset, text);
	}
	
	/**
	 * Send text to desktop using keyboard at xOffset, yOffset. Before sending keys, we robot clicks on position to gain focus
	 * @param mainScreen true if you want to force the action to be on the main screen, false to stay on the top left screen
	 * @param xOffset	
	 * @param yOffset
	 * @param text		Text to write
	 */
	public void sendKeys(boolean mainScreen, int xOffset, int yOffset, final CharSequence text) {
		clickAt(mainScreen, xOffset, yOffset);
		
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
		sendKeys(false, xOffset, yOffset, events);
	}
	
	/**
	 * Example of use: page.zone.sendKeys(0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
	 * Beware of key mapping which may be different depending on locale and keyboard. Use this to send control keys like "VK_ENTER"
	 * @param mainScreen true if you want to force the action to be on the main screen, false to stay on the top left screen
	 * @param xOffset
	 * @param yOffset
	 * @param events	Key events to send 
	 */
	public void sendKeys(boolean mainScreen, int xOffset, int yOffset, Integer ... events) {
		clickAt(mainScreen, xOffset, yOffset);
		
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
}
