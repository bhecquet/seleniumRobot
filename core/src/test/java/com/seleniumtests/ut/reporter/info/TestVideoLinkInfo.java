package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.info.VideoLinkInfo;

import com.seleniumtests.reporter.logger.FileContent;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

public class TestVideoLinkInfo extends GenericTest {

    @Test(groups = {"ut"})
    public void testHtmlLink() {
        File videoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", "videoCapture.mp4").toFile();
        String formatHtml = new VideoLinkInfo(new FileContent(videoFile))
                .encode("html");
        Assert.assertEquals(formatHtml, "<a href=\"testHtmlLink/video/videoCapture.mp4\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a>");
    }

    @Test(groups = {"ut"})
    public void testOtherFormatLink() {
        File videoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "videoCapture.mp4").toFile();
        String formatOther = new VideoLinkInfo(new FileContent(videoFile))
                .encode("avi");
        Assert.assertEquals(formatOther, "Video");
    }


    @Test(groups = {"ut"})
    public void testNullFormatLink() {
        File videoFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "videoCapture.mp4").toFile();
        String formatNull = new VideoLinkInfo(new FileContent(videoFile))
                .encode(null);
        Assert.assertEquals(formatNull, "Video");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", "videoCapture.mp4").toFile();
        JSONObject json = new VideoLinkInfo(new FileContent(imageFile))
                .toJson();
        Assert.assertEquals(json.getString("type"), "videolink");
        Assert.assertEquals(json.getString("info"), "Video");
        Assert.assertEquals(json.getString("link"), "videoCapture.mp4");
        Assert.assertNull(json.toMap().get("id"));
    }

    @Test(groups = {"ut"})
    public void testToJsonWithId() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "video", "videoCapture.mp4").toFile();
        JSONObject json = new VideoLinkInfo(new FileContent(imageFile, 12))
                .toJson();
        Assert.assertEquals(json.getString("type"), "videolink");
        Assert.assertEquals(json.getString("info"), "Video");
        Assert.assertEquals(json.getString("link"), "videoCapture.mp4");
        Assert.assertEquals(json.getInt("id"), 12);
    }
}

