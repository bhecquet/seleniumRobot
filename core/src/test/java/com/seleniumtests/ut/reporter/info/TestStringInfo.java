package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.HyperlinkInfo;
import com.seleniumtests.reporter.info.StringInfo;
import org.testng.annotations.Test;

public class TestStringInfo extends GenericTest {

    /**
     * Test encoding for null
     */
    @Test
    public void testStringInfoNull() {
        org.testng.Assert.assertNull(new StringInfo(null).encode("html"));
    }

    /**
     * Test encoding for various formats
     */
    @Test
    public void testStringInfoHtml() {
        org.testng.Assert.assertEquals(new StringInfo("foo <>").encode("html"), "foo &lt;&gt;");
    }
    @Test
    public void testStringInfoXml() {
        org.testng.Assert.assertEquals(new StringInfo("foo <>").encode("xml"), "foo &lt;&gt;");
    }
    @Test
    public void testStringInfoCsv() {
        org.testng.Assert.assertEquals(new StringInfo("foo <,>").encode("csv"), "\"foo <,>\"");
    }
    @Test
    public void testStringInfoJson() {
        org.testng.Assert.assertEquals(new StringInfo("foo {:/ ").encode("json"), "foo {:\\/ ");
    }
    @Test
    public void testStringInfoText() {
        org.testng.Assert.assertEquals(new StringInfo("foo <>").encode("text"), "foo <>");
    }
    @Test
    public void testStringInfoOther() {
        org.testng.Assert.assertEquals(new StringInfo("foo <>").encode("other"), "foo <>");
    }

    /**
     * Check the link is correctly formatted
     */
    @Test
    public void testHyperlinkInfoHtml() {
        org.testng.Assert.assertEquals(new HyperlinkInfo("foo <>", "http://foo/bar?key=value").encode("html"), "<a href=\"http://foo/bar?key=value\">foo &lt;&gt;</a>");
    }
    @Test
    public void testHyperlinkInfoXml() {
        org.testng.Assert.assertEquals(new HyperlinkInfo("foo <>", "http://foo/bar?key=value").encode("xml"), "link http://foo/bar?key=value;info foo &lt;&gt;");
    }

}
