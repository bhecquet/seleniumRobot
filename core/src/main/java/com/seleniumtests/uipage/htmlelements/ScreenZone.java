package com.seleniumtests.uipage.htmlelements;

import java.io.File;
import java.util.Arrays;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.ReplayOnError;

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
	
	public ScreenZone(String label, String resourcePath, double detectionThreshold) {
		this(label, createFileFromResource(resourcePath), detectionThreshold);
		this.resourcePath = resourcePath;
	}
	
	public ScreenZone(String label, File pictureFile) {
		this(label, pictureFile, 0.1);
	}
	
	public ScreenZone(String label, File pictureFile, double detectionThreshold) {		
		super(label, pictureFile, detectionThreshold, true, new ScreenshotUtil(null));
	}
	
	/**
	 * Search the picture in the screenshot taken by Robot
	 * 
	 */
	public void findElement() {
		screenshotUtil = new ScreenshotUtil(null); // keep this for unit tests

		// issue #136: we don't need driver when checking desktop
		File screenshotFile = screenshotUtil.captureDesktopToFile();
		super.findElement(screenshotFile);
	}

	
	/**
	 * Click at the coordinates xOffset, yOffset of the center of the found picture. Use negative offset to click on the left or
	 * top of the picture center
	 * In case the size ratio between searched picture and found picture is not 1, then, offset is
	 * the source offset so that it's compatible with any screen size and resolution
	 */
	@ReplayOnError
	public void clickAt(int xOffset, int yOffset) {
		findElement();

		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2;

		moveAndLeftClick(relativeX + (int)(xOffset * pictureSizeRatio), relativeY + (int)(yOffset * pictureSizeRatio));
	}
	
	@ReplayOnError
	public void rightClickAt(int xOffset, int yOffset) {
		findElement();
		
		int relativeX = detectedObjectRectangle.x + detectedObjectRectangle.width / 2;
		int relativeY = detectedObjectRectangle.y + detectedObjectRectangle.height / 2;
		
		moveAndRightClick(relativeX + (int)(xOffset * pictureSizeRatio), relativeY + (int)(yOffset * pictureSizeRatio));
	}
	
    public void swipe(int xMove, int yMove) {
		throw new ScenarioException("swipe is not supported for desktop capture");
	}
	
    public void tap() {
		throw new ScenarioException("tap is not supported for desktop capture");
	}
	
	public void moveAndLeftClick(int coordX, int coordY) {
		CustomEventFiringWebDriver.leftClicOnDesktopAt(coordX, coordY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
		
	}
	
	public void moveAndRightClick(int coordX, int coordY) {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(coordX, coordY, 
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
		
	}
	
	public void sendKeys(final CharSequence text, int xOffset, int yOffset) {
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
	 * @param events
	 */
	public void sendKeys(int xOffset, int yOffset, Integer ... events) {
		clickAt(xOffset, yOffset);
		
		CustomEventFiringWebDriver.sendKeysToDesktop( 
				Arrays.asList(events),
				SeleniumTestsContextManager.getThreadContext().getRunMode(), 
				SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
	}
}
