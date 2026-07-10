package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.reporter.logger.Check;
import com.seleniumtests.reporter.logger.TestStep;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestTestCheck extends GenericTest {

    @Test(groups={"ut"})
    public void testToJson() {
        Check value = new Check("Check: some assertion", true);
        JSONObject json = value.toJson();
        Assert.assertEquals(json.getString("type"), "check");
        Assert.assertEquals(json.getString("messageType"), "ERROR");
        Assert.assertEquals(json.getString("name"), "Check: some assertion");
        Assert.assertTrue(json.getBoolean("failed"));
    }

    @Test(groups={"ut"})
    public void testToJsonWithPassword() {
        TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, List.of("passwd"), true);

        Check value = new Check("Check: my_message passwd", false);
        step.addCheck(value);
        JSONObject json = value.toJson();
        Assert.assertEquals(json.getString("type"), "check");
        Assert.assertEquals(json.getString("messageType"), "INFO");
        Assert.assertEquals(json.getString("name"), "Check: my_message ******");
        Assert.assertFalse(json.getBoolean("failed"));
    }

    @Test(groups={"ut"})
    public void testEncodeTo() {
        Check value = new Check("Check: my_message<>", true);
        value.setPwdToReplace(List.of("foo", "bar"));
        Check newValue = value.encodeTo("xml");
        Assert.assertEquals(newValue.getName(), "Check: my_message&lt;&gt;");
        Assert.assertEquals(newValue.getTimestamp(), value.getTimestamp());
        Assert.assertEquals(newValue.getPosition(), value.getPosition());
        Assert.assertTrue(newValue.getFailed());
        Assert.assertEquals(newValue.getPwdToReplace(), value.getPwdToReplace());
    }

    @Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class, expectedExceptionsMessageRegExp = ".*only escaping of 'xml', 'html', 'csv', 'json' is allowed.*")
    public void testEncodeToWrongFormat() {
        Check value = new Check("my_message<>", false);
        value.encodeTo("bla");
    }
}
