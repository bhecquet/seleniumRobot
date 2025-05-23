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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.ImageElement;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.video.VideoCaptureMode;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevToolsException;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.it.driver.support.pages.DriverTestPageShadowDom;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;

/**
 * Other tests can be found in "TestBrowserSnapshot"
 * @author S047432
 *
 */
public class TestScreenshotUtil extends ReporterTest {


	/**
	 * Check the aspect ratio is taken into account when capturing element
	 * Better check with OS zoom setting set to a value > 100%
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testElementScreenshotUsingAspectRatio() throws Exception {

		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		WebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			HtmlElement shadowRoot = new HtmlElement("", ByC.shadow(By.id("shadow3")));
			SnapshotTarget snapshotTarget = new SnapshotTarget(new ImageElement("", By.id("fail2"), shadowRoot));

			File image2 = new ScreenshotUtil(driver).capture(snapshotTarget, File.class);
			BufferedImage image = new ScreenshotUtil(driver).capture(snapshotTarget, BufferedImage.class);
			double aspectRatio = ((CustomEventFiringWebDriver)driver).getDeviceAspectRatio();
			Assert.assertTrue(Math.abs(image.getHeight() - 150 * aspectRatio) < 1);
			Assert.assertTrue(Math.abs(image.getWidth() - 200 * aspectRatio) < 1);
		} finally {
			driver.close();
		}
	}

	@Test(groups={"it"})
	public void testChromeScreenshotUsingCDP() throws Exception {

		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		WebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPageUsingCDP(driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}
	
	
	@Test(groups={"it"})
	public void testEdgeScreenshotUsingCDP() throws Exception {
		
		setBrowser("edge");
		new DriverTestPageShadowDom(true);
		WebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPageUsingCDP(driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}
	@Test(groups={"it"}, expectedExceptions = DevToolsException.class)
	public void testFirefoxScreenshotUsingCDP() throws Exception {
		
		setBrowser("firefox");
		new DriverTestPageShadowDom(true);
		WebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPageUsingCDP(driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}
	

	public void setBrowser(String browserName) {
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserName);
	}
	
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
		String mainWindowCode = FileUtils.readFileToString(htmlFiles[0], StandardCharsets.UTF_8);
		Assert.assertTrue(mainWindowCode.contains("<h3>Test clicking an element</h3>"));
		Assert.assertFalse(mainWindowCode.contains("<a href=\"http://www.google.fr\" id=\"linkIFrame\" target=\"_blank\">My link in IFrame</a>"));
		
		String secondWindowCode = FileUtils.readFileToString(htmlFiles[1], StandardCharsets.UTF_8);
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
		Assert.assertTrue(screenshot.getImage().getFile().exists());
		
	}
	
	/**
	 * Check that step is hidden while we capture desktop
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"}, expectedExceptions = ImageSearchException.class)
	public void testDesktopScreenshotsWithVideoRecorder(ITestContext testContext) throws Exception {
		
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.TRUE.toString());
		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		try {
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			ScreenShot screenshot = new ScreenshotUtil(null).capture(SnapshotTarget.SCREEN, ScreenShot.class);
			Assert.assertTrue(screenshot.getImage().getFile().exists());
			
			ImageDetector detector = new ImageDetector(screenshot.getImage().getFile(), createImageFromResource("tu/images/step.png"), 0.20);
			
			detector.detectExactZoneWithScale();
			
		} finally {
			driver.close();
		}
		
	}
	
	/**
	 * issue #422: check we return null instead of an empty ScreenShot / File when captureSnapshot is set to false
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testScreenshotIsNull(ITestContext testContext) throws Exception {

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		Assert.assertNull(new ScreenshotUtil(null).capture(SnapshotTarget.PAGE, ScreenShot.class));

	}
	@Test(groups={"it"})
	public void testScreenshotIsNotNullWhenForced(ITestContext testContext) throws Exception {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		
		WebDriver localDriver = null;
		try {
			localDriver = WebUIDriver.getWebDriver(true);
			ScreenShot screenshot = new ScreenshotUtil(localDriver).capture(SnapshotTarget.PAGE, ScreenShot.class, true);
			Assert.assertNotNull(screenshot);
			Assert.assertTrue(screenshot.getImage().getFile().exists());
		} finally {
			if (localDriver != null) {
				localDriver.close();
				WebUIDriver.cleanUp();
			}
		}
	}
	
	/**
	 * issue #435: Test that when a modal is present on a relatively long web page (twice the height of the modal), screenshot is done
	 */
	@Test(groups={"it"})
	public void testCaptureWhenModalPresent() throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverModalSnapshot"});
		
		
		for (ISuiteResult suiteResult: SeleniumRobotTestListener.getSuiteList().get(0).getResults().values()) {
			for (ITestResult testResult: suiteResult.getTestContext().getPassedTests().getAllResults()) {
				List<TestStep> steps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
				for (TestStep step: steps) {
					if ("_captureSnapshot with args: (my snapshot, )".equals(step.getName())) {
						Assert.assertEquals(step.getSnapshots().size(), 2); // 1 reference image + image captured during step
						Assert.assertNotNull(step.getSnapshots().get(0).getScreenshot().getImage());
						
						BufferedImage image = ImageIO.read(step.getSnapshots().get(0).getScreenshot().getImage().getFile());
						Assert.assertTrue(image.getHeight() > 2000); // check we have a full picture of the page. As Chrome uses CDP for capturing, the whole page is taken
						return;
					}	
				}
			}
		}
		Assert.fail("step has not been found");

	}
	/**
	 * Test scrollDelay=0
	 * Check capture delay is not too high
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNoScrollDelay() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SNAPSHOT_SCROLL_DELAY, "0");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
	
			for (ISuiteResult suiteResult: SeleniumRobotTestListener.getSuiteList().get(0).getResults().values()) {
				for (ITestResult testResult: suiteResult.getTestContext().getPassedTests().getAllResults()) {
					List<TestStep> steps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
					for (TestStep step: steps) {
						if ("_captureSnapshot with args: (my snapshot, )".equals(step.getName())) {
							Assert.assertTrue(step.getSnapshots().get(0).getDurationToExclude() < 3000);
							return;
						}	
					}
				}
			}
			
			Assert.fail("step has not been found");

		} finally {
			System.clearProperty(SeleniumTestsContext.SNAPSHOT_SCROLL_DELAY);
		}
		
	}
	/**
	 * Test scrollDelay=1000 so that it can be distinguished
	 * Check that capture for report (when page opens or for 'Test end') is not affected by the scrollDelay setting
	 * Check that for image comparison, setting is used
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testWithScrollDelay() throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.SNAPSHOT_SCROLL_DELAY, "1000");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			
			int checkNumbers = 0;
			for (ISuiteResult suiteResult: SeleniumRobotTestListener.getSuiteList().get(0).getResults().values()) {
				for (ITestResult testResult: suiteResult.getTestContext().getPassedTests().getAllResults()) {
					List<TestStep> steps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
					for (TestStep step: steps) {

						// check that screenshots used for image comparison are affected by scrollDelay setting
						if ("_captureSnapshot with args: (my snapshot, )".equals(step.getName())) {
							Assert.assertTrue(step.getSnapshots().get(0).getDurationToExclude() > 4000);
							Assert.assertEquals(checkNumbers, 1);
							return;
						}	
					}
				}
			}
			
			Assert.fail("step has not been found");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.SNAPSHOT_SCROLL_DELAY);
		}
		
	}
}
