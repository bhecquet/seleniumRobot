/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.it.reporter;

import java.io.File;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.FileUtility;

public class TestArchiving extends ReporterTest {

	/**
	 * Check archive is created and content is correct
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingEnabled(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, "true");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
			Assert.assertTrue(tmpZip.exists());
			File outputFolder = FileUtility.unzipFile(tmpZip);
			
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "SeleniumTestReport.html").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "results.json").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "testAndSubActions", "PERF-result.xml").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	
	/**
	 * Check that we are able to create a zip file inside the output directory itself without writing the zip file in itself (issue #168)
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingInOutputDirectory(ITestContext testContext) throws Exception {
		String zipFilePath = SeleniumTestsContextManager.getGlobalContext().getOutputDirectory() + File.separator + "result.zip";
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, zipFilePath);
			System.setProperty(SeleniumTestsContext.ARCHIVE, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			Assert.assertTrue(new File(zipFilePath).exists());
			File outputFolder = FileUtility.unzipFile(new File(zipFilePath));
			
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "SeleniumTestReport.html").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "results.json").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "testAndSubActions", "PERF-result.xml").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	
	/**
	 * Archive must be produced when on success and tests are OK
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingOnSuccessWithTestsOK(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, "onSuccess");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			Assert.assertTrue(tmpZip.exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	
	/**
	 * Archive should not be produced when on success and tests are KO
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingOnSuccessWithTestsKO(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, "onSuccess");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			Assert.assertFalse(tmpZip.exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	/**
	 * Archive should not be produced when on error and tests are OK
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingOnErrorWithTestsOK(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, "onError");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			Assert.assertFalse(tmpZip.exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	
	/**
	 * Archive should be produced when on success and tests are KO
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingOnErrorWithTestsKO(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, "onError");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			Assert.assertTrue(tmpZip.exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	
	/**
	 * Archive is not produced if file is not specified
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingEnabledWithoutFile(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		
		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			Assert.assertFalse(tmpZip.exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}
	
	/**
	 * Check archive is not created
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testArchivingDisabled(ITestContext testContext) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		Assert.assertFalse(tmpZip.exists());
	}
}
