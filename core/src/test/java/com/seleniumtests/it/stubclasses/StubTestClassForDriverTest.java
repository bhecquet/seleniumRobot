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
package com.seleniumtests.it.stubclasses;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.MockitoTestListener;
import com.seleniumtests.it.driver.support.pages.*;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.seleniumtests.WebTestPageServer;
import com.seleniumtests.connectors.extools.Lighthouse;
import com.seleniumtests.connectors.extools.Lighthouse.Category;
import com.seleniumtests.connectors.extools.LighthouseFactory;
import com.seleniumtests.connectors.extools.WcagChecker;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.helper.WaitHelper;

@Listeners(MockitoTestListener.class)
public class StubTestClassForDriverTest extends StubParentClass {
	
	@BeforeMethod(groups="stub")
	public void init(Method method) {

		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		if (System.getProperty(SeleniumTestsContext.TEST_RETRY_COUNT) == null) {
			SeleniumTestsContextManager.getThreadContext().setTestRetryCount(0);
		}
	}
	
	/**
	 * added for issue #287 where we need to fail on configuration method to reproduce the bug
	 */
	@AfterMethod(groups="stub") 
	public void reset(Method method) {
		TestTasks.killProcess("foobar");
	}

	@Test(groups="stub")
	public void testDriver() {

		new DriverTestPage(true)
			._writeSomething()
			._reset()
			._sendKeysComposite()
			._clickPicture();
	}

	@Test(groups="stub")
	public void testDriverExposedViaWebServer() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);

		WebTestPageServer server = new WebTestPageServer();
		try {

			server.exposeTestPage();
			String localAddress = server.getLocalAddress();
			DriverTestPage page = new DriverTestPage(true, String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort()));

			page._writeSomething()
					._reset()
					._sendKeysComposite()
					._clickPicture();

		} finally {
			server.stopServer();
		}
	}
	
	@Test(groups="stub")
	public void testDriverLongFailed() {
		
		DriverTestPage page = new DriverTestPage(true)
		._writeSomething()
		._reset();
		WaitHelper.waitForSeconds(15);
		page._sendKeysComposite()
		._writeSomethingOnNonExistentElement();
	}
	
	@Test(groups="stub")
	public void testDriverFailed() {
		
		new DriverTestPage(true)
				._sendKeysComposite()
		._writeSomethingOnNonExistentElement();
	}
	
	@Test(groups="stub")
	public void testDriverWithWcag() {
		
		DriverTestPage page = new DriverTestPage(true)
		._writeSomething();
		
		WcagChecker.analyze(page.getDriver());
	}
	
	/**
	 * issue #414
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverWithFailureAfterSwitchToFrame() {
		
		new DriverTestPage(true)
		._goToFrame();
	}
	
	@Test(groups="stub")
	public void testMultipleDriver() {
		
		new DriverTestPage(true)
		._writeSomething()
		._reset();
		new DriverTestPage(true, BrowserType.CHROME)
		._writeSomething()
		._reset();
	}
	
	@Test(groups="stub")
	public void testDriverIsDisplayedRetry() {
		new DriverTestPage(true)._isElementNotPresentDisplayed();
	}
	
	@Test(groups="stub")
	public void testDriverShort() {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverShort2() {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverShort3() {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverShort4() {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverMultipleSnapshot() throws Exception {
		new DriverTestPage(true)
			._goToNewPage();
	}
	
	@Test(groups="stub")
	public void testDriverShortKo() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
			._writeSomethingOnNonExistentElement();
	}

	@Test(groups="stub", dependsOnMethods = "testDriverShortKo")
	public void testDriverShortSkipped() {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverWithAssert() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		DriverTestPage page = new DriverTestPage(true);
		page._reset();
		Assert.assertTrue(false);
		page._writeSomething();
	}

	@Test(groups="stub")
	public void testDriverWithLighthouse() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		WebTestPageServer server = new WebTestPageServer();
		try {
			
			server.exposeTestPage();
			String localAddress = server.getLocalAddress();
			DriverTestPage page = new DriverTestPage(true, String.format("http://%s:%d/test.html", localAddress, server.getServerHost().getPort()));

			Lighthouse lighthouseInstance = LighthouseFactory.getInstance();
			lighthouseInstance.execute(page.getUrl(), new ArrayList<>());
			logger.logTestValue("accessibility", "accessibility", lighthouseInstance.getScore(Category.ACCESSIBILITY).toString());
			
			page._reset();
			lighthouseInstance.execute("some.bad.url", new ArrayList<>());
	
			page._writeSomething();
		} finally {
			server.stopServer();
		}
	}
	
	@Test(groups="stub")
	public void testDriverShortKoWithCatchException() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
			._writeSomethingOnNonExistentElementWithCatch()
			._writeSomething();
		
	}
	
	@Test(groups="stub")
	public void testDriverCustomSnapshot() {
		
		new DriverTestPage(true)
		._writeSomething()
		._captureSnapshot("my snapshot")
		._reset();
	}

	@Test(groups="stub")
	public void testDriverPictureElementNotFound() {
		new DriverTestPage(true)
				._clickPictureNotPresent();
	}

	@Test(groups="stub")
	public void testDriverPictureElement() {
		new DriverTestPage(true)
				._clickPicture();
	}
	

	@Test(groups="stub")
	public void testDriverModalSnapshot() {
		
		new DriverModalTestPage(true)
			._openModal()
			._captureSnapshot("my snapshot");
	}
	
	@Test(groups="stub")
	public void testDriverWithFailure() {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
		._writeSomething()
		._writeSomethingOnNonExistentElement();
	}
	
	@Test(groups="stub")
	public void testDriverManualSteps() {

		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);

		addStep("Write");
		DriverTestPage page = new DriverTestPage(true)
			._writeSomething();
		addStep("Reset");
		page._reset();
	}
	
	/**
	 * check that with selenium override, logging is done
	 */
	@Test(groups="stub")
	public void testDriverNativeActions() {
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset()
		.select();
	}

	@Test(groups="stub")
	public void testDriverNativeActionsOnPageObjectFactory() {
		List<WebElement> inputs = new DriverTestPageObjectFatory(true)
		.sendKeys()
		.reset()
		.select()
		.switchToFirstFrameByIndex()
		.getElementsInsideFrame();
		logger.info(inputs.size());
	}

	@Test(groups="stub")
	public void testDriverNativeActionsOnPageObjectFactoryWithoutOverride() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		List<WebElement> inputs = new DriverTestPageObjectFatory(true)
				.sendKeys()
				.reset()
				.select()
				.switchToFirstFrameByIndex()
				.getElementsInsideFrame();
		logger.info(inputs.size());
	}
	
	/**
	 * check that without selenium override, logging is not done
	 */
	@Test(groups="stub")
	public void testDriverNativeActionsWithoutOverride() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1); 
		
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset()
		.select();
	}
	
	@Test(groups="stub")
	public void testDriverWithHtmlElementWithoutOverride() {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		
		new DriverTestPage(true)
			._writeSomething()
			._reset()
			._clickPicture()
			._sendKeysComposite();
	}
	
	@Test(groups="stub")
	public void testImageDetection() {
		new ImageDetectorPage()
			._clickErrorButtonInError();// force test to fail
	}
	
	@Test(groups="stub")
	public void testImageDetectionAssertionError() {
		
		new ImageDetectorPage()
		._clickErrorButton();
		Assert.assertTrue(false);
	}
	
	@Test(groups="stub")
	public void testImageDetectionNoError() {
		
		new ImageDetectorPage()
		._clickErrorButton();
	}

	@Test(groups="stub")
	public void testDownloadFile() {
		DriverPDFPage page = new DriverPDFPage(BrowserType.CHROME);
		page.clickPDFToDownload();
		File file = TestTasks.getDownloadedFile("nom-du-fichier.pdf");
		Assert.assertNotNull(file, "file is null");
		Assert.assertTrue(file.exists());
		logger.logFile(file, "PDF example");
	}

	@Test(groups="stub")
	public void testUserAgent() {
		DriverTestPage dtp = new DriverTestPage(true)
				._writeSomething()
				._reset();
		dtp.getBrowserUserAgent();
	}

}
