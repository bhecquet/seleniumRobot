package com.seleniumtests.ut.uipage;

import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestPageObject extends GenericTest {

	/**
	 * issue #273: check we get a readable error message when browser type is not set
	 * @throws Exception
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testDriverNull() throws Exception {
		new DriverTestPage(true, "http://foo.bar.com");
	}
}
