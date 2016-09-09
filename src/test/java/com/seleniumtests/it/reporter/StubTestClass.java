package com.seleniumtests.it.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StubTestClass {

	@Test(groups="stub")
	public void test1() {
		
	}
	
	@Test(groups="stub")
	public void testInError() {
		Assert.fail("error");
	}
}
