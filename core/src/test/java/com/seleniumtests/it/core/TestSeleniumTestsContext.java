package com.seleniumtests.it.core;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.osutility.OSUtility;

public class TestSeleniumTestsContext extends GenericTest {


	/**
	 * isue #309: check we can start a test with chromeBinaryPath option
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testCustomChromeBrowserGeneration(ITestContext testContext) throws Exception {

		try {
			Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
			List<BrowserInfo> chromes = browsers.get(BrowserType.CHROME);
			if (chromes.size() == 0) {
				throw new SkipException("Chrome not installed");
			}
			
			
			System.setProperty(SeleniumTestsContext.CHROME_BINARY_PATH, chromes.get(0).getPath());
			
			ReporterTest.executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			String log = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertTrue(log.contains("Test is OK"));
		} finally {
			OSUtility.resetInstalledBrowsersWithVersion();
			System.clearProperty(SeleniumTestsContext.CHROME_BINARY_PATH);
		}
	}
}
