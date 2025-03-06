package com.seleniumtests.ut.driver.screenshots;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.seleniumtests.core.SeleniumTestsContext;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;

import javax.imageio.ImageIO;

public class TestScreenshot extends GenericTest {
	@BeforeMethod(groups= {"ut"})
	public void init() {
		try {
			FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check files are copied at the right place
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testScreenshot() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), tmpImgFile.getName()).toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), tmpHtmlFile.getName()).toFile().exists());
		Assert.assertNotNull(screenshot.getImage());
		Assert.assertNotNull(screenshot.getHtml());
	}
	
	/**
	 * If provided file is already at the right place, no error should be raised
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testScreenshotFilePresent() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		File newImgPath = Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), tmpImgFile.getName()).toFile();
		FileUtils.moveFile(tmpImgFile, newImgPath);
		File tmpHtmlFile = File.createTempFile("html", ".html");
		File newHtmlPath = Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), tmpHtmlFile.getName()).toFile();
		FileUtils.moveFile(tmpHtmlFile, newHtmlPath);
		
		ScreenShot screenshot = new ScreenShot(newImgPath, newHtmlPath);
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), tmpImgFile.getName()).toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), tmpHtmlFile.getName()).toFile().exists());
		Assert.assertNotNull(screenshot.getImage());
		Assert.assertNotNull(screenshot.getHtml());
	}
	
	@Test(groups= {"ut"})
	public void testScreenshotImageDoesNotExist() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		tmpImgFile.delete();
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);
		
		Assert.assertNull(screenshot.getImage());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), tmpHtmlFile.getName()).toFile().exists());
	}
	
	@Test(groups= {"ut"})
	public void testScreenshotHtmlDoesNotExist() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		tmpHtmlFile.delete();
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile);
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), tmpImgFile.getName()).toFile().exists());
		Assert.assertNull(screenshot.getHtml());
	}
	
	@Test(groups= {"ut"})
	public void testScreenshotNoHtml() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, null);
		
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), tmpImgFile.getName()).toFile().exists());
	}
	
	@Test(groups= {"ut"})
	public void testScreenshotNoImage() throws IOException {
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(null, tmpHtmlFile);
		
		Assert.assertNull(screenshot.getImage());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), tmpHtmlFile.getName()).toFile().exists());
	}
	
	@Test(groups= {"ut"})
	public void testScreenshotWithRelativePath() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(tmpImgFile, tmpHtmlFile, "foo");
		
		Assert.assertTrue(Paths.get(screenshot.getOutputDirectory(), "foo", tmpImgFile.getName()).toFile().exists());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), tmpHtmlFile.getName()).toFile().exists());
	}
	
	@Test(groups= {"ut"})
	public void testScreenshotBuffer() throws IOException {

		BufferedImage img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/ffLogo1.png"));
		ScreenShot screenshot = new ScreenShot(img, "<html>");
		
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory()).toFile().listFiles().length, 1);
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory()).toFile().listFiles().length, 1);
		Assert.assertNotNull(screenshot.getImage());
		Assert.assertNotNull(screenshot.getHtml());
	}
	@Test(groups= {"ut"})
	public void testScreenshotBufferNoImage() throws IOException {

		ScreenShot screenshot = new ScreenShot(null, "<html>");
		
		Assert.assertNull(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory()).toFile().listFiles()); // listFiles returns null if directory does not exist
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory()).toFile().listFiles().length, 1);
		Assert.assertNull(screenshot.getImage());
		Assert.assertNotNull(screenshot.getHtml());
	}
	@Test(groups= {"ut"})
	public void testScreenshotBufferNoHtml() throws IOException {
		
		BufferedImage img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/ffLogo1.png"));
		ScreenShot screenshot = new ScreenShot(img, null);
		
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory()).toFile().listFiles().length, 1);
		Assert.assertNull(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory()).toFile().listFiles()); // listFiles returns 'null' when directory does not exist
		Assert.assertNotNull(screenshot.getImage());
		Assert.assertNull(screenshot.getHtml());
	}


	@Test(groups= {"ut"})
	public void testGetImagePath() throws IOException {
		File tmpImgFile = File.createTempFile("img", ".png");
		ScreenShot screenshot = new ScreenShot(tmpImgFile);
		Assert.assertEquals(screenshot.getImagePath(), "screenshots/" + tmpImgFile.getName());
	}
	@Test(groups= {"ut"})
	public void testGetHtmlSourcePath() throws IOException {
		File tmpHtmlFile = File.createTempFile("html", ".html");
		ScreenShot screenshot = new ScreenShot(null, tmpHtmlFile);
		Assert.assertEquals(screenshot.getHtmlSourcePath(), "htmls/" + tmpHtmlFile.getName());
	}
	
	@Test(groups= {"ut"})
	public void testGetImagePathNull() {
		Assert.assertNull(new ScreenShot(null).getImagePath());
	}
	
	@Test(groups= {"ut"})
	public void testHtmlSourcePathNull() {
		Assert.assertNull(new ScreenShot(null).getHtmlSourcePath());
	}
	
	@Test(groups= {"ut"})
	public void testRelocate() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		ScreenShot s = new ScreenShot(createFileFromResource("tu/ffLogo1.png"));
		String fileName = s.getImageName();
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", SeleniumTestsContext.SCREENSHOT_DIRECTORY, fileName).toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(),SeleniumTestsContext.SCREENSHOT_DIRECTORY, fileName).toFile().exists());
	}
	
	/**
	 * Check we can move a file from a sub-folder of output directoru to another sub-folder
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateSameFolder() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		
		ScreenShot s = new ScreenShot(createFileFromResource("tu/ffLogo1.png"), null, "video");
		s.relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), Paths.get(SeleniumTestsContext.SCREENSHOT_DIRECTORY, "foo.jpg").toString(), null);
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), "foo.jpg").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", "foo.jpg").toFile().exists());
		Assert.assertEquals(s.getImagePath(),  SeleniumTestsContext.SCREENSHOT_DIRECTORY + "/foo.jpg");
	}
	
	/**
	 * Check no error is raised if file already exists
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateExistingFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		File imageFile = createFileFromResource("tu/ffLogo1.png");
		FileUtils.copyFile(imageFile, Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", SeleniumTestsContext.SCREENSHOT_DIRECTORY, imageFile.getName()).toFile());
		ScreenShot s = new ScreenShot(imageFile);
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		
	}
	
	/**
	 * No error should be raised
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateNullFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		ScreenShot s = new ScreenShot(null);
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		
	}
	
	/**
	 * No error should be raised
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateNonExistingFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		ScreenShot s = new ScreenShot(createFileFromResource("tu/ffLogo1.png"));
		s.getImage().getFile().delete();
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
	}
	
	@Test(groups= {"ut"})
	public void testRelocateHtml() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		File htmlFile = createFileFromResource("tu/test.html");
		ScreenShot s = new ScreenShot(null, htmlFile);
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", SeleniumTestsContext.HTML_DIRECTORY, htmlFile.getName()).toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), htmlFile.getName()).toFile().exists());
	}
	
	/**
	 * No error should be raised when source and dest files are the same
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateHtmlSameFolder() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		
		File htmlFile = createFileFromResource("tu/test.html");
		ScreenShot s = new ScreenShot(null, htmlFile);
		s.relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), null, Paths.get(SeleniumTestsContext.HTML_DIRECTORY, "test.html").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), "test.html").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getHtmlsOutputDirectory(), htmlFile.getName()).toFile().exists());
		Assert.assertEquals(s.getHtmlSourcePath(),  SeleniumTestsContext.HTML_DIRECTORY + "/test.html");
	}
	
	/**
	 * Check no error is raised if file already exists
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateExistingHtmlFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		File htmlFile = createFileFromResource("tu/test.html");
		FileUtils.copyFile(htmlFile, Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", SeleniumTestsContext.HTML_DIRECTORY, htmlFile.getName()).toFile());
		ScreenShot s = new ScreenShot(htmlFile);
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
	}
	
	/**
	 * No error should be raised
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateNullHtmlFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		ScreenShot s = new ScreenShot(null, null, "");
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		
	}
	
	@Test(groups= {"ut"})
	public void testGetImageName() throws IOException {
		File imageFile = createFileFromResource("tu/ffLogo1.png");
		ScreenShot s = new ScreenShot(imageFile);
		Assert.assertEquals(s.getImageName(), imageFile.getName());
	}
	
	@Test(groups= {"ut"})
	public void testGetImageNameNull() {
		ScreenShot s = new ScreenShot(null);
		Assert.assertEquals(s.getImageName(), "");
	}
	
}
