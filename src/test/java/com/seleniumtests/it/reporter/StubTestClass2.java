package com.seleniumtests.it.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StubTestClass2 {

	@Test(groups="stub")
	public void test1() {
	}
	
	@Test(groups="stub", dependsOnGroups={"stub2"})
	public void test2() {
	}
	
	@Test(groups="stub", dependsOnMethods={"test4"})
	public void test3() {
	}
	
	@Test(groups="stub")
	public void test4() {
		Assert.fail("fail");
	}
	
	@Test(groups="stub2")
	public void test5() {
		Assert.fail("fail");
	}
	
	@Test(groups="stub", dependsOnMethods={"test3"})
	public void test6() {
	}
}
