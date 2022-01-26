package com.seleniumtests.ut.core.testanalysis;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.core.testanalysis.ErrorCause;
import com.seleniumtests.core.testanalysis.ErrorType;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestStep;

public class TestErrorCause extends GenericTest {
	

	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class)
	public void testNullType() {
		new ErrorCause(null, "some changes", new TestStep("step1", null, new ArrayList<>(), true)).toString();
		
	}
	
	@Test(groups= {"ut"})
	public void testToString() {
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true)).toString();
		Assert.assertEquals(desc, "The application has been modified: some changes on step 'step1'");
	}
	
	@Test(groups= {"ut"})
	public void testToStringNoRootCause() {
		String desc = new ErrorCause(ErrorType.ERROR_MESSAGE, "some changes", new TestStep("step1", null, new ArrayList<>(), true)).toString();
		Assert.assertEquals(desc, "Error message displayed: some changes on step 'step1'");
	}
	
	@Test(groups= {"ut"})
	public void testToStringNoStep() {
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", null).toString();
		Assert.assertEquals(desc, "The application has been modified: some changes");
	}
	
	@Test(groups= {"ut"})
	public void testToStringNoStep2() {
		String desc = new ErrorCause(ErrorType.ERROR_MESSAGE, "some changes", null).toString();
		Assert.assertEquals(desc, "Error message displayed: some changes");
	}
	
	@Test(groups= {"ut"})
	public void testToStringNoDescrition() {
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, null, new TestStep("step1", null, new ArrayList<>(), true)).toString();
		Assert.assertEquals(desc, "The application has been modified on step 'step1'");
	}
	

	/**
	 * If error type is "ERROR_MESSAGE", check RootCause is displayed
	 */
	@Test(groups= {"ut"})
	public void testToStringWithRootCauseAndDetails() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true, RootCause.DEPENDENCIES, "Error from system X", false);
		step.setFailed(true);
		String desc = new ErrorCause(ErrorType.ERROR_MESSAGE, "some changes", step).toString();
		Assert.assertEquals(desc, "Error message displayed: some changes on step 'step1'\n"
				+ "Declared root Cause: DEPENDENCIES => Error from system X");
	}
	
	@Test(groups= {"ut"})
	public void testToStringWithRootCauseNoDetails() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true, RootCause.DEPENDENCIES, "", false);
		step.setFailed(true);
		String desc = new ErrorCause(ErrorType.ERROR_MESSAGE, "some changes", step).toString();
		Assert.assertEquals(desc, "Error message displayed: some changes on step 'step1'\n"
				+ "Declared root Cause: DEPENDENCIES");
	}
	
	@Test(groups= {"ut"})
	public void testToStringWithRootCauseNoDetails2() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true, RootCause.DEPENDENCIES, null, false);
		step.setFailed(true);
		String desc = new ErrorCause(ErrorType.ERROR_MESSAGE, "some changes", step).toString();
		Assert.assertEquals(desc, "Error message displayed: some changes on step 'step1'\n"
				+ "Declared root Cause: DEPENDENCIES");
	}
	
	/**
	 * If error type is "UNKNOWN_PAGE", check RootCause is displayed
	 */
	@Test(groups= {"ut"})
	public void testToStringWithRootCauseAndDetails2() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true, RootCause.DEPENDENCIES, "Error from system X", false);
		step.setFailed(true);
		String desc = new ErrorCause(ErrorType.UNKNOWN_PAGE, "some changes", step).toString();
		Assert.assertEquals(desc, "This page has never been encountered: some changes on step 'step1'\n"
				+ "Declared root Cause: DEPENDENCIES => Error from system X");
	}
	
	/**
	 * If error type is "APPLICATION_CHANGED", check RootCause is not displayed
	 */
	@Test(groups= {"ut"})
	public void testToStringWithRootCauseAndDetails3() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true, RootCause.DEPENDENCIES, "Error from system X", false);
		step.setFailed(true);
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", step).toString();
		Assert.assertEquals(desc, "The application has been modified: some changes on step 'step1'");
	}
	
	@Test(groups= {"ut"})
	public void testToStringWithRootCauseNone() {
		TestStep step = new TestStep("step1", null, new ArrayList<>(), true, RootCause.NONE, "Error from system X", false);
		step.setFailed(true);
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", step).toString();
		Assert.assertEquals(desc, "The application has been modified: some changes on step 'step1'");
	}
	
	@Test(groups= {"ut"})
	public void testEquals() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		Assert.assertEquals(cause1, cause2);
	}
	
	
	@Test(groups= {"ut"})
	public void testEqualsDifferentStep() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step2", null, new ArrayList<>(), true));
		Assert.assertNotEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsNullStep1() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", null);
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step2", null, new ArrayList<>(), true));
		Assert.assertNotEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsNullStep2() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step2", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", null);
		Assert.assertNotEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsNullStep3() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", null);
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", null);
		Assert.assertEquals(cause1, cause2);
	}
	
	
	@Test(groups= {"ut"})
	public void testEqualsDifferentDescription() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes2", new TestStep("step1", null, new ArrayList<>(), true));
		Assert.assertNotEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsNullDescription1() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, null, new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes2", new TestStep("step1", null, new ArrayList<>(), true));
		Assert.assertNotEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsNullDescription2() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, null, new TestStep("step1", null, new ArrayList<>(), true));
		Assert.assertNotEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsNullDescription3() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, null, new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.APPLICATION_CHANGED, null, new TestStep("step1", null, new ArrayList<>(), true));
		Assert.assertEquals(cause1, cause2);
	}
	
	@Test(groups= {"ut"})
	public void testEqualsDifferentType() {
		ErrorCause cause1 = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		ErrorCause cause2 = new ErrorCause(ErrorType.ERROR_IN_FIELD, "some changes", new TestStep("step1", null, new ArrayList<>(), true));
		Assert.assertNotEquals(cause1, cause2);
	}

}
