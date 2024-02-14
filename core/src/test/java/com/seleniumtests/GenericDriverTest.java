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
package com.seleniumtests;

import java.io.IOException;
import java.net.ServerSocket;

import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.util.osutility.OSUtility;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.video.VideoCaptureMode;

/**
 * Parent test class for tests when driver needs to be closed after each test
 * @author s047432
 *
 */
@Listeners({CaptureVideoListener.class})
public class GenericDriverTest {
	protected static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(SeleniumRobotTestPlan.class);
	
	public WebDriver driver = null;

	@BeforeMethod(groups={"ut", "it"})  
	public void initTest(final ITestContext testNGCtx, final ITestResult testResult) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, testResult);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumGridDriverFactory.resetCounter();
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		try {
			SeleniumTestsContextManager.initThreadContext(testNGCtx, GenericTest.generateResult(testNGCtx, getClass()));
		} catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException
				| IllegalAccessException e) {
		}
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
	}
	
	/**
	 * destroys the driver if one has been created
	 */
	@AfterMethod(groups={"ut", "it"}, alwaysRun=true)
	public void destroyDriver() {
		WebUIDriver.cleanUp();

		GenericTest.resetTestNGREsultAndLogger();
		OSUtility.resetInstalledBrowsersWithVersion();
	}
	
	public static int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			int port = socket.getLocalPort();
			
			return port;
		} catch (IOException e) { 
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		throw new IllegalStateException("Could not find a free TCP/IP port ");
	}
	
	public void myTest() {
		
	}
}
