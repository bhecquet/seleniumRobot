package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.it.core.aspects.CalcPage;
import com.seleniumtests.reporter.logger.PageLoadTime;
import com.seleniumtests.reporter.logger.TestMessage;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestPageLoadTime  extends GenericTest {

    @Test(groups={"ut"})
    public void testToJson() {
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost", new CalcPage(), 1.23);
        JSONObject json = pageLoadTime.toJson();
        Assert.assertEquals(json.getString("url"), "http://localhost");
        Assert.assertEquals(json.getDouble("loadTime"), 1.23);
        Assert.assertEquals(json.getString("page"), "CalcPage");
        Assert.assertTrue(json.getString("name").startsWith("loading of CalcPage took 1"));
    }

    @Test(groups={"ut"})
    public void testEncodeXml() {
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost", new CalcPage(), 1.23);
        PageLoadTime newPageLoadTime = pageLoadTime.encode("xml");
        Assert.assertEquals(newPageLoadTime.getUrl(), "http://localhost");
    }
}
