package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
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
        Assert.assertTrue(json.getLong("timestamp") > 1);
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
        PageLoadTime newPageLoadTime = pageLoadTime.encodeTo("xml");
        Assert.assertEquals(newPageLoadTime.getUrl(), "http://localhost");
    }

    @Test(groups={"ut"})
    public void testEncodeTo() {
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost?a=b&c=d", new CalcPage(), 1230);
        PageLoadTime encodedPageLoad = pageLoadTime.encodeTo("xml");
        Assert.assertEquals(encodedPageLoad.getTimestamp(), pageLoadTime.getTimestamp());
        Assert.assertEquals(encodedPageLoad.getUrl(), "http://localhost?a=b&amp;c=d");
        Assert.assertEquals(encodedPageLoad.getPageObject(), pageLoadTime.getPageObject());
        Assert.assertEquals(encodedPageLoad.getLoadTime(), pageLoadTime.getLoadTime());
    }

    @Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class, expectedExceptionsMessageRegExp = ".*only escaping of 'xml', 'html', 'csv', 'json' is allowed.*")
    public void testEncodeToWrongFormat() {
        PageLoadTime pageLoadTime = new PageLoadTime("http://localhost", new CalcPage(), 1230);
        pageLoadTime.encodeTo("bla");
    }
}
