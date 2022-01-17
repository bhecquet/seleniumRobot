package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.VideoLinkInfo;
import org.junit.Assert;
import org.testng.annotations.Test;

public class TestVideoLinkInfo extends GenericTest {

    @Test(groups = {"ut"})
    public void testHtmlLink() {
        String formatHtml = new VideoLinkInfo("/videoCapture.html")
                .encode("html");
        Assert.assertEquals(formatHtml, "<a href=\"/videoCapture.html\"><i class=\"fas fa-video\" aria-hidden=\"true\"></i></a>");
    }


    @Test(groups = {"ut"})
    public void testOtherFormatLink() {
        String formatOther = new VideoLinkInfo("/videoCapture.avi")
                .encode("avi");
        Assert.assertEquals(formatOther, "Video");
    }


    @Test(groups = {"ut"})
    public void testNullFormatLink() {
        String formatNull = new VideoLinkInfo("/videoCapture.avi")
                .encode(null);
        Assert.assertEquals(formatNull, null);
    }

}

