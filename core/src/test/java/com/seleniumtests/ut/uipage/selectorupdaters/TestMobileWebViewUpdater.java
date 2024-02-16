package com.seleniumtests.ut.uipage.selectorupdaters;

import com.seleniumtests.GenericTest;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.selectorupdaters.MobileWebViewUpdater;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMobileWebViewUpdater extends GenericTest {

    @Test(groups="ut")
    public void testReplaceSelectorWebviewId() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("#present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewAppiumId() {
        HtmlElement el = new HtmlElement("", AppiumBy.id("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("#present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewName() {
        HtmlElement el = new HtmlElement("", By.name("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("*[name='present']"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewAppiumName() {
        HtmlElement el = new HtmlElement("", AppiumBy.name("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("*[name='present']"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewClassName() {
        HtmlElement el = new HtmlElement("", By.className("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector(".present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewAppiumClassName() {
        HtmlElement el = new HtmlElement("", AppiumBy.className("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector(".present"));
    }

    /**
     * TagName not replaced
     */
    @Test(groups="ut")
    public void testReplaceSelectorWebviewTagName() {
        HtmlElement el = new HtmlElement("", By.tagName("present"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.tagName("present"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewCssSelector() {
        HtmlElement el = new HtmlElement("", By.cssSelector("div"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("div"));
    }

    @Test(groups="ut")
    public void testReplaceSelectorWebviewXPath() {
        HtmlElement el = new HtmlElement("", By.xpath("//div"));
        new MobileWebViewUpdater(true, true).update(el);
        Assert.assertEquals(el.getBy(), By.xpath("//div"));
    }

    /**
     * Selector is not replaced if we are in native view
     */
    @Test(groups="ut")
    public void testReplaceSelectorAppId() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileWebViewUpdater(false, true).update(el);
        Assert.assertEquals(el.getBy(), By.id("present"));
    }

    /**
     * Selector is not replaced if we are in web browser view
     */
    @Test(groups="ut")
    public void testReplaceSelectorBrowserId() {
        HtmlElement el = new HtmlElement("", By.id("present"));
        new MobileWebViewUpdater(true, false).update(el);
        Assert.assertEquals(el.getBy(), By.id("present"));
    }
}
