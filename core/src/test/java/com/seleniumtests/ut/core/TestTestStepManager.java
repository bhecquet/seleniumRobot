package com.seleniumtests.ut.core;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.helper.WaitHelper;

public class TestTestStepManager extends GenericTest {


	@Test(groups= {"ut"})
	public void testLogStep() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step1);
		
		Assert.assertNull(TestStepManager.getCurrentRootTestStep());
		Assert.assertNull(SeleniumTestsContextManager.getThreadContext().getTestStepManager().getRunningTestStep());
	}
	
	@Test(groups= {"ut"})
	public void testLogStepWithPassword() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		SeleniumTestsContextManager.getThreadContext().getTestStepManager().addPasswordToReplace("foobar");
		TestStepManager.logTestStep(step1);
	
		Assert.assertEquals(step1.getPwdToReplace().get(0), "foobar");
	}
	
	@Test(groups= {"ut"})
	public void testCurrentRootTestStepWithoutVideoTimeStamp() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.setCurrentRootTestStep(step1);
		
		Assert.assertEquals(TestStepManager.getCurrentRootTestStep(), step1);
		Assert.assertEquals(step1.getVideoTimeStamp(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testCurrentRootTestStepWithVideoTimeStamp() {
		TestStepManager.setVideoStartDate();
		WaitHelper.waitForSeconds(1); // wait a bit so that step video timestamp is not 0
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.setCurrentRootTestStep(step1);
		
		Assert.assertEquals(TestStepManager.getCurrentRootTestStep(), step1);
		Assert.assertTrue(step1.getVideoTimeStamp() > 0);
	}
	
	/**
	 * Check that when no test result exists, we get no context and not TestStepManager, but, no error is raised
	 */
	@Test(groups= {"ut"})
	public void testGetCurrentRootTestStepNoResult() {
		Reporter.setCurrentTestResult(null);
		Assert.assertNull(TestStepManager.getCurrentRootTestStep());
	}
	@Test(groups= {"ut"})
	public void testSetCurrentRootTestStepNoResult() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		Reporter.setCurrentTestResult(null);
		TestStepManager.setCurrentRootTestStep(step1);
	}
	
	/**
	 * Check we get the "Test end" step if it exists
	 */
	@Test(groups= {"ut"})
	public void testGetLastStep() {
		TestStep step1 = new TestStep("Test end", "Test end", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step1);
		
		Assert.assertEquals(TestStepManager.getInstance().getLastTestStep(), step1);
	}
	
	/**
	 * Check we get null if the "Test end" step does not exist
	 */
	@Test(groups= {"ut"})
	public void testGetNoLastStep() {
		TestStep step1 = new TestStep("step", "step", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step1);
		
		Assert.assertNull(TestStepManager.getInstance().getLastTestStep());
	}
	
	@Test(groups= {"ut"})
	public void testWithSubStep() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.setCurrentRootTestStep(step1);
		
		TestStep subStep1 = new TestStep("sub step 1", "sub step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.getParentTestStep().addStep(subStep1);
		TestStep subStep2 = new TestStep("sub step 2", "sub step 2", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.getParentTestStep().addStep(subStep2);
		TestStepManager.setParentTestStep(subStep1);
		
		Assert.assertEquals(TestStepManager.getCurrentRootTestStep(), step1);
		Assert.assertEquals(TestStepManager.getInstance().getRunningTestStep(), subStep1);
		
		// check positions have been updated
		Assert.assertEquals(step1.getPosition(), 0);
		Assert.assertEquals(subStep2.getPosition(), 1);
		
		TestStepManager.logTestStep(step1);
		
		// once logTestStep is called, root step and parent steps are null
		Assert.assertNull(TestStepManager.getCurrentRootTestStep());
		Assert.assertNull(TestStepManager.getParentTestStep());
	}
	
	/**
	 * when a current root step exists, return it
	 */
	@Test(groups= {"ut"})
	public void testGetCurrentOrPreviousStep() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step1);
		TestStep step2 = new TestStep("step 2", "step 2", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.setCurrentRootTestStep(step2);
		
		Assert.assertNotNull(TestStepManager.getCurrentRootTestStep());
		Assert.assertEquals(TestStepManager.getCurrentOrPreviousStep(), step2);
	}
	
	/**
	 * when a current root step does not exist, return the previous one
	 */
	@Test(groups= {"ut"})
	public void testGetCurrentOrPreviousStepWithoutCurrent() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step1);
		TestStep step2 = new TestStep("step 2", "step 2", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step2);
		
		Assert.assertNull(TestStepManager.getCurrentRootTestStep());
		Assert.assertEquals(TestStepManager.getCurrentOrPreviousStep(), step2);
	}
	
	/**
	 * Returns the last recorded step in the list
	 */
	@Test(groups= {"ut"})
	public void testPreviousStep() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step1);
		TestStep step2 = new TestStep("step 2", "step 2", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		TestStepManager.logTestStep(step2);
		
		Assert.assertNull(TestStepManager.getCurrentRootTestStep());
		Assert.assertEquals(TestStepManager.getPreviousStep(), step2);
	}
	
	/**
	 * Returns null when no step exist
	 */
	@Test(groups= {"ut"})
	public void testNoPreviousStep() {

		Assert.assertNull(TestStepManager.getPreviousStep());
	}
	
	
}
