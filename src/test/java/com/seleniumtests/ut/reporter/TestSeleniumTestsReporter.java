package com.seleniumtests.ut.reporter;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.reporter.SeleniumTestsReporter;

public class TestSeleniumTestsReporter extends MockitoTest {
	
	@Mock
	private ITestContext context; 
	
	@Mock
	private IResultMap failedTests; 
	
	@Mock
	private IResultMap skippedTests; 
	
	private void init() {
		
		
		Mockito.when(context.getName()).thenReturn("a test");
		Mockito.when(context.getFailedTests()).thenReturn(failedTests);
		Mockito.when(context.getSkippedTests()).thenReturn(skippedTests);
	}

	@Test(groups={"ut"})
	public void testOnFinish() {
		SeleniumTestsReporter reporter = new SeleniumTestsReporter();
		init();
		reporter.onStart(context);
		reporter.onFinish(context);
		
//		Mockito.verify(reporter, Mockito.never()).removeIncorrectlySkippedTests(Mockito.any(), Mockito.any());
	}
	
//	@Test(groups={"ut"})
//	public void testOnStart() {
//		SeleniumTestsReporter reporter = new SeleniumTestsReporter();
//		reporter.onStart(null);
//	}
}
