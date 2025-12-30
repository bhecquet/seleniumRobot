/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.ImageElement;
import com.seleniumtests.util.imaging.ImageDetector;
import com.seleniumtests.util.video.VideoCaptureMode;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevToolsException;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteResult;
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
	 */
	@Test(groups={"it"})
	public void testElementScreenshotUsingAspectRatio() {

		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			HtmlElement shadowRoot = new HtmlElement("", ByC.shadow(By.id("shadow3")));
			SnapshotTarget snapshotTarget = new SnapshotTarget(new ImageElement("", By.id("fail2"), shadowRoot));

			new ScreenshotUtil(driver).capture(snapshotTarget, File.class);
			BufferedImage image = new ScreenshotUtil(driver).capture(snapshotTarget, BufferedImage.class);
			double aspectRatio = driver.getDeviceAspectRatio();
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
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPageUsingCDP(driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}

	@Test(groups={"it"})
	public void testCaptureWebPageChrome() {

		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPage(0, driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}
	@Test(groups={"it"})
	public void testCaptureWebPageChrome2() {

		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPage(1, driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}
	@Test(groups={"it"})
	public void testCaptureWebPageFullPage() {

		setBrowser("firefox");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPage(1, driver.getWindowHandle());
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}

	/**
	 * Test specific firefox screenshot
	 */
	@Test(groups={"it"})
	public void testFirefoxScreenshot() throws Exception {

		setBrowser("firefox");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			BufferedImage image = new ScreenshotUtil(driver).captureWebPageFullPage();
			Assert.assertTrue(image.getHeight() > 2000);
			Assert.assertTrue(image.getWidth() > 1000);
		} finally {
			driver.close();
		}
	}

	@Test(groups={"it"}, expectedExceptions = CustomSeleniumTestsException.class)
	public void testFirefoxScreenshotWithChrome() throws Exception {

		setBrowser("chrome");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			new ScreenshotUtil(driver).captureWebPageFullPage();
		} finally {
			driver.close();
		}
	}

	
	
	@Test(groups={"it"})
	public void testEdgeScreenshotUsingCDP() throws Exception {
		
		setBrowser("edge");
		new DriverTestPageShadowDom(true);
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
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
		CustomEventFiringWebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			new ScreenshotUtil(driver).captureWebPageUsingCDP(driver.getWindowHandle());
		} finally {
			driver.close();
		}
	}
	

	public void setBrowser(String browserName) {
		SeleniumTestsContextManager.getThreadContext().setBrowser(browserName);
	}
	
	/**
	 * check that duration of screenshots is logged into TestStep
	 */
	@Test(groups={"it"})
	public void testScreenshotDurationIsLogged() {

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
	 */
	@Test(groups={"it"})
	public void testMultipleScreenshots() throws Exception {
		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverMultipleSnapshot"});

		FilenameFilter fileNameFilter = (dir, name) -> name.matches( ".*Test_end.*" );
		
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
	 */
	@Test(groups={"it"})
	public void testDesktopScreenshots() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		ScreenShot screenshot = new ScreenshotUtil(null).capture(SnapshotTarget.SCREEN, ScreenShot.class);
		Assert.assertTrue(screenshot.getImage().getFile().exists());
		
	}
	
	/**
	 * Check that step is hidden while we capture desktop
	 */
	@Test(groups={"it"}, expectedExceptions = ImageSearchException.class)
	public void testDesktopScreenshotsWithVideoRecorder() throws Exception {
		
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
	 */
	@Test(groups={"it"})
	public void testScreenshotIsNull() {

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		Assert.assertNull(new ScreenshotUtil(null).capture(SnapshotTarget.PAGE, ScreenShot.class));

	}
	@Test(groups={"it"})
	public void testScreenshotIsNotNullWhenForced() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");

		CustomEventFiringWebDriver localDriver = null;
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
	 */
	@Test(groups={"it"})
	public void testNoScrollDelay() {
		
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
	 */
	@Test(groups={"it"})
	public void testWithScrollDelay() {
		
		try {
			System.setProperty(SeleniumTestsContext.SNAPSHOT_SCROLL_DELAY, "1000");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverCustomSnapshot"});
			

			for (ISuiteResult suiteResult: SeleniumRobotTestListener.getSuiteList().get(0).getResults().values()) {
				for (ITestResult testResult: suiteResult.getTestContext().getPassedTests().getAllResults()) {
					List<TestStep> steps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
					for (TestStep step: steps) {

						// check that screenshots used for image comparison are affected by scrollDelay setting
						if ("_captureSnapshot with args: (my snapshot, )".equals(step.getName())) {
							Assert.assertTrue(step.getSnapshots().get(0).getDurationToExclude() > 4000);
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
	 * Test page source generation with CDP browser
	 */
	@Test(groups={"it"})
	public void testScreenshotWithMetadata() throws IOException {
		setBrowser("chrome");
		DriverTestPage page = null;
		try {
			page = new DriverTestPage(true);
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			ScreenShot screenshot = new ScreenshotUtil((CustomEventFiringWebDriver) page.getDriver()).capture(SnapshotTarget.PAGE, ScreenShot.class);
			Assert.assertTrue(screenshot.getLocation().contains("test.html"));
			Assert.assertEquals(screenshot.getTitle(), "Current Window: Test page");
			Assert.assertTrue(screenshot.getImage().getFile().exists());
			Assert.assertTrue(screenshot.getHtml().getFile().exists());
			String htmlContent = FileUtils.readFileToString(screenshot.getHtml().getFile(), StandardCharsets.UTF_8);
			Assert.assertTrue(htmlContent.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;")); // iframe content directly in html
			Assert.assertTrue(htmlContent.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;")); // iframe in iframe content directly in html

		} finally {
			if (page != null) {
				page.getDriver().close();
			}
		}
	}

	/**
	 * Test page source retrieving with non CDP browser
	 */
	@Test(groups={"it"})
	public void testScreenshotWithMetadataNonCdp() throws IOException {
		setBrowser("firefox");
		DriverTestPage page = null;
		try {
			page = new DriverTestPage(true);
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			ScreenShot screenshot = new ScreenshotUtil((CustomEventFiringWebDriver) page.getDriver()).capture(SnapshotTarget.PAGE, ScreenShot.class);
			Assert.assertTrue(screenshot.getLocation().contains("test.html"));
			Assert.assertEquals(screenshot.getTitle(), "Current Window: Test page");
			Assert.assertTrue(screenshot.getImage().getFile().exists());
			Assert.assertTrue(screenshot.getHtml().getFile().exists());
			String htmlContent = FileUtils.readFileToString(screenshot.getHtml().getFile(), StandardCharsets.UTF_8);
			Assert.assertTrue(htmlContent.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;")); // iframe content directly in html
			Assert.assertTrue(htmlContent.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;")); // iframe in iframe content directly in html

		} finally {
			if (page != null) {
				page.getDriver().close();
			}
		}
	}

	/**
	 * Check that if we are inside a frame before doing the screenshot, after, we are in the same frame
	 */
	@Test(groups={"it"})
	public void testScreenshotWithMetadataRestoreFrameContext() throws IOException {
		setBrowser("firefox");
		DriverTestPage page = null;
		try {
			page = new DriverTestPage(true);
			page.getDriver().switchTo().frame(0).switchTo().frame(0);

			// to be sure we are on the right frame
			page.getDriver().findElement(By.id("textInIFrameWithValue3")).getText();

			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			ScreenShot screenshot = new ScreenshotUtil((CustomEventFiringWebDriver) page.getDriver()).capture(SnapshotTarget.PAGE, ScreenShot.class);
			Assert.assertTrue(screenshot.getLocation().contains("test.html"));
			Assert.assertEquals(screenshot.getTitle(), "Current Window: Test page");
			Assert.assertTrue(screenshot.getImage().getFile().exists());
			Assert.assertTrue(screenshot.getHtml().getFile().exists());
			String htmlContent = FileUtils.readFileToString(screenshot.getHtml().getFile(), StandardCharsets.UTF_8);
			Assert.assertTrue(htmlContent.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;")); // iframe content directly in html
			Assert.assertTrue(htmlContent.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;")); // iframe in iframe content directly in html

			// check we are in the same context, this should work
			page.getDriver().findElement(By.id("textInIFrameWithValue3")).getText();

		} finally {
			if (page != null) {
				page.getDriver().close();
			}
		}
	}


	@Test(groups={"it"})
	public void testScreenshotWithMetadataOnShadowDom() throws IOException {
		setBrowser("chrome");
		DriverTestPageShadowDom page = null;
		try {
			page = new DriverTestPageShadowDom(true);
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
			ScreenShot screenshot = new ScreenshotUtil((CustomEventFiringWebDriver) page.getDriver()).capture(SnapshotTarget.PAGE, ScreenShot.class);
			Assert.assertTrue(screenshot.getLocation().contains("testShadow.html"));
			Assert.assertEquals(screenshot.getTitle(), "Current Window: Level Access Inc. ShadowDOM Examples from https://not.webaccessibility.com/shadowdom.html");
			Assert.assertTrue(screenshot.getImage().getFile().exists());
			Assert.assertTrue(screenshot.getHtml().getFile().exists());
			String htmlContent = FileUtils.readFileToString(screenshot.getHtml().getFile(), StandardCharsets.UTF_8);
			Assert.assertTrue(htmlContent.contains("<div id=\"shadow\"><label for=\"fail1\">This is a label</label></div>")); // shadow dom content is present
			Assert.assertTrue(htmlContent.contains("<div id=\"shadow12\"><div id=\"anId\"><iframe id=\"frame11\" srcdoc=\"&lt;!DOCTYPE html&gt;&lt;html lang")); // iframe in shadow dom is present

		} finally {
			if (page != null) {
				page.getDriver().close();
			}
		}
	}

}
