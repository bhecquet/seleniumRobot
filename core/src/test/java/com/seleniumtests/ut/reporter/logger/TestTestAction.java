package com.seleniumtests.ut.reporter.logger;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;

public class TestTestAction extends MockitoTest {

    @Test(groups = { "ut" })
    public void testNoPasswordMasking() throws NoSuchFieldException, IllegalAccessException {
        TestAction action = new TestAction("Login with args (user, myPass<>)", false, Arrays.asList("myPass<>"));
        Field maskPasswordField = TestAction.class.getDeclaredField("maskPassword");
        maskPasswordField.setAccessible(true);
        maskPasswordField.set(action, false);
        Assert.assertEquals(action.getName(), "Login with args (user, myPass<>)");
    }

    @Test(groups = { "ut" })
    public void testPasswordMasking() {
        TestAction action = new TestAction("Login with args (user, myPass<>)", false, Arrays.asList("myPass<>"));
        Assert.assertEquals(action.getName(), "Login with args (user, ******)");
    }

    @Test(groups = { "ut" })
    public void testNoPasswordMaskingShortPassword() {
        TestAction action = new TestAction("Login with args (user, myPas)", false, Arrays.asList("myPas"));
        Assert.assertEquals(action.getName(), "Login with args (user, myPas)");
    }

    @Test(groups = { "ut" })
    public void testNoPasswordMaskingNoList() {
        TestAction action = new TestAction("Login with args (user, myPass)", false, new ArrayList<>());
        Assert.assertEquals(action.getName(), "Login with args (user, myPass)");
    }

    @Test(groups = { "ut" })
    public void testToJson() {
        long actionTimestamp = OffsetDateTime.now().toInstant().toEpochMilli();
        TestAction action = new TestAction("Login with args (user, myPass<>)", false, Arrays.asList("myPass<>"));
        JSONObject jsonAction = action.toJson();
        Assert.assertTrue(jsonAction.getLong("timestamp") >= actionTimestamp);
        Assert.assertEquals(jsonAction.getString("type"), "action");
        Assert.assertEquals(jsonAction.getString("name"), "Login with args (user, ******)");
        Assert.assertFalse(jsonAction.has("exception"));
        Assert.assertFalse(jsonAction.has("exceptionMessage"));
        Assert.assertFalse(jsonAction.getBoolean("failed"));
        Assert.assertEquals(jsonAction.getInt("position"), 0);
        Assert.assertFalse(jsonAction.has("action"));
        Assert.assertFalse(jsonAction.has("element"));
        Assert.assertFalse(jsonAction.has("page"));
    }

    @Test(groups = { "ut" })
    public void testToJsonWithParent() {
        TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
        TestAction action = new TestAction("Login with args (user, myPass<>)", false, Arrays.asList("myPass<>"));
        action.setParent(step);
        JSONObject jsonAction = action.toJson();
        Assert.assertFalse(jsonAction.has("exception"));
        Assert.assertFalse(jsonAction.has("exceptionMessage"));
    }

    /**
     * When exception occurs, it's attached to parent test step, not the action itself
     * Check we can get this exception
     */
    @Test(groups = { "ut" })
    public void testToJsonWithParentException() {
        TestStep step = new TestStep("step1", "step1", this.getClass(), null, new ArrayList<>(), true);
        step.setActionException(new WebDriverException("some exception clicking"));
        TestAction action = new TestAction("Login with args (user, myPass<>)", false, Arrays.asList("myPass<>"));
        action.setParent(step);
        JSONObject jsonAction = action.toJson();
        Assert.assertEquals(jsonAction.getString("exception"), "org.openqa.selenium.WebDriverException");
        Assert.assertTrue(jsonAction.getString("exceptionMessage").contains("some exception clicking"));
    }

    @Test(groups = { "ut" })
    public void testToJsonWithDetailedElementInformation() {
        HtmlElement el = new HtmlElement("my element", By.id("el"));
        el.setCallingPage(Mockito.mock(DriverTestPage.class));
        TestAction action = new TestAction("click on HtmlElement By.id(\"el\")", false, Arrays.asList("myPass<>"), "click", el);
        Assert.assertEquals(action.getName(), "click on HtmlElement By.id(\"el\")");
        JSONObject jsonAction = action.toJson();
        Assert.assertEquals(jsonAction.getString("action"), "click");
        Assert.assertEquals(jsonAction.getString("element"), "my element");
        Assert.assertEquals(jsonAction.getString("origin"), "com.seleniumtests.it.driver.support.pages.DriverTestPage");
    }

    @Test(groups = { "ut" })
    public void testToJsonWithDetailedPageInformation() {

        TestAction action = new TestAction("maximizeWindow on page DriverTestPage", false, Arrays.asList("myPass<>"), "maximizeWindow", DriverTestPage.class);
        Assert.assertEquals(action.getName(), "maximizeWindow on page DriverTestPage");
        JSONObject jsonAction = action.toJson();
        Assert.assertEquals(jsonAction.getString("action"), "maximizeWindow");
        Assert.assertFalse(jsonAction.has("element"));
        Assert.assertEquals(jsonAction.getString("origin"), "com.seleniumtests.it.driver.support.pages.DriverTestPage");
    }

    @Test(groups = { "ut" })
    public void testEncode() {
        TestAction action = new TestAction("Login with args (user, myPass<>)", false, Arrays.asList("myPass<>"));
        action.setPosition(2);
        action.setDurationToExclude(20);
        TestAction encodedAction = action.encode("html");
        Assert.assertEquals(encodedAction.getPwdToReplace().get(0), "myPass&lt;&gt;");
        Assert.assertEquals(encodedAction.getName(), "Login with args (user, ******)");
        Assert.assertEquals(encodedAction.getTimestamp(), action.getTimestamp());
        Assert.assertEquals(encodedAction.getFailed(), action.getFailed());
        Assert.assertEquals(encodedAction.getPosition(), action.getPosition());
        Assert.assertEquals(encodedAction.getDurationToExclude(), action.getDurationToExclude());

    }


    @Test(groups = { "ut" })
    public void testEncodeXml() {
        TestAction action = new TestAction("action2 \"'<>&", false, new ArrayList<>());
        TestAction encodedAction = action.encode("xml");
        Assert.assertEquals(encodedAction.toString(), "action2 &quot;&apos;&lt;&gt;&amp;");
    }

    @Test(groups = { "ut" })
    public void testEncodeXmlFailedStatus() {
        TestAction action = new TestAction("action2 \"'<>&", true, new ArrayList<>());
        TestAction encodedAction = action.encode("xml");
        Assert.assertTrue(encodedAction.getFailed());
    }

    @Test(groups = { "ut" })
    public void testEncodeXmlPasswordKept() {
        TestAction action = new TestAction("action2 \"'<>&", false, Arrays.asList("myPassword"));
        TestAction encodedAction = action.encode("xml");
        Assert.assertTrue(encodedAction.getPwdToReplace().contains("myPassword"));
    }

    @Test(groups = { "ut" })
    public void testEncodeXmlExceptionKept() {
        TestAction action = new TestAction("action2 \"'<>&", false, new ArrayList<>());
        action.setActionException(new Throwable("foo"));
        TestAction encodedAction = action.encode("xml");
        Assert.assertNotNull(encodedAction.getActionException());
        Assert.assertEquals(encodedAction.getActionExceptionMessage(), "class java.lang.Throwable: foo");
    }

    @Test(groups = { "ut" })
    public void testEncodeXmlWebDriverExceptionKept() {
        TestAction action = new TestAction("action2 \"'<>&", false, new ArrayList<>());
        action.setActionException(new NoSuchElementException("foo <>"));
        TestAction encodedAction = action.encode("xml");
        Assert.assertNotNull(encodedAction.getActionException());
        Assert.assertEquals(encodedAction.getActionExceptionMessage(), "class org.openqa.selenium.NoSuchElementException: foo &lt;&gt;\n");
    }

}
