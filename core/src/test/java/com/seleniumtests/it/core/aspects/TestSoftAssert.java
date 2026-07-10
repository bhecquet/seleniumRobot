package com.seleniumtests.it.core.aspects;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.Check;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestStep;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestSoftAssert  extends GenericTest {

    @Test(groups={"it"})
    public void testSoftAssertEnabled() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
            new CalcPage().assertAction().add(1);
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }
        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
        TestStep failedStep = stepManager.getTestSteps().get(1);
        Assert.assertEquals(failedStep.getStepActions().getFirst().getName(), "Check: false error");
        Assert.assertEquals(failedStep.getStepActions().get(1).getName(), "!!!FAILURE ALERT!!! - Assertion Failure: false error expected [true] but found [false]");
        Assert.assertEquals(stepManager.getTestSteps().size(), 3);

    }
    @Test(groups={"it"})
    public void testSoftAssertEnabledAssertionLoggedOnce() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
            new CalcPage().assertActionNoMessage().add(1);
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }
        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
        TestStep failedStep = stepManager.getTestSteps().get(1);
        Assert.assertEquals(failedStep.getStepActions().stream().filter(a -> !(a instanceof Check)).filter(TestMessage.class::isInstance).count(), 1);
        Assert.assertEquals(failedStep.getStepActions().get(1).getName(), "!!!FAILURE ALERT!!! - Assertion Failure: expected [true] but found [false]");
        Assert.assertEquals(stepManager.getTestSteps().size(), 3);

    }

    @Test(groups={"it"})
    public void testSoftAssertDisabled() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
            new CalcPage().assertAction().add(1);
            throw new ScenarioException("Test should have failed");
        } catch (AssertionError e) {
            TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
            TestStep failedStep = stepManager.getTestSteps().get(1);
            Assert.assertEquals(failedStep.getStepActions().getFirst().getName(), "Check: false error");
            Assert.assertEquals(failedStep.getStepActions().get(1).getName(), "Assertion Failure: false error expected [true] but found [false]");
            Assert.assertEquals(stepManager.getTestSteps().size(), 2); // add step is not there
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }
    }

    @Test(groups={"it"})
    public void testSoftAssertDisabledAssertionLoggedOnce() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
            new CalcPage().assertActionNoMessage().add(1);
            throw new ScenarioException("Test should have failed");
        } catch (AssertionError e) {
            TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
            TestStep failedStep = stepManager.getTestSteps().get(1);
            Assert.assertEquals(failedStep.getStepActions().stream().filter(a -> !(a instanceof Check)).filter(TestMessage.class::isInstance).count(), 1);
            Assert.assertEquals(failedStep.getStepActions().get(1).getName(), "Assertion Failure: expected [true] but found [false]");
            Assert.assertEquals(failedStep.getStepActions().getFirst().getName(), "Check: No Check message provided");
            Assert.assertEquals(stepManager.getTestSteps().size(), 2); // add step is not there
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }
    }


    @Test(groups={"it"})
    public void testAssertionNotRaisedIsLogged() {

        new CalcPage().assertActionOk().add(1);

        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
        TestStep failedStep = stepManager.getTestSteps().get(1);
        Assert.assertEquals(failedStep.getChecks().getFirst().getName(), "Check: No error");
        Assert.assertFalse(failedStep.getChecks().getFirst().getFailed());
    }

    @Test(groups={"it"})
    public void testAssertionNotRaisedNoMessageIsLogged() {

        new CalcPage().assertActionOkNoMessage().add(1);

        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
        TestStep failedStep = stepManager.getTestSteps().get(1);
        Assert.assertEquals(failedStep.getChecks().size(), 1);
        Assert.assertEquals(failedStep.getChecks().getFirst().getName(), "Check: No Check message provided");
        Assert.assertFalse(failedStep.getChecks().getFirst().getFailed());
    }

    @Test(groups={"it"})
    public void testAssertionIsLogged() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
            new CalcPage().assertAction().add(1);
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }

        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
        TestStep failedStep = stepManager.getTestSteps().get(1);
        List<Check> allChecks = failedStep.getChecks(true);
        Assert.assertEquals(failedStep.getChecks().getFirst().getName(), "Check: false error");
        Assert.assertTrue(failedStep.getChecks().getFirst().getFailed());

        // check getChecks(true) get assertions from sub steps
        Assert.assertEquals(allChecks.size(), 1);
        Assert.assertEquals(allChecks.get(0).getName(), "Check: false error");
    }

    @Test(groups={"it"})
    public void testAssertionIsLoggedInSubStep() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
            new CalcPage().assertWithSubStep();
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }

        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();

        // verify that assertion has been logged inside the sub step
        TestStep assertWithSubStepStep = stepManager.getTestSteps().get(1);
        List<Check> allChecks = assertWithSubStepStep.getChecks(true);
        TestStep assertSubStep = (TestStep)assertWithSubStepStep.getStepActions().get(1);
        Assert.assertEquals(assertSubStep.getChecks().getFirst().getName(), "Check: false error");
        Assert.assertTrue(assertSubStep.getChecks().getFirst().getFailed());

        // check getChecks(true) get assertions from sub steps
        Assert.assertEquals(allChecks.size(), 1);
        Assert.assertEquals(allChecks.get(0).getName(), "Check: false error");


    }

    @Test(groups={"it"})
    public void testAssertionIsLoggedFromOutsidePage() {
        try {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
            new CalcPage().add(1);
            Assert.assertEquals(1, 2, "Not equal");
        } finally {
            SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
        }

        TestStepManager stepManager = SeleniumTestsContextManager.getThreadContext().getTestStepManager();
        TestStep addStep = stepManager.getTestSteps().get(1);
        Assert.assertEquals(addStep.getAction(), "add");
        Assert.assertEquals(addStep.getChecks().getFirst().getName(), "Check: Not equal");
        Assert.assertTrue(addStep.getChecks().getFirst().getFailed());
    }
}
