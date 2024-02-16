package com.seleniumtests.ut.uipage.selectorupdaters;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.selectorupdaters.ShadowDomRootUpdater;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestShadowDomRootUpdater extends GenericTest {

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndTagName() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.tagName("div"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("div"));
    }
    
    /**
     * Check selector is not replaced if we are not a child of shadow root
     */
    @Test(groups = { "ut" })
    public void testReplaceSelectorNotApplied() {
        HtmlElement el = new HtmlElement("element", By.xpath("present"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), By.xpath("present"));
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndName() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.name("foo"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), By.cssSelector("[name=foo]"));
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndId() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.id("foo"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), By.id("foo"));
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndLinkText() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.linkText("foo"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), By.linkText("foo"));
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndXId() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xId("foo"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), ByC.attribute("id", "foo"));
    }

    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndXClassName() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xClassName("foo"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndXTagName() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xTagName("foo"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndXLinkText() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xLinkText("foo"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndXName() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.xName("foo"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), ByC.attribute("name", "foo"));
    }

    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndText() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.text("foo", "div"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndLabelForward() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.labelForward("foo", "div"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndLabelBackward() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.labelBackward("foo", "div"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorWithShadowRootAndAttribute() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(ByC.attribute("foo", "bar"));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(el.getBy(), ByC.attribute("foo", "bar"));
        Assert.assertTrue(((ByC.ByAttribute)el.getBy()).isUseCssSelector()); // check we use the Css Selector instead of XPath
    }

    /**
     * Check xpath selector is not valid if we are a child of shadow root
     */
    @Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
    public void testReplaceSelectorWithShadowRootAndXpath() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"))).findElement(By.xpath("//div"));
        new ShadowDomRootUpdater().update(el);
    }

    @Test(groups = { "ut" })
    public void testReplaceSelectorInsideByCShadow() {
        HtmlElement el = new HtmlElement("element", ByC.shadow(By.id("present"), By.tagName("div"), By.name("foo"), By.className("myClass"), ByC.attribute("key", "value")));
        new ShadowDomRootUpdater().update(el);
        Assert.assertEquals(((ByC.Shadow)el.getBy()).getBies()[0], By.id("present"));
        Assert.assertEquals(((ByC.Shadow)el.getBy()).getBies()[1], By.cssSelector("div"));
        Assert.assertEquals(((ByC.Shadow)el.getBy()).getBies()[2], By.cssSelector("[name=foo]"));
        Assert.assertEquals(((ByC.Shadow)el.getBy()).getBies()[3], By.className("myClass"));
        Assert.assertEquals(((ByC.Shadow)el.getBy()).getBies()[4], ByC.attribute("key", "value"));
        Assert.assertTrue(((ByC.ByAttribute)((ByC.Shadow)el.getBy()).getBies()[4]).isUseCssSelector());
    }
}
