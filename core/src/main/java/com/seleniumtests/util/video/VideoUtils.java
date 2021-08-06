package com.seleniumtests.util.video;

import static java.lang.Math.min;
import static org.monte.media.FormatKeys.MediaTypeKey;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.video.avi.AVIReader;

/**
 * Class for doing several action on video recorded by SeleniumRobot
 * @author S047432
 *
 */
public class VideoUtils {
	

	private static final Logger logger = SeleniumRobotLogger.getLogger(VideoUtils.class);

	/**
	 * Extract the picture associated to the beginning of a step to <output_dir>/video
	 * @param videoFile
	 * @param testSteps
	 * @param outputDirectory
	 */
	public static void extractReferenceForSteps(File videoFile, List<TestStep> testSteps, Path outputDirectory) {
		
		// create output
		Path videoOutputDirectory = outputDirectory.resolve("video");
		videoOutputDirectory.toFile().mkdirs();
		AVIReader in = null;
		try {
            // Create the reader
            in = new AVIReader(videoFile);
            
            // Look for the first video track
            int trackId = 0;
            while (trackId < in.getTrackCount()
                    && in.getFormat(trackId).get(MediaTypeKey) != MediaType.VIDEO) {
                trackId++;
            }
            
    		Map<Long, TestStep> samples = new HashMap<>();
    		for (TestStep testStep: testSteps) {
    			if (!testStep.getName().equals(TestStepManager.LAST_STEP_NAME)) {
    				// timestamp outside of video, do not try to extract as we would get the last picture
    				if (testStep.getVideoTimeStamp() / 1000 * in.getTimeScale(trackId) > in.getChunkCount(0)) {
    					continue;
    				}
    				
    				samples.put(min(in.timeToSample(trackId, new Rational(testStep.getVideoTimeStamp(), 1000)), in.getChunkCount(trackId) - 1), testStep);
    			}
    		}
    			
            // Read images from the track
            BufferedImage img = null;
            
            // read video and extract requested images
            long i = 0;
            int j = 0;
            do {
                img = in.read(trackId, img);
                if (samples.containsKey(i)) {
	            	Path extractedPicture = videoOutputDirectory.resolve(String.format("video-%d.jpg", j));
	                FileUtility.writeImage(extractedPicture.toString(), img);
	                samples.get(i).addSnapshot(new Snapshot(new ScreenShot(outputDirectory.relativize(extractedPicture).toString()), "Step beginning state", SnapshotCheckType.REFERENCE_ONLY), j, null);
	                j++;
                }
                i++;
                
            } while (img != null);

        } catch (IOException e) {
			logger.error("Cannot extract step reference " + e.getMessage());
		} finally {
            // Close the reader
            if (in != null) {
                try {
					in.close();
				} catch (IOException e) {
					logger.error("Cannot close video reader " + e.getMessage());
				}
            }
        }
		
	}
}
