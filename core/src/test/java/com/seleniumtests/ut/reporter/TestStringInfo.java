package com.seleniumtests.ut.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.logger.HyperlinkInfo;
import com.seleniumtests.reporter.logger.StringInfo;

public class TestStringInfo extends GenericTest {

	/**
	 * Test encoding for various formats
	 */
	@Test
	public void testStringInfoHtml() {
		Assert.assertEquals(new StringInfo("foo <>").encode("html"), "foo &lt;&gt;");
	}
	@Test
	public void testStringInfoXml() {
		Assert.assertEquals(new StringInfo("foo <>").encode("xml"), "foo &lt;&gt;");
	}
	@Test
	public void testStringInfoCsv() {
		Assert.assertEquals(new StringInfo("foo <,>").encode("csv"), "\"foo <,>\"");
	}
	@Test
	public void testStringInfoJson() {
		Assert.assertEquals(new StringInfo("foo {:/ ").encode("json"), "foo {:\\/ ");
	}
	@Test
	public void testStringInfoText() {
		Assert.assertEquals(new StringInfo("foo <>").encode("text"), "foo <>");
	}
	@Test
	public void testStringInfoOther() {
		Assert.assertEquals(new StringInfo("foo <>").encode("other"), "foo <>");
	}
	
	/**
	 * Check the link is correctly formatted
	 */
	@Test
	public void testHyperlinkInfoHtml() {
		Assert.assertEquals(new HyperlinkInfo("foo <>", "http://foo/bar?key=value").encode("html"), "<a href=\"http://foo/bar?key=value\">foo &lt;&gt;</a>");
	}
	@Test
	public void testHyperlinkInfoXml() {
		Assert.assertEquals(new HyperlinkInfo("foo <>", "http://foo/bar?key=value").encode("xml"), "link http://foo/bar?key=value;info foo &lt;&gt;");
	}
	
}
