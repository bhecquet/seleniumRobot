package com.seleniumtests.ut.driver;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.util.osutility.OSUtility;

public class TestDriverExtractor extends MockitoTest {
	
	@BeforeClass(groups={"it"})
	public void initContext(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
	}
	
	@Test(groups={"ut"})
	public void testDriverExtraction() throws IOException {
		
		Path driverPath = DriverExtractor.getDriverPath();
		
		// clean output directory
		FileUtils.deleteDirectory(driverPath.toFile());

		DriverExtractor extractor = new DriverExtractor();
		extractor.extractDriver("chromedriver");
		
		if (OSUtility.isWindows()) {
			Assert.assertTrue(Paths.get(driverPath.toString(), "chromedriver.exe").toFile().exists());
		} else {
			Assert.assertTrue(Paths.get(driverPath.toString(), "chromedriver").toFile().exists());
		}
		Assert.assertTrue(Paths.get(driverPath.toString(), "version_chromedriver.txt").toFile().exists());
	}
	
	/**
	 * Driver file already exists with version file up to date
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDriverNotExtractedAlreadyExists() throws IOException {
		
		Path driverPath = DriverExtractor.getDriverPath();
		
		// clean output directory
		FileUtils.deleteDirectory(driverPath.toFile());

		new DriverExtractor().extractDriver("chromedriver");
		
		DriverExtractor driverExtractor = spy(new DriverExtractor());
		driverExtractor.extractDriver("chromedriver");
		
		// check driver has not been copied as it already exists in the right version
		verify(driverExtractor, never()).copyDriver("chromedriver");
	}
	
	/**
	 * Error handling when the specified driver does not exist
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions=DriverExceptions.class)
	public void testCopyDriverNull() throws IOException {
		new DriverExtractor().extractDriver("toto");
	}
	
	/**
	 * driver extracted as version file does not exist
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDriverNotExtractedAlreadyExistsNoVersion() throws IOException {
		
		Path driverPath = DriverExtractor.getDriverPath();
		
		// clean output directory
		FileUtils.deleteDirectory(driverPath.toFile());

		new DriverExtractor().extractDriver("chromedriver");
		
		// remove version to force copy
		Paths.get(driverPath.toString(), "version_chromedriver.txt").toFile().delete();
		
		DriverExtractor driverExtractor = spy(new DriverExtractor());
		driverExtractor.extractDriver("chromedriver");
		
		// check driver has been copied as it already exists but no version has been specified
		verify(driverExtractor).copyDriver("chromedriver");
		
	}

}
