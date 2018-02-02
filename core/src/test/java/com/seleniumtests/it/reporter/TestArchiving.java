package com.seleniumtests.it.reporter;

import java.io.File;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContext;
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

			executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
			Assert.assertTrue(tmpZip.exists());
			File outputFolder = FileUtility.unzipFile(tmpZip);
			
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "SeleniumTestReport.html").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "results.json").toFile().exists());
			Assert.assertTrue(Paths.get(outputFolder.getAbsolutePath(), "PERF-com.seleniumtests.it.stubclasses.StubTestClass.testAndSubActions.xml").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.ARCHIVE_TO_FILE);
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

		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"});
		
		Assert.assertFalse(tmpZip.exists());
	}
}
