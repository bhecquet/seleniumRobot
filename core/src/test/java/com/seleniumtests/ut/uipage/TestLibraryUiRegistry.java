package com.seleniumtests.ut.uipage;

import java.util.List;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.htmlelements.UiLibraryRegistry;

public class TestLibraryUiRegistry  extends GenericDriverTest {

	@BeforeMethod(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {

		GenericTest.resetTestNGREsultAndLogger();
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		
	}
	

	@Test(groups={"ut"})
	public void testDriverNull() throws Exception {
		new DriverTestPage(true);
		List<String> declaredUiLibraries = UiLibraryRegistry.getUiLibraries();
		Assert.assertTrue(declaredUiLibraries.contains("html"));
		Assert.assertTrue(declaredUiLibraries.contains("AngularMaterial"));
		Assert.assertTrue(declaredUiLibraries.contains("Angular"));
		Assert.assertTrue(declaredUiLibraries.contains("SalesforceLightning"));
		Assert.assertTrue(declaredUiLibraries.contains("Lightning"));
	}


}
