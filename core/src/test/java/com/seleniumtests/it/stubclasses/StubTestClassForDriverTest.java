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
package com.seleniumtests.it.stubclasses;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
	public void init(Method method) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		if (System.getProperty(SeleniumTestsContext.TEST_RETRY_COUNT) == null) {
			SeleniumTestsContextManager.getThreadContext().setTestRetryCount(0);
		}
	}
	
	/**
	 * added for issue #287 where we need to fail on configuration method to reproduce the bug
	 * @param method
	 */
	@AfterMethod(groups="stub") 
	public void reset(Method method) {
		TestTasks.killProcess("foobar");
	}

	@Test(groups="stub")
	public void testDriver() throws Exception {

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
	public void testDriverLongFailed() throws Exception {
		
		DriverTestPage page = new DriverTestPage(true)
		._writeSomething()
		._reset();
		WaitHelper.waitForSeconds(15);
		page._sendKeysComposite()
		._writeSomethingOnNonExistentElement();
	}
	
	@Test(groups="stub")
	public void testDriverFailed() throws Exception {
		
		DriverTestPage page = new DriverTestPage(true)
				._sendKeysComposite()
		._writeSomethingOnNonExistentElement();
	}
	
	@Test(groups="stub")
	public void testDriverWithWcag() throws Exception {
		
		DriverTestPage page = new DriverTestPage(true)
		._writeSomething();
		
		WcagChecker.analyze(page.getDriver());
	}
	
	/**
	 * issue #414
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverWithFailureAfterSwitchToFrame() throws Exception {
		
		new DriverTestPage(true)
		._goToFrame();
	}
	
	@Test(groups="stub")
	public void testMultipleDriver() throws Exception {
		
		new DriverTestPage(true)
		._writeSomething()
		._reset();
		new DriverTestPage(true, BrowserType.CHROME)
		._writeSomething()
		._reset();
	}
	
	@Test(groups="stub")
	public void testDriverIsDisplayedRetry() throws Exception {
		new DriverTestPage(true)._isElementNotPresentDisplayed();
	}
	
	@Test(groups="stub")
	public void testDriverShort() throws Exception {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverShort2() throws Exception {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverShort3() throws Exception {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverShort4() throws Exception {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverMultipleSnapshot() throws Exception {
		new DriverTestPage(true)
			._goToNewPage();
	}
	
	@Test(groups="stub")
	public void testDriverShortKo() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
			._writeSomethingOnNonExistentElement();
	}

	@Test(groups="stub", dependsOnMethods = "testDriverShortKo")
	public void testDriverShortSkipped() throws Exception {
		new DriverTestPage(true);
	}
	
	@Test(groups="stub")
	public void testDriverWithAssert() throws Exception {
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
	public void testDriverShortKoWithCatchException() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
			._writeSomethingOnNonExistentElementWithCatch()
			._writeSomething();
		
	}
	
	@Test(groups="stub")
	public void testDriverCustomSnapshot() throws Exception {
		
		new DriverTestPage(true)
		._writeSomething()
		._captureSnapshot("my snapshot")
		._reset();
	}

	@Test(groups="stub")
	public void testDriverPictureElementNotFound() throws Exception {
		new DriverTestPage(true)
				._clickPictureNotPresent();
	}

	@Test(groups="stub")
	public void testDriverPictureElement() throws Exception {
		new DriverTestPage(true)
				._clickPicture();
	}
	

	@Test(groups="stub")
	public void testDriverModalSnapshot() throws Exception {
		
		new DriverModalTestPage(true)
			._openModal()
			._captureSnapshot("my snapshot");
	}
	
	@Test(groups="stub")
	public void testDriverWithFailure() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		
		new DriverTestPage(true)
		._writeSomething()
		._writeSomethingOnNonExistentElement();
	}
	
	@Test(groups="stub")
	public void testDriverManualSteps() throws Exception {

		SeleniumTestsContextManager.getThreadContext().setManualTestSteps(true);

		addStep("Write");
		DriverTestPage page = new DriverTestPage(true)
			._writeSomething();
		addStep("Reset");
		page._reset();
	}
	
	/**
	 * check that with selenium override, logging is done
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverNativeActions() throws Exception {
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset()
		.select();
	}

	@Test(groups="stub")
	public void testDriverNativeActionsOnPageObjectFactory() throws Exception {
		List<WebElement> inputs = new DriverTestPageObjectFatory(true)
		.sendKeys()
		.reset()
		.select()
		.switchToFirstFrameByIndex()
		.getElementsInsideFrame();
		logger.info(inputs.size());
	}

	@Test(groups="stub")
	public void testDriverNativeActionsOnPageObjectFactoryWithoutOverride() throws Exception {
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
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverNativeActionsWithoutOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1); 
		
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset()
		.select();
	}
	
	@Test(groups="stub")
	public void testDriverWithHtmlElementWithoutOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		
		new DriverTestPage(true)
			._writeSomething()
			._reset()
			._clickPicture()
			._sendKeysComposite();
	}
	
	@Test(groups="stub")
	public void testImageDetection() throws Exception {
		new ImageDetectorPage()
			._clickErrorButtonInError();// force test to fail
	}
	
	@Test(groups="stub")
	public void testImageDetectionAssertionError() throws Exception {
		
		new ImageDetectorPage()
		._clickErrorButton();
		Assert.assertTrue(false);
	}
	
	@Test(groups="stub")
	public void testImageDetectionNoError() throws Exception {
		
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
