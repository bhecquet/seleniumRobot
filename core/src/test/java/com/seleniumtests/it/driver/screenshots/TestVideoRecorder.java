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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.VideoRecorder;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.helper.WaitHelper;

public class TestVideoRecorder extends ReporterTest {

	/**
	 * check file is created
	 * @throws IOException
	 */
	@Test(groups="it")
	public void testVideoRecording() throws IOException {
		VideoRecorder recorder = new VideoRecorder(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()), "testFile.avi");
		recorder.start();
		WaitHelper.waitForSeconds(2);
		File recorded = recorder.stop();
		Assert.assertTrue("testFile.avi".equals(recorded.getName()));
		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "testFile.avi").toFile().exists());
		Assert.assertEquals(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()).listFiles().length, 1);
	}
	

	/**
	 * Check video is created and content is correct
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureEnabled(ITestContext testContext) throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
//			System.setProperty(SeleniumTestsContext.RUN_MODE, "grid");
//			System.setProperty(SeleniumTestsContext.WEB_DRIVER_GRID, "http://localhost:4444/wd/hub");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
		
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShort", "videoCapture.avi").toFile().exists());			

			String detailedReportContent = readTestMethodResultFile("testDriverShort");
			Assert.assertTrue(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	
	/**
	 * video must be produced when on success and tests are OK
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureOnSuccessWithTestsOK(ITestContext testContext) throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "onSuccess");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShort", "videoCapture.avi").toFile().exists());			

			String detailedReportContent = readTestMethodResultFile("testDriverShort");
			Assert.assertTrue(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
	
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	
	/**
	 * video should not be produced when on success and tests are KO
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureOnSuccessWithTestsKO(ITestContext testContext) throws Exception {

		try {	
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "onSuccess");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "videoCapture.avi").toFile().exists());			
			String detailedReportContent = readTestMethodResultFile("testDriverShortKo");
			Assert.assertFalse(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	/**
	 * video should not be produced when on error and tests are OK
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureOnErrorWithTestsKO(ITestContext testContext) throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "onError");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "videoCapture.avi").toFile().exists());	
			String detailedReportContent = readTestMethodResultFile("testDriverShortKo");
			Assert.assertTrue(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	
	/**
	 * video should be produced when on success and tests are KO
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureOnErrorWithTestsOK(ITestContext testContext) throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "onError");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
			
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShort", "videoCapture.avi").toFile().exists());			
			String detailedReportContent = readTestMethodResultFile("testDriverShort");
			Assert.assertFalse(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	
	/**
	 * Check capture is disabled (by default, it's created on error)
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureDisabled(ITestContext testContext) throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "false");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShortKo"});
		
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortKo", "videoCapture.avi").toFile().exists());			

			String detailedReportContent = readTestMethodResultFile("testDriverShortKo");
			Assert.assertFalse(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	

	/**
	 * Check video is created and content is correct
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureStartsInBeforeMethod(ITestContext testContext) throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForVideoTest.testDriverShortWithDataProvider"}, "", "video");

			// check video file has been moved to 'testDriverShortWithDataProvider' folder
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortWithDataProvider", "videoCapture.avi").toFile().exists());			
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "before-testDriverShortWithDataProvider", "videoCapture.avi").toFile().exists());			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortWithDataProvider-1", "videoCapture.avi").toFile().exists());			

			String detailedReportContent = readTestMethodResultFile("testDriverShortWithDataProvider");
			Assert.assertTrue(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	
	/**
	 * Check video is created and content is correct
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testVideoCaptureStartsInTestMethod(ITestContext testContext) throws Exception {
		
		try {
			System.setProperty(SeleniumTestsContext.VIDEO_CAPTURE, "true");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForVideoTest.testDriverShortWithDataProvider"}, "", "stub");
			
			// check video file has been moved to 'testDriverShortWithDataProvider' folder
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortWithDataProvider", "videoCapture.avi").toFile().exists());			
			Assert.assertFalse(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "before-testDriverShortWithDataProvider", "videoCapture.avi").toFile().exists());			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverShortWithDataProvider-1", "videoCapture.avi").toFile().exists());			
			
			String detailedReportContent = readTestMethodResultFile("testDriverShortWithDataProvider");
			Assert.assertTrue(detailedReportContent.contains("Video capture: <a href='videoCapture.avi'>file</a></div>"));
		} finally {
			System.clearProperty(SeleniumTestsContext.VIDEO_CAPTURE);
		}
	}
	

}
