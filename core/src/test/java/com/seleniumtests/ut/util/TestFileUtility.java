package com.seleniumtests.ut.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.FileUtility;

public class TestFileUtility extends GenericTest {

	
	@Test(groups={"ut"})
	public void testZipUnzipFile() throws IOException {
		File f1 = File.createTempFile("data", ".txt");
		File f2 = File.createTempFile("data2", ".txt");
		FileUtils.writeStringToFile(f1, "some data");
		FileUtils.writeStringToFile(f2, "other data");
		List<File> fileList = new ArrayList<>();
		fileList.add(f1);
		fileList.add(f2);
		
		File zip = FileUtility.createZipArchiveFromFiles(fileList);
		Assert.assertTrue(zip.exists());
		
		File outputDir = FileUtility.unzipFile(zip);
		File outFile1 = Paths.get(outputDir.getAbsolutePath(), f1.getName()).toFile();
		File outFile2 = Paths.get(outputDir.getAbsolutePath(), f2.getName()).toFile();
		Assert.assertTrue(outFile1.exists());
		Assert.assertTrue(outFile2.exists());
		
		Assert.assertEquals(FileUtils.readFileToString(outFile1), "some data");
	}
}
