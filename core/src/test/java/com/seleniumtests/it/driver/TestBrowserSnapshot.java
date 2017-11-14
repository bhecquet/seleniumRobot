package com.seleniumtests.it.driver;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class TestBrowserSnapshot extends MockitoTest {
	
	private static WebDriver driver;
	private DriverTestPage testPage;
	
	@BeforeMethod(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(0);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(0);
//		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
//		SeleniumTestsContextManager.getThreadContext().setFirefoxBinary("path to firefox");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterMethod(groups={"it"})
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
	 * Also count red pixels which is a line not to remove when cropping
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
		String b64Img = ScreenshotUtil.capturePageScreenshotToString(driver);
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
		String filePath = generateCaptureFilePath();
		ScreenshotUtil.capturePageScreenshotToFile(driver, filePath, 0, 0);
		BufferedImage image = ImageIO.read(new File(filePath));
		
		// check all scrollbars where already removed from screenshot
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
		String filePath = generateCaptureFilePath();
		ScreenshotUtil.capturePageScreenshotToFile(driver, filePath, 0, 0);
		BufferedImage image = ImageIO.read(new File(filePath));
		
		// check all scrollbars where already removed from screenshot
		Assert.assertEquals(image.getWidth(), screenshotDim.width);
		Assert.assertEquals(image.getHeight(), screenshotDim.height);
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
		ScreenshotUtil.capturePageScreenshotToFile(driver, filePath, 6, 0);
		
		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		
		// header should have been removed, not footer
		Assert.assertEquals(0, headerFooter[0]);
		Assert.assertEquals(5, headerFooter[1]);
		Assert.assertEquals(2, headerFooter[2]); 
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
		ScreenshotUtil.capturePageScreenshotToFile(driver, filePath, 0, 5);
		
		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		
		// header should have been removed, not footer
		Assert.assertEquals(6, headerFooter[0]);
		Assert.assertEquals(0, headerFooter[1]);
		Assert.assertEquals(2, headerFooter[2]); 
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
		ScreenshotUtil.capturePageScreenshotToFile(driver, filePath, 6, 5);
		
		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		
		// header should have been removed, not footer
		Assert.assertEquals(0, headerFooter[0]);
		Assert.assertEquals(0, headerFooter[1]);
		Assert.assertEquals(2, headerFooter[2]); 
	}
	
	/**
	 * Check we can rebuild the whole page from partial captures
	 * This is useful for chrome as snapshot is only taken from visible display
	 * For firefox, nothing to do
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCaptureAllPage() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		String topFilePath = generateCaptureFilePath();
		ScreenshotUtil.capturePageScreenshotToFile(driver, topFilePath, 0, 0);
		
		// get full picture
		String filePath = generateCaptureFilePath();
		ScreenshotUtil.captureEntirePageToFile(driver, filePath);
		
		String bottomFilePath = generateCaptureFilePath();
		ScreenshotUtil.capturePageScreenshotToFile(driver, bottomFilePath, 0, 0);

		// exception thrown if nothing found
		ImageDetector detectorTop = new ImageDetector(new File(filePath), new File(topFilePath), 0.001);
		detectorTop.detectExactZoneWithScale();
	}
	
	/**
	 * Check we are able to get all the content whereas we crop the header and footer
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFixedHeaderFooterCropping() throws IOException {
		driver.manage().window().setSize(new Dimension(400, 300));
		WaitHelper.waitForSeconds(1);
		
		// get full picture without cropping
		String filePathFull = generateCaptureFilePath();
		ScreenshotUtil.captureEntirePageToFile(driver, filePathFull);
		
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(5);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(6);
		
		// get picture with header and footer cropped
		String filePath = generateCaptureFilePath();
		ScreenshotUtil.captureEntirePageToFile(driver, filePath);
		
		int[] headerFooter = getHeaderAndFooterPixels(new File(filePath));
		int[] headerFooterFull = getHeaderAndFooterPixels(new File(filePathFull));
		
		// header and footer should have been removed
		Assert.assertEquals(6, headerFooter[0]);
		Assert.assertEquals(5, headerFooter[1]);
		Assert.assertTrue(headerFooter[2] >= headerFooterFull[2]); // depending on browser window size (depends on OS) image is split in more or less sections
		Assert.assertEquals(ImageIO.read(new File(filePath)).getHeight(), ImageIO.read(new File(filePathFull)).getHeight());
	}
	
	/**
	 * Check we get capture for each window
	 * Check also we remain on the same window handle after the capture
	 * Check that first captured image (popup) is smaller than the second one (main page)
	 */
	@Test(groups= {"it"})
	public void testMultipleWindowsCapture() {
		String currentWindowHandle = driver.getWindowHandle();
		testPage.link.click();
		List<ScreenShot> screenshots = new ScreenshotUtil().captureWebPageSnapshots(true);
		
		Assert.assertEquals(screenshots.size(), 2);
		Assert.assertEquals(currentWindowHandle, driver.getWindowHandle());
		Assert.assertTrue(FileUtils.sizeOf(new File(((ScreenShot)screenshots.get(0)).getFullImagePath())) < FileUtils.sizeOf(new File(((ScreenShot)screenshots.get(1)).getFullImagePath())));
	}
	
	/**
	 * Check that when an error occurs when communicating with driver, a desktop capture is taken 
	 */
	@Test(groups= {"it"})
	public void testMultipleWindowsCaptureWithError() {
		testPage.link.click();
		
		WebDriver mockedDriver = spy(driver);
		ScreenshotUtil screenshotUtil = spy(new ScreenshotUtil(mockedDriver));
		
		when(mockedDriver.getWindowHandles()).thenThrow(WebDriverException.class);
		
		List<ScreenShot> screenshots = screenshotUtil.captureWebPageSnapshots(true);
		
		Assert.assertEquals(screenshots.size(), 1);
		verify(screenshotUtil).captureDesktopToFile();
		
	}
	
	/**
	 * Check that only main window is captured
	 */
	@Test(groups= {"it"})
	public void testCurrentWindowsCapture() {
		String currentWindowHandle = driver.getWindowHandle();
		testPage.link.click();
		List<ScreenShot> screenshots = new ScreenshotUtil().captureWebPageSnapshots(false);
		
		Assert.assertEquals(screenshots.size(), 1);
		Assert.assertEquals(currentWindowHandle, driver.getWindowHandle());
	}
	
	/**
	 * Check that only main window is captured
	 */
	@Test(groups= {"it"})
	public void testCurrentWindowsCapture2() {
		testPage.link.click();
		ScreenShot screenshot = new ScreenshotUtil().captureWebPageSnapshot();
		
		Assert.assertNotNull(screenshot.getImagePath());
	}
	
}
