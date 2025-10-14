package com.seleniumtests.ut.util.video;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.video.VideoUtils;

public class TestVideoUtils extends GenericTest {
	

	public static File createVideoFileFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("video", ".avi");
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}
	
	/**
	 * Test invalid file, no exception should be raised
	 */
	@Test(groups= {"ut"})
	public void testReadAviInvalidFile() throws IOException {
		
		OffsetDateTime videoStartDateTime = OffsetDateTime.parse("2021-11-15T13:15:30+01:00");
		
		TestStepManager.getInstance().setVideoStartDate(videoStartDateTime);
		TestStep step = new TestStep("step 1", "step 1", this.getClass(), null, new ArrayList<String>(), false);
		step.setStartDate(videoStartDateTime.plusSeconds(1));
		step.setVideoTimeStamp(1000);
		Assert.assertEquals(step.getSnapshots().size(), 0);
		
		VideoUtils.extractReferenceForSteps(createVideoFileFromResource("tu/env.ini"), Arrays.asList(step), Paths.get(SeleniumTestsContextManager.getThreadContext().getVideoOutputDirectory()));
		
	}
	
	/**
	 * Check a picture is extracted for each test step
	 */
	@Test(groups= {"ut"})
	public void testReadAvi() throws IOException {

		OffsetDateTime videoStartDateTime = OffsetDateTime.parse("2021-11-15T13:15:30+01:00");
		
		TestStepManager.getInstance().setVideoStartDate(videoStartDateTime);
		TestStep step = new TestStep("step 1", "step 1", this.getClass(), null, new ArrayList<String>(), false);
		step.setStartDate(videoStartDateTime.plusSeconds(1));
		step.setVideoTimeStamp(1000);
		Assert.assertEquals(step.getSnapshots().size(), 0);
		
		VideoUtils.extractReferenceForSteps(createVideoFileFromResource("tu/video/videoCapture.avi"), Arrays.asList(step), Paths.get(SeleniumTestsContextManager.getThreadContext().getVideoOutputDirectory()));
		
		Assert.assertEquals(step.getSnapshots().size(), 1);
		Assert.assertEquals(step.getSnapshots().get(0).getName(), "Step beginning state");
		
	}
	
	/**
	 * Check no error is raised and no picture is extracted if timestamp is out of video
	 */
	@Test(groups= {"ut"})
	public void testReadAviTimestampOutsideVideo() throws IOException {

		OffsetDateTime videoStartDateTime = OffsetDateTime.parse("2021-11-15T13:15:30+01:00");
		
		TestStepManager.getInstance().setVideoStartDate(videoStartDateTime);
		TestStep step = new TestStep("step 1", "step 1", this.getClass(), null, new ArrayList<String>(), false);
		step.setStartDate(videoStartDateTime.plusSeconds(100));
		step.setVideoTimeStamp(100000);
		Assert.assertEquals(step.getSnapshots().size(), 0);
		
		VideoUtils.extractReferenceForSteps(createVideoFileFromResource("tu/video/videoCapture.avi"), Arrays.asList(step), Paths.get(SeleniumTestsContextManager.getThreadContext().getVideoOutputDirectory()));
		
		Assert.assertEquals(step.getSnapshots().size(), 0);
		
	}
	
	/**
	 * Check no picture is extracted for "Test end" step
	 */
	@Test(groups= {"ut"})
	public void testReadAviLastStep() throws IOException {

		OffsetDateTime videoStartDateTime = OffsetDateTime.parse("2021-11-15T13:15:30+01:00");
		
		TestStepManager.getInstance().setVideoStartDate(videoStartDateTime);
		TestStep step = new TestStep(TestStepManager.LAST_STEP_NAME, TestStepManager.LAST_STEP_NAME, this.getClass(), null, new ArrayList<String>(), false);
		step.setStartDate(videoStartDateTime.plusSeconds(1));
		step.setVideoTimeStamp(1000);
		Assert.assertEquals(step.getSnapshots().size(), 0);
		
		VideoUtils.extractReferenceForSteps(createVideoFileFromResource("tu/video/videoCapture.avi"), Arrays.asList(step), Paths.get(SeleniumTestsContextManager.getThreadContext().getVideoOutputDirectory()));
		
		Assert.assertEquals(step.getSnapshots().size(), 0);
		
	}
	
	/**
	 * Check the case where several steps have the same timestamp. In this case, only the last step gets a picture
	 */
	@Test(groups= {"ut"})
	public void testReadAviSeveralStepsWithSameTimeStamp() throws IOException {

		OffsetDateTime videoStartDateTime = OffsetDateTime.parse("2021-11-15T13:15:30+01:00");
		
		TestStepManager.getInstance().setVideoStartDate(videoStartDateTime);
		TestStep step = new TestStep("step 1", "step 1", this.getClass(), null, new ArrayList<String>(), false);
		step.setStartDate(videoStartDateTime.plusSeconds(1));
		step.setVideoTimeStamp(1000);
		TestStep step2 = new TestStep("step 1", "step 1", this.getClass(), null, new ArrayList<String>(), false);
		step2.setStartDate(videoStartDateTime.plusSeconds(1));
		step2.setVideoTimeStamp(1000);
		Assert.assertEquals(step.getSnapshots().size(), 0);
		Assert.assertEquals(step2.getSnapshots().size(), 0);
		
		VideoUtils.extractReferenceForSteps(createVideoFileFromResource("tu/video/videoCapture.avi"), Arrays.asList(step, step2), Paths.get(SeleniumTestsContextManager.getThreadContext().getVideoOutputDirectory()));
		
		Assert.assertEquals(step.getSnapshots().size(), 0);
		Assert.assertEquals(step2.getSnapshots().size(), 1);
		
	}
	

}
