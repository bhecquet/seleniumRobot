package com.seleniumtests.it.core.aspects;

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

        SeleniumTestsContextManager.getThreadContext().setReplayTimeout(10);
        LocalDateTime start = LocalDateTime.now();
        try {
            testPage._writeSomethingOnNonExistentElement();
        } catch (NoSuchElementException e) {
            // we expect this fails
        } finally {
            Assert.assertTrue(LocalDateTime.now().minusSeconds(13).isBefore(start));
            Assert.assertTrue(LocalDateTime.now().minusSeconds(10).isAfter(start));

            // check an action has been created for this
            TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
            Assert.assertEquals(step.getStepActions().size(), 2);
            Assert.assertEquals(step.getStepActions().get(0).getName(), "sendKeys on TextFieldElement Text, by={By.id: text___} with args: (true, true, [a text,], )");
            Assert.assertEquals(step.getStepActions().get(0).getAction(), "sendKeys");
            Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.textElementNotPresent);
            Assert.assertEquals(step.getStepActions().get(0).getOrigin(), DriverTestPage.class);
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

        testPage._writeSomething();

        Assert.assertTrue(LocalDateTime.now().minusSeconds(3).isBefore(start));

        // check an action has been created for this
        TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
        Assert.assertEquals(step.getStepActions().size(), 1);
        Assert.assertEquals(step.getStepActions().get(0).getName(), "sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [a text,], )");
        Assert.assertEquals(step.getStepActions().get(0).getAction(), "sendKeys");
        Assert.assertEquals(step.getStepActions().get(0).getElement(), testPage.textElement);
        Assert.assertEquals(step.getStepActions().get(0).getOrigin(), DriverTestPage.class);
        Assert.assertFalse(step.getStepActions().get(0).getFailed());
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
            Assert.assertTrue(LocalDateTime.now().minusSeconds(9).isAfter(start));

            // check object picture has not been deleted
            Assert.assertTrue(testPage.pictureNotPresent.getObjectPictureFile().exists());
            Assert.assertFalse(testPage.pictureNotPresent.getScenePictureFile().exists()); // this file has been moved

            // check an action has been created for this
            TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);
            Assert.assertEquals(step.getStepActions().size(), 2);
            Assert.assertEquals(step.getStepActions().get(0).getName(), "clickAt on Picture picture from resource tu/images/vosAlertes.png with args: (0, -30, )");
            Assert.assertTrue(step.getStepActions().get(0).getFailed());
            Assert.assertEquals(step.getFiles().size(), 2);
            Assert.assertEquals(step.getFiles().get(0).getName(), "searched picture");
            Assert.assertTrue(step.getFiles().get(0).getFile().exists());
            Assert.assertEquals(step.getFiles().get(1).getName(), "scene to search in");
            Assert.assertTrue(step.getFiles().get(1).getFile().exists());
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

        // 2 sub steps (one for each composite action)
        Assert.assertEquals(step.getStepActions().size(), 2);
        TestStep compositeAction1 = (TestStep) step.getStepActions().get(0);
        TestStep compositeAction2 = (TestStep) step.getStepActions().get(1);

        Assert.assertEquals(compositeAction1.getStepActions().get(0).getName(), "moveToElement with args: (TextFieldElement Text, by={By.id: text2}, )");
        Assert.assertEquals(compositeAction1.getStepActions().get(1).getName(), "sendKeys with args: ([composite,], )");
        Assert.assertEquals(compositeAction2.getStepActions().get(0).getName(), "moveToElement with args: (ButtonElement Reset, by={By.id: button2}, )");
        Assert.assertEquals(compositeAction2.getStepActions().get(1).getName(), "click ");
        Assert.assertFalse(compositeAction1.getStepActions().get(0).getFailed());
        Assert.assertFalse(compositeAction1.getStepActions().get(1).getFailed());

        Assert.assertFalse(compositeAction1.getFailed());
        Assert.assertFalse(compositeAction2.getFailed());
    }

    @Test(groups = {"ut"})
    public void testReplayFailedCompositeAction() {

        SeleniumTestsContextManager.getThreadContext().setReplayTimeout(10);
        LocalDateTime start = LocalDateTime.now();

        try {
            testPage._sendKeysCompositeInError();
        } catch (Exception e) {
            // we expect this fails
        }

        Assert.assertTrue(LocalDateTime.now().minusSeconds(13).isBefore(start));
        Assert.assertTrue(LocalDateTime.now().minusSeconds(10).isAfter(start));

        // check an action has been created for this
        TestStep step = SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().get(2);

        // 1 sub steps, other skipped as first one is failed
        Assert.assertEquals(step.getStepActions().size(), 1);
        TestStep compositeAction1 = (TestStep) step.getStepActions().get(0);

        // check composite actions are ALL marked as failed (we cannot know which one is causing problem)
        Assert.assertTrue(compositeAction1.getStepActions().get(0).getFailed());
        Assert.assertTrue(compositeAction1.getStepActions().get(1).getFailed());

        // check the step enclosing composite actions is failed
        Assert.assertTrue(compositeAction1.getFailed());
    }

}
