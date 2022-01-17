package com.seleniumtests.ut.core.testanalysis;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
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
	public void testToStringNoStep() {
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, "some changes", null).toString();
		Assert.assertEquals(desc, "The application has been modified: some changes");
	}
	
	@Test(groups= {"ut"})
	public void testToStringNoDescrition() {
		String desc = new ErrorCause(ErrorType.APPLICATION_CHANGED, null, new TestStep("step1", null, new ArrayList<>(), true)).toString();
		Assert.assertEquals(desc, "The application has been modified on step 'step1'");
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
