package com.seleniumtests.it.connector.extools;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.FFMpeg;
import com.seleniumtests.reporter.logger.TestStep;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class TestFFMpeg extends GenericTest {

    @Test(groups = "it")
    public void testAviEncoding() throws IOException {
        File videoFile = createFileFromResource("tu/video/videoCapture.avi");
        Path mp4VideoFile = Files.createTempFile("video", ".mp4");

        mp4VideoFile.toFile().delete(); // delete file so that FFMPEG can create it
        mp4VideoFile.toFile().deleteOnExit();

        String out = new FFMpeg().runFFmpegCommand(List.of("-i", videoFile.getAbsolutePath(), "-c:v", "libopenh264", "-preset", "veryfast", "-tune", "animation", mp4VideoFile.toAbsolutePath().toString()));
        logger.info(out);


        Assert.assertTrue(Files.exists(mp4VideoFile));
        Assert.assertTrue(Files.size(mp4VideoFile) > 0);
        logger.info("Video file size is " + Files.size(mp4VideoFile));
    }

    @Test(groups = "it")
    public void testAddChapters() throws IOException {

        // creates the mp4 file
        File videoFile = createFileFromResource("tu/video/videoCapture.avi");
        Path mp4VideoFile = Files.createTempFile("video", ".mp4");

        Files.deleteIfExists(mp4VideoFile); // delete file so that FFMPEG can create it
        mp4VideoFile.toFile().deleteOnExit();

        new FFMpeg().runFFmpegCommand(List.of("-i", videoFile.getAbsolutePath(), "-c:v", "libopenh264", "-preset", "veryfast", "-tune", "animation", mp4VideoFile.toAbsolutePath().toString()));

        TestStep step1 = new TestStep("step 1");
        step1.setVideoTimeStamp(40);
        step1.setDuration(3000L);
        TestStep step2 = new TestStep("step 2");
        step2.setVideoTimeStamp(3040);
        step2.setDuration(10040L);

        new FFMpeg().addChapters(mp4VideoFile.toFile(), Stream.of(step1, step2)
                .map(step -> new FFMpeg.Chapter(step.getName(), step.getVideoTimeStamp(), step.getVideoTimeStamp() + step.getDuration()))
                .toList());

        // check metadata
        String ffmpegOut = new FFMpeg().runFFmpegCommand(List.of("-i", mp4VideoFile.toAbsolutePath().toString()));
        Assert.assertTrue(ffmpegOut.contains("Chapter #0:0: start 0.000000, end 3.040000"));
        Assert.assertTrue(ffmpegOut.contains("Chapter #0:1: start 3.040000, end 13.080000"));
    }
}
