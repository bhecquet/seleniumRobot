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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.mockito.Mock;
import org.openqa.selenium.Proxy.ProxyType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestRunner;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.reporter.ReportInfo;

/**
 * Test parsing of test options into SeleniumTestContext
 * Tests will only be done on ThreadContext
 * This test MUST be executed through the tu.xml file as this file defines some parameters used by this test
 * @author behe
 *
 */
@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class, SeleniumGridConnectorFactory.class})
public class TestSeleniumTestContext extends MockitoTest {
	
	@Mock
	private SeleniumRobotVariableServerConnector variableServer;
	
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
	@Test(groups={"ut context"})
	public void testPlatformParsingForIOSWithoutVersion(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("platform", "iOS");
			System.setProperty("deviceName", "");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getPlatform(), "iOS");
			Assert.assertEquals(seleniumTestsCtx.getMobilePlatformVersion(), null);
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
	public void testReplayWaitTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReplayTimeout(), 15);
	}
	@Test(groups="ut context")
	public void testReplayWaitTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReplayTimeout(), 30);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBrowser(), BrowserType.NONE);
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
	public void testSnapshotTopCropping(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping(), 5);
	}
	@Test(groups="ut context")
	public void testSnapshotTopCroppingNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping(), 0);
	}
	
	@Test(groups="ut context")
	public void testSnapshotBottomCropping(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping(), 5);
	}
	@Test(groups="ut context")
	public void testSnapshotBottomCroppingNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping(), 0);
	}
	
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testSeleniumRobotServerActive(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerActive());
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerActiveWithoutUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl("http://localhost:8000");
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerActive());
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerActiveNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(null);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerActive());
	}
	
	@Test(groups="ut context")
	public void testSeleniumRobotServerUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl("http://localhost:8000");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerUrl(), "http://localhost:8000");
	}
	@Test(groups="ut context")
	public void testSeleniumRobotServerUrlNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerUrl());
	}
	
	@Test(groups="ut context")
	public void testCompareSnapshot(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerCompareSnapshot(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerCompareSnapshot(), true);
	}
	@Test(groups="ut context")
	public void testCompareSnapshotNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerCompareSnapshot(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerCompareSnapshot(), false);
	}
	
	@Test(groups="ut context")
	public void testRecordResults(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerRecordResults(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerRecordResults(), true);
	}
	@Test(groups="ut context")
	public void testRecordResultsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerRecordResults(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerRecordResults(), false);
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
	public void testFullReset(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setFullReset(false);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().getFullReset());
	}
	@Test(groups="ut context")
	public void testFullResetNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setFullReset(null);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getFullReset());
	}
	
	@Test(groups="ut context")
	public void testTms(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTms("{'type':'hp', 'run':1}");
		Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext().getTms());
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getTms() instanceof TestManager);
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testWrongType(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTms("{'type':'sonar'}");
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testWrongFormat(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTms("hp");
	}
	@Test(groups="ut context")
	public void testTmsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setTms(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getTms());
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
	public void testOutputDirectoryAbsolutePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory("/home/user/test-output", testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().endsWith("/home/user/test-output"));
	}
	@Test(groups="ut context")
	public void testOutputDirectoryRelativePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory("test-output/someSubdir", testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().endsWith("core/test-output/someSubdir"));
	}
	@Test(groups="ut context")
	public void testOutputDirectoryFromSystem(final ITestContext testNGCtx, final XmlTest xmlTest) {
		System.setProperty(SeleniumTestsContext.OUTPUT_DIRECTORY, "/home/user/test-output");
		initThreadContext(testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().endsWith("/home/user/test-output"));
	}
	@Test(groups="ut context")
	public void testOutputDirectoryNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		((TestRunner)testNGCtx).setOutputDirectory("/home/other/test-output/testsuite");
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory(null, testNGCtx);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "/home/other/test-output".replace("/", File.separator));
	}
	
	@Test(groups="ut context")
	public void testHeadlessMode(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setHeadlessBrowser(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().isHeadlessBrowser());
	}
	@Test(groups="ut context")
	public void testHeadlessModeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setHeadlessBrowser(null);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().isHeadlessBrowser());
	}
	
	@Test(groups="ut context")
	public void testManualSteps(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().isManualTestSteps());
	}
	@Test(groups="ut context")
	public void testManualStepsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(null);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().isManualTestSteps());
	}
	
	@Test(groups="ut context")
	public void testCustomTestsReports(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomTestReports("PERF::xml::reporter/templates/report.part.test.vm,SUPERVISION::xml::reporter/templates/report.part.test.step.vm");
		List<ReportInfo> reportInfos = SeleniumTestsContextManager.getThreadContext().getCustomTestReports();
		Assert.assertEquals(reportInfos.size(), 2);
		Assert.assertEquals(reportInfos.get(0).getExtension(), ".xml");
		Assert.assertEquals(reportInfos.get(0).getTemplatePath(), "reporter/templates/report.part.test.vm");
		Assert.assertEquals(reportInfos.get(0).getPrefix(), "PERF");
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testCustomTestsReportsWrongPath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomTestReports("PERF::xml::reporter/templates/report.part.test.wrong.vm");
		SeleniumTestsContextManager.getThreadContext().getCustomTestReports();
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testCustomTestsReportsWrongFormat(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomTestReports("PERF:xml::reporter/templates/report.part.test.wrong.vm");
		SeleniumTestsContextManager.getThreadContext().getCustomTestReports();
	}
	@Test(groups="ut context")
	public void testCustomTestsReportsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomTestReports(null);
		List<ReportInfo> reportInfos = SeleniumTestsContextManager.getThreadContext().getCustomTestReports();
		Assert.assertEquals(reportInfos.size(), 1);
		Assert.assertEquals(reportInfos.get(0).getExtension(), ".xml");
		Assert.assertEquals(reportInfos.get(0).getTemplatePath(), "reporter/templates/report.perf.vm");
		Assert.assertEquals(reportInfos.get(0).getPrefix(), "PERF");
	}
	
	@Test(groups="ut context")
	public void testCustomSummaryReports(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomSummaryReports("PERF::xml::reporter/templates/report.part.test.vm,SUPERVISION::xml::reporter/templates/report.part.test.step.vm");
		List<ReportInfo> reportInfos = SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports();
		Assert.assertEquals(reportInfos.size(), 2);
		Assert.assertEquals(reportInfos.get(0).getExtension(), ".xml");
		Assert.assertEquals(reportInfos.get(0).getTemplatePath(), "reporter/templates/report.part.test.vm");
		Assert.assertEquals(reportInfos.get(0).getPrefix(), "PERF");
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testCustomSummaryReportsWrongPath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomSummaryReports("PERF::xml::reporter/templates/report.part.test.wrong.vm");
		SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports();
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testCustomSummaryReportsWrongFormat(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomSummaryReports("PERF:xml::reporter/templates/report.part.test.wrong.vm");
		SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports();
	}
	@Test(groups="ut context")
	public void testCustomSummaryReportsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomSummaryReports(null);
		List<ReportInfo> reportInfos = SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports();
		Assert.assertEquals(reportInfos.size(), 1);
		Assert.assertEquals(reportInfos.get(0).getExtension(), ".json");
		Assert.assertEquals(reportInfos.get(0).getTemplatePath(), "reporter/templates/report.summary.json.vm");
		Assert.assertEquals(reportInfos.get(0).getPrefix(), "results");
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
		seleniumContext.setTestConfiguration();
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
	
	/**
	 * Check we create a variable server if all connexion params are present
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut context")
	public void testVariableServerConnection(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(true);
			
			initThreadContext(testNGCtx, "myTest");
			
			// check upsert has been called
			verify(variableServer).isAlive();
			
			Assert.assertNotNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check we create a no variable server if all it's not active
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut context")
	public void testNoVariableServerIfNotRequested(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "false");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			initThreadContext(testNGCtx, "myTest");
		
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getVariableServer());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Check we create a no variable server if no URL
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testNoVariableServerIfNoURL(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			
			initThreadContext(testNGCtx, "myTest");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
		}
	}
	
	/**
	 * Check we create a no variable server if not active
	 * @param testNGCtx
	 * @param xmlTest
	 * @throws Exception
	 */
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testNoVariableServerIfNotAlive(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE, "true");
			System.setProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL, "http://localhost:1234");
			
			PowerMockito.whenNew(SeleniumRobotVariableServerConnector.class).withAnyArguments().thenReturn(variableServer);
			when(variableServer.isAlive()).thenReturn(false);
			
			initThreadContext(testNGCtx, "myTest");
			
			// check upsert has been called
			verify(variableServer).isAlive();			
		} finally {
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_ACTIVE);
			System.clearProperty(SeleniumTestsContext.SELENIUMROBOTSERVER_URL);
		}
	}
	
	/**
	 * Test that a grid connection is created when all parameters are correct
	 * @param testNGCtx
	 */
	@Test(groups="ut context")
	public void testGridConnection(final ITestContext testNGCtx) {
		
		SeleniumGridConnector gridConnector = new SeleniumGridConnector("http://localhost:4444/hub/wd");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			
			initThreadContext(testNGCtx, "myTest");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(), gridConnector);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * Local test, no grid connector
	 * @param testNGCtx
	 */
	@Test(groups="ut context")
	public void testNoGridConnection(final ITestContext testNGCtx) {
		
		SeleniumGridConnector gridConnector = new SeleniumGridConnector("http://localhost:4444/hub/wd");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "local");
			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/hub/wd");
			
			initThreadContext(testNGCtx, "myTest");
			Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
			System.clearProperty(SeleniumTestsContext.WEB_DRIVER_GRID);
		}
	}
	
	/**
	 * grid test but missing url
	 * @param testNGCtx
	 */
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testGridConnectionWithoutUrl(final ITestContext testNGCtx) {
		
		SeleniumGridConnector gridConnector = new SeleniumGridConnector("http://localhost:4444/hub/wd");
		
		PowerMockito.mockStatic(SeleniumGridConnectorFactory.class);
		PowerMockito.when(SeleniumGridConnectorFactory.getInstance("http://localhost:4444/hub/wd")).thenReturn(gridConnector);
		
		try {
			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
			
			initThreadContext(testNGCtx, "myTest");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.RUN_MODE);
		}
	}
	
	
}
