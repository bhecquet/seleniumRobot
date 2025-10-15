package com.seleniumtests.ut.uipage;

import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestPageObject extends GenericDriverTest {

	@BeforeMethod(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {

		GenericTest.resetTestNGREsultAndLogger();
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser(null);
		
	}
	
	/**
	 * issue #273: check we get a readable error message when browser type is not set
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testDriverNull() {
		new DriverTestPage(true, "http://foo.bar.com");
	}

}
