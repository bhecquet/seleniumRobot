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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy.ProxyType;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestRunner;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
import com.seleniumtests.reporter.logger.ArchiveMode;
import com.seleniumtests.reporter.reporters.JUnitReporter;
import com.seleniumtests.reporter.reporters.ReportInfo;
import com.seleniumtests.reporter.reporters.TestManagerReporter;
import com.seleniumtests.uipage.htmlelements.ElementInfo;
import com.seleniumtests.ut.driver.WebDriverListener2;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.video.VideoCaptureMode;

/**
 * Test parsing of test options into SeleniumTestContext
 * Tests will only be done on ThreadContext
 * This test MUST be executed through the tu.xml file as this file defines some parameters used by this test
 * @author behe
 *
 */
public class TestSeleniumTestContext extends GenericTest {

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
	 * If parameter is defined in suite and several tests, check it's the right param value which is picked up (the one from test)
	 */
	@Test(groups={"ut context"})
	public void testMultipleTestShareSameParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("variable1").getValue(), "value1");	
	}
	
	/**
	 * If parameter is defined in test suite and test, the test parameter must override suite parameter
	 */
	@Test(groups={"ut context"})
	public void testTestLevelParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertTrue(seleniumTestsCtx.getBrowser().getBrowserType().toLowerCase().contains("firefox"));
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
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("variable1").getValue(), "value1");
	}
	
	/**
	 * If parameter is defined in env.ini and as JVM parameter (user defined), the user defined parameter must be used
	 */
	@Test(groups={"ut context"})
	public void testUserDefinedParamOverridesEnvIni(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty("key1", "userValue");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key1").getValue(), "userValue");
		} finally {
			System.clearProperty("key1");
		}
	}
	
	/**
	 * Check that unknown parameters are also stored in contextMap
	 */
	@Test(groups={"ut context"})
	public void testUndefinedParam(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("aParam").getValue(), "value1");
	}
	
	/**
	 * Check that unknown parameters in test override the same param in test suite
	 */
	@Test(groups={"ut context"})
	public void testUndefinedParamOverride(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("anOtherParam").getValue(), "value3");
	}
	
	/**
	 * Check that user defined arguments passed through command line (-DmyParam=myValue) is copied to testVariables and added to configuration 
	 */
	@Test(groups={"ut context"})
	public void testUserDefinedParamAddedToTestVariables(final ITestContext testNGCtx) {
		try {
			System.setProperty("myUserDefinedKey", "myUserDefinedValue");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("myUserDefinedKey").getValue(), "myUserDefinedValue");
			
		} finally {
			System.clearProperty("myUserDefinedKey");
		}
	}
	
	/**
	 * Check that when a user defined parameter is both defined in XML and on command line, the command line one has precedence
	 * @param testNGCtx
	 */
	@Test(groups={"ut context"})
	public void testUserDefinedParamOverriesXMLUserDefined(final ITestContext testNGCtx) {
		try {
			System.setProperty("variable1", "myUserDefinedValue");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("variable1").getValue(), "myUserDefinedValue");
			
		} finally {
			System.clearProperty("variable1");
		}
	}
	
	@Test(groups={"ut context"})
	public void testTechnicalParameterIsNotAddedToVariables(final ITestContext testNGCtx) {
		try {
			System.setProperty("browser", "safari");
			initThreadContext(testNGCtx);
			SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getThreadContext();
			Assert.assertNull(seleniumTestsCtx.getConfiguration().get("browser"));
			
		} finally {
			System.clearProperty("browser");
		}
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
	

	@Test(groups="ut context")
	public void testDeviceName(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDeviceName("myDevice");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDeviceName(), "myDevice");
	}
	@Test(groups="ut context")
	public void testDeviceNameNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDeviceName(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getDeviceName());
	}
	
	@Test(groups="ut context")
	public void testDeviceId(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDeviceId("myDevice");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDeviceId(), "myDevice");
	}
	@Test(groups="ut context")
	public void testDeviceNameId(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDeviceId(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getDeviceId());
	}
	
	@Test(groups="ut context")
	public void testWebSessionTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebSessionTimeout(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout(), 15000);
	}
	@Test(groups="ut context")
	public void testWebSessionTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebSessionTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout(), SeleniumTestsContext.DEFAULT_WEB_SESSION_TIMEOUT);
	}
	
	@Test(groups="ut context")
	public void testPageLoadStrategy(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPageLoadStrategy("eager");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getPageLoadStrategy(), PageLoadStrategy.EAGER);
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testWrongPageLoadStrategy(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPageLoadStrategy("foo");
	}
	@Test(groups="ut context")
	public void testPageLoadStrategyNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPageLoadStrategy(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getPageLoadStrategy(), PageLoadStrategy.NORMAL);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getImplicitWaitTimeout(), SeleniumTestsContext.DEFAULT_IMPLICIT_WAIT_TIME_OUT);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReplayTimeout(), SeleniumTestsContext.DEFAULT_REPLAY_TIME_OUT);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), SeleniumTestsContext.DEFAULT_EXPLICIT_WAIT_TIME_OUT);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getPageLoadTimeout(), SeleniumTestsContext.DEFAULT_PAGE_LOAD_TIME_OUT);
	}

	@Test(groups="ut context")
	public void testStartedBy(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setStartedBy("http://foo.bar/job/1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getStartedBy(), "http://foo.bar/job/1");
	}
	@Test(groups="ut context")
	public void testStartedByNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setStartedBy(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getStartedBy(), SeleniumTestsContext.DEFAULT_STARTED_BY);
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
	public void testNodeTags(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNodeTags("foo, bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNodeTags(), Arrays.asList("foo", "bar"));
	}
	@Test(groups="ut context")
	public void testNodeTagsEmpty(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNodeTags("");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNodeTags().size(), 0);
	}
	@Test(groups="ut context")
	public void testNodeTagsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNodeTags(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNodeTags().size(), 0);
	}
	

	@Test(groups="ut context")
	public void testWebDriverGrid(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub,http://localhost:4445/wd/hub");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebDriverGrid(), Arrays.asList("http://localhost:4444/wd/hub", "http://localhost:4445/wd/hub"));
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testWebDriverGridEmpty(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("");
	}
	@Test(groups="ut context")
	public void testWebDriverGridNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebDriverGrid().size(), 0);
	}
	
	@Test(groups="ut context")
	public void testArchiveMode(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchive("onSuccess");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getArchive().get(0), ArchiveMode.ON_SUCCESS);
	}
	/**
	 * Test compatibility
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context")
	public void testArchiveModeTrue(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchive("true");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getArchive().get(0), ArchiveMode.ALWAYS);
	}
	/**
	 * Test compatibility
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context")
	public void testArchiveModeFalse(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchive("false");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getArchive().get(0), ArchiveMode.NEVER);
	}
	@Test(groups="ut context")
	public void testMultipleArchiveMode(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchive("onSuccess,onSkip");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getArchive(), Arrays.asList(ArchiveMode.ON_SUCCESS, ArchiveMode.ON_SKIP));
	}
	@Test(groups="ut context")
	public void testArchiveModeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchive(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getArchive().get(0), ArchiveMode.NEVER);
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testArchiveModeKo(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchive("unknown");
	}
	
	@Test(groups="ut context")
	public void testVideoCaptureModeOnSuccess(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("onSuccess");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getVideoCapture(),VideoCaptureMode.ON_SUCCESS);
	}
	@Test(groups="ut context")
	public void testVideoCaptureModeOnError(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("onError");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getVideoCapture(),VideoCaptureMode.ON_ERROR);
	}
	@Test(groups="ut context")
	public void testVideoCaptureModeNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getVideoCapture(), VideoCaptureMode.ON_ERROR);
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testVideoCaptureModeKo(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("unknown");
	}
	
	@Test(groups="ut context")
	public void testAdvancedElementSearchFalse(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("false");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch(), ElementInfo.Mode.FALSE);
	}
	@Test(groups="ut context")
	public void testAdvancedElementSearchDom(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("dom");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch(), ElementInfo.Mode.DOM);
	}
	@Test(groups="ut context")
	public void testAdvancedElementSearchFull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("full");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch(), ElementInfo.Mode.FULL);
	}
	@Test(groups="ut context")
	public void testAdvancedElementSearchNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAdvancedElementSearch(), ElementInfo.Mode.FALSE);
	}

	@Test(groups="ut context")
	public void testMaskPassword(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(false);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().getMaskedPassword());
	}
	// by default, devMode is true if tests are launched from IDE
	@Test(groups="ut context")
	public void testMaskPasswordNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(null);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getMaskedPassword());
	}
	
	@Test(groups="ut context")
	public void testRandmoInAttachment(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRandomInAttachmentNames(false);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().getRandomInAttachments());
	}
	// by default, devMode is true if tests are launched from IDE
	@Test(groups="ut context")
	public void testRandmoInAttachmentNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRandomInAttachmentNames(null);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getRandomInAttachments());
	}
	
	@Test(groups="ut context")
	public void testDebugCore(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setDebug("core");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDebug(), Arrays.asList(DebugMode.CORE));
		} finally {
			SeleniumTestsContextManager.getThreadContext().setDebug(null);
		}
	}
	
	@Test(groups="ut context")
	public void testDebugCoreAndDriver(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setDebug("core,driver");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDebug(), Arrays.asList(DebugMode.CORE, DebugMode.DRIVER));
		} finally {
			SeleniumTestsContextManager.getThreadContext().setDebug(null);
		}
	}
	@Test(groups="ut context")
	public void testDebugDriver(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setDebug("driver");
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDebug(), Arrays.asList(DebugMode.DRIVER));
		} finally {
			SeleniumTestsContextManager.getThreadContext().setDebug(null);
		}
	}
	// by default, devMode is true if tests are launched from IDE
	@Test(groups="ut context")
	public void testDebugNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setDebug(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDebug(), Arrays.asList(DebugMode.NONE));
	}
	
	@Test(groups="ut context")
	public void testBrowserIE(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("iexplore");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBrowser(), BrowserType.INTERNET_EXPLORER);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getEdgeIeMode());
	}
	@Test(groups="ut context")
	public void testBrowserIEMode(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("iexploreEdge");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBrowser(), BrowserType.INTERNET_EXPLORER);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getEdgeIeMode());
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
	public void testChromeProfilePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setChromeUserProfilePath("default");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getChromeUserProfilePath(), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	@Test(groups="ut context")
	public void testChromeProfilePathForGrid(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setChromeUserProfilePath("/home/user/chrome");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getChromeUserProfilePath(), "/home/user/chrome");
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class)
	public void testWrongChromeProfilePathForLocal(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setChromeUserProfilePath("/home/user/chrome");
	}
	@Test(groups="ut context")
	public void testChromeProfilePathForLocal(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setChromeUserProfilePath(SeleniumTestsContextManager.getApplicationDataPath());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getChromeUserProfilePath(), SeleniumTestsContextManager.getApplicationDataPath());
	}
	@Test(groups="ut context")
	public void testChromeProfilePathWithOtherBrowser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setChromeUserProfilePath("default");
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getChromeUserProfilePath());
	}
	
	@Test(groups="ut context")
	public void testEdgeProfilePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("edge");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("default");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getEdgeUserProfilePath(), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	@Test(groups="ut context")
	public void testEdgeProfilePathForGrid(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		SeleniumTestsContextManager.getThreadContext().setBrowser("edge");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("/home/user/edge");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getEdgeUserProfilePath(), "/home/user/edge");
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class)
	public void testWrongEdgeProfilePathForLocal(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setBrowser("edge");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("/home/user/edge");
	}
	@Test(groups="ut context")
	public void testEdgeProfilePathForLocal(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setBrowser("edge");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath(SeleniumTestsContextManager.getApplicationDataPath());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getEdgeUserProfilePath(), SeleniumTestsContextManager.getApplicationDataPath());
	}
	@Test(groups="ut context")
	public void testEdgeProfilePathWithOtherBrowser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("default");
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getEdgeUserProfilePath());
	}
	
	@Test(groups="ut context")
	public void testFirefoxProfilePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setFirefoxUserProfilePath("default");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getFirefoxUserProfilePath(), BrowserInfo.DEFAULT_BROWSER_PRODFILE);
	}
	@Test(groups="ut context")
	public void testFirefoxProfilePathForGrid(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setFirefoxUserProfilePath("/home/user/chrome");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getFirefoxUserProfilePath(), "/home/user/chrome");
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class)
	public void testWrongFirefoxProfilePathForLocal(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setFirefoxUserProfilePath("/home/user/chrome");
	}
	@Test(groups="ut context")
	public void testFirefoxProfilePathForLocal(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setRunMode("local");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setFirefoxUserProfilePath(SeleniumTestsContextManager.getApplicationDataPath());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getFirefoxUserProfilePath(), SeleniumTestsContextManager.getApplicationDataPath());
	}
	@Test(groups="ut context")
	public void testFirefoxProfilePathWithOtherBrowser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setFirefoxUserProfilePath("default");
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getFirefoxUserProfilePath());
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAssumeUntrustedCertificateIssuer(), (Boolean)SeleniumTestsContext.DEFAULT_SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER);
	}
	
	@Test(groups="ut context")
	public void testBetaBrowser(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBetaBrowser(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBetaBrowser(), (Boolean)true);
	}
	@Test(groups="ut context")
	public void testBetBrowserNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBetaBrowser(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getBetaBrowser(), (Boolean)SeleniumTestsContext.DEFAULT_BETA_BROWSER);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAcceptUntrustedCertificates(), (Boolean)SeleniumTestsContext.DEFAULT_SET_ACCEPT_UNTRUSTED_CERTIFICATES);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot(), SeleniumTestsContext.DEFAULT_CAPTURE_SNAPSHOT);
	}
	
	@Test(groups="ut context")
	public void testNetworkSnapshot(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCaptureNetwork(), true);
	}
	@Test(groups="ut context")
	public void testNetworkSnapshotNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCaptureNetwork(), SeleniumTestsContext.DEFAULT_CAPTURE_NETWORK);
	}
	
	@Test(groups="ut context")
	public void testSnapshotScrollDelay(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotScrollDelay(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotScrollDelay(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSnapshotScrollDelayNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotScrollDelay(null);
		Assert.assertEquals((int)SeleniumTestsContextManager.getThreadContext().getSnapshotScrollDelay(), SeleniumTestsContext.DEFAULT_SNAPSHOT_SCROLL_DELAY);
	}
	
	@Test(groups="ut context")
	public void testSnapshotTopCropping(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSnapshotTopCroppingNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotTopCropping(null);
		Assert.assertEquals((Integer)SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping(), SeleniumTestsContext.DEFAULT_SNAPSHOT_TOP_CROPPING);
	}
	
	@Test(groups="ut context")
	public void testSnapshotBottomCropping(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(5);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping(), (Integer)5);
	}
	@Test(groups="ut context")
	public void testSnapshotBottomCroppingNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSnapshotBottomCropping(null);
		Assert.assertEquals((Integer)SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping(), SeleniumTestsContext.DEFAULT_SNAPSHOT_BOTTOM_CROPPING);
	}

	@Test(groups="ut context")
	public void testSoftAssertEnabled(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled(), false);
	}
	@Test(groups="ut context")
	public void testSoftAssertEnabledNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(null);
			Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().isSoftAssertEnabled(), SeleniumTestsContext.DEFAULT_SOFT_ASSERT_ENABLED);
		} finally {
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	@Test(groups="ut context")
	public void testOptimizeReports(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOptimizeReports(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOptimizeReports());
	}
	@Test(groups="ut context")
	public void testOptimizeReportsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOptimizeReports(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOptimizeReports(), SeleniumTestsContext.DEFAULT_OPTIMIZE_REPORTS);
	}
	
	@Test(groups="ut context")
	public void testKeepAllResults(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setKeepAllResults(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getKeepAllResults());
	}
	@Test(groups="ut context")
	public void testKeepAllResultsNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setKeepAllResults(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getKeepAllResults(), SeleniumTestsContext.DEFAULT_KEEP_ALL_RESULTS);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getApp(), SeleniumTestsContext.DEFAULT_APP);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCucumberTags(), SeleniumTestsContext.DEFAULT_CUCUMBER_TAGS);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestEnv(), SeleniumTestsContext.DEFAULT_TEST_ENV);
	}
	
	@Test(groups="ut context")
	public void testAutomationName(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAutomationName("UIAutomator2");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAutomationName(), "UIAutomator2");
	}
	@Test(groups="ut context")
	public void testAutomationNameNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAutomationName(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAutomationName(), null);
	}
	
	@Test(groups="ut context")
	public void testAppium1ServerUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://appium:4123/wd/hub/");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAppiumServerUrl(), "http://appium:4123/wd/hub/");
	}
	@Test(groups="ut context")
	public void testAppium2ServerUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://appium:4123/");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAppiumServerUrl(), "http://appium:4123/");
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class)
	public void testWrongAppiumServerUrl(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://appium:4123");
	}
	@Test(groups="ut context")
	public void testAppiumServerUrlNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAppiumServerUrl(), null);
	}
	
	@Test(groups="ut context")
	public void testAppiumCapabilities(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumCapabilities("cap1=val1;appium:cap2=val2");
		Capabilities caps = SeleniumTestsContextManager.getThreadContext().getAppiumCapabilities();
		Assert.assertEquals(caps.asMap().size(), 2);
		Assert.assertEquals(caps.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + "cap1"), "val1");
		Assert.assertEquals(caps.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + "cap2"), "val2");
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Format for appium capabilities must be 'key1=value1;key2=value2'")
	public void testAppiumCapabilitiesWrongFormat(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumCapabilities("foobar");
		SeleniumTestsContextManager.getThreadContext().getAppiumCapabilities();
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Appium capabilities keys must be unique")
	public void testAppiumCapabilitiesDuplicateKey(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumCapabilities("cap1=val1;cap1=val2");
		SeleniumTestsContextManager.getThreadContext().getAppiumCapabilities();
	}
	@Test(groups="ut context")
	public void testAppiumCapabilitiesNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppiumCapabilities(null);
		Capabilities caps = SeleniumTestsContextManager.getThreadContext().getAppiumCapabilities();
		Assert.assertEquals(caps.asMap().size(), 0);
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
	public void testNewCommandTimeout(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNewCommandTimeout(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNewCommandTimeout(), 15);
	}
	@Test(groups="ut context")
	public void testNewCommandTimeoutNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setNewCommandTimeout(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getNewCommandTimeout(), SeleniumTestsContext.DEFAULT_NEW_COMMAND_TIMEOUT);
	}
	
	@Test(groups="ut context")
	public void testActionDelay(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setActionDelay(15);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getActionDelay(), 15);
	}
	@Test(groups="ut context")
	public void testActionDelayNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setActionDelay(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getActionDelay(), SeleniumTestsContext.DEFAULT_ACTION_DELAY);
	}
	
	@Test(groups="ut context")
	public void testOutputDirectoryAbsolutePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory("/home/user/test-output", testNGCtx, false);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().endsWith("/home/user/test-output"));
	}
	@Test(groups="ut context")
	public void testOutputDirectoryRelativePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory("test-output/someSubdir", testNGCtx, false);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().endsWith("core/test-output/someSubdir"));
	}
	@Test(groups="ut context")
	public void testOutputDirectoryFromSystem(final ITestContext testNGCtx, final XmlTest xmlTest) {
		try {
			System.setProperty(SeleniumTestsContext.OUTPUT_DIRECTORY, "/home/user/test-output");
			initThreadContext(testNGCtx);
			Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getOutputDirectory().endsWith("/home/user/test-output/myTest")); // test name is myTest due to TestResult generation in GenericTest class
		} finally {
			System.clearProperty(SeleniumTestsContext.OUTPUT_DIRECTORY);
		}
	}
	@Test(groups="ut context")
	public void testOutputDirectoryNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		((TestRunner)testNGCtx).setOutputDirectory("/home/other/test-output/testsuite");
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory(null, testNGCtx, false);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "/home/other/test-output".replace("/", File.separator));
	}
	
	@Test(groups="ut context")
	public void testDefaultOutputDirectory(final ITestContext testNGCtx, final XmlTest xmlTest) {
		String out = SeleniumTestsContextManager.getRootPath();
		((TestRunner)testNGCtx).setOutputDirectory(out);
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setOutputDirectory(out, testNGCtx, true);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getDefaultOutputDirectory().replace("\\", "/") + "/", out);
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
	public void testCapabilities(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCapabilities("app=foo.apk");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCapabilities().asMap().size(), 1);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCapabilities().getCapability("app"), "foo.apk");
	}
	@Test(groups="ut context")
	public void testMultipleCapabilities(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCapabilities("app=foo.apk,foo=bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCapabilities().asMap().size(), 2);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCapabilities().getCapability("app"), "foo.apk");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getCapabilities().getCapability("foo"), "bar");
	}
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class)
	public void testWrongCapabilitiesFormat(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCapabilities("app=foo.apk,foobar");
	}
	@Test(groups="ut context")
	public void testCapabilitiesNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCapabilities(null);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getCapabilities().asMap().isEmpty());
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
	public void testFindErrorCause(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setFindErrorCause(true);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().isFindErrorCause());
	}
	@Test(groups="ut context")
	public void testFindErrorCauseNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setFindErrorCause(null);
		Assert.assertFalse(SeleniumTestsContextManager.getThreadContext().isFindErrorCause());
	}
	
	@Test(groups="ut context")
	public void testCustomTestsReports(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomTestReports("PERF::xml::reporter/templates/report.test.vm,SUPERVISION::xml::reporter/templates/report.perf.vm");
		List<ReportInfo> reportInfos = SeleniumTestsContextManager.getThreadContext().getCustomTestReports();
		Assert.assertEquals(reportInfos.size(), 2);
		Assert.assertEquals(reportInfos.get(0).getExtension(), ".xml");
		Assert.assertEquals(reportInfos.get(0).getTemplatePath(), "reporter/templates/report.test.vm");
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
	public void testWebDriverListener(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverListener("com.seleniumtests.ut.driver.WebDriverListener1,com.seleniumtests.ut.driver.WebDriverListener2");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebDriverListener().size(), 2); 
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebDriverListener().get(1), WebDriverListener2.class.getName());
	}
	
	/**
	 * We cannot mix the 2 listeners
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context", expectedExceptions = ConfigurationException.class)
	public void testMixWebDriverListener(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverListener("com.seleniumtests.ut.driver.WebDriverEventListener1,com.seleniumtests.ut.driver.WebDriverListener2");
		
	}
	
	/**
	 * We provide a class which is not a WebDriverEventListener
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testBadWebDriverListenerClass(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverListener("com.seleniumtests.core.Filter");
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testUnknownWebDriverListenerClass(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setWebDriverListener("com.seleniumtests.core.Foo");
	}
	@Test(groups="ut context")
	public void testNullWebDriverListener(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReporterPluginClasses(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebDriverListener().size(), 0); 
	}
	
	@Test(groups="ut context")
	public void testPluginReporterClass(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReporterPluginClasses("com.seleniumtests.reporter.reporters.JUnitReporter,com.seleniumtests.reporter.reporters.TestManagerReporter");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReporterPluginClasses().size(), 7); // 7 classes, the last 5 are internal seleniumRobot reporters
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReporterPluginClasses().get(5), JUnitReporter.class);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReporterPluginClasses().get(6), TestManagerReporter.class);
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testBadPluginReporterClass(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReporterPluginClasses("com.seleniumtests.core.Filter");
	}
	@Test(groups="ut context")
	public void testNullPluginReporterClass(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReporterPluginClasses(null);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getReporterPluginClasses().size(), 5); // the 5 classes are internal seleniumRobot reporters
	}
	
	@Test(groups="ut context")
	public void testCustomSummaryReports(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setCustomSummaryReports("PERF::xml::reporter/templates/report.test.vm,SUPERVISION::xml::reporter/templates/report.perf.vm");
		List<ReportInfo> reportInfos = SeleniumTestsContextManager.getThreadContext().getCustomSummaryReports();
		Assert.assertEquals(reportInfos.size(), 2);
		Assert.assertEquals(reportInfos.get(0).getExtension(), ".xml");
		Assert.assertEquals(reportInfos.get(0).getTemplatePath(), "reporter/templates/report.test.vm");
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

	@Test(groups="ut context")
	public void testArchiveToFile(final ITestContext testNGCtx, final XmlTest xmlTest) throws IOException {
		initThreadContext(testNGCtx);
		File archive = File.createTempFile("archive", ".zip");
		archive.deleteOnExit();
		SeleniumTestsContextManager.getThreadContext().setArchiveToFile(archive.getAbsolutePath());
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getArchiveToFile().equals(archive.getAbsolutePath()));
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testArchiveToTarFile(final ITestContext testNGCtx, final XmlTest xmlTest) throws IOException {
		initThreadContext(testNGCtx);
		File archive = File.createTempFile("archive", ".tar");
		archive.deleteOnExit();
		SeleniumTestsContextManager.getThreadContext().setArchiveToFile(archive.getAbsolutePath());
	}
	@Test(groups="ut context")
	public void testArchiveToFileNull(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setArchiveToFile(null);
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getArchiveToFile());
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
		seleniumContext.setCaptureNetwork(false);
		seleniumContext.setConfiguration(new HashMap<>());
		seleniumContext.setTestConfiguration();
		seleniumContext.updateProxyConfig();
		Assert.assertEquals(seleniumContext.getWebProxyType(), ProxyType.DIRECT);
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testProxyPresetExcludeCaptureNetworkWithAutoDetect(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
		SeleniumTestsContext seleniumContext = new SeleniumTestsContext();
		seleniumContext.setCaptureNetwork(true);
		seleniumContext.setWebProxyType("AUTODETECT");
		seleniumContext.setConfiguration(new HashMap<>());
		seleniumContext.setTestConfiguration();
		seleniumContext.updateProxyConfig();
	}
	@Test(groups="ut context", expectedExceptions=ConfigurationException.class)
	public void testProxyPresetExcludeCaptureNetworkWithPac(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
		SeleniumTestsContext seleniumContext = new SeleniumTestsContext();
		seleniumContext.setCaptureNetwork(true);
		seleniumContext.setWebProxyType("PAC");
		seleniumContext.setConfiguration(new HashMap<>());
		seleniumContext.setTestConfiguration();
		seleniumContext.updateProxyConfig();
	}
	@Test(groups="ut context")
	public void testProxyPresetCaptureNetworkWithManual(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
		SeleniumTestsContext seleniumContext = new SeleniumTestsContext();
		seleniumContext.setCaptureNetwork(true);
		seleniumContext.setWebProxyType("MANUAL");
		seleniumContext.setConfiguration(new HashMap<>());
		seleniumContext.setTestConfiguration();
		seleniumContext.updateProxyConfig();
		Assert.assertEquals(seleniumContext.getWebProxyType(), ProxyType.MANUAL);
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
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getWebProxyType(), ProxyType.SYSTEM);
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
	

	@Test(groups="ut context")
	public void testGetConfigPath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getConfigPath().replace(File.separator, "/").endsWith("data/core/config"));
	}
	@Test(groups="ut context")
	public void testGetDataPath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getDataPath().replace(File.separator, "/").endsWith("data/"));
	}
	@Test(groups="ut context")
	public void testGetApplicationDataPath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getApplicationDataPath().replace(File.separator, "/").endsWith("data/core"));
	}
	@Test(groups="ut context")
	public void testGetFeaturePath(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		Assert.assertTrue(SeleniumTestsContextManager.getThreadContext().getFeaturePath().replace(File.separator, "/").endsWith("data/core/features"));
	}
	
	/**
	 * Check that with a test name, we create an output folder for this test whose name is the name of the test
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@Test(groups="ut")
	public void testNewOutputFolder(final ITestContext testNGCtx) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		initThreadContext(testNGCtx);
		
		ITestResult testResult = generateResult(testNGCtx, getClass());
		testResult.setParameters(new String[] {"foo", "bar"});
		
		int outputNamesInitialSize = SeleniumTestsContext.getOutputFolderNames().size();
		
		SeleniumTestsContextManager.updateThreadContext(testResult);
		Assert.assertEquals(SeleniumTestsContext.getOutputFolderNames().size(), outputNamesInitialSize + 1);
		
		String key = testNGCtx.getSuite().getName()
				+ "-" + testNGCtx.getName()
				+ "-" + "com.seleniumtests.ut.core.TestSeleniumTestContext"
				+ "-" + "myTest"
				+ "-" + "3247054"
				+ "-" + "org.testng.internal.TestNGMethod";

		List<String> keys = SeleniumTestsContext.getOutputFolderNames().keySet().stream().map(o -> o.split("@")[0]).collect(Collectors.toList());
		Assert.assertTrue(keys.contains(key));
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir(), "myTest");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), 
							Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "myTest").toString().replace(File.separator, "/"));
		
	}
	
	/**
	 * Check that if a context is reinitialized, (test is retried), the same folder is used
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@Test(groups="ut")
	public void testOutputFolderWhenRetryingTest(final ITestContext testNGCtx) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		initThreadContext(testNGCtx);
		
		ITestResult testResult = generateResult(testNGCtx, getClass());
		testResult.setParameters(new String[] {"foo", "bar"});
		
		int outputNamesInitialSize = SeleniumTestsContext.getOutputFolderNames().size();
		
		SeleniumTestsContextManager.updateThreadContext(testResult);
		
		// reinitialize
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.updateThreadContext(testResult);
		
		// check that only one folder is created
		Assert.assertEquals(SeleniumTestsContext.getOutputFolderNames().size(), outputNamesInitialSize + 1);
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir(), "myTest");
		
	}
	
	/**
	 * Check that if test name already exists for an other test (the case with DataProvider where only parameters change), create an other directory
	 * suffixed with "-1" as it's the same test method but not the same test execution
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws NoSuchMethodException 
	 */
	@Test(groups="ut")
	public void testExistingOutputFolder(final ITestContext testNGCtx) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		initThreadContext(testNGCtx);
		ITestResult testResult = generateResult(testNGCtx, getClass());
		testResult.setParameters(new String[] {"foo", "bar"});
		
		SeleniumTestsContextManager.updateThreadContext(testResult);
		
		ITestResult testResult2 = generateResult(testNGCtx, getClass());
		testResult2.setParameters(new String[] {"foo", "bar2"});
		SeleniumTestsContextManager.updateThreadContext(testResult2);
		
		
		String key = testNGCtx.getSuite().getName()
				+ "-" + testNGCtx.getName()
				+ "-" + "com.seleniumtests.ut.core.TestSeleniumTestContext"
				+ "-" + "myTest"
				+ "-" + "3247054"
				+ "-" + "org.testng.internal.TestNGMethod";
		String key2 = testNGCtx.getSuite().getName()
				+ "-" + testNGCtx.getName()
				+ "-" + "com.seleniumtests.ut.core.TestSeleniumTestContext"
				+ "-" + "myTest"
				+ "-" + "6166074"
				+ "-" + "org.testng.internal.TestNGMethod";

		List<String> keys = SeleniumTestsContext.getOutputFolderNames().keySet().stream().map(o -> o.split("@")[0]).collect(Collectors.toList());
		Assert.assertTrue(keys.contains(key));
		Assert.assertTrue(keys.contains(key2));
		
		// check second created context
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir(), "myTest-1");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), 
				Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "myTest-1").toString().replace(File.separator, "/"));
		
	}
	
	@Test(groups="ut context")
	public void testTestTypeDesktopWeb(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp(null); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setPlatform("Windows 10");
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.WEB);
	}

	@Test(groups="ut context")
	public void testTestTypeDesktopApp(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp("nodepad"); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setPlatform("Windows 10");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("Windows 10");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_APP_WINDOWS);
	}

	@Test(groups="ut context")
	public void testTestTypeDesktopAppAlreadyLaunched(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAppActivity(".*nodepad.*"); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setPlatform("Windows 10");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile("Windows 10");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_APP_WINDOWS);
	}
	
	@Test(groups="ut context")
	public void testTestTypeAndroidWebFirefox(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp(null); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setAppActivity(null); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setPlatform("Android 10.0");
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_WEB_ANDROID);
	}
	
	/**
	 * APK path is given so that it can be installed
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context")
	public void testTestTypeAndroidAppFromApk(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPlatform("Android 10.0");
		SeleniumTestsContextManager.getThreadContext().setApp("myapp.apk");
		SeleniumTestsContextManager.getThreadContext().setAppPackage("com.foo.bar.app");
		SeleniumTestsContextManager.getThreadContext().setAppActivity("activity");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile(SeleniumTestsContextManager.getThreadContext().getPlatform());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_APP_ANDROID);
	}
	
	/**
	 * issue #326: APK path is not given, we want to use an already installed application. Only package is defined
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context")
	public void testTestTypeAndroidInstalledApp(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp(null); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setPlatform("Android 10.0");
		SeleniumTestsContextManager.getThreadContext().setAppPackage("com.foo.bar.app");
		SeleniumTestsContextManager.getThreadContext().setAppActivity("activity");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile(SeleniumTestsContextManager.getThreadContext().getPlatform());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_APP_ANDROID);
	}

	@Test(groups="ut context")
	public void testTestTypeIOSWebSafari(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setApp(null); // to override parameter from exampleConfigGenericParams
		SeleniumTestsContextManager.getThreadContext().setPlatform("iOS 12.0");
		SeleniumTestsContextManager.getThreadContext().setBrowser("safari");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_WEB_IOS);
	}
	
	/**
	 * Application path is given so that it can be installed
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut context")
	public void testTestTypeIOSAppFromIpa(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPlatform("iOS 12.0");
		SeleniumTestsContextManager.getThreadContext().setApp("myapp.ipa");
		SeleniumTestsContextManager.getThreadContext().updateTestAndMobile(SeleniumTestsContextManager.getThreadContext().getPlatform());
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.APPIUM_APP_IOS);
	}
	
	@Test(groups="ut context")
	public void testTestTypeDesktopNonGui(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setPlatform("Linux");
		SeleniumTestsContextManager.getThreadContext().setBrowser("none");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getTestType(), TestType.NON_GUI);
	}
	
	public void myTest() {}
	
	@Test(groups="ut context")
	public void testAttribute(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAttribute("foo", "bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("foo"), "bar");
	}
	@Test(groups="ut context")
	public void testAttributeNotFound(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAttribute("foo", "bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("foo2"), null);
	}
	@Test(groups="ut context")
	public void testAttributeFoundInVariableButNotRequested(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foo2", new TestVariable("foo2", "bar2"));
		SeleniumTestsContextManager.getThreadContext().setAttribute("foo", "bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("foo2"), null);
	}
	@Test(groups="ut context")
	public void testAttributeFoundInVariablePriority(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foo", new TestVariable("foo", "bar2"));
		SeleniumTestsContextManager.getThreadContext().setAttribute("foo", "bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("foo", true), "bar");
	}
	@Test(groups="ut context")
	public void testAttributeFoundInVariableAndRequested(final ITestContext testNGCtx, final XmlTest xmlTest) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("foo2", new TestVariable("foo2", "bar2"));
		SeleniumTestsContextManager.getThreadContext().setAttribute("foo", "bar");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getAttribute("foo2", true), "bar2");
	}
}
