package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.util.List;


public class TestMultipleInfo extends GenericTest {

    @Test(groups = {"ut"})
    public void testInfosFull() {
        MultipleInfo mInfo = new MultipleInfo("imageCapture.html");
        mInfo.addInfo(new StringInfo("description"));
        List<Info> countInfo = mInfo.getInfos();
        Assert.assertFalse(countInfo.isEmpty());
    }

    @Test(groups = {"ut"})
    public void testaddInfoNull() {
        MultipleInfo mInfo = new MultipleInfo("medusa.sea");
        mInfo.addInfo(null);
        String encoded = mInfo.encode("html");
        Assert.assertEquals(encoded, "");
    }

    @Test(groups = {"ut"})
    public void testEncodeInfos() {
        MultipleInfo mInfo = new MultipleInfo("medusa.doc");
        mInfo.addInfo(new StringInfo("wandering in the waves"));
        String encoded = mInfo.encode("doc");
        Assert.assertEquals(encoded, "wandering in the waves");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        MultipleInfo mInfo = new MultipleInfo("imageCapture.html");
        mInfo.addInfo(new StringInfo("description"));
        mInfo.addInfo(new LogInfo("some logs"));

        JSONObject infoJson = mInfo.toJson();
        Assert.assertEquals(infoJson.getString("type"), "multipleinfo");
        JSONArray infos = infoJson.getJSONArray("infos");
        Assert.assertEquals(infos.length(), 2);
        Assert.assertEquals(infos.getJSONObject(0).getString("info"), "description");
        Assert.assertEquals(infos.getJSONObject(1).getString("info"), "some logs");
    }
    @Test(groups = {"ut"})
    public void testToJsonEmpty() {
        MultipleInfo mInfo = new MultipleInfo("imageCapture.html");

        JSONObject infoJson = mInfo.toJson();
        Assert.assertEquals(infoJson.getString("type"), "multipleinfo");
        JSONArray infos = infoJson.getJSONArray("infos");
        Assert.assertEquals(infos.length(), 0);
    }
}
