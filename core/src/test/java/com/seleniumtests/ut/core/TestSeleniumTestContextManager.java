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
package com.seleniumtests.ut.core;

import java.io.File;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.PackageUtility;

/**
 * The first 4 tests will fail when executed directly because they use the tu.xml specific configurations
 * @author behe
 *
 */
public class TestSeleniumTestContextManager extends GenericTest {

	
	@Test(groups={"ut"})
	public void extendedConfigurationIsWrittentIntoCurrentTest(ITestContext iTestContext) {
		try {
			System.setProperty(SeleniumTestsContext.DEVICE_NAME, "Samsung Galaxy Nexus SPH-L700 4.3");
			initThreadContext(iTestContext);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getPlatform(), "Android");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion(), "4.3");
		} finally {
			System.clearProperty(SeleniumTestsContext.DEVICE_NAME);
		}
	}
	
	/**
	 * Check that version is shorten: 3.0.0 => 3.0
	 * 
	 * /!\ This test can only be successful once a 'mvn package' has been performed so that the ant task generates the right file
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		String packageVersion = PackageUtility.getVersion();
		Assert.assertTrue(packageVersion.contains(SeleniumTestsContextManager.getApplicationVersion()));
		Assert.assertTrue(packageVersion.length() > SeleniumTestsContextManager.getApplicationVersion().length());
	}
	
	/**
	 * Empty version => 0.0
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadEmptyApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/core-version-empty.txt"), "0.0");
	}
	
	/**
	 * 1.2alpha-all.3 => 1.2alpha-all
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadComplicatedApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/core-version-complicated.txt"), "1.2alpha-all");
	}
	
	/**
	 * 1.2alpha-all.3 => 1.2alpha-all
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadOneDigitApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/core-version-one-digit.txt"), "1");
	}
	
	/**
	 * no version => 0.0
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadUnexistingApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/version.txt"), "0.0");
	}
	
	/**
	 * Empty version => 0.0
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadFullEmptyApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/core-version-empty.txt", true), "0.0");
	}
	
	/**
	 * 1.2alpha-all.3 => 1.2alpha-all.3
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadFullComplicatedApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/core-version-complicated.txt", true), "1.2alpha-all.3");
	}
	
	/**
	 * no version => 0.0
	 * @param iTestContext
	 */
	@Test(groups={"ut"})
	public void testReadFullUnexistingApplicationVersion(ITestContext iTestContext) {
		initThreadContext(iTestContext);
		Assert.assertEquals(SeleniumTestsContextManager.readApplicationVersion("tu/version.txt", true), "0.0");
	}
	
	/**
	 * Check application path is read from TestNG xml file path
	 */
	@Test(groups= {"ut"})
	public void testGenerateApplicationPath() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/data/myApp/testng/testSRTCManager.xml");
		
		SeleniumTestsContextManager.generateApplicationPath(suite);
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationName(), "myApp");
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationDataPath().replace(File.separator, "/"), "/home/test/seleniumRobot/data/myApp");
		Assert.assertEquals(SeleniumTestsContextManager.getDataPath().replace(File.separator, "/"), "/home/test/seleniumRobot/data/");
		Assert.assertEquals(SeleniumTestsContextManager.getConfigPath(), "/home/test/seleniumRobot/data/myApp/config".replace("/", File.separator));
	}
	
	/**
	 * Check application path is read from TestNG xml file path and it drops version that could be present in path
	 * Test with standard format 'x.y.z'
	 */
	@Test(groups= {"ut"})
	public void testGenerateApplicationPathWithVersion() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/data/myApp_1.0.0/testng/testSRTCManager.xml");
		
		SeleniumTestsContextManager.generateApplicationPath(suite);
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationName(), "myApp");
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationDataPath().replace(File.separator, "/"), "/home/test/seleniumRobot/data/myApp_1.0.0");
		Assert.assertEquals(SeleniumTestsContextManager.getDataPath().replace(File.separator, "/"), "/home/test/seleniumRobot/data/");
		Assert.assertEquals(SeleniumTestsContextManager.getConfigPath(), "/home/test/seleniumRobot/data/myApp_1.0.0/config".replace("/", File.separator));
	}
	
	/**
	 * Check application path is read from TestNG xml file path and it drops version that could be present in path
	 * Test with short format 'x'
	 * 
	 */
	@Test(groups= {"ut"})
	public void testGenerateApplicationPathWithShortVersion() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/data/myApp_1/testng/testSRTCManager.xml");
		
		SeleniumTestsContextManager.generateApplicationPath(suite);
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationName(), "myApp");
		Assert.assertEquals(SeleniumTestsContextManager.getConfigPath(), "/home/test/seleniumRobot/data/myApp_1/config".replace("/", File.separator));
	}
	
	/**
	 * Check application path is read from TestNG xml file path and it drops version that could be present in path
	 * Test with short format 'x'
	 * 
	 */
	@Test(groups= {"ut"})
	public void testGenerateApplicationPathWithComplicatedVersion() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/data/myApp_alpha.1.2-beta/testng/testSRTCManager.xml");
		
		SeleniumTestsContextManager.generateApplicationPath(suite);
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationName(), "myApp");
		Assert.assertEquals(SeleniumTestsContextManager.getConfigPath(), "/home/test/seleniumRobot/data/myApp_alpha.1.2-beta/config".replace("/", File.separator));
	}
	
	/**
	 * Check that if TestNG XML file is not on the standard location, default application is core
	 */
	@Test(groups= {"ut"})
	public void testGenerateApplicationPathForIDE() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/testng/testSRTCManager.xml");
		
		SeleniumTestsContextManager.generateApplicationPath(suite);
		Assert.assertEquals(SeleniumTestsContextManager.getApplicationName(), "core");
	}
	
}
