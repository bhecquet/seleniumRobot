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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.mockito.MockitoAnnotations;
import org.mockito.quality.Strictness;
import org.mockito.testng.MockitoSettings;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.video.VideoCaptureMode;


@Listeners({CaptureVideoListener.class, MockitoTestNGListener.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class MockitoTest {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(MockitoTest.class);

	protected static final String SERVER_URL = "http://localhost:4321";
	protected static final String GRID_SERVLET_URL = "http://localhost:4331"; // grid servlet are listening on port of router (or node) + 10
	private static Map<Method, Boolean> beforeMethodDone = Collections.synchronizedMap(new HashMap<>());

	@BeforeMethod(groups={"ut", "it", "ie"})  
	public void beforeMethod(final Method method, final ITestContext testNGCtx, final ITestResult testResult) throws Exception {
		doBeforeMethod(method);
		beforeMethodDone.put(method, true);
		initThreadContext(testNGCtx, null, testResult);
		MockitoAnnotations.initMocks(this); 
	}
	
	/**
	 * Something to do at the beginning of \@BerforeMethod in case this MUST be done before context initialization
	 */
	protected void doBeforeMethod(final Method method) {
		// do nothing
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx, null, null);
	}
	
	public void initThreadContext(final ITestContext testNGCtx,  final String testName, final ITestResult testResult) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, testResult);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
	}
	

	@AfterMethod(groups={"ut", "it", "ie"}, alwaysRun=true)
	public void afterMethod(final Method method) throws Exception {

		GenericTest.resetTestNGREsultAndLogger();
	}
	

	protected File createImageFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", "." + FilenameUtils.getExtension(resource));
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource),
				tempFile);

		return tempFile;
	}

	public void myTest() {
		
	}
	
}
