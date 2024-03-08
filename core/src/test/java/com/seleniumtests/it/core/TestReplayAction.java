package com.seleniumtests.it.core;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.reporter.logger.TestStep;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This class will mainly focus on mechanisms that are not tested with TestPageObject2 / TestDriver where replay of failed actions
 * are extensively tested
 */
public class TestReplayAction extends GenericDriverTest {


    private DriverTestPage testPage;

    @BeforeMethod(groups = {"ut"})
    public void initDriver(final ITestContext testNGCtx) throws Exception {

        GenericTest.resetTestNGREsultAndLogger();
        initThreadContext(testNGCtx);
        SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
        SeleniumTestsContextManager.getThreadContext().setReplayTimeout(3);
        driver = WebUIDriver.getWebDriver(true);
        testPage = new DriverTestPage(true);

    }

    /**
     * Check replay is done
     * Check testAction is created
     */
    @Test(groups = {"ut"})
    public void testReplayFailedAction() {

        LocalDateTime start = LocalDateTime.now();
        try {
            testPage._writeSomethingOnNonExistentElement();
        } catch (NoSuchElementException e) {
            // we expect this fails
        } finally {
            Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isBefore(start));
            Assert.assertTrue(LocalDateTime.now().minusSeconds(3).isAfter(start));

            // check an action has been created for this
            TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
            Assert.assertEquals(step.getStepActions().size(), 2);
            Assert.assertEquals(step.getStepActions().get(0).getName(), "sendKeys on TextFieldElement Text, by={By.id: text___} with args: (true, true, [a text,], )");
            Assert.assertEquals(step.getStepActions().get(0).getAction(), "sendKeys");
            Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.textElementNotPresent);
            Assert.assertEquals(step.getStepActions().get(0).getPage(), DriverTestPage.class);
            Assert.assertTrue(step.getStepActions().get(0).getFailed());
        }
    }


    /**
     * Test replay is not performed if action is successful
     * Check testAction is created
     */
    @Test(groups = {"ut"})
    public void testDoNotReplaySuccessfullAction() {

        LocalDateTime start = LocalDateTime.now();
        try {
            testPage._writeSomething();
        } catch (NoSuchElementException e) {
            // we expect this fails
        } finally {
            Assert.assertTrue(LocalDateTime.now().minusSeconds(3).isBefore(start));

            // check an action has been created for this
            TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
            Assert.assertEquals(step.getStepActions().size(), 2);
            Assert.assertEquals(step.getStepActions().get(0).getName(), "sendKeys on TextFieldElement Text, by={By.id: text} with args: (true, true, [a text,], )");
            Assert.assertEquals(step.getStepActions().get(0).getAction(), "sendKeys");
            Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.textElement);
            Assert.assertEquals(step.getStepActions().get(0).getPage(), DriverTestPage.class);
            Assert.assertFalse(step.getStepActions().get(0).getFailed());
        }
    }

    /**
     * Test replay is not performed if action is successful
     * Check testAction is created
     */
    @Test(groups = {"ut"})
    public void testDoNotReplaySuccessfullPictureAction() {

        SeleniumTestsContextManager.getThreadContext().setReplayTimeout(10);
        LocalDateTime start = LocalDateTime.now();

        testPage._clickPicture();

        System.out.println(ChronoUnit.MILLIS.between(start, LocalDateTime.now()));
        // check we are under 10 seconds
        Assert.assertTrue(LocalDateTime.now().minusSeconds(6).isBefore(start));

        // check an action has been created for this
        TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
        Assert.assertEquals(step.getStepActions().size(), 3);
        Assert.assertEquals(step.getStepActions().get(0).getName(), "clickAt on Picture picture from resource tu/images/logo_text_field.png with args: (0, -30, )");
        Assert.assertEquals(step.getStepActions().get(0).getAction(), "clickAt");
        Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.picture);
        Assert.assertEquals(step.getStepActions().get(0).getPage(), DriverTestPage.class);
        Assert.assertFalse(step.getStepActions().get(0).getFailed());
    }

    @Test(groups = {"ut"})
    public void testReplayFailedPictureAction() {

        SeleniumTestsContextManager.getThreadContext().setReplayTimeout(10);
        LocalDateTime start = LocalDateTime.now();
        try {
            testPage._clickPictureNotPresent();
        } catch (ImageSearchException e) {
            // we expect this fails
        } finally {
            Assert.assertTrue(LocalDateTime.now().minusSeconds(13).isBefore(start));
            Assert.assertTrue(LocalDateTime.now().minusSeconds(10).isAfter(start));

            // check an action has been created for this
            TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
            Assert.assertEquals(step.getStepActions().size(), 2);
            Assert.assertEquals(step.getStepActions().get(0).getName(), "clickAt on Picture picture from resource tu/images/vosAlertes.png with args: (0, -30, )");
            Assert.assertEquals(step.getStepActions().get(0).getAction(), "clickAt");
            Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.pictureNotPresent);
            Assert.assertEquals(step.getStepActions().get(0).getPage(), DriverTestPage.class);
            Assert.assertTrue(step.getStepActions().get(0).getFailed());
        }
    }


    /**
     * Test replay is not performed if action is successful
     * Check testAction is created
     */
    @Test(groups = {"ut"})
    public void testDoNotReplaySuccessfullCompositeAction() {

        LocalDateTime start = LocalDateTime.now();

        testPage._sendKeysComposite();

        Assert.assertTrue(LocalDateTime.now().minusSeconds(3).isBefore(start));

        // check an action has been created for this
        TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

        Assert.assertEquals(step.getStepActions().size(), 2);
        TestStep compositeAction1 = (TestStep) step.getStepActions().get(0);
        TestStep compositeAction2 = (TestStep) step.getStepActions().get(1);

        // 2 sub steps (one for each composite action)
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getName(), "moveToElement with args: (TextFieldElement Text, by={By.id: text2}, )");
        Assert.assertEquals(compositeAction1.getStepActions().get(1).getName(), "sendKeys with args: ([composite,], )");
        Assert.assertEquals(compositeAction2.getStepActions().get(0).getName(), "moveToElement with args: (ButtonElement Reset, by={By.id: button2}, )");
        Assert.assertEquals(compositeAction2.getStepActions().get(1).getName(), "click ");
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getAction(), "moveToElement");
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getElement(), testPage.textElement);
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getPage(), DriverTestPage.class);
        Assert.assertFalse(compositeAction1.getStepActions().get(0).getFailed());
        Assert.assertEquals(compositeAction1.getStepActions().get(1).getAction(), "sendKeys");
        Assert.assertNull(compositeAction1.getStepActions().get(1).getElement());
        Assert.assertNull(compositeAction1.getStepActions().get(1).getPage());
        Assert.assertFalse(compositeAction1.getStepActions().get(1).getFailed());

        Assert.assertFalse(compositeAction1.getFailed());
        Assert.assertFalse(compositeAction2.getFailed());
    }

    @Test(groups = {"ut"})
    public void testReplayFailedCompositeAction() {

        LocalDateTime start = LocalDateTime.now();

        try {
            testPage._sendKeysCompositeInError();
        } catch (Exception e) {
            // we expect this fails
        }

        Assert.assertTrue(LocalDateTime.now().minusSeconds(3).isBefore(start));

        // check an action has been created for this
        TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

        Assert.assertEquals(step.getStepActions().size(), 1);
        TestStep compositeAction1 = (TestStep) step.getStepActions().get(0);

        // 1 sub steps, other skipped as first one is failed
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getName(), "moveToElement with args: (TextFieldElement Text, by={By.id: text2}, )");
        Assert.assertEquals(compositeAction1.getStepActions().get(1).getName(), "sendKeys with args: ([composite,], )");
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getAction(), "moveToElement");
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getElement(), testPage.textElement);
        Assert.assertEquals(compositeAction1.getStepActions().get(0).getPage(), DriverTestPage.class);
        Assert.assertFalse(compositeAction1.getStepActions().get(0).getFailed());
        Assert.assertEquals(compositeAction1.getStepActions().get(1).getAction(), "sendKeys");
        Assert.assertNull(compositeAction1.getStepActions().get(1).getElement());
        Assert.assertNull(compositeAction1.getStepActions().get(1).getPage());
        Assert.assertFalse(compositeAction1.getStepActions().get(1).getFailed());

        Assert.assertTrue(compositeAction1.getFailed());
    }

}
