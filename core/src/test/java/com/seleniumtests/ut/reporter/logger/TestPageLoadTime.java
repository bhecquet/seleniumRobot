package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.it.core.aspects.CalcPage;
import com.seleniumtests.reporter.logger.PageLoadTime;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

public class TestPageLoadTime  extends GenericTest {

    @Test(groups={"ut"})
    public void testToJson() {
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost", new CalcPage(), 1230);
        JSONObject json = pageLoadTime.toJson();
        Assert.assertEquals(json.getString("url"), "http://localhost");
        Assert.assertEquals(json.getDouble("loadTime"), 1230);
        Assert.assertEquals(json.getString("page"), "CalcPage");
        Assert.assertEquals(json.getString("name"), "loading of CalcPage took 1230 ms");
    }

    @Test(groups={"ut"})
    public void testTimeStamp() {
        Instant now = Instant.now();
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost", new CalcPage(), 2500);
        Assert.assertTrue(pageLoadTime.getTimestamp().toInstant().plusMillis(2000).isBefore(now));
    }

    @Test(groups={"ut"})
    public void testEncodeXml() {
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost", new CalcPage(), 1230);
        PageLoadTime newPageLoadTime = pageLoadTime.encode("xml");
        Assert.assertEquals(newPageLoadTime.getUrl(), "http://localhost");
    }
}
