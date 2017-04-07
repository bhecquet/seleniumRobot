/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.ut.driver;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.osutility.OSUtility;

public class TestDriverExtractor extends MockitoTest {
	
	private String rootPath;
	private DriverExtractor extractor;
	private Path driverPath;
	
	@BeforeClass(groups={"ut"})
	public void initContext(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		rootPath = SeleniumTestsContextManager.getRootPath() + "/tmp";
		extractor = new DriverExtractor(rootPath);
		driverPath = extractor.getDriverPath();
	}
	
	@BeforeMethod(groups={"ut"})
	public void deleteTmpDir() throws IOException {
		
		
		// clean output directory
		for (int i = 0; i < 5; i++) {
			try {
				FileUtils.deleteDirectory(driverPath.toFile());
				break;
			} catch (IOException e) {
				WaitHelper.waitForSeconds(2);
			}
		}
	}
	
	@Test(groups={"ut"})
	public void testDriverExtraction() throws IOException {
		
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

		extractor.extractDriver("chromedriver");
		
		DriverExtractor driverExtractor = spy(new DriverExtractor(rootPath));
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
		new DriverExtractor(rootPath).extractDriver("toto");
	}
	
	/**
	 * driver extracted as version file does not exist
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDriverNotExtractedAlreadyExistsNoVersion() throws IOException {

		extractor.extractDriver("chromedriver");
		
		// remove version to force copy
		Paths.get(driverPath.toString(), "version_chromedriver.txt").toFile().delete();
		
		DriverExtractor driverExtractor = spy(new DriverExtractor(rootPath));
		driverExtractor.extractDriver("chromedriver");
		
		// check driver has been copied as it already exists but no version has been specified
		verify(driverExtractor).copyDriver("chromedriver");
		
	}
	
	@Test(groups={"ut"})
	public void testPomVersionReadingWithParent() throws Exception {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/pomWithParent.xml");
		String version = new DriverExtractor().getVersionFromPom(stream);
		Assert.assertEquals(version, "2.7.0-SNAPSHOT");
	}
	
	@Test(groups={"ut"})
	public void testPomVersionReadingWithoutParent() throws Exception {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/pomWithoutParent.xml");
		String version = new DriverExtractor().getVersionFromPom(stream);
		Assert.assertEquals(version, "2.7.0-SNAPSHOT");
	}

}
