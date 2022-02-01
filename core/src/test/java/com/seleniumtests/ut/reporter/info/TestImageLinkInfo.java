package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.HyperlinkInfo;
import com.seleniumtests.reporter.info.ImageLinkInfo;
import com.seleniumtests.reporter.info.StringInfo;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestImageLinkInfo extends GenericTest {

    @Test(groups = {"ut"})
    public void testHtmlLink() {
        String formatHtml = new ImageLinkInfo("/imageCapture.html")
                .encode("html");
        Assert.assertEquals(formatHtml, "<a href=\"/imageCapture.html\"><i class=\"fas fa-file-image\" aria-hidden=\"true\"></i></a>");
    }


    @Test(groups = {"ut"})
    public void testOtherFormatLink() {
        String formatOther = new ImageLinkInfo("/imageCapture.png")
                .encode("avi");
        Assert.assertEquals(formatOther, "Image");
    }


    @Test(groups = {"ut"})
    public void testNullFormatLink() {
        String formatNull = new ImageLinkInfo("/imageCapture.png")
                .encode(null);
        Assert.assertEquals(formatNull, "Image");
    }

}
