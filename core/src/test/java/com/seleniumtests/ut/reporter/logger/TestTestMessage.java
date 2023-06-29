package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestValue;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    public void testEncodeXml() {
        TestMessage value = new TestMessage("my_message<>", TestMessage.MessageType.ERROR);
        TestMessage newValue = value.encode("xml");
        Assert.assertEquals(newValue.getName(), "my_message&lt;&gt;");
    }
}
