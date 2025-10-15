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
package com.seleniumtests.it.util;

import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.htmlelements.ImageElement;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;
import com.seleniumtests.util.imaging.ImageProcessor;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

import java.awt.image.BufferedImage;

public class TestPageObject extends GenericDriverTest {

	
	@Test(groups={"it"})
	public void testResizeWindow() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true).resizeTo(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}
	
	/**
	 * issue #421: check snapshot is not done when user set captureSnapshot=false
	 */
	@Test(groups={"it"})
	public void testSnapshotNotLogged() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true);
		
		// 0 capture because capture snapshot is set to false
		Assert.assertTrue(TestStepManager.getCurrentOrPreviousStep().getAllAttachments(true).isEmpty());

	}
	@Test(groups={"it"})
	public void testSnapshotLogged() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(true);
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true);
		
		// one capture, due to opening page
		Assert.assertFalse(TestStepManager.getCurrentOrPreviousStep().getAllAttachments(true).isEmpty());
		Assert.assertEquals(TestStepManager.getCurrentOrPreviousStep().getAllAttachments(true).size(), 1);
		
	}

	@Test(groups={"it"})
	public void testPageLoadTimeLogged() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		DriverTestPage page = new DriverTestPage(true);
		TestStep step1 = TestStepManager.getCurrentOrPreviousStep();
		Assert.assertNotNull(step1.getPageLoadTime());
		Assert.assertEquals(step1.getAction(), "openPage");
		Assert.assertEquals(step1.getOrigin().getSimpleName(), "DriverTestPage");
		Assert.assertTrue(step1.getPageLoadTime().getLoadTime() > 0.1);
		Assert.assertTrue(step1.getPageLoadTime().getUrl().contains("test.html"));
		Assert.assertTrue(step1.getPageLoadTime().getName().contains("loading of DriverTestPage took"));

		page._getTextElementContent();
		TestStep step2 = TestStepManager.getCurrentOrPreviousStep();

		// no page has been loaded for this step
		Assert.assertNull(step2.getPageLoadTime());
	}
	
	@Test(groups={"it"})
	public void testResizeWindowHeadless() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true);
		new DriverTestPage(true).resizeTo(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}

	/**
	 * Check that when an element is excluded from snapshot, its size respects the device aspect ratio
	 * Better use a screen at a zoom level > 100% to fully check
	 */
	@Test(groups={"it"})
	public void testElementScreenshotWithExclusionUsingAspectRatio() throws Exception {

		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(true);
		DriverTestPage page = new DriverTestPage(true);
		WebDriver driver = WebUIDriver.getWebDriver(true);
		try {
			ImageElement element = new ImageElement("", By.id("images"));
			TextFieldElement elementToExclude = new TextFieldElement("", By.id("logoText"));

			page.captureElementSnapshot("my capture", element, SnapshotCheckType.FULL.exclude(elementToExclude));

			Snapshot elementSnapshot = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(1).getSnapshots().get(1);
			Assert.assertEquals(elementSnapshot.getName(), "drv:main-my capture");
			BufferedImage image = ImageProcessor.loadFromFile(elementSnapshot.getScreenshot().getImage().getFile());

			double aspectRatio = ((CustomEventFiringWebDriver)driver).getDeviceAspectRatio();
			Assert.assertTrue(Math.abs(image.getWidth() - element.getRect().getWidth() * aspectRatio) < 1);
			Assert.assertTrue(Math.abs(image.getHeight() - element.getRect().getHeight() * aspectRatio) < 1);
			Assert.assertEquals(elementSnapshot.getCheckSnapshot().getExcludeElementsRect().size(), 1);
			Rectangle excludedRectangle = elementSnapshot.getCheckSnapshot().getExcludeElementsRect().get(0);
			Assert.assertTrue(Math.abs(excludedRectangle.width - elementToExclude.getRect().width * aspectRatio) < 1);
			Assert.assertTrue(Math.abs(excludedRectangle.height - elementToExclude.getRect().height * aspectRatio) < 1);
			Assert.assertTrue(excludedRectangle.x < 2);
			Assert.assertTrue(excludedRectangle.y < image.getHeight() / 2 + 10);

		} finally {
			driver.close();
		}
	}

}
