package com.seleniumtests.ut.util.video;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.video.VideoUtils;

public class TestVideoUtils {
	

	public static File createVideoFileFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("video", ".avi");
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}
	
	@Test(groups= {"ut"})
	public void testReadAvi() throws IOException {
		
		LocalDateTime videoStartDateTime = LocalDateTime.parse("2021-11-15T13:15:30");
		
		TestStepManager.getInstance().setVideoStartDate(Date.from(videoStartDateTime.toInstant(ZoneOffset.UTC)));
		TestStep step = new TestStep("step 1", null, new ArrayList<String>(), false);
		step.setStartDate(Date.from(videoStartDateTime.plusSeconds(1).toInstant(ZoneOffset.UTC)));
		VideoUtils.extractReferenceForSteps(createVideoFileFromResource("tu/video/videoCapture.avi"), null, null);
		
		
	}
	

}
