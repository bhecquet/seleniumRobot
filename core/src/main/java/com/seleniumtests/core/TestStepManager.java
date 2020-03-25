package com.seleniumtests.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;

public class TestStepManager {
	

	List<TestStep> testSteps;
	TestStep runningStep;
	TestStep rootStep;
	
	public TestStepManager() {
		runningStep = null;
		rootStep = null;
		testSteps = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * copy the test step manager
	 * @param managerToCopy
	 */
	public TestStepManager(TestStepManager managerToCopy) {
		testSteps = new CopyOnWriteArrayList<>();
		for (TestStep step: managerToCopy.testSteps) {
			testSteps.add(step.deepCopy());
		}
		runningStep = managerToCopy.runningStep;
		rootStep = managerToCopy.rootStep;
	}
	
	/**
	 * Returns the currently running step (root step or sub-step)
	 * @return
	 */
	public TestStep getRunningTestStep() {
		return runningStep;
	}

	public void setRunningTestStep(TestStep testStep) {
		runningStep = testStep;
	}
	

	public void setRootTestStep(TestStep testStep) {
		rootStep = testStep;
		runningStep = testStep;
	}
	
	public TestStep getRootTestStep() {
		return rootStep;
	}

	/**
	 * When iterating over the list, use a 'synchronized' block on the list
	 * @return
	 */
	public List<TestStep> getTestSteps() {
		return testSteps;
	}
	

    
    /**
     * Logs the testStep for this test
     * Once logging is done, parentTestStep and currentRootTestStep are reset to avoid storing new data in them
     * @param testStep
     * @param storeStep
     */
    public static void logTestStep(TestStep testStep, boolean storeStep) {
    	List<TestAction> actionList = testStep.getStepActions();
    	
    	if (!actionList.isEmpty()) {
    		for (TestAction action: actionList) {
	    		if (action instanceof TestStep) {	
					logTestStep((TestStep)action, false);	
				} 
			}
    	}
    	
    	if (storeStep) {
    		
    		// notify each TestStepManager about the new test step (useful for AfterClass / AfterTest configuration methods)
    		for (SeleniumTestsContext testContext: SeleniumTestsContextManager.getContextForCurrentTestState()) {
    			TestStepManager stepManager = testContext.getTestStepManager();
    	    	stepManager.getTestSteps().add(testStep);
    	    	stepManager.setRootTestStep(null);
    	    	stepManager.setRunningTestStep(null);
    		}
	    	
    	}
    	
    }
    
    public static void logTestStep(TestStep testStep) {
    	TestStepManager.logTestStep(testStep, true);
    }
    
    public static void setCurrentRootTestStep(TestStep testStep) {
		try {
			SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().setRootTestStep(testStep);
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
	}
	
	public static TestStep getCurrentRootTestStep() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRootTestStep();
    	} catch (IndexOutOfBoundsException e) {
    		// null, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}
	
	public static void setParentTestStep(TestStep testStep) {
		try {
			SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().setRunningTestStep(testStep);
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
	}
	
	public static TestStep getParentTestStep() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
    	} catch (IndexOutOfBoundsException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}
	

	/**
	 * Returns the previous TestStep in the list or null if no step exists for this test
	 * @return
	 */
	public static TestStep getPreviousStep() {

		try {
			List<TestStep> allSteps = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getTestSteps();
			return allSteps.get(allSteps.size() - 1);
		} catch (Exception e) {
			return null;
		}
	}
}