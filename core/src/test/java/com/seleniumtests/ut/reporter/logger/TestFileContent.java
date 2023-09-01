package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.FileContent;
import com.seleniumtests.reporter.logger.GenericFile;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class TestFileContent extends GenericTest {
    
    
    @Test(groups={"ut"})
    public void testRelocate() throws IOException {
        File videoFile = File.createTempFile("video", ".avi");
        videoFile.deleteOnExit();
        FileContent fileContent = new FileContent(videoFile);
        
        File outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest").toFile();
        File previousLocation = fileContent.getFile();
        Assert.assertTrue(previousLocation.exists());
        
        fileContent.relocate(outputDir.getAbsolutePath());
        
        Assert.assertEquals(fileContent.getFile(), Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest", videoFile.getName()).toFile());
        Assert.assertTrue(fileContent.getFile().exists());
        Assert.assertFalse(videoFile.exists());
    }
    
    /**
     * No error
     * @throws IOException
     */
    @Test(groups={"ut"})
    public void testRelocateNull() throws IOException {
        File videoFile = File.createTempFile("video", ".avi");
        videoFile.deleteOnExit();
        FileContent fileContent = new FileContent(videoFile);
        
        fileContent.relocate(null);
        
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
        FileContent fileContent = new FileContent(videoFile);

        File outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest").toFile();
        File dest = Paths.get(outputDir.getAbsolutePath(), videoFile.getName()).toFile();
        FileUtils.write(dest, "foo", StandardCharsets.UTF_8);
        File previousLocation = fileContent.getFile();
        Assert.assertTrue(previousLocation.exists());

        fileContent.relocate(outputDir.getAbsolutePath());

        Assert.assertEquals(fileContent.getFile(), dest);
        Assert.assertTrue(fileContent.getFile().exists());

        // check file has been replaced
        Assert.assertEquals(FileUtils.readFileToString(dest, StandardCharsets.UTF_8), "bar");
    }

    /**
     * Check that if the file is in use, no error is raised
     * @throws IOException
     */
    @Test(groups={"ut"})
    public void testFileNotReplacedOnRelocate() throws IOException {

        File videoFile = File.createTempFile("video", ".avi");
        videoFile.deleteOnExit();
        FileUtils.write(videoFile, "bar", StandardCharsets.UTF_8);
        FileContent fileContent = new FileContent(videoFile);

        File outputDir = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "dest").toFile();
        File dest = Paths.get(outputDir.getAbsolutePath(), videoFile.getName()).toFile();
        FileUtils.write(dest, "foo", StandardCharsets.UTF_8);
        File previousLocation = fileContent.getFile();
        Assert.assertTrue(previousLocation.exists());

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(dest),  StandardCharsets.UTF_8);) {
            fileContent.relocate(outputDir.getAbsolutePath());
        }

        // file has not been moved
        Assert.assertEquals(fileContent.getFile(), previousLocation);
        Assert.assertTrue(fileContent.getFile().exists());

        // check file has not been replaced
        Assert.assertEquals(FileUtils.readFileToString(dest, StandardCharsets.UTF_8), "foo");
    }
}
