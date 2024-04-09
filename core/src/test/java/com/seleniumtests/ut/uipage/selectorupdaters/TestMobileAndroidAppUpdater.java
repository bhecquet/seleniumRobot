package com.seleniumtests.ut.uipage.selectorupdaters;

import com.seleniumtests.GenericTest;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.selectorupdaters.MobileAndroidAppUpdater;
import com.seleniumtests.uipage.selectorupdaters.MobileWebViewUpdater;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMobileAndroidAppUpdater extends GenericTest {
    @Test(groups="ut")
    public void testReplaceSelectorAppId() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.id("my.package:id/present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAppAppiumId() {
        HtmlElement el = new HtmlElement("", AppiumBy.id("present"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.id("my.package:id/present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAppAppiumAccessibilityId() {
        HtmlElement el = new HtmlElement("", AppiumBy.accessibilityId("present"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), AppiumBy.accessibilityId("present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAppTagName() {
        HtmlElement el = new HtmlElement("", By.tagName("TextField"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("TextField"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAppAppiumTagName() {
        HtmlElement el = new HtmlElement("", AppiumBy.tagName("TextField"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("TextField"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAppName() {
        HtmlElement el = new HtmlElement("", By.name("present"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("[name=present]"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAppAppiumName() {
        HtmlElement el = new HtmlElement("", AppiumBy.name("present"));
        new MobileAndroidAppUpdater("Android", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("[name=present]"));
    }

    /**
     * Do not add package an other time
     */
    @Test(groups="ut")
    public void testReplaceSelectorAndroidAppWithPackage() {
        HtmlElement el = new HtmlElement("", By.id("my.package:id/present"));
        new MobileAndroidAppUpdater("Android", "my.package", true, true).update(el);
        Assert.assertEquals(el.getBy(), By.id("my.package:id/present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAndroidWebView() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileAndroidAppUpdater("Android", "my.package", true, true).update(el);
        Assert.assertEquals(el.getBy(), By.id("present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorAndroidBrowser() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileAndroidAppUpdater("Android", "my.package", true, false).update(el);
        Assert.assertEquals(el.getBy(), By.id("present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectoriOS() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileAndroidAppUpdater("iOS", "my.package", false, true).update(el);
        Assert.assertEquals(el.getBy(), By.id("present"));
    }
}
