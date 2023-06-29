package com.seleniumtests.ut.reporter.logger;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.logger.TestValue;

public class TestTestValue extends GenericTest {

	@Test(groups={"ut"})
	public void testToJson() {
		TestValue value = new TestValue("key", "message", "some_value");
		JSONObject json = value.toJson();
		Assert.assertEquals(json.getString("type"), "value");
		Assert.assertEquals(json.getString("message"), "message");
		Assert.assertEquals(json.getString("value"), "some_value");
		Assert.assertEquals(json.getString("id"), "key");
	}
	
	@Test(groups={"ut"})
	public void testEncodeHtml() {
		TestValue value = new TestValue("key", "message<>", "some<>value");
		TestValue encoded = value.encode("html");
		Assert.assertEquals(encoded.getValue(), "some&lt;&gt;value");
		Assert.assertEquals(encoded.getMessage(), "message&lt;&gt;");
	}
	
	@Test(groups={"ut"})
	public void testEncodeHtmlNull() {
		TestValue value = new TestValue("key", "message<>", null);
		TestValue encoded = value.encode("html" );
		Assert.assertEquals(encoded.getValue(), null);
		Assert.assertEquals(encoded.getMessage(), "message&lt;&gt;");
	}
}
