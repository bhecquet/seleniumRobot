package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.LogInfo;
import com.seleniumtests.reporter.info.StringInfo;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestLogInfo extends GenericTest {

    /**
     * Test encoding for null
     */
    @Test(groups = {"ut"})
    public void testLogInfoNull() {
        Assert.assertEquals(new LogInfo(null).encode("html"), "<a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"null\"></i></a>");
    }

    /**
     * Test encoding for various formats
     */
    @Test(groups = {"ut"})
    public void testLogInfoHtml() {
        Assert.assertEquals(new LogInfo("foo <>").encode("html"), "<a class=\"errorTooltip\"><i class=\"fas fa-file-alt\" aria-hidden=\"true\" data-toggle=\"popover\" title=\"Exception\" data-content=\"foo &lt;&gt;\"></i></a>");
    }
    @Test(groups = {"ut"})
    public void testLogInfoXml() {
        Assert.assertEquals(new LogInfo("foo <>").encode("xml"), "foo &lt;&gt;");
    }
    @Test(groups = {"ut"})
    public void testLogInfoCsv() {
        Assert.assertEquals(new LogInfo("foo <,>").encode("csv"), "\"foo <,>\"");
    }
    @Test(groups = {"ut"})
    public void testLogInfoJson() {
        Assert.assertEquals(new LogInfo("foo {:/ ").encode("json"), "foo {:\\/ ");
    }
    @Test(groups = {"ut"})
    public void testLogInfoText() {
        Assert.assertEquals(new LogInfo("foo <>").encode("text"), "foo <>");
    }
    @Test(groups = {"ut"})
    public void testLogInfoOther() {
        Assert.assertEquals(new LogInfo("foo <>").encode("other"), "foo <>");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        JSONObject info = new LogInfo("foo /").toJson();
        Assert.assertEquals(info.getString("info"), "foo /");
        Assert.assertEquals(info.getString("type"), "log");
    }
    @Test(groups = {"ut"})
    public void testToJsonNull() {
        JSONObject info = new LogInfo(null).toJson();
        Assert.assertEquals(info.get("info"), JSONObject.NULL);
        Assert.assertEquals(info.getString("type"), "log");
    }

}
