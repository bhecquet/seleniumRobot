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
package com.seleniumtests.it.driver;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverSubTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;

public class TestBrowserSnapshot extends GenericMultiBrowserTest {
	
	
	private final String browserName = "chrome";
	
	public TestBrowserSnapshot(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestBrowserSnapshot() throws Exception {
		super(BrowserType.CHROME, "DriverTestPage");  
	}
	
	@BeforeMethod(groups={"it"})
	public void initDriver(final ITestContext testNGCtx, final ITestResult testResult) throws Exception {
		initThreadContext(testNGCtx, null, testResult);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserName);
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(0); // no cropping at all
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(0); // no cropping at all
//		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://127.0.0.1:4444/wd/hub");
//		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
//		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
//		SeleniumTestsContextManager.getThreadContext().setFirefoxBinary("path to firefox");
		new DriverTestPage(true, testPageUrl); // start displaying page
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	/**
	 * Read the capture file to detect dimension
	 * It assumes that picture background is white and counts the number of white pixels in width and height (first column and first row)
	 * @return
	 * @throws IOException 
	 */
	private Dimension getViewPortDimension(File picture) throws IOException {
		BufferedImage image = ImageIO.read(picture);

		int width = 0;
		int height = 0;
		for (width=0; width < image.getWidth(); width++) {
			Color color = new Color(image.getRGB(width, 0));
			if (!(color.equals(Color.WHITE) || color.equals(Color.YELLOW) || color.equals(Color.ORANGE) || color.equals(Color.GREEN) || color.equals(Color.RED))) {
				break;
			}
		}
		for (height=0; height < image.getHeight(); height++) {
			Color color = new Color(image.getRGB(5, height));
			if (!(color.equals(Color.WHITE) || color.equals(Color.YELLOW) || color.equals(Color.ORANGE) || color.equals(Color.GREEN) || color.equals(Color.RED))) {
				break;
			}
		}
		return new Dimension(width, height);
	}
	
	/**
	 * Test page contains fixed header (yellow) and footer (orange) of 5 pixels height. Detect how many
	 * pixels of these colors are present in picture
	 * Also count red/green pixels which is a line not to remove when cropping
	 * 
	 * /!\ DESKTOP PIXEL ASPECT RATIO MUST BE SET TO 100% so that count is done correctly 
	 * 
	 * @param picture
	 * @return
	 * @throws IOException 
	 */
	private int[] getHeaderAndFooterPixels(File picture) throws IOException {
		BufferedImage image = ImageIO.read(picture);
		
		int topPixels = 0;
		int bottomPixels = 0;
		int securityLine = 0;
		for (int height=0; height < image.getHeight(); height++) {
			Color color = new Color(image.getRGB(0, height));
			if (color.equals(Color.YELLOW)) {
				topPixels++;
			} else if (color.equals(Color.ORANGE)) {
				bottomPixels++;
			} else if (color.equals(Color.RED) || color.equals(Color.GREEN)) {
				securityLine++;
			}
		}
		
		return new int[] {topPixels, bottomPixels, securityLine};
	}
	
	
	private String generateCaptureFilePath() {
		return SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "/" + HashCodeGenerator.getRandomHashCode("web") + ".png";
	}
	
	private void captureSnapshot(String filePath) {
		String b64Img = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, String.class);
		byte[] byteArray = b64Img.getBytes();
		FileUtility.writeImage(filePath, byteArray);
	}
	
	/**
	 * Test if we succeed in removing scrollbars from capture (horizontal and vertical)
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testRemoveScrollbarCapture() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		// get real capture
		String origFilePath = generateCaptureFilePath();
		captureSnapshot(origFilePath);
		Dimension screenshotDim = getViewPortDimension(new File(origFilePath));
		
		// get cropped picture
		BufferedImage image = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, BufferedImage.class);
		
		// check all scrollbars were already removed from screenshot
		Assert.assertEquals(image.getWidth(), screenshotDim.width);
		Assert.assertEquals(image.getHeight(), screenshotDim.height);
	}
	
	/**
	 * Test when no scrollbar is present in capture
	 * @throws Exception 
	 */
	@Test(groups={"it"})
	public void testNoScrollbarCapture() throws Exception {
		
		new DriverSubTestPage(true);
		
		// get real capture
		String origFilePath = generateCaptureFilePath();
		captureSnapshot(origFilePath);
		Dimension screenshotDim = getViewPortDimension(new File(origFilePath));
		
		// get cropped picture
		BufferedImage image = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, BufferedImage.class);
		
		// check all scrollbars where already removed from screenshot
		Assert.assertEquals(image.getWidth(), screenshotDim.width);
		Assert.assertEquals(image.getHeight(), screenshotDim.height);
	}
	
	/**
	 * issue #272: Test taking snapshot inside an iframe when some javascript error occurs. This happens with some website where access to iframe seems denied
	 * We want to get source and title, even if picture is not get
	 * @throws Exception 
	 */
	@Test(groups={"it"})
	public void testSnapshotWithJavascriptErrors() throws Exception {
		
		driver.switchTo().frame(DriverTestPage.iframe.getElement());
		
		// get real capture
		generateCaptureFilePath();

		CustomEventFiringWebDriver mockedDriver = (CustomEventFiringWebDriver) spy(driver);
		ScreenshotUtil screenshotUtil = spy(new ScreenshotUtil(mockedDriver));
		
		doThrow(JavascriptException.class).when(mockedDriver).scrollTop();
		doThrow(JavascriptException.class).when(mockedDriver).scrollTo(anyInt(), anyInt());
		
		ScreenShot screenshot = screenshotUtil.capture(SnapshotTarget.PAGE, ScreenShot.class);
		Assert.assertNotNull(screenshot.getHtmlSourcePath());
		Assert.assertNotNull(screenshot.getFullImagePath());
	}
	
	/**
	 * Test that if an unexpected error occurs when snapshot is taken, take desktop
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testSnapshotWithErrors() throws Exception {
		
		driver.switchTo().frame(DriverTestPage.iframe.getElement());
		
		// get real capture
		generateCaptureFilePath();
		
		CustomEventFiringWebDriver mockedDriver = (CustomEventFiringWebDriver) spy(driver);
		ScreenshotUtil screenshotUtil = spy(new ScreenshotUtil(mockedDriver));
		
		doThrow(WebDriverException.class).when(mockedDriver).scrollTop();
		doThrow(JavascriptException.class).when(mockedDriver).scrollTo(anyInt(), anyInt());
		
		ScreenShot screenshot = screenshotUtil.capture(SnapshotTarget.PAGE, ScreenShot.class);
		Assert.assertNotNull(screenshot.getHtmlSourcePath());
		Assert.assertNotNull(screenshot.getFullImagePath());
	}

	/**
	 * Test if we succeed in cropping picture according to requested parameters
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testRemoveHeader() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);

		// get cropped picture
		String filePath = generateCaptureFilePath();
		BufferedImage image = new ScreenshotUtil(driver).capturePage(6, 0);
		FileUtility.writeImage(filePath, image);
		
		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		
		// header should have been removed, not footer
		Assert.assertEquals(headerFooter[0], 0);
		Assert.assertEquals(headerFooter[1], 5);
		Assert.assertEquals(headerFooter[2], 2); 
	}
	
	/**
	 * Test if we succeed in capturing a single element
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCaptureElement() throws IOException {
		driver.manage().window().maximize();
		WaitHelper.waitForSeconds(1);
		
		// get cropped picture
		String filePath = generateCaptureFilePath();
		BufferedImage image = new ScreenshotUtil(driver).capture(new SnapshotTarget(DriverTestPage.table), BufferedImage.class, false, false).get(0);
		FileUtility.writeImage(filePath, image);
		
		Assert.assertTrue(image.getHeight() < 105 && image.getHeight() > 95); // should be 100 be may depend on browser / version
		Assert.assertTrue(image.getWidth() < 75 && image.getWidth() > 70); // should be 72 be may depend on browser / version
	}
	
	/**
	 * Check that if element does not exist, a Scenario exception is raised
	 * @throws IOException
	 */
	@Test(groups={"it"}, expectedExceptions = ScenarioException.class)
	public void testCaptureNonExistingElement() throws IOException {
		driver.manage().window().maximize();
		WaitHelper.waitForSeconds(1);
		
		// get cropped picture
		String filePath = generateCaptureFilePath();
		BufferedImage image = new ScreenshotUtil(driver).capture(new SnapshotTarget(DriverTestPage.textElementNotPresent), BufferedImage.class, false, false).get(0);
		FileUtility.writeImage(filePath, image);
		
		Assert.assertTrue(image.getHeight() < 105 && image.getHeight() > 95); // should be 100 be may depend on browser / version
		Assert.assertTrue(image.getWidth() < 75 && image.getWidth() > 70); // should be 72 be may depend on browser / version
	}
	
	/**
	 * Capture viewport
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCaptureViewport() throws IOException {
		driver.manage().window().maximize();
		WaitHelper.waitForSeconds(1);
		
		// get viewport picture and compare it to full page capture
		String filePath = generateCaptureFilePath();
		BufferedImage imageVp = new ScreenshotUtil(driver).capture(SnapshotTarget.VIEWPORT, BufferedImage.class, false, false).get(0);
		FileUtility.writeImage(filePath, imageVp);
		BufferedImage imageP = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, BufferedImage.class, false, false).get(0);

		// check no vertical scrolling has been performed
		Assert.assertTrue(imageVp.getHeight() < imageP.getHeight()); 
		Assert.assertTrue(imageVp.getWidth() <= imageP.getWidth()); 
	}
	
	/**
	 * Test if we succeed in cropping picture according to requested parameters
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testRemoveFooter() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		// get cropped picture
		String filePath = generateCaptureFilePath();
		BufferedImage image = new ScreenshotUtil(driver).capturePage(0, 5);
		FileUtility.writeImage(filePath, image);

		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		
		// header should have been removed, not footer
		Assert.assertEquals(headerFooter[0], 6);
		Assert.assertEquals(headerFooter[1], 0);
		Assert.assertEquals(headerFooter[2], 2); 
	}
	
	/**
	 * Test if we succeed in cropping picture according to requested parameters
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testRemoveHeaderAndFooter() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		// get cropped picture
		String filePath = generateCaptureFilePath();
		BufferedImage image = new ScreenshotUtil(driver).capturePage(6, 5);
		FileUtility.writeImage(filePath, image);

		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		
		// header should have been removed, not footer
		Assert.assertEquals(headerFooter[0], 0);
		Assert.assertEquals(headerFooter[1], 0);
		Assert.assertEquals(headerFooter[2], 2); 
	}
	
	/**
	 * Check we can rebuild the whole page from partial captures
	 * This is not the default mode for chrome, but test it anyway
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCaptureAllPage() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		String topFilePath = generateCaptureFilePath();
		FileUtility.writeImage(topFilePath, new ScreenshotUtil(driver).capturePage(0, 0));
		
		// get full picture
		String fullCapture = generateCaptureFilePath();
		FileUtility.writeImage(fullCapture, new ScreenshotUtil(driver).captureWebPage(1, driver.getWindowHandle()));
		
		String bottomFilePath = generateCaptureFilePath();
		FileUtility.writeImage(bottomFilePath, new ScreenshotUtil(driver).capturePage(0, 0));

		// exception thrown if nothing found
		ImageDetector detectorTop = new ImageDetector(new File(fullCapture), new File(topFilePath), 0.001);
		detectorTop.detectExactZoneWithScale();
	}
	
	/**
	 * Check we are able to get all the content whereas we crop the header and footer manually (top and bottom pixels set automatically)
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedHeaderFooterManualCropping() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		// get full picture without cropping
		File imageFull = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, File.class);
		
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(5);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(6);
		
		// get picture with header and footer cropped
		File image = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, File.class);
		
		int[] headerFooter = getHeaderAndFooterPixels(image);
		int[] headerFooterFull = getHeaderAndFooterPixels(imageFull);
		
		// header and footer should have been removed
		Assert.assertEquals(headerFooter[0], 6);
		Assert.assertEquals(headerFooter[1], 5);
		Assert.assertTrue(headerFooter[2] >= headerFooterFull[2]); // depending on browser window size (depends on OS) image is split in more or less sections
		Assert.assertEquals(ImageIO.read(image).getHeight(), ImageIO.read(imageFull).getHeight());
	}
	
	/**
	 * Check we are able to get all the content whereas we crop the header and footer automatically
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedHeaderFooterAutomaticCropping() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		// get full picture without cropping
		File imageFull = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, File.class);
		
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(null);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(null);
		
		// get picture with header and footer cropped
		File image = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, File.class);
		
		int[] headerFooter = getHeaderAndFooterPixels(image);
		
		// header and footer should have been removed
		Assert.assertEquals(headerFooter[0], 6);
		Assert.assertEquals(headerFooter[1], 5);
		Assert.assertEquals(headerFooter[2], 2); // with automatic cropping, all fixed lines are removed, even green and red ones. Only remains the first top and last bottom one
		Assert.assertEquals(ImageIO.read(image).getHeight(), ImageIO.read(imageFull).getHeight());
	}
	
	/**
	 * Check we get capture for each window
	 * Check also we remain on the same window handle after the capture
	 * Check that first captured image (popup) is smaller than the second one (main page)
	 */
	@Test(groups= {"it"})
	public void testMultipleWindowsCapture() {
		String currentWindowHandle = driver.getWindowHandle();
		DriverTestPage.link.click();
		List<ScreenShot> screenshots = new ScreenshotUtil().capture(SnapshotTarget.PAGE, ScreenShot.class, true, false);
		
		Assert.assertEquals(screenshots.size(), 2);
		Assert.assertEquals(currentWindowHandle, driver.getWindowHandle());
		Assert.assertTrue(FileUtils.sizeOf(new File(((ScreenShot)screenshots.get(0)).getFullImagePath())) < FileUtils.sizeOf(new File(((ScreenShot)screenshots.get(1)).getFullImagePath())));
	}
	
	/**
	 * Check that when an error occurs when communicating with driver, a desktop capture is taken 
	 */
	@Test(groups= {"it"})
	public void testMultipleWindowsCaptureWithError() {
		DriverTestPage.link.click();
		
		WebDriver mockedDriver = spy(driver);
		ScreenshotUtil screenshotUtil = spy(new ScreenshotUtil(mockedDriver));
		
		when(mockedDriver.getWindowHandles()).thenThrow(WebDriverException.class);
		
		List<ScreenShot> screenshots = screenshotUtil.capture(SnapshotTarget.PAGE, ScreenShot.class, true, false);
		
		Assert.assertEquals(screenshots.size(), 1);
		verify(screenshotUtil).captureDesktop();
		
	}
	
	/**
	 * Check we can capture desktop snapshots
	 */
	@Test(groups= {"it"})
	public void testDesktopSnapshot() {
		File output = new ScreenshotUtil(driver).capture(SnapshotTarget.SCREEN, File.class);
		Assert.assertTrue(FileUtils.sizeOf(output) > 0);
	}
	
	/**
	 * Check that only main window is captured
	 */
	@Test(groups= {"it"})
	public void testCurrentWindowsCapture() {
		String currentWindowHandle = driver.getWindowHandle();
		DriverTestPage.link.click();
		List<ScreenShot> screenshots = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, ScreenShot.class, false, false);
		
		Assert.assertEquals(screenshots.size(), 1);
		Assert.assertEquals(currentWindowHandle, driver.getWindowHandle());
	}
	
	/**
	 * Check that only main window is captured when no argument is given
	 */
	@Test(groups= {"it"})
	public void testCurrentWindowsCapture2() {
		DriverTestPage.link.click();
		ScreenShot screenshot = new ScreenshotUtil(driver).capture(SnapshotTarget.PAGE, ScreenShot.class);
		
		Assert.assertNotNull(screenshot.getImagePath());
	}
}
