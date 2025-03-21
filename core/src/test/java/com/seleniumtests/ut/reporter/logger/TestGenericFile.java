package com.seleniumtests.ut.reporter.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
	

	/**
	 * In case file is not in the output directory, move it even if it's not requested
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileMovedIfNotInOutputDirectory() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		GenericFile genericFile = new GenericFile(videoFile, "description", GenericFile.FileOperation.KEEP);
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
		Assert.assertEquals(genericFile.getRelativeFilePath(), videoFile.getName());
	}
	
	/**
	 * In case file is already in the test output directory 'test-output/mytest' for example, do not move it if not requested
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileNotMovedIfInOutputDirectory() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		File newVideoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", videoFile.getName()).toFile();
		FileUtils.copyFile(videoFile, newVideoFile);
		GenericFile genericFile = new GenericFile(newVideoFile, "description", GenericFile.FileOperation.KEEP);
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", videoFile.getName()).toFile());
		Assert.assertEquals(genericFile.getRelativeFilePath(), "video/" + videoFile.getName());
	}
	
	/**
	 * Path is already relative to the output directory (in a sub-directory)
	 * Check relativePath is corrected when file is moved
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileMovedIfRequested() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		File newVideoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", videoFile.getName()).toFile();
		FileUtils.copyFile(videoFile, newVideoFile);
		GenericFile genericFile = new GenericFile(newVideoFile, "description", GenericFile.FileOperation.MOVE);
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
		Assert.assertEquals(genericFile.getRelativeFilePath(), videoFile.getName());
	}

	/**
	 * Check copy is done and original file is kept
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testFileCopiedIfRequested() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		File newVideoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", videoFile.getName()).toFile();
		FileUtils.copyFile(videoFile, newVideoFile);
		GenericFile genericFile = new GenericFile(newVideoFile, "description", GenericFile.FileOperation.COPY);
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
		Assert.assertEquals(genericFile.getRelativeFilePath(), videoFile.getName());
		Assert.assertTrue(genericFile.getFile().exists());
		Assert.assertTrue(newVideoFile.exists());
	}
	
	@Test(groups={"ut"})
	public void testBuildLogWithRelativePath() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		File newVideoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", videoFile.getName()).toFile();
		FileUtils.copyFile(videoFile, newVideoFile);
		GenericFile genericFile = new GenericFile(newVideoFile, "description", GenericFile.FileOperation.KEEP);
		Assert.assertEquals(genericFile.buildLog(), String.format("description: <a href='video/%s'>file</a>", videoFile.getName()));
	}
	
	@Test(groups={"ut"})
	public void testBuildLog() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		GenericFile genericFile = new GenericFile(videoFile, "description", GenericFile.FileOperation.KEEP);
		Assert.assertEquals(genericFile.buildLog(), String.format("description: <a href='%s'>file</a>", videoFile.getName()));
	}
	
	@Test(groups={"ut"})
	public void testFileMovedOnCreation() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		GenericFile genericFile = new GenericFile(videoFile, "description");
		Assert.assertEquals(genericFile.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), videoFile.getName()).toFile());
		Assert.assertEquals(genericFile.getRelativeFilePath(), videoFile.getName());
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
	public void testToJson() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
		GenericFile genericFile = new GenericFile(videoFile, "description");
		
		JSONObject json = genericFile.toJson();
		Assert.assertEquals(json.getString("name"), "description");
		Assert.assertEquals(json.getString("type"), "file");
		Assert.assertTrue(json.isNull("id"));
	}
	
	@Test(groups={"ut"})
	public void testToJsonWithId() throws IOException {
		File videoFile = File.createTempFile("video", ".avi");
		videoFile.deleteOnExit();
		FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
		GenericFile genericFile = new GenericFile(videoFile, "description");
		genericFile.getFileContent().setId(1);
		
		JSONObject json = genericFile.toJson();
		Assert.assertEquals(json.getString("name"), "description");
		Assert.assertEquals(json.getString("type"), "file");
		Assert.assertEquals(json.getInt("id"), 1);
	}
}
