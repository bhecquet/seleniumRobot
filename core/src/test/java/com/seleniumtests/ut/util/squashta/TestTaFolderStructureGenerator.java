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
package com.seleniumtests.ut.util.squashta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.squashta.TaFolderStructureGenerator;

public class TestTaFolderStructureGenerator extends GenericTest {

	@BeforeClass(groups={"ut"})
	public void init(ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
	}
	
	
	/**
	 * folder structure generation without any file present in dest
	 * @throws IOException
	 */
	@Test(groups={"squash"})
	public void testGenerateDefaultStructure() throws IOException {
		File tmpFolder = Paths.get(SeleniumTestsContextManager.getDataPath(), "tmp").toFile();
		TaFolderStructureGenerator structGen = new TaFolderStructureGenerator("core", null, tmpFolder.getAbsolutePath());
		try {
			structGen.generateDefaultStructure();
			Assert.assertTrue(Paths.get(tmpFolder.getPath(), "pom.xml").toFile().exists());
			Assert.assertTrue(Paths.get(tmpFolder.getPath(), "src", "squashTA", "resources", "junit", "java", "SeleniumRobotTest.java").toFile().exists());
			Assert.assertTrue(Paths.get(tmpFolder.getPath(), "src", "squashTA", "tests", "core_generic.ta").toFile().exists());
		} finally {
			FileUtils.cleanDirectory(tmpFolder);
			tmpFolder.delete();
		}
	}
	
	/**
	 * folder structure generation with ta file present in source. We check that it's taken
	 * @throws IOException
	 */
	@Test(groups={"squash"})
	public void testGenerateStructureWithExistingTa() throws IOException {
		File tmpFolder = Paths.get(SeleniumTestsContextManager.getDataPath(), "tmp").toFile();
		File taFile = Paths.get(tmpFolder.getPath(), "src", "squashTA", "tests", "core_generic.ta").toFile();
		FileUtils.writeStringToFile(taFile, "exists");
		
		TaFolderStructureGenerator structGen = new TaFolderStructureGenerator("core", tmpFolder.getAbsolutePath(), tmpFolder.getAbsolutePath());
		try {
			structGen.generateDefaultStructure();
			Assert.assertTrue(taFile.exists());
			Assert.assertEquals(FileUtils.readFileToString(taFile), "exists");
		} finally {
			FileUtils.cleanDirectory(tmpFolder);
			tmpFolder.delete();
		}
	}
	
	/**
	 * folder structure generation with java file present in source. We check that it's taken
	 * @throws IOException
	 */
	@Test(groups={"squash"})
	public void testGenerateStructureWithExistingJava() throws IOException {
		File tmpFolder = Paths.get(SeleniumTestsContextManager.getDataPath(), "tmp").toFile();
		File javaFile = Paths.get(tmpFolder.getPath(), "src", "squashTA", "resources", "junit", "java", "SeleniumRobotTest.java").toFile();
		FileUtils.writeStringToFile(javaFile, "javaExist");
		
		TaFolderStructureGenerator structGen = new TaFolderStructureGenerator("core", tmpFolder.getAbsolutePath(), tmpFolder.getAbsolutePath());
		try {
			structGen.generateDefaultStructure();
			Assert.assertTrue(javaFile.exists());
			Assert.assertEquals(FileUtils.readFileToString(javaFile), "javaExist");
		} finally {
			FileUtils.cleanDirectory(tmpFolder);
			tmpFolder.delete();
		}
	}
	
	/**
	 * folder structure generation with java file present in output. We check that it's overwritten
	 * @throws IOException
	 */
	@Test(groups={"squash"})
	public void testGenerateStructureWithExistingJavaInOutput() throws IOException {
		File tmpFolder = Paths.get(SeleniumTestsContextManager.getDataPath(), "tmp").toFile();
		File javaFile = Paths.get(tmpFolder.getPath(), "src", "squashTA", "resources", "junit", "java", "SeleniumRobotTest.java").toFile();
		FileUtils.writeStringToFile(javaFile, "javaExist");
		
		TaFolderStructureGenerator structGen = new TaFolderStructureGenerator("core", null, tmpFolder.getAbsolutePath());
		try {
			structGen.generateDefaultStructure();
			Assert.assertTrue(javaFile.exists());
			Assert.assertNotEquals(FileUtils.readFileToString(javaFile), "javaExist");
		} finally {
			FileUtils.cleanDirectory(tmpFolder);
			tmpFolder.delete();
		}
	}
}
