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
package com.seleniumtests.ut.core.config;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.seleniumtests.MockitoTest;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.config.ConfigReader;
import com.seleniumtests.customexception.ConfigurationException;

public class TestConfigReader extends MockitoTest {

	@Test(groups={"ut"})
	public void readConfigurationWithValueOverride() {
		Map<String, TestVariable> config = new ConfigReader("DEV", null).readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/env.ini"));
		Assert.assertEquals(config.get("key1").getValue(), "value4", "Key override does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationWithoutValueOverride() {
		Map<String, TestVariable> config = new ConfigReader("VNR", null).readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/env.ini"));
		Assert.assertEquals(config.get("key1").getValue(), "value1", "Key should not be overriden");
	}

	/**
	 * Reads the default config.ini file
	 */
	@Test(groups={"ut"})
	public void testReadConfig() {
		Map<String, TestVariable> config = new ConfigReader("DEV", null).readConfig();
		Assert.assertEquals(config.get("key1").getValue(), "value4");
	}

	@Test(groups={"ut"})
	public void testReadConfigWithMoreFiles() {
		Map<String, TestVariable> config = new ConfigReader("DEV", "envSpecific.ini,spec/envSpecific2.ini").readConfig();
		Assert.assertEquals(config.size(), 8);
		Assert.assertEquals(config.get("key3").getValue(), "value30");
		Assert.assertEquals(config.get("key30").getValue(), "value300");
	}

	@Test(groups={"ut"})
	public void testReadConfigNoFile() {
		ConfigReader configReader = spy(new ConfigReader("DEV", null));
		when(configReader.getConfigFile()).thenThrow(new NullPointerException("No env.ini file"));
		Map<String, TestVariable> config = configReader.readConfig();
		Assert.assertEquals(config.size(), 0);
	}


	@Test(groups={"ut context"})
	public void getConfigFile(final ITestContext testNGCtx) throws IOException {
		initThreadContext(testNGCtx);
		InputStream config = new ConfigReader("DEV", null).getConfigFile();
		String configContent = IOUtils.toString(config, StandardCharsets.UTF_8);
		Assert.assertTrue(configContent.contains("key1=value1"));
	}

	/**
	 * Check case where application name is defined and data should be get from resources
	 * @param testNGCtx
	 * @throws IOException
	 */
	@Test(groups={"ut context"})
	public void getConfigFileFromResources(final ITestContext testNGCtx) throws IOException {
		String suiteFileName = testNGCtx.getCurrentXmlTest().getSuite().getFileName();
		try {
			System.setProperty("applicationName", "core2");

			testNGCtx.getCurrentXmlTest().getSuite().setFileName("/home/test/suite.xml");

			// regenerate variables to use the property set above
			SeleniumTestsContextManager.generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
			InputStream config = new ConfigReader("DEV", null).getConfigFile();
			String configContent = IOUtils.toString(config, StandardCharsets.UTF_8);
			Assert.assertTrue(configContent.contains("key1=core2"));
		} finally {
			testNGCtx.getCurrentXmlTest().getSuite().setFileName(suiteFileName);
			System.clearProperty("applicationName");
		}
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

		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("variable1").getValue(), "value1", "Value has not been get from xml file");
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
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "value4");
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
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key20").getValue(), "value20");
			
			// check variable overwriting is also OK on loaded files
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key10").getValue(), "value40");
			
			// check values are overwritten by loaded ini file if the same exists (for general and env specific)
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key2").getValue(), "value20");
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key3").getValue(), "value30");
			
			// check that if value is not present in additional file, it's taken from env.ini/config.ini
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("startedBy").getValue(), "local");
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
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key10").getValue(), "value100");

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
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key10").getValue(), "value100");
			
			// check values are overwritten by first loaded ini file 
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key2").getValue(), "value20");
			
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
