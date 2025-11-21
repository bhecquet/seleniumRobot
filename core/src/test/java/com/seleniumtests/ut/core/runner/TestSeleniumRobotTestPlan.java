package com.seleniumtests.ut.core.runner;

import java.util.*;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.Uft;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestStep;

import static org.mockito.Mockito.*;

public class TestSeleniumRobotTestPlan extends MockitoTest {

	
	@Mock
	private Uft uft;
	
	@Mock
	private TestStep currentTestStep;

	@Test(groups= {"ut"})
	public void testLoadUftScript() {

		try (MockedConstruction<Uft> mockedUft = mockConstruction(Uft.class)) {
			Uft uftInstance = new SeleniumRobotTestPlan().loadUftScript("", "", "", "", "", "", false);
			Assert.assertEquals(uftInstance, mockedUft.constructed().get(0));
			verify(mockedUft.constructed().get(0)).loadScript(false);
		}
	}
	
	@Test(groups= {"ut"})
	public void testExecuteUftScriptSuccess() {

		try (MockedStatic<TestStepManager> mockedStepManager = mockStatic(TestStepManager.class)) {
			mockedStepManager.when(TestStepManager::getCurrentRootTestStep).thenReturn(currentTestStep);

			Map<String, String> params = new HashMap<>();
			params.put("foo", "bar");

			TestStep step = new TestStep("step", "step", this.getClass(), null, new ArrayList<>(), false);

			when(uft.executeScript(5, params)).thenReturn(List.of(step));

			new SeleniumRobotTestPlan().executeUftScript(uft, 5, params);
			verify(uft).executeScript(5, params);

			// check test step is recorded
			// 1 call before the step, 1 call after
			mockedStepManager.verify(() -> TestStepManager.logTestStep(TestStepManager.getCurrentRootTestStep()), times(2));

			mockedStepManager.verify(() -> TestStepManager.setCurrentRootTestStep(step));
		}
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "UFT execution failed on script null")
	public void testExecuteUftScriptFailure() {
		
		Map<String, String> params = new HashMap<>();
		params.put("foo", "bar");
		
		TestStep step = new TestStep("step", "step", this.getClass(), null, new ArrayList<>(), false);
		step.setFailed(true);
		
		when(uft.executeScript(5, params)).thenReturn(List.of(step));
		
		new SeleniumRobotTestPlan().executeUftScript(uft, 5, params);
	}
}
