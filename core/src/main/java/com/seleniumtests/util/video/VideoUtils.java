package com.seleniumtests.util.video;

import static java.lang.Math.min;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.reporter.reporters.CommonReporter;
import org.apache.logging.log4j.Logger;
import org.monte.media.av.FormatKeys;
import org.monte.media.avi.AVIReader;
import org.monte.media.math.Rational;

import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for doing several action on video recorded by SeleniumRobot
 * @author S047432
 *
 */
public class VideoUtils {
	

	private static final Logger logger = SeleniumRobotLogger.getLogger(VideoUtils.class);

	private VideoUtils() {
		// do nothing
	}
	
	/**
	 * Extract the picture associated to the beginning of a step to <output_dir>/video
	 * @param videoFile
	 * @param testSteps
	 * @param videoOutputDirectory	Directory where pictures from video will be extracted
	 */
	public static void extractReferenceForSteps(File videoFile, List<TestStep> testSteps, Path videoOutputDirectory) {

		videoOutputDirectory.toFile().mkdirs();
		AVIReader in = null;
		try {
            // Create the reader
            in = new AVIReader(videoFile);
            
            // Look for the first video track
            int trackId = 0;
            while (trackId < in.getTrackCount()
                    && in.getFormat(trackId).get(FormatKeys.MediaTypeKey) != FormatKeys.MediaType.VIDEO) {
                trackId++;
            }
            
    		Map<Long, TestStep> samples = new HashMap<>();
    		for (TestStep testStep: testSteps) {
    			if (!testStep.isTestEndStep()) {
    				// timestamp outside of video, do not try to extract as we would get the last picture
    				if (new Rational(testStep.getVideoTimeStamp() / 1000 * in.getTimeScale(trackId), in.getTimeScale(trackId)).compareTo(in.getDuration(0)) > 0) {
    					continue;
    				}

					// duration is <number_of_samples>/<time_scale>
    				samples.put(min(in.timeToSample(trackId, new Rational(testStep.getVideoTimeStamp(), 1000)), in.getDuration(0).multiply(in.getTimeScale(trackId)).intValue()), testStep);
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
	                try {
		                Snapshot snapshot = new Snapshot(new ScreenShot(extractedPicture.toFile(), null, SeleniumTestsContext.VIDEO_DIRECTORY), "Step beginning state", SnapshotCheckType.REFERENCE_ONLY);
		                snapshot.setDisplayInReport(false); // by default, reference snapshot won't be displayed in report. This flag will be set to "true" only if step fails and we have a reference picture from server
						samples.get(i).addSnapshot(snapshot, j, null);
	                } catch (FileNotFoundException e) {}
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
