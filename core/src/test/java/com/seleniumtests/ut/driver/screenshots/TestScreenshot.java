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
	public void testRelocate() throws IOException {
		FileUtils.deleteDirectory(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()));
		FileUtils.deleteDirectory(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toFile());
		
		FileUtils.copyFile(createFileFromResource("tu/ffLogo1.png"), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile());
		ScreenShot s = new ScreenShot(ScreenshotUtil.SCREENSHOT_DIR + "/foo.jpg");
		s.relocate(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out").toString());
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory(), "out", ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile().exists());
		Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), ScreenshotUtil.SCREENSHOT_DIR, "foo.jpg").toFile().exists());
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
	
}
