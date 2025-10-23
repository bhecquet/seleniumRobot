package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestStep;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestTestMessage extends GenericTest {

    @Test(groups={"ut"})
    public void testToJson() {
        TestMessage value = new TestMessage("my_message", TestMessage.MessageType.ERROR);
        JSONObject json = value.toJson();
        Assert.assertEquals(json.getString("type"), "message");
        Assert.assertEquals(json.getString("name"), "my_message");
        Assert.assertEquals(json.getString("messageType"), "ERROR");
    }

    @Test(groups={"ut"})
    public void testToJsonWithPassword() {
        TestStep step = new TestStep("step1 with args: (bar, passwd)", "step1 with args: (bar, passwd)", this.getClass(), null, List.of("passwd"), true);

        TestMessage value = new TestMessage("my_message passwd", TestMessage.MessageType.ERROR);
        step.addMessage(value);
        JSONObject json = value.toJson();
        Assert.assertEquals(json.getString("type"), "message");
        Assert.assertEquals(json.getString("name"), "my_message ******");
        Assert.assertEquals(json.getString("messageType"), "ERROR");
    }

    @Test(groups={"ut"})
    public void testEncodeTo() {
        TestMessage value = new TestMessage("my_message<>", TestMessage.MessageType.ERROR);
        value.setPwdToReplace(List.of("foo", "bar"));
        TestMessage newValue = value.encodeTo("xml");
        Assert.assertEquals(newValue.getName(), "my_message&lt;&gt;");
        Assert.assertEquals(newValue.getTimestamp(), value.getTimestamp());
        Assert.assertEquals(newValue.getPosition(), value.getPosition());
        Assert.assertEquals(newValue.getMessageType(), value.getMessageType());
        Assert.assertEquals(newValue.getPwdToReplace(), value.getPwdToReplace());
    }

    @Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class, expectedExceptionsMessageRegExp = ".*only escaping of 'xml', 'html', 'csv', 'json' is allowed.*")
    public void testEncodeToWrongFormat() {
        TestMessage value = new TestMessage("my_message<>", TestMessage.MessageType.ERROR);
        value.encodeTo("bla");
    }
}
