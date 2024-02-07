package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.HyperlinkInfo;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestHyperlinkInfo extends GenericTest {
    /**
     * Check the link is correctly formatted
     */
    @Test(groups = {"ut"})
    public void testHyperlinkInfoHtml() {
        Assert.assertEquals(new HyperlinkInfo("foo <>", "http://foo/bar?key=value").encode("html"), "<a href=\"http://foo/bar?key=value\">foo &lt;&gt;</a>");
    }

    @Test(groups = {"ut"})
    public void testHyperlinkInfoXml() {
        Assert.assertEquals(new HyperlinkInfo("foo <>", "http://foo/bar?key=value").encode("xml"), "link http://foo/bar?key=value;info foo &lt;&gt;");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        JSONObject linkJson = new HyperlinkInfo("foo </>", "http://foo/bar?key=value").toJson();
        Assert.assertEquals(linkJson.getString("type"), "hyperlink");
        Assert.assertEquals(linkJson.getString("info"), "foo </>");
        Assert.assertEquals(linkJson.getString("link"), "http://foo/bar?key=value");
    }
}
