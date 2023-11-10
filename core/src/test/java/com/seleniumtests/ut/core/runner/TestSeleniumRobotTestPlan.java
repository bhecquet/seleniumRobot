package com.seleniumtests.ut.core.runner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.Uft;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestStep;

//@PrepareForTest({Uft.class, SeleniumRobotTestPlan.class, TestStepManager.class})
public class TestSeleniumRobotTestPlan extends MockitoTest {

	
	@Mock
	private Uft uft;
	
	@Mock
	private TestStep currentTestStep;
	
	@BeforeMethod(groups= {"ut"})
	public void init() throws Exception {
//		PowerMockito.mockStatic(TestStepManager.class);
//		PowerMockito.doReturn(currentTestStep).when(TestStepManager.class, "getCurrentRootTestStep");
	}

	@Test(groups= {"ut"})
	public void testLoadUftScript() throws Exception {
//		PowerMockito.whenNew(Uft.class).withAnyArguments().thenReturn(uft);
		
		
		Uft uftInstance = new SeleniumRobotTestPlan().loadUftScript("", "", "", "", "", "", false);
		Assert.assertEquals(uftInstance, uft);
		verify(uft).loadScript(false);
	}
	
	@Test(groups= {"ut"})
	public void testExecuteUftScriptSuccess() throws Exception {

		Map<String, String> params = new HashMap<>();
		params.put("foo", "bar");
		
		TestStep step = new TestStep("step", null, new ArrayList<String>(), false);
		
		when(uft.executeScript(5, params)).thenReturn(Arrays.asList(step));
		
		new SeleniumRobotTestPlan().executeUftScript(uft, 5, params);
		verify(uft).executeScript(5, params);
		
		// check test step is recorded
//		PowerMockito.verifyStatic(TestStepManager.class, times(2)); // 1 call before the step, 1 call after
		TestStepManager.logTestStep(TestStepManager.getCurrentRootTestStep());
		
//		PowerMockito.verifyStatic(TestStepManager.class);
		TestStepManager.setCurrentRootTestStep(step);
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "UFT execution failed on script null")
	public void testExecuteUftScriptFailure() throws Exception {
		
		Map<String, String> params = new HashMap<>();
		params.put("foo", "bar");
		
		TestStep step = new TestStep("step", null, new ArrayList<String>(), false);
		step.setFailed(true);
		
		when(uft.executeScript(5, params)).thenReturn(Arrays.asList(step));
		
		new SeleniumRobotTestPlan().executeUftScript(uft, 5, params);
	}
}
