package com.seleniumtests.it.driver;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageDetector;

public class TestBrowserSnapshot {
	
	private static WebDriver driver;
	
	@BeforeMethod(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(0);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(0);
//		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
//		SeleniumTestsContextManager.getThreadContext().setFirefoxBinary("path to firefox");
		new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	

	@AfterMethod(alwaysRun = true)
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
			Color color = new Color(image.getRGB(0, height));
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
	 * Test if we succeed in removing scrollbars from capture (horizontal and vertical)
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
		detectorTop.detectExactZone();
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
		
		// header should have been removed, not footer
		Assert.assertEquals(6, headerFooter[0]);
		Assert.assertEquals(5, headerFooter[1]);
		Assert.assertEquals(headerFooter[2], headerFooterFull[2] + 2); 
		Assert.assertEquals(ImageIO.read(new File(filePath)).getHeight(), ImageIO.read(new File(filePathFull)).getHeight());
	}
}
