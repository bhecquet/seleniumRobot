package com.seleniumtests.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;

public class TestStepManager {
	
	public static final int MIN_PASSWORD_LENGTH = 5;
	public static final String LAST_STEP_NAME = "Test end";
	public static final String LAST_STATE_NAME = "Last State";
	List<TestStep> testSteps;  // list of root steps
	TestStep runningStep;
	TestStep rootStep;
	Date videoStartDate;
	private List<String> pwdToReplace;
	
	public TestStepManager() {
		runningStep = null;
		rootStep = null;
		pwdToReplace = new ArrayList<>();
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
		videoStartDate = managerToCopy.videoStartDate;
		pwdToReplace = managerToCopy.pwdToReplace;
	}
	
	/**
	 * Returns the currently running step (root step or sub-step)
	 * @return
	 */
	public TestStep getRunningTestStep() {
		return runningStep;
	}
	
	/**
	 * Get the last test step (the one names "Test end") or null if not found
	 * @return
	 */
	public TestStep getLastTestStep() {
		for (TestStep testStep: testSteps) {
			if (LAST_STEP_NAME.equals(testStep.getName())) {
				return testStep;
			}
		}
		return null;
		
	}

	public void setRunningTestStep(TestStep testStep) {
		runningStep = testStep;
	}
	

	public void setRootTestStep(TestStep testStep) {
		rootStep = testStep;
		runningStep = testStep;
		if (testStep != null) {
			testStep.setPosition(testSteps.size());
		}
	}
	
	/**
	 * Add a password to the list of strings to obfuscate
	 * @param password
	 */
	public void addPasswordToReplace(String password) {
		if (password.length() > MIN_PASSWORD_LENGTH) {
			pwdToReplace.add(password);
		}
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
    			testStep.getPwdToReplace().addAll(stepManager.pwdToReplace);
    	    	stepManager.getTestSteps().add(testStep);
    	    	stepManager.setRootTestStep(null);
    	    	stepManager.setRunningTestStep(null);
    		}
	    	
    	}
    	
    }
    
    public static void logTestStep(TestStep testStep) {
    	TestStepManager.logTestStep(testStep, true);
    }
    
    public static void setVideoStartDate() {
    	try {
    		SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().setVideoStartDate(new Date());
    	} catch (IndexOutOfBoundsException | ConfigurationException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
    }
    
    public static void setCurrentRootTestStep(TestStep testStep) {
		try {
			TestStepManager stepManager = SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager();
			stepManager.setRootTestStep(testStep);
			
			if (stepManager.getVideoStartDate() != null) {
				testStep.setVideoTimeStamp(TimeUnit.MILLISECONDS.convert(testStep.getStartDate().getTime() - stepManager.getVideoStartDate().getTime(), TimeUnit.MILLISECONDS));
			}
    	} catch (IndexOutOfBoundsException | ConfigurationException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
	}
	
	public static TestStep getCurrentRootTestStep() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRootTestStep();
    	} catch (IndexOutOfBoundsException | ConfigurationException e) {
    		// null, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}
	
	public static void setParentTestStep(TestStep testStep) {
		try {
			SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().setRunningTestStep(testStep);
    	} catch (IndexOutOfBoundsException | ConfigurationException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    	}
	}
	
	public static TestStep getParentTestStep() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager().getRunningTestStep();
    	} catch (IndexOutOfBoundsException | ConfigurationException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}
	
	/**
	 * Get the current TestStepManager for this Test
	 * Return null if none can be found
	 * @return
	 */
	public static TestStepManager getInstance() {
		try {
			return SeleniumTestsContextManager.getContextForCurrentTestState().get(0).getTestStepManager();
    	} catch (IndexOutOfBoundsException | ConfigurationException e) {
    		// do nothing, no context has been created which is the case if we try to log message in @BeforeSuite / @BeforeGroup
    		return null;
    	}
	}
	
	/**
	 * Returns the current root test step (not already recorded)
	 * If not current step exits, returns the previously recorded one, or null if none available
	 * @return
	 */
	public static TestStep getCurrentOrPreviousStep() {
		TestStep testStep = getCurrentRootTestStep();
		if (testStep == null) {
			return getPreviousStep();
		} else {
			return testStep;
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
	
	/**
	 * For all steps of the test
	 * - look for files in attachments folder and remove all of them which do not belong to any test step (except .zip)
	 * - move all attachments in the "before-xxx" folder to the main attachment folder
	 * @throws IOException 
	 */
	public void cleanAttachments(String outputDirectory) throws IOException {
		
		String outputSubDirectory = new File(outputDirectory).getName();		
		
		for (TestStep testStep: getTestSteps()) {
			testStep.moveAttachments(outputSubDirectory);
		}
	}

	public Date getVideoStartDate() {
		return videoStartDate;
	}

	public void setVideoStartDate(Date videoStartDate) {
		this.videoStartDate = videoStartDate;
	}

	/**
	 * For tests only
	 * @param testSteps
	 */
	public void setTestSteps(List<TestStep> testSteps) {
		this.testSteps = testSteps;
	}

	public List<String> getPwdToReplace() {
		return pwdToReplace;
	}
}