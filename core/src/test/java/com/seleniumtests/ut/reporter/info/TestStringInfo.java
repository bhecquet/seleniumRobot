package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.StringInfo;
import com.seleniumtests.reporter.info.HyperlinkInfo;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestStringInfo extends GenericTest {

    /**
     * Test encoding for null
     */
    @Test(groups = {"ut"})
    public void testStringInfoNull() {
        Assert.assertNull(new StringInfo(null).encode("html"));
    }

    /**
     * Test encoding for various formats
     */
    @Test(groups = {"ut"})
    public void testStringInfoHtml() {
        Assert.assertEquals(new StringInfo("foo <>").encode("html"), "foo &lt;&gt;");
    }
    @Test(groups = {"ut"})
    public void testStringInfoXml() {
        Assert.assertEquals(new StringInfo("foo <>").encode("xml"), "foo &lt;&gt;");
    }
    @Test(groups = {"ut"})
    public void testStringInfoCsv() {
        Assert.assertEquals(new StringInfo("foo <,>").encode("csv"), "\"foo <,>\"");
    }
    @Test(groups = {"ut"})
    public void testStringInfoJson() {
        Assert.assertEquals(new StringInfo("foo {:/ ").encode("json"), "foo {:\\/ ");
    }
    @Test(groups = {"ut"})
    public void testStringInfoText() {
        Assert.assertEquals(new StringInfo("foo <>").encode("text"), "foo <>");
    }
    @Test(groups = {"ut"})
    public void testStringInfoOther() {
        Assert.assertEquals(new StringInfo("foo <>").encode("other"), "foo <>");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        JSONObject info = new StringInfo("foo /").toJson();
        Assert.assertEquals(info.getString("info"), "foo /");
        Assert.assertEquals(info.getString("type"), "string");
    }
    @Test(groups = {"ut"})
    public void testToJsonNull() {
        JSONObject info = new StringInfo(null).toJson();
        Assert.assertEquals(info.get("info"), JSONObject.NULL);
        Assert.assertEquals(info.getString("type"), "string");
    }

}
