package com.seleniumtests.ut.reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.GenericFile;

public class TestGenericFile extends GenericTest {
	
	@Test(groups={"ut"}, expectedExceptions = FileNotFoundException.class)
	public void testFileDoesNotExist() throws IOException {
		new GenericFile(new File("foo"), "description");
	}
	@Test(groups={"ut"}, expectedExceptions = FileNotFoundException.class)
	public void testFileNull() throws IOException {
		new GenericFile(null, "description");
	}
	
	
	@Test(groups={"ut"})
	public void testFileMovedOnCreation() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		GenericFile genericFile = new GenericFile(videoFile, "description");
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
	}
	
	@Test(groups={"ut"})
	public void testFileReplaceOnCreation() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
		
		// put a file in the destination
		File dest = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile();
		FileUtils.write(dest, "foo", StandardCharsets.UTF_8);
		
		GenericFile genericFile = new GenericFile(videoFile, "description");
		Assert.assertEquals(genericFile.getFile(), dest);
		
		// check file has been replaced
		Assert.assertEquals(FileUtils.readFileToString(dest, StandardCharsets.UTF_8), "bar");
	}
	
	/**
	 * Test case when file cannot be moved
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileNoReplaceOnCreation() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
		
		// put a file in the destination
		File dest = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile();
		FileUtils.write(dest, "foo", StandardCharsets.UTF_8);
		
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(dest),  StandardCharsets.UTF_8);) {
			GenericFile genericFile = new GenericFile(videoFile, "description");
			
			// file field has been updated with the source value as file has not been copied
			Assert.assertEquals(genericFile.getFile(), videoFile);
		}
		
		// check file has not been replaced
		Assert.assertEquals(FileUtils.readFileToString(dest, StandardCharsets.UTF_8), "foo");
		
		
	}
	
	@Test(groups={"ut"})
	public void testRelocate() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		GenericFile genericFile = new GenericFile(videoFile, "description");
		
		File outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest").toFile();
		File previousLocation = genericFile.getFile();
		Assert.assertTrue(previousLocation.exists());
		
		genericFile.relocate(outputDir.getAbsolutePath());
		
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest", videoFile.getName()).toFile());
		Assert.assertTrue(genericFile.getFile().exists());
	}
	
	/**
	 * No error
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testRelocateNull() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		GenericFile genericFile = new GenericFile(videoFile, "description");

		genericFile.relocate(null);
		
	}
	
	/**
	 * Check relocate can replace file on move
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileReplaceOnRelocate() throws IOException {
		
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
		GenericFile genericFile = new GenericFile(videoFile, "description");
		
		File outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest").toFile();
		File dest = Paths.get(outputDir.getAbsolutePath(), videoFile.getName()).toFile();
		FileUtils.write(dest, "foo", StandardCharsets.UTF_8);
		File previousLocation = genericFile.getFile();
		Assert.assertTrue(previousLocation.exists());
		
		genericFile.relocate(outputDir.getAbsolutePath());
		
		Assert.assertEquals(genericFile.getFile(), dest);
		Assert.assertTrue(genericFile.getFile().exists());
		
		// check file has been replaced
		Assert.assertEquals(FileUtils.readFileToString(dest, StandardCharsets.UTF_8), "bar");
	}
	
	@Test(groups={"ut"})
	public void testFileNotReplacedOnRelocate() throws IOException {
		
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
		GenericFile genericFile = new GenericFile(videoFile, "description");
		
		File outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest").toFile();
		File dest = Paths.get(outputDir.getAbsolutePath(), videoFile.getName()).toFile();
		FileUtils.write(dest, "foo", StandardCharsets.UTF_8);
		File previousLocation = genericFile.getFile();
		Assert.assertTrue(previousLocation.exists());
		
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(dest),  StandardCharsets.UTF_8);) {
			genericFile.relocate(outputDir.getAbsolutePath());
		}
		
		// file has not been moved
		Assert.assertEquals(genericFile.getFile(), previousLocation);
		Assert.assertTrue(genericFile.getFile().exists());
		
		// check file has been replaced
		Assert.assertEquals(FileUtils.readFileToString(dest, StandardCharsets.UTF_8), "foo");
	}
}
