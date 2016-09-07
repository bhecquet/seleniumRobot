package com.seleniumtests.ut.core.runner;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.CustomTestNGCucumberRunner;

public class TestCucumberRunner extends GenericTest {
	
	@BeforeMethod(alwaysRun=true)
	public void init() {
		System.setProperty("cucumberPackage", "com.seleniumTests");
	}

	@Test(groups={"ut"})
	public void testSingle(ITestContext testNGCtx) {
		try {
			System.setProperty("cucumberTags", "@new");
			SeleniumTestsContextManager.initThreadContext(testNGCtx);
			CustomTestNGCucumberRunner runner = new CustomTestNGCucumberRunner(this.getClass());
			Assert.assertEquals(runner.provideScenarios().length, 1);
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	@Test(groups={"ut"})
	public void testAnd(ITestContext testNGCtx) {
		try {
			System.setProperty("cucumberTags", "@new4 AND @new5");
			SeleniumTestsContextManager.initThreadContext(testNGCtx);
			CustomTestNGCucumberRunner runner = new CustomTestNGCucumberRunner(this.getClass());
			Assert.assertEquals(runner.provideScenarios().length, 1);
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	@Test(groups={"ut"})
	public void testOr(ITestContext testNGCtx) {
		try {
			System.setProperty("cucumberTags", "@new,@new2");
			SeleniumTestsContextManager.initThreadContext(testNGCtx);
			CustomTestNGCucumberRunner runner = new CustomTestNGCucumberRunner(this.getClass());
			Assert.assertEquals(runner.provideScenarios().length, 2);
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	@Test(groups={"ut"})
	public void testAndOr(ITestContext testNGCtx) {
		try {
			System.setProperty("cucumberTags", "@core AND @new2,@new");
			SeleniumTestsContextManager.initThreadContext(testNGCtx);
			CustomTestNGCucumberRunner runner = new CustomTestNGCucumberRunner(this.getClass());
			Assert.assertEquals(runner.provideScenarios().length, 2);
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
}
