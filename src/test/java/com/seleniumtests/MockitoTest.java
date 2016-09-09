package com.seleniumtests;

import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Redefine calls to PowerMockTestCase methods as they are not called when using TestNG groups
 * we MUST mark them as "alwaysRun"
 * @author behe
 *
 */
public class MockitoTest  extends PowerMockTestCase {

	@BeforeMethod(alwaysRun=true)  
	public void beforeMethod() throws Exception {
		beforePowerMockTestMethod();
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		MockitoAnnotations.initMocks(this); 
	}
	
	@BeforeClass(alwaysRun=true)  
	public void beforeClass() throws Exception {
		beforePowerMockTestClass();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod() throws Exception {
		afterPowerMockTestMethod();
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass() throws Exception {
		afterPowerMockTestClass();
	}
	
}
