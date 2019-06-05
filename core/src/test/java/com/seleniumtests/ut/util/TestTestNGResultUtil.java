package com.seleniumtests.ut.util;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;
import com.seleniumtests.core.utils.TestNGResultUtils;

public class TestTestNGResultUtil extends MockitoTest {

	@Mock
	ITestResult testResult;
	
	@Mock
	ITestNGMethod testNGMethod;
	
	@Mock
	ITestContext testContext;
	
	@Mock
	ISuite suite;
	
	@Mock
	CucumberScenarioWrapper cucumberScenarioWrapper;
	
	@Test(groups={"ut"})
	public void testTestNameWithoutCucumber() {
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(false);
		when(testNGMethod.getMethodName()).thenReturn("testMethod");
		
		Assert.assertEquals(TestNGResultUtils.getTestName(testResult), "testMethod");
	}
	
	@Test(groups={"ut"})
	public void testBeforeTestNameWithoutCucumber() throws NoSuchMethodException, SecurityException {
		when(testResult.getParameters()).thenReturn(new Object[] {TestTestNGResultUtil.class.getDeclaredMethod("testBeforeTestNameWithoutCucumber")});
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(true);
		when(testNGMethod.getMethodName()).thenReturn("testMethod");
		
		Assert.assertEquals(TestNGResultUtils.getTestName(testResult), "before-testBeforeTestNameWithoutCucumber");
	}
	
	@Test(groups={"ut"})
	public void testTestNameWithCucumber() {
		when(testResult.getParameters()).thenReturn(new Object[] {cucumberScenarioWrapper});
		when(cucumberScenarioWrapper.toString()).thenReturn("some test");
		
		Assert.assertEquals(TestNGResultUtils.getTestName(testResult), "some test");
	}
	
	@Test(groups={"ut"})
	public void testTestNameWithNull() {
		when(testResult.getParameters()).thenReturn(new Object[] {cucumberScenarioWrapper});
		when(cucumberScenarioWrapper.toString()).thenReturn("some test");
		
		Assert.assertNull(TestNGResultUtils.getTestName(null));
	}
	
	@Test(groups={"ut"})
	public void testHashWithTestMethod() {
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testContext.getSuite()).thenReturn(suite);
		when(suite.getName()).thenReturn("mySuite");
		when(testContext.getName()).thenReturn("myTest");
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.getRealClass()).thenReturn(TestTestNGResultUtil.class);
		when(testResult.getParameters()).thenReturn(new Object[] {});
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(false);
		when(testNGMethod.getMethodName()).thenReturn("testMethod");
		
		Assert.assertEquals(TestNGResultUtils.getHashForTest(testResult), "mySuite-myTest-com.seleniumtests.ut.util.TestTestNGResultUtil-testMethod-1");
		
	}
	
	@Test(groups={"ut"})
	public void testHashWithBeforeTestMethod() throws NoSuchMethodException, SecurityException {
		when(testResult.getTestContext()).thenReturn(testContext);
		when(testContext.getSuite()).thenReturn(suite);
		when(suite.getName()).thenReturn("mySuite");
		when(testContext.getName()).thenReturn("myTest");
		when(testResult.getMethod()).thenReturn(testNGMethod);
		when(testNGMethod.getRealClass()).thenReturn(TestTestNGResultUtil.class);
		when(testResult.getParameters()).thenReturn(new Object[] {TestTestNGResultUtil.class.getDeclaredMethod("testHashWithBeforeTestMethod")});
		when(testNGMethod.isBeforeMethodConfiguration()).thenReturn(true);
		when(testNGMethod.getMethodName()).thenReturn("testHashWithBeforeTestMethod");
		
		Assert.assertEquals(TestNGResultUtils.getHashForTest(testResult), "mySuite-myTest-com.seleniumtests.ut.util.TestTestNGResultUtil-before-testHashWithBeforeTestMethod-1");
		
	}
	
	@Test(groups={"ut"})
	public void testHashWithNullResult() throws NoSuchMethodException, SecurityException {
		Assert.assertEquals(TestNGResultUtils.getHashForTest(null), "null-null-null-null-0");
		
	}
}
