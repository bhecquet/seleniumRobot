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
package com.seleniumtests.it.reporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.FileUtility;

public class TestArchiving extends ReporterTest {

	/**
	 * Check archive is created and content is correct
	 */
	@Test(groups={"it"})
	public void testArchivingEnabled() throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();
		File outputFolder = null;

		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, "true");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			Assert.assertTrue(tmpZip.exists());
			outputFolder = FileUtility.unzipFile(tmpZip);

			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "SeleniumTestReport.html").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "results.json").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "testAndSubActions", "PERF-result.xml").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "testAndSubActions", "detailed-result.json").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);

			try {
				if (outputFolder != null) {
					FileUtils.deleteDirectory(outputFolder);
				}
			} catch (IOException e) {
				//
			}
		}
	}

	/**
	 * Check that we are able to create a zip file inside the output directory itself without writing the zip file in itself (issue #168)
	 */
	@Test(groups={"it"})
	public void testArchivingInOutputDirectory() throws Exception {
		String zipFilePath = SeleniumTestsContextManager.getGlobalContext().getOutputDirectory() + File.separator + "result.zip";
		File outputFolder = null;

		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, zipFilePath);
			System.setProperty(SeleniumTestsContext.ARCHIVE, "true");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});

			Assert.assertTrue(new File(zipFilePath).exists());
			outputFolder = FileUtility.unzipFile(new File(zipFilePath));

			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "SeleniumTestReport.html").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "results.json").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "testAndSubActions", "PERF-result.xml").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "testAndSubActions", "detailed-result.json").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);

			try {
				if (outputFolder != null) {
					FileUtils.deleteDirectory(outputFolder);
				}
			} catch (IOException e) {
				//
			}
		}
	}

	public void testArchiving(String archiveOption, String testToExecute) throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();

		try {
			System.setProperty(SeleniumTestsContext.ARCHIVE_TO_FILE, tmpZip.getAbsolutePath());
			System.setProperty(SeleniumTestsContext.ARCHIVE, archiveOption);

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {testToExecute});

			Assert.assertTrue(tmpZip.exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
			System.clearProperty(SeleniumTestsContext.ARCHIVE);
		}
	}

	/**
	 * Archive must be produced when on success and tests are OK
	 */
	@Test(groups={"it"})
	public void testArchivingOnSuccessWithTestsOK() throws Exception {
		testArchiving( "onSuccess", "testAndSubActions");
	}

	/**
	 * Archive must be produced when on skipped and tests are skipped
	 */
	@Test(groups={"it"})
	public void testArchivingOnSkipWithTestsSkipped() throws Exception {
		testArchiving("onSkip", "testSkipped");
	}

	/**
	 * issue #344: Test multiple archiving are possible
	 */
	@Test(groups={"it"})
	public void testArchivingOnSuccessOrOnErrorWithTestsOK() throws Exception {
		testArchiving( "onError,onSuccess", "testAndSubActions");
	}

	/**
	 * issue #344: Test multiple archiving are possible
	 */
	@Test(groups={"it"})
	public void testArchivingOnSuccessOrOnErrorWithTestsKO() throws Exception {
		testArchiving( "onError,onSuccess", "testInError");
	}

	/**
	 * Archive should be produced when on success and tests are KO
	 */
	@Test(groups={"it"})
	public void testArchivingOnErrorWithTestsKO() throws Exception {
		testArchiving( "onError", "testInError");
	}

	/**
	 * Archive should not be produced when on success and tests are KO
	 */
	@Test(groups={"it"})
	public void testArchivingOnSuccessWithTestsKO() throws Exception {
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
	 */
	@Test(groups={"it"})
	public void testArchivingOnErrorWithTestsOK() throws Exception {
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
	 * Archive is not produced if file is not specified
	 */
	@Test(groups={"it"})
	public void testArchivingEnabledWithoutFile() throws Exception {
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
	 */
	@Test(groups={"it"})
	public void testArchivingDisabled() throws Exception {
		File tmpZip = File.createTempFile("archive", ".zip");
		tmpZip.delete();

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
		
		Assert.assertFalse(tmpZip.exists());
	}
}
