package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.reporter.logger.TestStep;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.logger.TestValue;

import java.util.List;

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
	public void testToJsonWithPassword() {
		TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, List.of("passwd", "azerty"), true);

		TestValue value = new TestValue("key passwd", "message azerty", "passwd: some_value");
		step.addValue(value);
		JSONObject json = value.toJson();
		Assert.assertEquals(json.getString("type"), "value");
		Assert.assertEquals(json.getString("message"), "message ******");
		Assert.assertEquals(json.getString("value"), "******: some_value");
		Assert.assertEquals(json.getString("id"), "key ******");
	}
	
	@Test(groups={"ut"})
	public void testEncodeHtml() {
		TestValue value = new TestValue("key", "message<>", "some<>value");
		TestValue encoded = value.encodeTo("html");
		Assert.assertEquals(encoded.getValue(), "some&lt;&gt;value");
		Assert.assertEquals(encoded.getMessage(), "message&lt;&gt;");
	}
	
	@Test(groups={"ut"})
	public void testEncodeHtmlNull() {
		TestValue value = new TestValue("key", "message<>", null);
		TestValue encoded = value.encodeTo("html" );
        Assert.assertNull(encoded.getValue());
		Assert.assertEquals(encoded.getMessage(), "message&lt;&gt;");
	}


	@Test(groups={"ut"})
	public void testEncodeTo() {
		TestValue value = new TestValue("key<", "message<>", "some<>value");
		value.setPwdToReplace(List.of("foo", "bar"));
		TestValue newValue = value.encodeTo("xml");
		Assert.assertEquals(newValue.getName(), "key&lt;");
		Assert.assertEquals(newValue.getMessage(), "message&lt;&gt;");
		Assert.assertEquals(newValue.getValue(), "some&lt;&gt;value");
		Assert.assertEquals(newValue.getTimestamp(), value.getTimestamp());
		Assert.assertEquals(newValue.getPosition(), value.getPosition());
		Assert.assertEquals(newValue.getPwdToReplace(), value.getPwdToReplace());
	}

	@Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class, expectedExceptionsMessageRegExp = ".*only escaping of 'xml', 'html', 'csv', 'json' is allowed.*")
	public void testEncodeToWrongFormat() {
		TestValue value = new TestValue("key", "message<>", "some<>value");
		value.encodeTo("bla");
	}
}
