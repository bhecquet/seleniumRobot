package com.seleniumtests;

import org.mockito.MockitoAnnotations;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.reporter.MockingFrameworkReporter;
import org.powermock.core.reporter.MockingFrameworkReporterFactory;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.IObjectFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

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
