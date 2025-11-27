/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.connector.selenium;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import com.seleniumtests.core.TestTasks;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.uipage.htmlelements.ScreenZone;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.helper.WaitHelper;

public class TestSeleniumRobotGridConnector2 extends GenericMultiBrowserTest {

	private static final String SELENIUM_GRID_URL = "http://10.165.161.49:44444/wd/hub";

	public TestSeleniumRobotGridConnector2() {
		super(BrowserType.CHROME, "DriverTestPageWithoutFixedPattern", SELENIUM_GRID_URL, null);
	}
	
	@BeforeClass(groups={"it"})
	public void initConnector(ITestContext ctx) {

		if (!new SeleniumRobotGridConnector(SELENIUM_GRID_URL).isGridActive()) {
			throw new SkipException("no local seleniumrobot grid available");
		}
	}

	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void reset() {
		if (driver != null) {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			DriverTestPageWithoutFixedPattern.textElement.clear();
			driver.scrollTop();
		}
	}

	@Test(groups={"it"})
	public void testDonwloadFile() {
		DriverTestPageWithoutFixedPattern.downloadPdf.click();
		File pdfFile = TestTasks.getDownloadedFile("nom-du-fichier.pdf");
		Assert.assertTrue(pdfFile.exists());
	}
	
	@Test(groups={"it"})
	public void testClickOnGooglePicture() {
		try {
			DriverTestPageWithoutFixedPattern.googleForDesktop.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Check that "captureSnapshot=false" do not prevent to use ScreenZone
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePictureWithCaptureSnapshotFalse() {
		
		try {
			SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
			DriverTestPageWithoutFixedPattern.googleForDesktop.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		} finally {
			SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(SeleniumTestsContext.DEFAULT_CAPTURE_SNAPSHOT);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Clic at given coordinate on screen without picture reference
	 */
	@Test(groups={"it"})
	public void testClickAtCoordinates() {
		try {
			// search zone to click
			DriverTestPageWithoutFixedPattern.googleForDesktop.findElement();
			Rectangle rectangle = DriverTestPageWithoutFixedPattern.googleForDesktop.getDetectedObjectRectangle();
			
			// clic with a new ScreenZone
			new ScreenZone("image").clickAt(rectangle.x + 10, rectangle.y + 10);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Clic at given coordinate on screen without picture reference
	 */
	@Test(groups={"it"})
	public void testDoubleClickAtCoordinates() {
		try {
			// search zone to click
			DriverTestPageWithoutFixedPattern.googleForDesktop.findElement();
			Rectangle rectangle = DriverTestPageWithoutFixedPattern.googleForDesktop.getDetectedObjectRectangle();
			
			// clic with a new ScreenZone
			new ScreenZone("image").doubleClickAt(rectangle.x + 10, rectangle.y + 10);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "double click image");
	}
	
	/**
	 * test correction of issue #134 by clicking on element defined by a File object
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePictureFromFile() {
		try {
			DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	

	/**
	 * test that an action changed actionDuration value
	 */
	@Test(groups={"it"})
	public void testActionDurationIsLogged() {
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.getActionDuration(), 0);
		try {
			DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.getActionDuration() > 0);
	}
	
	@Test(groups={"it"})
	public void testSendKeysOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			driver.scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys(0, 40, "hello");
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "hello");
	}
	
	@Test(groups={"it"})
	public void testSendKeyboardKeysOnPicture() { 
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			driver.scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys(0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ab");
	}

	@Test(groups={"it"})
	public void testIsVisible() { 
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.googleForDesktop.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testIsNotVisible() {
		Assert.assertFalse(DriverTestPageWithoutFixedPattern.zoneNotPresent.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testClickDiv() {
		try {
			DriverTestPage.redSquare.click();
			Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "coucou");
		} finally {
			DriverTestPage.resetButton.click();
			Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "");
		}
	}

	@Test(groups={"it"})
	public void testClickOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.picture.clickAt(0, -20);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ff logo");
	}

	private String generateCaptureFilePath() {
		return SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "/" + HashCodeGenerator.getRandomHashCode("web") + ".png";
	}
	
	@Test(groups={"it"})
	public void testCaptureElement() {

		try {
			SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(true);
			driver.manage().window().maximize();
			WaitHelper.waitForSeconds(1);
			
			// get cropped picture
			String filePath = generateCaptureFilePath();
			BufferedImage image = new ScreenshotUtil(driver).capture(new SnapshotTarget(DriverTestPageWithoutFixedPattern.table), BufferedImage.class, false, false).get(0);
			FileUtility.writeImage(filePath, image);
			
			Assert.assertTrue(image.getHeight() < 80 && image.getHeight() > 75); // should be 76 be may depend on browser / version
			Assert.assertTrue(image.getWidth() < 75 && image.getWidth() > 70); // should be 71 be may depend on browser / version
		} finally {
			SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		}
	}
	
	
}
