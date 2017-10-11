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
package com.seleniumtests.ut.core.config;

import java.io.File;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.config.ConfigReader;
import com.seleniumtests.customexception.ConfigurationException;

public class TestConfigReader extends GenericTest {

	@Test(groups={"ut"})
	public void readConfigurationWithValueOverride() {
		Map<String, String> config = new ConfigReader().readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/env.ini"), "DEV");
		Assert.assertEquals(config.get("key1"), "value4", "Key override does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationWithoutValueOverride() {
		Map<String, String> config = new ConfigReader().readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/env.ini"), "VNR");
		Assert.assertEquals(config.get("key1"), "value1", "Key should not be overriden");
	}
	
	@Test(groups={"ut context"})
	public void getConfigFile(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		File config = ConfigReader.getConfigFile();
		Assert.assertEquals(config.getName(), "config.ini", "Key should not be overriden");
		Assert.assertTrue(config.getAbsolutePath().contains("core"));
	}
	
	/**
	 * Check that parameters in XML (which are not identified as a seleniumRobot option) are also put in testConfiguration
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups={"ut context"})
	public void mergeIniAndXmlConfiguration(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();

		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("variable1"), "value1", "Value has not been get from xml file");
	}
	
	/**
	 * Check that through context, configuration is correctly read
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testVariableOverwriteByEnvironment(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		
		// check value from a loaded additional ini file is present in configuration
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1"), "value4");
	}
	
	/**
	 * Check we are able to load several ini files in the order they are specified. Last file overwrites variable of previous if same variable name is provided
	 * These files are searched in "config" folder
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testLoadOneFile(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.LOAD_INI, "envSpecific.ini");
		
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			
			// check value from a loaded additional ini file is present in configuration
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key20"), "value20");
			
			// check variable overwriting is also OK on loaded files
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key10"), "value40");
			
			// check values are overwritten by loaded ini file if the same exists (for general and env specific)
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key2"), "value20");
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key3"), "value30");
			
			// check that if value is not present in additional file, it's taken from env.ini/config.ini
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("proxyType"), "direct");
		} finally {
			System.clearProperty(SeleniumTestsContext.LOAD_INI);
		}
	}
	
	/**
	 * Check we can read files in sub folders of config
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testLoadFileSubFolder(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.LOAD_INI, "spec/envSpecific2.ini");
		
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			
			// check variable overwriting is also OK on loaded files
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key10"), "value100");

		} finally {
			System.clearProperty(SeleniumTestsContext.LOAD_INI);
		}
	}
	
	/**
	 * Last file variables take precedence
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testLoadMultipleFiles(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.LOAD_INI, "envSpecific.ini,spec/envSpecific2.ini");
			
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			
			// check variable overwriting is also OK from last loaded files
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key10"), "value100");
			
			// check values are overwritten by first loaded ini file 
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key2"), "value20");
			
			// check that if value is not present in additional file, it's taken from env.ini/config.ini
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("proxyType"), "direct");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.LOAD_INI);
		}
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testLoadNotExistingFile(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.LOAD_INI, "spec/envSpecific.ini");
		
			initThreadContext(testNGCtx);

		} finally {
			System.clearProperty(SeleniumTestsContext.LOAD_INI);
		}
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testFileWithWrongFormat(final ITestContext testNGCtx) {
		try {
			System.setProperty(SeleniumTestsContext.LOAD_INI, "spec/wrongFileFormat.txt");
			
			initThreadContext(testNGCtx);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.LOAD_INI);
		}
	}
	
	// TODO: test with invalid file format
}
