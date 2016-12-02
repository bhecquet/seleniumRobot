package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.util.imaging.ImageDetector;

public class TestPictureElement extends MockitoTest {
	
	@Mock
	ImageDetector imageDetector;
	
	@Mock
	ScreenshotUtil screenshotUtil;

	@InjectMocks
	PictureElement pictureElement = new PictureElement();

	@Test(groups={"ut"})
	public void testRobotUsable() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		pictureElement.setObjectPictureFile(new File("tu/images/logo_text_field.png"));
		Assert.assertTrue(pictureElement.isRobotUsable());
	}
	
	@Test(groups={"ut"})
	public void testRobotNotUsableInMobile() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		pictureElement.setObjectPictureFile(new File("tu/images/logo_text_field.png"));
		Assert.assertFalse(pictureElement.isRobotUsable());
	}
	
	@Test(groups={"ut"})
	public void testRobotNotUsableInRemote() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		pictureElement.setObjectPictureFile(new File("tu/images/logo_text_field.png"));
		Assert.assertFalse(pictureElement.isRobotUsable());
	}
	
	/**
	 * Without configured robot, click is forbidden
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClickWithoutRobot() {
		pictureElement.setObjectPictureFile(new File(""));
		pictureElement.setRobot(null);
		pictureElement.click();
	}
	
	/**
	 * With configured robot, but without screenshot retrieved, error is raised
	 * @throws AWTException 
	 */
	@Test(groups={"ut"}, expectedExceptions=WebDriverException.class)
	public void testClickWithRobot() throws AWTException {
		pictureElement.setObjectPictureFile(new File(""));
		pictureElement.setRobot(new Robot());
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		pictureElement.click();
	}
	
	/**
	 * With configured robot, but without screenshot retrieved, error is raised
	 * @throws AWTException 
	 */
	@Test(groups={"ut"})
	public void testClick() throws AWTException {
		pictureElement.setObjectPictureFile(new File(""));
		Robot robot = Mockito.mock(Robot.class);
		pictureElement.setRobot(robot);
		when(screenshotUtil.captureDesktopToFile()).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(1.0);
		pictureElement.click();
		
		verify(robot).mouseMove(35, 60);
		verify(robot).mousePress(InputEvent.BUTTON1_MASK);
		verify(robot).mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	/**
	 * Click at 10, 10 from the center of the detected image, with different Aspect ratio
	 * Here, A/R is 2.0, so offset is multiplied by 2 so that the relative position between
	 * picture center in object and in scene is the same
	 * @throws AWTException 
	 */
	@Test(groups={"ut"})
	public void testClickAt() throws AWTException {
		pictureElement.setObjectPictureFile(new File(""));
		Robot robot = Mockito.mock(Robot.class);
		pictureElement.setRobot(robot);
		when(screenshotUtil.captureDesktopToFile()).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(2.0);
		pictureElement.clickAt(10, 10);
		
		verify(robot).mouseMove(55, 80);
		verify(robot).mousePress(InputEvent.BUTTON1_MASK);
		verify(robot).mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	@Test(groups={"ut"})
	public void testPictureNotVisible() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		Robot robot = Mockito.mock(Robot.class);
		picElement.setRobot(robot);
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectCorrespondingZone();
		
		Assert.assertFalse(picElement.isElementPresent());
		
		verify(picElement).findElement(true);
		
	}
	
	@Test(groups={"ut"})
	public void testPictureNotVisibleWithReplay() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		Robot robot = Mockito.mock(Robot.class);
		picElement.setRobot(robot);
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectCorrespondingZone();
		
		Assert.assertFalse(picElement.isElementPresent(350));
		
		verify(picElement, times(2)).findElement(true);
		
	}
	
	@Test(groups={"ut"})
	public void testPictureVisible() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		Robot robot = Mockito.mock(Robot.class);
		picElement.setRobot(robot);
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(1.0);
		
		Assert.assertTrue(picElement.isElementPresent(2000));
		verify(picElement).findElement(true);
		
	}
	
	@Test(groups={"ut"})
	public void testSendKeys() throws AWTException {
		pictureElement.setObjectPictureFile(new File(""));
		Robot robot = Mockito.mock(Robot.class);
		pictureElement.setRobot(robot);
		when(screenshotUtil.captureDesktopToFile()).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(600, 300, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(1.0);
		
		pictureElement.sendKeys("Hello _3 £ !");	
		
		verify(robot).keyPress(KeyEvent.VK_H);
		verify(robot).keyPress(KeyEvent.VK_E);
		verify(robot, times(2)).keyPress(KeyEvent.VK_L);
		verify(robot).keyPress(KeyEvent.VK_O);
		verify(robot).keyPress(KeyEvent.VK_3);
		verify(robot).keyPress(KeyEvent.VK_EXCLAMATION_MARK);
		verify(robot).keyPress(KeyEvent.VK_UNDERSCORE);
		verify(robot, times(3)).keyPress(KeyEvent.VK_SPACE);
		verify(robot, times(2)).keyPress(KeyEvent.VK_SHIFT); // 1 for 'H' and 1 for '3'
	}
	
	@AfterMethod(alwaysRun=true)
	public void reset(ITestContext testNGCtx) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
	}
}
