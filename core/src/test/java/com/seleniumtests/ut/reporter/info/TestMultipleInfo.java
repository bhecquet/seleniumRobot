package com.seleniumtests.ut.reporter.info;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.info.*;
import org.testng.Assert;
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
        Assert.assertEquals(encoded, "format cannot be null");
    }

    @Test(groups = {"ut"})
    public void testEncodeInfos() {
        MultipleInfo mInfo = new MultipleInfo("medusa.doc");
        mInfo.addInfo(new StringInfo("wandering in the waves"));
        String encoded = mInfo.encode("doc");
        Assert.assertEquals(encoded, "wandering in the waves");
    }

    @Test(groups = {"ut"})
    public void testEncodeNull() {
        MultipleInfo mInfo = new MultipleInfo("medusa.sea");
        mInfo.addInfo(new StringInfo("wandering in the waves"));
        String encoded = mInfo.encode(null);
        Assert.assertEquals(encoded, "format cannot be null");
    }

}
