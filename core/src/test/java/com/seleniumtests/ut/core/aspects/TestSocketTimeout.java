package com.seleniumtests.ut.core.aspects;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.aspects.SocketTimeout;
import com.seleniumtests.driver.WebUIDriver;

public class TestSocketTimeout extends GenericTest {

	/**
	 * Test that checks socket timeout is still applied
	 * This will inform about API change in selenium
	 */
	@Test(groups = {"ut"})
	public void testSocketTimeoutApplied(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		WebUIDriver.getWebDriver(true);
		Assert.assertTrue(SocketTimeout.isSocketTimeoutUpdated());
	}
}
