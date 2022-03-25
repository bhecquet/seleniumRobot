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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.internal.TestNGMethod;
import org.testng.internal.TestResult;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.video.VideoCaptureMode;

public class GenericTest {
	
	protected static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(SeleniumRobotTestPlan.class);

	/**
	 * Reinitializes context between tests so that it's clean before test starts
	 * Beware that this reset does not affect the set context
	 * @param testNGCtx
	 */
	@BeforeMethod(groups={"ut", "it", "ut context2"})  
	public void initTest(final ITestContext testNGCtx, final ITestResult testResult) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, testResult);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContext.resetOutputFolderNames();
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		try {
			SeleniumTestsContextManager.initThreadContext(testNGCtx, generateResult(testNGCtx, getClass()));
		} catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException
				| IllegalAccessException e) {
		}
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContext.resetOutputFolderNames();
	}
	
	@AfterMethod(groups={"ut", "it", "ut context2"}, alwaysRun=true) 
	public void reset() {
		resetTestNGREsultAndLogger();
	}
	
	@AfterClass(groups={"ut", "it", "ut context2"}, alwaysRun=true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	public static File createFileFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", null);
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}
	

	public static String readResourceToString(String resourceName) throws IOException {
		return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
	}
	

	public static void resetCurrentTestResult() {
		//Reporter.setCurrentTestResult(null); // do not reset, TestNG do this for us
	}

	public static void resetTestNGREsultAndLogger() {
		resetCurrentTestResult();
		
		try {
			SeleniumRobotLogger.reset();
		} catch (IOException e) {
			logger.error("Cannot delete log file" + e.getMessage());
		}
	}
	

	/**
	 * Generate a ITestResult from scratch
	 * @param testNGCtx
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static ITestResult generateResult(final ITestContext testNGCtx, final Class<?> clazz) throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ITestResult testResult = TestResult.newEmptyTestResult();
		testResult.setParameters(new String[] {"foo", "bar"});
		
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		XmlTest test = new XmlTest(suite);
		test.setName("myTestNg");
		
		ITestNGMethod testMethod = new TestNGMethod(clazz.getMethod("myTest"), new JDK15AnnotationFinder(new DefaultAnnotationTransformer()), test, null);
		Field methodField = TestResult.class.getDeclaredField("m_method");
		methodField.setAccessible(true);
		methodField.set(testResult, testMethod);
		Field contextField = TestResult.class.getDeclaredField("m_context");
		contextField.setAccessible(true);
		contextField.set(testResult, testNGCtx);
		
		return testResult;
	}
	
	public void myTest() {
		
	}
}
