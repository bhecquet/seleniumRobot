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
package com.seleniumtests.it.driver.screenshots;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;

public class TestScreenshotUtil extends ReporterTest {

	/**
	 * check that duration of screenshots is logged into TestStep
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testScreenshotDurationIsLogged(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
		for (ISuite suite: SeleniumRobotTestListener.getSuiteList()) {
			for (ISuiteResult suiteResult: suite.getResults().values()) {
				for (ITestResult testResult: suiteResult.getTestContext().getPassedTests().getAllResults()) {
					List<TestStep> steps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
					for (TestStep step: steps) {
						List<Snapshot> snapshots = step.getSnapshots();
						
						if (!snapshots.isEmpty()) {
							Assert.assertTrue(step.getDurationToExclude() > 0);
							Assert.assertEquals(snapshots.get(0).getScreenshot().getDuration(), step.getDurationToExclude());
						}
					}
				}
			}
		}

	}
	
	/**
	 * issue #300: check that with multiple windows, we have all screenshots and the corresponding HTML code
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMultipleScreenshots(ITestContext testContext) throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverMultipleSnapshot"});

		FilenameFilter fileNameFilter = new FilenameFilter() {
            public boolean accept( File dir, String name ) { 
                return name.matches( ".*Test_end.*" );
            }
		};
		
		Path resultDir = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverMultipleSnapshot");
		File[] htmlFiles = resultDir.resolve("htmls")
			.toFile()
			.listFiles(fileNameFilter);
		Assert.assertEquals(htmlFiles.length, 2);
		
		// check that html files reflect the real window code
		String mainWindowCode = FileUtils.readFileToString(htmlFiles[0]);
		Assert.assertTrue(mainWindowCode.contains("<h3>Test clicking an element</h3>"));
		Assert.assertFalse(mainWindowCode.contains("<a href=\"http://www.google.fr\" id=\"linkIFrame\" target=\"_blank\">My link in IFrame</a>"));
		
		String secondWindowCode = FileUtils.readFileToString(htmlFiles[1]);
		Assert.assertFalse(secondWindowCode.contains("<h3>Test clicking an element</h3>"));
		Assert.assertTrue(secondWindowCode.contains("<a href=\"http://www.google.fr\" id=\"linkIFrame\" target=\"_blank\">My link in IFrame</a>"));
		
		File[] imgFiles = resultDir.resolve("screenshots")
				.toFile()
				.listFiles(fileNameFilter);
		Assert.assertEquals(imgFiles.length, 2);
	}
	
	/**
	 * issue #300: check that desktop capture is done
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testDesktopScreenshots(ITestContext testContext) throws Exception {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		ScreenShot screenshot = new ScreenshotUtil(null).capture(SnapshotTarget.SCREEN, ScreenShot.class);
		Assert.assertTrue(new File(screenshot.getFullImagePath()).exists());
		
	}
}
