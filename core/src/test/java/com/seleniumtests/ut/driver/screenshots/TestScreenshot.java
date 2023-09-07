package com.seleniumtests.ut.driver.screenshots;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;

public class TestScreenshot extends GenericTest {

	@Test(groups= {"ut"})
	public void testGetFullImagePath() {
		Assert.assertEquals(new ScreenShot("foo.jpg").getFullImagePath(), SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "/foo.jpg");
	}
	
	@Test(groups= {"ut"})
	public void testGetFullImagePathNull() {
		Assert.assertNull(new ScreenShot(null).getFullImagePath());
	}
	
	@Test(groups= {"ut"})
	public void testGetImagePath() {
		Assert.assertEquals(new ScreenShot("foo.jpg").getImagePath(), "foo.jpg");
	}
	
	@Test(groups= {"ut"})
	public void testGetImagePathNull() {
		Assert.assertNull(new ScreenShot(null).getImagePath());
	}
	
	@Test(groups= {"ut"})
	public void testGetFullHtmlPath() {
		ScreenShot s = new ScreenShot("foo.jpg");
		s.setHtmlSourcePath("foo.html");
		Assert.assertEquals(s.getFullHtmlPath(), SeleniumTestsContextManager.getThreadContext().getOutputDirectory() + "/foo.html");
	}
	
	@Test(groups= {"ut"})
	public void testGetFullHtmlPathNull() {
		Assert.assertNull(new ScreenShot(null).getFullHtmlPath());
	}
	
	@Test(groups= {"ut"})
	public void testHtmlSourcePath() {
		ScreenShot s = new ScreenShot("foo.jpg");
		s.setHtmlSourcePath("foo.html");
		Assert.assertEquals(s.getHtmlSourcePath(), "foo.html");
	}
	
	@Test(groups= {"ut"})
	public void testHtmlSourcePathNull() {
		Assert.assertNull(new ScreenShot(null).getHtmlSourcePath());
	}
	
	@Test(groups= {"ut"})
	public void testRelocate() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		FileUtils.copyFile(createFileFromResource("tu/ffLogo1.png"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile());
		ScreenShot s = new ScreenShot(ScreenshotUtil.SCREENSHOT_DIR + "/foo.jpg");
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile().exists());
		Assert.assertEquals(s.getFullImagePath(), Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toString().replace("\\", "/"));
	}
	
	/**
	 * Check we can move a file from a sub-folder of output directoru to another sub-folder
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateSameFolder() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		
		FileUtils.copyFile(createFileFromResource("tu/ffLogo1.png"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", "foo.jpg").toFile());
		ScreenShot s = new ScreenShot("video/foo.jpg");
		s.relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), Paths.get(ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toString(), null);
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", "foo.jpg").toFile().exists());
		Assert.assertEquals(s.getImagePath(),  ScreenshotUtil.SCREENSHOT_DIR + "/foo.jpg");
	}
	
	/**
	 * Check no error is raised if file already exists
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateExistingFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		FileUtils.copyFile(createFileFromResource("tu/ffLogo1.png"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile());
		FileUtils.copyFile(createFileFromResource("tu/ffLogo1.png"), Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile());
		ScreenShot s = new ScreenShot(ScreenshotUtil.SCREENSHOT_DIR + "/foo.jpg");
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

	@Test(groups= {"ut"})
	public void testRelocateNonExistingFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		ScreenShot s = new ScreenShot(ScreenshotUtil.SCREENSHOT_DIR + "/foo.jpg");
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
	}
	
	@Test(groups= {"ut"})
	public void testRelocateHtml() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		FileUtils.copyFile(createFileFromResource("tu/test.html"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "foo.html").toFile());
		ScreenShot s = new ScreenShot();
		s.setHtmlSourcePath(ScreenshotUtil.HTML_DIR + "/foo.html");
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", ScreenshotUtil.HTML_DIR, "foo.html").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "foo.html").toFile().exists());
	}
	
	@Test(groups= {"ut"})
	public void testRelocateHtmlSameFolder() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		
		FileUtils.copyFile(createFileFromResource("tu/test.html"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "foo.html").toFile());
		ScreenShot s = new ScreenShot();
		s.setHtmlSourcePath(ScreenshotUtil.HTML_DIR + "/foo.html");
		s.relocate(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), null, Paths.get(ScreenshotUtil.SCREENSHOT_DIR, "foo.html").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.html").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "foo.html").toFile().exists());
		Assert.assertEquals(s.getHtmlSourcePath(),  ScreenshotUtil.SCREENSHOT_DIR + "/foo.html");
	}
	
	/**
	 * Check no error is raised if file already exists
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testRelocateExistingHtmlFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());

		FileUtils.copyFile(createFileFromResource("tu/test.html"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "foo.html").toFile());
		FileUtils.copyFile(createFileFromResource("tu/test.html"), Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", ScreenshotUtil.HTML_DIR, "foo.html").toFile());
		ScreenShot s = new ScreenShot();
		s.setHtmlSourcePath(ScreenshotUtil.HTML_DIR + "/foo.html");
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
		
		ScreenShot s = new ScreenShot();
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		
	}
	
	@Test(groups= {"ut"})
	public void testRelocateNonExistingHtmlFile() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		ScreenShot s = new ScreenShot();
		s.setHtmlSourcePath(ScreenshotUtil.HTML_DIR + "/foo.html");
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
	}
	
	@Test(groups= {"ut"})
	public void testGetHtmlSource() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		
		FileUtils.copyFile(createFileFromResource("tu/test.html"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.HTML_DIR, "foo.html").toFile());
		ScreenShot s = new ScreenShot();
		s.setHtmlSourcePath(ScreenshotUtil.HTML_DIR + "/foo.html");
		Assert.assertTrue(s.getHtmlSource().contains("<html>"));
	}
	
	@Test(groups= {"ut"})
	public void testGetHtmlSourceNonExistentFile() throws IOException {
		ScreenShot s = new ScreenShot();
		s.setHtmlSourcePath(ScreenshotUtil.HTML_DIR + "/foo.html");
		Assert.assertEquals(s.getHtmlSource(), "");
	}
	
	@Test(groups= {"ut"})
	public void testGetHtmlSourceNull() throws IOException {

		ScreenShot s = new ScreenShot();
		Assert.assertEquals(s.getHtmlSource(), "");
	}
	
	@Test(groups= {"ut"})
	public void testGetImageName() {
		ScreenShot s = new ScreenShot("video/foo.jpg");
		Assert.assertEquals(s.getImageName(), "foo.jpg");
	}
	
	@Test(groups= {"ut"})
	public void testGetImageNameNull() {
		ScreenShot s = new ScreenShot(null);
		Assert.assertEquals(s.getImageName(), "");
	}
	
}
