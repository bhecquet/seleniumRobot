package com.seleniumtests.ut.reporter;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.TestAction;
import com.seleniumtests.reporter.TestStep;

public class TestTestStep extends GenericTest {

	/**
	 * Checks getFailed correctly compute test step status if action is failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionKo() {
		TestStep step = new TestStep("step1");
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", true));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionOk() {
		TestStep step = new TestStep("step1");
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed but test step is KO
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithStepKo() {
		TestStep step = new TestStep("step1");
		step.setFailed(true);
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is  failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepKo() {
		TestStep step = new TestStep("step1");
		TestStep subStep = new TestStep("subStep");
		subStep.addAction(new TestAction("action1", true));
		step.addAction(new TestAction("action2", false));
		step.addAction(subStep);
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepOk() {
		TestStep step = new TestStep("step1");
		TestStep subStep = new TestStep("subStep");
		subStep.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertFalse(step.getFailed());
	}
}
