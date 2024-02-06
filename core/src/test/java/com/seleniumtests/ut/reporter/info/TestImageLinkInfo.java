package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.logger.FileContent;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.ImageLinkInfo;

import java.io.File;
import java.nio.file.Paths;

public class TestImageLinkInfo extends GenericTest {

    @Test(groups = {"ut"})
    public void testHtmlLink() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "image", "imageCapture.html").toFile();
        String formatHtml = new ImageLinkInfo(new FileContent(imageFile))
                .encode("html");
        Assert.assertEquals(formatHtml, "<a href=\"testHtmlLink/image/imageCapture.html\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a>");
    }


    @Test(groups = {"ut"})
    public void testOtherFormatLink() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "imageCapture.png").toFile();
        String formatOther = new ImageLinkInfo(new FileContent(imageFile))
                .encode("avi");
        Assert.assertEquals(formatOther, "Image");
    }


    @Test(groups = {"ut"})
    public void testNullFormatLink() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "imageCapture.png").toFile();
        String formatNull = new ImageLinkInfo(new FileContent(imageFile))
                .encode(null);
        Assert.assertEquals(formatNull, "Image");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "image", "imageCapture.png").toFile();
        JSONObject json = new ImageLinkInfo(new FileContent(imageFile))
                .toJson();
        Assert.assertEquals(json.getString("type"), "imagelink");
        Assert.assertEquals(json.getString("info"), "Image");
        Assert.assertEquals(json.getString("link"), "imageCapture.png");
        Assert.assertNull(json.toMap().get("id"));
    }
    @Test(groups = {"ut"})
    public void testToJsonWithId() {
        File imageFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), "image", "imageCapture.png").toFile();
        JSONObject json = new ImageLinkInfo(new FileContent(imageFile, 12))
                .toJson();
        Assert.assertEquals(json.getString("type"), "imagelink");
        Assert.assertEquals(json.getString("info"), "Image");
        Assert.assertEquals(json.getString("link"), "imageCapture.png");
        Assert.assertEquals(json.getInt("id"), 12);
    }

}
