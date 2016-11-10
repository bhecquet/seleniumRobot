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
package com.seleniumtests.ut.core;

import org.openqa.selenium.Proxy.ProxyType;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestRunner;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;

/**
 * Test parsing of test options into SeleniumTestContext
 * Tests will only be done on ThreadContext
 * This test MUST be executed through the tu.xml file as this file defines some parameters used by this test
 * @author behe
 *
 */
public class TestSeleniumTestContext extends GenericTest {

	private void initThreadContext(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
	}
	
	/**
	 * If parameter is only defined in test suite, it's correctly read
	 */
	@Test(groups={"ut context"})
	public void testSuiteLevelParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getImplicitWaitTimeout(), 2);
		
	}
	
	/**
	 * If parameter is defined in suite and several tests, check it's the right param value which is picked up
	 */
	@Test(groups={"ut context"})
	public void testMultipleTestShareSameParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getAttribute("variable1"), "value1");
		
	}
	
	/**
	 * If parameter is defined in test suite and test, the test parameter must override suite parameter
	 */
	@Test(groups={"ut context"})
	public void testTestLevelParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getApp(), "https://www.google.fr");
	}
	
	/**
	 * If parameter is defined in test suite and test, the test parameter must override suite parameter
	 * Here, we check that it's the right test parameter which is get (same parameter defined in several tests with different
	 * values
	 */
	@Test(groups={"ut context"})
	public void testTestLevelParam2(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getAttribute("variable1"), "value1");
	}
	
	/**
	 * If parameter is defined in test and as JVM parameter (user defined), the user defined parameter must be used
	 */
	@Test(groups={"ut context"})
	public void testUserDefinedParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("dpTagsInclude", "anOtherTag");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getDPTagsInclude(), "anOtherTag");
		} finally {
			System.clearProperty("dpTagsInclude");
		}
	}
	
	/**
	 * Check that unknown parameters are also stored in contextMap
	 */
	@Test(groups={"ut context"})
	public void testUndefinedParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getAttribute("aParam"), "value1");
	}
	
	/**
	 * Check that unknown parameters in test override the same param in test suite
	 */
	@Test(groups={"ut context"})
	public void testUndefinedParamOverride(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getAttribute("anOtherParam"), "value3");
	}
	
	/**
	 * Test parsing of platform name. For Desktop case, version and OS name are not split
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups={"ut context"})
	public void testPlatformParsingForWindows(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("platform", "Windows 7");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "Windows 7");
			Assert.assertEquals(seleniumTestsCtx.getMobilePlatformVersion(), null);
		} finally {
			System.clearProperty("platform");
			System.clearProperty("deviceName");
		}
	}
	@Test(groups={"ut context"})
	public void testPlatformParsingForOSX(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("platform", "os x 10.10");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "os x 10.10");
			Assert.assertEquals(seleniumTestsCtx.getMobilePlatformVersion(), null);
		} finally {
			System.clearProperty("platform");
			System.clearProperty("deviceName");
		}
	}
	@Test(groups={"ut context"})
	public void testPlatformParsingForAndroid(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("platform", "Android 5.0");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "Android");
			Assert.assertEquals(seleniumTestsCtx.getMobilePlatformVersion(), "5.0");
		} finally {
			System.clearProperty("platform");
			System.clearProperty("deviceName");
		}
	}
	
	/**
	 * Same test as above except that platform is set on test level, not in system property
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups={"ut context"})
	public void testPlatformParsingForAndroidFromTest(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			xmlTest.addParameter("platform", "Android 5.0");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "Android");
			Assert.assertEquals(seleniumTestsCtx.getMobilePlatformVersion(), "5.0");
		} finally {
			xmlTest.getLocalParameters().remove("platform");
			System.clearProperty("deviceName");
		}
	}
	@Test(groups={"ut context"})
	public void testPlatformParsingForIOS(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("platform", "iOS 9.01");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "iOS");
			Assert.assertEquals(seleniumTestsCtx.getMobilePlatformVersion(), "9.01");
		} finally {
			System.clearProperty("platform");
			System.clearProperty("deviceName");
		}
	}
	@Test(groups={"ut context"}, expectedExceptions=ConfigurationException.class)
	public void testPlatformParsingForIOSWithoutVersion(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("platform", "iOS");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "iOS");
		} finally {
			System.clearProperty("platform");
			System.clearProperty("deviceName");
		}
	}
	
	// test accessor + default values
	@Test(groups="ut context")
	public void testDataFile(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTestDataFile("DataFile");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestDataFile(), "DataFile");
	}
	@Test(groups="ut context")
	public void testDataFileNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTestDataFile(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestDataFile(), "testCase");
	}
	
	@Test(groups="ut context")
	public void testWebSessionTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebSessionTimeout(15000);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout(), 15000);
	}
	@Test(groups="ut context")
	public void testWebSessionTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebSessionTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout(), 90000);
	}
	
	@Test(groups="ut context")
	public void testImplicitWaitTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getImplicitWaitTimeout(), 15);
	}
	@Test(groups="ut context")
	public void testImplicitWaitTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getImplicitWaitTimeout(), 5);
	}
	
	@Test(groups="ut context")
	public void testExplicitWaitTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), 5);
	}
	@Test(groups="ut context")
	public void testExplicitWaitTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), 15);
	}
	
	@Test(groups="ut context")
	public void testPageLoadTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPageLoadTimeout(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getPageLoadTimeout(), 15);
	}
	@Test(groups="ut context")
	public void testPageLoadTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPageLoadTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getPageLoadTimeout(), 90);
	}
	
	@Test(groups="ut context")
	public void testRunMode(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("saucelabs");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRunMode(), DriverMode.SAUCELABS);
	}
	@Test(groups="ut context")
	public void testRunModeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRunMode(), DriverMode.LOCAL);
	}
	@Test(groups="ut context", expectedExceptions=IllegalArgumentException.class)
	public void testRunModeKo(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("unknown");
	}
	
	@Test(groups="ut context")
	public void testDevMode(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDevMode(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isDevMode(), true);
	}
	// by default, devMode is true if tests are launched from IDE
	@Test(groups="ut context")
	public void testDevModeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDevMode(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isDevMode(), true);
	}
	
	@Test(groups="ut context")
	public void testBrowser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBrowser(), BrowserType.CHROME);
	}
	@Test(groups="ut context")
	public void testBrowserNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBrowser(), BrowserType.FIREFOX);
	}
	@Test(groups="ut context", expectedExceptions=IllegalArgumentException.class)
	public void testBrowserKo(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("unknown");
	}

	@Test(groups="ut context")
	public void testAssumeUntrustedCertificateIssuer(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAssumeUntrustedCertificateIssuer(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAssumeUntrustedCertificateIssuer(), (Boolean)false);
	}
	@Test(groups="ut context")
	public void testAssumeUntrustedCertificateIssuerNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAssumeUntrustedCertificateIssuer(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAssumeUntrustedCertificateIssuer(), (Boolean)true);
	}
	
	@Test(groups="ut context")
	public void testAcceptUntrustedCertificates(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAcceptUntrustedCertificates(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAcceptUntrustedCertificates(), (Boolean)false);
	}
	@Test(groups="ut context")
	public void testAcceptUntrustedCertificatesNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAcceptUntrustedCertificates(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAcceptUntrustedCertificates(), (Boolean)true);
	}
	
	@Test(groups="ut context")
	public void testJavascriptEnabled(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setJavascriptEnabled(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getJavascriptEnabled(), (Boolean)false);
	}
	@Test(groups="ut context")
	public void testJavascriptEnabledNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setJavascriptEnabled(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getJavascriptEnabled(), (Boolean)true);
	}
	
	@Test(groups="ut context")
	public void testJsErrorCollectorExtension(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setJsErrorCollectorExtension(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getJsErrorCollectorExtension(), true);
	}
	@Test(groups="ut context")
	public void testJsErrorCollectorExtensionNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setJsErrorCollectorExtension(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getJsErrorCollectorExtension(), false);
	}
	
	@Test(groups="ut context")
	public void testUseDefaultFirefoxProfile(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setUseDefaultFirefoxProfile(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isUseFirefoxDefaultProfile(), true);
	}
	@Test(groups="ut context")
	public void testUseDefaultFirefoxProfileNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setUseDefaultFirefoxProfile(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isUseFirefoxDefaultProfile(), true);
	}
	
	@Test(groups="ut context")
	public void testCaptureSnapshot(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot(), false);
	}
	@Test(groups="ut context")
	public void testCaptureSnapshotNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot(), true);
	}
	
	@Test(groups="ut context")
	public void testEnableExceptionListener(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setEnableExceptionListener(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getEnableExceptionListener(), false);
	}
	@Test(groups="ut context")
	public void testEnableExceptionListenerNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setEnableExceptionListener(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getEnableExceptionListener(), true);
	}

	@Test(groups="ut context")
	public void testSoftAssertEnabled(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled(), false);
	}
	@Test(groups="ut context")
	public void testSoftAssertEnabledNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled(), true);
	}
	
	@Test(groups="ut context")
	public void testDeviceList(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDeviceList("{'dev1':'Samsung'}");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDeviceList().get("dev1"), "Samsung");
	}
	@Test(groups="ut context")
	public void testDeviceListNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDeviceList(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDeviceList().size(), 0);
	}
	
	@Test(groups="ut context")
	public void testApp(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp("app");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getApp(), "app");
	}
	@Test(groups="ut context")
	public void testAppNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getApp(), "");
	}
	
	@Test(groups="ut context")
	public void testCucumberTags(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCucumberTags("tag");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCucumberTags(), "tag");
	}
	@Test(groups="ut context")
	public void testCucumberTagsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCucumberTags(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCucumberTags(), "");
	}
	
	@Test(groups="ut context")
	public void testCucumberTests(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCucumberTests("tests");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCucumberTests().get(0), "tests");
	}
	@Test(groups="ut context")
	public void testCucumberTestsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCucumberTests(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCucumberTests().size(), 0);
	}
	
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testCucumberImplementationPackageNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCucumberTests("test");
		SeleniumTestsContextManager.getThreadContext().setCucumberTags(null);
		SeleniumTestsContextManager.getThreadContext().setCucumberImplementationPackage(null);
	}
	
	@Test(groups="ut context")
	public void testTestEnv(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTestEnv("VMOE");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestEnv(), "VMOE");
	}
	@Test(groups="ut context")
	public void testTestEnvNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTestEnv(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestEnv(), "DEV");
	}
	
	@Test(groups="ut context")
	public void testNewCommandTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNewCommandTimeout(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNewCommandTimeout(), 15);
	}
	@Test(groups="ut context")
	public void testNewCommandTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNewCommandTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNewCommandTimeout(), 120);
	}
	
	@Test(groups="ut context")
	public void testOutputDirectory(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory("/home/user/test-output", testNGCtx);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "/home/user/test-output");
	}
	@Test(groups="ut context")
	public void testOutputDirectoryNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		((TestRunner)testNGCtx).setOutputDirectory("/home/other/test-output/testsuite");
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory(null, testNGCtx);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "\\home\\other\\test-output");
	}
	
	/**
	 * Proxy type is set to "direct" in config.ini. check that this value is taken
	 */
	@Test(groups="ut context")
	public void testProxyFromEnvIniFile(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyType(), ProxyType.DIRECT);
	}
	@Test(groups="ut context")
	public void testProxyPreset(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
		SeleniumTestsContext seleniumContext = new SeleniumTestsContext();
		seleniumContext.postsetProxyConfig();
		Assert.assertEquals(seleniumContext.getWebProxyType(), ProxyType.DIRECT);
	}
	@Test(groups="ut context")
	public void testProxyOverride(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("proxyType", "system");
			initThreadContext(testNGCtx);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyType(), ProxyType.SYSTEM);
		} finally {
			System.clearProperty("proxyType");
		}
	}
	@Test(groups="ut context")
	public void testProxyType(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyType("PAC");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyType(), ProxyType.PAC);
	}
	/**
	 * Proxy type default value is set in postInit method, so setting null / unknown value will result in null
	 */
	@Test(groups="ut context")
	public void testProxyTypeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyType(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyType(), null);
	}
	
	@Test(groups="ut context")
	public void testProxyAddress(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyAddress("address");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyAddress(), "address");
	}
	
	@Test(groups="ut context")
	public void testProxyPort(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyPort(8080);
		Assert.assertEquals((int)SeleniumTestsContextManager.getThreadContext().getWebProxyPort(), (int)8080);
	}
	
	@Test(groups="ut context")
	public void testProxyLogin(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyLogin("login");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyLogin(), "login");
	}
	
	@Test(groups="ut context")
	public void testProxyPassword(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyPassword("password");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyPassword(), "password");
	}
	
	@Test(groups="ut context")
	public void testProxyExclude(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyExclude("127.0.0.1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyExclude(), "127.0.0.1");
	}
	
	@Test(groups="ut context")
	public void testProxyPac(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebProxyPac("http://pac");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyPac(), "http://pac");
	}
	
	@Test(groups="ut context")
	public void testProxyFilledInDefaultContext(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		
		// default proxy type value get from config.ini file
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyType(), ProxyType.DIRECT);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyAddress(), null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyPort(), null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyLogin(), null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyPassword(), null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyExclude(), null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyPac(), null);
	}
	
	
}
