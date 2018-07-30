/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.internal.TestNGMethod;
import org.testng.internal.TestResult;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;

public class GenericTest {

	/**
	 * Reinitializes context between tests so that it's clean before test starts
	 * Beware that this reset does not affect the set context
	 * @param testNGCtx
	 */
	@BeforeMethod(groups={"ut", "it"})  
	public void initTest(final ITestContext testNGCtx, final ITestResult testResult) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, null, null, testResult);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContext.resetOutputFolderNames();
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		try {
			SeleniumTestsContextManager.initThreadContext(testNGCtx, null, null, generateResult(testNGCtx, getClass()));
		} catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException
				| IllegalAccessException e) {
		}
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getThreadContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContextManager.getGlobalContext().setVideoCapture(VideoCaptureMode.FALSE.toString());
		SeleniumTestsContext.resetOutputFolderNames();
	}
	
	@AfterClass(groups={"ut", "it"})
	public void closeBrowser() {
		WebUIDriver.cleanUp();
		WebUIDriver.cleanUpWebUIDriver();
	}
	
	protected File createFileFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", null);
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
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
		ITestResult testResult = new TestResult();
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
