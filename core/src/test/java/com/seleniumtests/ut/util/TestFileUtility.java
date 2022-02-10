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
package com.seleniumtests.ut.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
		f1.deleteOnExit();
		f2.deleteOnExit();
		FileUtils.writeStringToFile(f1, "some data", StandardCharsets.UTF_8);
		FileUtils.writeStringToFile(f2, "other data", StandardCharsets.UTF_8);
		List<File> fileList = new ArrayList<>();
		fileList.add(f1);
		fileList.add(f2);
		
		File zip = FileUtility.createZipArchiveFromFiles(fileList);
		Assert.assertTrue(zip.exists());
		
		File outputDir = null;
		try {
			outputDir = FileUtility.unzipFile(zip);
			File outFile1 = Paths.get(outputDir.getAbsolutePath(), f1.getName()).toFile();
			File outFile2 = Paths.get(outputDir.getAbsolutePath(), f2.getName()).toFile();
			Assert.assertTrue(outFile1.exists());
			Assert.assertTrue(outFile2.exists());
			
			Assert.assertEquals(FileUtils.readFileToString(outFile1, StandardCharsets.UTF_8), "some data");
		} finally {
			
			try {
				if (outputDir != null) {
					FileUtils.deleteDirectory(outputDir);
				}
			} catch (IOException e) {}
		}
	}
}
